package ca.concordia.clac.ml.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.Test;

import ca.concordia.clac.util.graph.LabeledEdge;


public class GraphTExample {

	LabeledEdge<String> edge;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	DirectedGraph<String, LabeledEdge<String>> graph = new DefaultDirectedGraph(LabeledEdge.class);
	
	@Test
	public void duplicateVertex(){
		graph.addVertex("test");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void addEdgeWithoutAddingVertex() {
		graph.addEdge("a", "b");
	}
	
	@Test
	public void shortestPath(){
		graph.addVertex("a");
		graph.addVertex("b");
		graph.addVertex("c");
		
		graph.addEdge("a", "b", new LabeledEdge<>("ab"));
		graph.addEdge("b", "c", new LabeledEdge<>("bc"));
		
		List<LabeledEdge<String>> findPathBetween = DijkstraShortestPath.findPathBetween(graph, "a", "c");
		
		String toStr =
				findPathBetween.stream().map(LabeledEdge::getLabel).map(Object::toString).collect(Collectors.joining("-"));
		System.out.println(toStr);
	}
	
	@Test
	public void checkTheOrder(){
		graph.addVertex("a");
		graph.addVertex("b");
		graph.addVertex("c");
		
		graph.addEdge("a", "b", new LabeledEdge<>("ab"));
		graph.addEdge("a", "c", new LabeledEdge<>("ab"));
		graph.addEdge("b", "c", new LabeledEdge<>("bc"));

		
		Set<LabeledEdge<String>> outgoingEdge = graph.edgesOf("a");
		System.out.println(outgoingEdge.getClass().getName());
		System.out.println(outgoingEdge instanceof ArrayList);
		
	}
	
	@Test
	public void convertToUndirectedGraph(){
		graph.addVertex("a");
		graph.addVertex("b");
		graph.addVertex("c");
		
		graph.addEdge("a", "b", new LabeledEdge<>("ab"));
		graph.addEdge("a", "c", new LabeledEdge<>("ab"));
		graph.addEdge("b", "c", new LabeledEdge<>("bc"));

		AsUndirectedGraph<String, LabeledEdge<String>> asUndirectedGraph = new AsUndirectedGraph<String, LabeledEdge<String>>(graph);
		List<LabeledEdge<String>> findPathBetween = DijkstraShortestPath.findPathBetween(asUndirectedGraph, "c", "a");
		
		String toStr =
				findPathBetween.stream().map(LabeledEdge::getLabel).map(Object::toString).collect(Collectors.joining("-"));
		System.out.println(toStr);

	}
	
	
	@Test
	public void depthFirstSearch(){
		graph.addVertex("a");
		graph.addVertex("b");
		graph.addVertex("c");
		graph.addVertex("d");
		
		graph.addEdge("a", "b", new LabeledEdge<>("ab"));
		graph.addEdge("b", "d", new LabeledEdge<>("bc"));
		graph.addEdge("b", "c", new LabeledEdge<>("ab"));
		
		DepthFirstIterator<String, LabeledEdge<String>> iter = new DepthFirstIterator<>(graph);
		
		while (iter.hasNext()){
			System.out.println(iter.next());
		}
	}
}
