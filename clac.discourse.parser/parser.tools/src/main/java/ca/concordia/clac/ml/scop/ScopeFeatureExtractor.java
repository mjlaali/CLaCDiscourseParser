package ca.concordia.clac.ml.scop;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

public class ScopeFeatureExtractor {
	@SafeVarargs
	public static <T> Function<List<T>, List<Feature>> extractFromScope(Function<List<T>, List<Feature>>... featureExtractors) {
		
		return (scope) -> {
			List<Feature> results = new ArrayList<>();
			Stream.of(featureExtractors).forEach(f -> results.addAll(f.apply(scope)));
			return results;
		};
	}
	
	public static <ANN extends Annotation> Function<List<ANN>, ANN> getLast(Class<ANN> cls){
		return (annotations) -> annotations.get(annotations.size() - 1);
	}
	
	public static <ANN extends Annotation> Function<List<ANN>, Feature> joinInScope(Function<ANN, String> func, String featureName){
		return (annotations) -> new Feature(featureName, 
				annotations.stream()
					.map(func)
					.collect(Collectors.joining("-")));
	}
}
