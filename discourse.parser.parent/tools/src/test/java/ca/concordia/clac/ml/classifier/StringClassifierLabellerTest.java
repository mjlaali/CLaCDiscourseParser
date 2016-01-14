package ca.concordia.clac.ml.classifier;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class StringClassifierLabellerTest {
	public static class TokenClassificationAlgorithmFactory implements ClassifierAlgorithmFactory<String, Token>{
		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		@Override
		public InstanceExtractor<Token> getExtractor(JCas aJCas) {
			return (jcas) ->  new ArrayList<>(JCasUtil.select(jcas, Token.class));
		}

		@Override
		public List<Function<Token, List<Feature>>> getFeatureExtractor(JCas aJCas) {
			return Arrays.asList(token -> Arrays.asList(new Feature("firstLetter", token.getCoveredText().charAt(0))));
		}

		@Override
		public Function<Token, String> getLabelExtractor(JCas aJCas) {
			return token -> token.getPos().getPosValue();
		}

		@Override
		public BiConsumer<String, Token> getLabeller(JCas aJCas) {
			return (lable, token) -> {
				System.out.println(token.getCoveredText());
				token.removeFromIndexes();
			};
		}
		
	}
	
	private File outputs = new File("outputs");
	private File featureFile = new File(outputs, "training-files");

	
	@Test
	public void giveTokenToBeClassifiedWhenExtractionFirstLetterAsFeatureThenArffFileIsGeneratedCorrectly() throws UIMAException, IOException{
		JCas aJCas = JCasFactory.createJCas();
		aJCas.setDocumentLanguage("en");
		aJCas.setDocumentText("It is a test.");
		
		SimplePipeline.runPipeline(aJCas, 
				AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class), 
				AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class), 
				StringClassifierLabeller.getWriterDescription(
						TokenClassificationAlgorithmFactory.class, MaxentStringOutcomeDataWriter.class, 
						featureFile) 
						);
		
		String[] extractedFeatures = FileUtils.readFileToString(new File(featureFile, "training-data.maxent")).split("\n");
		assertThat(extractedFeatures).containsOnly(
				"PRP firstLetter_I",
				"VBZ firstLetter_i",
				"DT firstLetter_a",
				"NN firstLetter_t",
				". firstLetter_.");
	}
	
	@Test
	public void givenATokenWithPosVBZWhenClassifingThenTheTokenIsRemoved() throws Exception{
		JCas aJCas = JCasFactory.createJCas();
		aJCas.setDocumentLanguage("en");
		aJCas.setDocumentText("It is a test.");
		
		SimplePipeline.runPipeline(aJCas, 
				AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class), 
				AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class), 
				StringClassifierLabeller.getWriterDescription(
						TokenClassificationAlgorithmFactory.class, MaxentStringOutcomeDataWriter.class, 
						featureFile) 
						);

		JarClassifierBuilder.trainAndPackage(featureFile, new String[]{"10", "0"});
		
		SimplePipeline.runPipeline(aJCas, 
				StringClassifierLabeller.getClassifierDescription(
						TokenClassificationAlgorithmFactory.class, 
						new File(featureFile, "model.jar").toURI().toURL())
					);
		
		assertThat(JCasUtil.select(aJCas, Token.class)).hasSize(0);
	}
}
