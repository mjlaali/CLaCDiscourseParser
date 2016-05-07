package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.util.ArrayList;

import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;

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
}
