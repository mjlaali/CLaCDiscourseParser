package org.cleartk.corpus.conll2015.loader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cleartk.corpus.conll2015.ConllDatasetPath;

public class ConllDataLoaderFactory {
	public static final String PRE_PROCESSED_FILE_LOCATION = "raw-preprocessed"; 

	public static void clean(ConllDatasetPath path) throws IOException{
		File preprocessedFilesLocation = getPreprocessFilesLocation(path);
		FileUtils.deleteDirectory(preprocessedFilesLocation);
	}
	
	public static ConllDataLoader getInstance(ConllDatasetPath path){
		File preprocessedFilesLocation = getPreprocessFilesLocation(path);
		if (preprocessedFilesLocation.exists())
			return new PreprocessedDataLoader(preprocessedFilesLocation, path);
		
		return new LoaderPlusAnnotator(path, preprocessedFilesLocation);
	}
	
	public static File getPreprocessFilesLocation(ConllDatasetPath path) {
		String baseDirectory = FilenameUtils.getFullPathNoEndSeparator(path.getRawDirectory().getAbsolutePath());
		File preprocessedFilesLocation = new File(baseDirectory, PRE_PROCESSED_FILE_LOCATION);
		return preprocessedFilesLocation;
	}
	
}
