package ca.concordia.clac.penn.analysis;

import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPathFromRoot;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.getLast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.ClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.InstanceExtractor;
import ca.concordia.clac.ml.classifier.StringClassifierLabeller;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class RelationAnalysis implements ClassifierAlgorithmFactory<String, DiscourseRelation>{


	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
	}

	@Override
	public InstanceExtractor<DiscourseRelation> getExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseRelation.class);
	}
	
	public static String getPathBetweenConstituents(Pair<Constituent, Constituent> aPair, Map<Constituent, Collection<Constituent>> constituentTree){
		List<Annotation> path = getPath().apply(aPair.getFirst(), aPair.getSecond());
		if (path.isEmpty()){
			List<Constituent> firstPath = getPathFromRoot(Constituent.class, constituentTree).apply(aPair.getFirst());
			List<Constituent> secondPath = getPathFromRoot(Constituent.class, constituentTree).apply(aPair.getSecond());
			
			path.addAll(firstPath);
			path.add(null);
			path.add(null);
			path.addAll(secondPath);
		}
		
		return path.stream().map(getConstituentType()).collect(Collectors.joining("-"));
	}

	@Override
	public List<Function<DiscourseRelation, List<Feature>>> getFeatureExtractor(JCas jCas) {
		final Map<DiscourseArgument, Collection<Constituent>> coveringConstituents = 
				JCasUtil.indexCovering(jCas, DiscourseArgument.class, Constituent.class);

		final Map<Constituent, Collection<Constituent>> constituentTree = 
				JCasUtil.indexCovering(jCas, Constituent.class, Constituent.class);
		
		Function<DiscourseArgument, Constituent> getImmidateParent = 
				getPathFromRoot(DiscourseArgument.class, coveringConstituents).andThen(getLast(Constituent.class));
		Function<DiscourseRelation, Pair<Constituent, Constituent>> relationToPairArgs = (relation) ->
				new Pair<Constituent, Constituent>(
						getImmidateParent.apply(relation.getArguments(0)), 
						getImmidateParent.apply(relation.getArguments(0)));

		Function<DiscourseRelation, List<Feature>> features = multiMap(
				relationToPairArgs.andThen((pair)-> getPathBetweenConstituents(pair, constituentTree))
						.andThen(makeFeature("path"))
			);
		return Arrays.asList(features);
	}

	@Override
	public Function<DiscourseRelation, String> getLabelExtractor(JCas jCas) {
		return (relation) -> relation.getRelationType();
	}

	@Override
	public BiConsumer<String, DiscourseRelation> getLabeller(JCas jCas) {
		return null;
	}

	public static AnalysisEngineDescription getWriterDescription(File outputFld) throws ResourceInitializationException, MalformedURLException{
		return StringClassifierLabeller.getWriterDescription(
				RelationAnalysis.class,
				WekaStringOutcomeDataWriter.class, 
				outputFld
				);
	}
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, MalformedURLException, IOException {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());

		File outputs = new File("outputs/analysis/relations");
		if (outputs.exists())
			FileUtils.deleteDirectory(outputs);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(outputs)
				);
	}
}
