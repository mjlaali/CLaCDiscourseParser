package org.cleartk.discourse_parsing.module;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.weka.WekaStringOutcomeClassifier;

import weka.core.Instances;

public abstract class ClassifierLabeler<CLASSIFIER_OUTPUT, INSTANCE_TYPE> extends CleartkAnnotator<CLASSIFIER_OUTPUT> implements Labeler<CLASSIFIER_OUTPUT, INSTANCE_TYPE>{
	protected JCas aJCas;
	private WekaStringOutcomeClassifier wekaStringOutcomeClassifier ;
	private static int correct, total;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		if (!isTraining()){
			if (classifier instanceof WekaStringOutcomeClassifier){
				wekaStringOutcomeClassifier = (WekaStringOutcomeClassifier) classifier;
			}
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		this.aJCas = aJCas;
		init();
		List<INSTANCE_TYPE> instances = getInstances(aJCas);

		for (INSTANCE_TYPE instance: instances){
			List<Feature> features = extractFeature(aJCas, instance);
			CLASSIFIER_OUTPUT label = null;
			label = getLabel(instance);

			if (isTraining()){
				dataWriter.write(new Instance<CLASSIFIER_OUTPUT>(label, features));
			} else {
				CLASSIFIER_OUTPUT classifiedLabel = null;
				classifiedLabel = classifier.classify(features);
				setLabel(aJCas, instance, classifiedLabel);
				if (wekaStringOutcomeClassifier != null && label != null){
					wekaStringOutcomeClassifier.store(features, label.toString());
					correct += label.toString().equals(classifiedLabel.toString()) ? 1 : 0;
					total++;
				}
			}
		}
		
		wrapUp();
		
		if (wekaStringOutcomeClassifier != null){
			Instances allInstances = wekaStringOutcomeClassifier.getAllInstances();
			try {
				PrintWriter printStream = new PrintWriter("outputs/parser/testedInstances.arff");
				printStream.write(allInstances.toString());
				printStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	protected void wrapUp() {
	}
	
	public static int getCorrect() {
		return correct;
	}
	
	public static int getTotal() {
		return total;
	}

	protected abstract CLASSIFIER_OUTPUT getLabel(INSTANCE_TYPE instance);
	protected abstract List<Feature> extractFeature(JCas defView, INSTANCE_TYPE instance) throws CleartkExtractorException;

}
