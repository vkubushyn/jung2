/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.graph.Graph;
import edu.uci.ics.graph.util.EdgeType;
import edu.uci.ics.graph.util.Pair;
import edu.uci.ics.jung.algorithms.generators.EvolvingGraphGenerator;


/**
 * <p>Simple evolving scale-free random graph generator. At each time
 * step, a new vertex is created and is connected to existing vertices
 * according to the principle of "preferential attachment", whereby 
 * vertices with higher degree have a higher probability of being 
 * selected for attachment.</p>
 * 
 * <p>At a given timestep, the probability <code>p</code> of creating an edge
 * between an existing vertex <code>v</code> and the newly added vertex is
 * <pre>
 * p = (degree(v) + 1) / (|E| + |V|);
 * </pre>
 * 
 * <p>where <code>|E|</code> and <code>|V|</code> are, respectively, the number 
 * of edges and vertices currently in the network (counting neither the new
 * vertex nor the other edges that are being attached to it).</p>
 * 
 * <p>Note that the formula specified in the original paper
 * (cited below) was
 * <pre>
 * p = degree(v) / |E|
 * </pre>
 * </p>
 * 
 * <p>However, this would have meant that the probability of attachment for any existing
 * isolated vertex would be 0.  This version uses Lagrangian smoothing to give
 * each existing vertex a positive attachment probability.</p>
 * 
 * <p>The graph created may be either directed or undirected (controlled by a constructor
 * parameter); the default is undirected.  
 * If the graph is specified to be directed, then the edges added will be directed
 * from the newly added vertex u to the existing vertex v, with probability proportional to the 
 * indegree of v (number of edges directed towards v).  If the graph is specified to be undirected,
 * then the (undirected) edges added will connect u to v, with probability proportional to the 
 * degree of v.</p> 
 * 
 * <p>The <code>parallel</code> constructor parameter specifies whether parallel edges
 * may be created.</p>
 * 
 * @see "A.-L. Barabasi and R. Albert, Emergence of scaling in random networks, Science 286, 1999."
 * @author Scott White
 * @author Joshua O'Madadhain
 * @author Tom Nelson - adapted to jung2
 */
public class BarabasiAlbertGenerator<V,E> implements EvolvingGraphGenerator<V,E> {
    private Graph<V, E> mGraph = null;
    private int mNumEdgesToAttachPerStep;
    private int mElapsedTimeSteps;
    private Random mRandom;
    protected List<V> vertex_index;
    protected int init_vertices;
    protected Map<V,Integer> index_vertex;
    protected boolean directed;
    protected boolean parallel;
    protected Factory<Graph<V,E>> graphFactory;
    protected Factory<V> vertexFactory;
    protected Factory<E> edgeFactory;
    
    /**
     * Constructs a new instance of the generator.
     * @param init_vertices     number of unconnected 'seed' vertices that the graph should start with
     * @param numEdgesToAttach the number of edges that should be attached from the
     * new vertex to pre-existing vertices at each time step
     * @param directed  specifies whether the graph and edges to be created should be directed or not
     * @param parallel  specifies whether the algorithm permits parallel edges
     * @param seed  random number seed
     */
    public BarabasiAlbertGenerator(Factory<Graph<V,E>> graphFactory,
    		Factory<V> vertexFactory, Factory<E> edgeFactory, 
    		int init_vertices, int numEdgesToAttach, 
            boolean directed, boolean parallel, int seed, Set<V> seedVertices)
    {
        assert init_vertices > 0 : "Number of initial unconnected 'seed' vertices " + 
                    "must be positive";
        assert numEdgesToAttach > 0 : "Number of edges to attach " +
                    "at each time step must be positive";
        
        if(!parallel && init_vertices < numEdgesToAttach)
            throw new IllegalArgumentException("If parallel edges disallowed, initial" +
                    "number of vertices must be >= number of edges to attach at each time step");
        mNumEdgesToAttachPerStep = numEdgesToAttach;
        mRandom = new Random(seed);
        this.graphFactory = graphFactory;
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        this.init_vertices = init_vertices;
        this.directed = directed;
        this.parallel = parallel;
        initialize(seedVertices);
    }
    
    /**
     * Constructs a new instance of the generator, whose output will be an undirected graph.
     * @param init_vertices     number of unconnected 'seed' vertices that the graph should start with
     * @param numEdgesToAttach the number of edges that should be attached from the
     * new vertex to pre-existing vertices at each time step
     * @param seed  random number seed
     */
    public BarabasiAlbertGenerator(Factory<Graph<V,E>> graphFactory,
    		Factory<V> vertexFactory, Factory<E> edgeFactory,
    		int init_vertices, int numEdgesToAttach, int seed, 
            Set<V> seedVertices) 
    {
        this(graphFactory, vertexFactory, edgeFactory, init_vertices, numEdgesToAttach, false, false, seed, seedVertices);
    }

    /**
     * Constructs a new instance of the generator, whose output will be an undirected graph,
     * and which will use the current time as a seed for the random number generation.
     * @param init_vertices     number of vertices that the graph should start with
     * @param numEdgesToAttach the number of edges that should be attached from the
     * new vertex to pre-existing vertices at each time step
     */
    public BarabasiAlbertGenerator(Factory<Graph<V,E>> graphFactory, 
    		Factory<V> vertexFactory, Factory<E> edgeFactory,
    		int init_vertices, int numEdgesToAttach, Set<V> seedVertices) {
        this(graphFactory, vertexFactory, edgeFactory, init_vertices, numEdgesToAttach, (int) System.currentTimeMillis(), seedVertices);
    }
    
    private void initialize(Set<V> seedVertices) {
    	
    	mGraph = graphFactory.create();

        vertex_index = new ArrayList<V>(2*init_vertices);
        index_vertex = new HashMap<V, Integer>(2*init_vertices);
        for (int i = 0; i < init_vertices; i++) {
            V v = vertexFactory.create();
            mGraph.addVertex(v);
            vertex_index.add(v);
            index_vertex.put(v, i);
            seedVertices.add(v);
        }
            
        mElapsedTimeSteps = 0;
    }

    private E createRandomEdge(Collection<V> preexistingNodes,
    		V newVertex, Map<E, Pair<V>> added_pairs) {
        V attach_point;
        boolean created_edge = false;
        Pair<V> endpoints;
        do {
            attach_point = vertex_index.get(mRandom.nextInt(vertex_index.size()));
            
            endpoints = new Pair<V>(newVertex, attach_point);
            
            // if parallel edges are not allowed, skip attach_point if <newVertex, attach_point>
            // already exists; note that because of the way edges are added, we only need to check
            // the list of candidate edges for duplicates.
            if (!parallel && added_pairs.containsValue(endpoints))
                continue;
            
            double degree = directed ? 
            		mGraph.inDegree(attach_point) :
            			mGraph.degree(attach_point);
            
            // subtract 1 from numVertices because we don't want to count newVertex
            // (which has already been added to the graph, but not to vertex_index)
            double attach_prob = (degree + 1) / (mGraph.getEdgeCount() + mGraph.getVertexCount() - 1);
            if (attach_prob >= mRandom.nextDouble())
                created_edge = true;
        }
        while (!created_edge);

        E to_add = edgeFactory.create();
        added_pairs.put(to_add, endpoints);
        
        if (directed == false) {

            added_pairs.put(to_add, new Pair<V>(attach_point, newVertex));
        }
        return to_add;
    }

    public void evolveGraph(int numTimeSteps) {

        for (int i = 0; i < numTimeSteps; i++) {
            evolveGraph();
            mElapsedTimeSteps++;
        }
    }

    private void evolveGraph() {
        Collection<V> preexistingNodes = mGraph.getVertices();
        V newVertex = vertexFactory.create();

        mGraph.addVertex(newVertex);

        // generate and store the new edges; don't add them to the graph
        // yet because we don't want to bias the degree calculations
        // (all new edges in a timestep should be added in parallel)
        List<E> edges = new LinkedList<E>();
        HashMap<E, Pair<V>> added_pairs = 
            new HashMap<E, Pair<V>>(mNumEdgesToAttachPerStep*3);
        for (int i = 0; i < mNumEdgesToAttachPerStep; i++) 
            edges.add(createRandomEdge(preexistingNodes, newVertex, added_pairs));
        
        // add edges to graph, now that we have them all
        for(E edge : edges) {
            mGraph.addEdge(edge, added_pairs.get(edge).getFirst(), added_pairs.get(edge).getSecond(), EdgeType.DIRECTED);
        }
        // now that we're done attaching edges to this new vertex, 
        // add it to the index
        vertex_index.add(newVertex);
        index_vertex.put(newVertex, new Integer(vertex_index.size() - 1));
    }

    public int getIndex(V v)
    {
        return ((Integer)index_vertex.get(v)).intValue();
    }
    
    public int getNumElapsedTimeSteps() {
        return mElapsedTimeSteps;
    }

    public Graph<V, E> generateGraph() {
        return mGraph;
    }

    public void reset(Set<V> seedVertices) {
        initialize(seedVertices);
    }
}