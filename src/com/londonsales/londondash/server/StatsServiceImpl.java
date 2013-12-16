package com.londonsales.londondash.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.londonsales.londondash.client.StatsService;

@SuppressWarnings("serial")
public class StatsServiceImpl extends RemoteServiceServlet implements StatsService {
    private java.sql.Connection conn = null;
    //private final String url = "jdbc:microsoft:sqlserver://";
//    private final String[] serverName = {"203.143.84.21", "168.144.171.93"};
  private final String[] serverName = {"203.143.84.21", "203.143.84.21"};
    //private final String[] portNumber = {"1433", "1433"};
    private final String[] databaseName = {"BBSMain", "LS_QLD"};
    private final String[] userName = {"sa", "sa"};
    private final String[] password = {"bbs1955", "bbs1955"}; //bbs1955%E

    private Connection getConnection(String company) {
        try {
                int companyNumber = 0;
                switch(company.toLowerCase()) {
                    case "qld":
                        companyNumber = 1;
                        break;
                    case "vic":
                        companyNumber = 0;
                        break;
                }

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                conn = DriverManager.getConnection(getConnectionUrl(companyNumber));

//                if (conn != null)
//                        System.out.println("Connection Successful!");
        } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error Trace in getConnection() : "
                                + e.getMessage());
        }
        return conn;
    }

    // 0 is victoria
    // 1 is qld
    private String getConnectionUrl(int company) {
        String url = "";
        if(company < serverName.length && company >= 0)
            url = "jdbc:sqlserver://" + serverName[company] + ";user=" + userName[company]
                        + ";password=" + password[company] + ";databaseName=" + databaseName[company]
                        + ";";
        return url;
    }

    @Override
    public String getDataTable(String company, String stmt) {
        String json = "";
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement(stmt);
            ResultSet rs = ps.executeQuery();
            json = convert(rs);
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String getString(String company, String stmt) {
        String result = "";
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement(stmt);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                result = rs.getString(1);
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result == null ? "" : result;
    }

    @Override
    public HashMap<String, Integer> getRegions(String company) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement("SELECT ID, Name from regions");
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                result.put(rs.getString("Name"), rs.getInt("ID"));
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ArrayList<String> getProducts(String company) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement("SELECT Part_No, Description "
                    + "FROM Sales_Transactions_Lines WHERE Part_No <> '.GADJUSTMENT' AND Part_No <> '..' AND Description <> ' ' "
                    + "AND Part_No <> '..' GROUP BY Part_no, Description ORDER BY Part_No;");
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                result.add(rs.getString("Part_No") + " > " + rs.getString("Description"));
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ArrayList<String> getUsers(String company) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement("SELECT ID, Short_Name, Full_Name FROM Operators;");
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                result.add(rs.getInt("ID") + " > " + rs.getString("Short_Name") + " > " + rs.getString("Full_name"));
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ArrayList<String> getStands(String company) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement("SELECT ID, Name FROM Division;");
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                result.add(rs.getInt("ID") + " > " + rs.getString("Name"));
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public HashMap<String, String> getStores(String company) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            Connection conn = getConnection(company);
            PreparedStatement ps = conn.prepareStatement("SELECT ID, Name, Data_Table FROM stores WHERE ID <> 1 ORDER BY Name");
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                result.put(rs.getString("Name"), rs.getString("Data_Table") + ":" + rs.getString("ID"));
            rs.close();
            ps.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Maintaining JSON manually to preserve order
    private String convert(ResultSet rs) throws SQLException {
        String json = "[";
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        while (rs.next()) {
            String obj = "{";
            for (int i = 1; i < numColumns + 1; i++) {
                if(!obj.equals("{"))
                    obj = obj.concat(",");
                String column_name = rsmd.getColumnName(i);
                if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getLong(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.REAL) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getFloat(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getBoolean(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getDouble(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getDouble(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getInt(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getNString(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getString(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.CHAR) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getString(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.NCHAR) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getNString(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.LONGNVARCHAR) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getNString(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.LONGVARCHAR) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getString(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getByte(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getShort(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getDate(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.TIME) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getTime(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getTimestamp(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.BIT) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getBoolean(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.NUMERIC) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getBigDecimal(column_name) + "]");
                } else if (rsmd.getColumnType(i) == java.sql.Types.DECIMAL) {
                    obj = obj.concat("\"" + column_name + "\":[" + rs.getBigDecimal(column_name) + "]");
                } else {
                    obj = obj.concat("\"" + column_name + "\":[\"" + rs.getString(i) + "\"]");
                }
            }

            if(!json.equals("["))
                json = json.concat(",");
            json = json.concat(obj.concat("}"));
        }
        return json.concat("]");
    }
}