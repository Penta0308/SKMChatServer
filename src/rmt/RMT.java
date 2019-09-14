package rmt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RMT {
	public ArrayList<BufferedWriter> roomuser;
	
	public RMT() {
		roomuser = new ArrayList<BufferedWriter>();
		System.out.println("RMT Opening");
	}
	
	public void send(String message) throws IOException {
		for(BufferedWriter s : roomuser) {
			s.write(message + "\r\n");
			s.flush();
		}
	}
}
