package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Classifier;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.MockitoClassifierFactory;
import org.cleartk.ml.MockitoDataWriterFactory;
import org.json.JSONException;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ParseComponentBaseTest<T> {
	protected JCas aJCas;
	protected DiscourseRelation discourseRelation;
	protected SyntaxReader syntaxReader = new SyntaxReader();
	protected DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();

	protected String arg1;
	protected String arg2;

	protected @Captor ArgumentCaptor<Instance<T>> instanceCaptor;
	protected @Mock DataWriter<T> dataWrite;

	@Before
	public void setUp() throws UIMAException, CASException, JSONException,
	ResourceInitializationException, AnalysisEngineProcessException,
	CleartkProcessingException {
		MockitoAnnotations.initMocks(this);
		aJCas = JCasFactory.createJCas();

	}

	protected void run(boolean isTraining, AnalysisEngineDescription writerDescription, Classifier<String> classifier)
			throws ResourceInitializationException,
			AnalysisEngineProcessException, CleartkProcessingException {

		if (isTraining && discourseRelation != null){
			discourseRelation.addToIndexes();

			Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
			assertThat(discourseRelations).hasSize(1);
			assertThat(JCasUtil.select(aJCas, DiscourseConnective.class)).hasSize(1);
		}

		MockitoDataWriterFactory.setInstance(dataWrite);
		MockitoClassifierFactory.setInstance(classifier);
		AnalysisEngine createEngine = AnalysisEngineFactory.createEngine(writerDescription, 
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, 
				MockitoDataWriterFactory.class.getName(), 
				CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
				MockitoClassifierFactory.class.getName(),
				CleartkAnnotator.PARAM_IS_TRAINING, 
				isTraining);
		createEngine.process(aJCas);
		createEngine.collectionProcessComplete();

		if (isTraining){
			verify(dataWrite).finish();
		}
	}

	protected List<String> getFeature(List<Instance<String>> list, String featureName) {
		List<String> featureValues = new ArrayList<String>();
		for (Instance<String> instance: list){
			for (Feature feature: instance.getFeatures())
				if (featureName.equals(feature.getName()))
					featureValues.add(feature.getValue().toString());
		}
		return featureValues;
	}

	protected Set<String> getOutcomes(List<Instance<String>> target) {
		Set<String> outcomes = new TreeSet<String>();
		for (Instance<String> instance: target){
			outcomes.add(instance.getOutcome());
		}
		return outcomes;
	}

}