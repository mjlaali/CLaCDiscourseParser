package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeatures;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.extractFromScope;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.joinInScope;
import static ca.concordia.clac.ml.scop.Scopes.getPathToRoot;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class TreeFeatureExtractorTest {
	private PennTreeToJCasConverter converter;

	private MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null, (String)null);
	private MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null, null, (String)null);

	private String sent;

	private JCas aJCas;

	@Before
	public void setup() throws UIMAException{
		converter = new PennTreeToJCasConverter(
				posMappingProvider, 
				constituentMappingProvider);
		
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
		
		aJCas = JCasFactory.createJCas();
		posMappingProvider.configure(aJCas.getCas());
		constituentMappingProvider.configure(aJCas.getCas());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		sent = PennTreeUtils.toText(parsePennTree);
		
		aJCas.setDocumentText(sent);
		aJCas.setDocumentLanguage("en");
		Sentence aSentence = new Sentence(aJCas, 0, sent.length());
		aSentence.addToIndexes();
		int pos = 0;
		
		int idx = 0;
		List<PennTreeNode> nodes = PennTreeUtils.getPreTerminals(parsePennTree);
		for (String tokenStr: sent.split(" ")){
			Token token = new Token(aJCas, pos, pos + tokenStr.length());
			POS partOfSpeach = new POS(aJCas, token.getBegin(), token.getEnd());
			partOfSpeach.setPosValue(nodes.get(idx++).getLabel());
			token.setPos(partOfSpeach);
			token.addToIndexes();
			pos += tokenStr.length() + 1;
		}
		converter.convertPennTree(aSentence, parsePennTree);

	}
	
	private Constituent findFirstConstituent(String constituentType) {
		Collection<Constituent> constituents = JCasUtil.select(aJCas, Constituent.class);
		for (Constituent constituent: constituents){
			if (constituent.getConstituentType().equals(constituentType))
				return constituent;
		}
		return null;
	}
	
	@Test
	public void givenANodeWithParentWhenCalculatingThePathThenThePathStartWithRootAndFinishWithNode() throws UIMAException{
		String so = "so";
		int soPos = sent.indexOf(so);
		Token soToken = JCasUtil.selectCovered(aJCas, Token.class, soPos, soPos + so.length()).get(0);
		
		Function<Token, List<Feature>> feature = getPathToRoot(Token.class).andThen(extractFromScope(
				getFeatures(joinInScope((Constituent cons) -> cons.getConstituentType(), "full-path"))));
		
		List<Feature> res = feature.apply(soToken);
		assertThat(res).hasSize(1);
		Feature f = res.get(0);
		assertThat(f.getName()).isEqualTo("full-path");
		assertThat(f.getValue()).isEqualTo("ROOT-S-VP-ADVP");
	}

	@Test
	public void givenTheRootWhenCalculatingThePathThenThePathIsEmpty() throws UIMAException{
		ROOT root = JCasUtil.selectByIndex(aJCas, ROOT.class, 0);
		
		assertThat(root.getConstituentType()).isEqualTo("ROOT");
		assertThat(root.getParent()).isNull();
		
		Function<ROOT, List<Feature>> feature = getPathToRoot(ROOT.class).andThen(extractFromScope(
				getFeatures(joinInScope((Constituent cons) -> cons.getConstituentType(), "full-path"))));
		
		List<Feature> res = feature.apply(root);
		assertThat(res).hasSize(1);
		Feature f = res.get(0);
		assertThat(f.getName()).isEqualTo("full-path");
		assertThat(f.getValue()).isEqualTo("");
	}


	@Test
	public void givenADVPNodeWhenCalculatingItsParentThenReturnVP(){
		Constituent advp = findFirstConstituent("ADVP");
		assertThat(advp).isNotNull();
		
		Function<Constituent, String> feature = getParent().andThen(getConstituentType());
		
		String res = feature.apply(advp);
		assertThat(res).isEqualTo("VP");
		
	}
	
	@Test
	public void givenADVPNodeWhenCalculatingItsRightSiblignThenReturnVP(){
		Constituent advp = findFirstConstituent("ADVP");
		
		Function<Constituent, String> feature = getRightSibling().andThen(getConstituentType());
		
		String res = feature.apply(advp);
		assertThat(res).isEqualTo("VP");
	}

	@Test
	public void givenADVPNodeWhenCalculatingItsLeftSiblignThenReturnCC(){
		Constituent advp = findFirstConstituent("ADVP");
		
		Function<Constituent, String> feature = getLeftSibling().andThen(getConstituentType());
		
		String res = feature.apply(advp);
		assertThat(res).isEqualTo("CC");
	}
	
	@Test
	public void givenARootNodeWhenCalculatingItsLeftSiblingThenReturnNull(){
		ROOT root = JCasUtil.selectByIndex(aJCas, ROOT.class, 0);
		Function<Constituent, String> feature = getLeftSibling().andThen(getConstituentType());
		Object res = getFeature("FeatureWithNull", feature).apply(root).getValue();
		assertThat(res).isEqualTo("null");
	}

}
