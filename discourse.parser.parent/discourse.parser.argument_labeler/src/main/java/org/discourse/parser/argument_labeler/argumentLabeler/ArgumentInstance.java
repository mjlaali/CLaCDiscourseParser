package org.discourse.parser.argument_labeler.argumentLabeler;

import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class ArgumentInstance{
	Constituent imediateDcParent;
	Annotation instance;
	public ArgumentInstance(Annotation instance, Constituent imediateDcParent) {
		this.instance = instance;
		this.imediateDcParent = imediateDcParent;
	}
	
	public Annotation getInstance() {
		return instance;
	}
	
	public Constituent getImediateDcParent() {
		return imediateDcParent;
	}
}