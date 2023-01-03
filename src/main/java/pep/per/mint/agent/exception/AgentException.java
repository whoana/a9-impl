package pep.per.mint.agent.exception;

public class AgentException extends Exception {

	String errorCd;
	String errorMsgDetail;
	private static final long serialVersionUID = 4653710642560206307L;

	public AgentException(String errorCd, String errorMsg, String errorMsgDetail) {
		super(errorMsg);
		this.errorCd = errorCd;
		this.errorMsgDetail = errorMsgDetail;
	}

	public AgentException(String errorMsg) {
		super(errorMsg);
	}

	public AgentException(String errorMsg, Throwable e) {
		super(errorMsg, e);
	}

}
