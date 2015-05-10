package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.discourse_parsing.module.DiscourseConnectiveAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.junit.Test;

public class DiscourseConnectiveAnnotatorTest extends ParseComponentBaseTest<String>{

	@Test
	public void whenRunningInTestModeThenGetTheSamePrecisionAsInWeka() throws ResourceInitializationException, UIMAException, IOException{
		DatasetPath dataset = new ConllDataset();
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(DiscourseConnectiveAnnotator.getClassifierDescription(
				DiscourseParser.getDcAnnotatorTrainDir(new ConllDataset("train").getModelDir())));
		
		double precision = (double) DiscourseConnectiveAnnotator.getCorrect() / DiscourseConnectiveAnnotator.getTotal();
		assertThat(precision).isGreaterThan(0.90);
		System.out
				.println("Arg2LabelerTest.whenRunningInTestModeThenGetTheSamePrecisionAsInWeka(): Precision = " + precision);
	}

	@Test
	public void givenTrainingModeWhenExtractingInstacesThenAllDcsAreExtracted() throws ResourceInitializationException, CleartkProcessingException, AnalysisEngineProcessException{
		String parseTree = "( (S (`` ``) (S (S (NP (PRP We)) (VP (MD would) (VP (VB stop) (NP (NN index) (NN arbitrage)) (SBAR (WHADVP (WRB when)) (S (NP (DT the) (NN market)) (VP (VBZ is) (PP (IN under) (NP (NN stress))))))))) (, ,) (CC and) (S (NP (PRP we)) (VP (VBP have) (ADVP (RB recently))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said) (, ,) (S (VP (VBG citing) (NP (NP (NNP Oct.) (CD 13)) (CC and) (NP (RBR earlier) (DT this) (NN week)))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);
		
		String arg1 = "We would stop index arbitrage";
		String arg2 = "the market is under stress";
		String dc = "when";
		
		discourseRelation = discourseRelationFactory.makeSimpleRelation(aJCas, arg1, arg2, dc);
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(DiscourseConnectiveAnnotator.class);
		run(true, writerDescription, null);

		verify(dataWrite, times(4)).write(instanceCaptor.capture());
		List<Instance<String>> allValues = instanceCaptor.getAllValues();
		for (Instance<String> instance: allValues){
			String txt = null;
			for (Feature feature: instance.getFeatures())
				if (feature.getName().equals("txt"))
					txt = feature.getValue().toString();
			
			if (txt.equals("when"))
				assertThat(instance.getOutcome()).isEqualTo("true");
			else
				assertThat(instance.getOutcome()).isEqualTo("false");
		}
	}

	@Test
	public void givenTrainingModeWhenExtractingInstacesThenOnlyBiggerDcExtracted() throws ResourceInitializationException, CleartkProcessingException, AnalysisEngineProcessException{
		String parseTree = "(ROOT (S (PP (IN As) (NP (NP (DT a) (NN result)) (PP (IN of) (NP (NP (DT the) (NNS pilots) (POS ')) (NN strike))))) (, ,) (NP (DT all) (NNS flights)) (VP (VBP have) (VP (VBN had) (S (VP (TO to) (VP (VB be) (VP (VBN cancelled))))))) (. .)))";
		syntaxReader.initJCas(aJCas, parseTree);

		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(DiscourseConnectiveAnnotator.class);
		run(true, writerDescription, null);

		verify(dataWrite, times(1)).write(instanceCaptor.capture());
		List<Instance<String>> instances = instanceCaptor.getAllValues();
		
		assertThat(instances.get(0).getFeatures()).contains(new Feature("txt", "as a result"));
	}
	
	@Test
	public void giveTrainingModeWhenExtractingInstanceThenFeaturesAreCorrect() throws ResourceInitializationException, AnalysisEngineProcessException{
		String parseTree = "(ROOT (S (PP (IN As) (NP (NP (DT a) (NN result)) (PP (IN of) (NP (NP (DT the) (NNS pilots) (POS ')) (NN strike))))) (, ,) (NP (DT all) (NNS flights)) (VP (VBP have) (VP (VBN had) (S (VP (TO to) (VP (VB be) (VP (VBN cancelled))))))) (. .)))";
		syntaxReader.initJCas(aJCas, parseTree);

		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(DiscourseConnectiveAnnotator.class);
		run(true, writerDescription, null);

		verify(dataWrite, times(1)).write(instanceCaptor.capture());
		List<Instance<String>> instances = instanceCaptor.getAllValues();
		
		List<Feature> features = instances.get(0).getFeatures();
		assertThat(features).contains(new Feature("txt", "as a result"));
		assertThat(features).contains(new Feature("selfCat", "PP"));
		assertThat(features).contains(new Feature("selfCatParent", "S"));
		assertThat(features).contains(new Feature("selfCatLeftSibling", "empty"));
		assertThat(features).contains(new Feature("selfCatRightSibling", ","));

	}
	
	@Test
	public void givenASentWithAsConnectiveWhenExtractingDCThenAsIdentifies() throws ResourceInitializationException, CleartkProcessingException, AnalysisEngineProcessException{
		String parseTree = "( (S (NP (DT That) (NN debt)) (VP (MD would) (VP (VB be) (VP (VBN paid) (PRT (RP off)) (SBAR (IN as) (S (NP (DT the) (NNS assets)) (VP (VBP are) (VP (VBN sold) (, ,) (S (VP (VBG leaving) (NP (NP (DT the) (JJ total) (NN spending)) (PP (IN for) (NP (DT the) (NN bailout)))) (PP (IN at) (NP (NP (QP ($ $) (CD 50) (CD billion))) (, ,) (CC or) (NP (QP ($ $) (CD 166) (CD billion))) (PP (VBG including) (NP (NP (NN interest)) (PP (IN over) (NP (CD 10) (NNS years)))))))))))))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);
		
		String arg1 = "That debt would be paid off";
		String arg2 = "the assets are sold";
		String dc = " as ";
		
		discourseRelation = discourseRelationFactory.makeSimpleRelation(aJCas, arg1, arg2, dc);
		discourseRelation.getDiscourseConnective().addToIndexes();

		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(DiscourseConnectiveAnnotator.class);
		run(true, writerDescription, null);

		verify(dataWrite, atLeast(1)).write(instanceCaptor.capture());
		List<Instance<String>> allValues = instanceCaptor.getAllValues();
		
		boolean soMorked = false;
		for (Instance<String> instance: allValues){
			String txt = null;
			for (Feature feature: instance.getFeatures())
				if (feature.getName().equals("txt"))
					txt = feature.getValue().toString();
			
			if (txt.equals("as")){
				assertThat(instance.getOutcome()).isEqualTo("true");
				soMorked = true;
			}
		}
		
		assertThat(soMorked).isTrue();
	}
	
	public void test() throws ResourceInitializationException{
		String parseTree = "( (S (NP (DT That) (NN debt)) (VP (MD would) (VP (VB be) (VP (VBN paid) (PRT (RP off)) (SBAR (IN as) (S (NP (DT the) (NNS assets)) (VP (VBP are) (VP (VBN sold) (, ,) (S (VP (VBG leaving) (NP (NP (DT the) (JJ total) (NN spending)) (PP (IN for) (NP (DT the) (NN bailout)))) (PP (IN at) (NP (NP (QP ($ $) (CD 50) (CD billion))) (, ,) (CC or) (NP (QP ($ $) (CD 166) (CD billion))) (PP (VBG including) (NP (NP (NN interest)) (PP (IN over) (NP (CD 10) (NNS years)))))))))))))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);

		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(DiscourseConnectiveAnnotator.getClassifierDescription(
				DiscourseParser.getDcAnnotatorTrainDir(new ConllDataset("train").getModelDir())));
		
		

	}
	
}
