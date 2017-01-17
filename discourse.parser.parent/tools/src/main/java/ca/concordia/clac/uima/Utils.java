package ca.concordia.clac.uima;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

public class Utils {
	public static void runWithProgressbar(CollectionReader reader, AnalysisEngineDescription engineDescription)
			throws UIMAException, IOException, ResourceInitializationException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean toExit = false;
				while (!toExit){
					try {
						Thread.sleep(1000);
						Progress[] progresses = reader.getProgress();
						for (Progress progess: progresses){
							System.out.println(progess.getCompleted() + "/" + progess.getTotal());
							if (progess.getCompleted() == progess.getTotal())
								toExit = true;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		SimplePipeline.runPipeline(reader, engineDescription);
	}
}
