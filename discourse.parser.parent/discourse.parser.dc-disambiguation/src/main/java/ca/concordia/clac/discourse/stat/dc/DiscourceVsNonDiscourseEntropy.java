package ca.concordia.clac.discourse.stat.dc;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.uima.engines.stat.EntropyCalculator;
import ca.concordia.clac.uima.engines.stat.WekaToDistribution;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveFrequentValues;

public class DiscourceVsNonDiscourseEntropy {
	public static final String DC_OUTCOME = "outcome";
	public static final String DC_STR = DiscourseVsNonDiscourseClassifier.CONN_LStr;
	private List<Pair<String, Double>> connectiveEntropies;
	private Instances instances;
	private Set<String> validDcs = new HashSet<>();

	public DiscourceVsNonDiscourseEntropy(File arffFile, int minFreq) throws Exception {
		DataSource source = new DataSource(new FileInputStream(arffFile));
		instances = source.getDataSet();

		getPruned(minFreq);
		WekaToDistribution wekaToDistribution = new WekaToDistribution(instances);
		Map<String, EnumeratedDistribution<String>> conditionalDistribution = wekaToDistribution.
				getConditionalDistribution(DC_OUTCOME, DC_STR);

		connectiveEntropies = new ArrayList<>();
		for (Entry<String, EnumeratedDistribution<String>> aConnDistribution: conditionalDistribution.entrySet()){
			connectiveEntropies.add(new Pair<>(aConnDistribution.getKey(), 
					EntropyCalculator.getEntropy(aConnDistribution.getValue())));
		}

		Collections.sort(connectiveEntropies, (a, b) -> b.getValue().compareTo(a.getValue()));
	}

	private void getPruned(int minCount) throws Exception {
		Map<String, Integer> dcToFrequency = getFrequency(instances, DC_STR);
		List<Entry<String, Integer>> frequencies = new ArrayList<>(dcToFrequency.entrySet());
		Collections.sort(frequencies, (a, b) -> a.getValue().compareTo(b.getValue()));
		int invalidCnt = 0;
		for (Entry<String, Integer> aDcFreq: frequencies){
			if (aDcFreq.getValue() < minCount)
				++invalidCnt;
		}

		RemoveFrequentValues filter = new RemoveFrequentValues();
		filter.setAttributeIndex("" + (instances.attribute(DC_STR).index() + 1)); 
		filter.setInvertSelection(true);
		filter.setNumValues(invalidCnt);
		filter.setInputFormat(instances);
		filter.setModifyHeader(true);

		Instances prunedInstances = Filter.useFilter(instances, filter);
		Attribute attribute = prunedInstances.attribute(DC_STR);
		for (int i = 0; i < attribute.numValues(); i++){
			validDcs.add(attribute.value(i));
		}
	}

	public Map<String, Integer> getFrequency(){
		return getFrequency(instances, DC_STR);
	}

	public static Map<String, Integer> getFrequency(Instances instances, String attributeName){
		Attribute dcTextAttr = instances.attribute(attributeName);
		AttributeStats attributeStats = instances.attributeStats(dcTextAttr.index());
		int[] counts = attributeStats.nominalCounts;
		Map<String, Integer> freq = new HashMap<>();
		for (int i = 0; i < counts.length; i++){
			freq.put(dcTextAttr.value(i), counts[i]);
		}
		return freq;
	}

	public Set<String> getValidDcs() {
		return validDcs;
	}

	public List<Pair<String, Double>> sortBasedOnEntropy() {
		return connectiveEntropies;
	}

	public static void main(String[] args) throws Exception {

		File arffFile = new File("outputs/resources/discourse-vs-nondiscourse/training-data.arff");

		DiscourceVsNonDiscourseEntropy discourceVsNonDiscourseEntropy = new DiscourceVsNonDiscourseEntropy(arffFile, 20);
		List<Pair<String, Double>> connectiveEntropy = discourceVsNonDiscourseEntropy.sortBasedOnEntropy();
		Map<String, Integer> freq = discourceVsNonDiscourseEntropy.getFrequency();
		Set<String> validDcs = discourceVsNonDiscourseEntropy.getValidDcs();

		connectiveEntropy.stream().forEach( (p) -> { 
				if (validDcs.contains(p.getFirst()))
					System.out.printf("%s\t%f\t%d\n", p.getFirst(), p.getSecond(), freq.get(p.getFirst()));
			}
		);
	}

}
