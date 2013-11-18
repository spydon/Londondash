package com.londonsales.londondash.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.corechart.AreaChart;
import com.google.gwt.visualization.client.visualizations.corechart.BarChart;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart.Type;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.ScatterChart;
import com.londonsales.londondash.shared.Normalizer;
import com.londonsales.londondash.shared.QueryBuilder;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Londondash implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side Greeting
     * service.
     */
    private final StatsServiceAsync statsService = GWT
            .create(StatsService.class);

    private final FlexTable t = new FlexTable();
    private int usedRows = 0;
    private static int DELAY = 600;
    private int count = DELAY;
    private String company = "or";
    private int region = -1;
    private String store = "";
    private final HashMap<String, Integer> regions = new HashMap<String, Integer>();
    private final HashMap<String, String> storeTables = new HashMap<String, String>();
    private final HashMap<String, Integer> storeIDs = new HashMap<String, Integer>();
    private final ListBox regionsLb = new ListBox();
    private final ListBox storeLb = new ListBox();
    private final HTML headerName = new HTML("<h1>OR Dashboard</h1>");

    private enum Placement {
        LEFT, RIGHT, FULL
    };

    private final String loadUrl = "../images/loading2.gif";
    private String dateFrom = "DATEADD(d, DATEDIFF(d, 0, getdate()), 0)";
    private String dateTo = "getdate()";
    private final Label lastUpdate = new Label();
    private final Label countDown = new Label();
    private final CheckBox autoUpdateCB = new CheckBox();
    private final FlowPanel gross = new FlowPanel();
    private final FlowPanel profit = new FlowPanel();
    private final Timer autoUpdate = new Timer() {
        @Override
        public void run() {
            if(count<60)
                countDown.setText(Integer.toString(count) + "s");
            else
                countDown.setText(Integer.toString(count/60) + "m " + Integer.toString(count%60) + "s");
            if (count == 0) {
                update(dateFrom, dateTo);
                count = DELAY;
            } else if (count <= 10) {
                countDown.removeStyleName("normal");
                countDown.addStyleName("warning");
            } else {
                countDown.removeStyleName("warning");
                countDown.addStyleName("normal");
            }
            count--;
            // Schedule the timer to run once every second, 1000 ms.
            autoUpdate.schedule(1000);
        }
    };

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        final DialogBox loginBox = new DialogBox();
        HorizontalPanel loginPanel = new HorizontalPanel();
        final PasswordTextBox passwordTB = new PasswordTextBox();
        Button loginButton = new Button("Login");
        loginPanel.add(passwordTB);
        loginPanel.add(loginButton);
        loginBox.add(loginPanel);
        loginBox.setText("Please login");
        loginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(passwordTB.getValue().equals("citylink")) {
                    loginBox.hide();
                    firstInit();
                } else {
                    loginBox.setText("Faulty password!");
                    passwordTB.setText("");
                }
            }
        });
        passwordTB.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    if(passwordTB.getValue().equals("citylink")) {
                        loginBox.hide();
                        firstInit();
                    } else {
                        loginBox.setText("Faulty password!");
                        passwordTB.setText("");
                    }
                }
            }
        });
        loginBox.center();
        passwordTB.setFocus(true);
    }

    private void firstInit() {
        t.setSize("100%", "100%");
        t.getColumnFormatter().setWidth(0, "30%");
        t.getColumnFormatter().setWidth(1, "70%");
        t.setStylePrimaryName("rootTable");
        RootPanel r = RootPanel.get();
        Image logo = new Image("images/logo4.png");
        logo.setStylePrimaryName("logo");
        r.setStyleName("noHorizontalScroll");
        r.add(initHeader());
        r.add(initSubHeader());
        r.add(logo);
        r.add(t);
        updateRegions();
        updateStores();
        update(dateFrom, dateTo);
    }

    private void addSection(String name, FlowPanel table, FlowPanel chart, HorizontalPanel options) {
        if (table != null && chart != null) {
            table.setStylePrimaryName("tableLeft");
            chart.setStylePrimaryName("tableRight");
            t.setWidget(usedRows, 0, new HTML("<h2>" + name + "</h2>"));
            if (options!=null) {
                t.setWidget(usedRows + 1, 0, options);
                usedRows++;
            }
            t.setWidget(usedRows + 1, 0, table);
            t.setWidget(usedRows + 1, 1, chart);
        } else if (table == null) {
            t.getFlexCellFormatter().setColSpan(usedRows, 0, 2);
            t.getFlexCellFormatter().setColSpan(usedRows + 1, 0, 2);
            t.setWidget(usedRows, 0, new HTML("<h2>" + name + "</h2>"));
            if (options!=null) {
                t.setWidget(usedRows + 1, 0, options);
                usedRows++;
                t.getFlexCellFormatter().setColSpan(usedRows + 1, 0, 2);
            }
            t.setWidget(usedRows + 1, 0, chart);
        } else if (chart == null) {
            t.getFlexCellFormatter().setColSpan(usedRows, 0, 2);
            t.getFlexCellFormatter().setColSpan(usedRows + 1, 0, 2);
            t.setWidget(usedRows, 0, new HTML("<h2>" + name + "</h2>"));
            if (options!=null) {
                t.setWidget(usedRows + 1, 0, options);
                usedRows++;
                t.getFlexCellFormatter().setColSpan(usedRows + 1, 0, 2);
            }
            t.setWidget(usedRows + 1, 0, table);
        }
        usedRows += 2;
    }

    private void createChart(final String name, final String stmt,
            final CoreChart.Type type, final FlowPanel container) {
        container.clear();
        container.add(new Image(loadUrl));
        statsService.getDataTable(company, stmt, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                container.add(new HTML("<h3>Failed to connect to the server.</h3>"));
            }

            @Override
            public void onSuccess(String json) {
                container.clear();
                if (!json.equals("[]")) {
                    AbstractDataTable dt = toDataTable(json);
                    CoreChart chart = null;
                    Boolean isTable = false;
                    switch (type) {
                    case PIE:
                        chart = new PieChart(dt, createOptions(Placement.RIGHT,
                                name));
                        break;
                    case COLUMNS:
                        chart = new ColumnChart(dt, createOptions(
                                Placement.RIGHT, name));
                        break;
                    case BARS:
                        chart = new BarChart(dt, createOptions(
                                Placement.RIGHT, name));
                        break;
                    case AREA:
                        chart = new AreaChart(dt, createOptions(
                                Placement.RIGHT, name));
                        break;
                    case LINE:
                        chart = new LineChart(dt, createOptions(
                                Placement.RIGHT, name));
                        break;
                    case SCATTER:
                        chart = new ScatterChart(dt, createOptions(
                                Placement.RIGHT, name));
                        break;
                    case NONE:
                        Table table = new Table(dt, createTableOptions());
                        // table.addSelectHandler(createSelectHandler(table));
                        container.add(table);
                        isTable = true;
                        break;
                    default:
                        chart = new ColumnChart(dt, createOptions(Placement.RIGHT, name));
                        break;
                    }
                    if (!isTable) {
                        final GraphBox graphBox = new GraphBox();
                        graphBox.setStyleName("graphbox");
                        graphBox.setSelectedIndex(graphBox.getGraphIndex(type.name()));
                        graphBox.addChangeHandler(new ChangeHandler() {

                            @Override
                            public void onChange(ChangeEvent event) {
                                createChart(
                                        name,
                                        stmt,
                                        getChartType(graphBox),
                                        container);
                            }
                        });
                        //chart.addSelectHandler(createSelectHandler(chart));
                        container.add(chart);
                        container.add(graphBox);
                    }
                } else {
                    HTML noData = new HTML("<h3>No data available for the requested time span.</h3>");
                    noData.setStylePrimaryName("noData");
                    container.add(noData);
                }
            }
        });
    }

    private Options createOptions(Placement column, String name) {
        int width = getColumnWidth(column);
        int height = getColumnHeight();
        Options options = Options.create();
        options.setWidth(width);
        options.setHeight(height);
        options.setTitle(name);
        options.set("is3D", true);
        return options;
    }

    private Table.Options createTableOptions() {
        Table.Options options = Table.Options.create();
        options.setPage(Table.Options.Policy.ENABLE);
        options.setPageSize(20);
        return options;
    }

    private int getColumnWidth(Placement column) {
        return (int) (t.getOffsetWidth() * (1 + column.ordinal() % 2 * 0.7) * (Math
                .pow(0, column.ordinal()) * 0.3));
    }

    private int getColumnHeight() {
        return (int) (t.getOffsetWidth() * 0.25);
    }

//    @DEPRECATED
//    private SelectHandler createSelectHandler(final CoreChart chart) {
//        return new SelectHandler() {
//            @Override
//            public void onSelect(SelectEvent event) {
//                String message = "";
//
//                // May be multiple selections.
//                JsArray<Selection> selections = chart.getSelections();
//
//                for (int i = 0; i < selections.length(); i++) {
//                    // add a new line for each selection
//                    message += i == 0 ? "" : "\n";
//
//                    Selection selection = selections.get(i);
//
//                    if (selection.isCell()) {
//                        // isCell() returns true if a cell has been selected.
//
//                        // getRow() returns the row number of the selected cell.
//                        int row = selection.getRow();
//                        // getColumn() returns the column number of the selected
//                        // cell.
//                        int column = selection.getColumn();
//                        message += "cell " + row + ":" + column + " selected";
//                    } else if (selection.isRow()) {
//                        // isRow() returns true if an entire row has been
//                        // selected.
//
//                        // getRow() returns the row number of the selected row.
//                        int row = selection.getRow();
//                        message += "row " + row + " selected";
//                    } else {
//                        // unreachable
//                        message += "Pie chart selections should be either row selections or cell selections.";
//                        message += "  Other visualizations support column selections as well.";
//                    }
//                }
//
//                Window.alert(message);
//            }
//        };
//    }

    public static DataTable toDataTable(String json) {
        DataTable dt = DataTable.create();
        json = json.replaceAll("\\{", "");
        json = json.replaceAll("\\[", "");
        json = json.replaceAll("\\]", "");
        if(json.lastIndexOf("}") == -1) {
            return dt;
        }

        json = json.substring(0, json.lastIndexOf("}"));
        String[] rows = json.split("},");
        if (rows.length > 0) {
            String[] row = rows[0].split(",");
            for (String item : row) {
                String name = item.substring(1);
                name = name.substring(0, name.indexOf("\""));
                String value = item.substring(item.indexOf(":") + 1)
                        .replaceAll("\"", "");
                if (Normalizer.tryParseDouble(value) != null)
                    dt.addColumn(ColumnType.NUMBER, name);
                else if (Normalizer.tryParseInt(value) != null)
                    dt.addColumn(ColumnType.NUMBER, name);
                // else if(Normalizer.tryParseDate(value)!=null)
                // dt.addColumn(ColumnType.DATE, name);
                else
                    dt.addColumn(ColumnType.STRING, name);
            }
            for (int x = 0; x < rows.length; x++) {
                dt.addRow();
                String data = rows[x];
                String[] rowData = data.split(",");
                int skip = 0;
                for (int y = 0; y < rowData.length; y++) {
                    String value = rowData[y];
                    if (y + 1 < rowData.length && !rowData[y + 1].contains(":")) {
                        value = value + rowData[y + 1];
                        y++;
                        skip++;
                    }
                    value = value.substring(value.indexOf(":") + 1);
                    value = value.replaceAll("\"", "");
                    if (Normalizer.tryParseDouble(value) != null)
                        dt.setValue(x, y - skip, Double.parseDouble(value));
                    else if (Normalizer.tryParseInt(value) != null)
                        dt.setValue(x, y - skip, Integer.parseInt(value));
                    // else if(Normalizer.tryParseDate(value)!=null)
                    // dt.setValue(x, y, Normalizer.parseDate(value));
                    else
                        dt.setValue(x, y - skip, value);
                }
            }
        }
        return dt;
    }

    private Panel initHeader() {
        VerticalPanel header = new VerticalPanel();
        header.setStylePrimaryName("topHeader");
        // Make a new list box, for each fast time option
        final ListBox dateLb = new ListBox();
        dateLb.addItem("Today");
        dateLb.addItem("Yesterday");
        dateLb.addItem("This week");
        dateLb.addItem("Last week");
        dateLb.addItem("This month");
        dateLb.addItem("Last month");
        dateLb.addItem("This year");
        dateLb.addItem("Custom");
        dateLb.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String option = dateLb.getItemText(dateLb.getSelectedIndex());
                if (option.equals("Today")) {
                    dateFrom = "DATEADD(dd, DATEDIFF(dd, 0, getDate()), 0)";
                    dateTo = "DATEADD(dd, DATEDIFF(dd, 0, getDate()), 1)";
                } else if (option.equals("Yesterday")) {
                    dateFrom = "DATEADD(d,DATEDIFF(dd, 1, getdate()), 0)";
                    dateTo = "DATEADD(dd, DATEDIFF(dd, 0, getDate()), 0)";
                } else if (option.equals("This week")) {
                    dateFrom = "DATEADD(week, DATEDIFF(week, 0, getdate()), 0)";
                    dateTo = "DATEADD(dd, DATEDIFF(dd, 0, getDate()), 1)";
                } else if (option.equals("Last week")) {
                    dateFrom = "DATEADD(wk,DATEDIFF(wk,7,GETDATE()),0)";
                    dateTo = "DATEADD(wk,DATEDIFF(wk,7,GETDATE()),6)";
                } else if (option.equals("This month")) {
                    dateFrom = "DATEADD(s,-1,DATEADD(mm, DATEDIFF(m,0,GETDATE()),0))";
                    dateTo = "getdate()";
                } else if (option.equals("Last month")) {
                    dateFrom = "DATEADD(s,-1,DATEADD(mm, DATEDIFF(m,0,GETDATE())-1,0))";
                    dateTo = "DATEADD(s,-1,DATEADD(mm, DATEDIFF(m,0,GETDATE()),0))";
                } else if (option.equals("This year")) {
                    dateFrom = "DATEADD(YEAR, DATEDIFF(YEAR, 0, GETDATE()), 0)";
                    dateTo = "getdate()";
                } else if (option.equals("Custom")) {
                    dateBox();
                } else {
                    dateFrom = "DATEADD(dd, DATEDIFF(dd, 0, getDate()), 0)";
                    dateTo = "getdate()";
                }
                if (!option.equals("Custom"))
                    update(dateFrom, dateTo);
            }
        });

        final ListBox companyLb = new ListBox();
        companyLb.addItem("OR");
        companyLb.addItem("Jericho");
        companyLb.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String option = companyLb.getItemText(companyLb
                        .getSelectedIndex());
                if (!option.equals(company)) {
                    company = option.toLowerCase();
                    region = -1;
                    updateRegions();
                    updateStores();
                    update(dateFrom, dateTo);
                    headerName.setHTML("<h1>" + option + " Dashboard</h1>");
                }
            }
        });

        // Connect a listener to the regions listbox to update charts when it changes
        regionsLb.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String option = regionsLb.getItemText(regionsLb.getSelectedIndex());
                if(option.equals("All"))
                    region = -1;
                else
                    region = regions.get(option);
                update(dateFrom, dateTo);
            }
        });

        //ListBox for the stores
        storeLb.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String option = storeLb.getItemText(storeLb.getSelectedIndex());
                if (!option.equals(store)) {
                    if(option.equals("All"))
                        store = "";
                    else
                        store = storeTables.get(option);
                    update(dateFrom, dateTo);
                }
            }
        });

        // Button to update the charts
        Button helpB = new Button("Help");
        helpB.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                helpBox();
            }
        });

        // Button to update the charts
        Button updateB = new Button("Update");
        updateB.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (autoUpdateCB.getValue()) {
                    autoUpdate.cancel();
                    count = DELAY;
                    autoUpdate.schedule(50);
                }
                update(dateFrom, dateTo);
            }
        });
        autoUpdateCB.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (autoUpdateCB.getValue())
                    autoUpdate.schedule(50);
                else
                    autoUpdate.cancel();
            }
        });

        HorizontalPanel p = new HorizontalPanel();
        // p.setStylePrimaryName("topHeaderInner");
        VerticalPanel autoP = new VerticalPanel();
        autoP.setStylePrimaryName("autoPanel");
        autoP.add(new Label("Auto update"));
        autoP.add(autoUpdateCB);
        p.add(autoP);
        p.add(companyLb);
        p.add(regionsLb);
        p.add(storeLb);
        p.add(dateLb);
        p.add(lastUpdate);
        p.add(updateB);
        p.add(helpB);
        p.add(countDown);
        // header.add(new Image("../images/logo.png"));
        header.add(p);
        return header;
    }

    // Handles updating all the elements
    private void update(String from, String to) {
        usedRows = 0;
        updateSubHeader(dateFrom, dateTo);
        initCharts(dateFrom, dateTo);
    }

    // Handles updating of the regions listbox
    private void updateRegions() {
        statsService.getRegions(company, new AsyncCallback<HashMap<String, Integer>>() {

            @Override
            public void onFailure(Throwable caught) {
                //gross.setHTML("Connection failed.");
            }

            @Override
            public void onSuccess(HashMap<String, Integer> result) {
                regions.clear();
                regions.putAll(result);
                regionsLb.clear();
                regionsLb.addItem("All");
                for(String region : regions.keySet())
                    regionsLb.addItem(region);
            }
        });
    }

    private void updateStores() {
        statsService.getStores(company, new AsyncCallback<HashMap<String,String>>() {

            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(HashMap<String, String> result) {
                storeTables.clear();
                storeLb.clear();
                storeLb.addItem("All");
                Iterator<Entry<String, String>> it = result.entrySet().iterator();
                while (it.hasNext()) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, String> pairs = it.next();
                    String key = pairs.getKey();
                    String value = pairs.getValue();
                    storeLb.addItem(key);
                    storeTables.put(key, value.substring(0, value.indexOf(":")));
                    storeIDs.put(key, Integer.parseInt(value.substring(value.indexOf(":")+1)));
                    it.remove(); //Avoids a ConcurrentModificationException
                }
            }
        });
    }

    private Panel initSubHeader() {
        VerticalPanel header = new VerticalPanel();
        HorizontalPanel headerRow = new HorizontalPanel();
        header.setStylePrimaryName("header");
        VerticalPanel totalGrossP = new VerticalPanel();
        VerticalPanel totalProfitP = new VerticalPanel();
        //Gross revenue
        totalGrossP.add(new HTML("<h2>Total Gross Revenue</h2>"));
        totalGrossP.add(gross);

        //Profit
        totalProfitP.add(new HTML("<h2>Total Net Profit</h2>"));
        totalProfitP.add(profit);

        headerRow.add(totalGrossP);
        headerRow.add(totalProfitP);
        header.add(headerName);
        header.add(headerRow);
        return header;
    }

    private void updateSubHeader(final String from, final String to) {
        gross.add(new Image("../images/loading.gif"));
        profit.add(new Image("../images/loading.gif"));
        QueryBuilder q1 = new QueryBuilder(
                "SUM(Sales_Transactions_Header.Gross)",
                "Sales_Transactions_Header",
                "Sales_Type = 'INVOICE' AND Sale_Date >= " + from + " AND Sale_Date <= " + to,
                "",
                "");

        if(!store.equals("")) {
            q1.appendFrom("CROSS APPLY "
                    + "(SELECT TOP 1 Sales_Transactions_Lines.Transaction_No, Sales_Transactions_Lines.Store_ID "
                    + "FROM Sales_Transactions_Lines "
                    + "WHERE Sales_Transactions_Lines.Transaction_No = Sales_Transactions_Header.Transaction_No) STL2");
            q1.appendWhere("STL2.Store_ID = " + storeIDs.get(storeLb.getItemText(storeLb.getSelectedIndex())));
        } else if(region!=-1) {
            q1.appendFrom("INNER JOIN Division ON Division.ID = Branch_ID");
            q1.appendWhere("Division.Region_ID = " + region);
        }
        statsService.getString(company, q1.getQuery(), new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                gross.add(new HTML("<h3>Connection failed.</h3>"));
            }

            @Override
            public void onSuccess(String result) {
                gross.clear();
                if(!result.equals(""))
                    if(result.contains(".") && result.substring(result.indexOf("."), result.length()).length() > 2)
                        gross.add(new HTML("<h3>$"+result.substring(0, result.indexOf(".")+3)+"</h3>"));
                    else
                        gross.add(new HTML("<h3>$"+result+"</h3>"));
            }
        });
        statsService.getString(company, q1.setSelect("SUM(Sales_Transactions_Header.Net)-SUM(Sales_Transactions_Header.Cost)"), new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                //gross.setHTML("Connection failed.");
                profit.add(new HTML("<h3>Connection failed.</h3>"));
            }

            @Override
            public void onSuccess(String result) {
                profit.clear();
                if(!result.equals(""))
                    if(result.contains(".") && result.substring(result.indexOf("."), result.length()).length() > 2)
                        profit.add(new HTML("<h3>$"+result.substring(0, result.indexOf(".")+3)+"</h3>"));
                    else
                        profit.add(new HTML("<h3>$"+result+"</h3>"));
            }
        });
    }

    private void initCharts(final String from, final String to) {
        final DateTimeFormat fmt = DateTimeFormat.getFormat("hh:mm:ss");
        lastUpdate.setText("Last updated: " + fmt.format(new Date()));
        t.setWidget(0, 0, new Image(loadUrl));
        t.setWidget(0, 1, new Image(loadUrl));
        // Create a callback to be called when the visualization API
        // has been loaded.
        final Runnable onLoadCallback = new Runnable() {
            @Override
            public void run() {
                t.clear();

                // Setting up the Location Total section
                final FlowPanel chartPanel1 = new FlowPanel();
                FlowPanel tablePanel1 = new FlowPanel();

                //Building table for the locations total
                final QueryBuilder q1 = new QueryBuilder(
                        "Division.Name, ROUND(SUM(Gross), 0) AS 'Total($)'",
                        "Sales_Transactions_Header INNER JOIN Division ON Sales_Transactions_Header.Branch_ID = Division.ID",
                        "Sales_Type = 'INVOICE' AND Sale_Date >= " + from + " AND Sale_Date <= " + to,
                        "Division.Name HAVING ROUND(SUM(Gross), 0) > 0",
                        "'Total($)' DESC");
                if(region!=-1)
                    q1.appendWhere("Division.Region_ID = " + region);

                createChart(
                        "Total Sales by Division",
                        q1.getQuery(),
                        CoreChart.Type.NONE, tablePanel1);

                //Building graph for the locations total
                createChart(
                        "Total Sales by Division",
                        q1.setOrderBy(""),
                        CoreChart.Type.PIE, chartPanel1);
                addSection("Total Sales by Location", tablePanel1, chartPanel1, null);

                //Adds the possibility to change the graph type
//                final GraphBox graphBox1 = new GraphBox(3);
//                graphBox1.addChangeHandler(new ChangeHandler() {
//
//                    @Override
//                    public void onChange(ChangeEvent event) {
//                        createChart(
//                                "Total Sales by Division",
//                                q1.setOrderBy(""),
//                                getChartType(graphBox1), chartPanel1);
//                    }
//                });
//                chartPanel1.add(graphBox1);
                // Setting up Employee Total section
                FlowPanel chartPanel2 = new FlowPanel();
                FlowPanel tablePanel2 = new FlowPanel();

                //Building table for the Employee total
                QueryBuilder q2 = new QueryBuilder(
                        "Sales_Transactions_Header.Operator_Name, ROUND(SUM(Sales_Transactions_Header.Gross), 0) AS 'Total($)'",
                        "Sales_Transactions_Header",
                        "Sales_Type = 'INVOICE' AND Sale_Date >= " + from + " AND Sale_Date <= " + to,
                        "Operator_Name HAVING ROUND(SUM(Sales_Transactions_Header.Gross), 0) >= 0",
                        "'Total($)' DESC");

                if(!store.equals("")) {
                    q2.appendFrom("INNER JOIN Sales_Transactions_Lines ON Sales_Transactions_Lines.Transaction_No = Sales_Transactions_Header.Transaction_No");
                    q2.appendWhere("Sales_Transactions_Lines.Store_ID = " + storeIDs.get(storeLb.getItemText(storeLb.getSelectedIndex())));
                } else if (region!=-1) {
                    q2.appendFrom("INNER JOIN Division ON Division.ID = Branch_ID");
                    q2.appendWhere("Division.Region_ID = " + region);
                }
                createChart(
                        "Total Sales by Employee",
                        q2.getQuery(),
                        CoreChart.Type.NONE, tablePanel2);

                //Building graph for the Employee total
                createChart(
                        "Total Sales by Employee",
                        q2.getQuery(),
                        CoreChart.Type.PIE, chartPanel2);
                addSection("Total Sales by Employee", tablePanel2, chartPanel2, null);

                // Setting up Payment type section
                FlowPanel chartPanel3 = new FlowPanel();
                FlowPanel tablePanel3 = new FlowPanel();

                QueryBuilder q3 = new QueryBuilder(
                        "Payment_Type, SUM(Amount_Payment) As Amount",
                        "Sales_Payments_Received",
                        "Creation_Date >= " + from + " AND Creation_Date <= " + to,
                        "Payment_Type HAVING SUM(Amount_Payment) > 0",
                        "Amount DESC");
                if(!store.equals("")) {
                    q3.appendFrom("INNER JOIN Sales_Transactions_Lines ON Sales_Transactions_Lines.Transaction_No = Sales_Payments_Received.Sales_Invoice_No");
                    q3.appendWhere("Sales_Transactions_Lines.Store_ID = " + storeIDs.get(storeLb.getItemText(storeLb.getSelectedIndex())));
                } else if(region!=-1) {
                    q3.appendFrom("INNER JOIN Division ON Division.ID = Branch_ID");
                    q3.appendWhere("Division.Region_ID = " + region);
                }
                //Building table for the payment total
                createChart(
                        "Total Sales by Payment Method",
                        q3.getQuery(),
                        CoreChart.Type.NONE, tablePanel3);
                //Building graph for the payment total
                createChart(
                        "Total Sales by Payment Method",
                        q3.getQuery(),
                        CoreChart.Type.COLUMNS, chartPanel3);
                addSection("Total Sales by Payment Method", tablePanel3,
                        chartPanel3, null);

                // Setting up total sales by product section
                FlowPanel chartPanel4 = new FlowPanel();
                FlowPanel tablePanel4 = new FlowPanel();
                tablePanel4.setStylePrimaryName("centerText");

                QueryBuilder q4 = new QueryBuilder(
                        "Description, Part_No, AVG(l.Gross) As Avg_Gross, COUNT(*) As Qty",
                        "Sales_Transactions_Lines l INNER JOIN Sales_Transactions_Header h on l.Transaction_No = h.Transaction_No",
                        "Sales_Type = 'INVOICE' AND Part_No <> '.GADJUSTMENT' AND l.Modified_Date >= " + from + " AND l.Modified_Date <= " + to,
                        "Part_No, Description HAVING AVG(l.Gross) >= 0",
                        "Qty Desc");

                if(!store.equals("")) {
                    q4.appendWhere("Store_ID = " + storeIDs.get(storeLb.getItemText(storeLb.getSelectedIndex())));
                } else if(region!=-1) {
                    q4.appendFrom("INNER JOIN Division ON Division.ID = Store_ID");
                    q4.appendWhere("Division.Region_ID = " + region);
                }
                //Building table for the product section
                createChart(
                        "Total Sales by Product (Table)",
                        q4.getQuery(),
                        CoreChart.Type.NONE, tablePanel4);
                //Building graph for the product section
                createChart(
                        "Total Sales by Product (Chart)",
                        q4.setSelect("Description, COUNT(*) As Qty"),
                        CoreChart.Type.COLUMNS, chartPanel4);
                addSection("Total Sales by Product (Chart)", chartPanel4, null, null);
                addSection("Total Sales by Product (Table)", tablePanel4, null, null);

//                // Setting up product development
//                FlowPanel tablePanel5 = new FlowPanel();
//                QueryBuilder q5 = new QueryBuilder(
//                        "DATENAME(m,modified_date) As Month, Count(*) As 'OR24'",
//                        "Sales_Transactions_Lines",
//                        "Part_No = 'OR-24' AND modified_date >= " + from + " AND modified_date <= " + to,
//                        "DATENAME(m,modified_date), DATEPART(MM,modified_date)",
//                        "");
//                if(!store.equals("")) {
//                    q5.appendWhere("Store_ID = " + storeIDs.get(storeLb.getItemText(storeLb.getSelectedIndex())));
//                } else if(region!=-1) {
//                    q5.appendWhere("Division.Region_ID = " + region);
//                }
//                createChart(
//                        "Product Development",
//                        q5.getQuery(),
//                        CoreChart.Type.LINE, tablePanel5);
//                addSection("Product Development", tablePanel5, null, null);

                // Setting up invoices section
                FlowPanel tablePanel6 = new FlowPanel();
                QueryBuilder q6 = new QueryBuilder(
                        "TOP 500 Sales_Transactions_Header.Transaction_No, Division.Name As Division, "
                        + "Operators.Full_Name, CONVERT(VARCHAR, Sales_Transactions_Header.Sale_Date, 108) As Sale_Time, CONVERT(VARCHAR, Sales_Transactions_Header.Sale_Date, 103) As Sale_Date, Sales_Transactions_Header.Net, "
                        + "Sales_Transactions_Header.Gross, Sales_Transactions_Header.Tax, Sales_Transactions_Header.Cost",
                        "Sales_Transactions_Header INNER JOIN Division ON Sales_Transactions_Header.Branch_ID = Division.ID "
                        + "INNER JOIN Operators ON Sales_Transactions_Header.Operator_ID = Operators.ID",
                        "Sales_Type = 'INVOICE' AND Sale_Date >= " + from + " AND Sale_Date <= " + to,
                        "",
                        "Sales_Transactions_Header.Sale_Date DESC");
                if(!store.equals("")) {
                    q6.appendFrom("INNER JOIN (SELECT DISTINCT Transaction_No, Store_ID FROM Sales_Transactions_Lines) as t ON Sales_Transactions_Header.Transaction_No = t.Transaction_No");
                    q6.appendWhere("t.Store_ID = " + storeIDs.get(storeLb.getItemText(storeLb.getSelectedIndex())));
                } else if(region!=-1) {
                    q6.appendWhere("Division.Region_ID = " + region);
                }
                createChart(
                        "Invoices",
                        q6.getQuery(),
                        CoreChart.Type.NONE, tablePanel6);
                addSection("Invoices", tablePanel6, null, null);

                // Setting up stock section
                FlowPanel tablePanel7 = new FlowPanel();

                if(!store.equals("")) {
                    QueryBuilder q7 = new QueryBuilder(
                            "Part_No, Description, Cast(Qty_In_Stores As Integer) As Qty_In_Stores, Cast(Stock_Count_Qty As Integer) As Stock_Count_Qty",
                            store,
                            "Qty_In_Stores <> 0 OR Stock_Count_Qty <> 0",
                            "",
                            "Part_No");
                    createChart(
                            "Stock",
                            q7.getQuery(),
                            CoreChart.Type.NONE, tablePanel7);
                    addSection("Stock", tablePanel7, null, null);
                } else {
                    tablePanel7.add(new HTML("<h3>Please choose a specific store.</h3>"));
                    addSection("Stock", tablePanel7, null, null);
                }
            }
        };

        Runnable chartLoadCallback = new Runnable() {
            @Override
            public void run() {
                VisualizationUtils.loadVisualizationApi(onLoadCallback,
                        Table.PACKAGE);
            }
        };

        // Load the visualization api, passing the chartLoadCallback to be called
        // when loading is done.
        VisualizationUtils.loadVisualizationApi(chartLoadCallback,
                ColumnChart.PACKAGE);
    }

    private Type getChartType(GraphBox box) {
        return CoreChart.Type.valueOf(box.getItemText(box.getSelectedIndex()));
    }

    public void dateBox() {
        // Create dialogbox for the pickers to live, love and fight in.
        final DialogBox db = new DialogBox();
        db.setText("Pick the custom dates");
        final Label text = new Label();
        text.setStylePrimaryName("error");
        final DateTimeFormat fmt = DateTimeFormat
                .getFormat("yyyy-MM-dd");

        // Create a date picker
        final DatePicker dateFromDP = new DatePicker();
        // Set the default value
        dateFromDP.setValue(new Date(), true);

        // Create a date picker
        final DatePicker dateToDP = new DatePicker();
        // Set the default value
        dateToDP.setValue(new Date(), true);

        // Add button to set the values to the charts
        Button submit = new Button("Done");
        submit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (dateFromDP.getValue().after(dateToDP.getValue())) {
                    text.setText("From has to be before the \"To\"-date");
                } else {
                    db.hide();
                    dateFrom = "'" + fmt.format(dateFromDP.getValue()) + "'";
                    dateTo = "DATEADD(dd, DATEDIFF(dd, -1, '" + fmt.format(dateToDP.getValue()) + "'), 0)";
                    update(dateFrom, dateTo);
                }
            }
        });

        // Add button to cancel the current operation
        Button cancel = new Button("Cancel");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                db.hide();
            }
        });

        VerticalPanel v1 = new VerticalPanel();
        v1.add(new Label("Date From:"));
        v1.add(dateFromDP);
        VerticalPanel v2 = new VerticalPanel();
        v2.add(new Label("Date To:"));
        v2.add(dateToDP);
        HorizontalPanel h = new HorizontalPanel();
        h.add(v1);
        h.add(v2);
        FlowPanel f = new FlowPanel();
        f.add(submit);
        f.add(cancel);
        VerticalPanel v = new VerticalPanel();
        v.add(h);
        v.add(text);
        v.add(f);

        // Add the panel with all the widgets to the box
        db.setWidget(v);
        db.center();
    }

    public void helpBox() {
        // Create dialogbox for the pickers to live, love and fight in.
        final DialogBox db = new DialogBox();
        db.setText("Help");

        VerticalPanel panel = new VerticalPanel();
        // Create the help text
        HTML text = new HTML();
        text.setHTML("This dashboard has the purpous of giving an overview of sales and products. "
                + "You control most of the dashboard from the bar in the top<br/><br/>"
                + "<b>Top bar</b><br/>"
                + "From left to right the controls are:<br/>"
                + "<ul><li>Auto updates - Automatically updates the dashboard every ten minutes</li>"
                + "<li>Company chooser - Which company scope you want data for</li>"
                + "<li>Division chooser - Which division you want data for</li>"
                + "<li>Store chooser - Which store you want data for</li>"
                + "<li>Date chooser - Between which dates the data should be for, custom lets you choose freely</li>"
                + "<li>Last updated - Shows when the last time the dashboard fetched data</li>"
                + "<li>Update - Manually fetches new data from within your set scopes</li>"
                + "<li>Help - Opens up this dialog</li></ul>"
                + "<b>Tips and tricks</b><br />"
                + "<ul><li>Change any graph by using the graph chooser underneath each graph</li>"
                + "<li>Hold over each part of the graph to get more information</li>"
                + "<li>Order the table after a different column by pressing the column name</li></ul>"
                + "If you need further help, send a mail to <a href=\"mailto:lukas.klingsbo@gmail.com\">lukas.klingsbo@gmail.com</a>");

        // Add button to cancel the current operation
        Button cancel = new Button("Ok");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                db.hide();
            }
        });
        cancel.setWidth("100%");

        panel.add(text);
        panel.add(cancel);
        // Add the HTML to the box
        db.setWidget(panel);
        db.center();
    }
}
