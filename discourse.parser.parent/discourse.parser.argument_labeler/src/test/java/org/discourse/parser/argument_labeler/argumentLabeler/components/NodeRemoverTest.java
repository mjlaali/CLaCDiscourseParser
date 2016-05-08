package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


public class NodeRemoverTest {
	private NodeRemover classifier = new NodeRemover();
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private DiscourseRelationExample example = new NodeRemoverExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
	}
	
	@Test
	public void createsOneSequence(){
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		Collection<? extends DiscourseConnective> sequences = classifier.getSequenceExtractor(aJCas).apply(aJCas);
		assertThat(sequences).hasSize(1);
	}
	
	@Test
	public void createsTenInstancesAtTraining(){
		discourseRelation.addToIndexesRecursively();
		List<Annotation> instances = classifier.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		//12 + 1 + 23
		assertThat(instances).hasSize(36);
	}
	
	@Test
	public void thirteenLabelAreTrueAndEqualToVP(){
		discourseRelation.addToIndexesRecursively();
		List<Annotation> instances = classifier.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		List<String> labels = classifier.getLabelExtractor(aJCas).apply(instances, discourseRelation.getDiscourseConnective());

		long cnt = labels.stream().filter(Boolean::valueOf).count();
		assertThat(cnt).isEqualTo(13);
	}
	
	@Test
	public void removingFromTheArg1(){
		DiscourseArgument arg1 = discourseRelation.getArguments(0);
		int begin = aJCas.getDocumentText().indexOf("talked to");
		int end = aJCas.getDocumentText().indexOf("market") + "market".length();
		
		List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, begin, end);
		TokenListTools.initTokenList(arg1, tokens);
		
		discourseRelation.addToIndexesRecursively();
		
		List<Annotation> instances = classifier.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		assertThat(instances).hasSize(36);
		
		List<String> outcomes = new ArrayList<>();
		
		end = aJCas.getDocumentText().indexOf("and told them to cool it");
		for (int i = 0; i < instances.size(); i++){
			if (begin <= instances.get(i).getBegin() && instances.get(i).getEnd() <= end)
				outcomes.add(Boolean.toString(true));
			else
				outcomes.add(Boolean.toString(false));
		}
		
		classifier.getLabeller(aJCas).accept(outcomes, discourseRelation.getDiscourseConnective(), instances);
		
		assertThat(discourseRelation.getArguments(0).getCoveredText()).isEqualTo(example.getArg1());
	}
}
