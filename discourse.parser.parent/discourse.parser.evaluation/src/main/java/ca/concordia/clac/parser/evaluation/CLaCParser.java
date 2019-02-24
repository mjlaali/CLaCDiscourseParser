package ca.concordia.clac.parser.evaluation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllJSonGoldExporter;
import org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler;
import org.discourse.parser.implicit.NoRelationAnnotator;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class CLaCParser {

	public AnalysisEngineDescription getParser() throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(new DiscourseConnectiveDisambiguator().getParser(CAS.NAME_DEFAULT_SOFA));
		builder.add(ArgumentSequenceLabeler.getClassifierDescription());
		return builder.createAggregateDescription();
	}

	public AnalysisEngineDescription getPreprocessEngines() throws ResourceInitializationException{
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(createEngineDescription(OpenNlpSegmenter.class));
		builder.add(createEngineDescription(OpenNlpPosTagger.class));
		builder.add(createEngineDescription(BerkeleyParser.class, 
				BerkeleyParser.PARAM_MODEL_LOCATION, 
				"classpath:/clacParser/model/eng_sm5.gr"));
		return builder.createAggregateDescription();
	}

	public AnalysisEngineDescription getStandaloneParser() throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(getPreprocessEngines());
		builder.add(getParser());

		return builder.createAggregateDescription();
	}
	
	
	public interface Options{
		@Option(
				defaultToNull = true,
				shortName = "i",
				longName = "inputDataset", 
				description = "Specify the input directory")
		public String getInputDataset();
		
		@Option(
				shortName = "o",
				longName = "outputDir",
				description = "Specify the output directory to stores extracted texts")
		public String getOutputDir();
	}

	public static void main(String[] args) throws Exception {
		Options options = CliFactory.parseArguments(Options.class, args);

		String inputDirectory = options.getInputDataset();
		String outputDirectory = options.getOutputDir();
		CollectionReaderDescription reader;
		AnalysisEngineDescription pipeline;

		reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, inputDirectory, 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "*");
		AggregateBuilder builder = new AggregateBuilder();

		//add parser
		builder.add(new CLaCParser().getStandaloneParser());
		
		AnalysisEngineDescription jsonExporter = ConllJSONExporter.getDescription(new File(outputDirectory, "output.json").getAbsolutePath());		
		AnalysisEngineDescription noRelDetector = NoRelationAnnotator.getDescription();
		AnalysisEngineDescription noRelExporter = ConllJSonGoldExporter.getDescription(new File(outputDirectory, "no-relation.json"), "Explicit");


		//add console writer
		pipeline = builder.createAggregateDescription();

		SimplePipeline.runPipeline(
				reader, 
				pipeline,
				jsonExporter,
				noRelDetector, 
				noRelExporter);
	}
}
