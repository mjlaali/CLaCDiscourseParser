package org.cleartk.discourse_parsing.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

class SentencePair{
	public SentencePair(Sentence first, Sentence second) {
		this.first = first;
		this.second = second;
	}
	
	Sentence first;
	Sentence second;
}

public class ImplicitRelationAnnotator extends RuleBasedLabeler<String, SentencePair>{
	private DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(ImplicitRelationAnnotator.class);
	}

	@Override
	public void init() {
		
	}

	@Override
	public void setLabel(JCas defView, SentencePair instance,
			String sense) {
		List<Token> arg1 = JCasUtil.selectCovered(Token.class, instance.first); 
		List<Token> arg2 = JCasUtil.selectCovered(Token.class, instance.second); 
				
		discourseRelationFactory.makeAnImplicitRelation(defView, sense, 
				arg1.subList(0, arg1.size() - 1), arg2.subList(0, arg2.size() - 1)).addToIndexes();
	}

	@Override
	public List<SentencePair> getInstances(JCas defView)
			throws AnalysisEngineProcessException {
		Collection<Sentence> sentences = JCasUtil.select(defView, Sentence.class);
		Sentence prev = null;
		List<SentencePair> instances = new ArrayList<SentencePair>();
		for (Sentence sent: sentences){
			if (prev != null){
				instances.add(new SentencePair(prev, sent));
			} 
			prev = sent;
		}
		return instances;
	}

	@Override
	public String getLablel() {
		return "Expansion.Conjunction";
	}


}
