package org.discourse.parser.argument_labeler.argumentLabeler.implicit;

import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.discourse.parser.argument_labeler.argumentLabeler.NodeArgType;
import org.discourse.parser.argument_labeler.argumentLabeler.components.AnnotationSizeComparator;
import org.discourse.parser.argument_labeler.argumentLabeler.components.BaseClassifier;
import org.discourse.parser.argument_labeler.argumentLabeler.components.ConstituentArg2Arg1FeatureFactory;
import org.discourse.parser.argument_labeler.argumentLabeler.components.ConstituentFeatureFactory;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ImplicitArgumentLabeler extends BaseClassifier<String, Sentence, Annotation>{
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
		for (Sentence sent: new Sentence[]{sentToPrevSent.get(sentence), sentence}){
			candidates.addAll(sentenceConstituents.get(sent));
			candidates.addAll(mapToTokenList.get(sent));
		}
		Collections.sort(candidates, new AnnotationSizeComparator<>());
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

	Map<Sentence, Set<DiscourseRelation>> sentToRelation;
	@Override
	public BiFunction<List<Annotation>, Sentence, List<String>> getLabelExtractor(JCas jCas) {
		sentToRelation = createSentenceToRelationMap(jCas);
		
		return this::getLabels;
	}
	
	private List<String> getLabels(List<Annotation> annotations, Sentence sentence){
		System.out.println(mapToTokenList.get(ann).size());
		DiscourseRelation relation = getRelation(sentence);
		List<String> results = null;
		if (relation == null){
			String[] noneLabels = new String[annotations.size()];
			Arrays.fill(noneLabels, NodeArgType.None.toString());
			results = Arrays.asList(noneLabels);
		} else {
			results = getLables(annotations, relation);
		}
			
		return results;
	}
	
	private List<String> getLables(List<Annotation> annotations, DiscourseRelation relation) {
		Set<Token> toIgnore = new HashSet<>();
		Set<Token> arg1Tokens = new HashSet<>(TokenListTools.convertToTokens(relation.getArguments(0)));
		Set<Token> arg2Tokens = new HashSet<>(TokenListTools.convertToTokens(relation.getArguments(1)));
		
		List<String> results = new ArrayList<>();
		for (Annotation ann: annotations){
			if (ann.getCoveredText().equals("believe the position we've taken is reasonable"));
				System.out.println("ImplicitArgumentLabeler.getLables()" + this.ann.equals(ann));
				
			Set<Token> annTokens = new HashSet<>(mapToTokenSet.get(ann));
			annTokens.removeAll(toIgnore);
			boolean check = false;
			if (annTokens.size() == 0)
				results.add(results.get(results.size() - 1));
			else if (arg1Tokens.containsAll(annTokens))
				results.add(Boolean.toString(true));
			else if (arg2Tokens.containsAll(annTokens))
				results.add(Boolean.toString(true));
			else {
				results.add(Boolean.toString(false));
				check = true;
			}
			
			toIgnore.addAll(annTokens);
			
			if (check && (annTokens.removeAll(arg1Tokens) || annTokens.removeAll(arg2Tokens))){
				System.err.println("ArgumentLabeler.getLables(): TODO");
			}

		}
		return results;
	}

	private DiscourseRelation getRelation(Sentence sentence){
		Set<DiscourseRelation> commonRelations = new HashSet<>(sentToRelation.get(sentence));
		commonRelations.retainAll(sentToRelation.get(sentToPrevSent.get(sentence)));

		if (commonRelations.size() > 0){
			if (commonRelations.size() != 1)
				System.err.println("ArgumentLabeler.getRelation(): TODO");
			return commonRelations.iterator().next();
		}
		
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
		return this::setLabels;
	}
	
	private void setLabels(List<String> outcomes, Sentence sentence, List<Annotation> annotations){
		Set<Token> arg1Tokens = new HashSet<>();
		Set<Token> arg2Tokens = new HashSet<>();
		Set<Token> noneTokens = new HashSet<>();

		for (int i = 0; i < outcomes.size(); i++){
			Set<Token> constituentTokens = new HashSet<>(mapToTokenSet.get(annotations.get(i)));
			switch (NodeArgType.valueOf(outcomes.get(i))) {
			case Arg1:
				constituentTokens.removeAll(arg2Tokens);
				constituentTokens.removeAll(noneTokens);
				arg1Tokens.addAll(constituentTokens);
				break;
			case Arg2:
				constituentTokens.removeAll(arg1Tokens);
				constituentTokens.removeAll(noneTokens);
				arg2Tokens.addAll(constituentTokens);
				break;

			case None:
				constituentTokens.removeAll(arg1Tokens);
				constituentTokens.removeAll(arg2Tokens);
				noneTokens.addAll(constituentTokens);
				break;
			default:
				System.err.println("NodeJudge.setLabels(): TODO should not be reached");
				break;
			}
		}
		
		List<Token> sortedArg1Tokens = new ArrayList<>(arg1Tokens);
		Collections.sort(sortedArg1Tokens, new AnnotationSizeComparator<>());
		
		List<Token> sortedArg2Tokens = new ArrayList<>(arg2Tokens);
		Collections.sort(sortedArg1Tokens, new AnnotationSizeComparator<>());
		
		factory.makeAnImplicitRelation(jcas, "", sortedArg1Tokens, sortedArg2Tokens).addToIndexesRecursively();
	}

}
