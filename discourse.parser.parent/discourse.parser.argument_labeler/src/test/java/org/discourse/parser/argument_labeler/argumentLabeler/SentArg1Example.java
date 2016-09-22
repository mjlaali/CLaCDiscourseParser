package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.Arrays;
import java.util.List;

import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.json.JSONArray;
import org.json.JSONException;

public class SentArg1Example implements DiscourseRelationExample{
	String text = "He added that \"having just one firm do this isn't going to mean a hill of beans. "
			+ "But if this prompts others to consider the same thing, then it may become much more important.\"";
	
	String[] parseTrees = {
			"( (S (NP (PRP He)) (VP (VBD added) (SBAR (IN that) (`` ``) (S (S (VP (VBG having) (S (NP (RB just) (CD one) (NN firm)) (VP (VB do) (NP (DT this)))))) (VP (VBZ is) (RB n't) (VP (VBG going) (S (VP (TO to) (VP (VB mean) (NP (NP (DT a) (NN hill)) (PP (IN of) (NP (NNS beans)))))))))))) (. .)) )",
			"( (S (CC But) (SBAR (IN if) (S (NP (DT this)) (VP (VBZ prompts) (S (NP (NNS others)) (VP (TO to) (VP (VB consider) (NP (DT the) (JJ same) (NN thing)))))))) (, ,) (ADVP (RB then)) (NP (PRP it)) (VP (MD may) (VP (VB become) (ADJP (ADVP (RB much) (RBR more)) (JJ important)))) (. .)) )"};

	String arg1 = "He added that \"having just one firm do this isn't going to mean a hill of beans";
	String arg2 = "if this prompts others to consider the same thing, then it may become much more important";
	String dc = "But";
	
	String[] dependencies = {
			"[[\"nsubj\", \"added-2\", \"He-1\"], [\"root\", \"ROOT-0\", \"added-2\"], [\"mark\", \"going-13\", \"that-3\"], [\"csubj\", \"going-13\", \"having-5\"], [\"advmod\", \"firm-8\", \"just-6\"], [\"num\", \"firm-8\", \"one-7\"], [\"nsubj\", \"do-9\", \"firm-8\"], [\"ccomp\", \"having-5\", \"do-9\"], [\"dobj\", \"do-9\", \"this-10\"], [\"aux\", \"going-13\", \"is-11\"], [\"neg\", \"going-13\", \"n't-12\"], [\"ccomp\", \"added-2\", \"going-13\"], [\"aux\", \"mean-15\", \"to-14\"], [\"xcomp\", \"going-13\", \"mean-15\"], [\"det\", \"hill-17\", \"a-16\"], [\"dobj\", \"mean-15\", \"hill-17\"], [\"prep\", \"hill-17\", \"of-18\"], [\"pobj\", \"of-18\", \"beans-19\"]]",
			"[[\"cc\", \"important-18\", \"But-1\"], [\"mark\", \"prompts-4\", \"if-2\"], [\"nsubj\", \"prompts-4\", \"this-3\"], [\"advcl\", \"important-18\", \"prompts-4\"], [\"nsubj\", \"consider-7\", \"others-5\"], [\"aux\", \"consider-7\", \"to-6\"], [\"xcomp\", \"prompts-4\", \"consider-7\"], [\"det\", \"thing-10\", \"the-8\"], [\"amod\", \"thing-10\", \"same-9\"], [\"dobj\", \"consider-7\", \"thing-10\"], [\"advmod\", \"important-18\", \"then-12\"], [\"nsubj\", \"important-18\", \"it-13\"], [\"aux\", \"important-18\", \"may-14\"], [\"cop\", \"important-18\", \"become-15\"], [\"advmod\", \"more-17\", \"much-16\"], [\"advmod\", \"important-18\", \"more-17\"], [\"root\", \"ROOT-0\", \"important-18\"]]"
	};
	
	@Override
	public String getText() {
		return text;
	}

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
		return "Comparison.Concession";
	}

	@Override
	public List<List<List<String>>> getDependencies() {
		try {
			return Arrays.asList(DiscourseRelationExample.jSonToList(new JSONArray(dependencies[0])),
					DiscourseRelationExample.jSonToList(new JSONArray(dependencies[1])));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
