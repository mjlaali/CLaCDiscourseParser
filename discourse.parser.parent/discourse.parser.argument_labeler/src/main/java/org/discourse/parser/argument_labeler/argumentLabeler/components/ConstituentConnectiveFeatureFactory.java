package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.collect;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;

public class ConstituentConnectiveFeatureFactory {

	public BiFunction<Annotation, DiscourseConnective, List<Feature>> getInstance(){
		BiFunction<Annotation, DiscourseConnective, List<Annotation>> pathExtractor = 
				(cons, dc) -> getPath().apply(cons, dc); 
		BiFunction<Annotation, DiscourseConnective, Feature> path = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(collect(Collectors.joining("-")))
				.andThen(makeFeature("CON-NT-Path"));

		BiFunction<Annotation, DiscourseConnective, Feature> pathSize = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(collect(Collectors.counting()))
				.andThen(makeFeature("CON-NT-Path-Size"));

		return multiBiFuncMap(path, pathSize);
	}
}
