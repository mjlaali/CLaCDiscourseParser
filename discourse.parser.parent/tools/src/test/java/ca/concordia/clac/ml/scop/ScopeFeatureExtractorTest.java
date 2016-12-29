package ca.concordia.clac.ml.scop;

import static ca.concordia.clac.ml.feature.FeatureExtractors.NULL_STRING;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFunction;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.getLast;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.cleartk.ml.Feature;
import org.junit.Test;

public class ScopeFeatureExtractorTest {

	@Test
	public void givenAnEmptyListWhenCallingGetLastAndAFeatureThenTheFeatureIsNull(){
		List<String> list = new ArrayList<>();
		
		Function<List<String>, Feature> featureExtractor = getLast(String.class).andThen(makeFeature("lower"));
		Feature feature = featureExtractor.apply(list);
		assertThat(feature.getValue()).isEqualTo(NULL_STRING);
	}
	
	
	@Test
	public void givenAnEmptyListWhenCallingGetLastAndMultipleFeaturesThenTheFeaturesValueIsNull(){
		List<String> list = new ArrayList<>();
		
		Function<List<String>, List<Feature>> featureExtractor = getLast(String.class).andThen(multiMap(
				getFunction(String::toLowerCase, String.class, String.class).andThen(makeFeature("lower")), 
				getFunction(String::toUpperCase, String.class, String.class).andThen(makeFeature("uper"))));
		List<Feature> feature = featureExtractor.apply(list);
		assertThat(feature).containsOnly(new Feature("lower", "null"), new Feature("uper", "null"));
	}
	
	
}
