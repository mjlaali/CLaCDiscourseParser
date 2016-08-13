package org.cleartk.corpus.conll2015.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CharacterSpanList implements ConllJSONObject{
	public static final String CHARACTER_SPAN_LIST_KEY = "CharacterSpanList";
	private List<Long> starts = new ArrayList<Long>();
	private List<Long> ends = new ArrayList<Long>();

	public CharacterSpanList(long start, long end) {
		starts.add(start);
		ends.add(end);
	}
	
	public CharacterSpanList() {
	}
	
	public void init(JSONObject jsonObject) throws JSONException{
		JSONArray jsonArr = (JSONArray) jsonObject.get(CHARACTER_SPAN_LIST_KEY);
		if (jsonArr.length() == 0)
			return ;
		
		for (int i = 0; i < jsonArr.length(); i++){
			JSONArray pos = (JSONArray) jsonArr.get(i); 
			if (pos.getLong(0) > pos.getLong(1) || pos.length() != 2)
				throw new RuntimeException("" + (pos.getLong(0) > pos.getLong(1)) + ", " + pos.getLong(0) + ", "+ pos.getLong(1) + 
						 ", " + pos.length() + ", " + jsonObject.toString());
			starts.add(pos.getLong(0));
			ends.add(pos.getLong(1));
		}
	}
	
	public List<Long> getStarts(){
		
		return starts;
	}
	
	public List<Long> getEnds(){
		return ends;
	}

	@Override
	public void toJson(JSONObject jsonObject) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < starts.size(); i++){
			JSONArray poses = new JSONArray();
			poses.put(starts.get(i));
			poses.put(ends.get(i));
			jsonArray.put(poses);
		}
		jsonObject.put(CHARACTER_SPAN_LIST_KEY, jsonArray);
	}

	
}
