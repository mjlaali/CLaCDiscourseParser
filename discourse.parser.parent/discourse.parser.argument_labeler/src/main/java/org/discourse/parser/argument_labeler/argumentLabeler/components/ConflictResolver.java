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
import org.discourse.parser.argument_labeler.argumentLabeler.NodeArgType;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;


public class ConflictResolver extends BaseClassifier<String, DiscourseConnective, Annotation>{
	Map<DiscourseConnective, Set<Token>> argumentTokens = new HashMap<>();
	Map<Annotation, Collection<Token>> coveredTokens = new HashMap<>();

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

		coveredTokens.putAll(constituentCoveredTokens);
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		tokens.forEach((t) -> coveredTokens.put(t, Arrays.asList(t)));
	}


	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		init(jCas);
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Annotation>> getInstanceExtractor(JCas aJCas) {
		return this::getSubAnnotations;
	}

	private List<Annotation> getSubAnnotations(DiscourseConnective discourseConnective){
		Set<Annotation> candidates = new HashSet<>();
		DiscourseRelation discourseRelation = discourseConnective.getDiscourseRelation();
		for (int i = 0; i < 2; i++){
			DiscourseArgument arg = discourseRelation.getArguments(i);
			Constituent constituent = argumentCoveringConstituent.get(arg);
			Set<Annotation> annotaionSet = new HashSet<>();
			annotaionSet.addAll(constituentCoveredTokens.get(constituent));
			annotaionSet.addAll(constituentChilderen.get(constituent));
			
//			if (i == 0) {
				candidates.addAll(annotaionSet);
//			} else
//				candidates.retainAll(annotaionSet);
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
		BiFunction<Annotation, DiscourseConnective, Feature> dummyFeature = (cns, dc) -> new Feature("dummy", annotationToString(cns) + annotationToString(dc));
		BiFunction<Annotation, DiscourseConnective, List<Feature>> features = multiBiFuncMap(dummyFeature);
		return mapOneByOneTo(features);
	}

	@Override
	public BiFunction<List<Annotation>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		return this::getLabels;
	}

	private List<String> getLabels(List<Annotation> constituents, DiscourseConnective discourseConnective){
		Set<Token> arg1Tokens = new HashSet<>(TokenListTools.convertToTokens(discourseConnective.getDiscourseRelation().getArguments(0)));
		Set<Token> arg2Tokens = new HashSet<>(TokenListTools.convertToTokens(discourseConnective.getDiscourseRelation().getArguments(1)));

		String[] outcomes = new String[constituents.size()]; 
		Set<Token> ignore = new HashSet<>();
		for (int idx = 0; idx < constituents.size(); idx++){
			HashSet<Token> tokens = new HashSet<>(coveredTokens.get(constituents.get(idx)));
			tokens.removeAll(ignore);
			if (tokens.size() == 0)
				outcomes[idx] = outcomes[idx - 1];
			else
				outcomes[idx] = decideLabel(tokens, arg1Tokens, arg2Tokens);
			ignore.addAll(tokens);
		}

		return Arrays.asList(outcomes);
	}

	private String decideLabel(Set<Token> tokens, Set<Token> arg1Tokens, Set<Token> arg2Tokens){
		if (arg1Tokens.containsAll(tokens))
			return NodeArgType.Arg1.toString();
		if (arg2Tokens.containsAll(tokens))
			return NodeArgType.Arg2.toString();

		if (tokens.removeAll(arg1Tokens) || tokens.removeAll(arg2Tokens)){
			System.err.println("ConflictResolver.decideLabel(): TODO");
		}
		return NodeArgType.None.toString();

	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, Annotation> getLabeller(JCas jCas) {
		return this::setLabels;
	}

	@SuppressWarnings("unused")
	private void setLabelsBaseRules(List<String> outcomes, DiscourseConnective connective, List<Annotation>  constituents){
		DiscourseRelation relation = connective.getDiscourseRelation();
		DiscourseArgument arg1 = relation.getArguments(0);
		DiscourseArgument arg2 = relation.getArguments(0);
		
		List<Token> arg1Tokens = TokenListTools.convertToTokens(arg1);
		arg1Tokens.removeAll(TokenListTools.convertToTokens(arg2));
		TokenListTools.initTokenList(arg1, arg1Tokens, false);
	}
	
	
//	@SuppressWarnings("unused")
	private void setLabels(List<String> outcomes, DiscourseConnective connective, List<Annotation>  constituents){
		Set<Token> arg1Tokens = new HashSet<>();
		Set<Token> arg2Tokens = new HashSet<>();
		Set<Token> noneTokens = new HashSet<>();

		for (int i = 0; i < outcomes.size(); i++){
			Set<Token> constituentTokens = new HashSet<>(coveredTokens.get(constituents.get(i)));
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
		List<Set<Token>> argsTokens = Arrays.asList(arg1Tokens, arg2Tokens);
		for (int i = 0; i < 2; i++){
			DiscourseArgument arg = connective.getDiscourseRelation().getArguments(i);
			List<Token> updatedArgTokens = TokenListTools.convertToTokens(arg);
			updatedArgTokens.removeAll(noneTokens);
			updatedArgTokens.removeAll(argsTokens.get(1 - i));
			TokenListTools.initTokenList(arg, updatedArgTokens, false);	//do not update index
		}
	}

	public static AnalysisEngineDescription getClassifierDescription(String modelLocation, 
			String goldView, String systemView) throws ResourceInitializationException{
		return StringSequenceClassifier.getClassifierDescription(goldView, systemView, NodeArgType.Arg2.toString(), 
				ConflictResolver.class, modelLocation);
	}

	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		return StringSequenceClassifier.getWriterDescription(ConflictResolver.class, MalletCrfStringOutcomeDataWriter.class, outputDirectory);
	}
}
