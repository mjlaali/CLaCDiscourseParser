package ca.concordia.clac.ml.feature;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

public class FeatureExtractors{

	public static <ANN_TYPE extends Annotation> Function<ANN_TYPE, Feature> getAttribute(
			Function<ANN_TYPE, String> extractor, String featureName) {
		return annotation -> new Feature(featureName, extractor.apply(annotation));
	}
	
	public static <ANN_TYPE extends Annotation> Function<ANN_TYPE, List<Feature>> convertToFeatureList(
			Function<ANN_TYPE, Feature> extractor) {
		return annotation -> Arrays.asList(extractor.apply(annotation));
	}

//	public static <INPUT> Function<List<INPUT>, List<Feature>> scopFeatureExtractor(Function<T, R>){
//		
//	}
}
