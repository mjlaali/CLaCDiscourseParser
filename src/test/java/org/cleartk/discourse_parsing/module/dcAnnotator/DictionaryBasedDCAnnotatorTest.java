package org.cleartk.discourse_parsing.module.dcAnnotator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.discourse.type.DiscourseConnective;
import org.junit.Test;

public class DictionaryBasedDCAnnotatorTest {

	@Test
	public void givenAListOfDcsWhenIndexingAllTheOccurrencesAreTagged() throws UIMAException, IOException{
		JCas ajCas = JCasFactory.createJCas();
		
		String parseTree = "(ROOT (S (NP (PRP It)) (VP (VBZ is) (NP (DT a) (NN test)))))";
		SyntaxReader syntaxReader = new SyntaxReader();
		syntaxReader.initJCas(ajCas, parseTree);
		
		String text = ajCas.getDocumentText();
		
		String[] words = text.toLowerCase().split(" ");
		String filePath = "outputs/test/dc-test.txt";
		FileUtils.writeLines(new File(filePath), Arrays.asList(words));
		
		
		AnalysisEngineDescription description = DictionaryBasedDCAnnotator.getDescription(filePath);
		AnalysisEngine dcAnnotator = AnalysisEngineFactory.createEngine(description);
		dcAnnotator.process(ajCas);
		
		Collection<DiscourseConnective> select = JCasUtil.select(ajCas, DiscourseConnective.class);
		assertThat(select).hasSize(4);
		
	}
}
