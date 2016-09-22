package ca.concordia.clac.ml.classifier;

import java.util.List;

public interface SequenceClassifierConsumer<CLASSIFIER_OUTPUT, SEQUENCE_TYPE, INSTANCE_TYPE>{
	
	public void accept(List<CLASSIFIER_OUTPUT> outcomes, SEQUENCE_TYPE aSequence, List<INSTANCE_TYPE> instances);

}
