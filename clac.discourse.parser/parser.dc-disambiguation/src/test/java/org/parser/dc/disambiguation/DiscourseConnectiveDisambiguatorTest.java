package org.parser.dc.disambiguation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseConnective;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

public class DiscourseConnectiveDisambiguatorTest {
	
	@BeforeClass
	public static void train() throws Exception{
		DiscourseConnectiveDisambiguator disambiguator = new DiscourseConnectiveDisambiguator(new File("resources"));
		disambiguator.train();
	}

	@Test
	public void whenParseSimpleSentenceThenItsDiscourseConnectiveIsLabeled() throws ResourceInitializationException, UIMAException, IOException, SAXException{
		DiscourseConnectiveDisambiguator disambiguator = new DiscourseConnectiveDisambiguator(new File("resources"));
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
		System.out.println(connective.getSense());
		assertThat(connective.getSense()).isEqualTo("Comparison.Contrast");
	}
}
