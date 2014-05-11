package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import dto.FeedObjects;
import dto.GSforDealDetails;
import dto.GSforRegister;

public class Project {

	Statement stmt = null;
	ResultSet rs;
	int rowCount = 0;

	public ArrayList<FeedObjects> GetFeeds(Connection connection)
			throws Exception {
		ArrayList<FeedObjects> feedData = new ArrayList<FeedObjects>();
		try {
			// String uname = request.getParameter("uname");

			System.out.println("in s1");

			PreparedStatement ps = connection
					.prepareStatement("SELECT title,description,url FROM website");
			// ps.setString(1,uname);

			System.out.println("in s2");
			ResultSet rs = ps.executeQuery();
			System.out.println("in s3");

			while (rs.next()) {
				FeedObjects feedObject = new FeedObjects();
				feedObject.setTitle(rs.getString("title"));
				feedObject.setDescription(rs.getString("description"));
				feedObject.setUrl(rs.getString("url"));
				feedData.add(feedObject);
			}
			return feedData;
		} catch (Exception e) {
			throw e;
		}
	}

	public ArrayList<GSforDealDetails> GetDealFromId(Connection connection,
			int adId) throws Exception {
		ArrayList<GSforDealDetails> feedData = new ArrayList<GSforDealDetails>();
		try {

			int dealcount = 0;
			System.out.println("in s1deal");

			PreparedStatement ps = connection
					.prepareStatement("SELECT * FROM \"PromotionAdDetails\" where adid = '"
							+ adId + "'");

			System.out.println("in s2deal");
			ResultSet rs = ps.executeQuery();
			System.out.println("in s3deal");

			while (rs.next()) {
				GSforDealDetails dealObject = new GSforDealDetails();
				dealObject.setAdname(rs.getString("adname"));
				dealObject.setAddescription(rs.getString("addescription"));

				dealcount = rs.getInt("subscribeddealcount");

				feedData.add(dealObject);
			}

			dealcount++;
			System.out.println("in s3deal dealcoiunt is ++ " + dealcount);

			PreparedStatement ps1 = connection
					.prepareStatement("update \"PromotionAdDetails\" set subscribeddealcount = '"
							+ dealcount + "' where adid = '" + adId + "'");

			System.out.println("Count Updated1");

			int rs1 = ps1.executeUpdate();

			System.out.println("Count Updated5");

			return feedData;
		} catch (Exception e) {
			throw e;
		}
	}

	public String postRegistrationDetails(Connection connection, String fname,
			String lname, String uname, String pwd, String gender, String pref)
			throws Exception {
		String resp = null;
		try {

			System.out.println("in Project class for DB insert");

			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO \"User\" (firstname, lastname, username, password,gender,age) VALUES (?,?,?,?,?,?)");

			
			System.out.println("after prepr stmt!!!!");

			
			ps.setString(1, fname);
			ps.setString(2, lname);
			ps.setString(3, uname);
			ps.setString(4, pwd);
			ps.setString(5, gender);
			ps.setInt(6, 25);
			int rowCount = ps.executeUpdate();
			if (rowCount > 0) {
				System.out.println("Record is inserted into user table!");
				resp = setUserPreferences(connection,uname, pref);
				resp = "Success";
			} else {
				System.out.println("Error!!!!");
				resp = "Failure";

			}
			return resp;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String setUserPreferences(Connection connection, String userName,
			String pref) throws SQLException {
		
		System.out.println("in set user pref!!!!");

		String resp = null;
		Statement stmt1 = null;
		stmt1 = connection.createStatement();

		String query1 = "select userid from  \"User\" where userName= '"
				+ userName + "'";
		System.out.println("Query: " + query1);
		rs = stmt1.executeQuery(query1);
		if (rs != null) {
			if (rs.next()) {
				int userid = rs.getInt(1);
				System.out.println("userId: " + userid);
				String query2 = "INSERT INTO \"UserLikings\" (userlikings,u_id) VALUES ('"
						+ pref + "'," + userid + ")";
				rowCount = stmt1.executeUpdate(query2);
				if (rowCount > 0)
					resp = "Success";
				else
					resp = "Failure";
			}
		}
		return resp;
	}

	public String GetLoginDetails(Connection connection, String userName,
			String pwd) throws SQLException {
		ResultSet rs;
		int rowCount = 0;
		Statement stmt = connection.createStatement();
		String resp = "Failure";
		ArrayList<GSforRegister> feedData = new ArrayList<GSforRegister>();

		String query = "select * from \"User\" where userName= '" + userName
				+ "' and password = '" + pwd + "';";
		System.out.println("Query: " + query);
		rs = stmt.executeQuery(query);
		if (rs != null) {
			System.out.println("in rs");
			while (rs.next()) {

				System.out.println("in next login");

					GSforRegister userObject = new GSforRegister();
					userObject.setUserid(rs.getInt("userid"));

					feedData.add(userObject);
					
					System.out.println(rs.getInt("userid"));


				System.out.println("in next");
				// resp = "Success";

				int id = rs.getInt("userid");
				resp = Integer.toString(rs.getInt("userid"));

			} 
//			else {
//				resp = "";
//
//			}
		} else {
			
			System.out.println("fail");

			resp = "Failure";
		}
		return resp;
	}

	

	public ArrayList<GSforDealDetails> getDeals(Connection connection,
			Double lat, Double lng, String uid) throws SQLException {
		ArrayList<GSforDealDetails> dealsList = new ArrayList<GSforDealDetails>();
		HashMap<String, Boolean> likings = new HashMap<String, Boolean>();

		likings = getUserLikings(likings, connection, uid);

		Statement stmt1 = null;
		ResultSet rs;
		int rowCount = 0;
		stmt1 = connection.createStatement();

		String query2 = "select adid,adname,addescription,adtags,(point("
				+ lat
				+ ","
				+ lng
				+ ") <@> promotionlocationlattitude) as distance from \"PromotionalDeals\",\"PromotionAdDetails\" "
				+ "where (point("
				+ lat
				+ ","
				+ lng
				+ ") <@> promotionlocationlattitude) < 1 and p_id = promotionid order by distance;";
		System.out.println("query1:" + query2);

		rs = stmt1.executeQuery(query2);
		if (rs != null) {
			while (rs.next()) {
				String adtag = rs.getString("adtags").toLowerCase();
				if (likings.containsKey(adtag)) {
					GSforDealDetails d = new GSforDealDetails();
					d.setAdname(rs.getString("adname"));
					d.setAddescription(rs.getString("addescription"));
					d.setAdid(rs.getInt("adid"));
					d.setAdtags(rs.getString("adtags"));
					dealsList.add(d);
				}
			}
		}
		return dealsList;
	}

	public HashMap<String, Boolean> getUserLikings(
			HashMap<String, Boolean> likings, Connection connection, String uid)
			throws SQLException {
		stmt = connection.createStatement();
		ResultSet rs1;
		String query = "select userlikings from \"UserLikings\" where u_id = "
				+ uid;

		rs1 = stmt.executeQuery(query);
		if (rs1 != null) {
			while (rs1.next()) {
				String likelist = rs1.getString("userlikings");
				String s[] = likelist.split("&");
				for (int i = 0; i < s.length; i++) {
					likings.put(s[i].toLowerCase(), true);
				}
			}
		}
		return likings;
	}
}
// public ArrayList<GSforRegister> postRegistrationDetails(
// Connection connection) throws Exception {
// ArrayList<GSforRegister> feedData = new ArrayList<GSforRegister>();
// try {
// // String uname = request.getParameter("uname");
//
// System.out.println("in s1");
//
// PreparedStatement ps = connection
// .prepareStatement("INSERT INTO \"User\" (firstname, lastname, username, password,gender,age) VALUES (?,?,?,?,?,?)");
// // INSERT INTO "User"
// // (firstname,lastname,username,password,gender,age) Values
// // ('v','s','v','123','f',23);
//
// ps.setString(1, "kalash");
// ps.setString(2, "shah");
// ps.setString(3, "vidhi110");
// ps.setString(4, "123");
// ps.setString(5, "Female");
// ps.setInt(6, 23);
//
// ps.executeUpdate();
//
// System.out.println("Record is inserted into user table!");
//
// System.out.println("in s2");
// // ResultSet rs = ps.executeQuery();
// // System.out.println("in s3");
// //
// // while (rs.next()) {
// // FeedObjects feedObject = new FeedObjects();
// // feedObject.setTitle(rs.getString("title"));
// // feedObject.setDescription(rs.getString("description"));
// // feedObject.setUrl(rs.getString("url"));
// // feedData.add(feedObject);
// // }
// return feedData;
// } catch (Exception e) {
// throw e;
// }
// }

