package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.join;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.feature.TreeFeatureExtractor;

public class ArgumentLabelerAlgorithmFactory implements SequenceClassifierAlgorithmFactory<String, DiscourseConnective, ArgumentInstance>{

	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<ArgumentInstance>> getInstanceExtractor(JCas aJCas) {
		return new ArgumentInstanceExtractor();
	}
	
	public BiFunction<ArgumentInstance, DiscourseConnective, List<Feature>> getArgumentFeatureExtractor(){
		BiFunction<ArgumentInstance, DiscourseConnective, List<Feature>> dcFeatures = 
				(ins, dc) -> DiscourseVsNonDiscourseClassifier.getDiscourseConnectiveFeatures().apply(dc);
			
		Function<ArgumentInstance, Annotation> convertToConstituent = ArgumentInstance::getInstance;
		Function<ArgumentInstance, Feature> childPatterns =
				convertToConstituent.andThen(
						TreeFeatureExtractor.getChilderen()).andThen(
								mapOneByOneTo(TreeFeatureExtractor.getConstituentType())).andThen(
										join(Collectors.joining("-"))).andThen(
												makeFeature("ChildPat"));
		Function<ArgumentInstance, Feature> ntCtx = convertToConstituent
				.andThen(multiMap(
						getConstituentType(), 
						getParent().andThen(getConstituentType()), 
						getLeftSibling().andThen(getConstituentType()),
						getRightSibling().andThen(getConstituentType())
						))
				.andThen(join(Collectors.joining("-")))
				.andThen(makeFeature("NT-Ctx"));
		
		Function<ArgumentInstance, List<Annotation>> pathExtractor = (inst) -> getPath().apply(inst.getImediateDcParent(), inst.getInstance()); 
		Function<ArgumentInstance, Feature> path = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(join(Collectors.joining("-")))
				.andThen(makeFeature("CON-NT-Path"));
		
		BiFunction<ArgumentInstance, DiscourseConnective, List<Feature>> constituentFeatures =
				(inst, dc) -> multiMap(childPatterns, ntCtx, path).apply(inst);
				
		return multiBiFuncMap(dcFeatures, constituentFeatures).andThen(flatMap(Feature.class));
	}
	

	@Override
	public BiFunction<List<ArgumentInstance>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		BiFunction<ArgumentInstance, DiscourseConnective, List<Feature>> biFunc = 
				getArgumentFeatureExtractor();
		return mapOneByOneTo(biFunc);
	}

	@Override
	public BiFunction<List<ArgumentInstance>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		return mapOneByOneTo(new LabelExtractor());
	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, ArgumentInstance> getLabeller(JCas jCas) {
		return new ArgumentConstructor(jCas);
	}


}
