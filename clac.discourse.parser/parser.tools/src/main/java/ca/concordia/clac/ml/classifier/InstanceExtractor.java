package ca.concordia.clac.ml.classifier;

import java.util.Collection;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public interface InstanceExtractor<INSTANCE_TYPE extends Annotation> {

	Collection<INSTANCE_TYPE> getInstances(JCas aJCas);
}
