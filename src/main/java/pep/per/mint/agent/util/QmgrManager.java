package pep.per.mint.agent.util;

import java.util.List;
 
 
import pep.per.mint.common.data.basic.agent.QmgrInfo;
import pep.per.mint.common.data.basic.agent.QmgrLogs; 


public interface QmgrManager {
	 
	
	public List<QmgrLogs> getQmgrLogs(String agentId, List<QmgrInfo> qmgrInfos) throws Exception;
}
