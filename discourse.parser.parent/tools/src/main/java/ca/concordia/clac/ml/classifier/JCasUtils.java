package ca.concordia.clac.ml.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JCasUtils {
	public static Object[] addParams(Object[] otherParams, Object... newParams){
		List<Object> params = new ArrayList<>();
		params.addAll(Arrays.asList(otherParams));
		params.addAll(Arrays.asList(newParams));
		
		return params.toArray(new Object[params.size()]);

	}
}
