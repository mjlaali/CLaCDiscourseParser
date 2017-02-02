package org.discourse.parser.implicit;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordCoreferenceResolver;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import edu.stanford.nlp.dcoref.Constants;

@Ignore
public class TestCoreferenceResolution {

	@Test
	public void checkTimeComplexity() throws UIMAException, IOException{
		ConllDatasetPathFactory factor = new ConllDatasetPathFactory();
		ConllDatasetPath dataset = factor.makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.trial);
		
		CollectionReader reader = CollectionReaderFactory.createReader(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		JCas jCas = JCasFactory.createJCas();
		if (reader.hasNext())
			reader.getNext(jCas.getCas());
		else
			throw new RuntimeException();
		System.out.println(jCas.getDocumentLanguage());
		
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());
		AnalysisEngineDescription lematizer = AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class);

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());
		
		AnalysisEngineDescription namedEntityRecognizer = AnalysisEngineFactory.createEngineDescription(StanfordNamedEntityRecognizer.class);
		AnalysisEngineDescription coreferenceResolver = AnalysisEngineFactory.createEngineDescription(StanfordCoreferenceResolver.class,
                StanfordCoreferenceResolver.PARAM_SIEVES, Constants.SIEVEPASSES);
		
		SimplePipeline.runPipeline(jCas, conllSyntaxJsonReader, conllGoldJsonReader, lematizer, namedEntityRecognizer, coreferenceResolver);
		Collection<CoreferenceChain> corefs = JCasUtil.select(jCas, CoreferenceChain.class);
		
		for (CoreferenceChain coref: corefs){
			for (CoreferenceLink link: coref.links()){
				System.out.print(link.getCoveredText() + "[" + link.getReferenceType() + ", " + link.getReferenceRelation() + "] ");
			}
			
			System.out.println();
		}
		
	}
	
	@Test
	public void aSimpleTest() throws AnalysisEngineProcessException, ResourceInitializationException{
        AnalysisEngine engine = AnalysisEngineFactory.createEngine(AnalysisEngineFactory.createEngineDescription(
                AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class),
                AnalysisEngineFactory.createEngineDescription(StanfordParser.class,
                        StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                        StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                        StanfordParser.PARAM_WRITE_PENN_TREE, true,
                        StanfordParser.PARAM_WRITE_POS, true),
                AnalysisEngineFactory.createEngineDescription(
                        StanfordNamedEntityRecognizer.class),
                AnalysisEngineFactory.createEngineDescription(StanfordCoreferenceResolver.class,
                        StanfordCoreferenceResolver.PARAM_SIEVES, Constants.SIEVEPASSES)));

//        String aText = "Kemper also blasted the Big Board for ignoring the interests of individual and institutional holders. \"The New York Stock Exchange has vested interests\" in its big member securities firms \"that cloud its objectivity,\" Mr. Timbers said.";
//        String aText = "But the RTC also requires \"working\" capital to maintain the bad assets of thrifts that are sold, until the assets can be sold separately.";
        String aText = "We would have to wait until we have collected on those assets.";
//        String aText = "John bought a car. He is very happy with it.";
        // Set up a simple example
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(aText);
        engine.process(jcas);
        
		Collection<CoreferenceChain> corefs = JCasUtil.select(jcas, CoreferenceChain.class);
		System.out.println("Size = " + corefs.size());
		for (CoreferenceChain coref: corefs){
			for (CoreferenceLink link: coref.links()){
				System.out.print(link.getCoveredText() + "[" + link.getReferenceType() + ", " + link.getReferenceRelation() + "] ");
			}
			
			System.out.println();
		}

	}
}
