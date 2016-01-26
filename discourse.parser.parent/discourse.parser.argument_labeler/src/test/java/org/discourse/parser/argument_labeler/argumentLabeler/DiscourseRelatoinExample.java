package org.discourse.parser.argument_labeler.argumentLabeler;

import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.DiscourseRelation;

public interface DiscourseRelatoinExample {

	JCas getJCas();
	DiscourseRelation getRelation();
	String getArg1();
	String getArg2();
	String getDc();

}