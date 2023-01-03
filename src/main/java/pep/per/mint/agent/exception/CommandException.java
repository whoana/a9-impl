package pep.per.mint.agent.exception;

public class CommandException extends Exception {

	private static final long serialVersionUID = 5142289311001778627L;
	String errorCd;
	String errorMsgDetail;


	public CommandException() {
		super();
	}

	public CommandException(String msg) {
		super(msg);
	}

	public CommandException(String errorCd, String errorMsg, String errorMsgDetail) {
		super(errorMsg);
		this.errorCd = errorCd;
		this.errorMsgDetail = errorMsgDetail;
	}

	public CommandException(Throwable t) {
		super(t);
	}
}
