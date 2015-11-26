package ca.concordia.clac.ml.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

public class FeatureExtractors{

	public static <ANN_TYPE extends Annotation> Function<String, Feature> getFeature(String featureName) {
		return val -> new Feature(featureName, "" + val);
	}
	
	public static <T, R> Function<T, R> nullSafeCall(
			Function<T, R> extractor) {
		return annotation -> annotation == null ? null : extractor.apply(annotation);
	}
	
	@SafeVarargs
	public static <T> Function<T, List<Feature>> getFeatures(
			Function<T, Feature>... extractor) {
		return annotation -> {
			List<Feature> results = new ArrayList<>();
			Stream.of(extractor).forEach((f) -> results.add(f.apply(annotation)));
			return results;
		};
	}

	public static Function<? extends Annotation, String> getText(){
		return nullSafeCall((ann) -> ann.getCoveredText());
	}

	public static <T extends Annotation> Function<T, String> getText(Class<T> cls){
		return nullSafeCall((ann) -> ann.getCoveredText());
	}
	
	
}
