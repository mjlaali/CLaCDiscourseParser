package org.cleartk.discourse_parsing;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;

public class CreateGoldOutput {
	public static final String JSON_OUT_FILE = "outputs/parser/system_gold_%s.json";

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		System.out.println("TestModel.main()");
		
//		DiscourseParser discourseParser = new DiscourseParser(new Dataset("train").getModelDir());
		
		DatasetPath dataset = new ConllDataset();
		String jsonFile = String.format(JSON_OUT_FILE, dataset.getMode());
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
		datasetStatistics.readDataset();

		datasetStatistics.getStatistics(ConllJSONExporter.getDescription(jsonFile));
		
		
		LinkedList<String> outputs = Tools.runScorer(dataset.getDiscourseGoldAnnotationFile(), jsonFile);
		for (String line: outputs)
			System.out.println(line);
		
		System.out.println("TestModel.main(): Done.");

	}
}
