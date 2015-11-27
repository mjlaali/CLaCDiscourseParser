package ca.concordia.clac.ml.scop;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeatures;
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
		
		Function<List<String>, Feature> featureExtractor = getLast(String.class).andThen(getFeature("val", String::toLowerCase));
		Feature feature = featureExtractor.apply(list);
		assertThat(feature).isNull();
	}
	
	
	@Test
	public void givenAnEmptyListWhenCallingGetLastAndMultipleFeaturesThenTheFeaturesIsEmpty(){
		List<String> list = new ArrayList<>();
		
		Function<List<String>, List<Feature>> featureExtractor = getLast(String.class).andThen(getFeatures(
				getFeature("lower", String::toLowerCase), getFeature("upper", String::toUpperCase)));
		List<Feature> feature = featureExtractor.apply(list);
		assertThat(feature).isEmpty();;
	}
	
	
}
