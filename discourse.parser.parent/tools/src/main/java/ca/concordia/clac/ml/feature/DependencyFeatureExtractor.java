package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.getText;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import ca.concordia.clac.util.graph.LabeledEdge;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DependencyFeatureExtractor {

	public static Function<Dependency, String> dependencyToString(){
		return dependency -> dependency.getDependencyType() + "(" + getText().apply(dependency.getGovernor()) + "-" + getText().apply(dependency.getDependent()) + ")";
	}
	
	public static Map<Token, Dependency> getDependantDependencies(JCas jCas){

		Collection<Dependency> dependencies = JCasUtil.select(jCas, Dependency.class);
		Map<Token, Dependency> graph = new HashMap<>(); 
		for (Dependency dependency: dependencies){
			Token dependent = dependency.getDependent();
			if (graph.containsKey(dependent)) {
				throw new RuntimeException("Dependency graph is not a tree!");
			}
			graph.put(dependent, dependency);
		}
		return graph;
	}

	public static DirectedGraph<Token, LabeledEdge<Dependency>> getDependencyGraph(JCas jCas){
		Collection<Dependency> dependencies = JCasUtil.select(jCas, Dependency.class);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		DirectedGraph<Token, LabeledEdge<Dependency>> graph = new DefaultDirectedGraph(LabeledEdge.class); 
		for (Dependency dependency: dependencies){
			if (dependency.getGovernor() != null)
				graph.addVertex(dependency.getGovernor());
			if (dependency.getDependent() != null)
				graph.addVertex(dependency.getDependent());

			if (dependency.getGovernor() != null && dependency.getDependent() != null){
				graph.addEdge(dependency.getGovernor(), dependency.getDependent(), new LabeledEdge<Dependency>(dependency));
			} 
		}
		return graph;

	}

	public static Function<Annotation, Token> getHead(final Map<Token, Dependency> dependantToDependencies,
			Function<Annotation, ? extends Collection<Token>> getTokens){
		return (ann) -> {
			if (ann instanceof Token)
				return (Token) ann;

			if (ann instanceof Constituent){
				Set<Token> tokens = new HashSet<>(getTokens.apply(ann));
				Set<Token> remaining = new HashSet<>(tokens);
				Token aToken = tokens.iterator().next();
				Token head = aToken;
				while (aToken != null && tokens.contains(aToken)){
					remaining.remove(aToken);
					head = aToken;
					Dependency dependency = dependantToDependencies.get(aToken);
					if (dependency == null){	//not valid token. it does not attach to anyone!
						if (remaining.size() > 0)
							aToken = remaining.iterator().next();
						else{
							head = null;
							break;
						}
					} else {
						aToken = dependency.getGovernor();
					}
				}
				return head;
			}

			return null;

		};
	}

	
}
