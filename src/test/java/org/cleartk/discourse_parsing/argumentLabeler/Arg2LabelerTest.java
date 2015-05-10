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
import org.cleartk.discourse_parsing.DiscourseParser;
import org.cleartk.discourse_parsing.ParseComponentBaseTest;
import org.cleartk.discourse_parsing.module.DiscourseConnectiveAnnotator;
import org.cleartk.discourse_parsing.module.argumentLabeler.Arg2Labeler;
import org.cleartk.discourse_parsing.module.argumentLabeler.NodeArgType;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.junit.Test;

public class Arg2LabelerTest extends ParseComponentBaseTest<String>{

	@Test
	public void whenRunningInTestModeThenGetTheSamePrecisionAsInWeka() throws ResourceInitializationException, UIMAException, IOException{
		DatasetPath dataset = new ConllDataset();
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(Arg2Labeler.getClassifierDescription(
				DiscourseParser.getArg2LabelerTrainDir(new ConllDataset("train").getModelDir()), Arg2Labeler.DEFAULT_PATTERN_FILE));
		
		double precision = (double) Arg2Labeler.getCorrect() / Arg2Labeler.getTotal();
		assertThat(precision).isGreaterThan(0.90);
		System.out
				.println("Arg2LabelerTest.whenRunningInTestModeThenGetTheSamePrecisionAsInWeka(): Precision = " + precision);
	}
	
	
	@Test
	public void whenLabelingArg2ThenThePrecisionIsAbove50() throws UIMAException, IOException{
		String jsonFile = "outputs/test/arg2.json";
		AggregateBuilder builder = new AggregateBuilder();
		DatasetPath trainDataset = new ConllDataset("train");
		builder.add(DiscourseConnectiveAnnotator.getClassifierDescription(
				DiscourseParser.getDcAnnotatorTrainDir(trainDataset.getModelDir())));
		builder.add(Arg2Labeler.getClassifierDescription(
				DiscourseParser.getArg2LabelerTrainDir(trainDataset.getModelDir()), Arg2Labeler.DEFAULT_PATTERN_FILE));
		builder.add(ConllJSONExporter.getDescription(jsonFile));

		DatasetPath dataset = new ConllDataset();
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
		datasetStatistics.readDataset();
		datasetStatistics.run(builder.createAggregateDescription());

		LinkedList<String> outputs = Tools.runScorer(dataset.getDiscourseGoldAnnotationFile(), jsonFile);
		boolean targetLine = false;
		for (String line: outputs){
			if (line.equals("Arg 2 extractor--------------")){
				targetLine = true;
			} else if (targetLine){
				double precision = Double.parseDouble(line.split(" ")[1]);
				System.out
						.println("Arg2LabelerTest.whenLabelingArg2ThenThePrecisionIsAbove50(): Precision = " + precision);
				assertThat(precision).isGreaterThan(0.5);
				targetLine = false;
			}
		}

	}
	
	@Test
	public void givenADCWithTwoSParentWhenCreatingInstancesThenTwoInstancesAreCreated() throws CASException, CleartkProcessingException, AnalysisEngineProcessException, ResourceInitializationException{
		String parseTree = "( (S (`` ``) (S (S (NP (PRP We)) (VP (MD would) (VP (VB stop) (NP (NN index) (NN arbitrage)) (SBAR (WHADVP (WRB when)) (S (NP (DT the) (NN market)) (VP (VBZ is) (PP (IN under) (NP (NN stress))))))))) (, ,) (CC and) (S (NP (PRP we)) (VP (VBP have) (ADVP (RB recently))))) (, ,) ('' '') (NP (PRP he)) (VP (VBD said) (, ,) (S (VP (VBG citing) (NP (NP (NNP Oct.) (CD 13)) (CC and) (NP (RBR earlier) (DT this) (NN week)))))) (. .)) )";
		syntaxReader.initJCas(aJCas, parseTree);
		String arg1 = "We would stop index arbitrage";
		String arg2 = "the market is under stress";
		String dc = "when";

		discourseRelation = discourseRelationFactory.makeSimpleRelation(aJCas, arg1, arg2, dc);
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(Arg2Labeler.class);

		run(true, writerDescription, null);
		verify(dataWrite, times(4)).write(instanceCaptor.capture());
		List<Instance<String>> allValues = instanceCaptor.getAllValues();
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		instances.add(new Instance<String>(NodeArgType.Arg2.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "when"), 
						new Feature("CON-NT-Path", "SBAR;;WHADVP;;WRB"), new Feature("NT-Ctx", "SBAR_VP_NP_empty"), 
						new Feature("ChildPat", "WHADVP-S")})));

		instances.add(new Instance<String>(NodeArgType.Non.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "when"), 
						new Feature("CON-NT-Path", "S;;VP;;VP;;SBAR;;WHADVP;;WRB"), new Feature("NT-Ctx", "S_S_empty_,"),
						new Feature("ChildPat", "NP-VP")})));
		
		instances.add(new Instance<String>(NodeArgType.Non.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "when"), 
						new Feature("CON-NT-Path", "S;;S;;VP;;VP;;SBAR;;WHADVP;;WRB"), new Feature("NT-Ctx", "S_S_``_,"),
						new Feature("ChildPat", "S-,-CC-S")})));
		
		instances.add(new Instance<String>(NodeArgType.Non.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "when"), 
						new Feature("CON-NT-Path", "S;;S;;S;;VP;;VP;;SBAR;;WHADVP;;WRB"), new Feature("NT-Ctx", "S_TOP_empty_empty"), 
						new Feature("ChildPat", "``-S-,-''-NP-VP-.")})));

		
		for (int i = 0; i < allValues.size(); i++){
			assertThat(allValues.get(i).getOutcome()).isEqualTo(instances.get(i).getOutcome());
			assertThat(allValues.get(i).getFeatures()).containsOnlyElementsOf(instances.get(i).getFeatures());
		}
		

	}
	
	
}
