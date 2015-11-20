package org.cleartk.corpus.conll2015.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SentenceTokenFinder {
	private String[] SentIDX=new String[2];
	private String[] TokenIDX=new String[4];

	
	public String[] SentFinder(JSONArray WordList) throws JSONException
	{
		
		JSONArray FirstWord = (JSONArray) WordList.get(0);
		JSONObject FirstWordSpec = (JSONObject) FirstWord.get(1);
		SentIDX[0] = FirstWordSpec.get("CharacterOffsetBegin").toString();
		JSONArray EndWord = (JSONArray) WordList.get(WordList.length()-1);
		JSONObject EndWordSpec = (JSONObject) EndWord.get(1);
		SentIDX[1] = EndWordSpec.get("CharacterOffsetEnd").toString();
		
		
		return SentIDX;
	}
	
	public String[] TokenFinder(JSONArray Word) throws JSONException
	{
		String WordTonen = Word.get(0).toString();
		JSONObject WordSpec = (JSONObject) Word.get(1);
		
		//System.out.println(WordSpec);
		TokenIDX[0] = WordTonen;
		TokenIDX[1] = WordSpec.get("CharacterOffsetBegin").toString();
		TokenIDX[2] = WordSpec.get("CharacterOffsetEnd").toString();
		TokenIDX[3] = WordSpec.get("PartOfSpeech").toString();
		
		return TokenIDX;
	}
	
	
	
	
	
}
