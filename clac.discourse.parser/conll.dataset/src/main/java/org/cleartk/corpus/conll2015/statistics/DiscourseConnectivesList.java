package org.cleartk.corpus.conll2015.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

public class DiscourseConnectivesList {
	public static final String CONLL_JSON_CONNECTIVES_HEAD_FILE = "data/validator/conn_head_mapping.json";
	public static final String DISCOURSE_CONNECTIVES_LIST_FILE = "data/analysisResults/dcHeadList.txt";
	public static final String DISCOURSE_CONNECTIVES_MOST_FREQUENT_RELATION_FILE = "data/analysisResults/dcMostFrequentRelation.csv";

	public static void extractAListFormAJSonFile(String headFile) throws JSONException, FileNotFoundException {
		Scanner scanner = new Scanner(new File(headFile));
		
		StringBuilder sb = new StringBuilder();
		while (scanner.hasNext()){
			String line = scanner.nextLine();
			sb.append(line);
		}
		JSONObject map = new JSONObject(sb.toString());
		Set<String> heads = new TreeSet<String>();
		@SuppressWarnings("unchecked")
		Iterator<String> keys = map.keys();
		while (keys.hasNext()){
			String aDc = keys.next();
			String head = map.getString(aDc);
			boolean included = aDc.toLowerCase().matches(".*\\b" + head + "\\b.*");
			if (!included)
				System.err.println("Manually add this dc: " + aDc + "\t->\t" + head);
			heads.add(head);
		}
		
		PrintStream ps = new PrintStream(DISCOURSE_CONNECTIVES_LIST_FILE);
		for (String head: heads){
			ps.println(head);
		}
		ps.close();
		scanner.close();
	}
	
	public static void main(String[] args) throws JSONException, FileNotFoundException {
		DiscourseConnectivesList.extractAListFormAJSonFile(CONLL_JSON_CONNECTIVES_HEAD_FILE);
	}
}
