package ca.concordia.clac.parser.evaluation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

public class TryParser {

	CollectionReaderDescription reader;
	AnalysisEngineDescription pipeline;
	
	public void initialize() throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		reader = CollectionReaderFactory.createReaderDescription(TerminalReader.class);
		AggregateBuilder builder = new AggregateBuilder();
		
    	//add parser
    	builder.add(new CLaCParser().getStandaloneParser());

    	//add console writer
    	builder.add(createEngineDescription(TerminalOutputWriter.class));
    	pipeline = builder.createAggregateDescription();
	}
	
	public void run() throws UIMAException, IOException {
		SimplePipeline.runPipeline(reader, pipeline);
	}
	
	public static void main(String[] args) throws UIMAException, IOException, URISyntaxException {
		System.out.println("Please write your sentence or just enter to exit:");
		TryParser parser = new TryParser();
		parser.initialize();
		parser.run();
	}
}
