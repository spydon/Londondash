package com.londonsales.londondash.client;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;

public class GraphBox extends ListBox {
    private final ArrayList<String> graphs = new ArrayList<String>();

    public GraphBox() {
        graphs.add(CoreChart.Type.AREA.name());
        graphs.add(CoreChart.Type.BARS.name());
        graphs.add(CoreChart.Type.COLUMNS.name());
        graphs.add(CoreChart.Type.LINE.name());
        graphs.add(CoreChart.Type.PIE.name());
        for(String graph : graphs)
            addItem(graph);
    }

    public int getGraphIndex(String graph) {
        for(int x = 0; x<graphs.size(); x++)
            if(graphs.get(x).equals(graph))
                return x;
        return -1;
    }
}
