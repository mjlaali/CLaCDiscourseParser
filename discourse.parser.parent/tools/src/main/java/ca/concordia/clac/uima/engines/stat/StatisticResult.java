package ca.concordia.clac.uima.engines.stat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

@SuppressWarnings("serial")
public class StatisticResult implements Serializable{
	private final Map<String, Map<String, Integer>> counts;
	private final Map<String, EnumeratedDistribution<String>> distributions;
	
	public StatisticResult(Map<String, Map<String, Integer>> counts) {
		this.counts = counts;
		distributions = new HashMap<>();
		for (Entry<String, Map<String, Integer>> catFreqs: counts.entrySet()){
			Map<String, Integer> freqs = catFreqs.getValue();
			List<String> labels = new ArrayList<String>(freqs.keySet());
			Collections.sort(labels);
			
			List<Pair<String, Double>> pmf = new ArrayList<>();
			
			for (String label: labels){
				pmf.add(new Pair<String, Double>(label, freqs.get(label).doubleValue()));
			}
			
			distributions.put(catFreqs.getKey(), new EnumeratedDistribution<String>(pmf));
		}
	}

	public EnumeratedDistribution<String> getDistribution(String name) {
		return distributions.get(name);
	}

	public Map<String, Map<String, Integer>> getCounts() {
		return counts;
	}
}
