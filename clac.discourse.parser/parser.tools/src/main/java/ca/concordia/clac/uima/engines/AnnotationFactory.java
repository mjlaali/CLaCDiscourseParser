package ca.concordia.clac.uima.engines;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public interface AnnotationFactory<T extends Annotation> {
	public T buildAnnotation(JCas aJCas, int start, int end);
}
