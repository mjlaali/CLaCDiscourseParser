package ca.concordia.clac.discourse.parser.dc.disambiguation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.ml.classifier.GenericClassifierLabeller;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class DiscourseVsNonDiscourseClassifierTest {
	private PennTreeToJCasConverter converter;

	private MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null,
			(String) null);
	private MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null,
			null, (String) null);

	private String sent;

	private JCas aJCas;

	@Before
	public void setup() throws UIMAException {
		converter = new PennTreeToJCasConverter(posMappingProvider, constituentMappingProvider);

		String parseTree = "(ROOT (S (CC But) (NP (PRP$ its) (NNS competitors)) (VP (VP (VBP have) "
				+ "(NP (RB much) (JJR broader) (NN business) (NNS interests))) (CC and) (ADVP (RB so)) "
				+ "(VP (VBP are) (ADVP (RBR better)) (VP (VBN cushioned) (PP (IN against) (NP (NN price) (NNS swings))))))(. .)))";

		/*
		(ROOT
		(S (CC But)
				(NP (PRP$ its) (NNS competitors))
				(VP
						(VP (VBP have)
								(NP (RB much) (JJR broader) (NN business) (NNS interests)))
						(CC and)
						(ADVP (RB so))
						(VP (VBP are)
								(ADVP (RBR better))
								(VP (VBN cushioned)
										(PP (IN against)
												(NP (NN price) (NNS swings))))))))
		*/

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

		for (String tokenStr : sent.split(" ")) {
			Token token = new Token(aJCas, pos, pos + tokenStr.length());
			token.addToIndexes();
			pos += tokenStr.length() + 1;
		}
		converter.setCreatePosTags(true);
		converter.convertPennTree(aSentence, parsePennTree);

	}

	@Test
	public void givenTheTreeWhenCalculatingFeaturesThenAllOfThemCorrect(){
		List<Function<DiscourseConnective, List<Feature>>> featureExtractor = new DiscourseVsNonDiscourseClassifier()
				.getFeatureExtractor();
		
		String so = "so";
		int soPos = sent.indexOf(so);
		DiscourseConnective dc = new DiscourseConnective(aJCas, soPos, soPos + so.length());
		
		Map<DiscourseConnective, List<Feature>> features = GenericClassifierLabeller.calcFeatures(
				Stream.of(dc), featureExtractor);
		
		assertThat(features).containsKey(dc);
		List<Feature> dcFeatures = features.get(dc);
		assertThat(dcFeatures).hasSize(6);
		
		Collections.sort(dcFeatures, (f1, f2) -> f1.getName().compareTo(f2.getName()));
		assertThat(dcFeatures).containsOnly(new Feature("CON-LStr", "so") 
				, new Feature("CON-POS", "true")
				, new Feature("selfCat", "ADVP")
				, new Feature("selfCatLeftSibling", "CC")
				, new Feature("selfCatLeftSibling", "VP")
				, new Feature("selfCatParent", "VP"));
	}
}
