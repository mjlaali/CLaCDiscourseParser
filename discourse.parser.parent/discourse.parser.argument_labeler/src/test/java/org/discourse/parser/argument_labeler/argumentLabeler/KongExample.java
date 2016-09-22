package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.List;

import org.cleartk.corpus.conll2015.DiscourseRelationExample;

public class KongExample implements DiscourseRelationExample{
	protected String parseTree = "(S (CC But)(NP (PRP its) (NNS competitors))(VP(VP (VBP have)(NP (RB much) (JJR broader) (NN business) (NNS interests)))(CC and)(RB so)(VP (VBP are)(VP (ADVP (RBR better))(VBN cushioned)(PP (IN against)(NP (NN price) (NNS swings)))))))";
	protected String arg1 = "But its competitors have much broader business interests";
	protected String[] arg2 = new String[]{"and ", "are better cushioned against price swings"};
	protected String dc = "so";
	private String text = "But its competitors have much broader business interests and so are better cushioned against price swings";
	
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
		return new String[]{parseTree};
	}

	@Override
	public String getSense() {
		return "???";
	}

	@Override
	public List<List<List<String>>> getDependencies() {
		return null;
	}
	
}
