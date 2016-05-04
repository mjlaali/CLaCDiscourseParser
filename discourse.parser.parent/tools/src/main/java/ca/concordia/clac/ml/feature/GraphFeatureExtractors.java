package ca.concordia.clac.ml.feature;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

public class GraphFeatureExtractors {
	
	public static <V, T extends Collection<V>, E> Function<T, Set<V>> getRoots(Graph<V, E> graph){
		return (tokens) -> {
			Set<V> seen = new HashSet<V>();
			Set<V> rootCandidates = new HashSet<>(tokens);
			
			for (V token: tokens){
				if (!seen.contains(token)){
					seen.add(token);
					GraphIterator<V, E> iter = new DepthFirstIterator<>(graph, token);
					while (iter.hasNext()){
						V aChild = iter.next();
						if (seen.contains(aChild)){
							if (!aChild.equals(token))
								rootCandidates.remove(aChild);
							continue;
						}
						seen.add(aChild);
						rootCandidates.remove(aChild);
					}
				}
			}

			return rootCandidates;
		};
	}
}
