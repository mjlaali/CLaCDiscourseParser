package ca.concordia.clac.ml.scop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cleartk.ml.Feature;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


public class ScopeFeatureExtractor {
	@SafeVarargs
	public static <T, R> Function<List<T>, List<R>> mapTo(Function<List<T>, List<R>>... featureExtractors) {
		
		return (scope) -> {
			List<R> results = new ArrayList<>();
			Stream.of(featureExtractors).forEach(f -> results.addAll(
					Optional.of(scope).map(f).orElse(Collections.emptyList())));
			return results;
		};
	}
	
	public static <S, T, R> BiFunction<List<S>, T, List<R>> mapOneByOneTo(BiFunction<S, T, R> fun){
		return (scope, c) -> {
			return scope.stream().map((s) -> fun.apply(s, c)).collect(Collectors.toList());
		};
	}

	public static <T, R> Function<List<T>, List<R>> mapOneByOneTo(Function<T, R> fun){
		
		return (scope) -> {
			return scope.stream().map(fun).collect(Collectors.toList());
		};
	}
	
	public static <T> Function<List<T>, T> getLast(Class<T> cls){
		return (annotations) -> annotations.size() > 0 ? annotations.get(annotations.size() - 1) : null;
	}
	
	public static <T> Function<List<T>, Feature> joinInScope(String featureName, Function<T, String> func){
		return (annotations) -> new Feature(featureName, 
				annotations.stream()
					.map(func)
					.collect(Collectors.joining("-")));
	}
	
	public static <T, A, R> Function<List<T>, R> collect(Collector<? super T, A, R> collector){
		return (scope) -> {
			return scope.stream().collect(collector);
		};
	}
	
	public static Function<Set<Token>, Token> pickLeftMostToken(){
		return (set) -> {
			if (set == null || set.size() == 0)
				return null;
			List<Token> list = new ArrayList<>(set);
			Collections.sort(list, (a, b) -> a.getBegin() - b.getBegin());
			return list.get(0);
		};
	}
	
	public static Function<Set<Token>, Token> pickRightMostToken(){
		return (set) -> {
			if (set == null || set.size() == 0)
				return null;
			List<Token> list = new ArrayList<>(set);
			Collections.sort(list, (a, b) -> a.getBegin() - b.getBegin());
			return list.get(list.size() - 1);
		};
	}

}
