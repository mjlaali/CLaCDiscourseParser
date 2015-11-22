package org.parser.dc.disambiguation;

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
	public Function<DiscourseConnective, List<Feature>> getFeatureExtractor() {
		return null;
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
