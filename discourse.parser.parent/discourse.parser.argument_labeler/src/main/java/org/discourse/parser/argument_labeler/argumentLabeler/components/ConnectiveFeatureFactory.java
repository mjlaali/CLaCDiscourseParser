package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.dummyFunc;
import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getText;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ConnectiveFeatureFactory {
	public static <T extends Annotation, R extends Annotation> Function<T, R> getLeft(Class<R> r){
		return (ann) -> {
			List<R> selectPreceding = JCasUtil.selectPreceding(r, ann, 1);
			if (selectPreceding.size() > 0)
				return selectPreceding.get(0);
			return null;
		};
	}
	
	public static <T extends Annotation, R extends Annotation> Function<T, R> getRight(Class<R> r){
		return (ann) -> {
			List<R> selectPreceding = JCasUtil.selectFollowing(r, ann, 1);
			if (selectPreceding.size() > 0)
				return selectPreceding.get(0);
			return null;
		};
	}

	public Function<DiscourseConnective, List<Feature>> getInstance(){
		Function<String, String> toLowerCase = String::toLowerCase;
		Function<String, Boolean> isAllLowerCase = StringUtils::isAllLowerCase;
		
		Function<DiscourseConnective, List<Feature>> textFeatures = dummyFunc(DiscourseConnective.class).andThen(getText()).andThen(multiMap(
				toLowerCase.andThen(makeFeature("Connective")),
				isAllLowerCase.andThen((b) -> b.toString()).andThen(makeFeature("ConnectivePosition"))
				));
		
		Function<DiscourseConnective, List<Feature>> contextFeature = dummyFunc(DiscourseConnective.class).andThen(
				multiMap(getLeft(Token.class).andThen(
							multiMap(
									getConstituentType().andThen(makeFeature("ConnectiveLeftPOS")),
									getText().andThen(makeFeature("ConnectiveLeftText"))
									)
							),
						getRight(Token.class).andThen(
							multiMap(
									getConstituentType().andThen(makeFeature("ConnectiveRightPOS")),
									getText().andThen(makeFeature("ConnectiveRightText"))
									)
							)
						)
				).andThen(flatMap(Feature.class));
		
		return multiMap(textFeatures, contextFeature).andThen(flatMap(Feature.class));
	}
}
