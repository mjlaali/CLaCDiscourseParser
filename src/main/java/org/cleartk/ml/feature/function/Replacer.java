package org.cleartk.ml.feature.function;

import java.util.Collections;
import java.util.List;

import org.cleartk.ml.Feature;

public class Replacer implements FeatureFunction{
	private String[] regex, replacement;
	
	public Replacer(String... regexReplacementPair) {
		if (regexReplacementPair.length % 2 != 0)
			throw new IllegalArgumentException();
		
		regex = new String[regexReplacementPair.length / 2];
		replacement = new String[regexReplacementPair.length / 2];
		for (int i = 0; i < regexReplacementPair.length; i += 2){
			regex[i/2] = regexReplacementPair[i];
			replacement[i/2] = regexReplacementPair[i + 1];
		}
	}

	@Override
	public List<Feature> apply(Feature input) {
		String value = input.getValue().toString();
		
		for (int i = 0; i < regex.length; i++)
			value = value.replace(regex[i], replacement[i]);
		
		return Collections.singletonList(new Feature(input.getName(), value));
	}

}
