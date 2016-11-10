package ca.concordia.clac.ml.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

import ca.concordia.clac.ml.scop.ScopeFeatureExtractor;

/**
 * To create feature, first map each instance to a scope (e.g. {@link TreeFeatureExtractor#getChilderen()} 
 * and then calculate a value based on the scope (e.g. {@link ScopeFeatureExtractor#collect(java.util.stream.Collector)}
 * and finally create a feature for the calculated value ({@link #makeFeature(String)}
 * 
 * If you need to calculate multiple features from an scope (e.g. the calculation of the scope is heavy computing process)
 * it is better to use {@link #multiMap(Function...)} in which each map function is a feature extractor on the scope.
 * 
 * @author majid
 *
 */
public class FeatureExtractors{
	public static final String NULL_STRING = "null";
	public static <T> Function<T, Feature> makeFeature(String featureName){
		return (value) -> new Feature(featureName, value == null ? NULL_STRING : value);
	}

	public static <T> Function<Optional<T>, Feature> makeFeature(String featureName, T other){
		return (value) -> new Feature(featureName, value.orElse(other));
	}

	public static <T> Function<List<List<T>>, List<T>>flatMap(Class<T> cls){
		return (input) ->
			input.stream().flatMap((a) -> a.stream()).collect(Collectors.toList());
		
	}
	
	@SafeVarargs
	public static <S, T, R> BiFunction<S, T, List<R>> multiBiFuncMap(BiFunction<? super S, ? super T, R>... mapFunctions){
		return (s, t) -> {
			if (t == null || s == null)
				return Collections.emptyList();
			else 
			return Stream.of(mapFunctions).map((f) ->  f.apply(s, t))
				.collect(Collectors.toList());
		};
	}
	
	@SafeVarargs
	public static <T, R> Function<T, List<R>> multiMap(Function<? super T, R>... mapFunctions){
		return (t) -> {
			return Stream.of(mapFunctions).map((f) -> f.apply(t))
				.collect(Collectors.toList());
		};
	}
	
	@SafeVarargs
	public static <T, R> Function<T, List<R>> combineMap(Function<? super T, List<R>>... mapFunctions){
		return (t) -> {
			if (t == null)
				return Collections.emptyList();
			else 
			return Stream.of(mapFunctions).map((f) ->  Optional.of(t).map(f).orElse(null))
				.filter((v) -> v != null).flatMap((l) -> l.stream()).collect(Collectors.toList());
		};
	}
	
	public static <T> Function<T, List<T>> recursiveCall(Function<T, T> func, Predicate<? super T> condition){
		return (t) -> {
			List<T> res = new ArrayList<>();
			while (condition.test(t)){
				res.add(t);
				t = func.apply(t);
			}
			return res;
		};
	}
	
	public static <T extends Annotation> Function<T, String> getText(){
		return (ann) -> ann != null ? ann.getCoveredText() : null;
	}

	public static <T extends Annotation> Function<T, String> getText(Class<T> cls){
		return (ann) -> ann != null ? ann.getCoveredText() : null;
	}
	
	public static <T, R> Function<T, R> getFunction(Function<T, R> f){
		return f;
	}

	public static <T, U, R> BiFunction<T, U, R> getFunction(BiFunction<T, U, R> f){
		return f;
	}
	
	public static <T, R> Function<T, R> getFunction(Function<T, R> f, Class<T> clsT, Class<R> clsR){
		return f;
	}

	public static <T> Function<T, T> dummyFunc(Class<T> cls){
		return (t) -> t;
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
	
	public static <T, U, R> BiFunction<T, U, R> makeBiFunc(final Function<T, R> func){
		return (t, u) -> func.apply(t);
	}
	
}
