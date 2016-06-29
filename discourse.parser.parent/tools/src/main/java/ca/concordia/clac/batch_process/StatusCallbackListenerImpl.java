package ca.concordia.clac.batch_process;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;

public class StatusCallbackListenerImpl
implements StatusCallbackListener
{

	private final List<Exception> exceptions = new ArrayList<Exception>();

	private boolean processing = true;

	@Override
	public void entityProcessComplete(CAS arg0, EntityProcessStatus arg1)
	{
		if (arg1.isException()) {
			for (Exception e : arg1.getExceptions()) {
				exceptions.add(e);
			}
		}
	}

	@Override
	public void aborted()
	{
		synchronized (this) {
			if (processing) {
				processing = false;
				notify();
			}
		}
	}

	@Override
	public void batchProcessComplete()
	{
		// Do nothing
	}

	@Override
	public void collectionProcessComplete()
	{
		synchronized (this) {
			if (processing) {
				processing = false;
				notify();
			}
		}
	}

	@Override
	public void initializationComplete()
	{
		// Do nothing
	}

	@Override
	public void paused()
	{
		// Do nothing
	}

	@Override
	public void resumed()
	{
		// Do nothing
	}
	
	public boolean isProcessing() {
		return processing;
	}
	
	public List<Exception> getExceptions() {
		return exceptions;
	}
}