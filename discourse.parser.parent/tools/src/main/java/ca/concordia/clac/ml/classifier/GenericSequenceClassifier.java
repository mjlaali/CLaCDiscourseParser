package ca.concordia.clac.ml.classifier;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.Instances;

public class GenericSequenceClassifier<CLASSIFIER_OUTPUT, SEQUENCE_TYPE, INSTANCE_TYPE> extends CleartkSequenceAnnotator<CLASSIFIER_OUTPUT>{
	
	protected SequenceClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, SEQUENCE_TYPE, INSTANCE_TYPE> algorithmFactory;
	protected Consumer<List<Instance<CLASSIFIER_OUTPUT>>> writerFunc;
	protected Function<List<List<Feature>>, List<CLASSIFIER_OUTPUT>> classifierFunc;
	
	public static final String PARAM_ALGORITHM_FACTORY_CLASS_NAME = "algorithmFactoryClassName";

	@ConfigurationParameter(name = PARAM_ALGORITHM_FACTORY_CLASS_NAME)
	private String algorithmFactoryClassName;
	
	@ConfigurationParameter(name = GenericClassifierLabeller.PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT, mandatory = false)
	private CLASSIFIER_OUTPUT defaultGoldClassifierOutput;
	
	@ConfigurationParameter(name = GenericClassifierLabeller.PARAM_GOLD_VIEW, mandatory = false)
	private String goldViewName;

	@ConfigurationParameter(name = GenericClassifierLabeller.PARAM_SYSTEM_VIEW, mandatory = false)
	private String systemViewName;

	private GoldSequenceClassifier<CLASSIFIER_OUTPUT> goldSequenceClassifier;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		algorithmFactory = InitializableFactory.create(context, algorithmFactoryClassName, SequenceClassifierAlgorithmFactory.class);
		
		
		if (goldViewName != null && systemViewName != null){
			if (defaultGoldClassifierOutput == null)
				throw new ResourceInitializationException(GenericClassifierLabeller.PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT + " is null", null);
			goldSequenceClassifier = new GoldSequenceClassifier<>(defaultGoldClassifierOutput);
			writerFunc = goldSequenceClassifier;
			classifierFunc = goldSequenceClassifier;
		} else {
			writerFunc = (instance) -> {
				try {
					dataWriter.write(instance) ;
				} catch (CleartkProcessingException e) {
					throw new RuntimeException(e);
				}
			};
			classifierFunc = (features) -> {
				try {
					return classifier.classify(features);
				} catch (CleartkProcessingException e) {
					throw new RuntimeException(e);
				}
			};
		}

		
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if (goldSequenceClassifier != null){
			try {
				JCas goldView = aJCas.getView(goldViewName);
				internalProcess(goldView);
				JCas systemView = aJCas.getView(systemViewName);
				internalProcess(systemView);
				goldSequenceClassifier.clear();
			} catch (CASException e) {
				e.printStackTrace();
			}
		} else
			internalProcess(aJCas);
	}
	
	public void internalProcess(JCas aJCas) throws AnalysisEngineProcessException {
		Function<JCas, ? extends Collection<? extends SEQUENCE_TYPE>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Function<SEQUENCE_TYPE, List<INSTANCE_TYPE>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		BiFunction<List<INSTANCE_TYPE>, SEQUENCE_TYPE, List<List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
		BiFunction<List<INSTANCE_TYPE>, SEQUENCE_TYPE, List<CLASSIFIER_OUTPUT>> labelExtractor = algorithmFactory.getLabelExtractor(aJCas);
		SequenceClassifierConsumer<CLASSIFIER_OUTPUT, SEQUENCE_TYPE, INSTANCE_TYPE> labeller = algorithmFactory.getLabeller(aJCas);

		Collection<? extends SEQUENCE_TYPE> sequences =  sequenceExtractor.apply(aJCas);
		
		for (SEQUENCE_TYPE aSequence: sequences){
			List<INSTANCE_TYPE> instances = instanceExtractor.apply(aSequence);
			List<List<Feature>> featureLists = featureExtractor.apply(instances, aSequence);
			
			if (isTraining()){
				List<CLASSIFIER_OUTPUT> outcomes = labelExtractor.apply(instances, aSequence);
				writerFunc.accept(Instances.toInstances(outcomes, featureLists));
			} else {
				if (featureLists.size() > 0) {
					List<CLASSIFIER_OUTPUT> outcomes = classifierFunc.apply(featureLists);
					labeller.accept(outcomes, aSequence, instances);
				}
			}
		}
	
		
	}
	


}
