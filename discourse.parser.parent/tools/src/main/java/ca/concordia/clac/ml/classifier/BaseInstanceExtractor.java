package ca.concordia.clac.ml.classifier;

import java.util.Collection;

public interface BaseInstanceExtractor<INSTANCE_TYPE, CONTAINER_TYPE> {
	public Collection<INSTANCE_TYPE> getInstances(CONTAINER_TYPE container);
}
