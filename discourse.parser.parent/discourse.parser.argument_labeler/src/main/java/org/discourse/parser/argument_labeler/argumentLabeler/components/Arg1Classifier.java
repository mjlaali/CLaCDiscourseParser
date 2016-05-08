package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class Arg1Classifier extends Arg2Classifier{

	@Override
	protected DiscourseArgument getArgument(DiscourseConnective discourseConnective) {
		return discourseConnective.getDiscourseRelation().getArguments(0);
	}

	@Override
	protected BiFunction<Annotation, DiscourseConnective, List<Feature>> getGeneralFeatures(JCas jCas) {
		BiFunction<Annotation, DiscourseConnective, List<Feature>> arg2ClassifierFeatures = super.getGeneralFeatures(jCas);
		
		BiFunction<Annotation, DiscourseConnective, List<Feature>> arg1ClassifierFeatures = this::getArg1Features;
		
		return multiBiFuncMap(arg2ClassifierFeatures, arg1ClassifierFeatures).andThen(flatMap(Feature.class));
	}
	
	private List<Feature> getArg1Features(Annotation constituent, DiscourseConnective connective){
		Constituent arg2CoveringConstituent = argumentCoveringConstituent.get(connective.getDiscourseRelation().getArguments(1));
		Set<Token> arg2CoveringTokens = new HashSet<>(mapToTokenList.get(arg2CoveringConstituent));
		Set<Token> arg1CoveringTokens = new HashSet<>();
		
		return new ConstituentArg2Arg1FeatureFactory(dependencyGraph, mapToTokenSet)
				.getInstance(arg1CoveringTokens, arg2CoveringTokens).apply(constituent);
	}

	@Override
	protected void makeARelation(DiscourseConnective discourseConnective, Constituent argConnstituent) {
		DiscourseRelation relation = discourseConnective.getDiscourseRelation();
		DiscourseArgument argument = relation.getArguments(0);
		argument.removeFromIndexes();
		
		List<Token> dc = TokenListTools.convertToTokens(discourseConnective);
		List<Token> arg1 = new ArrayList<>(mapToTokenList.get(argConnstituent));
		arg1.removeAll(dc);
		TokenListTools.initTokenList(argument, arg1);
		argument.addToIndexes();
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String modelLocation, 
			String goldView, String systemView) throws ResourceInitializationException{
		return StringSequenceClassifier.getClassifierDescription(goldView, systemView, Boolean.toString(false), 
				Arg1Classifier.class, modelLocation);
	}
	
	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		return StringSequenceClassifier.getWriterDescription(Arg1Classifier.class, MalletCrfStringOutcomeDataWriter.class, outputDirectory);
	}

}
