package org.cleartk.corpus.conll2015.json;

import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONComplexAnnotation implements ConllJSONObject, JSONAnnotation{
//	private static int err = 0;
	private String key;
	private CharacterSpanList characterSpanList = new CharacterSpanList();
	private RawText rawText = new RawText();
	private JSONTokenList tokenList = new JSONTokenList();
	
	private ConllJSONObject[] jsonReaders = new ConllJSONObject[]{characterSpanList, rawText, tokenList};
	
	public JSONComplexAnnotation(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	@Override
	public void init(JSONObject jsonObject) throws JSONException {
		JSONObject annotation = (JSONObject) jsonObject.get(key);
		
		for (ConllJSONObject reader: jsonReaders){
			reader.init(annotation);
		}
		
	}

	@Override
	public void toJson(JSONObject jsonObject) throws JSONException {
		JSONObject json = new JSONObject();
		for (ConllJSONObject reader: jsonReaders){
			reader.toJson(json);
		}
		jsonObject.put(key, json);
	}

//	public void testInconsistency(Document doc) {
//		AnnotationSet docTokens = doc.getAnnotations().get(ANNIEConstants.TOKEN_ANNOTATION_TYPE);
//		List<Long> ends = characterSpanList.getEnds();
//		List<Long> starts = characterSpanList.getStarts();
//		List<Annotation> tokensAnn = new ArrayList<Annotation>();
//		
//		for (int spanIdx = 0; spanIdx < ends.size(); spanIdx++){
//			long spanStart = starts.get(spanIdx);
//			long spanEnd = ends.get(spanIdx);
//			List<Annotation> spanTokens = Utils.inDocumentOrder(docTokens.get(spanStart, spanEnd));
//			long lastTokenEndOffset = spanTokens.get(spanTokens.size() - 1).getEndNode().getOffset();
//			long firstTokenBeginOffset = spanTokens.get(0).getStartNode().getOffset();
//			spanTokens = Utils.inDocumentOrder(docTokens.get(firstTokenBeginOffset, lastTokenEndOffset));		//U.S. has two tokens, one 'U.S.' and the other '.', this line add the second token.
//			tokensAnn.addAll(spanTokens);
//			if (spanEnd != lastTokenEndOffset || firstTokenBeginOffset != spanStart){
//				System.out.println("JSONComplexAnnotation.testInconsistency(): " + ++err);
//				ends.remove(spanIdx);
//				ends.add(spanIdx, lastTokenEndOffset);
//				starts.remove(spanIdx);
//				starts.add(spanIdx, firstTokenBeginOffset);
//			}
//		}
//		
//		tokenList.setTokenList(converToTokenList(tokensAnn));
//		
//		
//		//optional, can be removed
//		List<Token> tokens = tokenList.getTokenList();
//		if (tokens.size() == 0)
//			return;
//		int startTokens = tokens.get(0).getBegin();
//		int endTokens = tokens.get(tokens.size() - 1).getEnd();
//		
//		int startSpan = Integer.MAX_VALUE;
//		for (long idx: starts){
//			if (startSpan > idx)
//				startSpan = (int)idx;
//		}
//		
//		int endSpan = 0;
//		for (long idx: ends){
//			if (endSpan < idx)
//				endSpan = (int)idx;
//		}
//
//		if (startTokens != startSpan || endTokens != endSpan){
//			System.err.print("JSONComplexAnnotation.init(): <" + startTokens + " != " + startSpan + ">, <" + endTokens + " != "+ endSpan + ">");
//			System.err.println("\t" + rawText.getString());
//			update(startTokens, startSpan, starts);
//			update(endTokens, endSpan, ends);
//		}
//	}
//
//	private List<Token> converToTokenList(List<Annotation> tokens){
//		List<Token> tokenList = new ArrayList<Token>();
//		for (Annotation tokenAnnotation: tokens){
//			Token toAdd = new Token();
//			toAdd.init(tokenAnnotation.getStartNode().getOffset().intValue(), 
//					tokenAnnotation.getEndNode().getOffset().intValue(), 
//					Integer.parseInt(tokenAnnotation.getFeatures().get("IndexInDoc").toString()), 
//					Integer.parseInt(tokenAnnotation.getFeatures().get("IndexOfJSONParseLine").toString()), 
//					Integer.parseInt(tokenAnnotation.getFeatures().get("IndexInJSONParseLine").toString()) 
//					);
//			tokenList.add(toAdd);
//		}
//		return tokenList;
//	}


//	private void update(int startTokens, int startSpan, List<Long> starts) {
//		int idx = starts.indexOf(new Long(startSpan));
//		starts.remove(idx);
//		starts.add(idx, new Long(startTokens));
//	}
	
	public CharacterSpanList getCharacterSpanList() {
		return characterSpanList;
	}
	
	public ConllJSONObject[] getJsonReaders() {
		return jsonReaders;
	}
	
	public JSONTokenList getTokenList() {
		return tokenList;
	}

	public String getRawText() {
		return rawText.getString();
	}

	@Override
	public Map<String, String> getFeatures() {
		return new TreeMap<String, String>();
	}

	

}
