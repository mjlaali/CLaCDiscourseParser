package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getText;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class FeatureExtractorsTest {

	@Test
	public void givenAFunctionThatExtractAnAttributeWhenAttributeIsCoveredTextThenMakesAFeatureForTheCoveredText() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		
		aJCas.setDocumentText("it is a test.");
		String coverText = "it";
		Token token = new Token(aJCas, 0, coverText.length());
		
		String featureName = "coverText";
		Feature feature = getText(Token.class).andThen(makeFeature(featureName)).apply(token);
		
		assertThat(feature.getName()).isEqualTo(featureName);
		assertThat(feature.getValue()).isEqualTo(coverText);
	}
	
	
}
