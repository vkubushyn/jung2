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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JRootPane;

import edu.uci.ics.graph.Graph;
import edu.uci.ics.jung.graph.SimpleDirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.layout.FRLayout;
import edu.uci.ics.jung.visualization.layout.LayoutMutable;
import edu.uci.ics.jung.visualization.layout.SpringLayout;
import edu.uci.ics.jung.visualization.layout.SpringLayout.LengthFunction;

/*
 * Created on May 10, 2004
 */

/**
 * Thanks to Brad Allen for an original inspiration for this.
 * 
 * @author danyelf
 */
public class AddNodeDemo extends javax.swing.JApplet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5345319851341875800L;

	private Graph<Number,Number> g = null;

    private VisualizationViewer<Number,Number> vv = null;

    private LayoutMutable<Number,Number> layout = null;

    Timer timer;

    protected JButton switchLayout;

    public static final LengthFunction<Number> UNITLENGTHFUNCTION = new SpringLayout.UnitLengthFunction<Number>(
            100);

    public void init() {

        //create a graph
        g = new SimpleDirectedSparseGraph<Number,Number>();

        //create a graphdraw
        layout = new FRLayout<Number,Number>(g);
        
        vv = new VisualizationViewer<Number,Number>(layout);

        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(java.awt.Color.lightGray);
        getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

        //define my layout for dynamic graphing
        // info can be found at
        // https://sourceforge.net/forum/forum.php?thread_id=1021284&forum_id=252062

        //set a visualization viewer
        
        vv.getModel().setRelaxerThreadSleepTime(500);
        vv.setGraphMouse(new DefaultModalGraphMouse());

        getContentPane().add(vv);
        switchLayout = new JButton("Switch to SpringLayout");
        switchLayout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (switchLayout.getText().indexOf("Spring") > 0) {
                    switchLayout.setText("Switch to FRLayout");
                    layout = new SpringLayout<Number,Number>(g, UNITLENGTHFUNCTION);
                    vv.getModel().setGraphLayout(layout);
                } else {
                    switchLayout.setText("Switch to SpringLayout");
                    layout = new FRLayout<Number,Number>(g);
                    vv.getModel().setGraphLayout(layout);
                }
                if (!vv.isVisRunnerRunning())
                    vv.getModel().init();
            }
        });

        getContentPane().add(switchLayout, BorderLayout.SOUTH);

        timer = new Timer();
    }

    public void start() {
        validate();
        //set timer so applet will change
        timer.schedule(new RemindTask(), 1000, 1000); //subsequent rate
        vv.repaint();
    }

    Integer v_prev = null;

    public void process() {

        System.out.println("-[----------------------------");
        int label_number = 0;

        boolean redraw = false;

        //run in loop populating data on graph as it goes into the database
        //        while (t != -1) {
        redraw = true;
        try {

            if (g.getVertices().size() < 100) {
                redraw = true;

                //pull out last record processed and label
                label_number = (int) (Math.random() * 10000);

                System.out.println("P: adding a node " + label_number);

                //add a vertex
                Integer v1 = new Integer(g.getVertices().size());
                g.addVertex(v1);

                // wire it to some edges
                if (v_prev != null) {
                    g.addEdge(g.getEdges().size(), v_prev, v1);
                    // let's connect to a random vertex, too!
                    int rand = (int) (Math.random() * g.getVertices().size());
                    g.addEdge(g.getEdges().size(), v1, rand);
                }

                v_prev = v1;

            }

            if (redraw) {
                System.out.println("P: Updating");
                //update the layout
                // see
                // https://sourceforge.net/forum/forum.php?thread_id=1021284&forum_id=252062
                
                layout.update();
                if (!vv.isVisRunnerRunning())
                    vv.getModel().init();
                vv.repaint();
            }

        } catch (Exception e) {
            System.out.println(e);

        }
        System.out.println("------------end process------------");
    }

    class RemindTask extends TimerTask {

        public void run() {
            process();

        }
    }
}