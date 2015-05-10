package org.cleartk.corpus.conll2015.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONRelation implements ConllJSONObject{
	private Map<String, JSONComplexAnnotation> annotaions = new TreeMap<String, JSONComplexAnnotation>();
	private Map<String, Object> features = new TreeMap<String, Object>();

	@Override
	public void init(JSONObject jsonObject) throws JSONException {
		JSONArray names = jsonObject.names();
		
		for (int i = 0; i < names.length(); i++){
			String key = names.getString(i);
			JSONComplexAnnotation annotation = new JSONComplexAnnotation(key);
			
			try {
				annotation.init(jsonObject);
				annotaions.put(annotation.getKey(), annotation);
			} catch (ClassCastException e) {	//it is feature not annotation
				Object value = jsonObject.get(key);
				if (value instanceof JSONArray){
					JSONArray arr = (JSONArray) value;
					List<String> values = new ArrayList<String>();
					for (int j = 0; j < arr.length(); j++){
						values.add(arr.getString(j));
					}
					features.put(key, values);
				} else
					features.put(key, value.toString());
			}
		}
	}
	
	public Map<String, JSONComplexAnnotation> getAnnotaions() {
		return annotaions;
	}
	
	public Map<String, Object> getFeatures() {
		return features;
	}

//	public void setDocument(Document doc){
//		for (JSONAnnotation jsonAnnotation: annotaions){
//			((JSONComplexAnnotation)jsonAnnotation).testInconsistency(doc);
//		}
//	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void toJson(JSONObject jsonObject) throws JSONException {
		for (JSONAnnotation jsonAnnotation: annotaions.values()){
			((JSONComplexAnnotation)jsonAnnotation).toJson(jsonObject);
		}

		for (Entry<String, Object> aFeatureVal: features.entrySet()){
			Object value = aFeatureVal.getValue();
			Object jsonValue;
			if (value instanceof List){
				JSONArray array = new JSONArray();
				for (Object v: (List)value){
					array.put(v);
				}
				jsonValue = array;
			} else
				jsonValue = value;
			jsonObject.put(aFeatureVal.getKey(), jsonValue);
		}
	}
	
}
