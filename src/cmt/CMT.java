package cmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.*;

import javax.net.ssl.SSLSocket;

import SKMChatServer.SKMChatServer;
import data.Data;
import rmt.RMT;
import smt.SMT;

public class CMT extends Thread {
	private SSLSocket socket;
	
	private BufferedReader br;
	private BufferedWriter bw;
	
	RMT rmt;
	
	public int uid = -1;
	public int sid = -1;
	public int rid = -1;
	
	public CMT() {
		try {
			socket = (SSLSocket)SKMChatServer.sslserversocket.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.start();
		System.out.println("CMT Opening");
	}

	public void run() {
		String fromClient = "";
		
        try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        try {
			while((fromClient = br.readLine()) != null){
				String[] split = fromClient.split(" ");
				System.out.println(fromClient);
			    System.out.flush();
				if(uid == -1)
				{
					if(split[0].compareTo("login") == 0 && split.length == 3) {
						try {
							String exec = "SELECT uid FROM users WHERE email=? AND pw=?";
							java.sql.Connection conn = DriverManager.getConnection(Data.dbURL, Data.dbID, Data.password);
							java.sql.PreparedStatement st = conn.prepareStatement(exec);
							st.setString(1, split[1]);
							st.setString(2, split[2]);
							java.sql.ResultSet rs = st.executeQuery();
							if(rs.next()) {
								uid = rs.getInt(1);
								bw.write("ok uid " + Integer.toString(uid) + "\r\n");
								bw.flush();
								st.close();
								System.out.println(Integer.toString(uid));
							    System.out.flush();
							    continue;
							} else {
								bw.write("error login\r\n");
								bw.write("fin\r\n");
								bw.flush();
								st.close();
								conn.close();
								break;
							}
						} catch (SQLException e) {
							e.printStackTrace();
							break;
						}
						
					} else {
						bw.write("fin\r\n");
					    bw.flush();
					    break;
					}
				} else {
					if(split[0].compareTo("connect") == 0 && split.length == 2) {
						try {
							String exec = "SELECT sid FROM servers WHERE sid=?";
							java.sql.Connection conn = DriverManager.getConnection(Data.dbURL, Data.dbID, Data.password);
							java.sql.PreparedStatement st = conn.prepareStatement(exec);
							st.setInt(1, Integer.parseInt(split[1]));
							java.sql.ResultSet rs = st.executeQuery();
							if(rs.next()) { //exist
								sid = rs.getInt(1);
								bw.write("ok sid " + Integer.toString(sid) + "\r\n");
								bw.flush();
								st.close();
								System.out.println(Integer.toString(sid));
							    System.out.flush();
							    if(SKMChatServer.SMTList.containsKey(sid)) {
							    	SKMChatServer.SMTList.get(sid);
							    } else {
							    	SKMChatServer.SMTList.put(sid, new SMT());
							    }
							} else {
								bw.write("error join");
								bw.flush();
							}
							rs.close();
							st.close();
							conn.close();
						} catch (SQLException e) {
							e.printStackTrace();
							break;
						}
					} else if(split[0].compareTo("cs") == 0 && split.length == 2) {
						try {
							String exec = "INSERT INTO servers (name) VALUES (?) RETURNING sid";
							java.sql.Connection conn = DriverManager.getConnection(Data.dbURL, Data.dbID, Data.password);
							java.sql.PreparedStatement st = conn.prepareStatement(exec);
							st.setString(1, split[1]);
							java.sql.ResultSet rs = st.executeQuery();
							if(!rs.next()) {
								bw.write("error create");
								bw.flush();
								rs.close();
								st.close();
							} else {
								rs.close();
								st.close();
								exec = "CREATE TABLE ? (rid serial PRIMARY KEY, name character varying(64))";
								st = conn.prepareStatement(exec);
								st.setString(1, "server-" + rs.getInt(1));
								if(st.executeUpdate() != -1) {
									bw.write("error cs");
									bw.flush();
									rs.close();
									st.close();
									conn.close();
								} else {
									bw.write("ok sid " + rs.getInt(1) + "\r\n");
									bw.flush();
								}
							}
						} catch (SQLException e) {
							e.printStackTrace();
							break;
						}
					} else if(split[0].compareTo("cr") == 0 && split.length == 2) {
						try {
							String exec = "INSERT INTO rooms (name) VALUES (?) RETURNING rid";
							java.sql.Connection conn = DriverManager.getConnection(Data.dbURL, Data.dbID, Data.password);
							java.sql.PreparedStatement st = conn.prepareStatement(exec);
							st.setString(1, split[1]);
							java.sql.ResultSet rs = st.executeQuery();
							if(!rs.next()) {
								bw.write("error cr");
								bw.flush();
								rs.close();
								st.close();
								conn.close();
							} else {
								int srid = rs.getInt(1);
								rs.close();
								st.close();
								//exec = "CREATE TABLE ? (cid serial PRIMARY KEY, name character varying(64))";
								//st = conn.prepareStatement(exec);
								//st.setString(1, "server-" + rs.getInt(1));
								//if(st.executeUpdate() != -1) {
								//	bw.write("error create");
								//	bw.flush();
								//} else {
									bw.write("ok rid " + srid + "\r\n");
								//	bw.flush();
								//}
							}
						} catch (SQLException e) {
							e.printStackTrace();
							break;
						}
					} else if(split[0].compareTo("quit") == 0 && split.length == 1) {
						if(sid != -1) {
							sid = -1;
							bw.write("ok\r\n");
						} else {
							bw.write("error quit\r\n");
						}
					} else if(split[0].compareTo("join") == 0 && split.length == 2) {
						try {
							String exec = "SELECT rid FROM rooms WHERE rid=?";
							java.sql.Connection conn = DriverManager.getConnection(Data.dbURL, Data.dbID, Data.password);
							java.sql.PreparedStatement st = conn.prepareStatement(exec);
							st.setInt(1, Integer.parseInt(split[1]));
							java.sql.ResultSet rs = st.executeQuery();
							if(rs.next()) { //exist
								rid = rs.getInt(1);
								rs.close();
								st.close();
								conn.close();
								System.out.println(Integer.toString(rid));
							    System.out.flush();
							    if(SKMChatServer.SMTList.containsKey(sid)) {
							    	SMT smt = SKMChatServer.SMTList.get(sid);
							    	if(smt.RMTList.containsKey(rid)) {
							    		rmt = smt.RMTList.get(rid);
							    		rmt.roomuser.add(bw);
							    		bw.write("ok join rid " + rid + "\r\n");
							    		bw.flush();
							    		System.out.println("RMT Connected");
							    	} else {
							    		smt.RMTList.put(rid, new RMT());
							    		bw.write("ok join rid " + rid + " RMT created\r\n");
							    		bw.flush();
							    		System.out.println("RMT Created");
							    	}
							    } else {
							    	bw.write("error join\r\n");
							    	bw.flush();
							    	System.out.println("No SMT Connected");
							    }
							} else {
								bw.write("error join\r\n");
								bw.flush();
								System.out.println("No RMT named");
							}
							rs.close();
							st.close();
							conn.close();
						} catch (SQLException e) {
							e.printStackTrace();
							break;
						}
					} else if(split[0].compareTo("send") == 0) {
						rmt.send(fromClient.substring(5));
						System.out.println(fromClient);
					    System.out.flush();
					}
				}
			    //bw.write(fromClient + "\r\n");
			    //bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			System.out.println("CMT Closing");
		}
	}
}
