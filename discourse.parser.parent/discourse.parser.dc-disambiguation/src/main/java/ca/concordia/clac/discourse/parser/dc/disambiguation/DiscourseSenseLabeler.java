package ca.concordia.clac.discourse.parser.dc.disambiguation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.ClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.GenericClassifierLabeller;
import ca.concordia.clac.ml.classifier.InstanceExtractor;
import ca.concordia.clac.ml.classifier.StringClassifierLabeller;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class DiscourseSenseLabeler implements ClassifierAlgorithmFactory<String, DiscourseConnective>{
	public static final String PACKAGE_DIR = "discourse-sense-labeler/";
	public static final Set<String> validSenses = new HashSet<>(Arrays.asList("Comparison.Concession", "Comparison.Contrast", 
			"Contingency.Cause.Reason", "Contingency.Cause.Result", "Contingency.Condition", "Expansion.Alternative", 
			"Expansion.Alternative.Chosen alternative", "Expansion.Conjunction", "Expansion.Exception", "Expansion.Instantiation",
			"Expansion.Restatement", "Temporal.Asynchronous.Precedence", "Temporal.Asynchronous.Succession", "Temporal.Synchrony"));
	
	public static final List<String> frequentSense = Arrays.asList("Comparison.Contrast", "Contingency.Condition", 
			"Expansion.Conjunction", "Temporal.Synchrony", "Temporal.Asynchronous.Succession");
	
	

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
	}

	@Override
	public InstanceExtractor<DiscourseConnective> getExtractor(JCas aJCas) {
		return (jCas) -> JCasUtil.select(jCas, DiscourseConnective.class);
	}

	@Override
	public List<Function<DiscourseConnective, List<Feature>>> getFeatureExtractor(JCas aJCas) {
		return new DiscourseVsNonDiscourseClassifier().getFeatureExtractor(aJCas);
	}

	@Override
	public Function<DiscourseConnective, String> getLabelExtractor(JCas aJCas) {
		return (dc) -> {
			String sense = dc.getSense();
			if (validSenses.contains(sense))
				return sense;
			
			for (String aSense: frequentSense){
				if (sense.contains(aSense) || aSense.contains(sense)){
					return aSense;
				}
			}
			
//			String category = sense.substring(0, sense.indexOf('.'));
//			for (String aSense: frequentSense){
//				if (aSense.contains(category)){
//					return aSense;
//				}
//			}
			
			System.err.println("DiscourseSenseLabeler.getLabelExtractor(): Not a valid sense = " + sense);
			return sense;
		};
	}

	@Override
	public BiConsumer<String, DiscourseConnective> getLabeller(JCas aJCas) {
		return (sense, dc) -> {
			sense = sense.replaceAll("'", "");
			dc.setSense(sense);
			DiscourseRelation relation = dc.getDiscourseRelation();
			if (relation != null)
				relation.setSense(sense);
		};
	}

	public static AnalysisEngineDescription getWriterDescription(File outputFld) throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(StringClassifierLabeller.class, 
				GenericClassifierLabeller.PARAM_LABELER_CLS_NAME, DiscourseSenseLabeler.class.getName(), 
				DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, WekaStringOutcomeDataWriter.class.getName(), 
				DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputFld);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(URL packageDir) throws ResourceInitializationException, MalformedURLException {
		return AnalysisEngineFactory.createEngineDescription(StringClassifierLabeller.class, 
				GenericClassifierLabeller.PARAM_LABELER_CLS_NAME, DiscourseSenseLabeler.class.getName(), 
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, new URL(packageDir, "model.jar").toString()
				);
	}
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());
		
		File featureFile = new File(new File("outputs/resources"), PACKAGE_DIR);
		if (featureFile.exists())
			FileUtils.deleteDirectory(featureFile);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(featureFile)
						);
	}

	
}
