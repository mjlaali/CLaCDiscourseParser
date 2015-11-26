package ca.concordia.clac.ml.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

public class FeatureExtractors{

	public static <T> Function<T, Feature> getFeature(String featureName, Function<T, String> featureExtractor) {
		return val -> new Feature(featureName, Optional.of(val).map(featureExtractor).orElse("null"));
	}
	
	@SafeVarargs
	public static <T> Function<T, List<Feature>> getFeatures(
			Function<? super T, Feature>... extractor) {
		return annotation -> {
			List<Feature> results = new ArrayList<>();
			Stream.of(extractor).forEach((f) -> results.add(f.apply(annotation)));
			return results;
		};
	}

	public static Function<? extends Annotation, String> getText(){
		return (ann) -> ann.getCoveredText();
	}

	public static <T extends Annotation> Function<T, String> getText(Class<T> cls){
		return (ann) -> ann.getCoveredText();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <T> Function<FSArray, List<T>> convertToList(Class<T> cls){
		return (FSArray fsArray) -> {
			List<T> res = new ArrayList<>();
			for (int i = 0; i < fsArray.size(); i++){
				res.add((T)fsArray.get(i));
			}
			return res;
		};
	}
}
