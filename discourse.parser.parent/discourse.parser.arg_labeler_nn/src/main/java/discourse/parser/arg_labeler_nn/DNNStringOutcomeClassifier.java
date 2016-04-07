package discourse.parser.arg_labeler_nn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.ml.jar.SequenceClassifier_ImplBase;

/**
 * This Class is designed to replace the MalletCrfStringOutcomeClassifierBuilder
 * as written in the ClearTK Framework. As such it builds on the same
 * implementations that the ClearTK framework provides.
 * 
 * The code exposes out the data and acts as a wrapper around code written in
 * Python in order to communicate with the new SequenceClassifier that is
 * created for this project
 * 
 * @author Sohail Hooda
 */
public class DNNStringOutcomeClassifier extends SequenceClassifier_ImplBase<List<NameNumber>, String, String> {

	Process pyProcess;

	public DNNStringOutcomeClassifier(FeaturesEncoder<List<NameNumber>> featuresEncoder,
			OutcomeEncoder<String, String> outcomeEncoder) {
		super(featuresEncoder, outcomeEncoder);
	}

	/**
	 * This method passes the test set to the Python code for classification
	 * task. All the data is written out and a Python file is called to classify
	 * and write out the results Once the results are written out, the result
	 * file is then picked up and processed within the pipeline.
	 * 
	 * @param features
	 *            This is a list of 'lists of features'. Essentially, each item
	 *            in the list is a list of features. These secondary lists
	 *            represent instances (where each list of features is an
	 *            instance). The list of features is basically the text data
	 *            itself 'marked' as "text" and the discourse connective marked
	 *            as "dc"
	 */
	@Override
	public List<String> classify(List<List<Feature>> features) throws CleartkProcessingException {
		String[][] featureStringArray = toStrings(features);

		if (PyNNRunner.sout == null) {
			PyNNRunner.executeWithArgs(new String[] { "--test", "conditionubuntu:7860",
					"--model-file", "outputs/resources/package/model.h5",
					"--log", "test.data"});
			PyNNRunner.connectToSocket("conditionubuntu", 7860);
		}

		StringBuilder discourse = new StringBuilder();
		


		for (List<Feature> instance : features) {
			String text = (String) instance.get(0).getValue();
			discourse.append(text);
			discourse.append(' ');
		}
		
		PyNNRunner.sout.println(discourse.toString());

		String returnString = null;
		while (returnString == null) {
			try {
				while (!PyNNRunner.sin.ready()) {
					Thread.sleep(100);
				}
				returnString = PyNNRunner.sin.readLine();
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		List<String> returnValues = new ArrayList<String>();
		for (String instance : returnString.split(" ")) {
			returnValues.add(instance);
		}

		System.out.println("Java got: " + returnString);
		System.out.println("Java sees length: " + returnValues.size());
		return returnValues;
	}

	private String[][] toStrings(List<List<Feature>> features) throws CleartkEncoderException {
		List<List<String>> encodedFeatures = new ArrayList<List<String>>(features.size());
		for (List<Feature> features1 : features) {
			List<NameNumber> nameNumbers = this.featuresEncoder.encodeAll(features1);
			List<String> encodedFeatures1 = new ArrayList<String>();
			for (NameNumber nameNumber : nameNumbers) {
				encodedFeatures1.add(nameNumber.name);
			}
			encodedFeatures.add(encodedFeatures1);
		}

		String[][] encodedFeaturesArray = new String[encodedFeatures.size()][];
		for (int i = 0; i < encodedFeatures.size(); i++) {
			String[] encodedFeaturesArray1 = encodedFeatures.get(i).toArray(new String[0]);
			encodedFeaturesArray[i] = encodedFeaturesArray1;
		}

		return encodedFeaturesArray;
	}

}