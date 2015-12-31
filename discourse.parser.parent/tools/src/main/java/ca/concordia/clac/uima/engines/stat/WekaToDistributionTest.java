package ca.concordia.clac.uima.engines.stat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class WekaToDistributionTest {
	public static List<String> range(int len, String prefix){
		List<String> res = new ArrayList<>();
		for (int i = 0; i < len; i++){
			res.add(prefix + i);
		}

		return res;
	}

	@Test
	public void givenTwoFeaturesInInstancesWhenExtractingConditionalDistributionThenTheDistributionIsCorrect() throws Exception{
		ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
		String feature = "feature";
		String cls = "cls";
		
		Attribute featureAttr = new Attribute(feature, range(2, "f"));
		Attribute clsAttr = new Attribute(cls, range(2, "c"));
		
		attInfo.add(featureAttr);
		attInfo.add(clsAttr);

		Instances instances = new Instances("test", attInfo, 8);
		for (int i = 0; i < 4; i++){
			DenseInstance instance = new DenseInstance(2);
			instance.setValue(featureAttr, "f0");
			instance.setValue(clsAttr, i < 2 ? "c0" : "c1");
			instances.add(instance);
		}

		for (int i = 0; i < 4; i++){
			DenseInstance instance = new DenseInstance(2);
			instance.setValue(featureAttr, "f1");
			instance.setValue(clsAttr, "c1");
			instances.add(instance);
		}
		
		WekaToDistribution wekaToDistribution = new WekaToDistribution(instances);
		String randomVariable = cls;
		String conditionedVariable = feature;
		Map<String, EnumeratedDistribution<String>> conditionalDistribution = 
				wekaToDistribution.getConditionalDistribution(randomVariable, conditionedVariable);
		
		assertThat(conditionalDistribution.keySet()).containsOnly(range(2, "f").toArray(new String[2]));
		assertThat(EntropyCalculator.getEntropy(conditionalDistribution.get("f0"))).isEqualTo(1);
		assertThat(EntropyCalculator.getEntropy(conditionalDistribution.get("f1"))).isEqualTo(0);
	}
}
