package ca.concordia.clac.parser.evaluation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.cpe.CpeBuilder;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler;

import ca.concordia.clac.batch_process.StatusCallbackListenerImpl;
import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

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

	public static void main(String[] args) throws Exception {
		File inputDir = new File("outputs/texts");
		//		File outputDir = new File("");
		long start = new Date().getTime();
		int threadCount = 8;

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, inputDir,
				TextReader.PARAM_LANGUAGE, "en", 
				TextReader.PARAM_PATTERNS, "*");

		CpeBuilder builder=new CpeBuilder();
		builder.setReader(reader);
		AnalysisEngineDescription seg = AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class);
		AnalysisEngineDescription parse = AnalysisEngineFactory.createEngineDescription(StanfordParser.class);
		builder.setAnalysisEngine(AnalysisEngineFactory.createEngineDescription(
				seg, parse
				//				TerminalOutputWriter.getDescription(null)
				));
		builder.setMaxProcessingUnitThreadCount(threadCount);

		StatusCallbackListenerImpl status = new StatusCallbackListenerImpl();
		CollectionProcessingEngine engine = builder.createCpe(status);
		engine.process();
		try {
			synchronized (status) {
				while (status.isProcessing()) {
					status.wait();
				}
			}
		}
		catch (InterruptedException e) {
			// Do nothing
		}

		if (status.getExceptions().size() > 0) {
			throw status.getExceptions().get(0);
		}

		long end = new Date().getTime();
		System.out.println("CLaCParser.main() " + (end - start));

	}
}
