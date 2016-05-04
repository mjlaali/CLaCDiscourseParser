package org.cleartk.corpus.conll2015.loader;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.cleartk.corpus.conll2015.ConllDatasetPath;

public class ConllDataLoaderFactory {
	public static final String PRE_PROCESSED_FILE_LOCATION = "raw-preprocessed"; 

	public static ConllDataLoader getInstance(ConllDatasetPath path){
		File preprocessedFilesLocation = getPreprocessFilesLocation(path);
		if (preprocessedFilesLocation.exists())
			return new PreprocessedDataLoader(preprocessedFilesLocation);
		
		return new LoaderPlusAnnotator(path, preprocessedFilesLocation);
	}
	
	public static File getPreprocessFilesLocation(ConllDatasetPath path) {
		String baseDirectory = FilenameUtils.getFullPathNoEndSeparator(path.getRawDirectory().getAbsolutePath());
		File preprocessedFilesLocation = new File(baseDirectory, PRE_PROCESSED_FILE_LOCATION);
		return preprocessedFilesLocation;
	}
	
}
