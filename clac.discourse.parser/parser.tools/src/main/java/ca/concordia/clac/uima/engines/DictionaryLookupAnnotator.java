package ca.concordia.clac.uima.engines;

import java.net.URL;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

public class DictionaryLookupAnnotator<T extends Annotation> extends JCasAnnotator_ImplBase{
	
	private LookupInstanceExtractor<T> lookupInstanceExtractor = new LookupInstanceExtractor<>();
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		lookupInstanceExtractor.initialize(context);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<T> instances = lookupInstanceExtractor.getInstances(aJCas);
		
		for (T discourseConnective: instances){
			discourseConnective.addToIndexes();
		}
		
	}
	
	public static <T extends Annotation> AnalysisEngineDescription getDescription(
			URL lookupfile, Class<? extends AnnotationFactory<T>> factoryClass) throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(DictionaryLookupAnnotator.class, 
				LookupInstanceExtractor.PARAM_LOOKUP_FILE_URL, lookupfile.toString(), 
				LookupInstanceExtractor.PARAM_ANNOTATION_FACTORY_CLASS_NAME, factoryClass.getName());
	}

}
