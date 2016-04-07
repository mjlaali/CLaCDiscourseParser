package discourse.parser.arg_labeler_nn;

import java.io.File;

import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;

public class ConllDatasetPathFactory2016 {
	
	private File baseFld;
	
	public ConllDatasetPath makeADataset(File dataFld, ConllDatasetPath.DatasetMode mode) {
		switch (mode) {
		case train:
			baseFld = new File(dataFld, "conll16st-train/train");
			break;

		case dev:
			baseFld = new File(dataFld, "conll16st-train/dev");
			break;
		
		case test:
			baseFld = dataFld;
			
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
		ConllDatasetPathFactory2016 factory = new ConllDatasetPathFactory2016();
		File dataFld = new File("data");
		for (ConllDatasetPath.DatasetMode mode: ConllDatasetPath.DatasetMode.values())
			factory.makeADataset(dataFld, mode);
	}

	public ConllDatasetPath makeADataset(DatasetMode mode) {
		return makeADataset(new File("data"), mode);
	}
}
