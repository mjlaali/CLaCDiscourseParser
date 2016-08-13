package ca.concordia.clac.penn;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.cleartk.util.treebank.TreebankFormatParser;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class TextExtractor {
	public interface Options{
		@Option(
				shortName = "i",
				longName = "inputDataset", 
				description = "Specify the input directory")
		public String getInputDataset();
		
		@Option(
				shortName = "o",
				longName = "outputDir",
				description = "Specify the output directory to stores extracted texts")
		public String getOutputDir();
	}
	
	public static void main(String[] args) throws IOException {
		Options options = CliFactory.parseArguments(Options.class, args);
		
		File inputDirectory = new File(options.getInputDataset());
		File outputDirectory = new File(options.getOutputDir());
		
		for (File f: inputDirectory.listFiles()){
			System.out.println(f.getName());
			String pennTreebankText = FileUtils.readFileToString(f);
			String text = TreebankFormatParser.inferPlainText(pennTreebankText);
			FileUtils.writeStringToFile(new File(outputDirectory, f.getName()), text);
		}
		
	}
}
