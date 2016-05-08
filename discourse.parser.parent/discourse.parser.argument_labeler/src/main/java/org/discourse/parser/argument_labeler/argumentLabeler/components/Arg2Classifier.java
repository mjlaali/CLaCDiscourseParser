package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class Arg2Classifier extends BaseClassifier<String, DiscourseConnective, Constituent>{

	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		super.getSequenceExtractor(jCas);
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<Constituent>> getInstanceExtractor(JCas aJCas) {
		return this::getArgsConstituents;
	}

	private List<Constituent> getArgsConstituents(DiscourseConnective discourseConnective){
		Sentence coveringSentence = connectiveCoveringSentence.get(discourseConnective);
		if (coveringSentence == null){
			System.err.println("Arg2Classifier.getArgsConstituents(): TODO ");
			return Collections.emptyList();
		}
		List<Sentence> prevSents = JCasUtil.selectPreceding(Sentence.class, coveringSentence, 1);

		List<Constituent> results = new ArrayList<>();
		if (prevSents.size() == 1){
			results.addAll(sentenceConstituents.get(prevSents.get(0)));
		}
		results.addAll(sentenceConstituents.get(coveringSentence));

		return results;
	}

	@Override
	public BiFunction<List<Constituent>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(
			JCas jCas) {
		BiFunction<Constituent, DiscourseConnective, List<Feature>> features = (cns, dc) 
				-> getGeneralFeatures(jCas).apply(cns, dc);

		return mapOneByOneTo(features);
	}

	@Override
	public BiFunction<List<Constituent>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		return this::getLabels;
	}

	private List<String> getLabels(List<Constituent> constituents, DiscourseConnective discourseConnective){
		DiscourseArgument arg2 = getArgument(discourseConnective);

		Constituent arg2Constituent = argumentCoveringConstituent.get(arg2);
		int index = constituents.indexOf(arg2Constituent);
		if (index == -1 && arg2Constituent != null){
			Sentence dcSent = connectiveCoveringSentence.get(discourseConnective);
			Sentence constituentSent = JCasUtil.selectCovering(Sentence.class, arg2Constituent).iterator().next();
			int begin = Math.min(dcSent.getEnd(), constituentSent.getEnd());
			int end = Math.max(dcSent.getBegin(), constituentSent.getBegin());

			int distance = JCasUtil.selectCovered(jcas, Sentence.class, begin, end).size();
			if (distance == 0)
				System.err.println("Arg2Classifier.getLabels(): TODO for " + this.getClass().getSimpleName());
			else
				System.err.println("Arg2Classifier.getLabels(): Arg1 is " + distance + " far from the sentence of Arg2.");
		}


		List<String> results = constituents.stream()
				.map((cns) -> cns.equals(arg2Constituent) ? Boolean.toString(true) : Boolean.toString(false))
				.collect(Collectors.toList());

		long cnt = results.stream().filter(Boolean::valueOf).count();
		if (cnt > 1){
			System.err.println("Arg2Classifier.getLabels(): Error for " + this.getClass().getSimpleName());
		}

		return results;
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
					if (this.getClass().equals(Arg2Classifier.class))
						System.out.println("Arg2Classifier.setLabels()");
					System.err.println("Arg2Classifier.setLabels(): TODO for " + this.getClass().getSimpleName());
				}
				argConnstituent = constituents.get(i);
			}
		}

		makeARelation(discourseConnective, argConnstituent);
	}

	protected void makeARelation(DiscourseConnective discourseConnective, Constituent argConnstituent) {
		ArrayList<Token> arg2 = new ArrayList<>(mapToTokenList.get(argConnstituent));
		arg2.removeAll(TokenListTools.convertToTokens(discourseConnective));
		DiscourseRelation relation = factory.makeAnExplicitRelation(jcas, discourseConnective.getSense(), discourseConnective, 
				Collections.emptyList(), arg2);
		relation.addToIndexes();
		relation.getArguments(1).addToIndexes();

		discourseConnective.setDiscourseRelation(relation);
	}

	public static AnalysisEngineDescription getClassifierDescription(String modelLocation, 
			String goldView, String systemView) throws ResourceInitializationException{
		return StringSequenceClassifier.getClassifierDescription(goldView, systemView, Boolean.toString(false), 
				Arg2Classifier.class, modelLocation);
	}

	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		return StringSequenceClassifier.getWriterDescription(Arg2Classifier.class, MalletCrfStringOutcomeDataWriter.class, outputDirectory);
	}
}
