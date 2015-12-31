package ca.concordia.clac.discourse.parser.dc.disambiguation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.DiscourseConnective;
import org.junit.Test;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;

public class ConnectiveLabelExtractorTest {

	@Test
	public void whenDiscourseConnectiveWasNotAddedToJCasThenExtractorReturnFalse() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		
		aJCas.setDocumentText("but, this is another issue.");
		DiscourseConnective connective = new DiscourseConnective(aJCas, 0, "but".length());
		
		Function<DiscourseConnective, String> labelExtractor = new DiscourseVsNonDiscourseClassifier().getLabelExtractor();
		String label = labelExtractor.apply(connective);
		
		assertThat(label).isEqualTo("false");
	}
	
	@Test
	public void whenDiscourseConnectiveWasAddedToJCasThenExtractorReturnTrue() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		
		aJCas.setDocumentText("but, this is another issue.");
		DiscourseConnective gold = new DiscourseConnective(aJCas, 0, "but".length());
		gold.addToIndexes();
		
		DiscourseConnective test = new DiscourseConnective(aJCas, 0, "but".length());
		
		Function<DiscourseConnective, String> labelExtractor = new DiscourseVsNonDiscourseClassifier().getLabelExtractor();
		String label = labelExtractor.apply(test);
		
		assertThat(label).isEqualTo("true");
	}
}
