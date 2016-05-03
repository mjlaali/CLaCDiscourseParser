package ca.concordia.clac.util.graph;

import org.jgrapht.graph.DefaultEdge;

@SuppressWarnings("serial")
public class LabeledEdge<T> extends DefaultEdge{
	private T label;
	public LabeledEdge(T label) {
		this.label = label;
	}
	
	public T getLabel() {
		return label;
	}
	
	public void setLabel(T label) {
		this.label = label;
	}
}