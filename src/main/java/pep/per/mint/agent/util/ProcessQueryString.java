package pep.per.mint.agent.util;

 
public class ProcessQueryString { 
	 
    private final String command; 
    private final String argument; 
 
    public ProcessQueryString(String command, String argument) { 
        this.command = command; 
        this.argument = argument; 
    } 
 
    public String getCommand() { 
        return command; 
    } 
 
    public String getArgument() { 
        return argument; 
    } 
 
}