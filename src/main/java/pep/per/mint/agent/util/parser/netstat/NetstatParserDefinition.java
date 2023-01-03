/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.util.parser.netstat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pep.per.mint.agent.util.parser.annotation.Check;
import pep.per.mint.agent.util.parser.annotation.GetKey;
import pep.per.mint.agent.util.parser.annotation.GetValue;
import pep.per.mint.agent.util.parser.annotation.ParserDefinition;
import pep.per.mint.common.util.Util;
 

/**
 * <pre>
 * test
 * DefaultKeyValueParser.java
 * </pre>
 * @author whoana
 * @date Nov 26, 2019
 */

@ParserDefinition(name = "NetstatOutputParser", type = ParserDefinition.TYPE_K_V_PARSER)
public class NetstatParserDefinition {

	  
	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@GetKey
	public String key(String data) throws Exception{
		String delim = " ";
		String[] results = data.split(delim);
		List<String> columns = new ArrayList<String>();
		 
		for (String string : results) {
			if(string.trim().length() > 0) {
				columns.add(string); 
			}
		}
		
		String key = columns.get(3) +"-"+ columns.get(4); 		
		return key;
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@GetValue
	public Map<String, String> value(String data) throws Exception {
		 
		String delim = " ";
		String[] results = data.split(delim);
		List<String> columns = new ArrayList<String>();
		 for (String string : results) {
			if(string.trim().length() > 0) {
				columns.add(string);
			}
		}
		
		Map<String, String> map = new HashMap<String, String>();
		
		put(map, "protocol"			, 0 , columns);
		put(map, "recvQ"			, 1 , columns);
		put(map, "sendQ"			, 2 , columns);
		put(map, "localAddress"		, 3 , columns);
		put(map, "foreignAddress"	, 4 , columns);
		put(map, "state"			, 5 , columns);
		 
//		String localAddress = map.get("localAddress");
//		if(!Util.isEmpty(localAddress) && localAddress.contains(":")){
//			String [] sprit = localAddress.split(":");
//			if(sprit != null && sprit.length > 1) {
//				localAddress = sprit[0] + "(" + sprit[1] + ")";
//			}
//		}
//		map.put("localAddress", localAddress);
//		
//		String foreignAddress = map.get("foreignAddress");
//		if(!Util.isEmpty(foreignAddress) && foreignAddress.contains(":")){
//			String [] sprit = foreignAddress.split(":");
//			if(sprit != null && sprit.length > 1) {
//				foreignAddress = sprit[0] + "(" + sprit[1] + ")";
//			}
//		}
//		map.put("foreignAddress", foreignAddress);
		 
		return map;		
	}
	
	
	
	/**
	 * @param string
	 * @param i
	 */
	private void put(Map<String, String> map, String key, int i, List<String> columns) {
		if(i < columns.size())
			map.put(key, columns.get(i));
		else 
			map.put(key, "");
	}

	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@Check
	public Boolean check(String data) throws Exception {
		try {
		String delim = " ";
		String[] results = data.split(delim); 	
		
		return results == null || !results[0].toUpperCase().startsWith("TCP") ? new Boolean("false") : new Boolean("true");
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(\\d?)$";
	
	public static void main(String [] args) {
		try {
			
			Pattern patternForMac   = Pattern.compile(IPADDRESS_PATTERN);
//			Pattern patternForLinux = Pattern.compile("[\\d\\.\\d\\.\\d\\.\\d\\:\\d]");
			
			Matcher macher1 = patternForMac.matcher("10.10.1.10.123");
			System.out.println("mac:" + macher1.matches());
//			Matcher macher2 = patternForLinux.matcher("10.10.1.10.22");
//			System.out.println("mac:" + macher2.matches());
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
