package acza.ee.sun.geyserM2M;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GeyserSQLDatabase {

	private static final Logger logger = LogManager.getLogger(DataStamp.class);
	private String JDBC_DRIVER;
	private String db_url; 
	private String db_user; 
	private String db_psk;
	
	/***
	 * Constructor
	 * @param db_url
	 * @param db_user
	 * @param db_psk
	 */
	public GeyserSQLDatabase(String db_url, String db_user, String db_psk){
		this.db_url = db_url;
		this.db_user = db_user;
		this.db_psk = db_psk;
		this.JDBC_DRIVER = "com.mysql.jdbc.Driver";
		
	}
	
	/***
	 * 
	 * @param geyser_id
	 * @param start_ts
	 * @param end_ts
	 * @return
	 */
	public LinkedList<DataStamp> select(String geyser_id, Date start_ts, Date end_ts){
		
		LinkedList<DataStamp> data_points = new LinkedList<DataStamp>();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String query = "SELECT * from timestamps where geyser_id="
				+ geyser_id
				+ " AND server_stamp >= "
				+ "'" + df.format(start_ts) + "'"
				+ "AND server_stamp <= "
				+ "'" + df.format(end_ts) + "'";
		
		
		Connection rdb_conn = null;
		Statement stmt = null;
		try{
			//Register JDBC driver
			Class.forName(this.JDBC_DRIVER);

			//Open a connection
			rdb_conn = DriverManager.getConnection(db_url,db_user,db_psk);

			//Execute a query
			stmt = rdb_conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				String ewh_id = rs.getString("geyser_id");
				long server_stamp = rs.getTimestamp("server_stamp").getTime();
				long client_stamp = rs.getTimestamp("client_stamp").getTime();
				double t_outlet = rs.getInt("t1");
				double t_far = rs.getInt("t2");
				double t_inlet = rs.getInt("t3");
				double t_ambient = rs.getInt("t4");
				boolean drip_detect = rs.getBoolean("drip_detect");
				boolean valve_state = rs.getBoolean("valve_state");
				boolean relay_state = rs.getBoolean("relay_state");
				double watt_avgpmin = rs.getFloat("watt_avgpmin");
				double kwatt_tot = rs.getFloat("kwatt_tot");
				double hot_flow_ratepmin = rs.getFloat("hot_flow_ratepmin");
				double hot_litres_tot = rs.getFloat("hot_litres_tot");
				double cold_flow_ratepmin = rs.getFloat("cold_flow_ratepmin");
				double cold_litres_tot = rs.getFloat("cold_litres_tot");
				
				
				data_points.add(new DataStamp(ewh_id, server_stamp, client_stamp, 
						t_outlet, t_far, t_inlet, t_ambient,
						drip_detect, valve_state, relay_state, 
						watt_avgpmin, kwatt_tot, 
						hot_flow_ratepmin, hot_litres_tot, cold_flow_ratepmin, cold_litres_tot));
			}
			
			logger.info("Successfully read database with query: " + query);
			
			stmt.close();
			rdb_conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			logger.error("SQLException: ", se);
		}catch(Exception e){
			//Handle errors for Class.forName
			logger.error("Unexpected database exception: ", e);
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException se2){
				logger.error("SQLException closing statement: ", se2);
			}// nothing we can do
			try{
				if(rdb_conn!=null)
					rdb_conn.close();
			}catch(SQLException se){
				logger.error("SQLException closing database connection: ", se);
			}
		}
		
		return data_points;
	}
	
}
