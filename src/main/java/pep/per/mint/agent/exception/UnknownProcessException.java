package pep.per.mint.agent.exception;

public class UnknownProcessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3741302259265164812L;
	
	public UnknownProcessException(){
		super();
	}
	
	public UnknownProcessException(String msg){
		super(msg);
	}
}
