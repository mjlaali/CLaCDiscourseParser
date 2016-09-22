package org.cleartk.corpus.conll2015;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;

public interface DiscourseRelationExample {
	String getText();
	String getArg1();
	String[] getArg2();
	String getDiscourseConnective();
	String[] getParseTree();
	String getSense();
	List<List<List<String>>> getDependencies();
	
	public static String toString(String[] segments){
		return Arrays.asList(segments).stream().collect(Collectors.joining(""));
	}
	
	public static List<List<String>> jSonToList(JSONArray dependencies) throws JSONException{
		
		List<List<String>> dependeciesValues = new ArrayList<>();

		for (int i = 0; i < dependencies.length(); i++){
			JSONArray aJsonDepRel = dependencies.getJSONArray(i);
			String relationType = aJsonDepRel.getString(0);
			String govern = aJsonDepRel.getString(1);
			String dep = aJsonDepRel.getString(2);
			dependeciesValues.add(Arrays.asList(relationType, govern, dep));
		}
		
		return dependeciesValues;
	}
}
