package org.cleartk.ml.feature.function;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.ml.Feature;

public class FeatureNameChangerFunc implements FeatureFunction{
	private FeatureFunction toBeDecorated;
	private String featureName; 
	
	public FeatureNameChangerFunc(FeatureFunction toBeDecorated, String featureName) {
		this.toBeDecorated = toBeDecorated;
		this.featureName = featureName;
	}

	@Override
	public List<Feature> apply(Feature input) {
		List<Feature> features = toBeDecorated.apply(input);
		int idx = 0;
		
		List<Feature> newFeatures = new ArrayList<Feature>(features.size());
		for (Feature feature: features){
			String newFeatureName = featureName;
			if (idx != 0){
				newFeatureName = featureName + "-" + idx;
			}
			newFeatures.add(new Feature(newFeatureName, feature.getValue()));
			idx++;
		}
		return newFeatures;
	}

}
