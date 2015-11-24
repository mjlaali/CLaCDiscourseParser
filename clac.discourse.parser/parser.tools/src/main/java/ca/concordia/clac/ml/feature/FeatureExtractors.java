package ca.concordia.clac.ml.feature;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.cleartk.ml.Feature;

public class FeatureExtractors{

	public static <ANN_TYPE> Function<ANN_TYPE, List<Feature>> makeAttributeFeatureExtractor(
			Function<ANN_TYPE, String> extractor, String featureName) {
		return annotation -> Arrays.asList(new Feature(featureName, extractor.apply(annotation)));
	}

}
