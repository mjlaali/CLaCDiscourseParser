package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.discourse.type.DiscourseRelation;

public class AdditionallyExample implements DiscourseRelationExample {
	private String text = "A new contract is in place. Additionally, staff will be offered a bonus scheme.";
			
	protected String[] parseTrees = new String[]{"(ROOT (S (NP (DT A) (JJ new) (NN contract)) (VP (VBZ is) (PP (IN in) (NP (NN place)))) (. .)))", 
			"(ROOT (S (ADVP (RB Additionally)) (, ,) (NP (NN staff)) (VP (MD will) (VP (VB be) (VP (VBN offered) (NP (DT a) (NN bonus) (NN scheme))))) (. .)))"};
	protected JCas aJCas;
	protected DiscourseRelation discourseRelation;
	protected String arg1 = "A new contract is in place.";
	protected String arg2 = ", staff will be offered a bonus scheme.";
	protected String dc = "Additionally"; 
	
	
	@Override
	public String getArg1() {
		return arg1;
	}
	
	@Override
	public String[] getArg2() {
		return new String[]{arg2};
	}
	
	@Override
	public String getDiscourseConnective() {
		return dc;
	}


	@Override
	public String[] getParseTree() {
		return parseTrees;
	}


	@Override
	public String getSense() {
		return "???";
	}


	@Override
	public String getText() {
		return text;
	}

	@Override
	public List<List<List<String>>> getDependencies() {
		return null;
	}

	
}
