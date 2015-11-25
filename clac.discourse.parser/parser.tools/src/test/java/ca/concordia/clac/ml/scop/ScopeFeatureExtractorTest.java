package ca.concordia.clac.ml.scop;

import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.extractFromScope;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.joinInScope;
import static ca.concordia.clac.ml.scop.Scopes.getPathToRoot;

import java.util.List;
import java.util.function.Function;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class ScopeFeatureExtractorTest {
	private PennTreeToJCasConverter converter;

	private MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null, (String)null);
	private MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null, null, (String)null);

	@Before
	public void setup(){
		converter = new PennTreeToJCasConverter(
				posMappingProvider, 
				constituentMappingProvider);
	}
	
	@Test
	public void givenANodeInParseTreeWhenJoiningTheConstituentLableThenItCreateThePath() throws UIMAException{
		String parseTree = "(ROOT (S (CC But) (NP (PRP$ its) (NNS competitors)) (VP (VP (VBP have) "
				+ "(NP (RB much) (JJR broader) (NN business) (NNS interests))) (CC and) (ADVP (RB so)) "
				+ "(VP (VBP are) (ADVP (RBR better)) (VP (VBN cushioned) (PP (IN against) (NP (NN price) (NNS swings))))))(. .)))";

//		(ROOT
//				(S (CC But)
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
		
		JCas aJCas = JCasFactory.createJCas();
		posMappingProvider.configure(aJCas.getCas());
		constituentMappingProvider.configure(aJCas.getCas());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		String sent = PennTreeUtils.toText(parsePennTree);
		
		aJCas.setDocumentText(sent);
		aJCas.setDocumentLanguage("en");
		Sentence aSentence = new Sentence(aJCas, 0, sent.length());
		aSentence.addToIndexes();
		int pos = 0;
		for (String tokenStr: sent.split(" ")){
			new Token(aJCas, pos, pos + tokenStr.length()).addToIndexes();
			pos += tokenStr.length() + 1;
		}
		converter.convertPennTree(aSentence, parsePennTree);
		
		String so = "so";
		int soPos = sent.indexOf(so);
		Token soToken = JCasUtil.selectCovered(aJCas, Token.class, soPos, soPos + so.length()).get(0);
		
		Function<Token, List<Feature>> feature = getPathToRoot(Token.class).andThen(extractFromScope(
				joinInScope((Constituent cons) -> cons.getConstituentType(), "full-path")));
		
		List<Feature> res = feature.apply(soToken);
		System.out.println(res);
	}
}
