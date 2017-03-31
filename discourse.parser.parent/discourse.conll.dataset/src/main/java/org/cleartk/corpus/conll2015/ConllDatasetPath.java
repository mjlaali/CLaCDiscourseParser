package org.cleartk.corpus.conll2015;

import java.io.File;

public class ConllDatasetPath {
	
	public static enum DatasetMode{
		trial, train, dev, test, pdtb_test, blind_test
	}

	File rawDirectory;
	File dataJSonFile;
	File parsesJSonFile;
	File relationNoSenseFile;
	DatasetMode mode;
	
	public ConllDatasetPath(File rawDirectory, File dataJSonFile, File parsesJSonFile, File relationNoSenseFile, DatasetMode mode){
		this.rawDirectory = rawDirectory;
		this.dataJSonFile = dataJSonFile;
		this.parsesJSonFile = parsesJSonFile;
		this.relationNoSenseFile = relationNoSenseFile;
		
		if (mode != DatasetMode.test && dataJSonFile == null)
			throw new RuntimeException("There is issue with the file: " + dataJSonFile);
		
		for (File file: new File[]{rawDirectory, parsesJSonFile}){
			if (file == null || !file.exists())
				throw new RuntimeException("The file/folder does not exist: " + file);
		}
		
		File[] files = rawDirectory.listFiles();
		if (files.length == 0)
			throw new RuntimeException("Raw directory is empty: " + rawDirectory.getAbsolutePath());
		System.err.println(files.length + " files are available in the raw directory." );
		
		this.mode = mode;
	}


	public File getRelationsJSonFile() {
		return dataJSonFile;
	}
	
	public DatasetMode getMode() {
		return mode;
	}
	
	public File getParsesJSonFile() {
		return parsesJSonFile;
	}
	
	public File getRawDirectory() {
		return rawDirectory;
	}
	
	public File getRelationNoSenseFile(){
		return relationNoSenseFile;
	}
	
}
