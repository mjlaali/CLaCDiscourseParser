package ca.concordia.clac.parser.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.DiscourseConnective;

public class ErrorAnalysis extends JCasAnnotator_ImplBase {
	public static final String GOLD_VIEW = "goldView";
	public static final String SYSTEM_VIEW = "systemView";
	
//	private PrintStream output;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			JCas goldView = aJCas.getView(GOLD_VIEW);
			JCas systemView = aJCas.getView(SYSTEM_VIEW);
			evaluateExplicitRelations(goldView, systemView);
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void evaluateExplicitRelations(JCas goldView, JCas systemView) {
		Collection<DiscourseConnective> goldConnectives = JCasUtil.select(goldView, DiscourseConnective.class);
		Collection<DiscourseConnective> systemConnectives = JCasUtil.select(systemView, DiscourseConnective.class);
		
		List<Pair<DiscourseConnective, DiscourseConnective>> incorrectRelations = new ArrayList<>();
		List<DiscourseConnective> notDetected = new ArrayList<>();
		List<DiscourseConnective> invalidConnectives = new ArrayList<>();
		
		Iterator<DiscourseConnective> iterSystemConnective = systemConnectives.iterator();
		
		for (DiscourseConnective aGoldConnective: goldConnectives){
			List<DiscourseConnective> alignedSystemConnectives = findDCInside(iterSystemConnective, invalidConnectives);
			if (alignedSystemConnectives.isEmpty())
				notDetected.add(aGoldConnective);
			
			for (DiscourseConnective aSystemConnective: alignedSystemConnectives){
				if (!hasSameRelation(aGoldConnective, aSystemConnective)){
					incorrectRelations.add(new Pair<DiscourseConnective, DiscourseConnective>(aGoldConnective, aSystemConnective));
				}
			}
		}
		
	}

	private List<DiscourseConnective> findDCInside(Iterator<DiscourseConnective> iterSystemConnective,
			List<DiscourseConnective> invalidConnectives) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean hasSameRelation(DiscourseConnective aGoldConnective, DiscourseConnective aSystemConnective) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
