package ca.concordia.clac.uima.engines.stat;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NotFiniteNumberException;
import org.apache.commons.math3.exception.NotPositiveException;


@SuppressWarnings("serial")
public class LabeledEnumeratedDistribution extends EnumeratedIntegerDistribution{
	public static int[] createARange(int start, int end){
		int[] range = new int[end - start];
		for (int i = start; i < end; i++){
			range[i - start] = i;
		}
		
		return range;
	}
	
	private final List<String> labels;
	public LabeledEnumeratedDistribution(String[] labels, double[] probabilities) throws DimensionMismatchException,
			NotPositiveException, MathArithmeticException, NotFiniteNumberException, NotANumberException {
		super(createARange(0, labels.length), probabilities);
		this.labels = Arrays.asList(labels);
	}

	public List<String> getLabels() {
		return labels;
	}

}
