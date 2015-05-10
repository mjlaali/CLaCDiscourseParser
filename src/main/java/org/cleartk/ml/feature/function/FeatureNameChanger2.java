package org.cleartk.ml.feature.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor2;

public class FeatureNameChanger2<T extends Annotation, U extends Annotation> implements FeatureExtractor2<T, U>{
	private FeatureExtractor2<T, U> toBeDecorated;
	private String featureName; 
	private String filterFeature;
	
	public FeatureNameChanger2(FeatureExtractor2<T, U> toBeDecorated, String featureName, String filterFeature) {
		this.toBeDecorated = toBeDecorated;
		this.featureName = featureName;
		this.filterFeature = filterFeature;
	}

	@Override
	public List<Feature> extract(JCas view, T annotation1, U annotation2)
			throws CleartkExtractorException {
		List<Feature> features = toBeDecorated.extract(view, annotation1, annotation2);
		int idx = 0;
		
		List<Feature> newFeatures = new ArrayList<Feature>();
		for (Feature feature: features){
			if (filterFeature.equals(feature.getName()))
				continue;
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
