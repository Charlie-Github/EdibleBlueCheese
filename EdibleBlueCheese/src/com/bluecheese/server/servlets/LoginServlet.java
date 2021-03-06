package com.bluecheese.server.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet{

	private static final String userName = "root";
	private static final String passWord = "";
//	private static final String passWord = "edible2014";
	public LoginServlet() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String url = null;
		Connection conn = null;
		String uid = "";
		String upwd = "";
		JSONObject result = new JSONObject();
		//set the header of http response
		resp.setContentType("application/json;charset=utf-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter writer = resp.getWriter();
		try {
			//read the input request and transfer to a json object 
			try {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
				String s = null;
				while((s = br.readLine()) != null) {
					sb.append(s);
				}
				JSONObject data = new JSONObject(sb.toString());
				if(!data.isNull("uid") && !data.isNull("upwd")) {
					uid = data.getString("uid");
					upwd = data.getString("upwd");
				}
				else {
					result.put("status", false);
					result.put("log", "Invalid user id or password!");
					writer.println(result);
					writer.flush();
					return;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				return;
			}
			
			//check if uid and upwd are legal
			
			
			//connect to database
			try {
				if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
		    		// Load the class that provides the new "jdbc:google:mysql://" prefix.
		    		Class.forName("com.mysql.jdbc.GoogleDriver");
		    		url = "jdbc:google:mysql://edible-bluecheese-server:bluecheese/bluecheese";
		    	} else {
		    		// Local MySQL instance to use during development.
		    		Class.forName("com.mysql.jdbc.Driver");
		    		url = "jdbc:mysql://127.0.0.1:3721/bluecheese";
		    	}
				conn = DriverManager.getConnection(url, userName, passWord);
			} catch (Exception e) {
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				return;
			}
			
		    
		    try {			
				//query the database for specific user
				String statement = "SELECT * FROM User WHERE uid = ? and upwd = ?";
				PreparedStatement stmt = conn.prepareStatement(statement);
				stmt.setString(1, uid);
				stmt.setString(2, upwd);
				ResultSet rs = stmt.executeQuery();
				//if there is no such a user id or password is wrong
				result.put("status", false);
				result.put("log", "User ID does not exist or password is wrong");
				//else return the info of this user
				while(rs.next()) {
					result.put("uid", rs.getString("uid"));
					result.put("uname", rs.getString("uname"));
					result.put("utype", rs.getInt("utype"));
					result.put("ucreate_time", rs.getDate("ucreate_time").toString());
					Blob blob = rs.getBlob("uselfie");
					if(blob != null) {
						byte[] selfie = getByteArray(blob);
						result.put("uselfie", selfie);
					}
					else {
						result.put("uselfie", JSONObject.NULL);
					}
					result.put("status", true);
					result.put("log", "Login Success!");
				}
				
				writer.println(result);
				writer.flush();
				
				
			} catch (SQLException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result.put("status", false);
				result.put("log", e.toString());
				writer.println(result);
				return;
			} 
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			try {
				writer.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	    
	}
	
	private byte[] getByteArray(Blob blob) throws SQLException, IOException {
		BufferedInputStream is = new BufferedInputStream(blob.getBinaryStream());
  	  	byte[] imgData = new byte[(int) blob.length()];
  	  	int len = imgData.length;
  	  	int offset = 0;
  	  	int read = 0;
  	  	while (offset < len && (read = is.read(imgData, offset, len - offset)) >= 0) {  
  		  	offset += read;  
  	  	} 
  	  	return imgData;
	}

	
}
