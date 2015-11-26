package org.cleartk.corpus.conll2015;

import java.io.File;

public class ConllDatasetPath {
	
	public static enum DatasetMode{
		trial, train, dev
	}

	File rawDirectory;
	File dataJSonFile;
	File parsesJSonFile;
	DatasetMode mode;
	
	public ConllDatasetPath(File rawDirectory, File dataJSonFile, File parsesJSonFile, DatasetMode mode){
		this.rawDirectory = rawDirectory;
		this.dataJSonFile = dataJSonFile;
		this.parsesJSonFile = parsesJSonFile;
		
		for (File file: new File[]{rawDirectory, dataJSonFile, parsesJSonFile}){
			if (file == null || !file.exists())
				throw new RuntimeException("There is issue with the file: " + file);
		}
		this.mode = mode;
	}


	public File getDataJSonFile() {
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
	
}
