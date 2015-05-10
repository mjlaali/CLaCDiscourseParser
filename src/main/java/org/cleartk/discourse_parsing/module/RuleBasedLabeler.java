package org.cleartk.discourse_parsing.module;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

public abstract class RuleBasedLabeler<CLASSIFIER_OUTPUT, INSTANCE_TYPE> extends JCasAnnotator_ImplBase implements Labeler<CLASSIFIER_OUTPUT, INSTANCE_TYPE>{
	protected JCas aJCas;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		this.aJCas = aJCas;
		init();
		List<INSTANCE_TYPE> instances = getInstances(aJCas);

		for (INSTANCE_TYPE instance: instances){
			CLASSIFIER_OUTPUT classifiedLabel = null;
			classifiedLabel = getLablel();
			setLabel(aJCas, instance, classifiedLabel);
		}		
	}


	public abstract CLASSIFIER_OUTPUT getLablel();
}
