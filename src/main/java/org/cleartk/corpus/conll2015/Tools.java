package org.cleartk.corpus.conll2015;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ViewUriUtil;

public class Tools {
	public static final String SCORER_DIR = "data/validator";
	public static final String SCORER_PY_FILE = "scorer.py";
	
	public static String getDocName(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			if (aJCas.getView(ViewUriUtil.URI) != null)
				return new File(ViewUriUtil.getURI(aJCas)).getName();
		} catch (CASRuntimeException | CASException e) {
		}
		return "";
	}

	public static LinkedList<String> runScorer(String goldAnnotation, String systemOutput) throws IOException {
		ProcessBuilder pb = new ProcessBuilder("python", SCORER_PY_FILE, 
				new File(goldAnnotation).getAbsolutePath(), new File(systemOutput).getAbsolutePath());
		pb.directory(new File(SCORER_DIR));
		Process p = pb.start();

		InputStream[] inputstreams = new InputStream[]{p.getErrorStream(), p.getInputStream()};
		String line;
		LinkedList<String> consolOutputs = new LinkedList<>();
		for (InputStream inputStream: inputstreams){
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = in.readLine()) != null){
				consolOutputs.add(line);
			}
		}
		return consolOutputs;
	}
	
	public static Map<String, String> readCSVFile(
			String csvFile) {
		Map<String, String> csvTable = new TreeMap<String, String>();
		
		try {
			for (String line: FileUtils.readLines(new File(csvFile))) {
				String[] cells = line.split(",");
				csvTable.put(cells[0], cells[1]);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return csvTable;
	}
}
