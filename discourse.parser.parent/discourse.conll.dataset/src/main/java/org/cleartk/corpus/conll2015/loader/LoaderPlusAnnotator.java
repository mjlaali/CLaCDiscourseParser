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
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class LoaderPlusAnnotator implements ConllDataLoader{
	private ConllDatasetPath dataset;
	private File outputFolder;
	public LoaderPlusAnnotator(ConllDatasetPath dataset, File outputFolder){
		this.dataset = dataset;
		this.outputFolder = outputFolder;
	}
	
	@Override
	public AnalysisEngineDescription getAnnotator() throws ResourceInitializationException {
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile()));
		builder.add(ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationNoSenseFile()));
		builder.add(AnalysisEngineFactory.createEngineDescription(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, outputFolder));
		return builder.createAggregateDescription();
	}

	@Override
	public CollectionReaderDescription getReader() throws ResourceInitializationException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "*");
		return reader;
	}

}
