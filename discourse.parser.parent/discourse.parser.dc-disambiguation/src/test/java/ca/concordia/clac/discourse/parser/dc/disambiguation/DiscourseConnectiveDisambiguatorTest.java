package ca.concordia.clac.discourse.parser.dc.disambiguation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseConnective;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseConnectiveDisambiguator;
import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseSenseLabeler;
import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;


public class DiscourseConnectiveDisambiguatorTest {
	private static File MODEL_OUTPUT = new File("outputs/resources");

	public void trainAModel() throws Exception{
		File resourceDir = MODEL_OUTPUT;
		
		URL model = new URL(DiscourseConnectiveDisambiguator.DEFAULT_URL, DiscourseConnectiveDisambiguator.DEFAULT_BERKELEY_MODEL_FILE);
		URL dcList = new URL(DiscourseConnectiveDisambiguator.DEFAULT_URL, DiscourseVsNonDiscourseClassifier.DC_HEAD_LIST_FILE);

		FileUtils.copyFile(
				new File(model.getFile()),
				new File(resourceDir, DiscourseConnectiveDisambiguator.DEFAULT_BERKELEY_MODEL_FILE));
		
		FileUtils.copyFile(
				new File(dcList.getFile()),
				new File(resourceDir, DiscourseVsNonDiscourseClassifier.DC_HEAD_LIST_FILE));

		DiscourseVsNonDiscourseClassifier.main(new String[0]);
		DiscourseSenseLabeler.main(new String[0]);
		DiscourseConnectiveDisambiguator disambiguator = new DiscourseConnectiveDisambiguator(resourceDir);
		disambiguator.train();
	}
	
	@Ignore
	@Test
	public void whenParseSimpleSentenceThenItsDiscourseConnectiveIsLabeled() throws Exception{
		trainAModel();
		DiscourseConnectiveDisambiguator disambiguator = new DiscourseConnectiveDisambiguator();
		test(disambiguator);
	}

	@Test
	public void whenParseWithDefaultModelsThenTheResultsOfParserAreCorrect() throws ResourceInitializationException, UIMAException, IOException, SAXException, URISyntaxException{
		DiscourseConnectiveDisambiguator disambiguator = new DiscourseConnectiveDisambiguator();
		test(disambiguator);
	}
	
	private void test(DiscourseConnectiveDisambiguator disambiguator)
			throws ResourceInitializationException, UIMAException, IOException, FileNotFoundException, SAXException, URISyntaxException {
		String name = getClass().getName();
		name = name.substring(0, name.length() - getClass().getSimpleName().length() - 1);
		
		File dir = new File(getClass().getClassLoader().getResource(name).getFile());
		File output = new File(new File("outputs"), getClass().getSimpleName());
		disambiguator.parse(dir, output);
		
		assertThat(output.listFiles()).hasSize(2);
		
		JCas jCas = JCasFactory.createJCas();
		InputStream is = CompressionUtils.getInputStream(output.getAbsolutePath(), new FileInputStream(output.listFiles()[0]));
		XmiCasDeserializer.deserialize(is, jCas.getCas(), false);
		
		DiscourseConnective connective = JCasUtil.selectByIndex(jCas, DiscourseConnective.class, 0);
		assertThat(connective.getCoveredText()).isEqualTo("But");
		assertThat(connective.getSense()).isEqualTo("Comparison.Contrast");
	}
}
