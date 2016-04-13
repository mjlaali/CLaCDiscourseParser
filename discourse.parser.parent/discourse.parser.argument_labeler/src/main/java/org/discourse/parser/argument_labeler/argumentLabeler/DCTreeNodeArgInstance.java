package org.discourse.parser.argument_labeler.argumentLabeler;

import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class DCTreeNodeArgInstance{
	Constituent imediateDcParent;
	Annotation node;
	public DCTreeNodeArgInstance(Annotation node, Constituent imediateDcParent) {
		this.node = node;
		this.imediateDcParent = imediateDcParent;
	}
	
	public Annotation getNode() {
		return node;
	}
	
	public Constituent getImediateDcParent() {
		return imediateDcParent;
	}
}