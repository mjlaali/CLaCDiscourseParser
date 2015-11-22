package org.parser.dc.disambiguation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

public class LookupInstanceExtractorTest {
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
		LookupInstanceExtractor instanceExtractor = new LookupInstanceExtractor();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(text.split(" "));
		instanceExtractor.setTerms(terms);
		
		Map<Integer, Integer> occurrence = instanceExtractor.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(5);
		assertThat(occurrence.keySet()).contains(0, 3, 6, 8, 13);
	}
	
	@Test
	public void whenTermsAreOverLapThenOnlyOneOfThemAreIndexed(){
		LookupInstanceExtractor instanceExtractor = new LookupInstanceExtractor();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(new String[]{"it is", "is a"});
		instanceExtractor.setTerms(terms);
		
		Map<Integer, Integer> occurrence = instanceExtractor.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(1);
	}
	
	@Test
	public void whenATermContainAnotherTermThenTheBiggestIsIdentified(){
		LookupInstanceExtractor instanceExtractor = new LookupInstanceExtractor();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(new String[]{"it is", "is", "a test", "a"});
		instanceExtractor.setTerms(terms);
		
		Map<Integer, Integer> occurrence = instanceExtractor.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(2);
		assertThat(occurrence).contains(entry(0, 5), entry(6, 12));
		
	}

	
	@Test
	public void whenCreatingInstancesThenTheyAreNotAddedToJCas() throws UIMAException{
		LookupInstanceExtractor instanceExtractor = new LookupInstanceExtractor();
		String text = "it";
		List<String> terms = Arrays.asList(new String[]{"it"});
		instanceExtractor.setTerms(terms);
		
		JCas aJCas = JCasFactory.createJCas();
		aJCas.setDocumentText(text);
		
		instanceExtractor.getInstances(aJCas);
		assertThat(JCasUtil.select(aJCas, Annotation.class)).hasSize(1);
	}
}
