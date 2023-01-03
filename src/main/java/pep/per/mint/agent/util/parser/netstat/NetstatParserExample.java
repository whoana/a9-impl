/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.util.parser.netstat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map; 

import pep.per.mint.agent.util.parser.KeyValueParser;
 
/**
 * <pre>
 * NetstatParserExample.java
 * parser output :
 *  map.put("protocol", 	  columns.get(0));
 *  map.put("recvQ", 		  columns.get(1));
 *  map.put("sendQ", 		  columns.get(2));
 *  map.put("localAddress",   columns.get(3));
 *  map.put("foreignAddress", columns.get(4));
 *  map.put("state", 		  columns.get(5));	
 * </pre>
 * @author whoana
 * @date Nov 26, 2019
 */
public class NetstatParserExample {

	public static void main(String args[]) {
		BufferedReader br =  null;
		try {

			/**
			 * 1.read line
			 * 2.get primary key
			 * 3.save as key and value
			 */
			if(args == null || args.length == 0 || args[0] == null) {
				System.out.println("We need a shell file to execute netstat.");
				System.out.println("java NetstatParserExample /Users/whoana/DEV/workspace-sts/mint-test/myrun.sh");
				System.exit(-1);
			}
			
			//Process p = Runtime.getRuntime().exec("/Users/whoana/DEV/workspace-sts/mint-test/myrun.sh");
			Process p = Runtime.getRuntime().exec(args[0]);
			
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			NetstatParserDefinition definition = new NetstatParserDefinition();
			KeyValueParser parser = new KeyValueParser(definition);
			
			while(true) {
				String record = br.readLine();
				if(record == null) break;
				boolean ok = parser.check(record);
				if(ok) {
					parser.parse(record);
				}else {
					//.out.println("skip record:" + record);					
				}
			}
			
			List<Map<String, String>> logs = parser.getLogs();
			 
			System.out.println("protocol	recvQ	sendQ	localAddress	foreignAddress	state	");
			System.out.println("------------------------------------------------------------------------");
			for (Map<String, String> row : logs) {
				System.out.println(
					row.get("protocol") + "	" + 
					row.get("recvQ") + "	" + 
					row.get("sendQ") + "	" + 
					row.get("localAddress") + "	" + 
					row.get("foreignAddress") + "	" + 
					row.get("state"));
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

}
