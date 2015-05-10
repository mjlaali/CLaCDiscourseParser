package org.cleartk.ml;

import java.io.IOException;

public class MockitoClassifierFactory<OUTCOME_TYPE> implements ClassifierFactory<OUTCOME_TYPE>{

	private static Classifier<?> instance;
	
	public static void setInstance(Classifier<?> instance){
		MockitoClassifierFactory.instance = instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Classifier<OUTCOME_TYPE> createClassifier() throws IOException {
		return (Classifier<OUTCOME_TYPE>)instance;
	}

}
