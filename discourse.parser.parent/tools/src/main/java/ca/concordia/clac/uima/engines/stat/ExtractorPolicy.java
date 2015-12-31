package ca.concordia.clac.uima.engines.stat;

import java.util.function.Function;

import org.apache.uima.fit.factory.initializable.Initializable;

import ca.concordia.clac.ml.classifier.InstanceExtractor;

public interface ExtractorPolicy<T> extends Initializable{

	public InstanceExtractor<T> getInstanceExtractor();
	
	public Function<T, String> getKeyExtractor();
	public Function<T, String> getValueExtractor();
}
