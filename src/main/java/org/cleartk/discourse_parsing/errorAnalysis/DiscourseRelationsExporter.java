package org.cleartk.discourse_parsing.errorAnalysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.cleartk.corpus.conll2015.type.SentenceWithSyntax;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

public class DiscourseRelationsExporter extends JCasAnnotator_ImplBase{
	public static final String FILE_NAME_CONVENTION = "%s_%s_%s.txt";
	public static final String PARAM_EXPLICIT_FILE = "PARAM_EXPLICIT_FILE";
	public static final String PARAM_IMPLICIT_FILE = "PARAM_IMPLICIT_FILE";

	@ConfigurationParameter(
			name = PARAM_EXPLICIT_FILE,
			description = "The exportFile for explicit relations",
			mandatory = true)
	private String explicitRelFilePath;

	@ConfigurationParameter(
			name = PARAM_IMPLICIT_FILE,
			description = "The implicit for explicit relations",
			mandatory = true)
	private String implicitRelFilePath;

	public static AnalysisEngineDescription getDescription(String explicitFilePath, String implicitFilePath) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DiscourseRelationsExporter.class, 
				PARAM_EXPLICIT_FILE, 
				explicitFilePath,
				PARAM_IMPLICIT_FILE,
				implicitFilePath);
	}
	
	private PrintStream pwExplicit;
	private PrintStream pwImplicit;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			pwExplicit = new PrintStream(explicitRelFilePath);
			pwImplicit = new PrintStream(implicitRelFilePath);
			
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e);
		}
	}

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		printExplicitRelations(aJCas);
		printImplicitRelations(aJCas);
	}


	private void printImplicitRelations(JCas aJCas)
			throws AnalysisEngineProcessException {
		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
		for (DiscourseRelation discourseRelation: discourseRelations){
			if (discourseRelation.getRelationType().equals(RelationType.Implicit.toString())){
				printDiscourseRelation(pwImplicit, aJCas, discourseRelation);
			}
		}
	}
	
	private void printExplicitRelations(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<DiscourseConnective> discourseConnectives = JCasUtil.select(aJCas, DiscourseConnective.class);
		for (DiscourseConnective discourseDonnective: discourseConnectives){
			printDiscourseRelation(pwExplicit, aJCas, discourseDonnective);
		}
	}

	public static void printDiscourseRelation(PrintStream pw, JCas aJCas, DiscourseConnective discourseConnective) throws AnalysisEngineProcessException{
		DiscourseRelation discourseRelation = discourseConnective.getDiscourseRelation();
		if (discourseRelation == null)
			return;
		
		printDiscourseRelation(pw, aJCas, discourseRelation);
	}
	
	public static void printDiscourseRelation(PrintStream pw, JCas aJCas, DiscourseRelation discourseRelation) throws AnalysisEngineProcessException{
		pw.println(Tools.getDocName(aJCas));

		List<SentenceWithSyntax> relationSentences = JCasUtil.selectCovering(SentenceWithSyntax.class, discourseRelation);
		if (relationSentences.size() == 0){
			relationSentences = JCasUtil.selectCovered(SentenceWithSyntax.class, discourseRelation);
		}

		if (relationSentences.size() == 0)
			pw.println("No sentences covered the relations");
		else{
			pw.println("Covered Sentences:" + relationSentences.size());
			for (SentenceWithSyntax sent: relationSentences){
				pw.println(sent.getCoveredText());
				pw.println(sent.getSyntaxTree().replace('(', '[').replace(')', ']'));
			}
		}
		
		pw.println("Whole Relation: " + TokenListTools.getTokenListText(discourseRelation));
		pw.println("Arg1: " + TokenListTools.getTokenListText(discourseRelation.getArguments(0)));
		pw.println("Arg2: " + TokenListTools.getTokenListText(discourseRelation.getArguments(1)));
		
		DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
		int startIdx = -1;
		if (discourseConnective != null){
			startIdx = ((ConllToken) TokenListTools.convertToTokens(discourseConnective).get(0)).getDocumentOffset();
		}
		
		String dcTokenListText = discourseConnective == null ? "" : TokenListTools.getTokenListText(discourseConnective);
		String dcText = String.valueOf(discourseRelation.getDiscourseConnectiveText());
		
		pw.println(String.format("Type = <%s>, Sense = <%s>, DC = <%s>, DC TokenList = <%s-%d>", discourseRelation.getRelationType(), 
				discourseRelation.getSense(), dcText, dcTokenListText, startIdx));
		pw.println();
		pw.println("======================================================");
		pw.println();

	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		pwExplicit.close();
	}
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		DatasetPath dataSet = new ConllDataset();
		String fileNameFormat = "outputs/parser/" + FILE_NAME_CONVENTION;
		
		String goldKey = "gold";
		String explicitFile = String.format(fileNameFormat, dataSet.getMode(), goldKey, "explicit");
		String implicitFile = String.format(fileNameFormat, dataSet.getMode(), goldKey, "implicit");
		
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataSet, String.format(DatasetStatistics.XMI_DIR, dataSet.getMode()));
		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(getDescription(explicitFile, implicitFile));
		System.out.println("DiscourseRelationsExporter.main()");
	}

}
