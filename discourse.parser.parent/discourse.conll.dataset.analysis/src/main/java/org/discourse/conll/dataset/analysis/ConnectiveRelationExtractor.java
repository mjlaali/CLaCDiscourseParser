package org.discourse.conll.dataset.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ConnectiveRelationExtractor extends JCasAnnotator_ImplBase{
	PrintWriter writer;
	
	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(ConnectiveRelationExtractor.class);
	}
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			writer = new PrintWriter(new FileWriter("outputs/dc_sense.txt", false));
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		for (DiscourseConnective dc: JCasUtil.select(aJCas, DiscourseConnective.class)){
			String dcText = TokenListTools.getTokenListText(dc).toLowerCase();
			String sense = dc.getSense();
			writer.println(dcText + "\t" + sense);
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		writer.close();
	}
	
	public static void main(String[] args) throws UIMAException, IOException {
		File dataFld = new File("../discourse.conll.dataset/data");
		DatasetMode mode = DatasetMode.train;
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(dataFld, mode);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());
		
		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllGoldJsonReader, getDescription());
	}
}
