package ca.concordia.clac.batch_process;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public class SlowEngine extends JCasAnnotator_ImplBase{
	private static Integer mutex = 0;
	private static int cnt = 0;
	private int id;
	
	public SlowEngine() {
		synchronized (mutex) {
			cnt++;
		}
		id = cnt;
		
		System.out.println("SlowEngine.initialize() " + id);

	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			System.out.println("SlowEngine.process() " + id);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		System.out.println("SlowEngine.collectionProcessComplete() " + id);
	}
	
	@Override
	public void batchProcessComplete() throws AnalysisEngineProcessException {
		super.batchProcessComplete();
		System.out.println("SlowEngine.batchProcessComplete() " + id);
	}
}
