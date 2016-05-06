package org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler.copy;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class DCTreeNodeArgInstance{
	private Constituent imediateDcParent;
	private Annotation node;
	private ArgumentTreeNode argTreeNode;
	
	public DCTreeNodeArgInstance(Annotation node, Constituent imediateDcParent, JCas jcas) {
		this.node = node;
		this.imediateDcParent = imediateDcParent;
		ArgumentTreeNode argTreeNode = new ArgumentTreeNode(jcas);
		argTreeNode.setTreeNode(node);
		this.argTreeNode = argTreeNode;
	}
	
	public Annotation getNode() {
		return node;
	}
	
	public Constituent getImediateDcParent() {
		return imediateDcParent;
	}
	
	public ArgumentTreeNode getArgTreeNode() {
		return argTreeNode;
	}
}