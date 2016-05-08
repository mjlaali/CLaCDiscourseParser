package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public abstract class BaseClassifier<OUTCOME, SEQUENCE extends Annotation, INSTANCE> implements SequenceClassifierAlgorithmFactory<OUTCOME, SEQUENCE, INSTANCE>{
	protected DiscourseRelationFactory factory = new DiscourseRelationFactory();
	protected Map<DiscourseConnective, Sentence> coveringSentences = new HashMap<>();
	protected Map<Sentence, Collection<Constituent>> sentenceConstituents = new HashMap<>();
	protected Map<DiscourseArgument, Constituent> argumentCoveringConstituent = new HashMap<>();
	protected Map<Constituent, Collection<Token>> constituentCoveredTokens = new HashMap<>();
	protected Map<Constituent, Collection<Constituent>> constituentChilderen = new HashMap<>();
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
}
