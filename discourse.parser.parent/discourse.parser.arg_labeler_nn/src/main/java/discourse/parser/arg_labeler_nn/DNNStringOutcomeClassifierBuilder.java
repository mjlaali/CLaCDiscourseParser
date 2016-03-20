package discourse.parser.arg_labeler_nn;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.jar.SequenceClassifierBuilder_ImplBase;

/** 
 * This Class is designed to replace the MalletCrfStringOutcomeClassifierBuilder as 
 * written in the ClearTK Framework. As such it builds on the same implementations
 * that the ClearTK framework provides.
 * 
 * The code exposes out the data and acts as a wrapper around code written in Python
 * in order to communicate with the new SequenceClassifier that is created for this
 * project
 * 
 * @author Sohail Hooda
 */
public class DNNStringOutcomeClassifierBuilder extends 
	SequenceClassifierBuilder_ImplBase<DNNStringOutcomeClassifier, List<NameNumber>, String, String> {
	
	@Override
	protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
		// TODO Auto-generated method stub
		super.packageClassifier(dir, modelStream);
	    JarStreams.putNextJarEntry(modelStream, MODEL_NAME, new File(dir, MODEL_NAME));
	}
	
	@Override
	protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
		// TODO Auto-generated method stub
		super.unpackageClassifier(modelStream);
	    JarStreams.getNextJarEntry(modelStream, MODEL_NAME);
	}

	private static final String MODEL_NAME = "model.h5";

	@Override
	public File getTrainingDataFile(File dir) {
		// TODO Auto-generated method stub
		return new File(dir, "training-data.DNN");
	}

	@Override
	protected DNNStringOutcomeClassifier newClassifier() {
		// TODO Auto-generated method stub
		return new DNNStringOutcomeClassifier(this.featuresEncoder, this.outcomeEncoder);
	}

	@Override
	public void trainClassifier(File dir, String... args) throws Exception {
		// TODO Auto-generated method stub
		String[] dnnArgs = new String[args.length + 4];
		System.arraycopy(args, 0, dnnArgs, 0, args.length);
		dnnArgs[dnnArgs.length - 4] = "--train";
		dnnArgs[dnnArgs.length - 3] = getTrainingDataFile(dir).getPath();
		dnnArgs[dnnArgs.length - 2] = "--model-file";
		dnnArgs[dnnArgs.length - 1] = new File(dir, MODEL_NAME).getPath(); 
		Process pyProc = PyNNRunner.executeWithArgs(dnnArgs);
		pyProc.waitFor();
		
		
	}
	
}