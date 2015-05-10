package org.cleartk.ml.feature.extractor.context;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Bounds;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Context;

public abstract class AbstractSelectorContext implements Context{
	
	@Override
	public <SEARCH_T extends Annotation> List<Feature> extract(JCas jCas,
			Annotation focusAnnotation, Bounds bounds,
			Class<SEARCH_T> annotationClass,
			FeatureExtractor1<SEARCH_T> extractor)
			throws CleartkExtractorException {
		return null;
	}

}
