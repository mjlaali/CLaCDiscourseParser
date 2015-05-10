package org.cleartk.ml.feature.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.TokenList;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Bounds;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Context;
import org.cleartk.token.type.Token;

import com.google.common.base.Joiner;

public class TokenListContext implements Context{

	@Override
	public String getName() {
		return "TokenListContext";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <SEARCH_T extends Annotation> List<Feature> extract(JCas jCas,
			Annotation focusAnnotation, Bounds bounds,
			Class<SEARCH_T> annotationClass,
			FeatureExtractor1<SEARCH_T> extractor)
					throws CleartkExtractorException {
		if (annotationClass != Token.class || !(focusAnnotation instanceof TokenList))
			throw new UnsupportedOperationException();


		List<String> values = new ArrayList<String>();
		TokenList focusTokenList = (TokenList) focusAnnotation; 
		List<Token> tokens = TokenListTools.convertToTokens(focusTokenList);

		for (Token ann : tokens) {
			for (Feature feature : extractor.extract(jCas, (SEARCH_T) ann)) {
				values.add(String.valueOf(feature.getValue()));
			}
		}

//		String featureName = getName();
		String featureName = extractor instanceof NamedFeatureExtractor1
				? ((NamedFeatureExtractor1<SEARCH_T>) extractor).getFeatureName()
						: null;

		Feature feature = new Feature(featureName, Joiner.on('_').join(values));

		List<Feature> features = new ArrayList<Feature>();
		features.add(new ContextFeature(this.getName(), feature));

		return features;
	}

	private static class ContextFeature extends Feature {
		private static final long serialVersionUID = 1L;

		public ContextFeature(String baseName, Feature feature) {
			this.setName(Feature.createName(baseName, feature.getName()));
			this.setValue(feature.getValue());
		}


	}


}
