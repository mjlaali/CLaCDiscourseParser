package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
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
	protected void makeARelation(DiscourseConnective discourseConnective, Constituent argConnstituent) {
		DiscourseRelation relation = discourseConnective.getDiscourseRelation();
		DiscourseArgument argument = relation.getArguments(0);
		List<Token> dc = TokenListTools.convertToTokens(discourseConnective);
		List<Token> arg1 = new ArrayList<>(constituentCoveredTokens.get(argConnstituent));
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
