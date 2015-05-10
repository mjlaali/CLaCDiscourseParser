package org.cleartk.ml.feature.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

import com.google.common.base.Joiner;

public class AggregateExtractor1<T extends Annotation> implements FeatureExtractor1<T> {
	private FeatureExtractor1<T>[] extractors;
	private String name;
	
	@SuppressWarnings("unchecked")
	public AggregateExtractor1(String name, FeatureExtractor1<T>... extractors){
		this.extractors = extractors;
		this.name = name;
	}

	@Override
	public List<Feature> extract(JCas view, T focusAnnotation)
			throws CleartkExtractorException {
		List<String> values = new ArrayList<String>();
		for (FeatureExtractor1<T> featureExtractor1: extractors){
			List<Feature> features = featureExtractor1.extract(view, focusAnnotation);
			if (features.isEmpty())
				values.add("empty");
			for (Feature feature: features){
				values.add(feature.getValue().toString());
			}
		}
		
		return Collections.singletonList(new Feature(name, Joiner.on('_').join(values)));
	}

}
