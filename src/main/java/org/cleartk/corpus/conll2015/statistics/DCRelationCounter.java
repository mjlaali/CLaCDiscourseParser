package org.cleartk.corpus.conll2015.statistics;

import ir.laali.tools.ds.DSManagment;
import ir.laali.tools.ds.DSPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
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
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

public class DCRelationCounter extends JCasAnnotator_ImplBase{
	public static final String PARAM_DC_RELATION_CNT_FILE = "PARAM_DC_RELATION_CNT_FILE";
	public static final String DC_RELATION_CNT_FILE_DESCRIPTION = "Specify the output dc relation count file.";
	
	@ConfigurationParameter(
			name = PARAM_DC_RELATION_CNT_FILE,
			description = DC_RELATION_CNT_FILE_DESCRIPTION,
			mandatory = true)
	private String dcRealtionCntFile;
	private Set<String> dcHeads;
	private Map<String, Map<String, Integer>> dcRelationCnt = new TreeMap<String, Map<String,Integer>>();

	public static AnalysisEngineDescription getDescription(String outptufile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DCRelationCounter.class, PARAM_DC_RELATION_CNT_FILE, outptufile);
	}
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			dcHeads = new TreeSet<>(FileUtils.readLines(new File(DiscourseConnectivesList.DISCOURSE_CONNECTIVES_LIST_FILE)));
			new File(dcRealtionCntFile).getParentFile().mkdirs();
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<DiscourseRelation> dcRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
		
		for (DiscourseRelation dcRelation: dcRelations){
			DiscourseConnective dc = dcRelation.getDiscourseConnective();
			if (dc == null)
				continue;
			String dcText = TokenListTools.getTokenListText(dc).toLowerCase();
			String bestMatch = "";
			for (String aDcHead: dcHeads){
				if (dcText.contains(aDcHead) && bestMatch.length() < aDcHead.length()){
					bestMatch = aDcHead;
				}
			}
			
			if (bestMatch.length() == 0){
				System.err.println("DCRelationCounter.process(): No matching found for the <" + dcText + ">");
			}
			DSManagment.incValue(dcRelationCnt, bestMatch, dcRelation.getSense());
		}
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		PrintStream output;
		try {
			output = new PrintStream(dcRealtionCntFile);
		} catch (FileNotFoundException e) {
			throw new AnalysisEngineProcessException(e);
		}
		DSPrinter.printTable("DC Relation Cnt", dcRelationCnt.entrySet(), output);
		output.close();
	}
	
	public static void main(String[] args) throws UIMAException, IOException {
		System.out.println("DatasetStatistics.main()");
		DatasetPath dataSet = new ConllDataset("dev");
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataSet, String.format(DatasetStatistics.XMI_DIR, dataSet.getMode()));
//		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(DCRelationCounter.getDescription("data/analysisResults/dcRelationCnt.txt"));
		System.out.println("DCRelationCounter.main(): Done!");
	}


}
