package ca.concordia.clac.batch_process;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class BatchProcessTest {
	public static final String TEST_FILE_LOCATION = "/test/BatchProcessTest";
	public static final String TEST_INPUT_FILE_LOCATION = "resources" + TEST_FILE_LOCATION;
	public static final String TEST_OUTPUT_FILE_LOCATION = "outputs" + TEST_FILE_LOCATION;
	
	public static class FaultyAnalysisEngine extends JCasAnnotator_ImplBase{
		public static final String FAILED_IDX_PARAM = "failedIdx";
		
		@ConfigurationParameter(name=FAILED_IDX_PARAM)
		public Integer failedIdx;

		private int idx = 0;
		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {
			if (idx == failedIdx)
				throw new AnalysisEngineProcessException(new RuntimeException("This is a faulty engine!"));
			++idx;
		}
		
	}
	
	private File inputDir;
	private File outputDir; 
	
	public BatchProcessTest() throws IOException {
		inputDir = new File(TEST_INPUT_FILE_LOCATION);
		outputDir = new File(TEST_OUTPUT_FILE_LOCATION);
		
		if (outputDir.exists())
			FileUtils.deleteDirectory(outputDir);
		
		for (File file: new File[]{inputDir, outputDir})
			if (!file.exists())
				inputDir.mkdirs();
		
		
	}
	
	@Test
	public void twoDifferentBatchAreNotEqual() throws ResourceInitializationException{
		BatchProcess b1 = new BatchProcess(inputDir, outputDir);
		b1.addProcess("test", AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class));
		
		BatchProcess b2 = new BatchProcess(inputDir, outputDir);
		b2.addProcess("test", AnalysisEngineFactory.createEngineDescription(FaultyAnalysisEngine.class));
		
		assertThat(b1).isNotEqualTo(b2);
	}
	
	@Test
	public void twoEqaulBatchAreEqaul() throws ResourceInitializationException{
		BatchProcess b1 = new BatchProcess(inputDir, outputDir);
		b1.addProcess("test", AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class));
		
		BatchProcess b2 = new BatchProcess(inputDir, outputDir);
		b2.addProcess("test", AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class));
		assertThat(b1).isEqualTo(b2);
	}
	
	@Test
	public void whenSavedThenRestoredObjectAreTheSame() throws ResourceInitializationException, FileNotFoundException, IOException, ClassNotFoundException{
		BatchProcess saved = new BatchProcess(inputDir, outputDir);
		saved.addProcess("test", AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class));
		saved.save();
		
		BatchProcess loaded = BatchProcess.load(outputDir);
		assertThat(loaded).isEqualTo(saved);
	}
	
	@Test
	public void whenAddingWithTheSameNameThenEnginesAreConcatinating() throws FileNotFoundException, IOException, ClassNotFoundException, UIMAException{
		BatchProcess toRun = new BatchProcess(inputDir, outputDir);
		toRun.addProcess("test", AnalysisEngineFactory.createEngineDescription(OpenNlpSegmenter.class));
		toRun.addProcess("test", AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class));
		
		toRun.run();
		JCas jCas = getJCas(new File(outputDir, "test/a.txt.xmi"));
		Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
		assertThat(tokens).isNotEmpty();
		assertThat(tokens.iterator().next().getPos()).isNotNull();
	}
	
	private JCas getJCas(File aFile) throws UIMAException, IOException {
		JCas aJCas = JCasFactory.createJCas();
		
		CasIOUtil.readJCas(aJCas, aFile);
		return aJCas;
	}
	@Test
	public void whenAbruptThenNextTimeContinued() throws UIMAException, IOException, ClassNotFoundException{
		BatchProcess faulty = new BatchProcess(inputDir, outputDir);
		faulty.addProcess("faultyEngine", AnalysisEngineFactory.createEngineDescription(FaultyAnalysisEngine.class, 
				FaultyAnalysisEngine.FAILED_IDX_PARAM, 1));
		faulty.clean();
		faulty.save();
		
		try {
			faulty.run();
			throw new RuntimeException("This code should not be reached");
		} catch (AnalysisEngineProcessException e) {
			//recover
			
			assertThat(new File(outputDir, "faultyEngine"));
			faulty = BatchProcess.load(outputDir);
			faulty.run();
		}
	}
	
}
