package pep.per.mint.agent.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;
import pep.per.mint.agent.util.QmgrManager;
import pep.per.mint.common.data.basic.agent.QmgrInfo;
import pep.per.mint.common.data.basic.agent.QmgrLogs;
import pep.per.mint.common.util.Util;

//@Service
public class QmgrCheckService {

	Logger logger = LoggerFactory.getLogger(QmgrCheckService.class);

	QmgrManager qmgrManagerForMI;

	QmgrManager qmgrManagerForWMQ;

	public List<QmgrLogs> getQmgrLogs(String agentId, List<QmgrInfo> qmgrs) throws Throwable{
		return getQmgrLogs(agentId, qmgrs, QmgrInfo.TYPE_ILINK);
	}

	public List<QmgrLogs> getQmgrLogs(String agentId, List<QmgrInfo> qmgrs, String type) throws Throwable{
		QmgrManager qmgrManager = Util.isEmpty(type) || QmgrInfo.TYPE_ILINK.equals(type) ? qmgrManagerForMI : qmgrManagerForWMQ;
		return qmgrManager.getQmgrLogs(agentId, qmgrs);

	}

	public QmgrManager getQmgrManagerForMI() {
		return qmgrManagerForMI;
	}

	public void setQmgrManagerForMI(QmgrManager qmgrManagerForMI) {
		this.qmgrManagerForMI = qmgrManagerForMI;
	}

	public QmgrManager getQmgrManagerForWMQ() {
		return qmgrManagerForWMQ;
	}

	public void setQmgrManagerForWMQ(QmgrManager qmgrManagerForWMQ) {
		this.qmgrManagerForWMQ = qmgrManagerForWMQ;
	}




}
