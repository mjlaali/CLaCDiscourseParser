package org.cleartk.corpus.conll2015.json;

import org.json.JSONException;
import org.json.JSONObject;

public interface ConllJSONObject {

	public void init(JSONObject jsonObject) throws JSONException;
	public void toJson(JSONObject jsonObject) throws JSONException;
}
