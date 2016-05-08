package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.CompareToBuilder;
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

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;


class SortPermutation {
	public static <T> List<Integer> getPermutations(final List<T> list, final Comparator<? super T> comparator){
		List<Integer> permutation = new ArrayList<>();
		for (int i = 0; i < list.size(); i++)
			permutation.add(i);
		
		Collections.sort(permutation, (a, b) -> comparator.compare(list.get(a), list.get(b)));
		
		return permutation;
	}
}

class HeavyPair<L, R> implements Comparable<HeavyPair<L, R>>{
	public final L left;
	public final R right;
	public final Comparator<? super L> comparator;

	public HeavyPair(L left, R right, Comparator<? super L> comparator) {
		this.left = left;
		this.right = right;
		this.comparator = comparator;
	}

	public static<L, R> List<R> sort(List<L> weights, List<R> toSort, Comparator<? super L> comparator) {
		assert(weights.size() == toSort.size());
		List<R> output = new ArrayList<>(toSort.size());
		List<HeavyPair<L, R>> workHorse = new ArrayList<>(toSort.size());
		for(int i = 0; i < toSort.size(); i++) {
			workHorse.add(new HeavyPair<>(weights.get(i), toSort.get(i), comparator));
		}
		Collections.sort(workHorse);
		for(int i = 0; i < workHorse.size(); i++) {
			output.add(workHorse.get(i).right);
		}
		return output;
	}

	@Override
	public int compareTo(HeavyPair<L, R> o) {
		return this.comparator.compare(this.left, o.left);
	}

}

class SizeComparator implements Comparator<Annotation>{

	@Override
	public int compare(Annotation o1, Annotation o2) {
		return new CompareToBuilder()
				.append(size(o1), size(o2))
				.append(o1.getBegin(), o2.getBegin())
				.append(o1.getClass().getName(), o2.getClass().getName())
				.toComparison();
	}

	private int size(Annotation ann){
		return ann.getEnd() - ann.getBegin();
	}

}


public class ConflictResolver implements SequenceClassifierAlgorithmFactory<String, DiscourseConnective, Constituent>{
	Arg2Classifier arg1Classifier = new Arg2Classifier();
	Map<DiscourseConnective, Set<Token>> argumentTokens = new HashMap<>();
	Map<Annotation, Collection<Token>> coveredTokens = new HashMap<>();

	JCas jcas = null;

	protected void init(JCas jCas) {
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

		coveredTokens.putAll(arg1Classifier.constituentCoveredTokens);
		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		tokens.forEach((t) -> coveredTokens.put(t, Arrays.asList(t)));
	}


	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		init(jCas);
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Constituent>> getInstanceExtractor(JCas aJCas) {
		return this::getSubAnnotations;
	}

	private List<Constituent> getSubAnnotations(DiscourseConnective discourseConnective){
		Set<Constituent> candidates = new HashSet<>();
		DiscourseRelation discourseRelation = discourseConnective.getDiscourseRelation();
		for (int i = 0; i < 2; i++){
			DiscourseArgument arg = discourseRelation.getArguments(i);
			Constituent constituent = arg1Classifier.argumentCoveringConstituent.get(arg);
			Collection<Constituent> constituents = arg1Classifier.constituentChilderen.get(constituent);
			if (i == 0) 
				candidates.addAll(constituents);
			else
				candidates.retainAll(constituents);
		}
		ArrayList<Constituent> results = new ArrayList<>(candidates);

		Collections.sort(results, new SizeComparator());
		return results;
	}

	private String annotationToString(Annotation annotation){
		return "(" + annotation.getBegin() + "-" + annotation.getEnd() + "-" + annotation.getClass().getSimpleName() + ")"; 
	}


	@Override
	public BiFunction<List<Constituent>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		BiFunction<Constituent, DiscourseConnective, Feature> dummyFeature = (cns, dc) -> new Feature("dummy", annotationToString(cns) + annotationToString(dc));
		BiFunction<Constituent, DiscourseConnective, List<Feature>> features = multiBiFuncMap(dummyFeature);
		return mapOneByOneTo(features);
	}

	@Override
	public BiFunction<List<Constituent>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		return this::getLabels;
	}

	private List<String> getLabels(List<Constituent> constituents, DiscourseConnective discourseConnective){
		Set<Token> arg1Tokens = new HashSet<>(TokenListTools.convertToTokens(discourseConnective.getDiscourseRelation().getArguments(0)));
		Set<Token> arg2Tokens = new HashSet<>(TokenListTools.convertToTokens(discourseConnective.getDiscourseRelation().getArguments(1)));

//		List<Integer> permutaiton = SortPermutation.getPermutations(constituents, new SizeComparator());
		
		String[] outcomes = new String[constituents.size()]; 
		Set<Token> ignore = new HashSet<>();
		for (int idx = 0; idx < constituents.size(); idx++){
			HashSet<Token> tokens = new HashSet<>(coveredTokens.get(constituents.get(idx)));
			tokens.removeAll(ignore);
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

		return NodeArgType.None.toString();

	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, Constituent> getLabeller(JCas jCas) {
		return this::setLabels;
	}

	private void setLabels(List<String> outcomes, DiscourseConnective connective, List<Constituent>  constituents){
		Set<Token> arg1Tokens = new HashSet<>();
		Set<Token> arg2Tokens = new HashSet<>();
		Set<Token> noneTokens = new HashSet<>();
		
		if (TokenListTools.getTokenListText(connective.getDiscourseRelation().getArguments(0)).contains("We would have to wait")){
//			System.out.println("ConflictResolver.setLabels()" + outcomes.stream().collect(Collectors.joining(", ")));
//			System.out.println("ConflictResolver.setLabels()" + constituents.stream().map(Constituent::getCoveredText).collect(Collectors.joining(", ")));
			
		}

		constituents = new ArrayList<>(constituents);
		Collections.sort(constituents, new SizeComparator());
		outcomes = HeavyPair.sort(constituents, outcomes, new SizeComparator());

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
//		System.out.println(noneTokens.stream().map(Token::getCoveredText).collect(Collectors.joining(" ")));
		for (int i = 0; i < 2; i++){
//			System.out.println(argsTokens.get(i).stream().map(Token::getCoveredText).collect(Collectors.joining(" ")));
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
