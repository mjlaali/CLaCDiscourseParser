package ca.concordia.clac.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.traverse.ClosestFirstIterator;

public final class ShortestPathForMultiDestination<V, E>
{
    private GraphPath<V, E> path;

    /**
     * Creates and executes a new DijkstraShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     */
    public ShortestPathForMultiDestination(Graph<V, E> graph,
        V startVertex,
        Set<V> endVertex)
    {
        this(graph, startVertex, endVertex, Double.POSITIVE_INFINITY);
    }

    /**
     * Creates and executes a new DijkstraShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     * @param radius limit on weighted path length, or Double.POSITIVE_INFINITY
     * for unbounded search
     */
    public ShortestPathForMultiDestination(
        Graph<V, E> graph,
        V startVertex,
        Set<V> endVertex,
        double radius)
    {
    	for (V v: endVertex)
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException(
                "graph must contain the end vertex");
        }

        ClosestFirstIterator<V, E> iter =
            new ClosestFirstIterator<V, E>(graph, startVertex, radius);

        while (iter.hasNext()) {
            V vertex = iter.next();

            if (endVertex.contains(vertex)) {
                createEdgeList(graph, iter, startVertex, vertex);
                return;
            }
        }

        path = null;
    }

    /**
     * Return the edges making up the path found.
     *
     * @return List of Edges, or null if no path exists
     */
    public List<E> getPathEdgeList()
    {
        if (path == null) {
            return null;
        } else {
            return path.getEdgeList();
        }
    }

    /**
     * Return the path found.
     *
     * @return path representation, or null if no path exists
     */
    public GraphPath<V, E> getPath()
    {
        return path;
    }

    /**
     * Return the weighted length of the path found.
     *
     * @return path length, or Double.POSITIVE_INFINITY if no path exists
     */
    public double getPathLength()
    {
        if (path == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return path.getWeight();
        }
    }

    /**
     * Convenience method to find the shortest path via a single static method
     * call. If you need a more advanced search (e.g. limited by radius, or
     * computation of the path length), use the constructor instead.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     *
     * @return List of Edges, or null if no path exists
     */
    public static <V, E> List<E> findPathBetween(
        Graph<V, E> graph,
        V startVertex,
        Set<V> endVertex)
    {
        ShortestPathForMultiDestination<V, E> alg =
            new ShortestPathForMultiDestination<V, E>(
                graph,
                startVertex,
                endVertex);

        return alg.getPathEdgeList();
    }

    private void createEdgeList(
        Graph<V, E> graph,
        ClosestFirstIterator<V, E> iter,
        V startVertex,
        V endVertex)
    {
        List<E> edgeList = new ArrayList<E>();

        V v = endVertex;

        while (true) {
            E edge = iter.getSpanningTreeEdge(v);

            if (edge == null) {
                break;
            }

            edgeList.add(edge);
            v = Graphs.getOppositeVertex(graph, edge, v);
        }

        Collections.reverse(edgeList);
        double pathLength = iter.getShortestPathLength(endVertex);
        path =
            new GraphPathImpl<V, E>(
                graph,
                startVertex,
                endVertex,
                edgeList,
                pathLength);
    }
}