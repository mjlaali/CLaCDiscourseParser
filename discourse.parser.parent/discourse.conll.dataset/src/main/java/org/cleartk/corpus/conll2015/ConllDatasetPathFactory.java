package org.cleartk.corpus.conll2015;

import java.io.File;

import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;

public class ConllDatasetPathFactory {
	
	private File baseFld;
	public ConllDatasetPath makeADataset2016(File dataFld, ConllDatasetPath.DatasetMode mode){
		File conllDatasetBase = new File(dataFld, "conll2016-dataset/conll16st-en-zh-dev-train_LDC2016E50/");
		
		switch (mode) {
		case train:
			baseFld = new File(conllDatasetBase, "conll16st-en-01-12-16-train/");
			break;

		case dev:
			baseFld = new File(conllDatasetBase, "conll16st-en-01-12-16-dev/");
			break;
		
		case test:
			baseFld = dataFld;
			break;
			
		case trial:
			baseFld = new File(dataFld, "conll16st/tutorial/conll16st-en-01-12-16-trial");
			break;
		default:
			return null;
		}

		if (baseFld == null || !baseFld.exists())
			throw new RuntimeException("The base fld is not valid folder for the CoNLL dataset: " + baseFld.getAbsolutePath());
		
		return new ConllDatasetPath(new File(baseFld, "raw"), new File(baseFld, "relations.json"), 
				new File(baseFld, "parses.json"), new File(baseFld, "relations-no-senses.json"), mode);
		
	}
	
	public ConllDatasetPath makeADataset(File dataFld, ConllDatasetPath.DatasetMode mode) {
		switch (mode) {
		case train:
			baseFld = new File(dataFld, "conll15st-train-dev/conll15st_data/conll15-st-03-04-15-train");
			break;

		case dev:
			baseFld = new File(dataFld, "conll15st-train-dev/conll15st_data/conll15-st-03-04-15-dev");
			break;
		
		case test:
			baseFld = dataFld;
			
		default:
			baseFld = new File(dataFld, "conll15st-trial-data");
			break;
		}

		if (baseFld == null || !baseFld.exists())
			throw new RuntimeException("The base fld is not valid folder for the CoNLL dataset: " + baseFld);
		
		return new ConllDatasetPath(findFile("raw"), findFile("data"), findFile("parses"), null, mode);
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
		File dataFld = new File("../discourse.conll.dataset/data");
		for (ConllDatasetPath.DatasetMode mode: ConllDatasetPath.DatasetMode.values()){
			factory.makeADataset(dataFld, mode);
			factory.makeADataset2016(dataFld, mode);
		}
		System.out.println("ConllDatasetPathFactory.main()");
	}

	public ConllDatasetPath makeADataset(DatasetMode mode) {
		return makeADataset(new File("data"), mode);
	}
}
