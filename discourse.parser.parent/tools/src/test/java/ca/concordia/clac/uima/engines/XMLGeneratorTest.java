package ca.concordia.clac.uima.engines;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.uima.engines.XMLGenerator;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class XMLGeneratorTest {
	JCas jcas;
	AnalysisEngineDescription xmlGenerator;
	File tempFile;
	
	@Before
	public void init() throws IOException, UIMAException{
		tempFile = Files.createTempDirectory(this.getClass().getName()).toFile();
		
		xmlGenerator = XMLGenerator.getDescription(tempFile, ".xml", true);
		jcas = JCasFactory.createJCas();
	}

	@Test
	public void whenGenerateXMLFromADocumentWithoutAnnotationThenStillAValidXMLIsGenerated() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("This is a test.");
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\">This is a test.</%(DOC)>");
		
		testXMLFile(expectedXml);
	}

	private void testXMLFile(String expectedXml)
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {
		SimplePipeline.runPipeline(jcas, xmlGenerator);
		String xmlOutput = FileUtils.readFileToString(new File(tempFile, "1.xml"));
		assertThat(xmlOutput).isXmlEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + expectedXml);
	}
	
	@Test
	public void whenGenerateXMLThenAllAnnotationsAreAvailableOnTheOutput() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("This is a test.");
		new Token(jcas, 0, 4).addToIndexes();
		new Annotation(jcas, 5, 7).addToIndexes();
		
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\"><%(TOKEN) annotation_id=\"0\">This</%(TOKEN)> <%(ANNOTATION) annotation_id=\"5\">is</%(ANNOTATION)> a test.</%(DOC)>");
		testXMLFile(expectedXml);
	}
	
	@Test
	public void whenFilterTypesThenOnXMLThoseTypesDoNotApear() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("This is a test.");
		new Token(jcas, 0, 4).addToIndexes();
		new Annotation(jcas, 5, 7).addToIndexes();
		
		xmlGenerator = XMLGenerator.getDescription(tempFile, ".xml", true, "Token");
		
		String expectedXml = createAnXML("<%(TOKEN) annotation_id=\"0\">This</%(TOKEN)> is a test.");
		testXMLFile(expectedXml);
	}

	
	@Test
	public void whenAnnotationsStartAtTheSamePlaceThenTheOutputIsStillValidXML() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("This is a test.");
		new Token(jcas, 0, 4).addToIndexes();
		new Annotation(jcas, 0, 7).addToIndexes();
		
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\"><%(ANNOTATION) annotation_id=\"0\"><%(TOKEN) annotation_id=\"0\">This</%(TOKEN)> is</%(ANNOTATION)> a test.</%(DOC)>");
		testXMLFile(expectedXml);
	}

	@Test
	public void whenAnnotationsEndAtTheSamePlaceThenTheOutputIsStillValidXML() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("This is a test.");
		new Token(jcas, 5, 7).addToIndexes();
		new Annotation(jcas, 0, 7).addToIndexes();
		
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\"><%(ANNOTATION) annotation_id=\"0\">This <%(TOKEN) annotation_id=\"5\">is</%(TOKEN)></%(ANNOTATION)> a test.</%(DOC)>");
		testXMLFile(expectedXml);
	}
	
	@Test
	public void whenTextContainAmpersignThenTheOutputIsStillValidXML() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("& is a special character.");
		
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\">&amp; is a special character.</%(DOC)>");
		testXMLFile(expectedXml);
	}
	
	@Test
	public void whenThereAreTwoAnnotationAtTheSamePlaceTheyOrderedAsThey() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		jcas.setDocumentText("This is a test.");
		new Token(jcas, 0, 7).addToIndexes();
		new Annotation(jcas, 0, 7).addToIndexes();
		
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\"><%(ANNOTATION) annotation_id=\"0\"><%(TOKEN) annotation_id=\"0\">This is</%(TOKEN)></%(ANNOTATION)> a test.</%(DOC)>");
		testXMLFile(expectedXml);
	}
	
	
	@Test
	public void whenThereIsDuplicateTokensThenTheyMarkedWithSuperscript() throws AnalysisEngineProcessException, ResourceInitializationException, IOException{
		String text = "The word 'the' appears twice (the with sbuscript 3).";
		xmlGenerator = XMLGenerator.getDescription(tempFile, ".xml", Sentence.class, Token.class, true, "DocumentAnnotation");
		jcas.setDocumentText(text);
		
		int start = 0;
		String token = "the";
		while ((start = text.toLowerCase().indexOf(token, start)) != -1){
			new Token(jcas, start, start + token.length()).addToIndexes();
			start += token.length() + 1;
		}
		new Sentence(jcas, 0, text.length()).addToIndexes();;
		
		String expectedXml = createAnXML("<%(DOC) annotation_id=\"0\" language=\"x-unspecified\">The&lt;sub&gt;1&lt;/sub&gt; word 'the&lt;sub&gt;2&lt;/sub&gt;' appears twice (the&lt;sub&gt;3&lt;/sub&gt; with sbuscript 3).</%(DOC)>");
		testXMLFile(expectedXml);
	}
	
	@Test
	public void whenAnnotationsAreMixedThenTheXMLGeneratorDoesNotCrashed() throws ResourceInitializationException, AnalysisEngineProcessException, IOException{
		String text = "en outre que";
		jcas.setDocumentText(text);
		
		new Token(jcas, 0, 8).addToIndexes();
		new Token(jcas, 3, 12).addToIndexes();
		
		xmlGenerator = XMLGenerator.getDescription(tempFile, ".xml", true, "Token");
		
		String expectedXml = createAnXML("<%(TOKEN) annotation_id=\"0\">en outre</%(TOKEN)> que");
		testXMLFile(expectedXml);
	}

	private String createAnXML(String format){
		String[] placeHolders = new String[]{
				"ROOT", XMLGenerator.ROOT_ELEMENTE,
				"DOC", "DocumentAnnotation",
				"TOKEN", "Token",
				"ANNOTATION",  "Annotation"}; 
		Map<String, String> map = new HashMap<>();
		
		for (int i = 0; i < placeHolders.length; i += 2)
			map.put(placeHolders[i], placeHolders[i + 1]);
		StrSubstitutor sub = new StrSubstitutor(map, "%(", ")");
		return sub.replace("<%(ROOT)>" + format + "</%(ROOT)>");
		
	}
	
	@After
	public void cleanup(){
		tempFile.delete();
	}

}
