package ca.concordia.clac.ml.feature;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DependencyFeatureExtractor {
	
	public static Function<JCas, Map<Token, Dependency>> getDependantDependency(){
		
		return (jCas) ->{
			Collection<Dependency> dependencies = JCasUtil.select(jCas, Dependency.class);
			Map<Token, Dependency> graph = new HashMap<>(); 
			for (Dependency dependency: dependencies){
				graph.put(dependency.getDependent(), dependency);
			}
			return graph;
		};
	}
	
	public static Function<Annotation, Token> getHead(Map<Token, Dependency> dependantToDependencies,
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
