package org.cleartk.corpus.conll2015.loader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

public interface ConllDataLoader {
	public AnalysisEngineDescription getAnnotator(boolean addMultiSense) throws ResourceInitializationException;
	public CollectionReaderDescription getReader() throws ResourceInitializationException;
	
}
