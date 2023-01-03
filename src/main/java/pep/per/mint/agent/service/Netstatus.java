/**
 * Copyright 2018 mocomsys Inc. All Rights Reserved.
 */
package pep.per.mint.agent.service;

 
/**
 * <pre>
 * pep.per.mint.agent.service
 * Netstatus.java
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
 * @date Jan 14, 2020
 */
public enum Netstatus {
	
	
	ESTABLISHED("1"),  
	SYN_SENT("9"),  
	SYN_RECV("9"), 
	FIN_WAIT1("9"), 
	FIN_WAIT2("9"), 
	TIME_WAIT("9"),  
	CLOSE("9") , 
	CLOSE_WAIT("9"), 
	LAST_ACK("9"), 
	LISTEN("1"), 
	CLOSING("9"),
	UNKNOWN("9"),
	NONE("9");

	final private String status;
	 
	Netstatus(String status) {this.status = status;} 
	
	public String getStatus() {return status;}
	
	public static void main(String []args) { 
		
		for( Netstatus a : Netstatus.values()) {
			System.out.println(a.name()+ ":"+ a.getStatus());
			
		}
		System.out.println(Netstatus.valueOf("ESTABLISHED").getStatus()); 
	 
	}
	
	
}
