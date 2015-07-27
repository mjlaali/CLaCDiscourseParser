package org.cleartk.discourse_parsing.module.dcAnnotator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;

public class Baseline {
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		String dictionaryFile = "data/analysisResults/dcHeadList.txt";
		String dcHeadFile = "data/analysisResults/dcMapToItsHead.txt";
		String outputFile = "outputs/baseline/dc-evaluation.txt";
		
		DatasetPath dataset = new ConllDataset("train");
		DatasetStatistics statistics = new DatasetStatistics(dataset);
		
//		statistics.readDataset();
		statistics.run(DictionaryBasedDCAnnotator.getDescription(dictionaryFile), DCEvaluator.getDescription(outputFile, dcHeadFile));
		
		System.out.println(FileUtils.readFileToString(new File(outputFile)));
	}
}
