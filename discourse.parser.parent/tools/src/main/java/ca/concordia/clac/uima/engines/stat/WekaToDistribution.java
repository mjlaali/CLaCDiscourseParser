package ca.concordia.clac.uima.engines.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class WekaToDistribution {
	private final Instances instances;
	
	public WekaToDistribution(Instances instances) {
		this.instances = instances;
	}

	public Map<String, EnumeratedDistribution<String>> getConditionalDistribution(String targetAttributeName,
			String conditionedVariable) throws Exception {

		Attribute filterByValueAttribute = getAttribute(conditionedVariable);
		List<String> filterLabels = new ArrayList<>();
		for (int i = 0; i < filterByValueAttribute.numValues(); i++){
			filterLabels.add(filterByValueAttribute.value(i));
		}
		
		Attribute targetAttribute = getAttribute(targetAttributeName);
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < targetAttribute.numValues(); i++){
			labels.add(targetAttribute.value(i));
		}
		
		Map<String, EnumeratedDistribution<String>> dists = new TreeMap<>();
		
		AttributeStats attributeStats = instances.attributeStats(filterByValueAttribute.index());
		for (int i = 0; i < attributeStats.distinctCount; i++){
			
			RemoveWithValues filterWithValue = new RemoveWithValues();
			filterWithValue.setAttributeIndex("" + (filterByValueAttribute.index() + 1));
			filterWithValue.setInvertSelection(true);
			filterWithValue.setNominalIndices("" + (i + 1));
			filterWithValue.setSplitPoint(0.0);
			filterWithValue.setInputFormat(instances);
			
			Instances filetedInstance = Filter.useFilter(instances, filterWithValue);
			AttributeStats conditionalStat = filetedInstance.attributeStats(targetAttribute.index());
			
			int[] counts = conditionalStat.nominalCounts;
			List<Pair<String, Double>> pmf = new ArrayList<>();
			for (int j = 0; j < counts.length; j++){
				pmf.add(new Pair<String, Double>(labels.get(j), (double)counts[j]));
			}
			
			dists.put(filterLabels.get(i), new EnumeratedDistribution<>(pmf));
		}
		return dists;
	}

	private Attribute getAttribute(String attributeName) {
		Attribute filterByValueAttribute = instances.attribute(attributeName);
		if (filterByValueAttribute == null){
			throw new RuntimeException(String.format("Cannot find attribute <%s> in the dataset", filterByValueAttribute));
		}
		return filterByValueAttribute;
	}

}
