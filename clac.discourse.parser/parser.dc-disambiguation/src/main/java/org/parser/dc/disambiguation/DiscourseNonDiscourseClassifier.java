package org.parser.dc.disambiguation;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFeatures;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getText;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.extractFromScope;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.getLast;
import static ca.concordia.clac.ml.scop.Scopes.getPathToRoot;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;

import ca.concordia.clac.ml.classifier.ClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.InstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;


public class DiscourseNonDiscourseClassifier implements ClassifierAlgorithmFactory<String, DiscourseConnective>{

	private LookupInstanceExtractor lookupInstanceExtractor = new LookupInstanceExtractor();
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		lookupInstanceExtractor.initialize(context);
	}

	@Override
	public InstanceExtractor<DiscourseConnective> getExtractor() {
		return lookupInstanceExtractor;
	}

	
	@Override
	public List<Function<DiscourseConnective, List<Feature>>> getFeatureExtractor() {
		Function<DiscourseConnective, List<Feature>> pathFeatures = getPathToRoot(DiscourseConnective.class)
				.andThen(extractFromScope(
				 getLast(Constituent.class).andThen(
						 getFeatures(getFeature("selfCat", getConstituentType()),
								 	 getFeature("selfCatParent", getParent().andThen(getConstituentType())),
								 	 getFeature("selfCatLeftSibling", getLeftSibling().andThen(getConstituentType())),
						 			 getFeature("selfCatLeftSibling", getRightSibling().andThen(getConstituentType()))
						 )) 
			));
		
		return Arrays.asList(
				getFeatures(getFeature("coveredText", getText(DiscourseConnective.class))), 
				pathFeatures);
	}

	@Override
	public Function<DiscourseConnective, String> getLabelExtractor() {
		return (dc) -> {
			List<DiscourseConnective> selectCovered = JCasUtil.selectCovering(DiscourseConnective.class, dc);
			for (DiscourseConnective indexedDc: selectCovered){
				if (indexedDc.getBegin() == dc.getBegin() &&
					indexedDc.getEnd() == dc.getEnd()){
					return Boolean.toString(true);
				}
			}
			return Boolean.toString(false);
		};
	}

	@Override
	public BiConsumer<String, DiscourseConnective> getLabeller() {
		return (label, dc) ->{
			boolean toAdd = Boolean.parseBoolean(label);
			if (toAdd)
				dc.addToIndexes();
		};
	}

}
