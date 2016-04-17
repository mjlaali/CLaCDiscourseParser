package org.discourse.parser.argument_labeler.argumentLabeler;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;

public class NoneNodeExample implements DiscourseRelationExample{
	String text = "He said the market's volatility disturbs him, but that all the exchange can do is \"slow down the process\" by using its circuit breakers and shock absorbers.";
	String arg1 = "the market's volatility disturbs him";
	String arg2 = "that all the exchange can do is \"slow down the process\" by using its circuit breakers and shock absorbers";
	String connective = "but";
	String sense = "Comparison.Contrast";
	String parseTree = "( (S (NP (PRP He)) (VP (VBD said) (SBAR (SBAR (S (NP (NP (DT the) (NN market) (POS 's)) (NN volatility)) "
			+ "(VP (VBZ disturbs) (NP (PRP him))))) (, ,) (CC but) (SBAR (IN that) (S (NP (PDT all) (DT the) (NN exchange)) (VP (MD can) "
			+ "(VP (VB do) (VP (VBZ is) (`` ``) (ADJP (JJ slow) (PP (IN down) (NP (DT the) (NN process)))) ('' '')"
			+ " (PP (IN by) (S (VP (VBG using) (NP (NP (PRP$ its) (NN circuit) (NNS breakers)) (CC and) (NP (NN shock) "
			+ "(NNS absorbers))))))))))))) (. .)) )";
	

	public String getArg1() {
		return arg1;
	}
	
	public String[] getArg2() {
		return new String[]{arg2};
	}
	
	public String getDiscourseConnective() {
		return connective;
	}
	
	public String[] getParseTree() {
		return new String[]{parseTree};
	}
	
	public String getSense() {
		return sense;
	}
	
	public String getText() {
		return text;
	}
	
	
	public static void main(String[] args) throws UIMAException {
		
		DiscourseRelationFactory factory = new DiscourseRelationFactory();
		NoneNodeExample anExample = new NoneNodeExample();
		JCas aJCas = JCasFactory.createJCas();
		factory.makeDiscourseRelationFrom(aJCas, anExample);
		System.out.println("NoneNodeExample.main()");
	}

	
	
}
