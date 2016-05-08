package org.discourse.parser.argument_labeler.argumentLabeler.components;

import java.util.Comparator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.uima.jcas.tcas.Annotation;

public class AnnotationSizeComparator<T extends Annotation> implements Comparator<T>{

	@Override
	public int compare(Annotation o1, Annotation o2) {	//it is like DFS
		return new CompareToBuilder()
				.append(o1.getEnd(), o2.getEnd())
				.append(size(o1), size(o2))
				.append(o1.getClass().getName(), o2.getClass().getName())
				.toComparison();
	}

	private int size(Annotation ann){
		return ann.getEnd() - ann.getBegin();
	}

}