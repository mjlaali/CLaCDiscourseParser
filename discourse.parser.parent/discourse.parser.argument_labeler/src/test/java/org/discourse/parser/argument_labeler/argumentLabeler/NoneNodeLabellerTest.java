package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class NoneNodeLabellerTest{
	private NoneNodeLabeller algorithmFactory = new NoneNodeLabeller();
	
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private DiscourseRelationExample example = new NoneNodeExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
	}

	
	@Test
	public void whenExtractingSequencesThenOnlyOneSequenceIsExtracted(){
		discourseRelation.addToIndexes();
		Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends ArgumentTreeNode> sequences = sequenceExtractor.apply(aJCas);
//		assertThat(sequences).hasSize(1);
//		ArgumentTreeNode dc = sequences.iterator().next();
//		assertThat(dc).isEqualTo(discourseRelation);
	}
	
	@Test
	public void givenKongExampleWhenArgumentInstancesAreExtractedThreAreFiveArgumentInstances(){
		addToIndex();
		
		Map<DiscourseArgument, Collection<Constituent>> indexCovered = JCasUtil.indexCovered(aJCas, DiscourseArgument.class, Constituent.class);
		
//		System.out.println(indexCovered.get(discourseRelation.getArguments(0)).size());
//		System.out.println(indexCovered.get(discourseRelation.getArguments(1)).size());
		
		Function<ArgumentTreeNode, List<Annotation>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
//		List<Annotation> instances = instanceExtractor.apply(discourseRelation);
//		
//		System.out.println(instances.stream().map(getConstituentType()).collect(Collectors.toList()));
//		assertThat(instances).hasSize(27);
//		List<String> nodes = instances.stream()
//				.map(DCTreeNodeArgInstance::getNode)
//				.map(getConstituentType()).collect(Collectors.toList());
//		assertThat(nodes).containsExactly("CC", "NP", "VP", "CC", "RB", "VP");
	}

	private void addToIndex() {
		discourseRelation.addToIndexes();
		discourseRelation.getArguments(0).addToIndexes();
		discourseRelation.getArguments(1).addToIndexes();
		discourseRelation.getDiscourseConnective().addToIndexes();
	}
	
//	@Test
//	public void givenKongExampleWhenLabelingInstancesThenTheyAre1112dc2(){
//		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
//		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
//		
//		BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<String>> labelExtractor = 
//				algorithmFactory.getLabelExtractor(aJCas);
//		List<String> labels = labelExtractor.apply(instances, discourseRelation.getDiscourseConnective());
//		
//		assertThat(labels).containsExactly("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
//	}
//	
//	@Test
//	public void givenKongExampleWhenExtractingFeaturesThenTheyAreCorrect(){
//		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
//		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
//
//		BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
//		List<List<Feature>> features = featureExtractor.apply(instances, discourseRelation.getDiscourseConnective());
//		
//		List<String> strFeatures = features.stream()
//			.map((fs) -> fs.stream().map(
//					(f) -> "<" + f.getName() + ":" + f.getValue().toString() + ">")
//					.collect(Collectors.joining("-")))
//			.collect(Collectors.toList());
//		
//		String[] goldFeatures = new String[]{
//				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:>-<NT-Ctx:CC-S-null-NP>-<CON-NT-Path:VP-S-null-CC>-<CON-NT-Path-Size:4>-<firstToken:but>-<lastToken:but>-<tokenBeforeFirst:null>-<tokenAfterLast:its>-<mainVerb:null>",
//				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:PRP-NNS>-<NT-Ctx:NP-S-CC-VP>-<CON-NT-Path:VP-S-null-NP>-<CON-NT-Path-Size:4>-<firstToken:its>-<lastToken:competitors>-<tokenBeforeFirst:but>-<tokenAfterLast:have>-<mainVerb:null>",
//				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:VBP-NP>-<NT-Ctx:VP-VP-null-CC>-<CON-NT-Path:VP-null-VP>-<CON-NT-Path-Size:3>-<firstToken:have>-<lastToken:interests>-<tokenBeforeFirst:competitors>-<tokenAfterLast:and>-<mainVerb:have>",
//				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:>-<NT-Ctx:CC-VP-VP-RB>-<CON-NT-Path:VP-null-CC>-<CON-NT-Path-Size:3>-<firstToken:and>-<lastToken:and>-<tokenBeforeFirst:interests>-<tokenAfterLast:so>-<mainVerb:null>",
//				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:false>-<ChildPat:>-<NT-Ctx:RB-VP-CC-VP>-<CON-NT-Path:VP-null-RB>-<CON-NT-Path-Size:3>-<firstToken:so>-<lastToken:so>-<tokenBeforeFirst:and>-<tokenAfterLast:are>-<mainVerb:null>",
//				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:false>-<ChildPat:VBP-VP>-<NT-Ctx:VP-VP-RB-null>-<CON-NT-Path:VP-null-VP>-<CON-NT-Path-Size:3>-<firstToken:are>-<lastToken:swings>-<tokenBeforeFirst:so>-<tokenAfterLast:null>-<mainVerb:cushioned>"
//		};
//				
//		assertThat(strFeatures).containsExactly(goldFeatures);
//	}
//	
//	@Test
//	public void givenGoldLableWhenConstructingTheRelationThenItIsCorrect(){
//		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
//		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
//		List<String> goldLabels = Arrays.asList("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
//		
//		SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance> labeller = algorithmFactory.getLabeller(aJCas);
//		
//		labeller.accept(goldLabels, discourseRelation.getDiscourseConnective(), instances);
//		
//		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
//		assertThat(relations).hasSize(1);
//		DiscourseRelation relation = relations.iterator().next();
//		
//		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(arg1);
//		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(arg2);
//		assertThat(TokenListTools.getTokenListText(relation.getDiscourseConnective())).isEqualTo(dc);
//
//	}
//	
	
}
