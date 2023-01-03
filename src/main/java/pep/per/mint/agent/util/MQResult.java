package pep.per.mint.agent.util;

public class MQResult {

	public final static String[] STATUS_TEXT = {
		"", 					// 0
		"MQCHS_BINDING", 		// 1
		"MQCHS_STARTING", 		// 2
		"MQCHS_RUNNING", 		// 3
		"MQCHS_STOPPING",		// 4
		"MQCHS_RETRYING", 		// 5
		"MQCHS_STOPPED", 		// 6
		"MQCHS_REQUESTING", 	// 7
		"MQCHS_PAUSED", 		// 8
		"", 					// 9
		"", 					//10
		"", 					//11
		"",						//12
		"MQCHS_INITIALIZING" 	//13
	};

	public final static String RESULT_OK = "0";

	public static final String RESULT_FAIL = "9";

	int status;

	String msg;

	String errcd;

	int queueDepth = 0;

	int mqrc = 0;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getErrcd() {
		return errcd;
	}

	public void setErrcd(String errcd) {
		this.errcd = errcd;
	}

	public int getQueueDepth() {
		return queueDepth;
	}

	public void setQueueDepth(int queueDepth) {
		this.queueDepth = queueDepth;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getMqrc() {
		return mqrc;
	}

	public void setMqrc(int mqrc) {
		this.mqrc = mqrc;
	}



}
