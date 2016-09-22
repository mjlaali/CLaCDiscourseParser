package org.cleartk.corpus.conll2015;

public class DependencyBean{
	String dependencyType;
	String dependant;
	String governor;
	
	public DependencyBean(String dependencyType, String dependant, String governor) {
		super();
		this.dependencyType = dependencyType;
		this.dependant = dependant;
		this.governor = governor;
	}
	
	public DependencyBean() {
	}
	
	public String getDependencyType() {
		return dependencyType;
	}
	public void setDependencyType(String dependencyType) {
		this.dependencyType = dependencyType;
	}
	public String getDependant() {
		return dependant;
	}
	public void setDependant(String dependant) {
		this.dependant = dependant;
	}
	public String getGovernor() {
		return governor;
	}
	public void setGovernor(String governor) {
		this.governor = governor;
	}
	
	
}