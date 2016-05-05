package ca.concordia.clac.ml.classifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GenericClassifierLabeller<CLASSIFIER_OUTPUT, INSTANCE_TYPE>
		extends CleartkAnnotator<CLASSIFIER_OUTPUT> {
	public static final String PARAM_PARALLEL_CLASSIFICATION = "parallelClassification";
	public static final String PARAM_LABELER_CLS_NAME = "labellerClsName";
	public static final String PARAM_GOLD_VIEW = "goldViewName";
	public static final String PARAM_SYSTEM_VIEW = "systemViewName";
	public static final String PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT = "defaultGoldClassifierOutput";

	@ConfigurationParameter(name = PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT, mandatory = false)
	private CLASSIFIER_OUTPUT defaultGoldClassifierOutput;
	
	@ConfigurationParameter(name = PARAM_GOLD_VIEW, mandatory = false)
	private String goldViewName;

	@ConfigurationParameter(name = PARAM_SYSTEM_VIEW, mandatory = false)
	private String systemViewName;

	@ConfigurationParameter(name = PARAM_LABELER_CLS_NAME)
	private String labellerClsName;

	@ConfigurationParameter(name = PARAM_PARALLEL_CLASSIFICATION, defaultValue = "false")
	private Boolean parallelClassification;

	private GenericClassifier<CLASSIFIER_OUTPUT, INSTANCE_TYPE> genericClassifier;
	
	private GoldClassifier<CLASSIFIER_OUTPUT> goldClassifier = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory = InitializableFactory.create(context, labellerClsName, ClassifierAlgorithmFactory.class);
		
		Consumer<Instance<CLASSIFIER_OUTPUT>> writerFunc = null;
		Function<List<Feature>, CLASSIFIER_OUTPUT> classifierFunc = null;
		if (goldViewName != null){
			if (systemViewName == null)
				systemViewName = CAS.NAME_DEFAULT_SOFA;
			if (defaultGoldClassifierOutput == null)
				throw new ResourceInitializationException(PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT + " is null", null);
			goldClassifier = new GoldClassifier<>(defaultGoldClassifierOutput);
			writerFunc = goldClassifier;
			classifierFunc = goldClassifier;
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
		
		this.genericClassifier = new GenericClassifier<>(algorithmFactory, writerFunc, 
				classifierFunc, parallelClassification, isTraining());
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if (goldClassifier != null){
			try {
				JCas goldView = aJCas.getView(goldViewName);
				genericClassifier.setTraining(true);
				genericClassifier.process(goldView);
				
				JCas systemView = aJCas.getView(systemViewName);
				genericClassifier.setTraining(false);
				genericClassifier.process(systemView);
				goldClassifier.clear();
			} catch (CASException e) {
				e.printStackTrace();
			}
		} else
			genericClassifier.process(aJCas);
	}
}
