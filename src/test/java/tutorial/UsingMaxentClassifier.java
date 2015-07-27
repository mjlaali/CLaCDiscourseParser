package tutorial;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.corpus.conll2015.statistics.DiscourseConnectivesList;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse_parsing.module.dcAnnotator.DCClassifierAnnotator;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeClassifier;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;

public class UsingMaxentClassifier {
	private DatasetPath trainDataset = new ConllDataset("train");
	private File outDir = new File("outputs/test/maxent");
	private DatasetStatistics datasetStatistics = new DatasetStatistics(trainDataset, trainDataset.getXmiOutDir());
	
	public UsingMaxentClassifier() throws UIMAException, IOException {
		outDir.mkdirs();
	}
	

	public void train() throws Exception {
//		datasetStatistics.readDataset();
		AnalysisEngineDescription analysisEngineDescription = AnalysisEngineFactory.createEngineDescription(
				DCClassifierAnnotator.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
			    true,
		        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
		        MaxentStringOutcomeDataWriter.class.getName(), 
//		        DefaultDataWriterFactory.PARAM_DATA_WRITER_CONSTRUCTOR_INPUTS,
//		        "arguments 10",
		        DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		        outDir.getAbsolutePath(),
		        DCClassifierAnnotator.PARAM_DC_LIST_FILE,
		        DiscourseConnectivesList.DISCOURSE_CONNECTIVES_LIST_FILE);
		
		datasetStatistics.getStatistics(analysisEngineDescription);
		System.out.println("UsingMaxentClassifier.train(): training");
		JarClassifierBuilder.trainAndPackage(outDir, "100", "5");
		
	}
	
	public void test() throws UIMAException, IOException{
		AnalysisEngineDescription analysisEngineDescription =  AnalysisEngineFactory.createEngineDescription(
				DCClassifierAnnotator.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(outDir), 
				DCClassifierAnnotator.PARAM_DC_LIST_FILE,
		        DiscourseConnectivesList.DISCOURSE_CONNECTIVES_LIST_FILE);
		
		JCas aJCas = JCasFactory.createJCas();
		SyntaxReader syntaxReader = new SyntaxReader();
		String parseTree = "( (S (NP (DT That) (NN debt)) (VP (MD would) (VP (VB be) (VP (VBN paid) (PRT (RP off)) (SBAR (IN as) (S (NP (DT the) (NNS assets)) (VP (VBP are) (VP (VBN sold) (, ,) (S (VP (VBG leaving) (NP (NP (DT the) (JJ total) (NN spending)) (PP (IN for) (NP (DT the) (NN bailout)))) (PP (IN at) (NP (NP (QP ($ $) (CD 50) (CD billion))) (, ,) (CC or) (NP (QP ($ $) (CD 166) (CD billion))) (PP (VBG including) (NP (NP (NN interest)) (PP (IN over) (NP (CD 10) (NNS years)))))))))))))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);


		SimplePipeline.runPipeline(aJCas, analysisEngineDescription);
		
		Collection<DiscourseConnective> dcs = JCasUtil.select(aJCas, DiscourseConnective.class);
		for (DiscourseConnective dc: dcs){
			System.out.printf("Identified discourse connective: %s\n", dc.getCoveredText());
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("UsingMaxentClassifier.main()");
		
		UsingMaxentClassifier usingMaxentClassifier = new UsingMaxentClassifier();
		usingMaxentClassifier.train();
		usingMaxentClassifier.test();
		
		System.out.println("UsingMaxentClassifier.main(): Done!");
	}
}
