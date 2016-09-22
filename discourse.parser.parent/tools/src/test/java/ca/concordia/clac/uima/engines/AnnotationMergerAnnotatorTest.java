package ca.concordia.clac.uima.engines;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class AnnotationMergerAnnotatorTest {
	public static class TestMergePolicy implements MergePolicy<Token>{

		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		@Override
		public InstanceExtractor<Token> getInstanceExtractor() {
			return (aJCas) -> JCasUtil.select(aJCas, Token.class);
		}

		@Override
		public BiFunction<Token, Token, Token> getMerger() {
			return (t1, t2) -> t1.getStem() == null ? t2 : t1;
		}
		
	}

	@Test
	public void givenASetOfTokensWithTwoDuplicateTokenWhenRemovingDuplicatsThenJustTheDuplicateTokenIsRemoved() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		TokenBuilder<Token, Sentence> tokenBuilder = new TokenBuilder<>(Token.class, Sentence.class, "pos", "stem");
		String stems = "It is a test .";
		tokenBuilder.buildTokens(aJCas, "It is a test.", stems);
		
		new Token(aJCas, 0, 2).addToIndexes(); //duplicate token
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		assertThat(tokens).hasSize(6);
		
		SimplePipeline.runPipeline(aJCas, createEngineDescription(AnnotationMergerAnnotator.class, 
				AnnotationMergerAnnotator.PARAM_MERGE_POLICY_CLS_NAME, TestMergePolicy.class.getName()));
		
		tokens = JCasUtil.select(aJCas, Token.class);
		assertThat(tokens).hasSize(5);
		
		List<String> strToken = tokens.stream().map(Token::getCoveredText).collect(Collectors.toList());
		assertThat(strToken).containsOnly(stems.split(" "));
	}
}
