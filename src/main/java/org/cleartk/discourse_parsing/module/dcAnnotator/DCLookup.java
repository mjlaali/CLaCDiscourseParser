package org.cleartk.discourse_parsing.module.dcAnnotator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public class DCLookup {
	private List<String> terms;

	public void setTerms(List<String> terms) {
		this.terms = terms;
	}
	
	public List<String> loadDC(String filePath) throws IOException{
		terms = FileUtils.readLines(new File(filePath), StandardCharsets.UTF_8);
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
	
	public static TreeMap<Integer, Integer> coverToTokens(JCas aJCas, Class<? extends Annotation> cls){
		TreeMap<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		Collection<? extends Annotation> words = JCasUtil.select(aJCas, cls);
		for (Annotation ann: words){
			indexes.put(ann.getBegin(), ann.getEnd());
		}
		return indexes;
	}

	public Map<Integer, Integer> getOccurrence(String text, TreeMap<Integer, Integer> words) {
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

	

}
