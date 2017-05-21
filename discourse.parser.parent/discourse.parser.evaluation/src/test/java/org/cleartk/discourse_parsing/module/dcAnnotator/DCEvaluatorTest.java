package org.cleartk.discourse_parsing.module.dcAnnotator;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DefaultDiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.eval.EvaluationConstants;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.uima.engines.ViewAnnotationCopier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DCEvaluatorTest {
	private File outputFld = new File("outputs/test/" + DCEvaluatorTest.class.getSimpleName());
	private File outputFile = new File(outputFld, "PandRIsOne.txt");
	
	@Before
	public void init(){
		if (!outputFld.mkdirs() && !outputFile.exists())
			throw new RuntimeException();
	}
	
	private void testAverageOutput(int sumTotal, int sumSystem, int sumCorrect) throws IOException {
		System.out.println("\n\n=====================");
		System.out.println(FileUtils.readFileToString(outputFile));
		List<String> readLines = FileUtils.readLines(outputFile);
		String lastLine = readLines.get(readLines.size() - 1);
		assertThat(lastLine).isEqualTo(DCEvaluator.convertToString("Average", sumTotal, sumSystem, sumCorrect));
	}

	@Test
	public void whenThePrecisionIsOneAndRecallIsOne() throws UIMAException, IOException{
		JCas aJCas = JCasFactory.createJCas();
		DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
		
		discourseRelationFactory.makeDiscourseRelationFrom(aJCas, new DefaultDiscourseRelationExample(
				"Le Parlement devrait dès lors envoyer un message en ce sens, étant donné qu'une grande majorité des députés le souhaite.", 
				new String[]{"(ROOT (SENT (NP (DET Le) (NC Parlement)) (VN (V devrait)) (MWADV (P dès) (ADV lors)) (VPinf (VN (VINF envoyer)) (NP (DET un) (NC message)) (MWADV (P en) (DET ce) (N sens))) (PUNC ,) (VPpart (VN (VPR étant) (VPP donné)) (Ssub (CS qu') (NP (DET une) (ADJ grande) (NC majorité) (PP (P des) (NP (NC députés)))) (VN (CLO le) (V souhaite)))) (PUNC .)))"}, 
				"Le Parlement devrait dès lors envoyer un message en ce sens,", "une grande majorité des députés le souhaite.", "étant donné qu'", null
				)).addToIndexesRecursively();
		
		SimplePipeline.runPipeline(aJCas,  
				createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, EvaluationConstants.GOLD_VIEW),
				createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, EvaluationConstants.GOLD_VIEW),
				DCEvaluator.getDescription(outputFile));
		
		testAverageOutput(1, 1, 1);
	}
	
	@Test
	public void whenThePrecisionIsFiftyAndRecallIsOne() throws UIMAException, IOException{
		JCas aJCas = JCasFactory.createJCas();
		DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
		
		discourseRelationFactory.makeDiscourseRelationFrom(aJCas, new DefaultDiscourseRelationExample(
				"Le Parlement devrait dès lors envoyer un message en ce sens, étant donné qu'une grande majorité des députés le souhaite.", 
				new String[]{"(ROOT (SENT (NP (DET Le) (NC Parlement)) (VN (V devrait)) (MWADV (P dès) (ADV lors)) (VPinf (VN (VINF envoyer)) (NP (DET un) (NC message)) (MWADV (P en) (DET ce) (N sens))) (PUNC ,) (VPpart (VN (VPR étant) (VPP donné)) (Ssub (CS qu') (NP (DET une) (ADJ grande) (NC majorité) (PP (P des) (NP (NC députés)))) (VN (CLO le) (V souhaite)))) (PUNC .)))"}, 
				"Le Parlement devrait dès lors envoyer un message en ce sens,", "une grande majorité des députés le souhaite.", "étant donné qu'", null
				)).addToIndexesRecursively();
		
		SimplePipeline.runPipeline(aJCas, 
				createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, EvaluationConstants.GOLD_VIEW),
				createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, EvaluationConstants.GOLD_VIEW)
				);
		DiscourseConnective discourseConnective = new DiscourseConnective(aJCas, 0, 2);
		discourseConnective.addToIndexes();
		TokenListTools.initTokenList(discourseConnective, Arrays.asList(new Token(aJCas, 0, 2)));
		
		SimplePipeline.runPipeline(aJCas, 
				DCEvaluator.getDescription(outputFile));
		testAverageOutput(1, 2, 1);
	}
	
	@Test
	public void whenThePrecisionIsOneAndRecallIsFifty() throws UIMAException, IOException{
		JCas aJCas = JCasFactory.createJCas();
		DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
		
		discourseRelationFactory.makeDiscourseRelationFrom(aJCas, new DefaultDiscourseRelationExample(
				"Le Parlement devrait dès lors envoyer un message en ce sens, étant donné qu'une grande majorité des députés le souhaite.", 
				new String[]{"(ROOT (SENT (NP (DET Le) (NC Parlement)) (VN (V devrait)) (MWADV (P dès) (ADV lors)) (VPinf (VN (VINF envoyer)) (NP (DET un) (NC message)) (MWADV (P en) (DET ce) (N sens))) (PUNC ,) (VPpart (VN (VPR étant) (VPP donné)) (Ssub (CS qu') (NP (DET une) (ADJ grande) (NC majorité) (PP (P des) (NP (NC députés)))) (VN (CLO le) (V souhaite)))) (PUNC .)))"}, 
				"Le Parlement devrait dès lors envoyer un message en ce sens,", "une grande majorité des députés le souhaite.", "étant donné qu'", null
				)).addToIndexesRecursively();
		
		SimplePipeline.runPipeline(aJCas, 
				createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, EvaluationConstants.GOLD_VIEW),
				createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, EvaluationConstants.GOLD_VIEW)
				);
		
		JCas goldView  = aJCas.getView(EvaluationConstants.GOLD_VIEW);
		DiscourseConnective discourseConnective = new DiscourseConnective(goldView, 0, 2);
		discourseConnective.addToIndexes();
		TokenListTools.initTokenList(discourseConnective, Arrays.asList(new Token(goldView, 0, 2)));
		
		SimplePipeline.runPipeline(aJCas, 
				DCEvaluator.getDescription(outputFile));
		testAverageOutput(2, 1, 1);
	}


	
	@Test
	public void whenThePrecisionIsZeroAndRecallIsZero() throws UIMAException, IOException{
		JCas aJCas = JCasFactory.createJCas();
		DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
		JCas goldView = aJCas.createView(EvaluationConstants.GOLD_VIEW);
		
		discourseRelationFactory.makeDiscourseRelationFrom(goldView, new DefaultDiscourseRelationExample(
				"Le Parlement devrait dès lors envoyer un message en ce sens, étant donné qu'une grande majorité des députés le souhaite.", 
				new String[]{"(ROOT (SENT (NP (DET Le) (NC Parlement)) (VN (V devrait)) (MWADV (P dès) (ADV lors)) (VPinf (VN (VINF envoyer)) (NP (DET un) (NC message)) (MWADV (P en) (DET ce) (N sens))) (PUNC ,) (VPpart (VN (VPR étant) (VPP donné)) (Ssub (CS qu') (NP (DET une) (ADJ grande) (NC majorité) (PP (P des) (NP (NC députés)))) (VN (CLO le) (V souhaite)))) (PUNC .)))"}, 
				"Le Parlement devrait dès lors envoyer un message en ce sens,", "une grande majorité des députés le souhaite.", "étant donné qu'", null
				)).addToIndexesRecursively();
		aJCas.setDocumentText(goldView.getDocumentText());
		
		SimplePipeline.runPipeline(aJCas, 
				DCEvaluator.getDescription(outputFile));
		testAverageOutput(1, 0, 0);

	}
	
	@Test
	public void whenTheRelationsAreDifferentConnectivesAreConsideredNotAMatch() throws UIMAException, IOException{
		JCas aJCas = JCasFactory.createJCas();
		DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
		
		discourseRelationFactory.makeDiscourseRelationFrom(aJCas, new DefaultDiscourseRelationExample(
				"Le Parlement devrait dès lors envoyer un message en ce sens, étant donné qu'une grande majorité des députés le souhaite.", 
				new String[]{"(ROOT (SENT (NP (DET Le) (NC Parlement)) (VN (V devrait)) (MWADV (P dès) (ADV lors)) (VPinf (VN (VINF envoyer)) (NP (DET un) (NC message)) (MWADV (P en) (DET ce) (N sens))) (PUNC ,) (VPpart (VN (VPR étant) (VPP donné)) (Ssub (CS qu') (NP (DET une) (ADJ grande) (NC majorité) (PP (P des) (NP (NC députés)))) (VN (CLO le) (V souhaite)))) (PUNC .)))"}, 
				"Le Parlement devrait dès lors envoyer un message en ce sens,", "une grande majorité des députés le souhaite.", "étant donné qu'", "Reason"
				)).addToIndexesRecursively();

		SimplePipeline.runPipeline(aJCas,  
				createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, EvaluationConstants.GOLD_VIEW),
				createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, EvaluationConstants.GOLD_VIEW));
		JCasUtil.select(aJCas, DiscourseConnective.class).iterator().next().setSense("Cause");

		SimplePipeline.runPipeline(aJCas, 
				DCEvaluator.getDescription(outputFile, null, true));
		testAverageOutput(1, 1, 0);
	}
}
