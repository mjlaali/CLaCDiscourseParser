package ca.concordia.clac.ml.classifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GenericSequenceClassifier<CLASSIFIER_OUTPUT, INSTANCE_TYPE> extends CleartkSequenceAnnotator<CLASSIFIER_OUTPUT>{
	
	protected ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory;
	protected Consumer<Instance<CLASSIFIER_OUTPUT>> writerFunc;
	protected Function<List<Feature>, CLASSIFIER_OUTPUT> classifierFunc;
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		List<List<INSTANCE_TYPE>> instances;
		List<List<List<Feature>>> allFeatures;
		
		if (this.isTraining()){
			List<List<CLASSIFIER_OUTPUT>> outcomes;
		} else {
			List<List<CLASSIFIER_OUTPUT>> outcomes;
		}
	}

}
