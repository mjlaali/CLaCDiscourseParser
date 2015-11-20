package test.dkpro;

import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class PennTreeBankReader {

	private PennTreeToJCasConverter converter;
	
	@Test
	public void whenReadingPennTreeBankStringThenAnnotationsAreSetCorrectly() throws UIMAException{
		MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null, "en");
		MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null, null, "en");
		converter = new PennTreeToJCasConverter(
				posMappingProvider, 
				constituentMappingProvider);
		
		String pennTree = "(TOP (S (NP (NN it)) (VP (VBZ is) (NP (DT a) (NN test))) (. .)))";
		String text = "it is a test .";
		
        PennTreeNode root = PennTreeUtils.parsePennTree(pennTree);
        
        JCas jcas = JCasFactory.createJCas();
        
        posMappingProvider.configure(jcas.getCas());
        constituentMappingProvider.configure(jcas.getCas());
//        StringBuilder sb = new StringBuilder();
        converter.setCreatePosTags(true);
//        converter.convertPennTree(jcas, sb, root);
        jcas.setDocumentText(text);
        Sentence sent = new Sentence(jcas, 0, text.length());
		sent.addToIndexes();
		int start = 0;
		for (String token: text.split(" ")){
			new Token(jcas, start, start + token.length()).addToIndexes();
			start += token.length() + 1;
		}
		
        converter.convertPennTree(sent, root);
        jcas.setDocumentLanguage("en");
        
        Collection<Constituent> select = JCasUtil.select(jcas, Constituent.class);
        System.out.println(select.size());
        for (Constituent constituent: select){
        	System.out.println(constituent.getConstituentType() + ": " + constituent.getCoveredText());
        }
        
	}
}
