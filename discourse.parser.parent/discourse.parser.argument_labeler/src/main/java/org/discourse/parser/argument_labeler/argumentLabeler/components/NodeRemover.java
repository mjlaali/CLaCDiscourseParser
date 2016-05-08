package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.io.File;
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
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class NodeRemover extends BaseClassifier<String, DiscourseConnective, Annotation>{
	Map<DiscourseConnective, Set<Token>> argumentTokens = new HashMap<>();
	Map<Annotation, Collection<Token>> coveringTokens = new HashMap<>();

	@Override
	protected void init(JCas jCas) {
		super.init(jCas);

		argumentTokens.clear();
		Collection<DiscourseConnective> connectives = JCasUtil.select(jcas, DiscourseConnective.class);
		connectives.stream().forEach((dc) -> {
			Set<Token> results = new HashSet<>();
			for (int i = 0; i < 2; i++)
				results.addAll(TokenListTools.convertToTokens(dc.getDiscourseRelation().getArguments(i)));
			argumentTokens.put(dc, results);

		});

		coveringTokens.putAll(mapToTokenList);
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		tokens.forEach((t) -> coveringTokens.put(t, Arrays.asList(t)));
	}


	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		super.getSequenceExtractor(jCas);
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Annotation>> getInstanceExtractor(JCas aJCas) {
		init(aJCas);
		return this::getSubAnnotations;
	}

	private List<Annotation> getSubAnnotations(DiscourseConnective discourseConnective){
		Set<Annotation> candidates = new HashSet<>();
		DiscourseRelation discourseRelation = discourseConnective.getDiscourseRelation();
		for (int i = 0; i < 2; i++){
			DiscourseArgument arg = discourseRelation.getArguments(i);
			Constituent constituent = argumentCoveringConstituent.get(arg);
			candidates.addAll(mapToTokenList.get(constituent));
			candidates.addAll(constituentChilderen.get(constituent));
		}
		ArrayList<Annotation> results = new ArrayList<>(candidates);

		Collections.sort(results, new AnnotationSizeComparator<>());
		return results;
	}

	private String annotationToString(Annotation annotation){
		return "(" + annotation.getBegin() + "-" + annotation.getEnd() + "-" + annotation.getClass().getSimpleName() + ")"; 
	}


	@Override
	public BiFunction<List<Annotation>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		init(jCas);
		BiFunction<Annotation, DiscourseConnective, Feature> dummyFeature = (cns, dc) -> new Feature("dummy", annotationToString(cns) + annotationToString(dc));
		BiFunction<Annotation, DiscourseConnective, List<Feature>> features = multiBiFuncMap(dummyFeature);
		return mapOneByOneTo(features);
	}

	@Override
	public BiFunction<List<Annotation>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		init(jCas);
		return this::getLabels;
	}

	private List<String> getLabels(List<Annotation> annotations, DiscourseConnective discourseConnective){
		List<String> outcomes = annotations.stream()
				.map((ann) -> coveringTokens.get(ann))
				.map((tokens) -> new HashSet<Token>(tokens))
				.map((tokens) -> tokens.removeAll(argumentTokens.get(discourseConnective)))
				.map((changed) -> Boolean.toString(!changed))
				.collect(Collectors.toList());

		return outcomes;
	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, Annotation> getLabeller(JCas jCas) {
		init(jCas);
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
			if (i == 0)
				tokens.removeAll(TokenListTools.convertToTokens(connective.getDiscourseRelation().getArguments(1)));
			tokens.removeAll(TokenListTools.convertToTokens(connective));
			TokenListTools.initTokenList(arg, tokens, false); //if we set the offsets then index will be incorrect
		}
	}

	public static AnalysisEngineDescription getClassifierDescription(String modelLocation, 
			String goldView, String systemView) throws ResourceInitializationException{
		return StringSequenceClassifier.getClassifierDescription(goldView, systemView, Boolean.toString(false), 
				NodeRemover.class, modelLocation);
	}

	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		return StringSequenceClassifier.getWriterDescription(NodeRemover.class, MalletCrfStringOutcomeDataWriter.class, outputDirectory);
	}


}
