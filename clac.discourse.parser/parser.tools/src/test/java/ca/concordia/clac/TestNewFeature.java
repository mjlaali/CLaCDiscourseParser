package ca.concordia.clac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.ComplexInstance;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

class Node{
	Node next;
	double val;
	
	public Node(Node next){
		this.next = next;
		val = Math.random();
	}
}

class MyStreams{
	public static <T> Stream<T> iterateUntil(final T seed, UnaryOperator<T> f, Predicate<T> p){
		 Objects.requireNonNull(f);
	        final Iterator<T> iterator = new Iterator<T>() {
	            T cur = null;
	            T next = seed;

	            @Override
	            public boolean hasNext() {
	                return p.test(next);
	            }

	            @Override
	            public T next() {
	            	cur = next;
	            	next = f.apply(cur);
	                return cur;
	            }
	        };
	        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
	                iterator,
	                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}
}

public class TestNewFeature {

	@Test
	public void test1(){
		Node first = new Node(new Node(new Node(null)));
		List<Node> collect = 
				MyStreams.iterateUntil(first, n -> n.next, n -> n != null).collect(Collectors.toList());
//		System.out.println(collect);
		List<Double> copy = new CopyOnWriteArrayList<>();
		collect.parallelStream()
			.map(n -> n.val)
			.peek(d -> copy.add(d))
			.forEach(n -> System.out.println(n));
		System.out.println(copy);
		
	}
	
	@Test
	public void test2() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		aJCas.setDocumentText("it is a test.");
		aJCas.setDocumentLanguage("en");
		
		SimplePipeline.runPipeline(aJCas
				, AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class)
				, AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class));
		
		List<Token> anns = new ArrayList<>(JCasUtil.select(aJCas, Token.class));
		
		List<Function<Token, List<Feature>>> extractors = new ArrayList<>();
		extractors.add(ann -> Arrays.asList(new Feature("f1", ann.getCoveredText())));
		extractors.add(ann -> {
			Feature f2 = new Feature("f2", ann.getPos().getPosValue());
			System.out.println(f2);
			return Arrays.asList(f2);
		});
		
		
		Map<Token, List<Feature>> allFeatures = anns.stream()
		.flatMap(ann -> extractors.stream().map(f -> {
			ComplexInstance<String, Token> res = new ComplexInstance<>(ann);
			res.setFeatures(f.apply(ann));
			return res;	
			}))
		.collect(Collectors.toMap(
				ci -> ci.getInstance(), 
				ci -> ci.getFeatures(), 
				(list1, list2) -> {
					List<Feature> res = new LinkedList<>(list1); 
					res.addAll(list2);
					return res;}));
		
		allFeatures.entrySet().stream().forEach(e -> System.out.println(e.getKey().getCoveredText() + e.getValue()));

	}
	
}
