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

public class CMT extends Thread {
	private SSLSocket socket;
	private BufferedReader br;
	private BufferedWriter bw;
	
	public int uid = -1;
	
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
				if(uid == -1)
				{
					if(split[0].compareTo("login") == 0) {
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
							} else {
								bw.write("error login\r\n");
								bw.write("fin\r\n");
								bw.flush();
								st.close();
								break;
							}
							conn.close();
						} catch (SQLException e) {
							e.printStackTrace();
							break;
						}
						
					} else {
						bw.write("fin\r\n");
					    bw.flush();
					    break;
					}
				}
			    System.out.println(fromClient);
			    System.out.flush();
			    bw.write(fromClient + "\r\n");
			    bw.flush();
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
