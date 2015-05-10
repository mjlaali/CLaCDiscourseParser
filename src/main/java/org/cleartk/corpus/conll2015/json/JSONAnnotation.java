package org.cleartk.corpus.conll2015.json;

import java.util.Map;

public interface JSONAnnotation{
	public String getKey();
	public CharacterSpanList getCharacterSpanList();
	public Map<String, String> getFeatures();
	public String getRawText();
}
