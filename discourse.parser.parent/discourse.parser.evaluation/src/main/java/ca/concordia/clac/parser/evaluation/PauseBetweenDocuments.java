package ca.concordia.clac.parser.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class PauseBetweenDocuments extends JCasAnnotator_ImplBase{
	private Scanner scanner;
	private PrintStream output;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		scanner = new Scanner(System.in);
		output = System.out;
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		output.println("Go to the next document, please enter to continue:");
		scanner.nextLine();
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(PauseBetweenDocuments.class);
	}
	public static void main(String[] args) throws UIMAException, IOException {
		
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset(new File("../discourse.conll.dataset/data"), DatasetMode.trial);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());
		
		AnalysisEngineDescription terminalOutput = TerminalOutputWriter.getDescription(null);
		AnalysisEngineDescription pause = getDescription();

		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				terminalOutput,
				pause
				);
		
	}

}
