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

import edu.uci.ics.graph.Edge;
import edu.uci.ics.graph.Graph;


/**
 * An interface for algorithms that generate graphs that evolve over time
 * @author Scott White
 */
public interface EvolvingGraphGenerator<V, E extends Edge<V>> extends GraphGenerator<V,E> {

    /**
     * Instructs the algorithm to evolve the graph N time steps and return
     * the most current evolved state of the graph
     * @param numTimeSteps number of time steps to simulate from its
     * current state
     */
    void evolveGraph(int numTimeSteps);

    /**
     * Retrieves the total number of time steps elapsed
     * @return number of elapsed time steps
     */
    int getNumElapsedTimeSteps();

    /**
     * Returns a copy of the evolved graph in its current state
     * @return new instance of the evolved graph
     */
    public Graph<V,? extends E> generateGraph();

    /**
     * Resets the random graph to the state that it had after the
     * constructor returned.
     */
    public void reset();
}