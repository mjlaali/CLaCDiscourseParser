package org.cleartk.discourse_parsing.argumentLabeler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.corpus.conll2015.statistics.DiscourseConnectivesList;
import org.cleartk.discourse_parsing.DiscourseParser;
import org.cleartk.discourse_parsing.ParseComponentBaseTest;
import org.cleartk.discourse_parsing.module.argumentLabeler.Arg1Labeler;
import org.cleartk.discourse_parsing.module.argumentLabeler.Arg2Labeler;
import org.cleartk.discourse_parsing.module.argumentLabeler.NodeArgType;
import org.cleartk.discourse_parsing.module.argumentLabeler.Position;
import org.cleartk.discourse_parsing.module.dcAnnotator.DCClassifierAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.junit.Test;

public class Arg1LabelerTest extends ParseComponentBaseTest<String>{
	
	@Test
	public void givenArg1InHigherLevelOfArg2WhenExtractingInstancesThenAllSiblingExceptOnesInthePathAreAdded() throws CASException, ResourceInitializationException, CleartkProcessingException, AnalysisEngineProcessException{
		String parseTree = "( (S (`` ``) (S (S (NP (PRP We)) (VP (MD would) (VP (VB stop) (NP (NN index) (NN arbitrage)) (SBAR (WHADVP (WRB when)) (S (NP (DT the) (NN market)) (VP (VBZ is) (PP (IN under) (NP (NN stress))))))))) (, ,) (CC and) (S (NP (PRP we)) (VP (VBP have) (ADVP (RB recently))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said) (, ,) (S (VP (VBG citing) (NP (NP (NNP Oct.) (CD 13)) (CC and) (NP (RBR earlier) (DT this) (NN week)))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);
		String arg1 = "We would stop index arbitrage";
		String arg2 = "the market is under stress";
		String dc = "when";
		
		discourseRelation = discourseRelationFactory.makeSimpleRelation(aJCas, arg1, arg2, dc);
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(Arg1Labeler.class);

		run(true, writerDescription, null);
		
		verify(dataWrite, times(4)).write(instanceCaptor.capture());
	}
	
	@Test
	public void givenArg1AndArg2InTheSameLevelWhenAddingInstancesThenAllNodesInThatLevelAreAlsoAdded() throws CASException, ResourceInitializationException, CleartkProcessingException, AnalysisEngineProcessException{
		String parseTree = "(S (CC But)(NP (PRP its) (NNS competitors))(VP(VP (VBP have)(NP (RB much) (JJR broader) (NN business) (NNS interests)))(CC and)(RB so)(VP (VBP are)(ADVP (RBR better))(VP (VBN cushioned)(PP (IN against)(NP (NN price) (NNS swings)))))))";
		syntaxReader.initJCas(aJCas, parseTree);

		String arg1 = "But its competitors have much broader business interests";
		String arg2 = "and so are better cushioned against price swings";
		String dc = "so";
		
		discourseRelation = discourseRelationFactory.makeSimpleRelation(aJCas, arg1, arg2, dc);
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(Arg1Labeler.class);

		run(true, writerDescription, null);
		
		verify(dataWrite, times(3)).write(instanceCaptor.capture());
	}
	
	@Test
	public void givenWhenExampleWhenExtractingFeatureThenThereAreFiveFeatureWithCorrectValue() throws CASException, ResourceInitializationException, CleartkProcessingException, AnalysisEngineProcessException{
		String parseTree = "( (S (`` ``) (S (S (NP (PRP We)) (VP (MD would) (VP (VB stop) (NP (NN index) (NN arbitrage)) (SBAR (WHADVP (WRB when)) (S (NP (DT the) (NN market)) (VP (VBZ is) (PP (IN under) (NP (NN stress))))))))) (, ,) (CC and) (S (NP (PRP we)) (VP (VBP have) (ADVP (RB recently))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said) (, ,) (S (VP (VBG citing) (NP (NP (NNP Oct.) (CD 13)) (CC and) (NP (RBR earlier) (DT this) (NN week)))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);
		String arg1 = "We would stop index arbitrage";
		String arg2 = "the market is under stress";
		String dc = "when";
		
		discourseRelation = discourseRelationFactory.makeSimpleRelation(aJCas, arg1, arg2, dc);
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(Arg1Labeler.class);

		run(true, writerDescription, null);
		
		verify(dataWrite, times(4)).write(instanceCaptor.capture());
		List<Instance<String>> systemOutputs = instanceCaptor.getAllValues();
		List<Instance<String>> goldValues = new ArrayList<Instance<String>>();
		
		goldValues.add(new Instance<String>(NodeArgType.Arg1.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "when"), 
						new Feature("CON-NT-Path", "VB::VP;;SBAR"), new Feature("NT-Ctx", "VB_VP_empty_NP"), 
						new Feature("CON-NT-Position", Position.Left.toString())})));

		for (int i = 0; i < 1; i++){
			Instance<String> toTest = systemOutputs.get(i);
			Instance<String> goldValue = goldValues.get(i);
			assertThat(toTest.getOutcome()).isEqualTo(goldValue.getOutcome());
			assertThat(toTest.getFeatures()).containsOnlyElementsOf(goldValue.getFeatures());
		}
	}
	
	@Test
	public void whenRunningOnTrialDatasetThenThePrecisionIsAbove10() throws UIMAException, IOException{
		String jsonFile = "outputs/test/arg2.json";
		AggregateBuilder builder = new AggregateBuilder();
		DatasetPath trainDataset = new ConllDataset("train");
		builder.add(DCClassifierAnnotator.getClassifierDescription(
				DiscourseParser.getDcAnnotatorTrainDir(trainDataset.getModelDir()), DiscourseConnectivesList.DISCOURSE_CONNECTIVES_LIST_FILE));
		builder.add(Arg2Labeler.getClassifierDescription(
				DiscourseParser.getArg2LabelerTrainDir(trainDataset.getModelDir()), Arg2Labeler.DEFAULT_PATTERN_FILE));
		builder.add(Arg1Labeler.getClassifierDescription(
				DiscourseParser.getArg1LabelerTrainDir(trainDataset.getModelDir())));
		builder.add(ConllJSONExporter.getDescription(jsonFile));

		DatasetPath dataset = new ConllDataset();
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
		datasetStatistics.readDataset();
		datasetStatistics.run(builder.createAggregateDescription());

		LinkedList<String> outputs = Tools.runScorer(dataset.getDiscourseGoldAnnotationFile(), jsonFile);
		boolean targetLine = false;
		for (String line: outputs){
			if (line.equals("Arg 1 extractor--------------")){
				targetLine = true;
			} else if (targetLine){
				double precision = Double.parseDouble(line.split(" ")[1]);
				System.out
						.println("Arg2LabelerTest.whenLabelingArg2ThenThePrecisionIsAbove50(): Precision = " + precision);
				assertThat(precision).isGreaterThan(0.09);
				targetLine = false;
			}
		}

	}

	@Test
	public void whenRunningTheClassifierThenThePrecisoinIsAbove80() throws UIMAException, IOException{
		DatasetPath dataset = new ConllDataset();
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(Arg1Labeler.getClassifierDescription(
				DiscourseParser.getArg1LabelerTrainDir(new ConllDataset("train").getModelDir())));
		
		double precision = (double) Arg1Labeler.getCorrect() / Arg1Labeler.getTotal();
		assertThat(precision).isGreaterThan(0.80);
		System.out
				.println("Arg2LabelerTest.whenRunningInTestModeThenGetTheSamePrecisionAsInWeka(): Precision = " + precision);
	}
}
