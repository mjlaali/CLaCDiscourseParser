package org.cleartk.corpus.conll2015.loader;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;

public class PreprocessedDataLoader implements ConllDataLoader{
	File preprocessedFilesLocation;
	public PreprocessedDataLoader(File preprocessedFilesLocation) {
		this.preprocessedFilesLocation = preprocessedFilesLocation;
	}

	@Override
	public AnalysisEngineDescription getAnnotator() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DummyAnnontator.class);
	}

	@Override
	public CollectionReaderDescription getReader() throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(XmiReader.class, 
				XmiReader.PARAM_SOURCE_LOCATION, preprocessedFilesLocation, 
				XmiReader.PARAM_PATTERNS, new String[]{"*.xmi"});
	}

}
