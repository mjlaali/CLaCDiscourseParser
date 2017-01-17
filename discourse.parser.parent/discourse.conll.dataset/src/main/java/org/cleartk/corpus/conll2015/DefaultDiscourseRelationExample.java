package org.cleartk.corpus.conll2015;

import java.util.List;

public class DefaultDiscourseRelationExample implements DiscourseRelationExample{
	protected String text;
	protected String[] parseTrees;
	protected String arg1;
	protected String arg2;
	protected String dc; 
	protected String sense;


	public DefaultDiscourseRelationExample(String text, String[] parseTrees, 
			String arg1, String arg2, String dc, String sense) {
		super();
		this.text = text;
		this.parseTrees = parseTrees;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.dc = dc;
		this.sense = sense;
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
		return sense;
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
