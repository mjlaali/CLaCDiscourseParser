package ca.concordia.clac.uima.engines;

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
	
	private AnnotationFactory<Annotation> annotationFactory = (aJCas, start, end) -> new Annotation(aJCas, start, end); 

	@Test
	public void whenLookingOfTermsThenTheirOccurrenceAreIdentified(){
		LookupInstanceExtractor<Annotation> instanceExtractor = new LookupInstanceExtractor<>();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(text.split(" "));
		instanceExtractor.init(terms, annotationFactory);
		
		Map<Integer, Integer> occurrence = instanceExtractor.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(5);
		assertThat(occurrence.keySet()).contains(0, 3, 6, 8, 13);
	}
	
	@Test
	public void whenTermsAreOverLapThenOnlyOneOfThemAreIndexed(){
		LookupInstanceExtractor<Annotation> instanceExtractor = new LookupInstanceExtractor<>();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(new String[]{"it is", "is a"});
		instanceExtractor.init(terms, annotationFactory);
		
		Map<Integer, Integer> occurrence = instanceExtractor.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(1);
	}
	
	@Test
	public void whenATermContainAnotherTermThenTheBiggestIsIdentified(){
		LookupInstanceExtractor<Annotation> instanceExtractor = new LookupInstanceExtractor<>();
		String text = "it is a test .";
		List<String> terms = Arrays.asList(new String[]{"it is", "is", "a test", "a"});
		instanceExtractor.init(terms, annotationFactory);
		
		Map<Integer, Integer> occurrence = instanceExtractor.getOccurrence(text, tokenize(text));
		assertThat(occurrence).hasSize(2);
		assertThat(occurrence).contains(entry(0, 5), entry(6, 12));
		
	}

	
	@Test
	public void whenCreatingInstancesThenTheyAreNotAddedToJCas() throws UIMAException{
		LookupInstanceExtractor<Annotation> instanceExtractor = new LookupInstanceExtractor<>();
		String text = "it";
		List<String> terms = Arrays.asList(new String[]{"it"});
		instanceExtractor.init(terms, annotationFactory);
		
		JCas aJCas = JCasFactory.createJCas();
		aJCas.setDocumentText(text);
		
		instanceExtractor.getInstances(aJCas);
		assertThat(JCasUtil.select(aJCas, Annotation.class)).hasSize(1);
	}
}
