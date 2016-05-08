package org.cleartk.corpus.conll2015.loader;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;

public class PreprocessedDataLoader implements ConllDataLoader{
	private File preprocessedFilesLocation;
	private ConllDatasetPath dataset;
	public PreprocessedDataLoader(File preprocessedFilesLocation, ConllDatasetPath dataset) {
		this.preprocessedFilesLocation = preprocessedFilesLocation;
		this.dataset = dataset;
		
	}

	@Override
	public AnalysisEngineDescription getAnnotator(boolean addMultiSense) throws ResourceInitializationException {
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(AnalysisEngineFactory.createEngineDescription(DummyAnnontator.class));
		builder.add(ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile(), addMultiSense));
		return builder.createAggregateDescription();
	}

	@Override
	public CollectionReaderDescription getReader() throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(XmiReader.class, 
				XmiReader.PARAM_SOURCE_LOCATION, preprocessedFilesLocation, 
				XmiReader.PARAM_PATTERNS, new String[]{"*.xmi"});
	}

}
