package org.cleartk.corpus.conll2015.json;

import org.json.JSONException;
import org.json.JSONObject;

public class RawText implements ConllJSONObject{
	public static final String RAW_TEXT_KEY = "RawText";
	private String string;
	
	@Override
	public void init(JSONObject jsonObject) throws JSONException {
		string = jsonObject.getString(RAW_TEXT_KEY);
	}

	public String getString() {
		return string;
	}

	@Override
	public void toJson(JSONObject jsonObject) throws JSONException {
		jsonObject.put(RAW_TEXT_KEY, string);
		
	}
}
