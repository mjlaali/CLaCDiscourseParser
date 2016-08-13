package ca.concordia.clac.uima.engines.stat;

import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

public class EntropyCalculator {
	public static <T> double getEntropy(EnumeratedDistribution<T> distribution){
		double entropy = 0;
		List<Pair<T, Double>> pmf = distribution.getPmf();
		for (Pair<T, Double> pm : pmf) {
			
			Double probability = pm.getValue();
			if (probability == 0) 
				continue;

			entropy -= probability * FastMath.log(2, probability);
		}

		return entropy;
	}
}
