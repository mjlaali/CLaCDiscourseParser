package ca.concordia.clac.uima.engines;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

public class LookupInstanceAnnotator<T extends Annotation> extends JCasAnnotator_ImplBase{

	private LookupInstanceExtractor<T> lookupInstanceExtractor = new LookupInstanceExtractor<>();

	public static <T extends Annotation> AnalysisEngineDescription getDescription(File dictFile, Class<? extends AnnotationFactory<T>> annotaitonFactory) throws ResourceInitializationException, MalformedURLException{
		return getDescription(dictFile.toURI().toURL(), annotaitonFactory);
	}
	public static <T extends Annotation> AnalysisEngineDescription getDescription(URL dictFile, Class<? extends AnnotationFactory<T>> annotaitonFactory) 
			throws ResourceInitializationException, MalformedURLException {
		return AnalysisEngineFactory.createEngineDescription(LookupInstanceAnnotator.class, 
				LookupInstanceExtractor.PARAM_LOOKUP_FILE_URL, dictFile.toString(),
				LookupInstanceExtractor.PARAM_ANNOTATION_FACTORY_CLASS_NAME, annotaitonFactory.getName()
				);
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		lookupInstanceExtractor.initialize(context);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<T> select = new ArrayList<T>(JCasUtil.select(aJCas, lookupInstanceExtractor.getAnnotationType()));
		for (T t: select){
			t.removeFromIndexes();
		}
		Collection<T> instances = lookupInstanceExtractor.getInstances(aJCas);
		
		for (T instance: instances)
			instance.addToIndexes();
	}

}
