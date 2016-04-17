package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;

public class PurifyDiscourseRelations implements SequenceClassifierConsumer<String, ArgumentTreeNode, Annotation> {

	@Override
	public void accept(List<String> outcomes, ArgumentTreeNode aSequence, List<Annotation> instances) {
		
	}

}
