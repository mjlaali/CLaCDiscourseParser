package org.cleartk.corpus.conll2015.json;

import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONToken implements JSONAnnotation{
	public static final String OFFSET_IN_SENT = "IndexInSentence";
	public static final String SENT_OFFSET = "IndexOfSentence";
	public static final String DOC_OFFSET = "IndexInDoc";
	
	private int begin;
	private int end;
	private int docOffset;
	private int sentOffset;
	private int offsetInSent;
	
	public void init(JSONArray jsonArray) throws JSONException {
		begin = jsonArray.getInt(0);
		end = jsonArray.getInt(1);
		docOffset = jsonArray.getInt(2);
		sentOffset = jsonArray.getInt(3);
		offsetInSent = jsonArray.getInt(4);
		
	}
	
	public void init(int... values){
		begin = values[0];
		end = values[1];
		docOffset = values[2];
		sentOffset = values[3];
		offsetInSent = values[4];
		
	}
	
	public JSONArray toJSon(){
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(begin);
		jsonArray.put(end);
		jsonArray.put(docOffset);
		jsonArray.put(sentOffset);
		jsonArray.put(offsetInSent);
		return jsonArray;
	}
	
	
	
	public int getBegin() {
		return begin;
	}
	
	public int getEnd() {
		return end;
	}
	
	public int getDocOffset() {
		return docOffset;
	}
	
	public int getSentOffset() {
		return sentOffset;
	}
	
	public int getOffsetInSent() {
		return offsetInSent;
	}

	@Override
	public String getKey() {
		return "token";
	}

	@Override
	public CharacterSpanList getCharacterSpanList() {
		return new CharacterSpanList(begin, end);
	}

	@Override
	public Map<String, String> getFeatures() {
		Map<String, String> features = new TreeMap<String, String>();
		features.put(DOC_OFFSET, "" + docOffset);
		features.put(SENT_OFFSET, "" + sentOffset);
		features.put(OFFSET_IN_SENT, "" + offsetInSent);
		return features;
	}

	@Override
	public String getRawText() {
		return null;
	}
}
