package ca.concordia.clac.parser.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ConllEvaluation {

	public interface Options{
		@Option(
				defaultToNull = true,
				shortName = "i",
				longName = "inputDataset", 
				description = "Specify the input directory")
		public String getInputDataset();
		
		@Option(
				defaultToNull = true,
				shortName = "m",
				longName = "mode", 
				description = "Specify the mode {dev, trial}")
		public String getMode();
		
		@Option(
				shortName = "o",
				longName = "outputDir",
				description = "Specify the output directory to stores extracted texts")
		public String getOutputDir();
	}
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException, URISyntaxException {
		Options options = CliFactory.parseArguments(Options.class, args);
		
		DatasetMode mode = DatasetMode.test;
		String inputDataset = null;
		File outputDirectory = null;
		if (options.getInputDataset() == null){
			mode = DatasetMode.valueOf(options.getMode());
			inputDataset = "../discourse.conll.dataset/data";
			outputDirectory = new File("outputs/" + mode + "/");
		} else {
			inputDataset = options.getInputDataset();
			outputDirectory = new File(options.getOutputDir());
		}
		
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset(new File(inputDataset), mode);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription dcDisambiguator = new DiscourseConnectiveDisambiguator().getParser(CAS.NAME_DEFAULT_SOFA);
		
		AnalysisEngineDescription argumentLabeler = ArgumentSequenceLabeler.getClassifierDescription(ArgumentSequenceLabeler.DEFAULT_URL);
		
		
		AnalysisEngineDescription jsonExporter = ConllJSONExporter.getDescription(new File(outputDirectory, "ptdb-data.json").getAbsolutePath());
				
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				dcDisambiguator, 
				argumentLabeler,
				jsonExporter
				);
	}
}
