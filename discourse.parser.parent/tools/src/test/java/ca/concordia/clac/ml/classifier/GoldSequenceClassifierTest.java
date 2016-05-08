package ca.concordia.clac.ml.classifier;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.junit.Test;

public class GoldSequenceClassifierTest {

	@Test
	public void givenOneFeatureWhenLearnThenReturnCorrectAtTest(){
		GoldSequenceClassifier<String> goldSequenceClassifier = new GoldSequenceClassifier<String>("null", "test");
		
		Feature f1 = new Feature("f1", "f1");
		goldSequenceClassifier.accept(Arrays.asList(
				new Instance<>("A", 
						Arrays.asList(
								f1
								)
						)
				));
		
		List<String> outcomes = goldSequenceClassifier.apply(Arrays.asList(Arrays.asList(f1)));
		
		assertThat(outcomes).containsExactly("A");
	}
	
	@Test
	public void givenTwoFeaturesWhenLearnThenReturnCorrectAtTest(){
		GoldSequenceClassifier<String> goldSequenceClassifier = new GoldSequenceClassifier<String>("null", "test");
		
		Feature f1 = new Feature("f1", "f1");
		Feature f2 = new Feature("f2", "f2");
		goldSequenceClassifier.accept(Arrays.asList(
				new Instance<>("A", 
						Arrays.asList(
								f1, f2
								)
						)
				));
		
		List<String> outcomes = goldSequenceClassifier.apply(Arrays.asList(Arrays.asList(f1, f2)));
		
		assertThat(outcomes).containsExactly("A");
	}
	
	@Test
	public void givenTwoInstancesWhenLearnThenReturnCorrectAtTest(){
		GoldSequenceClassifier<String> goldSequenceClassifier = new GoldSequenceClassifier<String>("null", "test");
		
		Feature f1 = new Feature("f1", "f1");
		Feature f2 = new Feature("f2", "f2");
		Feature f3 = new Feature("f3", "f3");
		goldSequenceClassifier.accept(Arrays.asList(
				new Instance<>("A", 
						Arrays.asList(
								f1, f2
								)
						),
				new Instance<>("B", 
						Arrays.asList(
								f2, f3
								)
						),
				new Instance<>("C", 
						Arrays.asList(
								f1, f3
								)
						)
				));
		
		List<String> outcomes = goldSequenceClassifier.apply(Arrays.asList(
				Arrays.asList(f1, f2),
				Arrays.asList(f2, f3),
				Arrays.asList(f1, f3)
				));
		
		assertThat(outcomes).containsExactly("A", "B", "C");
	}

}
