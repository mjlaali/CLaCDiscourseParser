package ca.concordia.clac.uima;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

public class Utils {
	public static String formatInterval(final long l)
    {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
	
	public static void runWithProgressbar(CollectionReader reader, AnalysisEngineDescription... engineDescription)
			throws UIMAException, IOException, ResourceInitializationException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean toExit = false;
				long completed = 0;
				Date start = new Date();
				while (!toExit){
					try {
						Thread.sleep(1000);
						Progress[] progresses = reader.getProgress();
						for (Progress progess: progresses){
							if (completed  != progess.getCompleted()){
								Date end = new Date();
								long processtime = end.getTime() - start.getTime();
								System.out.println(progess.getCompleted() + "/" + progess.getTotal() + "->" + formatInterval(processtime));
								start = end;
								completed = progess.getCompleted();
							}
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
