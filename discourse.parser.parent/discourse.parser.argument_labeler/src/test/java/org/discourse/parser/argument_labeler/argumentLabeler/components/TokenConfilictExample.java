package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.util.Collections;
import java.util.List;

import org.cleartk.corpus.conll2015.DiscourseRelationExample;

public class TokenConfilictExample implements DiscourseRelationExample{
	String text = "\"We would have to wait until we have collected on those assets before we can move forward,\" he said.";
	String arg1 = "We would have to wait";
	String[] arg2 = {"we have collected on those assets"};
	String dc = "until";
	String[] parseTrees = {"( (S (`` ``) (S (NP (PRP We)) (VP (MD would) (VP (VB have) (S (VP (TO to) (VP (VB wait) (SBAR (IN until) (S (NP (PRP we)) (VP (VBP have) (VP (VBN collected) (PP (IN on) (NP (DT those) (NNS assets))) (SBAR (IN before) (S (NP (PRP we)) (VP (MD can) (VP (VB move) (ADVP (RB forward)))))))))))))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said)) (. .)) )"};
	String sense = "Temporal.Asynchronous.Precedence";
	String dependencies = "[[]]";

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
		return Collections.emptyList();
	}

}
