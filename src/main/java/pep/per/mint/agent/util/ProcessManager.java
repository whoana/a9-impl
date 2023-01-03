package pep.per.mint.agent.util;

import java.io.IOException;
import java.util.List;


public interface ProcessManager {

	public static final long PID_UNKNOWN = -1;
    /**
     * @param query
     * @return the pid or {@link #PID_UNKNOWN}
     * @throws IOException
     */
    public long findPidBy(ProcessQueryString query) throws Throwable;
    public List<Long> findPidList(ProcessQueryString query) throws Throwable;

}
