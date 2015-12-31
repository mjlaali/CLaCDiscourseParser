package ca.concordia.clac.uima.engines;

import java.util.function.BiFunction;

import org.apache.uima.fit.factory.initializable.Initializable;

import ca.concordia.clac.ml.classifier.InstanceExtractor;

public interface MergePolicy<T> extends Initializable{

	public InstanceExtractor<T> getInstanceExtractor();
	public BiFunction<T, T, T> getMerger();
}
