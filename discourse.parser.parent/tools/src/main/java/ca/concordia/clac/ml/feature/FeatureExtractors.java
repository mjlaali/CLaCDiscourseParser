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
	public static <T, R> Function<T, Feature> getFeature(String featureName, Function<T, R> featureExtractor) {
		return val -> val == null ? null : new Feature(featureName, Optional.of(val)
				.map(featureExtractor).map(Object::toString).orElse("null"));
	}
	

	@SafeVarargs
	public static <T> Function<T, List<Feature>> getFeatures(
			Function<? super T, Feature>... extractor) {
		return annotation -> {
			List<Feature> results = new ArrayList<>();
			Stream.of(extractor).forEach((f) -> {
				Feature features = f.apply(annotation);
				if (features != null)
					results.add(features);
			});
			return results;
		};
	}

	public static Function<? extends Annotation, String> getText(){
		return (ann) -> ann.getCoveredText();
	}

	public static <T extends Annotation> Function<T, String> getText(Class<T> cls){
		return (ann) -> ann.getCoveredText();
	}
	
	public static <T, R> Function<T, R> getFunction(Function<T, R> f){
		return f;
	}
	
	public static <T, R> Function<T, R> getFunction(Function<T, R> f, Class<T> clsT, Class<R> clsR){
		return f;
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
