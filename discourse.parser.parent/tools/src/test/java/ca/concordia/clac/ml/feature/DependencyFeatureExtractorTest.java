package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getDependantDependency;
import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getHead;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getTokenList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.uima.test.util.DocumentFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class DependencyFeatureExtractorTest {
	private String sent;

	private JCas aJCas;

	@Before
	public void setup() throws UIMAException{
		String parseTree = "(ROOT (S (CC But) (NP (PRP$ its) (NNS competitors)) (VP (VP (VBP have) "
				+ "(NP (RB much) (JJR broader) (NN business) (NNS interests))) (CC and) (ADVP (RB so)) "
				+ "(VP (VBP are) (ADVP (RBR better)) (VP (VBN cushioned) (PP (IN against) (NP (NN price) (NNS swings))))))(. .)))";
		
		String dependencies = "cc(have-4, But-1) nmod:poss(competitors-3, its-2) nsubj(have-4, competitors-3) root(ROOT-0, have-4) "
				+ "advmod(interests-8, much-5) amod(interests-8, broader-6) compound(interests-8, business-7) dobj(have-4, interests-8) "
				+ "cc(have-4, and-9) dep(and-9, so-10) auxpass(cushioned-13, are-11) advmod(cushioned-13, better-12) conj(have-4, cushioned-13) "
				+ "case(swings-16, against-14) compound(swings-16, price-15) nmod(cushioned-13, swings-16)";

//		(ROOT
//				(S 
//						(CC But)
//						(NP (PRP$ its) (NNS competitors))
//						(VP
//								(VP (VBP have)
//										(NP (RB much) (JJR broader) (NN business) (NNS interests)))
//								(CC and)
//								(ADVP (RB so))
//								(VP (VBP are)
//										(ADVP (RBR better))
//										(VP (VBN cushioned)
//												(PP (IN against)
//														(NP (NN price) (NNS swings))))))))
		DocumentFactory factory = new DocumentFactory();
		aJCas = factory.createADcoument(parseTree, dependencies); 
		
		sent = aJCas.getDocumentText();
		System.out.println(sent);
	}
	
	@Test
	public void whenExtractingTheHeadOfTheSentenceThenTheHeadIsHave(){
		Map<Constituent, Collection<Token>> constituentsToTokens = JCasUtil.indexCovered(aJCas, Constituent.class, Token.class);
		Function<Annotation, Token> headFinder = getHead(getDependantDependency().apply(aJCas), getTokenList(constituentsToTokens, List.class));
		
		Constituent root = TreeFeatureExtractorTest.findFirstConstituent("ROOT", aJCas);
		assertThat(headFinder.apply(root).getCoveredText()).isEqualTo("have");
		
	}
}
