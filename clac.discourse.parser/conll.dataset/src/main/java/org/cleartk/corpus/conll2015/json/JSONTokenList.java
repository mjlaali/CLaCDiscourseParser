package org.cleartk.corpus.conll2015.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONTokenList implements ConllJSONObject{
	public static final String TOKEN_LIST_KEY = "TokenList";
	
	private List<JSONToken> tokenList = new ArrayList<JSONToken>();
	
	@Override
	public void init(JSONObject jsonObject) throws JSONException {
		if (!jsonObject.has(TOKEN_LIST_KEY))	//a token list can be optional
			return;
		JSONArray tokenList = (JSONArray) jsonObject.get(TOKEN_LIST_KEY);
		
		for (int i = 0; i < tokenList.length(); i++){
			JSONToken token = new JSONToken();
			token.init(tokenList.getJSONArray(i));
			this.tokenList.add(token);
		}
	}
	
	public void setTokenList(List<JSONToken> tokenList) {
		this.tokenList = tokenList;
	}
	
	public List<JSONToken> getTokenList() {
		return tokenList;
	}

	@Override
	public void toJson(JSONObject jsonObject) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (JSONToken token: tokenList){
			jsonArray.put(token.toJSon());
		}
		jsonObject.put(TOKEN_LIST_KEY, jsonArray);
	}

}
