package ca.concordia.clac.uima.engines.stat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class StatisticFactory {

	public static StatisticResult getStatistic(File statOutputFile) throws IOException, ClassNotFoundException {
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(statOutputFile));
		Object res = input.readObject();
		input.close();
		return (StatisticResult) res;
	}

}
