package smt;

import java.util.HashMap;
import java.util.Map;

import rmt.RMT;

public class SMT {
	public Map<Integer, RMT> RMTList;
	
	public SMT() {
		RMTList = new HashMap<Integer, RMT>();
		System.out.println("SMT Opening");
	}
}
