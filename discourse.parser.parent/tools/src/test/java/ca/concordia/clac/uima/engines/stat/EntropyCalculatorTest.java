package ca.concordia.clac.uima.engines.stat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

public class EntropyCalculatorTest {

	@Test
	public void givenASpikeWhenCalculatingEntropyThenTheEntropyIsZero(){
		EnumeratedDistribution<String> distribution = new EnumeratedDistribution<>(Arrays.asList(new Pair<String, Double>("a", 12.0)));
		
		double entropy = EntropyCalculator.getEntropy(distribution);
		assertThat(entropy).isEqualTo(0.0);
	}
	
	@Test
	public void givenAnUniformDistributionOver2ElementsWhenCalculatingEntropyThenTheEntropyIsOne(){
		EnumeratedDistribution<String> distribution = new EnumeratedDistribution<>(
				Arrays.asList(new Pair<String, Double>("a", 1.0), new Pair<String, Double>("b", 1.0)));
		
		double entropy = EntropyCalculator.getEntropy(distribution);
		assertThat(entropy).isEqualTo(1.0);
	}

}
