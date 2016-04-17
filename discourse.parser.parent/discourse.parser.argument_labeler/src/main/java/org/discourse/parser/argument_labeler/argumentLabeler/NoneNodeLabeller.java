package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class NoneNodeLabeller implements SequenceClassifierAlgorithmFactory<String, ArgumentTreeNode, Annotation>{
	private Map<DiscourseArgument, Collection<Constituent>> argToConstituents;
	private Map<DiscourseArgument, List<Token>> argToTokens;

	@Override
	public Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, ArgumentTreeNode.class);
	}

	@Override
	public Function<ArgumentTreeNode, List<Annotation>> getInstanceExtractor(JCas aJCas) {
		argToConstituents = JCasUtil.indexCovered(aJCas, DiscourseArgument.class, Constituent.class);
		
		return (treeNode) -> {
			return getAllChilderen(treeNode.getTreeNode(), new ArrayList<>());
		};
	}
	
	public List<Annotation> getAllChilderen(Annotation ann, List<Annotation> allChilderen){
		if (ann instanceof Constituent){
			Constituent node = (Constituent) ann;
			for (int i = 0; i < node.getChildren().size(); i++){
				Annotation aChild = node.getChildren(i);
				allChilderen.add(aChild);
				getAllChilderen(aChild, allChilderen);
			}
			return allChilderen;
		}
		
		return Collections.emptyList();
	}
	
	

	@Override
	public BiFunction<List<Annotation>, ArgumentTreeNode, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> annotationFeatureExtractor = null;
		return mapOneByOneTo(annotationFeatureExtractor);
	}

	@Override
	public BiFunction<List<Annotation>, ArgumentTreeNode, List<String>> getLabelExtractor(JCas jCas) {
		BiFunction<Annotation, ArgumentTreeNode, String> getLabel = null;

		return mapOneByOneTo(getLabel);
	}

	@Override
	public SequenceClassifierConsumer<String, ArgumentTreeNode, Annotation> getLabeller(JCas jCas) {
		return new PurifyDiscourseRelations();
	}

}
