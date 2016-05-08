package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.util.Collections;
import java.util.List;

import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.json.JSONArray;
import org.json.JSONException;

public class NodeRemoverExample implements DiscourseRelationExample{
	String text = "We've talked to proponents of index arbitrage and told them to cool it because they're ruining the market.";
	String arg1 = "and told them to cool it";
	String[] arg2 = {"they're ruining the market"};
	String dc = "because";
	String[] parseTrees = {"( (S (NP (PRP We)) (VP (VBP 've) (VP (VP (VBN talked) (PP (TO to) (NP (NP (NNS proponents)) (PP (IN of) (NP (NN index) (NN arbitrage)))))) (CC and) (VP (VBD told) (NP (PRP them)) (S (VP (TO to) (VP (VB cool) (NP (PRP it)) (SBAR (IN because) (S (NP (PRP they)) (VP (VBP 're) (VP (VBG ruining) (NP (DT the) (NN market)))))))))))) (. .)) )"};
	String sense = "Contingency.Cause.Reason";
	String dependencies = "[[\"nsubj\", \"talked-3\", \"We-1\"], [\"aux\", \"talked-3\", \"'ve-2\"], [\"root\", \"ROOT-0\", \"talked-3\"], [\"prep\", \"talked-3\", \"to-4\"], [\"pobj\", \"to-4\", \"proponents-5\"], [\"prep\", \"proponents-5\", \"of-6\"], [\"nn\", \"arbitrage-8\", \"index-7\"], [\"pobj\", \"of-6\", \"arbitrage-8\"], [\"cc\", \"talked-3\", \"and-9\"], [\"conj\", \"talked-3\", \"told-10\"], [\"dobj\", \"told-10\", \"them-11\"], [\"aux\", \"cool-13\", \"to-12\"], [\"xcomp\", \"told-10\", \"cool-13\"], [\"dobj\", \"cool-13\", \"it-14\"], [\"mark\", \"ruining-18\", \"because-15\"], [\"nsubj\", \"ruining-18\", \"they-16\"], [\"aux\", \"ruining-18\", \"'re-17\"], [\"advcl\", \"cool-13\", \"ruining-18\"], [\"det\", \"market-20\", \"the-19\"], [\"dobj\", \"ruining-18\", \"market-20\"]]";

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
		return dc;
	}

	@Override
	public String[] getParseTree() {
		return parseTrees;
	}

	@Override
	public String getSense() {
		return sense;
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
