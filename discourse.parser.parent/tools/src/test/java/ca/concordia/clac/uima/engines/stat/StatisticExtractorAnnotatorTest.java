package ca.concordia.clac.uima.engines.stat;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class StatisticExtractorAnnotatorTest {
	public static class TokenExtractorPolicy implements ExtractorPolicy<Token> {
		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		@Override
		public InstanceExtractor<Token> getInstanceExtractor() {
			return (aJCas) -> JCasUtil.select(aJCas, Token.class);
		}

		@Override
		public Function<Token, String> getKeyExtractor() {
			return (token) -> Token.class.getName();
		}

		@Override
		public Function<Token, String> getValueExtractor() {
			return (token) -> token.getCoveredText();
		}
		
	}
	
	@Test
	public void givenAListOfTokensWhenExtractingDistributionOfThemThenTheDistributionIsCorrect() throws Exception{
		JCas aJCas = JCasFactory.createJCas();
		
		String text = "It is a test with two words equal to test .";
		aJCas.setDocumentText(text);
		
		String[] words = text.split(" ");
		int start = 0;
		int end;
		for (String word: words){
			end = start + word.length();
			new Token(aJCas, start, end).addToIndexes();
			start = end + 1;
		}
	
		File statOutputFile = new File("outputs/test/StatisticExtractorAnnotatorTest/stats.txt"); 
		SimplePipeline.runPipeline(aJCas, createEngineDescription(StatisticExtractorAnnotator.class,
				StatisticExtractorAnnotator.PARAM_OUTPUT_FILE, statOutputFile, 
				StatisticExtractorAnnotator.PARAM_POLICY_CLASS_NAME, TokenExtractorPolicy.class.getName()));
		
		
		StatisticResult statistic = StatisticFactory.getStatistic(statOutputFile);
		LabeledEnumeratedDistribution distributoin = statistic.getDistribution(Token.class.getName());
		List<String> labels = distributoin.getLabels();
		assertThat(labels).containsOnly(words);
		
		for (int i = 0; i < labels.size(); i++){
			if (labels.get(i).equals("test")){
				assertThat(distributoin.probability(i)).isCloseTo(2.0 / words.length, within(1.0E-10));
			} else {
				assertThat(distributoin.probability(i)).isEqualTo(1.0 / words.length, within(1.0E-10));
			}
			
		}
		
	}
}
