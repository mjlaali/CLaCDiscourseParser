package org.parser.dc.disambiguation;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeatures;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getText;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.extractFromScope;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.getLast;
import static ca.concordia.clac.ml.scop.Scopes.getPathToRoot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.ClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.GenericClassifierLabeller;
import ca.concordia.clac.ml.classifier.InstanceExtractor;
import ca.concordia.clac.ml.classifier.StringClassifierLabeller;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;


public class DiscourseVsNonDiscourseClassifier implements ClassifierAlgorithmFactory<String, DiscourseConnective>{

	private LookupInstanceExtractor lookupInstanceExtractor = new LookupInstanceExtractor();
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		lookupInstanceExtractor.initialize(context);
	}

	@Override
	public InstanceExtractor<DiscourseConnective> getExtractor() {
		return lookupInstanceExtractor;
	}

	
	@Override
	public List<Function<DiscourseConnective, List<Feature>>> getFeatureExtractor() {
		Function<DiscourseConnective, List<Feature>> pathFeatures = getPathToRoot(DiscourseConnective.class)
				.andThen(extractFromScope(
				 getLast(Constituent.class).andThen(
						 getFeatures(getFeature("selfCat", getConstituentType()),
								 	 getFeature("selfCatParent", getParent().andThen(getConstituentType())),
								 	 getFeature("selfCatLeftSibling", getLeftSibling().andThen(getConstituentType())),
						 			 getFeature("selfCatLeftSibling", getRightSibling().andThen(getConstituentType()))
						 )) 
			));
		
		
		return Arrays.asList(
				getFeatures(getFeature("CON-LStr", getText(DiscourseConnective.class).andThen(String::toLowerCase)), 
						    getFeature("CON-POS", getText(DiscourseConnective.class)
						    		.andThen(StringUtils::isAllLowerCase)
						    		.andThen((b) -> b.toString()))), 
				pathFeatures);
	}

	@Override
	public Function<DiscourseConnective, String> getLabelExtractor() {
		return (dc) -> {
			List<DiscourseConnective> selectCovered = JCasUtil.selectCovering(DiscourseConnective.class, dc);
			for (DiscourseConnective indexedDc: selectCovered){
				if (indexedDc.getBegin() == dc.getBegin() &&
					indexedDc.getEnd() == dc.getEnd()){
					return Boolean.toString(true);
				}
			}
			return Boolean.toString(false);
		};
	}

	@Override
	public BiConsumer<String, DiscourseConnective> getLabeller() {
		return (label, dc) ->{
			boolean toAdd = Boolean.parseBoolean(label);
			if (toAdd)
				dc.addToIndexes();
		};
	}

	
	public static AnalysisEngineDescription getEngineDescription(File dcList, File outputFld) throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(StringClassifierLabeller.class, 
				GenericClassifierLabeller.PARAM_LABELER_CLS_NAME, DiscourseVsNonDiscourseClassifier.class.getName(), 
				CleartkAnnotator.PARAM_IS_TRAINING, true,
				LookupInstanceExtractor.PARAM_LOOKUP_FILE, dcList,
				DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, MaxentStringOutcomeDataWriter.class.getName(), 
				DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputFld);
	}
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset(new File("../conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getDataJSonFile());
		
		File dcList = new File("resources/dcHeadList.txt");
		File featureFile = new File("outputs/discourse-vs-nondiscourse");
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getEngineDescription(dcList, featureFile)
						);
		
		
	}
}
