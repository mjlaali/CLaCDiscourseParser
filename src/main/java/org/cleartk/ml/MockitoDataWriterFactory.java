package org.cleartk.ml;

import java.io.IOException;

public class MockitoDataWriterFactory<OUTCOME_TYPE> implements DataWriterFactory<OUTCOME_TYPE>{

	private static DataWriter<?> instance;
	public static void setInstance(DataWriter<?> instance){
		MockitoDataWriterFactory.instance = instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DataWriter<OUTCOME_TYPE> createDataWriter() throws IOException {
		return ((DataWriter<OUTCOME_TYPE>) instance);
	}

	
}
