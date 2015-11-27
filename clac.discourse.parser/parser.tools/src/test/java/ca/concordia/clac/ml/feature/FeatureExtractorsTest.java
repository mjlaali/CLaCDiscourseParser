package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeatures;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getText;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class FeatureExtractorsTest {

	@Test
	public void givenAFunctionThatExtractAnAttributeWhenAttributeIsCoveredTextThenMakesAFeatureExtractorForTheCoveredText() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		
		aJCas.setDocumentText("it is a test.");
		String coverText = "it";
		Token token = new Token(aJCas, 0, coverText.length());
		
		String featureName = "coverText";
		List<Feature> features = getFeatures(getFeature(featureName, getText(Token.class))).apply(token);
		
		assertThat(features).hasSize(1);
		Feature feature = features.get(0);
		assertThat(feature.getName()).isEqualTo(featureName);
		assertThat(feature.getValue()).isEqualTo(coverText);
	}
	
	
}
