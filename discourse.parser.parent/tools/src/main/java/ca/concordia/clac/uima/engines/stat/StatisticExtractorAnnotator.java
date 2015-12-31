package ca.concordia.clac.uima.engines.stat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import ca.concordia.clac.ml.classifier.InstanceExtractor;

public class StatisticExtractorAnnotator<T> extends JCasAnnotator_ImplBase{

	public static final String PARAM_OUTPUT_FILE = "outputFile";
	public static final String PARAM_POLICY_CLASS_NAME = "policyClassName";

	@ConfigurationParameter(name=PARAM_OUTPUT_FILE)
	private File outputFile;
	
	@ConfigurationParameter(name=PARAM_POLICY_CLASS_NAME)
	private String policyClassName;
	
	private InstanceExtractor<T> instanceExtractor;
	private Function<T, String> keyExtractor;
	private Function<T, String> valueExtractor;
	
	private Map<String, Map<String, Integer>> counts = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		ExtractorPolicy<T> extractorPolicy = InitializableFactory.create(context, policyClassName, ExtractorPolicy.class);
		instanceExtractor = extractorPolicy.getInstanceExtractor();
		keyExtractor = extractorPolicy.getKeyExtractor();
		valueExtractor = extractorPolicy.getValueExtractor();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<T> instances = instanceExtractor.getInstances(aJCas);
		 
		instances.stream().forEach(this::process);
	}

	private void process(T instance){
		String key = keyExtractor.apply(instance);
		String value = valueExtractor.apply(instance);
		
		Map<String, Integer> freq = counts.get(key);
		if (freq == null){
			freq = new HashMap<>();
			counts.put(key, freq);
		}
		
		Integer cnt = freq.get(value);
		if (cnt == null){
			cnt = 0;
		}
		freq.put(value, cnt + 1);
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		
		try {
			outputFile.getParentFile().mkdirs();
			StatisticResult statisticResult = new StatisticResult(counts);
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(outputFile));
			output.writeObject(statisticResult);
			output.close();
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
	}
}
