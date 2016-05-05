package org.cleartk.corpus.conll2015.loader;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;

import ca.concordia.clac.uima.engines.CoreferenceToDependencyAnnotator;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordCoreferenceResolver;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import edu.stanford.nlp.dcoref.Constants;

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
		
		AnalysisEngineDescription lematizer = AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class);
		builder.add(lematizer);
		
		AnalysisEngineDescription namedEntityRecognizer = AnalysisEngineFactory.createEngineDescription(StanfordNamedEntityRecognizer.class);
		builder.add(namedEntityRecognizer);
		
		AnalysisEngineDescription coreferenceResolver = AnalysisEngineFactory.createEngineDescription(StanfordCoreferenceResolver.class,
				StanfordCoreferenceResolver.PARAM_SIEVES, Constants.SIEVEPASSES);
		builder.add(coreferenceResolver);
		
		AnalysisEngineDescription corefrenceToDependency = CoreferenceToDependencyAnnotator.getDescription();
		builder.add(corefrenceToDependency);

		builder.add(AnalysisEngineFactory.createEngineDescription(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, outputFolder));
		builder.add(ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationNoSenseFile()));

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

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		File dataFld = new File("data/");
		DatasetMode mode = DatasetMode.dev;
		
		ConllDatasetPath path = new ConllDatasetPathFactory().makeADataset2016(dataFld, mode);
		ConllDataLoaderFactory.clean(path);
		ConllDataLoader instance  = ConllDataLoaderFactory.getInstance(path);
		
		SimplePipeline.runPipeline(instance.getReader(), instance.getAnnotator());
	}
	

}
