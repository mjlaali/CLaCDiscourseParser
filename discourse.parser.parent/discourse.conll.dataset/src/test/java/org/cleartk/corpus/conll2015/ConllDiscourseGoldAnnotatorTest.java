package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ConllDiscourseGoldAnnotatorTest {

	protected JCas jCas;

	@Before
	public void setUp() throws UIMAException, IOException{

		// A collection reader that creates one CAS per file, containing the file's URI
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, new File(ConllJSON.TRIAL_RAW_TEXT_LD), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(new File(ConllJSON.TRIAL_SYNTAX_FILE));
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(new File(ConllJSON.TRIAL_DISCOURSE_FILE));
//		AnalysisEngineDescription syntaxParseTreeReader = AnalysisEngineFactory.createEngineDescription(TreebankGoldAnnotator.class);
		
		for (JCas jCas : SimplePipeline.iteratePipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader)) {
			assertThat(this.jCas).isNull();
			this.jCas = jCas;
		}
	}
	
	@Test
	public void whenReadingTrialDataSetThen29DiscourseRelationsAreAddedToTheDocument(){
		assertThat(JCasUtil.select(jCas, DiscourseRelation.class)).hasSize(29);
	}
	
	@Test
	public void givenTheSecondDiscourseRelationInTrialDataSetWhenReadingThenItIsProperlyInitialized(){
		Iterator<DiscourseRelation> iterRelations = JCasUtil.select(jCas, DiscourseRelation.class).iterator();
		iterRelations.next();
		DiscourseRelation secondRelation = iterRelations.next();
		
		FSArray arguments = secondRelation.getArguments();
		assertThat(secondRelation.getRelationType()).isEqualTo(RelationType.Implicit.toString());
		assertThat(arguments.size()).isEqualTo(2);
		assertThat(secondRelation.getDiscourseConnective()).isNull();
		assertThat(secondRelation.getDiscourseConnectiveText()).isEqualTo("specifically");

		DiscourseArgument arg1 = (DiscourseArgument) arguments.get(0);
		assertThat(arg1.getArgumentType()).isEqualTo(ArgType.Arg1.toString());
		DiscourseArgument arg2 = (DiscourseArgument) arguments.get(1);
		assertThat(arg2.getArgumentType()).isEqualTo(ArgType.Arg2.toString());
		
		String arg1Text = "The Kemper Corp. unit and other critics complain that program trading causes wild swings in stock prices, such as on Tuesday and on Oct. 13 and 16, and has increased chances for market crashes";
		assertThat(arg1.getCoveredText()).isEqualTo(arg1Text);

		String arg2Text = "Over the past nine months, several firms, including discount broker Charles Schwab & Co. and Sears, Roebuck & Co. 's Dean Witter Reynolds Inc. unit, have attacked program trading as a major market evil";
		assertThat(arg2.getCoveredText()).isEqualTo(arg2Text);
	}

	@Test
	public void givenTheFirstDiscourseRelationInTrialDataSetWhenReadingThenItIsProperlyInitialized(){
		DiscourseRelation firstRelation = JCasUtil.select(jCas, DiscourseRelation.class).iterator().next();
		
		FSArray arguments = firstRelation.getArguments();
		assertThat(firstRelation.getRelationType()).isEqualTo(RelationType.Explicit.toString());
		assertThat(arguments.size()).isEqualTo(2);
		assertThat(firstRelation.getDiscourseConnective()).isNotNull();
		String dcText = "also";
		assertThat(firstRelation.getDiscourseConnective().getCoveredText()).isEqualTo(dcText);
		assertThat(firstRelation.getDiscourseConnective().getDiscourseRelation()).isEqualTo(firstRelation);
		assertThat(firstRelation.getDiscourseConnectiveText()).isEqualTo(firstRelation.getDiscourseConnective().getCoveredText());

		DiscourseArgument arg1 = (DiscourseArgument) arguments.get(0);
		assertThat(arg1.getArgumentType()).isEqualTo(ArgType.Arg1.toString());
		DiscourseArgument arg2 = (DiscourseArgument) arguments.get(1);
		assertThat(arg2.getArgumentType()).isEqualTo(ArgType.Arg2.toString());
		
		String arg1Text = "Kemper Financial Services Inc., charging that program trading is ruining the stock market, cut off four big Wall Street firms from doing any of its stock-trading business";
		assertThat(arg1.getCoveredText()).isEqualTo(arg1Text);

		FSArray tokens = arg2.getTokens();
		for (int i = 0; i < tokens.size(); i++){
			Token token = (Token) tokens.get(i);
			assertThat(token.getCoveredText()).isNotEqualTo(dcText);
		}
	}

	@Test
	public void giveAJCasWithDiscourseRelationWhenSaveInXMIFileAndLoadThenItContainsDiscourseRelations() throws IOException, SAXException, UIMAException{
		File xmiFile = new File("outputs/test/temp/withDiscourse.xmi");
		xmiFile.getParentFile().mkdirs();
		
		CasIOUtil.writeXmi(jCas, xmiFile);
		
		FileInputStream inputStream = new FileInputStream(xmiFile);
		JCas newJCas = JCasFactory.createJCas();
		XmiCasDeserializer.deserialize(inputStream, newJCas.getCas());
		inputStream.close();
		
		assertThat(JCasUtil.select(jCas, DiscourseRelation.class).size()).isGreaterThan(0);
		assertThat(JCasUtil.select(newJCas, DiscourseRelation.class)).hasSize(JCasUtil.select(jCas, DiscourseRelation.class).size());
	}
	
	@Test
	public void whenReadingDiscourseAnnotationsThenTheNumberOfDCAreEqualToTheNumberOfExplicitRelations(){
		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(jCas, DiscourseRelation.class);
		int explicitRelCnt = 0;
		for (DiscourseRelation discourseRelation: discourseRelations){
			if (discourseRelation.getRelationType().equals(RelationType.Explicit.toString())){
				++explicitRelCnt;
			}
		}
		
		int dcCount = JCasUtil.select(jCas, DiscourseConnective.class).size();
		assertThat(dcCount).isEqualTo(explicitRelCnt);
	}
	
	@Test
	public void whenReadingDiscourseAnnotationsThenTheNumberOfArgumentAreTwiceToTheNumberOfDiscourseRelations(){
		int relationCount = JCasUtil.select(jCas, DiscourseRelation.class).size();
		int argumentCount = JCasUtil.select(jCas, DiscourseArgument.class).size();
		assertThat(relationCount * 2).isEqualTo(argumentCount);
	}
	
	@Test
	public void whenReadingDiscourseAnnotationThenEveryDCHasOneRelation(){
		Collection<DiscourseConnective> discourseConnectives = JCasUtil.select(jCas, DiscourseConnective.class);
		
		for (DiscourseConnective discourseConnective: discourseConnectives){
			DiscourseRelation selectDiscourseRelation = selectDiscourseRelation(discourseConnective);
			assertThat(selectDiscourseRelation).isNotNull();
			assertThat(selectDiscourseRelation.getDiscourseConnective()).isEqualTo(discourseConnective);
		}
	}
	
	public static DiscourseRelation selectDiscourseRelation(DiscourseConnective discourseConnective) {
		List<DiscourseRelation> discourseRelations = JCasUtil.selectCovering(DiscourseRelation.class, discourseConnective);
		List<Token> connectiveTokenList = TokenListTools.convertToTokens(discourseConnective);
		DiscourseRelation selectedRelation = null;
		for (DiscourseRelation discourseRelation: discourseRelations){
			DiscourseConnective relationConnective = discourseRelation.getDiscourseConnective();
			if (relationConnective == null)	//it is an implicit relation
				continue;
			List<Token> relationConnectiveTokenList = TokenListTools.convertToTokens(relationConnective);
			if (TokenListTools.isEqualTokenList(connectiveTokenList, relationConnectiveTokenList)){
				selectedRelation = discourseRelation;
				break;
			}
		}
		
		return selectedRelation;
	}

}
