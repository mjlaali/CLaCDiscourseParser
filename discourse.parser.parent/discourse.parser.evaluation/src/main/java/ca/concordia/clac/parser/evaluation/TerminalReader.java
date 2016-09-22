package ca.concordia.clac.parser.evaluation;

import java.io.IOException;
import java.util.Scanner;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TerminalReader extends JCasCollectionReader_ImplBase{

	private Scanner scanner = new Scanner(System.in);
	private String line;
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		line = scanner.nextLine();
		return line.length() > 0;
	}

	@Override
	public Progress[] getProgress() {
		return null;
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		if (line == null)
			hasNext();
		
		jCas.setDocumentText(line);

		DocumentMetaData documentMetaData = DocumentMetaData.create(jCas);
		documentMetaData.setDocumentId("Terminal");
		jCas.setDocumentLanguage("en");
	}

}
