package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ConllSyntaxGoldAnnotatorTest {
	private JCas jCas;

	@Before
	public void setUp() throws UIMAException, IOException{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, new File(ConllJSON.TRIAL_RAW_TEXT_LD), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(ConllJSON.TRIAL_SYNTAX_FILE);
//		AnalysisEngineDescription syntaxParseTreeReader = AnalysisEngineFactory.createEngineDescription(TreebankGoldAnnotator.class);
		
		for (JCas jCas : SimplePipeline.iteratePipeline(reader, conllSyntaxJsonReader)) {
			assertThat(this.jCas).isNull();
			this.jCas = jCas;
		}
	}
	
	@Test
	public void whenReadingTheTrialDataSetThenOnlyOneDocumentExists(){
		
	}
	
	@Test
	public void whenReadingTheTrialDataSetThenThereAre33Sentences(){
		assertThat(JCasUtil.select(jCas, Sentence.class)).hasSize(33);
	}

	@Test
	public void whenReadingTheTrialDataSetThenThereAre896Words(){
		assertThat(JCasUtil.select(jCas, ConllToken.class)).hasSize(896);
	}
	
	@Test
	public void whenReadingTheTriadDataSetThenFirstAndLastTokenBoundariesAreCorrect(){
		Collection<ConllToken> tokens = JCasUtil.select(jCas, ConllToken.class);
		ConllToken firstToken = null, lastToken = null;
		for (ConllToken token: tokens){
			if (firstToken == null)
				firstToken = token;
			lastToken = token; 
		}
		
		assertThat(firstToken.getBegin()).isEqualTo(9);
		assertThat(firstToken.getEnd()).isEqualTo(15);
		assertThat(firstToken.getDocumentOffset()).isEqualTo(0);

		assertThat(lastToken.getBegin()).isEqualTo(4650);
		assertThat(lastToken.getEnd()).isEqualTo(4651);
		assertThat(lastToken.getDocumentOffset()).isEqualTo(895);
	}
	
	
	@Test
	public void whenReadingTheTrialDataSetThenTheNumberOfTopTreeNodeIsEqualToTheNumberOfSent(){
		int numSent = JCasUtil.select(jCas, Sentence.class).size();
		int numTopNode = JCasUtil.select(jCas, ROOT.class).size();
		assertThat(numTopNode).isEqualTo(numSent);
	}

	@Test
	public void whenAddingSyntaxAnnotationsOfTrialDatasetThenTheNumberOfConstituentAndTokensAre707And896(){
		int numToken = JCasUtil.select(jCas, Token.class).size();
		int numTreebank = JCasUtil.select(jCas, Constituent.class).size();
		assertThat(numToken).isEqualTo(896);
		assertThat(numTreebank).isEqualTo(707);
	}
	
	@Test
	public void whenGettingLeavesInTreeThenTheyMatchedWithTokens(){
		Sentence firstSentence = JCasUtil.select(jCas, Sentence.class).iterator().next();
		Constituent firstTreeRoot = JCasUtil.select(jCas, Constituent.class).iterator().next();
		List<Token> tokens = JCasUtil.selectCovered(Token.class, firstSentence);

		List<Annotation> leaves = new ArrayList<>();
		getLeave(firstTreeRoot, leaves);
		
		assertThat(leaves.size()).isEqualTo(tokens.size());
		for (int i = 0; i < leaves.size(); i++){
			assertThat(leaves.get(i).getCoveredText()).isEqualTo(tokens.get(i).getCoveredText());
		}
	}

	private void getLeave(Constituent aNode, List<Annotation> leaves) {
		for (int i = 0; i < aNode.getChildren().size(); i++){

			Annotation aChild = aNode.getChildren(i);
			if (aChild instanceof Constituent)
				getLeave((Constituent)aChild, leaves);
			else
				leaves.add(aChild);
		}
			
	}
	
//	@Test
//	public void whenReadingDataThenDependenciesAreSet(){
//		assertThat(JCasUtil.exists(jCas, DependencyRelation.class)).isTrue();
//		String word = " cut ";
//		
//		int idxWord = jCas.getDocumentText().indexOf(word);
//		List<DependencyNode> dependenciesNodes = JCasUtil.selectCovered(jCas, DependencyNode.class, idxWord, idxWord + word.length());
//		assertThat(dependenciesNodes.size()).isEqualTo(1);
//		
//		DependencyNode wordDependencyNode = dependenciesNodes.get(0);
//		assertThat(wordDependencyNode.getCoveredText()).isEqualTo(word.trim());
//		
//		assertThat(wordDependencyNode.getHeadRelations().size()).isEqualTo(1);
//		assertThat(wordDependencyNode.getHeadRelations(0).getChild()).isEqualTo(wordDependencyNode);
//		assertThat(wordDependencyNode.getChildRelations().size()).isEqualTo(5);
//		for (int i = 0; i < wordDependencyNode.getChildRelations().size(); i++){
//			assertThat(wordDependencyNode.getChildRelations(i).getHead()).isEqualTo(wordDependencyNode);
//		}
//		
//	}
}
