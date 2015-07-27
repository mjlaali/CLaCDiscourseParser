package org.cleartk.discourse_parsing.module.dcAnnotator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class DCLookupTest {
	private TreeMap<Integer, Integer> tokenize(String text){
		TreeMap<Integer, Integer> idxWords = new TreeMap<Integer, Integer>();
		String[] words = text.split(" ");
		int offset = 0;
		for (String word: words){
			idxWords.put(offset, offset + word.length());
			offset += word.length() + 1;
		}
		return idxWords;
	}

	@Test
	public void whenLookingOfTermsThenTheirOccurrenceAreIdentified(){
		DCLookup dcLookup = new DCLookup();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(text.split(" "));
		dcLookup.setTerms(terms);
		
		Map<Integer, Integer> occurrence = dcLookup.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(5);
		assertThat(occurrence.keySet()).contains(0, 3, 6, 8, 13);
	}
	
	@Test
	public void whenTermsAreOverLapThenOnlyOneOfThemAreIndexed(){
		DCLookup dcLookup = new DCLookup();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(new String[]{"it is", "is a"});
		dcLookup.setTerms(terms);
		
		Map<Integer, Integer> occurrence = dcLookup.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(1);
	}
	
	@Test
	public void whenATermContainAnotherTermThenTheBiggestIsIdentified(){
		DCLookup dcLookup = new DCLookup();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(new String[]{"it is", "is", "a test", "a"});
		dcLookup.setTerms(terms);
		
		Map<Integer, Integer> occurrence = dcLookup.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(2);
		assertThat(occurrence).contains(entry(0, 5), entry(6, 12));
		
	}

}
