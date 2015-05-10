package org.cleartk.discourse_parsing.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DiscourseConnectivesList;
import org.cleartk.discourse.type.DiscourseRelation;

public class DiscourseSenseAnnotator extends JCasAnnotator_ImplBase{
	private Map<String, String> dcToMostFrequentRel;
	
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(DiscourseSenseAnnotator.class);
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		dcToMostFrequentRel = Tools.readCSVFile(DiscourseConnectivesList.DISCOURSE_CONNECTIVES_MOST_FREQUENT_RELATION_FILE);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
		
		List<DiscourseRelation> toRemove = new ArrayList<DiscourseRelation>();
		for (DiscourseRelation discourseRelation: discourseRelations){
			String dcText = discourseRelation.getDiscourseConnectiveText().toLowerCase();
			String relationSense = dcToMostFrequentRel.get(dcText);
			if (relationSense == null){
				System.err.println("DiscourseSenseAnnotator.process(): No sense has found for dc <" + dcText + ">");
				toRemove.add(discourseRelation);
			} else
				discourseRelation.setSense(relationSense);
		}
		
		for (DiscourseRelation discourseRelation: toRemove){
			discourseRelation.removeFromIndexes();
		}
	}

}
