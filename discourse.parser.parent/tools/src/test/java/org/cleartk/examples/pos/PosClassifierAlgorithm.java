package org.cleartk.examples.pos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction.Orientation;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.LowerCaseFeatureFunction;
import org.cleartk.ml.feature.function.NumericTypeFeatureFunction;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;
import org.cleartk.ml.viterbi.DefaultOutcomeFeatureExtractor;
import org.cleartk.ml.viterbi.ViterbiDataWriterFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

import ca.concordia.clac.ml.classifier.GenericSequenceClassifier;
import ca.concordia.clac.ml.classifier.InstanceExtractor;
import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;

public class PosClassifierAlgorithm implements SequenceClassifierAlgorithmFactory<String, Sentence, Token>, Initializable {

	private FeatureExtractor1<Token> tokenFeatureExtractor;
	private CleartkExtractor<Token, Token> contextFeatureExtractor;

	public InstanceExtractor<List<Token>> getExtractor(JCas jCas) {
		return (aJCas) -> {
			List<List<Token>> results = new ArrayList<>();
			Collection<Sentence> sents = JCasUtil.select(aJCas, Sentence.class);
			for (Sentence sent: sents){
				results.add(JCasUtil.selectCovered(Token.class, sent));
			}
			return results;
		};
	}

	@Override
	public Function<JCas, ? extends Collection<? extends Sentence>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, Sentence.class);
	}

	@Override
	public Function<Sentence, List<Token>> getInstanceExtractor(JCas aJCas) {
		return (sent) -> JCasUtil.selectCovered(Token.class, sent);
	}

	@Override
	public BiFunction<List<Token>, Sentence, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		BiFunction<Token, Sentence, List<Feature>> tfe, cfe;
		tfe = (t, sent) -> {
			try {
				return tokenFeatureExtractor.extract(jCas, t);
			} catch (CleartkExtractorException e) {
				throw new RuntimeException(e);
			}
		};
		cfe = (t, sent) -> {
			try {
				return contextFeatureExtractor.extractWithin(jCas, t, sent);
			} catch (CleartkExtractorException e) {
				throw new RuntimeException(e);
			}
		};
		return (tokens, sent) -> {
			List<List<Feature>> results = new ArrayList<>();
			List<List<Feature>> f1 = tokens.stream().map((t) -> tfe.apply(t, sent)).collect(Collectors.toList());
			List<List<Feature>> f2 = tokens.stream().map((t) -> cfe.apply(t, sent)).collect(Collectors.toList());

			for (int i = 0; i < f1.size(); i++){
				List<Feature> features = new ArrayList<>();
				features.addAll(f1.get(i));
				features.addAll(f2.get(i));
				results.add(features);
			}
			return results;
		};
	}

	@Override
	public BiFunction<List<Token>, Sentence, List<String>> getLabelExtractor(JCas jCas) {
		return (tokens, sent) -> 
		tokens.stream().map(Token::getPos).collect(Collectors.toList());
	}

	@Override
	public SequenceClassifierConsumer<String, Sentence, Token> getLabeller(JCas jCas) {
		return (poses, sent, tokens) -> {
			for (int i = 0; i < poses.size(); i++)
				tokens.get(i).setPos(poses.get(i));
		};
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		// a feature extractor that creates features corresponding to the word, the word lower cased
		// the capitalization of the word, the numeric characterization of the word, and character ngram
		// suffixes of length 2 and 3.
		this.tokenFeatureExtractor = new FeatureFunctionExtractor<Token>(
				new CoveredTextExtractor<Token>(),
				new LowerCaseFeatureFunction(),
				new CapitalTypeFeatureFunction(),
				new NumericTypeFeatureFunction(),
				new CharacterNgramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 2),
				new CharacterNgramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 3));

		// a feature extractor that extracts the surrounding token texts (within the same sentence)
		this.contextFeatureExtractor = new CleartkExtractor<Token, Token>(
				Token.class,
				new CoveredTextExtractor<Token>(),
				new Preceding(2),
				new Following(2));
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(StringSequenceClassifier.class,
				GenericSequenceClassifier.PARAM_ALGORITHM_FACTORY_CLASS_NAME,
				PosClassifierAlgorithm.class.getName(),
				CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				ViterbiDataWriterFactory.class.getName(),
				DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
				outputDirectory,
				ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS,
				DefaultDataWriterFactory.class.getName(),
				DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
				MaxentStringOutcomeDataWriter.class.getName(),
				ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
				new String[] { DefaultOutcomeFeatureExtractor.class.getName() }
				);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String modelFileName) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
		        StringSequenceClassifier.class,
		        GenericSequenceClassifier.PARAM_ALGORITHM_FACTORY_CLASS_NAME,
				PosClassifierAlgorithm.class.getName(),
		        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
		        modelFileName);
	}


}
