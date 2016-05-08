package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getDependencyGraph;
import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;

import java.util.ArrayList;
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
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;
import org.jgrapht.DirectedGraph;

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.util.graph.LabeledEdge;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public abstract class BaseClassifier<OUTCOME, SEQUENCE extends Annotation, INSTANCE> implements SequenceClassifierAlgorithmFactory<OUTCOME, SEQUENCE, INSTANCE>{
	protected DiscourseRelationFactory factory = new DiscourseRelationFactory();
	protected Map<DiscourseConnective, Sentence> connectiveCoveringSentence = new HashMap<>();
	protected Map<Sentence, Collection<Constituent>> sentenceConstituents = new HashMap<>();
	protected Map<DiscourseArgument, Constituent> argumentCoveringConstituent = new HashMap<>();
	protected Map<Constituent, Collection<Constituent>> constituentChilderen = new HashMap<>();

	protected Map<Annotation, Set<Token>> mapToTokenSet = new HashMap<>(); 
	protected Map<Annotation, List<Token>> mapToTokenList = new HashMap<>();
	
	protected DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph;

	protected JCas jcas = null;
	
	boolean initialize = true;
	
	private void setup(JCas jCas){
		if (isInitialize(jCas)){
			setInitialize(false, jCas);
			init(jCas);
		}
	}
	
	protected boolean isInitialize(JCas jCas) {
		return initialize;
	}
	
	protected void setInitialize(boolean initialize, JCas jCas) {
		this.initialize = initialize;
	}
	
	protected void init(JCas jCas){
		this.jcas = jCas;
		connectiveCoveringSentence.clear();
		JCasUtil.indexCovering(jCas, DiscourseConnective.class, Sentence.class).forEach(
				(k, v) -> connectiveCoveringSentence.put(k, v.iterator().next()));

		sentenceConstituents = JCasUtil.indexCovered(jCas, Sentence.class, Constituent.class);

		argumentCoveringConstituent.clear();
		JCasUtil.indexCovering(jCas, DiscourseArgument.class, Constituent.class).forEach(
				(k, v) -> argumentCoveringConstituent.put(k, smallest(v)));

		mapToTokenList.clear();
		mapToTokenSet.clear();
		JCasUtil.indexCovered(jCas, Constituent.class, Token.class).forEach(
				(cns, tokens) -> {
					mapToTokenList.put(cns, new ArrayList<>(tokens));
					mapToTokenSet.put(cns, new HashSet<>(tokens));
				});
		
		for (Token token: JCasUtil.select(jCas, Token.class)){
			mapToTokenList.put(token, Collections.singletonList(token));
			mapToTokenSet.put(token, new HashSet<Token>(Collections.singletonList(token)));
		}

		dependencyGraph = getDependencyGraph(jCas);
		
		constituentChilderen = JCasUtil.indexCovered(jCas, Constituent.class, Constituent.class);
	}
	
	@Override
	public Function<JCas, ? extends Collection<? extends SEQUENCE>> getSequenceExtractor(JCas jCas) {
		setInitialize(true, jCas);
		setup(jCas);
		return null;
	}
	
	public static Constituent smallest(Collection<Constituent> constituents) {
		HashSet<Constituent> children = new HashSet<>(constituents);

		for (Constituent constituent: constituents){
			if (constituent.getParent() != null)
				children.remove(constituent.getParent());
		}

		if (children.size() != 1)
			throw new RuntimeException("Should not be reached");
		return children.iterator().next();
	}
	
	protected BiFunction<Annotation, DiscourseConnective, List<Feature>> getGeneralFeatures(JCas jCas) {
		BiFunction<Annotation, DiscourseConnective, List<Feature>> connectiveFeatures = 
				(cns, dc) -> new ConnectiveFeatureFactory().getInstance().apply(dc);
		
		BiFunction<Annotation, DiscourseConnective, List<Feature>> constituentFeatures = 
				(cns, dc) -> new ConstituentFeatureFactory(mapToTokenSet, dependencyGraph, mapToTokenList)
					.getInstance().apply(cns);
				
		BiFunction<Annotation, DiscourseConnective, List<Feature>> constituentConnectiveFeatuers = new ConstituentConnectiveFeatureFactory().getInstance();
		
		BiFunction<Annotation, DiscourseConnective, List<Feature>> features = 
				multiBiFuncMap(connectiveFeatures, constituentFeatures, constituentConnectiveFeatuers)
				.andThen(flatMap(Feature.class));
		return features;
	}

}
