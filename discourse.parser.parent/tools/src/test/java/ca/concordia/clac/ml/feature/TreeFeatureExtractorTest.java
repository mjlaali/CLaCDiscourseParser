package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getChilderen;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPathFromRoot;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.join;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.joinInScope;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

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
		
		for (String tokenStr: sent.split(" ")){
			Token token = new Token(aJCas, pos, pos + tokenStr.length());
			token.addToIndexes();
			pos += tokenStr.length() + 1;
		}
		converter.setCreatePosTags(true);
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
		
		Function<Token, Feature> feature = getPathFromRoot(Token.class)
					.andThen(mapOneByOneTo((c) -> (Annotation)c))
					.andThen(mapOneByOneTo(getConstituentType()))
					.andThen(join(Collectors.joining("-")))
					.andThen(makeFeature("full-path"));
		
		Feature f = feature.apply(soToken);
		assertThat(f.getName()).isEqualTo("full-path");
		assertThat(f.getValue()).isEqualTo("ROOT-S-VP-ADVP");
	}
	
	@Test
	public void givenTheADVPNodeAndTheNNSNodeWhenCalculatingThePathBetweenThemThenTheResultIsNP_S_null_VP_ADVP(){
		Constituent np = findFirstConstituent("NP");
		Constituent advp = findFirstConstituent("ADVP");
		
		List<Annotation> pathConstituents = getPath().apply(np, advp);
		String path = pathConstituents.stream().map(getConstituentType()).collect(Collectors.joining("-"));
		assertThat(path).isEqualTo("NP-S-null-VP-ADVP");
	}

	@Test
	public void givenTheRootWhenCalculatingThePathThenThePathIsEmpty() throws UIMAException{
		ROOT root = JCasUtil.selectByIndex(aJCas, ROOT.class, 0);
		
		assertThat(root.getConstituentType()).isEqualTo("ROOT");
		assertThat(root.getParent()).isNull();
		
		Function<ROOT, Feature> feature = getPathFromRoot(ROOT.class)
				.andThen(mapOneByOneTo((c) -> (Annotation)c))
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(join(Collectors.joining("-")))
				.andThen(makeFeature("full-path"));
		
		Feature f = feature.apply(root);
		assertThat(f.getName()).isEqualTo("full-path");
		assertThat(f.getValue()).isEqualTo("");
	}


	@Test
	public void givenADVPNodeWhenCalculatingItsParentThenReturnVP(){
		Constituent advp = findFirstConstituent("ADVP");
		assertThat(advp).isNotNull();
		
		Function<Annotation, String> feature = getParent().andThen(getConstituentType());
		
		String res = feature.apply(advp);
		assertThat(res).isEqualTo("VP");
		
	}
	
	@Test
	public void givenADVPNodeWhenCalculatingItsRightSiblignThenReturnVP(){
		Constituent advp = findFirstConstituent("ADVP");
		
		Function<Annotation, String> feature = getRightSibling().andThen(getConstituentType());
		
		String res = feature.apply(advp);
		assertThat(res).isEqualTo("VP");
	}

	@Test
	public void givenADVPNodeWhenCalculatingItsLeftSiblignThenReturnCC(){
		Constituent advp = findFirstConstituent("ADVP");
		
		Function<Annotation, String> feature = getLeftSibling().andThen(getConstituentType());
		
		String res = feature.apply(advp);
		assertThat(res).isEqualTo("CC");
	}
	
	@Test
	public void givenARootNodeWhenCalculatingItsLeftSiblingThenReturnNull(){
		ROOT root = JCasUtil.selectByIndex(aJCas, ROOT.class, 0);
		Function<Annotation, Feature> feature = getLeftSibling().andThen(getConstituentType()).andThen(makeFeature("FeatureWithNull"));
		Object res = feature.apply(root).getValue();
		assertThat(res).isEqualTo("null");
	}

	@Test
	public void givenTheSNodeWhenCalculatingItsChilderenThenCC_NP_VP_DOT(){
		ROOT root = JCasUtil.selectByIndex(aJCas, ROOT.class, 0);
		Annotation s = root.getChildren(0);
		Function<Annotation, Feature> func = getChilderen().andThen(
				joinInScope("childeren", TreeFeatureExtractor.getConstituentType()));
		String value = (String) func.apply(s).getValue();
		
		assertThat(value).isEqualTo("CC-NP-VP-.");
	}
	
	@Test
	public void givenCCAndSWhenCalculatingThePathThenThePathIsEqualToCC_S(){
		Constituent np = findFirstConstituent("NP");
		Constituent s = findFirstConstituent("S");
		
		String path = getPath().apply(np, s).stream().map(getConstituentType()).collect(Collectors.joining("-"));
		assertThat(path).isEqualTo("NP-S-null");
	}
}
