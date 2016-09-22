package ca.concordia.clac.batch_process;

import java.io.File;
import java.net.URI;
import java.util.List;

public interface SyncronizedDirectory {

	boolean init(File file, List<URI> uris);

	URI pickAURI();

	boolean remove(URI uri);

}
