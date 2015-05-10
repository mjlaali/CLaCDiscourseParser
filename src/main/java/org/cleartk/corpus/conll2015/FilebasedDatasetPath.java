package org.cleartk.corpus.conll2015;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;


public class FilebasedDatasetPath implements DatasetPath{

	private String explicitRelSystemOutFile = "";
	private String implicitRelSystemOutFile = "";
	private String xmiOutputDir = "";
	private String modelDir = "";
	private String reportFile = "";
	private String jsonFile = "";
	private String rawTextFld = "";
	private String discourseGoldAnnotationFile = "";
	private String syntaxAnnotationFile = "";
	private String baseFld = "";
	private String mode = "";

	@Override
	public String getExplicitRelSystemOutFile() {
		return explicitRelSystemOutFile;
	}

	@Override
	public String getImplicitRelSystemOutFile() {
		return implicitRelSystemOutFile;
	}

	@Override
	public String getXmiOutDir() {
		return xmiOutputDir;
	}

	@Override
	public String getModelDir() {
		return modelDir;
	}

	@Override
	public String getReportFile() {
		return reportFile;
	}

	@Override
	public String getJsonFile() {
		return jsonFile;
	}

	@Override
	public String getRawTextsFld() {
		return rawTextFld;
	}

	@Override
	public String getDiscourseGoldAnnotationFile() {
		return discourseGoldAnnotationFile;
	}

	@Override
	public String getSyntaxAnnotationFlie() {
		return syntaxAnnotationFile;
	}

	@Override
	public String getBaseFld() {
		return baseFld;
	}

	@Override
	public String getMode() {
		return mode;
	}
	
	public static DatasetPath constructADatasetPath(File file) throws FileNotFoundException{
		XStream xstream = new XStream(new StaxDriver());
		FilebasedDatasetPath newJoe = (FilebasedDatasetPath)xstream.fromXML(new FileReader(file));
		return newJoe;
	}
	
	public void toXml(File file) throws IOException{
		XStream xstream = new XStream(new StaxDriver());
		xstream.toXML(this, new FileWriter(file));
	}

	
	public static void main(String[] args) throws IOException {
		new FilebasedDatasetPath().toXml(new File("outputs/test.xml"));
	}

}
