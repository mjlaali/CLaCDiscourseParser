package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.dependencyToString;
import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getDependencyGraph;
import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getTokenList;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.pickLeftMostToken;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.uima.test.util.DocumentFactory;
import ca.concordia.clac.util.graph.LabeledEdge;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

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

		//But its competitors have much broader business interests and so are better cushioned against price swings .
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
	}
	
	@Test
	public void whenExtractingTheHeadOfTheSentenceThenTheHeadIsHave(){
		Map<Constituent, Collection<Token>> constituentsToTokens = JCasUtil.indexCovered(aJCas, Constituent.class, Token.class);
//		Function<Annotation, Token> headFinder = getHeadBasedDependencyMap(getDependantDependencies(aJCas), getTokenList(constituentsToTokens, List.class));
		Function<Annotation, Token> headFinder = getTokenList(constituentsToTokens, List.class).andThen(getRoots(getDependencyGraph(aJCas))).andThen(pickLeftMostToken()); 
		
		Constituent root = TreeFeatureExtractorTest.findFirstConstituent("ROOT", aJCas);
		assertThat(headFinder.apply(root).getCoveredText()).isEqualTo("have");
		
	}
	
	private Token getToken(String text){
		int start = sent.indexOf(text);
		List<Token> selectedTokens = JCasUtil.selectCovered(aJCas, Token.class, start, start + text.length());
		if (selectedTokens.size() != 1)
			throw new RuntimeException("" + selectedTokens.size() + " has been found");
		
		return selectedTokens.get(0);
	}
	
	@Test
	public void givenTheSourceAndTheTargetOfThePathWhenCaclulatingThePathThenThePathIsCorrect(){
		Token source = getToken(" its ");
		Token target = getToken(" cushioned ");
		 
		String expectedPath = "nmod:poss(competitors-its)-nsubj(have-competitors)-conj(have-cushioned)";
		
		DirectedGraph<Token, LabeledEdge<Dependency>> graph = getDependencyGraph(aJCas);
		List<LabeledEdge<Dependency>> dependencies = DijkstraShortestPath.findPathBetween(new AsUndirectedGraph<>(graph), source, target);
		String actualPath = dependencies.stream().map(LabeledEdge::getLabel).map(dependencyToString()).collect(Collectors.joining("-"));
		
		assertThat(actualPath).isEqualTo(expectedPath);
		
	}
	
	@Test
	public void givenATextWhenCalculatingTheHeadItReturnsTheHead(){
		String aText = "But its competitors have much broader business interests";
		
		List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, 0, aText.length());
		
		DirectedGraph<Token, LabeledEdge<Dependency>> graph = getDependencyGraph(aJCas);
		
		Token head = getRoots(graph).andThen(pickLeftMostToken()).apply(tokens);
		assertThat(head.getCoveredText()).isEqualTo("have");
	}
	
	@Test
	public void givenASentenceWhenCalculatingTheHeadItReturnsTheHead(){
		
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		
		Token head = getRoots(getDependencyGraph(aJCas)).andThen(pickLeftMostToken()).apply(tokens);
		assertThat(head.getCoveredText()).isEqualTo("have");
	}
	
}
