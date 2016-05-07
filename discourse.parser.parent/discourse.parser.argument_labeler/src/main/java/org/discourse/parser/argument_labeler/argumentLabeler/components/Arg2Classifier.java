package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.io.File;
import java.util.ArrayList;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;

import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class Arg2Classifier extends Arg1Classifier{

	@Override
	protected DiscourseArgument getArgument(DiscourseConnective discourseConnective) {
		return discourseConnective.getDiscourseRelation().getArguments(0);
	}

	@Override
	protected void makeARelation(DiscourseConnective discourseConnective, Constituent argConnstituent) {
		DiscourseArgument argument = new DiscourseArgument(jcas);
		TokenListTools.initTokenList(argument, new ArrayList<>(constituentCoveredTokens.get(argConnstituent)));
		discourseConnective.getDiscourseRelation().setArguments(0, argument);
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
