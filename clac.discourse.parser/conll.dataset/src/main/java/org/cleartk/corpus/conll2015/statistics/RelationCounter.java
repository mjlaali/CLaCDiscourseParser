package org.cleartk.corpus.conll2015.statistics;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseRelation;

public class RelationCounter extends JCasAnnotator_ImplBase{
	public static int discourseRelationCnt = 0;

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(RelationCounter.class);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		discourseRelationCnt += JCasUtil.select(aJCas, DiscourseRelation.class).size();
		
	}

}
