package org.discourse.parser.argument_labeler.argumentLabeler.implicit;

import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.discourse.parser.argument_labeler.argumentLabeler.components.BaseClassifier;
import org.discourse.parser.argument_labeler.argumentLabeler.components.ConstituentArg2Arg1FeatureFactory;
import org.discourse.parser.argument_labeler.argumentLabeler.components.ConstituentFeatureFactory;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArgumentLabeler extends BaseClassifier<String, Sentence, Annotation>{
	private Map<Sentence, Sentence> sentToPrevSent = new HashMap<>();


	@Override
	protected void init(JCas jCas) {
		super.init(jCas);
		
		sentToPrevSent.clear();
		Sentence prevSent = null;
		for (Sentence sent: JCasUtil.select(jCas, Sentence.class)){
			if (prevSent == null)
				prevSent = sent;
			else 
				sentToPrevSent.put(sent, prevSent);
		}
		
		Map<Sentence, Collection<Token>> sentenceTokens = JCasUtil.indexCovered(jCas, Sentence.class, Token.class);
		sentenceTokens.forEach((k, v) -> {
			mapToTokenList.put(k, new ArrayList<>(v));
			mapToTokenSet.put(k, new HashSet<>(v));
		});
	}
	
	@Override
	public Function<JCas, ? extends Collection<? extends Sentence>> getSequenceExtractor(JCas jCas) {
		super.getSequenceExtractor(jCas);
		return (aJCas) -> sentToPrevSent.keySet();
	}
	
	@Override
	public Function<Sentence, List<Annotation>> getInstanceExtractor(JCas aJCas) {
		return this::getCandidates;
	}
	
	private List<Annotation> getCandidates(Sentence sentence){
		List<Annotation> candidates = new ArrayList<>();
		candidates.addAll(sentenceConstituents.get(sentToPrevSent.get(sentence)));
		candidates.addAll(sentenceConstituents.get(sentence));
		return candidates;
	}

	@Override
	public BiFunction<List<Annotation>, Sentence, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		BiFunction<Annotation, Sentence, List<Feature>> constituentFeatures = (ann, snt) -> 
				new ConstituentFeatureFactory(mapToTokenSet, dependencyGraph, mapToTokenList)
					.getInstance().apply(ann);

	    BiFunction<Annotation, Sentence, List<Feature>> newFeatures = this::getNewFeatures;		
				
		BiFunction<Annotation, Sentence, List<Feature>> features = 
				multiBiFuncMap(constituentFeatures, newFeatures)
				.andThen(flatMap(Feature.class));
		return mapOneByOneTo(features);
	}
	
	private List<Feature> getNewFeatures(Annotation constituent, Sentence arg2){
		Set<Token> arg1CoveringTokens = null;
		arg1CoveringTokens = new HashSet<>(mapToTokenList.get(sentToPrevSent.get(arg2)));
			
		Set<Token> arg2CoveringTokens = null;
		arg2CoveringTokens = new HashSet<>(mapToTokenList.get(arg2));

		return new ConstituentArg2Arg1FeatureFactory(dependencyGraph, mapToTokenSet)
				.getInstance(arg1CoveringTokens, arg2CoveringTokens).apply(constituent);
	}

	@Override
	public BiFunction<List<Annotation>, Sentence, List<String>> getLabelExtractor(JCas jCas) {
		return null;
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

	@Override
	public SequenceClassifierConsumer<String, Sentence, Annotation> getLabeller(JCas jCas) {
		// TODO Auto-generated method stub
		return null;
	}

}
