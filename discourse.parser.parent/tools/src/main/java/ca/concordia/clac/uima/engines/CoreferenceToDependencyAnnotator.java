package ca.concordia.clac.uima.engines;

import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getDependencyGraph;
import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.jgrapht.DirectedGraph;

import ca.concordia.clac.util.graph.LabeledEdge;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class CoreferenceToDependencyAnnotator extends JCasAnnotator_ImplBase{
	public static final String COREFF_DEPENDENCY_TYPE = "COREF-";

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(CoreferenceToDependencyAnnotator.class);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
//		try {
//			super.process(aJCas);
//		} catch (Exception e) {
//			System.err.println("Count not add coreference to " + Tools.getDocName(aJCas));
//			e.printStackTrace();
//			return;
//		}
		Collection<CoreferenceChain> corefs = JCasUtil.select(aJCas, CoreferenceChain.class);
		Map<CoreferenceLink, Collection<Token>> coreferenceLinkToTokens = JCasUtil.indexCovered(aJCas, CoreferenceLink.class, Token.class);
		DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph = getDependencyGraph(aJCas);
		
		for (CoreferenceChain coref: corefs){
			List<Set<Token>> linkRoots = new ArrayList<>();
			List<CoreferenceLink> links = coref.links();
			for (CoreferenceLink link: links){
				Collection<Token> linkTokens = coreferenceLinkToTokens.get(link);
				Set<Token> roots = getRoots(dependencyGraph).apply(linkTokens);
				linkRoots.add(roots);
			}
			
			for (int i = 0; i < linkRoots.size(); i++){
				for (int j = i + 1; j < linkRoots.size(); j++){
					for (Token aRoot: linkRoots.get(i))
						for (Token anotherRoot: linkRoots.get(j)){
							Dependency dependency = null;
							dependency = new Dependency(aJCas);
							dependency.setGovernor(aRoot);
							dependency.setDependent(anotherRoot);
							dependency.setDependencyType(COREFF_DEPENDENCY_TYPE + links.get(i).getReferenceType());
							dependency.addToIndexes();
							
							dependency = new Dependency(aJCas);
							dependency.setGovernor(anotherRoot);
							dependency.setDependent(aRoot);
							dependency.setDependencyType(COREFF_DEPENDENCY_TYPE + links.get(j).getReferenceType());
							dependency.addToIndexes();
						}
				}
			}
		}
	}

}
