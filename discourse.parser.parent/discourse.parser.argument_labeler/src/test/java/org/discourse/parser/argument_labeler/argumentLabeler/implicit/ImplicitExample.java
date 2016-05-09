package org.discourse.parser.argument_labeler.argumentLabeler.implicit;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.json.JSONArray;
import org.json.JSONException;

public class ImplicitExample implements DiscourseRelationExample {
	String text = "\"We continue to believe the position we've taken is reasonable,\" a Morgan Stanley official said. \"We would stop index arbitrage when the market is under stress, and we have recently,\" he said, citing Oct. 13 and earlier this week.";
	String arg1 = "the position we've taken is reasonable";
	String[] arg2 = {"We would stop index arbitrage when the market is under stress"};
	String[] parseTrees = {
			"( (S (`` ``) (S (NP (PRP We)) (VP (VBP continue) (S (VP (TO to) (VP (VB believe) (SBAR (S (NP (NP (DT the) (NN position)) (SBAR (S (NP (PRP we)) (VP (VBP 've) (VP (VBN taken)))))) (VP (VBZ is) (ADJP (JJ reasonable)))))))))) (, ,) ('' '') (NP (DT a) (NNP Morgan) (NNP Stanley) (NN official)) (VP (VBD said)) (. .)) )",
			"( (S (`` ``) (S (S (NP (PRP We)) (VP (MD would) (VP (VB stop) (NP (NN index) (NN arbitrage)) (SBAR (WHADVP (WRB when)) (S (NP (DT the) (NN market)) (VP (VBZ is) (PP (IN under) (NP (NN stress))))))))) (, ,) (CC and) (S (NP (PRP we)) (VP (VBP have) (ADVP (RB recently))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said) (, ,) (S (VP (VBG citing) (NP (NP (NNP Oct.) (CD 13)) (CC and) (NP (RBR earlier) (DT this) (NN week)))))) (. .)) )"
	};

	String[] dependencies = {
			"[[\"nsubj\", \"continue-3\", \"We-2\"], [\"ccomp\", \"said-19\", \"continue-3\"], [\"aux\", \"believe-5\", \"to-4\"], [\"xcomp\", \"continue-3\", \"believe-5\"], [\"det\", \"position-7\", \"the-6\"], [\"nsubj\", \"reasonable-12\", \"position-7\"], [\"nsubj\", \"taken-10\", \"we-8\"], [\"aux\", \"taken-10\", \"'ve-9\"], [\"rcmod\", \"position-7\", \"taken-10\"], [\"cop\", \"reasonable-12\", \"is-11\"], [\"ccomp\", \"believe-5\", \"reasonable-12\"], [\"det\", \"official-18\", \"a-15\"], [\"nn\", \"official-18\", \"Morgan-16\"], [\"nn\", \"official-18\", \"Stanley-17\"], [\"nsubj\", \"said-19\", \"official-18\"], [\"root\", \"ROOT-0\", \"said-19\"]]",
			"[[\"nsubj\", \"stop-4\", \"We-2\"], [\"aux\", \"stop-4\", \"would-3\"], [\"ccomp\", \"said-21\", \"stop-4\"], [\"nn\", \"arbitrage-6\", \"index-5\"], [\"dobj\", \"stop-4\", \"arbitrage-6\"], [\"advmod\", \"is-10\", \"when-7\"], [\"det\", \"market-9\", \"the-8\"], [\"nsubj\", \"is-10\", \"market-9\"], [\"advcl\", \"stop-4\", \"is-10\"], [\"prep\", \"is-10\", \"under-11\"], [\"pobj\", \"under-11\", \"stress-12\"], [\"cc\", \"stop-4\", \"and-14\"], [\"nsubj\", \"have-16\", \"we-15\"], [\"conj\", \"stop-4\", \"have-16\"], [\"advmod\", \"have-16\", \"recently-17\"], [\"nsubj\", \"said-21\", \"he-20\"], [\"root\", \"ROOT-0\", \"said-21\"], [\"xcomp\", \"said-21\", \"citing-23\"], [\"dobj\", \"citing-23\", \"Oct.-24\"], [\"num\", \"Oct.-24\", \"13-25\"], [\"cc\", \"Oct.-24\", \"and-26\"], [\"advmod\", \"week-29\", \"earlier-27\"], [\"det\", \"week-29\", \"this-28\"], [\"conj\", \"Oct.-24\", \"week-29\"]]"
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
		return arg2;
	}

	@Override
	public String getDiscourseConnective() {
		return null;
	}

	@Override
	public String[] getParseTree() {
		return parseTrees;
	}

	@Override
	public String getSense() {
		return "Expansion.Restatement";
	}

	@Override
	public List<List<List<String>>> getDependencies() {
		try {
			List<List<List<String>>> results = new ArrayList<>();
			for (String dependency: dependencies){
				results.add(DiscourseRelationExample.jSonToList(new JSONArray(dependency)));
			}
			return results;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}	
	}

}
