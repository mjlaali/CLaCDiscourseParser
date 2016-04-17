package org.discourse.conll.dataset.analysis;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseRelation;
import org.discourse.parser.argument_labeler.argumentLabeler.KongExample;
import org.junit.Before;
import org.junit.Test;

public class ArgumentVisulizerTest{
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private KongExample example = new KongExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
	}


	@Test
	public void visualizingKognExampleTestReturns(){
		
	}
	
}
