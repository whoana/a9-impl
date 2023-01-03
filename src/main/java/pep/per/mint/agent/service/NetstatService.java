/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

import pep.per.mint.agent.util.parser.KeyValueParser;
import pep.per.mint.agent.util.parser.netstat.NetstatParserDefinition;
import pep.per.mint.common.util.Util;

/**
 * <pre>
 * pep.per.mint.agent.service
 * NetstatService.java
 *
 * ---------------------------------------------------------
 * State
 *      The state of the socket. Since there are no states in raw mode and usually no states used in UDP and UDPLite, this column may be left blank. Normally this can be one of several values:
 *
 *      ESTABLISHED
 *             The socket has an established connection.
 *
 *      SYN_SENT
 *             The socket is actively attempting to establish a connection.
 *
 *      SYN_RECV
 *             A connection request has been received from the network.
 *
 *      FIN_WAIT1
 *             The socket is closed, and the connection is shutting down.
 *
 *      FIN_WAIT2
 *             Connection is closed, and the socket is waiting for a shutdown from the remote end.
 *
 *      TIME_WAIT
 *             The socket is waiting after close to handle packets still in the network.
 *
 *      CLOSE  The socket is not being used.
 *
 *      CLOSE_WAIT
 *             The remote end has shut down, waiting for the socket to close.
 *
 *      LAST_ACK
 *             The remote end has shut down, and the socket is closed. Waiting for acknowledgement.
 *
 *      LISTEN The socket is listening for incoming connections.  Such sockets are not included in the output unless you specify the --listening (-l) or --all (-a) option.
 *
 *      CLOSING
 *             Both sockets are shut down but we still don't have all our data sent.
 *
 *      UNKNOWN
 *             The state of the socket is unknown.
 *
 *
 * </pre>
 * @author whoana
 * @date Dec 2, 2019
 */
//@Service
public class NetstatService {

	Logger logger = LoggerFactory.getLogger(getClass());

	private static final String TYPE_SERVER = "1";
	private static final String TYPE_CLIENT = "2";

	List<Map<String,String>> netstatCheckList = new ArrayList<Map<String,String>>();

	Map<String, Map<String,String>> netstatCheckMap = new HashMap<String, Map<String,String>>();

	public List<Map<String, String>> netstat(String cmd) throws Exception{

		List<Map<String, String>> res = new ArrayList<Map<String, String>>();

		if(netstatCheckList == null || netstatCheckList.size() == 0) {
			return res;
		}else {

			List<Map<String, String>>logs = null;
			List<Map<String, String>>establishedLogs = null;
			
			KeyValueParser parser = new KeyValueParser(new NetstatParserDefinition());

			BufferedReader br =  null;
			try {

				Process p = Runtime.getRuntime().exec(cmd);
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while(true) {
					String record = br.readLine();
						System.out.println(record);
					if(record == null) break;
					boolean ok = parser.check(record);
					if(ok) {
						parser.parse(record);
					}else {
						//.out.println("skip record:" + record);
					}
				}



				logs = parser.getLogs();
				if(logs != null) {
					establishedLogs = new ArrayList<Map<String, String>>();
					//ESTABLISHED 상태만 체크하도록 수정. 
					for(Map<String, String> log : logs) {
						if(Netstatus.valueOf(log.get("state")) != Netstatus.ESTABLISHED ) continue;
						establishedLogs.add(log);
					}
				}

			}finally {
				if(br != null)
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}


			String date = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);

			//logger.debug("netstatCheckList:" + Util.toJSONString(netstatCheckList));
			//logger.debug("-------------------------------------------------------");
			//logger.debug("logs:" + Util.toJSONString(logs));

			for (Map<String, String> map : netstatCheckList) {

				String serverId = map.get("serverId");
				String serverNm = map.get("serverNm");
				String serverIp = map.get("ip");
				if(serverIp == null || !serverIp.contains(":")) continue;
				String type     = map.get("type");
				String oldState = map.get("state");

				Map<String, String> netstat = null;

				if(establishedLogs != null) {
				 
					
					for(Map<String, String> log : establishedLogs) {

						String state 		  = log.get("state");
						String localAddress   = log.get("localAddress");
						String foreignAddress = log.get("foreignAddress");
 
						//ESTABLISHED 상태만 체크하도록 수정. 
						if(Netstatus.valueOf(state) != Netstatus.ESTABLISHED ) continue;
						
						if(type.equals(TYPE_SERVER)) {

							//String[] address = serverIp.split(":");
							String[] checkAddress = parseAddress(serverIp);
							if(checkAddress.length < 2) continue;

							String checkIp   = checkAddress[0];
							String checkPort = checkAddress[1];

							String[] local = parseAddress(localAddress);
							if(local.length < 2) continue;
							//String localIp = local[0];
							String localPort = local[1];

							String[] foreign = parseAddress(foreignAddress);
							if(foreign.length < 2) continue;
							String foreignIp = foreign[0];
							//String foreignPort = foreign[1];


							if(serverIp.equals(foreignAddress) || (checkPort.endsWith(localPort) && checkIp.equals(foreignIp))) {
								netstat = log;
								break;
							}


						}else if(type.equals(TYPE_CLIENT)) {
							if(serverIp.equals(foreignAddress)){
								netstat = log;
								break;
							}
						}
					}
				}

				if(netstat == null) {
					netstat = new HashMap<String,String>();
					netstat.put("protocol", 	 "TCP");
					netstat.put("recvQ", 		 "0");
					netstat.put("sendQ", 		 "0");
					netstat.put("localAddress",  "NONE");
					netstat.put("foreignAddress","NONE");
					netstat.put("state", 		 "NONE");
				}
				netstat.put("serverId", serverId);
				netstat.put("serverNm", serverNm);

				netstat.put("regDate", date);
				netstat.put("modDate", date);

				//상태값 매핑 
				//0 -> "NONE"
				//1 -> "CONNECT    (ESTABLISHED)"
				//2 -> "DISCONNECT (NOT ESTABLISHED)"
				netstat.put("state", Netstatus.valueOf(netstat.get("state")).getStatus());				
				//logger.debug("check old(state:"+ map.get("state")     +"):" + Util.toJSONString(map));
				//logger.debug("check new(state:"+ netstat.get("state") +"):" + Util.toJSONString(netstat));
				//상태 변경시에만 로그를 전송한다. 
				if(!oldState.equalsIgnoreCase(netstat.get("state"))) {
					res.add(netstat);
				}
				if(establishedLogs != null && establishedLogs.contains(netstat)) establishedLogs.remove(netstat);

			}

			return res;

		}
	}

/**
	 * @param serverIp
	 * @return
	 */
	private String[] parseAddress(String addressInfo) {
		String[] address = {"", ""};
		String[] result = addressInfo.split(":");
		if(result == null || result.length < 2) return address;
		address[0] = result[0];
		address[1] = result[result.length-1];
		return address;
	}
 
	public static void main(String args[]) {
		 try {

			NetstatService service = new NetstatService();
			
			String[] ips = service.parseAddress(":::8080");
			System.out.println(ips[0]);
			System.out.println(ips[1]);


			List<Map<String, String>> logs = service.netstat("netstat -anp TCP");

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
		}
	}

	/**
	 * @param responseObject
	 */
	public void setCheckList(Object responseObject) {
		netstatCheckList = (List<Map<String,String>>)responseObject;
		for (Map<String, String> row : netstatCheckList) {
			String serverId =  row.get("serverId");
			netstatCheckMap.put(serverId, row);
			logger.debug("serverId: " +serverId + "hash:" + row.hashCode());
		}
	}

	public void updateState(String serverId, String state) {
		logger.debug("update state serverId:"+serverId+":hash:"+netstatCheckMap.get(serverId).hashCode());
		netstatCheckMap.get(serverId).put("state", state);

	}

}
