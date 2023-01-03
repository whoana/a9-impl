package pep.per.mint.agent.exception;

public class AgentLauncherException extends Exception {

	private static final long serialVersionUID = -3092590695324491174L;
	public final static String ERROR_CD_NOT_FOUND_AGENT_INFO = "NOT_FOUND_AGENT_INFO";
	public final static String ERROR_CD_NOT_RECEIVE_AGENT_INFO = "NOT_RECEIVE_AGENT_INFO";

	String errorCd;
	String errorMsgDetail;


	public AgentLauncherException() {
		super();
	}

	public AgentLauncherException(String msg) {
		super(msg);
	}

	public AgentLauncherException(String errorCd, String errorMsg) {
		this(errorCd, errorMsg, null);
	}


	public AgentLauncherException(String errorCd, String errorMsg, String errorMsgDetail) {
		super(errorMsg);
		this.errorCd = errorCd;
		this.errorMsgDetail = errorMsgDetail;
	}

	public String getErrorCd(){
		return this.errorCd;
	}
}
