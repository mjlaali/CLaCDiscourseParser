package ca.concordia.clac.discourse.parser.dc.disambiguation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
		return (dc) -> dc.getSense();
	}

	@Override
	public BiConsumer<String, DiscourseConnective> getLabeller(JCas aJCas) {
		return (sense, dc) -> dc.setSense(sense);
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
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getDataJSonFile());
		
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
