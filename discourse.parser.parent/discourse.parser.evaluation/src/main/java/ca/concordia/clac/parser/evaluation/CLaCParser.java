package ca.concordia.clac.parser.evaluation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;
import org.discourse.parser.argument_labeler.argumentLabeler.ArgumentSequenceLabeler;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class CLaCParser {

	public AnalysisEngineDescription getParser(String goldView, String systemView) throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(new DiscourseConnectiveDisambiguator().getDCDisambiguator(goldView, systemView));
		builder.add(ArgumentSequenceLabeler.getClassifierDescription(goldView, systemView));
		return builder.createAggregateDescription();
	}
	
	public AnalysisEngineDescription getParser() throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(new DiscourseConnectiveDisambiguator().getDCDisambiguator(CAS.NAME_DEFAULT_SOFA));
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
}
