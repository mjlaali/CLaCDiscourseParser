package discourse.parser.arg_labeler_nn;

import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.Train;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.GenericSequenceClassifier;
import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

class LabelDetector implements BiFunction<List<Token>, DiscourseConnective, List<String>> {

	@Override
	public List<String> apply(List<Token> tokenList, DiscourseConnective dc) {
		DiscourseRelation discourseRelation = dc.getDiscourseRelation();
		List<Token> arg1 = TokenListTools.convertToTokens(discourseRelation.getArguments(0));
		List<Token> arg2 = TokenListTools.convertToTokens(discourseRelation.getArguments(1));
		List<Token> discourseConnective = TokenListTools.convertToTokens(dc);
		List<String> labels = new ArrayList<>();
		
		for (Token token: tokenList){
			if (include(arg1, token)){
				labels.add("arg1");
			} else if (include(arg2, token)){
				labels.add("arg2");
			} else if (include(discourseConnective, token)){
				labels.add("dc");
			} else {
				labels.add("non");
			}
		}
		
		return labels;
	}

	private boolean include(List<Token> arg1, Token token) {
		return arg1.contains(token);
	}
	
	
	
}

public class ArgLabelerFeatureExtractor implements SequenceClassifierAlgorithmFactory<String, DiscourseConnective, Token> {

	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		return (jcas) -> JCasUtil.select(jcas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Token>> getInstanceExtractor(JCas aJCas) {
		return (dc) -> {
			List<Token> tokens = new ArrayList<>();
			Sentence sentenceWithDc = JCasUtil.selectCovering(Sentence.class, dc).get(0);
			tokens.addAll(JCasUtil.selectCovered(Token.class, sentenceWithDc));
			List<Sentence> selectPreceding = JCasUtil.selectPreceding(Sentence.class, sentenceWithDc, 1);
			if (selectPreceding.size() > 0){
				tokens.addAll(0, JCasUtil.selectCovered(Token.class, selectPreceding.get(0)));
			}
			return tokens;
		};
	}

	@Override
	public BiFunction<List<Token>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		
		return (tokenList, dc) -> tokenList.stream()
					.map(Token::getCoveredText)
					.map(makeFeature("text"))
					.map(Collections::singletonList)
					.collect(Collectors.toList());
				
	}

	@Override
	public BiFunction<List<Token>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		return new LabelDetector();
	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, Token> getLabeller(JCas jCas) {
		return null;
	}
	
	
	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(StringSequenceClassifier.class,
				GenericSequenceClassifier.PARAM_ALGORITHM_FACTORY_CLASS_NAME,
				ArgLabelerFeatureExtractor.class.getName(),
				CleartkSequenceAnnotator.PARAM_IS_TRAINING,
		        true,
		        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		        outputDirectory,
		        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
		        MalletCrfStringOutcomeDataWriter.class);
	}
	
	public static void main(String[] args) throws Exception {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getDataJSonFile());

		File outputDirectory = new File(new File("outputs/resources"), "package");
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(outputDirectory)
				);

		 Train.main(outputDirectory);
	}
	

}
