/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package edu.uci.ics.jung.visualization;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import edu.uci.ics.graph.Edge;

/**
 * This class translates mouse clicks into vertex clicks
 * 
 * @author danyelf
 */
public class MouseListenerTranslator<V, E extends Edge<V>> extends MouseAdapter {

	private VisualizationViewer<V,E> vv;
	private GraphMouseListener<V> gel;

	/**
	 * @param gel
	 * @param vv
	 */
	public MouseListenerTranslator(GraphMouseListener<V> gel, VisualizationViewer<V,E> vv) {
		this.gel = gel;
		this.vv = vv;
	}
	
	/**
	 * Transform the point to the coordinate system in the
	 * VisualizationViewer, then use either PickSuuport
	 * (if available) or Layout to find a Vertex
	 * @param point
	 * @return
	 */
	private V getVertex(Point2D point) {
	    // adjust for scale and offset in the VisualizationViewer
	    Point2D p = vv.inverseViewTransform(point);
	    PickSupport<V,E> pickSupport = vv.getPickSupport();
	    V v = null;
	    if(pickSupport != null) {
	        v = pickSupport.getVertex(p.getX(), p.getY());
	    } 
	    return v;
	}
	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	    V v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphClicked(v, e );
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		V v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphPressed(v, e );
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		V v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphReleased(v, e );
		}
	}
}