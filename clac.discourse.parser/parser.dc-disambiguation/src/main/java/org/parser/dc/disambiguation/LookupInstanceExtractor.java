package org.parser.dc.disambiguation;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.component.initialize.ExternalResourceInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;

import ca.concordia.clac.ml.classifier.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

class DefaultLoader implements LookupInstanceExtractor.Loader{

	@Override
	public List<String> load(File file) throws IOException {
		List<String> terms = FileUtils.readLines(file, StandardCharsets.UTF_8);
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

public class LookupInstanceExtractor implements Initializable, InstanceExtractor<DiscourseConnective>{
	public static final String PARAM_LOOKUP_FILE = "lookupFile";
	
	public static interface Loader{
		public List<String> load(File file) throws IOException;
	}
	
	@ConfigurationParameter(name=PARAM_LOOKUP_FILE)
	private File lookupFile;
	private List<String> terms;
	private Loader loader;

	public LookupInstanceExtractor() {
		loader = new DefaultLoader();
	}
	
	public LookupInstanceExtractor(Loader loader) {
		this.loader = loader;
	}
	
	public void setTerms(List<String> terms) {
		this.terms = terms;
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
	public Collection<DiscourseConnective> getInstances(JCas aJCas) {
		String documentText = aJCas.getDocumentText().toLowerCase();

		List<DiscourseConnective> candidates = new ArrayList<>();
		Map<Integer, Integer> occurrences = getOccurrence(documentText, coverToTokens(aJCas, Token.class));
		for (Entry<Integer, Integer> occurence: occurrences.entrySet()){
			int indexOfDC = occurence.getKey();
			int endOfDc = occurence.getValue();
			DiscourseConnective discourseConnective = new DiscourseConnective(aJCas);
			List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, indexOfDC, endOfDc);
			TokenListTools.initTokenList(aJCas, discourseConnective, tokens);
			candidates.add(discourseConnective);
		}
		return candidates;
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
	    ConfigurationParameterInitializer.initialize(this, context);
	    ExternalResourceInitializer.initialize(this, context);
	    
	    try {
			terms = loader.load(lookupFile);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
}

