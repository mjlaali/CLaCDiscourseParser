package org.cleartk.corpus.conll2015;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class NoRelationAnnotator extends JCasAnnotator_ImplBase{
	DiscourseRelationFactory factory = new DiscourseRelationFactory();

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		Map<Sentence, Collection<Token>> sentToToken = JCasUtil.indexCovered(aJCas, Sentence.class, Token.class);
		Map<Sentence, Set<DiscourseRelation>> sentToRelation = createSentenceToRelationMap(aJCas);

		Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);
		Sentence prev = null;
		for (Sentence current: sentences){
			if (prev == null){
				prev = current;
				continue;
			}

			Set<DiscourseRelation> intersect = new HashSet<>();

			Set<DiscourseRelation> prevRelations = sentToRelation.get(prev);
			if (prevRelations != null)
				intersect.addAll(prevRelations);

			Set<DiscourseRelation> currentRelation = sentToRelation.get(current);
			if (currentRelation == null)
				intersect.clear();
			else
				intersect.retainAll(currentRelation);

			if (intersect.isEmpty()){	 //there is no relation between these two sentences
				RelationType type = RelationType.NoRel;
				String sense = "";
				String discourseConnectiveText = null;
				List<Token> discourseConnectiveTokens = null;
				List<Token> arg1 = new ArrayList<>(sentToToken.get(prev));
				List<Token> arg2 = new ArrayList<>(sentToToken.get(current));

				arg1.remove(arg1.size() - 1);	//remove the last dot
				arg2.remove(arg2.size() - 1);	//remove the last dot

				DiscourseRelation discourseRelation = 
						factory.makeDiscourseRelation(aJCas, 
								type, sense, discourseConnectiveText, discourseConnectiveTokens, arg1, arg2);

				discourseRelation.addToIndexesRecursively();
			}
			
			prev = current;
		}

	}

	private Map<Sentence, Set<DiscourseRelation>> createSentenceToRelationMap(JCas aJCas) {
		Map<Sentence, Set<DiscourseRelation>> sentToRelations = new HashMap<>();
		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
		Map<Token, Collection<Sentence>> tokenToSents = JCasUtil.indexCovering(aJCas, Token.class, Sentence.class);
		for (DiscourseRelation relation: relations){
			for (int i = 0; i < 2; i++){
				Set<Sentence> sents = getSentences(relation.getArguments(i), tokenToSents);
				if (sents.size() == 1){		//ignore relations that has a span larger than a sentence.
					for (Sentence sent: sents){
						Set<DiscourseRelation> sentRelations = sentToRelations.get(sent);
						if (sentRelations == null){
							sentRelations = new HashSet<>();
							sentToRelations.put(sent, sentRelations);
						}

						sentRelations.add(relation);
					}
				}
			}
		}

		return sentToRelations;
	}

	private Set<Sentence> getSentences(DiscourseArgument argument, Map<Token, Collection<Sentence>> tokenToSents) {
		Set<Sentence> selectedSentence = new HashSet<>();
		for (Token token: TokenListTools.convertToTokens(argument)){
			selectedSentence.addAll(tokenToSents.get(token));
		}
		return selectedSentence;
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(NoRelationAnnotator.class);
	}
	
	public static void main(String[] args) throws UIMAException, IOException {
		File dataFld = new File("data/");
		DatasetMode mode = DatasetMode.dev;
		ConllDatasetPath datasetPath = new ConllDatasetPathFactory().makeADataset2016(dataFld, mode);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, datasetPath.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(datasetPath.getParsesJSonFile());
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(datasetPath.getDataJSonFile(), false);
		AnalysisEngineDescription noRelationAnnotator = NoRelationAnnotator.getDescription();
		AnalysisEngineDescription conllGoldJSONExporter = ConllJSonGoldExporter.getDescription(new File("outputs/no-relations-" + mode + ".json"));
		
		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader, noRelationAnnotator, conllGoldJSONExporter);
		System.out.println("NoRelationAnnotator.main()");
	}

}
