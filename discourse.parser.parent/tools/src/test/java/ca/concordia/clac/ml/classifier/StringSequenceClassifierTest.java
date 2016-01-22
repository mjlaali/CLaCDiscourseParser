package ca.concordia.clac.ml.classifier;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cleartk.examples.pos.BuildTestExamplePosModel;
import org.cleartk.examples.pos.ExamplePosAnnotator;
import org.cleartk.examples.pos.PosClassifierAlgorithm;
import org.cleartk.examples.pos.RunExamplePosAnnotator;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.junit.Test;

public class StringSequenceClassifierTest {

	@Test
	public void givenClearTKExampleWhenGeneratingDataUsingStringSequenceClassifierThenProduceTheSameOutput() throws Exception{
		String cleartkOutput = "outputs/test/" + getClass().getSimpleName() + "/cleartk/";
		String stringSequenceClassifierOutput = "outputs/test/" + getClass().getSimpleName() + "/seqClassifier/";
		BuildTestExamplePosModel.main(ExamplePosAnnotator.getWriterDescription(cleartkOutput), cleartkOutput);
		BuildTestExamplePosModel.main(PosClassifierAlgorithm.getWriterDescription(stringSequenceClassifierOutput), stringSequenceClassifierOutput);
		
//		String featureFile = "delegated-model/training-data.maxent";
		String featureFile = "training-data.malletcrf";
		assertThat(FileUtils.readFileToString(new File(cleartkOutput + featureFile))).isEqualTo(
				FileUtils.readFileToString(new File(stringSequenceClassifierOutput + featureFile)));
		
		RunExamplePosAnnotator.main(ExamplePosAnnotator.getClassifierDescription(JarClassifierBuilder.getModelJarFile(
					cleartkOutput).getPath()), cleartkOutput);
		RunExamplePosAnnotator.main(PosClassifierAlgorithm.getClassifierDescription(JarClassifierBuilder.getModelJarFile(
					stringSequenceClassifierOutput).getPath()), stringSequenceClassifierOutput);
		
		String posFile = "2008_Sichuan_earthquake.txt.pos";
		assertThat(FileUtils.readFileToString(new File(cleartkOutput + posFile))).isEqualTo(
				FileUtils.readFileToString(new File(stringSequenceClassifierOutput + posFile)));
	}
}
