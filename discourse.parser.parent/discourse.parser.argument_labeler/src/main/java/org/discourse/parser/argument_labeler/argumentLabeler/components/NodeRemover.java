package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class NodeRemover implements SequenceClassifierAlgorithmFactory<String, DiscourseConnective, Annotation>{
	Arg1Classifier arg1Classifier = new Arg1Classifier();
	Map<DiscourseConnective, Set<Token>> argumentTokens = new HashMap<>();
	Map<Annotation, Collection<Token>> coveringTokens = new HashMap<>();
	
	JCas jcas = null;
	
	protected void init(JCas jCas) {
		if (!jCas.equals(jcas)){
			this.jcas = jCas;
		
			arg1Classifier.init(jCas);
			argumentTokens.clear();
			Collection<DiscourseConnective> connectives = JCasUtil.select(jcas, DiscourseConnective.class);
			connectives.stream().forEach((dc) -> {
				Set<Token> results = new HashSet<>();
				for (int i = 0; i < 2; i++)
				results.addAll(TokenListTools.convertToTokens(dc.getDiscourseRelation().getArguments(i)));
				argumentTokens.put(dc, results);
				
			});
			
			coveringTokens.putAll(arg1Classifier.constituentCoveredTokens);
			Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
			tokens.forEach((t) -> coveringTokens.put(t, Arrays.asList(t)));
		}
	}


	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Annotation>> getInstanceExtractor(JCas aJCas) {
		
		return this::getSubAnnotations;
	}
	
	private List<Annotation> getSubAnnotations(DiscourseConnective discourseConnective){
		List<Annotation> candidates = new ArrayList<>();
		DiscourseRelation discourseRelation = discourseConnective.getDiscourseRelation();
		for (int i = 0; i < 2; i++){
			DiscourseArgument arg = discourseRelation.getArguments(i);
			Constituent constituent = arg1Classifier.argumentCoveringConstituent.get(arg);
			candidates.addAll(arg1Classifier.constituentCoveredTokens.get(constituent));
			candidates.addAll(arg1Classifier.constituentChilderen.get(constituent));
		}
		return candidates;
	}

	@Override
	public BiFunction<List<Annotation>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		BiFunction<Annotation, DiscourseConnective, Feature> dummyFeature = (cns, dc) -> new Feature("dummy", "" + cns.getBegin() + "-" + cns.getEnd() + "-" + dc.hashCode());
		BiFunction<Annotation, DiscourseConnective, List<Feature>> features = multiBiFuncMap(dummyFeature);
		return mapOneByOneTo(features);
	}

	@Override
	public BiFunction<List<Annotation>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		
		return this::getLabels;
	}
	
	private List<String> getLabels(List<Annotation> annotations, DiscourseConnective discourseConnective){
		return annotations.stream()
			.map((ann) -> coveringTokens.get(ann))
			.map((tokens) -> new HashSet<Token>(tokens))
			.map((tokens) -> tokens.removeAll(argumentTokens.get(discourseConnective)))
			.map((changed) -> Boolean.toString(!changed))
			.collect(Collectors.toList());
	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, Annotation> getLabeller(JCas jCas) {
		return this::setLabels;
	}
	
	private void setLabels(List<String> outcomes, DiscourseConnective connective, List<Annotation>  annotations){
		Set<Token> toRemove = new HashSet<>();
		for (int i = 0; i < outcomes.size(); i++){
			if (Boolean.valueOf(outcomes.get(i))){
				toRemove.addAll(coveringTokens.get(annotations.get(i)));
			}
		}
		
		
		for (int i = 0; i < 2; i++){
			DiscourseArgument arg = connective.getDiscourseRelation().getArguments(i);
			List<Token> tokens = TokenListTools.convertToTokens(arg);
			tokens.removeAll(toRemove);
			TokenListTools.initTokenList(arg, tokens);
		}
	}


}
