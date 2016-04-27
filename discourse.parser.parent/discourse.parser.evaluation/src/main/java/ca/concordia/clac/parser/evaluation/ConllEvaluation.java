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
import org.cleartk.corpus.conll2015.ConllJSonGoldExporter;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler;
import org.discourse.parser.implicit.NoRelationAnnotator;

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
				description = "Specify the mode {dev, trial, test}")
		public String getMode();
		
		@Option(
				shortName = "o",
				longName = "outputDir",
				description = "Specify the output directory to stores extracted texts")
		public String getOutputDir();
	}
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException, URISyntaxException {
		Options options = CliFactory.parseArguments(Options.class, args);
		
		DatasetMode mode = DatasetMode.dev;
		String inputDataset = null;
		File outputDirectory = null;
		
		if (options.getMode() != null)
			mode = DatasetMode.valueOf(options.getMode());
		
		if (options.getInputDataset() == null){
			inputDataset = "../discourse.conll.dataset/data";
		} else {
			inputDataset = options.getInputDataset();
		}
		outputDirectory = new File(options.getOutputDir());
		
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File(inputDataset), mode);
		if (dataset == null)
			throw new RuntimeException();
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription dcDisambiguator = new DiscourseConnectiveDisambiguator().getParser(CAS.NAME_DEFAULT_SOFA);
		
		AnalysisEngineDescription argumentLabeler = ArgumentSequenceLabeler.getClassifierDescription();
		
		AnalysisEngineDescription jsonExporter = ConllJSONExporter.getDescription(new File(outputDirectory, "output.json").getAbsolutePath());
		
		AnalysisEngineDescription noRelDetector = NoRelationAnnotator.getDescription();
		AnalysisEngineDescription noRelExporter = ConllJSonGoldExporter.getDescription(new File(outputDirectory, "no-relation.json"), "Explicit");
				
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				dcDisambiguator, 
				argumentLabeler,
				jsonExporter,
				noRelDetector, 
				noRelExporter
				);
	}
}
