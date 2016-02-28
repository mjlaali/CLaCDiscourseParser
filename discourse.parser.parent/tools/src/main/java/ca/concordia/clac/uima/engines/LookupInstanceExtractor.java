package ca.concordia.clac.uima.engines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.component.initialize.ExternalResourceInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.assertj.core.util.VisibleForTesting;

import ca.concordia.clac.ml.classifier.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

class DefaultLoader implements LookupInstanceExtractor.Loader{

	List<String> readLines(URL url, Charset encoding) throws IOException{
		BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
		String line = null;
		List<String> lines = new ArrayList<>();
		while ((line = input.readLine()) != null){
			lines.add(line);
		}
		return lines;
	}
	
	@Override
	public List<String> load(URL file) throws IOException {
		
		Charset utf8 = StandardCharsets.UTF_8;
		List<String> terms = readLines(file, utf8);
		Collections.sort(terms, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
		});
		String smallestDc = terms.get(terms.size() - 1);
		int smallestDcLength = smallestDc.length();
		if (smallestDcLength == 0 || smallestDcLength > 3){
			throw new RuntimeException("Are you sure the content of the discourse connective list is correct: " + smallestDc);
		}
		return terms;
	}
	
}

public class LookupInstanceExtractor<T extends Annotation> implements Initializable, InstanceExtractor<T>{
	public static final String PARAM_LOOKUP_FILE_URL = "lookupFileUrl";
	public static final String PARAM_ANNOTATION_FACTORY_CLASS_NAME = "annotationFactoryClassName";
	
	public static interface Loader{
		public List<String> load(URL file) throws IOException;
	}
	
	@ConfigurationParameter(name=PARAM_LOOKUP_FILE_URL)
	private URL lookupFileUrl;
	
	@ConfigurationParameter(name=PARAM_ANNOTATION_FACTORY_CLASS_NAME)
	private String annotationFactoryClassName;
	
	private AnnotationFactory<T> annotationFactory;
	
	private List<String> terms;
	private Loader loader;

	public LookupInstanceExtractor() {
		loader = new DefaultLoader();
	}
	
	public LookupInstanceExtractor(Loader loader) {
		this.loader = loader;
	}
	
	@VisibleForTesting
	public void init(List<String> terms, AnnotationFactory<T> annotationFactory) {
		this.terms = terms;
		this.annotationFactory = annotationFactory;
	}
	
	
	
	public static TreeMap<Integer, Integer> coverToTokens(JCas aJCas, Class<? extends Annotation> cls){
		TreeMap<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		Collection<? extends Annotation> words = JCasUtil.select(aJCas, cls);
		for (Annotation ann: words){
			indexes.put(ann.getBegin(), ann.getEnd());
		}
		return indexes;
	}

	Map<Integer, Integer> getOccurrence(String text, TreeMap<Integer, Integer> words) {
		TreeMap<Integer, Integer>  intervals = new TreeMap<Integer, Integer>();
		for (String term: terms){
			
			int start = text.indexOf(term);
			int end = start + term.length();
			while (start != -1){
				if (!containedInPrevCandid(start, end, intervals) && checkBoundray(start, end, words)){
					intervals.put(start, end);
				}
				
				start = text.indexOf(term, end);
				end = start + term.length();
			}
		}

		return intervals;
		
	}
	
	private boolean checkBoundray(int indexOfDC, int endOfDc, TreeMap<Integer, Integer> words) {
		boolean validBegin = words.get(indexOfDC) != null;
		Entry<Integer, Integer> closestWord = words.floorEntry(endOfDc - 1);
		boolean validEnd = closestWord != null && closestWord.getValue() == endOfDc;
		return validBegin && validEnd;
	}

	/**
	 * Check if this interval is not sub interval of previous ones. 
	 * @param indexOfDC
	 * @param endOfDc
	 * @return
	 */
	private boolean containedInPrevCandid(int indexOfDC, int endOfDc, TreeMap<Integer, Integer> dcIntervals) {
		Integer floorKey = dcIntervals.floorKey(indexOfDC);
		if (floorKey == null || dcIntervals.get(floorKey) < indexOfDC){
			return false;
		}
		return true;
	}

	@Override
	public Collection<T> getInstances(JCas aJCas) {
		String documentText = aJCas.getDocumentText().toLowerCase();

		List<T> candidates = new ArrayList<>();
		Map<Integer, Integer> occurrences = getOccurrence(documentText, coverToTokens(aJCas, Token.class));
		for (Entry<Integer, Integer> occurence: occurrences.entrySet()){
			int indexOfDC = occurence.getKey();
			int endOfDc = occurence.getValue();
			T discourseConnective = annotationFactory.buildAnnotation(aJCas, indexOfDC, endOfDc);
			candidates.add(discourseConnective);
		}
		return candidates;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
	    ConfigurationParameterInitializer.initialize(this, context);
	    ExternalResourceInitializer.initialize(this, context);
	    
	    try {
			terms = loader.load(lookupFileUrl);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	    
	   annotationFactory = InitializableFactory.create(context, annotationFactoryClassName, AnnotationFactory.class);
	}
}

