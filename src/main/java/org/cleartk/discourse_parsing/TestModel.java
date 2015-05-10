package org.cleartk.discourse_parsing;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.discourse_parsing.errorAnalysis.AnalysisParserOutput;
import org.cleartk.discourse_parsing.errorAnalysis.DiscourseRelationsExporter;
import org.cleartk.discourse_parsing.module.ParserComplement;

enum PrintType{
	fullPrint, lastLine, noPrint
}

public class TestModel {
	private DiscourseParser discourseParser; 
	private DatasetStatistics datasetStatistics;
	private DatasetPath dataset;
	
	public TestModel(DatasetPath testDataset, DatasetPath trainDataset) throws UIMAException, IOException {
		discourseParser = new DiscourseParser(trainDataset.getModelDir());
		datasetStatistics = new DatasetStatistics(testDataset, testDataset.getXmiOutDir());
		this.dataset = testDataset;
		datasetStatistics.readDataset();

	}
	
	public void testOnDataset() throws ResourceInitializationException, UIMAException, IOException{
		testParser(discourseParser.getDescription(), dataset.getJsonFile(), dataset.getExplicitRelSystemOutFile(), dataset.getImplicitRelSystemOutFile(), 
				dataset.getReportFile(), PrintType.fullPrint);
	}
	
	private void testParser(AnalysisEngineDescription parser, String jsonFile, String explicitRelFile, String implicitRelFile, String reportFile, PrintType printType) throws ResourceInitializationException, UIMAException, IOException{
		datasetStatistics.run(parser, 
				ConllJSONExporter.getDescription(jsonFile), 
				DiscourseRelationsExporter.getDescription(explicitRelFile, implicitRelFile), 
				AnalysisParserOutput.getDescription(reportFile));
		
		if (printType != PrintType.noPrint){
			LinkedList<String> outputs = Tools.runScorer(dataset.getDiscourseGoldAnnotationFile(), jsonFile);
			if (printType == PrintType.fullPrint){
				for (String line: outputs)
					System.out.println(line);
			} else if (printType == PrintType.lastLine)
				System.out.println(outputs.getLast());
		}
		
	}
	
	
	public void testModules(PrintType printType) throws ResourceInitializationException, UIMAException, IOException{
		File dbgFld = new File("outputs/parser/debug");
		
		AggregateBuilder modules = new AggregateBuilder();
		AnalysisEngineDescription complement = ParserComplement.getDescription(dataset.getDiscourseGoldAnnotationFile(), false);

		int moduleIdx = 0;
		for (AnalysisEngineDescription description: discourseParser.getModules()){
			AggregateBuilder parser = new AggregateBuilder();
			parser.add(modules.createAggregateDescription());
			parser.add(complement);
			
			File baseDir = new File(dbgFld, "" + moduleIdx);
			baseDir.mkdirs();
			testParser(parser.createAggregateDescription(), 
					new File(baseDir, new File(dataset.getJsonFile()).getName()).getAbsolutePath(), 
					new File(baseDir, new File(dataset.getExplicitRelSystemOutFile()).getName()).getAbsolutePath(),
					new File(baseDir, new File(dataset.getImplicitRelSystemOutFile()).getName()).getAbsolutePath(),
					new File(baseDir, new File(dataset.getReportFile()).getName()).getAbsolutePath(), printType);
			
			modules.add(description);
			moduleIdx++;
		}
		
		File baseDir = new File(dbgFld, "" + moduleIdx);
		baseDir.mkdirs();
		testParser(modules.createAggregateDescription(), 
				new File(baseDir, new File(dataset.getJsonFile()).getName()).getAbsolutePath(), 
				new File(baseDir, new File(dataset.getExplicitRelSystemOutFile()).getName()).getAbsolutePath(),
				new File(baseDir, new File(dataset.getImplicitRelSystemOutFile()).getName()).getAbsolutePath(),
				new File(baseDir, new File(dataset.getReportFile()).getName()).getAbsolutePath(), printType);
		
	}
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		System.out.println("TestModel.main()");
		
		DatasetPath testDataset = null;
		DatasetPath trainDatasetPath = null;
		if (args.length > 0){
			
		} else {
			testDataset = new ConllDataset();
			trainDatasetPath = new ConllDataset("train");
		}
		
		TestModel testModel = new TestModel(testDataset, trainDatasetPath);
		testModel.testOnDataset();
//		testModel.testModules(PrintType.lastLine);
		
		System.out.println("TestModel.main(): Done.");
	}

}
