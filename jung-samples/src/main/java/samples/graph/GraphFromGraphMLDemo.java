/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package samples.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.graph.Graph;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.SimpleDirectedSparseGraph;
import edu.uci.ics.jung.io.GraphMLFile;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;


/**
 * Demonstrates the use of <code>GraphZoomScrollPane</code>.
 * This class shows the <code>VisualizationViewer</code> zooming
 * and panning capabilities, using horizontal and
 * vertical scrollbars.
 *
 * <p>This demo also shows ToolTips on graph vertices and edges,
 * and a key listener to change graph mouse modes.</p>
 * 
 * @author Tom Nelson
 * 
 */
public class GraphFromGraphMLDemo {

    /**
     * the graph
     */
    Graph<Number, Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number, Number> vv;
    
    /**
     * create an instance of a simple graph with controls to
     * demo the zoom features.
     * 
     */
    public GraphFromGraphMLDemo(String filename) {
        
    	Factory<Graph<Number,Number>> graphFactory = new Factory<Graph<Number,Number>>() {
    		public Graph<Number,Number> create() {
    			return new SimpleDirectedSparseGraph<Number,Number>();
    		}
    	};
    	Factory<Number> vertexFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};
    	Factory<Number> edgeFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};
    	GraphMLFile<Number,Number> file = new GraphMLFile<Number,Number>(graphFactory,vertexFactory,edgeFactory);
    	
        // create a simple graph for the demo
        graph = file.load(filename);
        vv =  new VisualizationViewer<Number,Number>(new FRLayout<Number,Number>(graph));

        vv.addGraphMouseListener(new TestGraphMouseListener<Number>());
        vv.getRenderer().setVertexRenderer(
        		new GradientVertexRenderer<Number,Number>(
        				Color.white, Color.red, 
        				Color.white, Color.blue,
        				vv.getPickedVertexState(),
        				false));
        
        // add my listeners for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        vv.setEdgeToolTipTransformer(new Transformer<Number,String>() {
			public String transform(Number edge) {
				return "E"+graph.getEndpoints(edge).toString();
			}});
        
        vv.getRenderContext().setVertexStringer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
        
        // create a frome to hold the graph
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

        JMenuBar menubar = new JMenuBar();
        menubar.add(graphMouse.getModeMenu());
        panel.setCorner(menubar);

        
        vv.addKeyListener(graphMouse.getModeKeyListener());
        vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
        
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener<V> implements GraphMouseListener<V> {
        
    		public void graphClicked(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphPressed(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphReleased(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
    		}
    }

    /**
     * a driver for this demo
     */
    public static void main(String[] args) 
    {
    	String filename = "simple.graphml";
    	if(args.length > 0) filename = args[0];
        new GraphFromGraphMLDemo(filename);
    }
}