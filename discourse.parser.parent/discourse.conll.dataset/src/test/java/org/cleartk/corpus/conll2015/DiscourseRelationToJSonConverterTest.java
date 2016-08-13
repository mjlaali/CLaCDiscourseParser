package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.DiscourseRelation;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class DiscourseRelationToJSonConverterTest {
	protected JCas jCas;

	@Before
	public void setUp() throws UIMAException, IOException{

		// A collection reader that creates one CAS per file, containing the file's URI
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, new File(ConllJSON.TRIAL_RAW_TEXT_LD), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(new File(ConllJSON.TRIAL_SYNTAX_FILE));
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(new File(ConllJSON.TRIAL_DISCOURSE_FILE));
//		AnalysisEngineDescription syntaxParseTreeReader = AnalysisEngineFactory.createEngineDescription(TreebankGoldAnnotator.class);
		
		for (JCas jCas : SimplePipeline.iteratePipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader)) {
			assertThat(this.jCas).isNull();
			this.jCas = jCas;
		}
	}

	@Test
	public void whenConvertingToJSonTheFormatIsCorrect() throws JAXBException{
		Collection<DiscourseRelation> relations = JCasUtil.select(jCas, DiscourseRelation.class);
		DiscourseRelationToJSonConverter converter = new DiscourseRelationToJSonConverter();
		converter.marshal(relations.iterator().next(), System.out);
	}
}
