package org.cleartk.corpus.conll2015;

import java.io.File;

public class ConllDatasetPathFactory {
	
	private File baseFld;
	
	public ConllDatasetPath makeADataset(File dataFld, ConllDatasetPath.DatasetMode mode) {
		switch (mode) {
		case train:
			baseFld = new File(dataFld, "conll15st-train-dev/conll15st_data/conll15-st-03-04-15-train");
			break;

		case dev:
			baseFld = new File(dataFld, "conll15st-train-dev/conll15st_data/conll15-st-03-04-15-dev");
			break;
		
		default:
			baseFld = new File(dataFld, "conll15st-trial-data");
			break;
		}

		if (baseFld == null || !baseFld.exists())
			throw new RuntimeException("The base fld is not valid folder for the CoNLL dataset: " + baseFld);
		
		return new ConllDatasetPath(findFile("raw"), findFile("data"), findFile("parses"), mode);
	}
	
	public File findFile(String... tags){
		for (File file: getBaseFld().listFiles()){
			String name = file.getName();
			if (name.contains(".bak"))
				continue;
			boolean found = true;
			for (String tag: tags){
				if (!name.contains(tag)){
					found = false;
					break;
				}
			}
			if (found)
				return file;
		}
		return null;
	}
	
	
	public File getBaseFld() {
		return baseFld;
	}
	
	public static void main(String[] args) {
		ConllDatasetPathFactory factory = new ConllDatasetPathFactory();
		File dataFld = new File("data");
		for (ConllDatasetPath.DatasetMode mode: ConllDatasetPath.DatasetMode.values())
			factory.makeADataset(dataFld, mode);
	}
}
