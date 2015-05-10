package org.cleartk.discourse_parsing.module;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

public interface Labeler <CLASSIFIER_OUTPUT, INSTANCE_TYPE>{
	void init();
	void setLabel(JCas defView, INSTANCE_TYPE instance,	CLASSIFIER_OUTPUT classifiedLabel);
	List<INSTANCE_TYPE> getInstances(JCas defView) throws AnalysisEngineProcessException;
}
