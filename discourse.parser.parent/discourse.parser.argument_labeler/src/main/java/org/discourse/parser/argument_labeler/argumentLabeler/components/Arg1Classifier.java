package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class Arg1Classifier implements SequenceClassifierAlgorithmFactory<String, DiscourseConnective, Constituent>{
	DiscourseRelationFactory factory = new DiscourseRelationFactory();
	Map<DiscourseConnective, Sentence> coveringSentences = new HashMap<>();
	Map<Sentence, Collection<Constituent>> sentenceConstituents = new HashMap<>();
	Map<DiscourseArgument, Constituent> argumentCoveringConstituent = new HashMap<>();
	Map<Constituent, Collection<Token>> constituentCoveredTokens = new HashMap<>();
	Map<Constituent, Collection<Constituent>> constituentChilderen = new HashMap<>();
	
	JCas jcas = null;
	
	protected void init(JCas jCas) {
		if (!jCas.equals(jcas)){
			this.jcas = jCas;
			coveringSentences.clear();
			JCasUtil.indexCovering(jCas, DiscourseConnective.class, Sentence.class).forEach(
					(k, v) -> coveringSentences.put(k, v.iterator().next()));

			sentenceConstituents = JCasUtil.indexCovered(jCas, Sentence.class, Constituent.class);

			argumentCoveringConstituent.clear();
			JCasUtil.indexCovering(jCas, DiscourseArgument.class, Constituent.class).forEach(
					(k, v) -> argumentCoveringConstituent.put(k, smallest(v)));
			
			constituentCoveredTokens = JCasUtil.indexCovered(jCas, Constituent.class, Token.class);
			
			constituentChilderen = JCasUtil.indexCovered(jCas, Constituent.class, Constituent.class);

		}
	}
	
	private Constituent smallest(Collection<Constituent> constituents) {
		HashSet<Constituent> children = new HashSet<>(constituents);
		
		for (Constituent constituent: constituents){
			if (constituent.getParent() != null)
				children.remove(constituent.getParent());
		}
		
		if (children.size() != 1)
			throw new RuntimeException("Should not be reached");
		return children.iterator().next();
	}


	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Constituent>> getInstanceExtractor(JCas aJCas) {
		init(aJCas);
		
		return (dc) -> new ArrayList<>(sentenceConstituents.get(coveringSentences.get(dc)));
	}

	@Override
	public BiFunction<List<Constituent>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(
			JCas jCas) {
		init(jCas);
		
		BiFunction<Constituent, DiscourseConnective, Feature> dummyFeature = (cns, dc) -> new Feature("dummy", "" + cns.getBegin() + "-" + cns.getEnd() + "-" + dc.hashCode());
		BiFunction<Constituent, DiscourseConnective, List<Feature>> features = multiBiFuncMap(dummyFeature);
		return mapOneByOneTo(features);
	}

	@Override
	public BiFunction<List<Constituent>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		return this::getLabels;
	}
	


	private List<String> getLabels(List<Constituent> constituents, DiscourseConnective discourseConnective){
		DiscourseArgument arg2 = getArgument(discourseConnective);
		
		Constituent arg2Constituent = argumentCoveringConstituent.get(arg2);
		return constituents.stream()
				.map((cns) -> cns.equals(arg2Constituent) ? Boolean.toString(true) : Boolean.toString(false))
				.collect(Collectors.toList());
	}

	protected DiscourseArgument getArgument(DiscourseConnective discourseConnective) {
		DiscourseArgument arg2 = discourseConnective.getDiscourseRelation().getArguments(1);
		return arg2;
	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, Constituent> getLabeller(JCas jCas) {
		return this::setLabels;
	}

	private void setLabels(List<String> outcomes, DiscourseConnective discourseConnective, List<Constituent> constituents){
		Constituent argConnstituent = null;
		for (int i = 0; i < outcomes.size(); i++){
			if (Boolean.parseBoolean(outcomes.get(i))){
				if (argConnstituent != null){
					System.err.println("Arg1Classifier.setLabels(): TODO");
				}
				argConnstituent = constituents.get(i);
			}
		}
		
		makeARelation(discourseConnective, argConnstituent);
	}

	protected void makeARelation(DiscourseConnective discourseConnective, Constituent argConnstituent) {
		factory.makeAnExplicitRelation(jcas, null, discourseConnective, null, new ArrayList<>(constituentCoveredTokens.get(argConnstituent)));
	}
}
