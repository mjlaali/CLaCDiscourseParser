package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.Collections;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.json.JSONArray;
import org.json.JSONException;

public class NoneNodeExample implements DiscourseRelationExample{
	String text = "\"The impression I've got is they'd love to do away with it {program trading}, but they {the exchange} can't do it,\" he said. ";
	String arg1 = "they'd love to do away with it {program trading}";
	String arg2 = "they {the exchange} can't do it";
	String connective = "but";
	String sense = "Comparison.Contrast";
	String parseTree = "( (S (`` ``) (S (S (NP (NP (DT The) (NN impression)) (SBAR (S (NP (PRP I)) (VP (VBP 've) "
			+ "(VP (VBD got)))))) (VP (VBZ is) (SBAR (S (NP (PRP they)) (VP (MD 'd) (VP (VB love) (S (VP (TO to) "
			+ "(VP (VB do) (ADVP (RB away) (PP (IN with) (NP (PRP it)))) (PRN (-LRB- -LCB-) (NP (NN program) (NN trading)) "
			+ "(-RRB- -RCB-))))))))))) (, ,) (CC but) (S (NP (NP (PRP they)) (PRN (-LRB- -LCB-) (NP (DT the) (NN exchange)) "
			+ "(-RRB- -RCB-))) (VP (MD ca) (RB n't) (VP (VB do) (NP (PRP it)))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said)) (. .)) )";
	
	String dependencies = "[[\"det\", \"impression-3\", \"The-2\"], [\"nsubj\", \"is-7\", \"impression-3\"], "
			+ "[\"nsubj\", \"got-6\", \"I-4\"], [\"aux\", \"got-6\", \"'ve-5\"], [\"rcmod\", \"impression-3\", \"got-6\"], "
			+ "[\"ccomp\", \"said-34\", \"is-7\"], [\"nsubj\", \"love-10\", \"they-8\"], [\"aux\", \"love-10\", \"'d-9\"], "
			+ "[\"ccomp\", \"is-7\", \"love-10\"], [\"aux\", \"do-12\", \"to-11\"], [\"xcomp\", \"love-10\", \"do-12\"], "
			+ "[\"advmod\", \"do-12\", \"away-13\"], [\"prep\", \"away-13\", \"with-14\"], [\"pobj\", \"with-14\", \"it-15\"], "
			+ "[\"nn\", \"trading-18\", \"program-17\"], [\"dep\", \"do-12\", \"trading-18\"], [\"cc\", \"is-7\", \"but-21\"], "
			+ "[\"nsubj\", \"do-29\", \"they-22\"], [\"det\", \"exchange-25\", \"the-24\"], [\"appos\", \"they-22\", \"exchange-25\"], "
			+ "[\"aux\", \"do-29\", \"ca-27\"], [\"neg\", \"do-29\", \"n't-28\"], [\"conj\", \"is-7\", \"do-29\"], "
			+ "[\"dobj\", \"do-29\", \"it-30\"], [\"nsubj\", \"said-34\", \"he-33\"], [\"root\", \"ROOT-0\", \"said-34\"]]";

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

	@Override
	public List<List<List<String>>> getDependencies() {
		try {
			return Collections.singletonList(DiscourseRelationExample.jSonToList(new JSONArray(dependencies)));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	
	
}
