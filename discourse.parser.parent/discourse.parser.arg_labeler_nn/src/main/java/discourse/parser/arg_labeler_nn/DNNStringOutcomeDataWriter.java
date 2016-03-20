package discourse.parser.arg_labeler_nn;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.ml.encoder.features.StringEncoder;
import org.cleartk.ml.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.ml.jar.SequenceDataWriter_ImplBase;


/**
 * This Class is designed to replace the MalletCrfStringOutcomeDataWriter as 
 * written in the ClearTK Framework. As such it builds on the same implementations
 * that the ClearTK framework provides.
 * 
 * The code exposes out the data and acts as a wrapper around code written in Python
 * in order to communicate with the new SequenceClassifier that is created for this
 * project
 * 
 * @author Sohail Hooda
 */
public class DNNStringOutcomeDataWriter extends
SequenceDataWriter_ImplBase<DNNStringOutcomeClassifierBuilder, List<NameNumber>, String, String> {

	public DNNStringOutcomeDataWriter(File outputDirectory) throws FileNotFoundException {
		// TODO Auto-generated method stub
		super(outputDirectory);
		NameNumberFeaturesEncoder nnfe = new NameNumberFeaturesEncoder();
	    nnfe.addEncoder(new StringEncoder());
	    this.setFeaturesEncoder(nnfe);
	    this.setOutcomeEncoder(new StringToStringOutcomeEncoder());
	}

	@Override
	protected void writeEncoded(List<NameNumber> features, String outcome) throws CleartkProcessingException {
		// TODO Auto-generated method stub
		for (NameNumber nameNumber : features) {
			this.trainingDataWriter.print(nameNumber.name);
		    this.trainingDataWriter.print(" ");
		}
		this.trainingDataWriter.print(outcome);
		this.trainingDataWriter.println();
	}

	@Override
	protected void writeEndSequence() {
		// TODO Auto-generated method stub
		this.trainingDataWriter.println();
		
	}

	@Override
	protected DNNStringOutcomeClassifierBuilder newClassifierBuilder() {
		// TODO Auto-generated method stub
		return new DNNStringOutcomeClassifierBuilder();
	}

	
	

}