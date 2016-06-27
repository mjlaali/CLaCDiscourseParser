package ca.concordia.clac.uima.engines;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.xml.sax.SAXException;

import ca.concordia.clac.batch_process.BatchProcess;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class ViewAnnotationCopierTest {
	File inputDir = new File("resources/test/ViewAnnotationCopierTest/input");
	File inputFile = new File(inputDir, "a.txt");
	File outputDir = new File("outputs/test/ViewAnnotationCopierTest/output");

	@Test
	public void giveOneAnnotationInViewAThenWhenCopiedToViewBThenItExists() throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();

		String sourceView = "sourceView";
		String targetView = "targetView";

		JCas viewA = aJCas.createView(sourceView);
		JCas viewB = aJCas.createView(targetView);

		viewA.setDocumentText("it is a test");
		viewB.setDocumentText(viewA.getDocumentText());

		new Token(viewA, 0, 2).addToIndexes();

		SimplePipeline.runPipeline(aJCas, AnalysisEngineFactory.createEngine(ViewAnnotationCopier.class, 
				ViewAnnotationCopier.PARAM_SOURCE_VIEW_NAME, sourceView, 
				ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, targetView));
		assertThat(JCasUtil.select(viewB, Token.class)).hasSize(1);
	}

	@Test
	public void givenDocumentMetadataInSourceViewWhenCopyingThenItIsCopied() throws UIMAException, IOException{
		CollectionReader reader = CollectionReaderFactory.createReader(TextReader.class, 
				TextReader.PARAM_LANGUAGE, "en", 
				TextReader.PARAM_SOURCE_LOCATION, inputFile);

		String targetView = "targetView";

		JCas aJCas = JCasFactory.createJCas();
		if (reader.hasNext())
			reader.getNext(aJCas.getCas());

		SimplePipeline.runPipeline(aJCas, AnalysisEngineFactory.createEngineDescription(ViewAnnotationCopier.class, 
				ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, targetView));

		assertThat(JCasUtil.select(aJCas.getView(targetView), DocumentMetaData.class)).hasSize(1);

	}

	@Test
	public void givenASourceViewWhenCopyingToATargetViewThenTheResultCanBeSavedToFile() throws UIMAException, IOException{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_LANGUAGE, "en", 
				TextReader.PARAM_SOURCE_LOCATION, inputFile);

		String targetView = "targetView";

		SimplePipeline.runPipeline(reader, 
				AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, targetView), 
				AnalysisEngineFactory.createEngineDescription(XmiWriter.class, 
						XmiWriter.PARAM_TARGET_LOCATION, outputDir));

	}

	@Test
	public void givenACasAfterCopingAnnotationsWhenSavingThenNullPointerExceptionDoesNotThrow() throws UIMAException, IOException, SAXException, CpeDescriptorException{
		String targetView = "GOLD_VIEW";

		BatchProcess batchProcess;

		batchProcess = new BatchProcess(inputDir, outputDir, "fr", "*");
		batchProcess.addProcess("goldView", 
				AnalysisEngineFactory.createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, targetView));

		batchProcess.addProcess("init", 
				AnalysisEngineFactory.createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, targetView)
				);

		batchProcess.clean();
		batchProcess.run();


	}
}
