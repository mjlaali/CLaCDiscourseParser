package ca.concordia.clac.uima.test.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class DocumentFactory {
	private PennTreeToJCasConverter converter;
	private MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null, (String)null);
	private MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null, null, (String)null);

	public DocumentFactory() {
		converter = new PennTreeToJCasConverter(
				posMappingProvider, 
				constituentMappingProvider);
	}

	public JCas createADcoument(String parseTree) throws UIMAException{
		JCas aJCas = JCasFactory.createJCas();
		posMappingProvider.configure(aJCas.getCas());
		constituentMappingProvider.configure(aJCas.getCas());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		String sent = PennTreeUtils.toText(parsePennTree);
		
		aJCas.setDocumentText(sent);
		aJCas.setDocumentLanguage("en");
		Sentence aSentence = new Sentence(aJCas, 0, sent.length());
		aSentence.addToIndexes();
		int pos = 0;
		
		for (String tokenStr: sent.split(" ")){
			Token token = new Token(aJCas, pos, pos + tokenStr.length());
			token.addToIndexes();
			pos += tokenStr.length() + 1;
		}
		converter.setCreatePosTags(true);
		converter.convertPennTree(aSentence, parsePennTree);
		return aJCas;
	}
	
	public JCas addDependency(JCas aJCas, String dependencies) throws UIMAException{
		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
		String[] splitDependencies = dependencies.split("\\) ");
		for (String aDependency: splitDependencies){
			String[] dependencyTokens = aDependency.split("[(,)]");
			if (dependencyTokens.length != 3)
				throw new RuntimeException();
			
			Dependency dependency = new Dependency(aJCas);
			dependency.setDependencyType(dependencyTokens[0]);
			dependency.setGovernor(getIndex(dependencyTokens[1], tokens));
			dependency.setDependent(getIndex(dependencyTokens[2], tokens));
			dependency.addToIndexes();
		}
		
		return aJCas;
	}

	private Token getIndex(String tokenString, List<Token> tokens) {
		int idx = Integer.parseInt(tokenString.substring(tokenString.lastIndexOf("-") + 1)) - 1;
		if (idx < 0)
			return null;
		String tokenText = tokenString.substring(0, tokenString.lastIndexOf("-")).trim();
		Token token = tokens.get(idx);
		if (!token.getCoveredText().equals(tokenText))
			throw new RuntimeException();
		return token;
	}

	public JCas createADcoument(String parseTree, String dependencies) throws UIMAException {
		JCas aJCas = createADcoument(parseTree);
		return addDependency(aJCas, dependencies);
	}

}
