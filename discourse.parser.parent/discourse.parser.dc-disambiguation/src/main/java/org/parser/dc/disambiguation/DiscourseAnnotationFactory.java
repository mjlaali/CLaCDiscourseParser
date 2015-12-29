package org.parser.dc.disambiguation;

import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;

import ca.concordia.clac.uima.engines.AnnotationFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DiscourseAnnotationFactory implements AnnotationFactory<DiscourseConnective> {

	@Override
	public DiscourseConnective buildAnnotation(JCas aJCas, int start, int end) {
		DiscourseConnective discourseConnective = new DiscourseConnective(aJCas);
		List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, start, end);
		TokenListTools.initTokenList(aJCas, discourseConnective, tokens);
		return discourseConnective;
	}

}
