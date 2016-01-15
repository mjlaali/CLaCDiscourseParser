package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.util.treebank.TreebankNode;

import ca.concordia.clac.ml.classifier.ClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.InstanceExtractor;

class Arg1Instance{
	public Arg1Instance(TreebankNode aCandid, DiscourseRelation discourseRelation, TreebankNode imidiateParent, TreebankNode imediateDcParent) {
		this.treebankNode = aCandid;
		this.discourseRelation = discourseRelation;
		this.imidiateParent = imidiateParent;
		this.imediateDcParent = imediateDcParent;
	}

	TreebankNode treebankNode;
	DiscourseRelation discourseRelation;
	TreebankNode imidiateParent;
	TreebankNode imediateDcParent;
}

public class Arg1Labeler implements ClassifierAlgorithmFactory<String, Arg1Instance>{
	@Override
	public InstanceExtractor<Arg1Instance> getExtractor(JCas jCas) {
		// TODO Auto-generated method stub
		return null;
	}


	
	@Override
	public List<Function<Arg1Instance, List<Feature>>> getFeatureExtractor(JCas jCas) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Function<Arg1Instance, String> getLabelExtractor(JCas jCas) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BiConsumer<String, Arg1Instance> getLabeller(JCas jCas) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		// TODO Auto-generated method stub
		
	}

}
