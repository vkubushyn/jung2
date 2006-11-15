/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uci.ics.graph.Graph;


/**
 * Graph generator that generates undirected sparse graphs with power-law distributions.
 * @author Scott White
 * @see "A Steady State Model for Graph Power Law by David Eppstein and Joseph Wang"
 */
public class EppsteinPowerLawGenerator<V,E> implements GraphGenerator<V,E> {
    private int mNumVertices;
    private int mNumEdges;
    private int mNumIterations;
    private double mMaxDegree;
    private Random mRandom;
    private GraphElementFactory<V,E> factory;

    /**
     * Constructor which specifies the parameters of the generator
     * @param numVertices the number of vertices for the generated graph
     * @param numEdges the number of edges the generated graph will have, should be Theta(numVertices)
     * @param r the model parameter. The larger the value for this parameter the better the graph's degree
     * distribution will approximate a power-law.
     */
    public EppsteinPowerLawGenerator(GraphElementFactory<V,E> factory, int numVertices, int numEdges, int r) {
    	this.factory = factory;
        mNumVertices = numVertices;
        mNumEdges = numEdges;
        mNumIterations = r;
        mRandom = new Random();
    }

    protected Graph<V,E> initializeGraph() {
        Graph<V,E> graph = null;
        graph = new SimpleUndirectedSparseGraph<V,E>();
        for(int i=0; i<mNumVertices; i++) {
        	graph.addVertex(factory.generateVertex(graph));
        }
        List<V> vertices = new ArrayList<V>(graph.getVertices());
        while (graph.getEdges().size() < mNumEdges) {
            V u = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            V v = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            if (!graph.isSuccessor(v,u)) {
            	graph.addEdge(factory.generateEdge(graph), u, v);
            }
        }

        double maxDegree = 0;
        for (V v : graph.getVertices()) {
            maxDegree = Math.max(graph.degree(v),maxDegree);
        }
        mMaxDegree = maxDegree; //(maxDegree+1)*(maxDegree)/2;

        return graph;
    }

    /**
     * Generates a graph whose degree distribution approximates a power-law.
     * @return the generated graph
     */
    public Graph<V,E> generateGraph() {
        Graph<V,E> graph = initializeGraph();

        List<V> vertices = new ArrayList<V>(graph.getVertices());
        for (int rIdx = 0; rIdx < mNumIterations; rIdx++) {

            V v = null;
            int degree = 0;
            do {
                v = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
                degree = graph.degree(v);

            } while (degree == 0);

            List<E> edges = new ArrayList<E>(graph.getIncidentEdges(v));
            E randomExistingEdge = edges.get((int) (mRandom.nextDouble()*degree));

            V x = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            V y = null;
            do {
                y = vertices.get((int) (mRandom.nextDouble() * mNumVertices));

            } while (mRandom.nextDouble() > ((double) (graph.degree(y)+1)/mMaxDegree));

            if (!graph.isSuccessor(y,x) && x != y) {
                graph.removeEdge(randomExistingEdge);
                graph.addEdge(factory.generateEdge(graph), x, y);
            }
        }

        return graph;
    }

    public void setSeed(long seed) {
        mRandom.setSeed(seed);
    }
}
