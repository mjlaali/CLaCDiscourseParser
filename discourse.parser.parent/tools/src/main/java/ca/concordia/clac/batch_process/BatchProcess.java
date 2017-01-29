package ca.concordia.clac.batch_process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.cpe.CpeBuilder;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class BatchProcess implements Serializable{
	private static final long serialVersionUID = -2440614205167347514L;
	private final static Logger logger = LogManager.getLogger(BatchProcess.class);
	public static final String BATCH_CONFIG_FILE_NAME = "batch.config";
	public static final String LOADING_PROCESS_NAME = "loading";

	private Map<String, List<AnalysisEngineDescription>> processeEngines = new HashMap<>();
	private List<String> processInOrder = new ArrayList<>();
	private Map<String, String> prevProcessName = new HashMap<>();
	private final CollectionReaderDescription inputReaderDescription;
	private final File outputDir;
	private File temp;
	private int threadCount = 1;



	public static CollectionReaderDescription getXmiReader(File inputDir) throws ResourceInitializationException{
		return CollectionReaderFactory.createReaderDescription(XmiReader.class, 
				XmiReader.PARAM_SOURCE_LOCATION, inputDir, 
				XmiReader.PARAM_PATTERNS, new String[]{"*.xmi"});	
	}

	public BatchProcess(File inputDir, File outputDir) throws ResourceInitializationException{
		this(inputDir, outputDir, "en", "*.txt");
	}

	public BatchProcess(File inputDir, File outputDir, String lang, String... patterns) throws ResourceInitializationException{
		this(CollectionReaderFactory.createReaderDescription(TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, inputDir,
				TextReader.PARAM_LANGUAGE, lang, 
				TextReader.PARAM_PATTERNS, patterns), outputDir);
	}


	public BatchProcess(CollectionReaderDescription inputReaderDescription, File outDir) {
		this.inputReaderDescription = inputReaderDescription;
		this.outputDir = outDir;
		addProcess(LOADING_PROCESS_NAME, new AnalysisEngineDescription[0]);
		setTempDirectory();
	}

	private void setTempDirectory() {
		temp = getDirectory("temp");
	}


	public void addProcess(String name, AnalysisEngineDescription... engines){

		List<AnalysisEngineDescription> enginesList = processeEngines.get(name);
		if (enginesList == null){
			if (processInOrder.size() > 0)
				prevProcessName.put(name, processInOrder.get(processInOrder.size() - 1));
			processInOrder.add(name);
			enginesList = new ArrayList<>();
			processeEngines.put(name, enginesList);
		}
		enginesList.addAll(Arrays.asList(engines));
	}

	public void addProcess(String name, String viewName, AnalysisEngineDescription... engines) throws ResourceInitializationException{
		AggregateBuilder aggregate = new AggregateBuilder();
		for (AnalysisEngineDescription engine: engines)
			aggregate.add(engine, CAS.NAME_DEFAULT_SOFA, viewName);

		addProcess(name, aggregate.createAggregateDescription());
	}

	public void run() throws UIMAException, IOException, SAXException, CpeDescriptorException{
		for (String processName: processInOrder){
			logger.info(String.format("Start processing %s ...", processName));

			List<AnalysisEngineDescription> engines = new ArrayList<>(processeEngines.get(processName));
			engines.add(getWriter(processName));

			CollectionReaderDescription reader = getReader(processName);
			AnalysisEngineDescription allEngine = AnalysisEngineFactory.createEngineDescription(
					engines.toArray(new AnalysisEngineDescription[engines.size()]));

			enginesProcess(reader, allEngine);

			logger.info(String.format("Processing %s done!", processName));
		}

	}

	private void enginesProcess(CollectionReaderDescription reader,
			AnalysisEngineDescription allEngine) throws IOException, SAXException, CpeDescriptorException,
					InvalidXMLException, ResourceInitializationException, AnalysisEngineProcessException {
		CpeBuilder builder=new CpeBuilder();
		builder.setReader(reader);
		builder.setAnalysisEngine(allEngine);
		builder.setMaxProcessingUnitThreadCount(threadCount);

		StatusCallbackListenerImpl status = new StatusCallbackListenerImpl();
		CollectionProcessingEngine engine = builder.createCpe(status); 
		engine.process();
		try {
			synchronized (status) {
				int cnt = 0;
				while (status.isProcessing()) {
					if (status.getCompletedEntityCnt() != cnt){
						cnt = status.getCompletedEntityCnt();
						logger.info("Completed Files: " + cnt);
					}
					status.wait();
				}
			}
		}
		catch (InterruptedException e) {
			// Do nothing
		}
		
		if (status.getExceptions().size() > 0) {
			throw new AnalysisEngineProcessException(status.getExceptions().get(0));
		}

	}

	private AnalysisEngineDescription getWriter(String processName) throws ResourceInitializationException {
		File outputDir = getDirectory(processName);

		return AnalysisEngineFactory.createEngineDescription(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, outputDir);
	}

	private File getDirectory(String processName){
		File processDir = new File(outputDir, processName);
		if (!processDir.exists())
			processDir.mkdirs();
		return processDir;

	}
	private CollectionReaderDescription getReader(String process) throws IOException, ResourceInitializationException {
		String prevProcess = prevProcessName.get(process);

		if (prevProcess == null){
			return inputReaderDescription;
		} else {
			moveNotProcessedFileToTempDir(getDirectory(prevProcess), getDirectory(process));
			return CollectionReaderFactory.createReaderDescription(XmiReader.class, 
					XmiReader.PARAM_SOURCE_LOCATION, temp, 
					XmiReader.PARAM_PATTERNS, new String[]{"*.xmi"});
		}

	}

	private void moveNotProcessedFileToTempDir(File from, File processed) throws IOException {
		File to = temp;

		if (to.exists()){
			FileUtils.deleteDirectory(to);
		}

		Set<String> processedFiles = new HashSet<>();
		File[] listFiles = processed.listFiles();
		if (listFiles != null)
			for (File f: listFiles){
				processedFiles.add(f.getName());
			}

		for (File f: from.listFiles()){
			if (!processedFiles.contains(f.getName())){
				FileUtils.copyFile(f, new File(to, f.getName()));
			}
		}
	}

	public static BatchProcess load(File outputDir) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(outputDir, BATCH_CONFIG_FILE_NAME))));
		Object batchProcess = input.readObject();
		input.close();
		return (BatchProcess)batchProcess;
	}

	public void save() throws FileNotFoundException, IOException{
		ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(outputDir, BATCH_CONFIG_FILE_NAME))));
		output.writeObject(this);
		output.close();
	}

	public void clean() throws IOException{
		FileUtils.deleteDirectory(outputDir);
		setTempDirectory();
	}

	public void clean(String processName) throws IOException{
		File dir = getDirectory(processName);
		if (dir.exists()){
			FileUtils.deleteDirectory(dir);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
