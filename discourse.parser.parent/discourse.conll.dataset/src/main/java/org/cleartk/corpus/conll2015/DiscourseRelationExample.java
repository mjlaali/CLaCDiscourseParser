package org.cleartk.corpus.conll2015;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface DiscourseRelationExample {
	String getText();
	String getArg1();
	String[] getArg2();
	String getDiscourseConnective();
	String[] getParseTree();
	String getSense();
	
	public static String toString(String[] segments){
		return Arrays.asList(segments).stream().collect(Collectors.joining(""));
	}
}
