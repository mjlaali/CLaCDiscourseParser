package ca.concordia.clac.ml.classifier;

import java.util.Collection;

import org.apache.uima.jcas.JCas;

public interface InstanceExtractor<INSTANCE_TYPE> {

	Collection<INSTANCE_TYPE> getInstances(JCas aJCas);
}
