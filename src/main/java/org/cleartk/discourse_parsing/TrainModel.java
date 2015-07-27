package org.cleartk.discourse_parsing;

import java.io.File;

import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;

public class TrainModel {

	public static void main(String[] args) throws Exception {
		System.out.println("TrainModel.main()");
		DatasetPath dataset = new ConllDataset("train");
		
		File outDir = new File(dataset.getModelDir());
//		FileUtils.deleteDirectory(outDir);
		DiscourseParser discourseParser = new DiscourseParser(outDir.getAbsolutePath());
		
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, String.format(DatasetStatistics.XMI_DIR, dataset.getMode()));
//		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(discourseParser.getWriterDescription());
		
//		String options = "weka.classifiers.trees.J48 -C 0.25 -M 2";
		String[] options = new String[]{"100", "5"};
		if (args.length > 0)
			options = args;
//			options = args[0];
		discourseParser.trainAndPackage(options);
		
		System.out.println("TrainModel.main(): Done.");

	}

}
