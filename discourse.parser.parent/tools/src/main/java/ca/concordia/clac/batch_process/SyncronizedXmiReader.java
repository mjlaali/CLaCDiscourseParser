package ca.concordia.clac.batch_process;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;

public class SyncronizedXmiReader extends XmiReader{
	private String sourceLocation;
	private SyncronizedDirectory synchronizedDirectory;
	private Map<URI, Resource> uriToResourse = new TreeMap<>();
	private URI uri;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		sourceLocation = getSourceLocation();
		
		Collection<Resource> resources = getResources();
		List<URI> uris = new ArrayList<>();
		for (Resource resource: resources){
			uriToResourse.put(resource.getResolvedUri(), resource);
		}
		
		synchronizedDirectory.init(new File(sourceLocation), uris);
		
	}
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		if (uri != null){
			synchronizedDirectory.remove(uri);
		}
		uri = synchronizedDirectory.pickAURI();
		return uri != null;
	}
	
	@Override
	protected Resource nextFile() {
		return uriToResourse.get(uri);
	}
}
