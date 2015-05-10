package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse_parsing.module.argumentLabeler.KongEtAl2014ArgumentLabeler;
import org.cleartk.discourse_parsing.module.argumentLabeler.NodeArgType;
import org.cleartk.discourse_parsing.module.argumentLabeler.Position;
import org.cleartk.ml.Classifier;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.token.type.Token;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

public class KongEtAl2014ArgumentLabelerTest extends ParseComponentBaseTest<String> {
	public static final String SYSTEM_OUT_FILE = "outputs/test/temp/temp.json";
	public static final String OUTPUT_DIRECTORY = "outputs/test/temp/trainingOutput";
	
	Classifier<String> classifier = new Classifier<String>() {
		String[] featureValue = new String[]{"CC::S;;VP;;RB", "NP::S;;VP;;RB", "S;;VP;;RB", "CC::VP;;RB", "VP;;RB"}; 
		NodeArgType[] classLabel = new NodeArgType[]{NodeArgType.Arg1, NodeArgType.Arg1, NodeArgType.Non, NodeArgType.Arg2, NodeArgType.Non};
		
//		assertThat(getOutcomes(pathToInstance.get("VP::VP;;RB"))).containsOnly(NodeArgType.Arg2.toString(), NodeArgType.Arg1.toString());

		private Map<String, String> featureToLabel;
		
		{
			featureToLabel = new TreeMap<String, String>();
			for (int i= 0; i < featureValue.length; i++){
				featureToLabel.put(featureValue[i], classLabel[i].toString());
			}
		}

		@Override
		public Map<String, Double> score(List<Feature> features)
				throws CleartkProcessingException {
			return null;
		}

		@Override
		public String classify(List<Feature> features)
				throws CleartkProcessingException {
			String label = null;
			String position = null;
			for (Feature feature: features){
				if ("CON-NT-Path".equals(feature.getName())){
					String value = featureToLabel.get(feature.getValue());
					if (value != null)
						label = value;
				} else if ("CON-NT-Position".equals(feature.getName())){
					position = feature.getValue().toString();
				}
			}
			
			if (label == null){
				if (position.equals(Position.Left.toString()))
					label = NodeArgType.Arg1.toString();
				else
					label = NodeArgType.Arg2.toString();
			}
			return label;
		}
	};

	@Before
	public void setUp() throws CleartkProcessingException, AnalysisEngineProcessException, CASException, ResourceInitializationException, UIMAException, JSONException{
		super.setUp();
		String sent = "But its competitors have much broader business interests and so are better cushioned against price swings";
		String words = "But its competitors have much broader business interests and so are better cushioned against price swings";
		String poses = "CC PRP NNS VBP RB JJR NN NNS CC RB VBP RBR VBN IN NN NNS";
		String parseTree = "(S (CC But)(NP (PRP its) (NNS competitors))(VP(VP (VBP have)(NP (RB much) (JJR broader) (NN business) (NNS interests)))(CC and)(RB so)(VP (VBP are)(ADVP (RBR better))(VP (VBN cushioned)(PP (IN against)(NP (NN price) (NNS swings)))))))";
	
		new SyntaxReader().initJCas(aJCas, sent, words, poses, parseTree);
		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
		List<Token> arg2 = new ArrayList<Token>(tokens.subList(8, 9));	//and
		arg2.addAll(tokens.subList(10, 16));
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelation(aJCas, RelationType.Explicit, "Contrast", "so", 
				tokens.subList(9, 10), 
				tokens.subList(0, 8), 
				arg2);
		
		this.arg1 = "But its competitors have much broader business interests";
		this.arg2 = "and are better cushioned against price swings";
		discourseRelation.getDiscourseConnective().addToIndexes();


	}

	@Test
	public void givenTheSentenceInArticleWhenExtractFeatureThenAllFeaturesHaveAName() throws UIMAException, JSONException{
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(KongEtAl2014ArgumentLabeler.class);

		run(true, writerDescription, classifier);
		List<Instance<String>> instances = instanceCaptor.getAllValues();
		for (Instance<String> instance: instances){
			assertThat(instance.getFeatures()).hasSize(5);
			for (Feature feature: instance.getFeatures()){
				assertThat(feature.getName()).isNotNull();
				assertThat(feature.getValue()).isNotNull();
			
			}
		}
		
	}

	@Test
	public void givenTheSentenceInArticleWhenExtractFeatureThenTheirValueIsTheSame() throws UIMAException, JSONException{
		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(KongEtAl2014ArgumentLabeler.class);

		run(true, writerDescription, classifier);

		verify(dataWrite, times(5)).write(instanceCaptor.capture());
		
		List<Instance<String>> systemOutputs = instanceCaptor.getAllValues();

		List<Instance<String>> goldValues = new ArrayList<Instance<String>>();
		
		goldValues.add(new Instance<String>(NodeArgType.Arg1.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "so"), 
						new Feature("CON-NT-Path", "VP::VP;;RB"), new Feature("NT-Ctx", "VP_VP_empty_CC"), 
						new Feature("CON-NT-Position", Position.Left.toString())})));

		goldValues.add(new Instance<String>(NodeArgType.Arg2.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "so"), 
						new Feature("CON-NT-Path", "CC::VP;;RB"), new Feature("NT-Ctx", "CC_VP_VP_RB"), 
						new Feature("CON-NT-Position", Position.Left.toString())})));
		
		goldValues.add(new Instance<String>(NodeArgType.Arg2.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "so"), 
						new Feature("CON-NT-Path", "VP::VP;;RB"), new Feature("NT-Ctx", "VP_VP_RB_empty"), 
						new Feature("CON-NT-Position", Position.Right.toString())})));
		
		goldValues.add(new Instance<String>(NodeArgType.Arg1.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "so"), 
						new Feature("CON-NT-Path", "CC::S;;VP;;RB"), new Feature("NT-Ctx", "CC_S_empty_NP"), 
						new Feature("CON-NT-Position", Position.Left.toString())})));
		
		goldValues.add(new Instance<String>(NodeArgType.Arg1.toString(),
				Arrays.asList(new Feature[]{new Feature("CON-CapitalType", "ALL_LOWERCASE"), new Feature("CON-LStr", "so"), 
						new Feature("CON-NT-Path", "NP::S;;VP;;RB"), new Feature("NT-Ctx", "NP_S_CC_VP"), 
						new Feature("CON-NT-Position", Position.Left.toString())})));
		

		for (int i = 0; i < systemOutputs.size(); i++){
			Instance<String> toTest = systemOutputs.get(i);
			Instance<String> goldValue = goldValues.get(i);
			assertThat(toTest.getOutcome()).isEqualTo(goldValue.getOutcome());
			assertThat(toTest.getFeatures()).containsOnlyElementsOf(goldValue.getFeatures());
		}
	}

	@Test
	public void givenTheSentenceInArticleWhenClassifyThenTheSameDiscourseRelationIsConstructed() throws CleartkProcessingException, AnalysisEngineProcessException, ResourceInitializationException{
		assertThat(JCasUtil.select(aJCas, DiscourseRelation.class)).hasSize(0);

		AnalysisEngineDescription writerDescription = AnalysisEngineFactory.createEngineDescription(KongEtAl2014ArgumentLabeler.class);

		run(false, writerDescription, classifier);

		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
		assertThat(discourseRelations).hasSize(1);

		DiscourseRelation discourseRelation = discourseRelations.iterator().next();
		assertThat(discourseRelation.getDiscourseConnectiveText()).isEqualTo("so");
		assertThat(TokenListTools.getTokenListText(discourseRelation.getDiscourseConnective())).isEqualTo("so");
		assertThat(discourseRelation.getRelationType()).isEqualTo(RelationType.Explicit.toString());
		
		assertThat(discourseRelation.getArguments().size()).isEqualTo(2);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(0))).isEqualTo(arg1);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(1))).isEqualTo(arg2);
		assertThat(TokenListTools.getTokenListText(discourseRelation)).isEqualTo(aJCas.getDocumentText());
	}
}
