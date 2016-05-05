package org.cleartk.corpus.conll2015.loader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.Tools;

public class DummyAnnontator extends JCasAnnotator_ImplBase{

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		System.out.println("DummyAnnontator.process(): " + Tools.getDocName(aJCas));
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(DummyAnnontator.class);
	}
}
