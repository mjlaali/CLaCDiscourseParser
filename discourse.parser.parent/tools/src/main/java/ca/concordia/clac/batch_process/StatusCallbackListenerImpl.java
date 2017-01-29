package ca.concordia.clac.batch_process;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;

public class StatusCallbackListenerImpl implements StatusCallbackListener {

	private final List<Exception> exceptions = new ArrayList<Exception>();

	private boolean processing = true;
	private int cnt;

	@Override
	public void entityProcessComplete(CAS arg0, EntityProcessStatus arg1) {
		if (arg1.isException()) {
			for (Exception e : arg1.getExceptions()) {
				synchronized (this) {
					exceptions.add(e);
				}
			}
		}
		synchronized (this) {
			++cnt;
		}
	}

	@Override
	public void aborted() {
		synchronized (this) {
			if (processing) {
				processing = false;
				notify();
			}
		}
	}

	@Override
	public void batchProcessComplete() {
		
	}

	@Override
	public void collectionProcessComplete() {
		synchronized (this) {
			if (processing) {
				processing = false;
				notify();
			}
		}
	}

	@Override
	public void initializationComplete() {
		// Do nothing
	}

	@Override
	public void paused() {
		// Do nothing
	}

	@Override
	public void resumed() {
		// Do nothing
	}

	public boolean isProcessing() {
		return processing;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}
	
	public int getCompletedEntityCnt(){
		return cnt;
	}
}