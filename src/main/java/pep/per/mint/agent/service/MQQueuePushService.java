package pep.per.mint.agent.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.omg.CORBA._PolicyStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.MQCFIL;
import com.ibm.mq.pcf.MQCFIN;
import com.ibm.mq.pcf.MQCFST;
import com.ibm.mq.pcf.PCFAgent;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.ibm.mq.pcf.PCFParameter;

// import pep.per.mint.agent.controller.MQObjectMonitorController;
import pep.per.mint.agent.exception.AgentException;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.Extension;
import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.data.basic.agent.MonitorItem;
import pep.per.mint.common.data.basic.agent.QmgrInfo;
import pep.per.mint.common.data.basic.qmgr.QmgrProperty;
import pep.per.mint.common.data.basic.qmgr.QueueProperty;
import pep.per.mint.common.msg.handler.ServiceCodeConstant;
import pep.per.mint.common.util.Util;

public class MQQueuePushService implements Runnable {

	Logger logger = LoggerFactory.getLogger(MQQueuePushService.class);

	String qmgrNm = "";
	String queueNm = "";
	int delay = 1;
	boolean isRun = true;

	// MQObjectMonitorController parent;

	public static String DEFAULT_SVRCONN_CHANNEL = "SYSTEM.DEF.SVRCONN";

	String frontSessionId = "";

	PCFAgent qmanager = null;

	boolean isConn = false;

	/**
	 * MQObjectMonitorController 요놈땜에 기존 소스는 못쓴다.
	 * 
	 * @deprecated
	 * @param _qmgrNm
	 * @param _queueNm
	 * @param _delay
	 * @param _parent
	 * @param _frontSessionId
	 */
	// public MQQueuePushService(String _qmgrNm, String _queueNm, int _delay,
	// MQObjectMonitorController _parent, String _frontSessionId) {
	// qmgrNm = _qmgrNm;
	// queueNm = _queueNm;
	// delay = _delay;
	// parent = _parent;
	// frontSessionId = _frontSessionId;
	// }
	IIPAgentInfo agentInfo;

	public MQQueuePushService(String _qmgrNm, String _queueNm, int _delay, IIPAgentInfo agentInfo,
			String _frontSessionId) {
		qmgrNm = _qmgrNm;
		queueNm = _queueNm;
		delay = _delay;
		this.agentInfo = agentInfo;
		frontSessionId = _frontSessionId;
	}

	/**
	 * @deprecated
	 */
	public List<QueueProperty> getQStatus(Map agent) throws AgentException {
		ArrayList<QueueProperty> list = new ArrayList<QueueProperty>();
		PCFAgent qmanager = null;
		try {

			String qmgrName = (String) agent.get("qmgrNm");
			qmanager = new PCFAgent();
			qmanager.setCharacterSet(1208);

			qmanager.setWaitInterval(5);
			qmanager.connect(qmgrName);

			QueueProperty queueInfo = new QueueProperty();

			PCFParameter[] parameters = { new MQCFST(CMQC.MQCA_Q_NAME, (String) agent.get("queueNm")) };
			MQMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			responses = qmanager.send(CMQCFC.MQCMD_RESET_Q_STATS, parameters);
			for (int i = 0; i < responses.length; i++) {
				cfh = new MQCFH(responses[i]);

				if (cfh.reason == 0) {
					for (int j = 0; j < cfh.parameterCount; j++) {
						p = PCFParameter.nextParameter(responses[i]);
						switch (p.getParameter()) {
							case CMQC.MQCA_Q_NAME:
								queueInfo.setName(((String) p.getValue()).trim());
								break;
							case CMQC.MQIA_CURRENT_Q_DEPTH:
								queueInfo.setCurrentDepth(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_ENQ_COUNT:
								queueInfo.setOpenInputCount(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_DEQ_COUNT:
								queueInfo.setOpenOutputCount(((Integer) p.getValue()));
								break;
							default:
						}
					}
				}
			}

			// 큐 깊이
			{
				int[] attrs = null;
				attrs = new int[1];
				attrs[0] = CMQC.MQIA_CURRENT_Q_DEPTH;

				PCFParameter[] parameters1 = { new MQCFST(CMQC.MQCA_Q_NAME, (String) agent.get("queueNm")),
						new MQCFIN(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL), new MQCFIL(CMQCFC.MQIACF_Q_ATTRS, attrs) };
				MQMessage[] responses1;
				MQCFH cfh1;
				PCFParameter p1;

				responses1 = qmanager.send(CMQCFC.MQCMD_INQUIRE_Q, parameters1);
				for (int i = 0; i < responses1.length; i++) {
					cfh1 = new MQCFH(responses1[i]);
					if (cfh1.reason == 0) {
						for (int j = 0; j < cfh1.parameterCount; j++) {
							p1 = PCFParameter.nextParameter(responses1[i]);
							switch (p1.getParameter()) {
								case CMQC.MQCA_Q_NAME:
									queueInfo.setName(((String) p1.getValue()).trim());
									break;
								case CMQC.MQIA_CURRENT_Q_DEPTH:
									queueInfo.setCurrentDepth(((Integer) p1.getValue()));
									break;
								default:
							}
						}

					}
				}

			}
			list.add(queueInfo);
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn(e.getMessage(), e);
		} catch (MQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (qmanager != null)
				try {
					qmanager.disconnect();
				} catch (MQException e) {
				}
		}

		return list;
	}

	/**
	 * @deprecated
	 */
	private List<QueueProperty> getQStatusLocal() throws AgentException {
		ArrayList<QueueProperty> list = new ArrayList<QueueProperty>();
		PCFAgent qmanager = null;
		try {

			qmanager = new PCFAgent();
			qmanager.setCharacterSet(1208);

			qmanager.setWaitInterval(5);
			qmanager.connect(qmgrNm);

			int[] attrs = null;
			attrs = new int[3];
			// attrs[0]=CMQC.MQCA_Q_NAME;
			attrs[0] = CMQC.MQIA_CURRENT_Q_DEPTH;
			// attrs[1]=CMQC.MQIA_OPEN_INPUT_COUNT;
			// attrs[2]=CMQC.MQIA_OPEN_OUTPUT_COUNT;

			attrs[1] = CMQC.MQIA_MSG_DEQ_COUNT;
			attrs[2] = CMQC.MQIA_MSG_ENQ_COUNT;

			PCFParameter[] parameters = { new MQCFST(CMQC.MQCA_Q_NAME, queueNm) };
			MQMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			responses = qmanager.send(CMQCFC.MQCMD_RESET_Q_STATS, parameters);
			for (int i = 0; i < responses.length; i++) {
				cfh = new MQCFH(responses[i]);
				QueueProperty queueInfo = new QueueProperty();
				if (cfh.reason == 0) {
					for (int j = 0; j < cfh.parameterCount; j++) {
						p = PCFParameter.nextParameter(responses[i]);
						switch (p.getParameter()) {
							case CMQC.MQCA_Q_NAME:
								queueInfo.setName(((String) p.getValue()).trim());
								break;
							case CMQC.MQIA_CURRENT_Q_DEPTH:
								queueInfo.setCurrentDepth(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_ENQ_COUNT:
								queueInfo.setOpenInputCount(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_DEQ_COUNT:
								queueInfo.setOpenOutputCount(((Integer) p.getValue()));
								break;
							default:
						}
					}
					list.add(queueInfo);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn(e.getMessage(), e);
		} catch (MQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (qmanager != null)
				try {
					qmanager.disconnect();
				} catch (MQException e) {
				}
		}

		return list;
	}

	/**
	 * @deprecated
	 */
	private QueueProperty getQStatusLocalForIIPAgentInfo() throws AgentException {
		QueueProperty queueProperty = new QueueProperty();
		PCFAgent qmanager = null;
		queueProperty.setQmgrName(qmgrNm);

		try {
			List<MonitorItem> items = agentInfo.getMonitorItems();
			List<QmgrProperty> mqList = new ArrayList();
			for (int i = 0; i < items.size(); i++) {
				MonitorItem item = items.get(i);
				if (MonitorItem.ITEM_TYPE_QMGR.equals(item.getItemType())) {
					if (item.getQmgrs() != null && item.getQmgrs().size() > 0) {
						for (QmgrInfo qmgrInfo : item.getQmgrs()) {
							if (QmgrInfo.TYPE_MQ.equals(qmgrInfo.getType())
									&& qmgrInfo.getQmgrNm().equalsIgnoreCase(qmgrNm)) {
								try {
									qmanager = new PCFMessageAgent(qmgrInfo.getIp(),
											Integer.parseInt(qmgrInfo.getPort()), DEFAULT_SVRCONN_CHANNEL);
								} catch (Exception e) {
								}
							}
						} // end of for
					} // end of item if
				}
			}

			if (qmanager == null) {
				throw new AgentException("IIPAgent QmgrInfo not invalid.");
			}
			qmanager.setCharacterSet(1208);
			qmanager.setWaitInterval(5);

			PCFParameter[] parameters = { new MQCFST(CMQC.MQCA_Q_NAME, queueNm) };
			MQMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			responses = qmanager.send(CMQCFC.MQCMD_RESET_Q_STATS, parameters);
			for (int i = 0; i < responses.length; i++) {
				cfh = new MQCFH(responses[i]);
				if (cfh.reason == 0) {
					for (int j = 0; j < cfh.parameterCount; j++) {
						p = PCFParameter.nextParameter(responses[i]);
						switch (p.getParameter()) {
							case CMQC.MQCA_Q_NAME:
								queueProperty.setName(((String) p.getValue()).trim());
								break;
							case CMQC.MQIA_CURRENT_Q_DEPTH:
								queueProperty.setCurrentDepth(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_ENQ_COUNT:
								queueProperty.setOpenInputCount(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_DEQ_COUNT:
								queueProperty.setOpenOutputCount(((Integer) p.getValue()));
								break;
							default:
						}
					}
				}
			}

			// 큐 깊이
			{
				int[] attrs = null;
				attrs = new int[1];
				attrs[0] = CMQC.MQIA_CURRENT_Q_DEPTH;

				PCFParameter[] parameters1 = { new MQCFST(CMQC.MQCA_Q_NAME, queueNm),
						new MQCFIN(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL), new MQCFIL(CMQCFC.MQIACF_Q_ATTRS, attrs) };
				MQMessage[] responses1;
				MQCFH cfh1;
				PCFParameter p1;

				responses1 = qmanager.send(CMQCFC.MQCMD_INQUIRE_Q, parameters1);
				for (int i = 0; i < responses1.length; i++) {
					cfh1 = new MQCFH(responses1[i]);
					if (cfh1.reason == 0) {
						for (int j = 0; j < cfh1.parameterCount; j++) {
							p1 = PCFParameter.nextParameter(responses1[i]);
							switch (p1.getParameter()) {
								case CMQC.MQIA_CURRENT_Q_DEPTH:
									queueProperty.setCurrentDepth(((Integer) p1.getValue()));
									break;
								default:
							}
						}

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn(e.getMessage(), e);
		} catch (MQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (qmanager != null)
				try {
					qmanager.disconnect();
				} catch (MQException e) {
				}
		}

		return queueProperty;
	}

	private QueueProperty getQStatusLocalForIIPAgentInfoConnectionLess() throws AgentException {
		QueueProperty queueProperty = new QueueProperty();
		queueProperty.setQmgrName(qmgrNm);
		queueProperty.setName(queueNm);
		try {
			if (qmanager == null) {
				isConn = false;
				throw new AgentException("IIPAgent QmgrInfo not invalid.");
			}
			qmanager.setCharacterSet(1208);
			qmanager.setWaitInterval(5);

			PCFParameter[] parameters = { new MQCFST(CMQC.MQCA_Q_NAME, queueNm) };
			MQMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			responses = qmanager.send(CMQCFC.MQCMD_RESET_Q_STATS, parameters);
			for (int i = 0; i < responses.length; i++) {
				cfh = new MQCFH(responses[i]);
				if (cfh.reason == 0) {
					for (int j = 0; j < cfh.parameterCount; j++) {
						p = PCFParameter.nextParameter(responses[i]);
						switch (p.getParameter()) {
							case CMQC.MQCA_Q_NAME:
								// queueProperty.setName(((String)p.getValue()).trim());
								break;
							case CMQC.MQIA_CURRENT_Q_DEPTH:
								queueProperty.setCurrentDepth(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_ENQ_COUNT:
								queueProperty.setOpenInputCount(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_DEQ_COUNT:
								queueProperty.setOpenOutputCount(((Integer) p.getValue()));
								break;
							// case CMQC.MQIA_TIME_SINCE_RESET:
							// queueProperty.setDesc((Integer)p.getValue() +"");
							// break;
							default:
						}
					}
				}
			}

			// 큐 깊이
			{
				int[] attrs = null;
				attrs = new int[1];
				attrs[0] = CMQC.MQIA_CURRENT_Q_DEPTH;

				PCFParameter[] parameters1 = { new MQCFST(CMQC.MQCA_Q_NAME, queueNm),
						new MQCFIN(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL), new MQCFIL(CMQCFC.MQIACF_Q_ATTRS, attrs) };
				MQMessage[] responses1;
				MQCFH cfh1;
				PCFParameter p1;

				responses1 = qmanager.send(CMQCFC.MQCMD_INQUIRE_Q, parameters1);
				for (int i = 0; i < responses1.length; i++) {
					cfh1 = new MQCFH(responses1[i]);
					if (cfh1.reason == 0) {
						for (int j = 0; j < cfh1.parameterCount; j++) {
							p1 = PCFParameter.nextParameter(responses1[i]);
							switch (p1.getParameter()) {
								case CMQC.MQIA_CURRENT_Q_DEPTH:
									queueProperty.setCurrentDepth(((Integer) p1.getValue()));
									break;
								default:
							}
						}

					}
				}
			}
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
			throw new AgentException("IIPAgent IOException" + e.getMessage());
		} catch (MQException e) {
			logger.warn(e.getMessage(), e);
			throw new AgentException("IIPAgent Exception." + e.getMessage() + "[" + e.getErrorCode() + "]");
		} finally {

		}

		return queueProperty;
	}

	/**
	 * @deprecated
	 */
	private List<QueueProperty> getQStatusLocalListForIIPAgentInfo() throws AgentException {
		ArrayList<QueueProperty> list = new ArrayList<QueueProperty>();
		PCFAgent qmanager = null;
		try {
			List<MonitorItem> items = agentInfo.getMonitorItems();
			List<QmgrProperty> mqList = new ArrayList();
			for (int i = 0; i < items.size(); i++) {
				MonitorItem item = items.get(i);
				if (MonitorItem.ITEM_TYPE_QMGR.equals(item.getItemType())) {
					if (item.getQmgrs() != null && item.getQmgrs().size() > 0) {
						for (QmgrInfo qmgrInfo : item.getQmgrs()) {
							if (QmgrInfo.TYPE_MQ.equals(qmgrInfo.getType())
									&& qmgrInfo.getQmgrNm().equalsIgnoreCase(qmgrNm)) {
								try {
									qmanager = new PCFMessageAgent(qmgrInfo.getIp(),
											Integer.parseInt(qmgrInfo.getPort()), DEFAULT_SVRCONN_CHANNEL);
								} catch (Exception e) {
								}
							}
						} // end of for
					} // end of item if
				}
			}

			if (qmanager == null) {
				throw new AgentException("IIPAgent QmgrInfo not invalid.");
			}
			qmanager.setCharacterSet(1208);
			qmanager.setWaitInterval(5);

			int[] attrs = null;
			attrs = new int[3];
			// attrs[0]=CMQC.MQCA_Q_NAME;
			attrs[0] = CMQC.MQIA_CURRENT_Q_DEPTH;
			// attrs[1]=CMQC.MQIA_OPEN_INPUT_COUNT;
			// attrs[2]=CMQC.MQIA_OPEN_OUTPUT_COUNT;

			attrs[1] = CMQC.MQIA_MSG_DEQ_COUNT;
			attrs[2] = CMQC.MQIA_MSG_ENQ_COUNT;

			PCFParameter[] parameters = { new MQCFST(CMQC.MQCA_Q_NAME, queueNm) };
			MQMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			responses = qmanager.send(CMQCFC.MQCMD_RESET_Q_STATS, parameters);
			for (int i = 0; i < responses.length; i++) {
				cfh = new MQCFH(responses[i]);
				QueueProperty queueInfo = new QueueProperty();
				if (cfh.reason == 0) {
					for (int j = 0; j < cfh.parameterCount; j++) {
						p = PCFParameter.nextParameter(responses[i]);
						switch (p.getParameter()) {
							case CMQC.MQCA_Q_NAME:
								queueInfo.setName(((String) p.getValue()).trim());
								break;
							case CMQC.MQIA_CURRENT_Q_DEPTH:
								queueInfo.setCurrentDepth(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_ENQ_COUNT:
								queueInfo.setOpenInputCount(((Integer) p.getValue()));
								break;
							case CMQC.MQIA_MSG_DEQ_COUNT:
								queueInfo.setOpenOutputCount(((Integer) p.getValue()));
								break;
							default:
						}
					}
					list.add(queueInfo);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn(e.getMessage(), e);
		} catch (MQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (qmanager != null)
				try {
					qmanager.disconnect();
				} catch (MQException e) {
				}
		}

		return list;
	}

	@Override
	public void run() {
		logger.info(Thread.currentThread().getName() + " " + Util.getFormatedDate());
		long currentTime, executionTime;
		executionTime = System.currentTimeMillis();

		while (isRun) {
			pcfConnect();
			boolean taskFired = false;
			currentTime = System.currentTimeMillis();
			taskFired = (executionTime <= currentTime);
			if (taskFired) {
				ComMessage<Map, QueueProperty> msg = new ComMessage();
				msg.setStartTime(Util.getFormatedDate());
				msg.setUserId(agentInfo.getAgentNm());

				Map reqObj = new HashMap();
				reqObj.put("agentNm", agentInfo.getAgentNm());
				reqObj.put("qmgrNm", qmgrNm);
				reqObj.put("queueNm", queueNm);

				msg.setRequestObject(reqObj);

				Extension ext = new Extension();
				ext.setMsgType(Extension.MSG_TYPE_PUSH);
				ext.setServiceCd(ServiceCodeConstant.WS0053);
				ext.setFrontSessionId(frontSessionId);
				msg.setExtension(ext);
				try {

					// list = getQStatusLocal();
					// QueueProperty queueProperty = getQStatusLocalForIIPAgentInfo();
					QueueProperty queueProperty = getQStatusLocalForIIPAgentInfoConnectionLess();
					msg.setCheckSession(false);
					msg.setResponseObject(queueProperty);

				} catch (AgentException e1) {
					this.isRun = false;
					logger.warn(e1.getMessage(), e1);

					msg.setCheckSession(false);
					msg.setErrorCd("9");
					msg.setErrorMsg(e1.getMessage());
				}

				// whoana commented 20221202 수정
				// parent.putQueue(msg);

				logger.debug("QueueMonitor elapsedtime[" + (System.currentTimeMillis() - currentTime) + "]ms");
				logger.debug("Check :" + Thread.currentThread().getName() + " " + Util.getFormatedDate() + "  " + qmgrNm
						+ "-" + queueNm);
				long realDelayTime = (delay * 1000) - (System.currentTimeMillis() - currentTime);
				if (realDelayTime < 0 || realDelayTime > (delay * 1000)) {
					realDelayTime = 0;
				}
				executionTime = executionTime + (delay * 1000) - (System.currentTimeMillis() - currentTime);
				try {
					Thread.sleep(realDelayTime);
				} catch (InterruptedException e) {
				}
			} else {
				logger.info("Check executionTime-currentTime :" + (executionTime - currentTime) + " ");
				try {
					Thread.sleep(executionTime - currentTime);
				} catch (InterruptedException e) {
				}
			}
		}

		pcfDisconnect();
	}

	public void setStop() {
		this.isRun = false;
		pcfDisconnect();
		logger.warn("this MQObject Thread stop!!!" + qmgrNm + "  " + queueNm);
	}

	private void pcfConnect() {

		if (!isConn) {
			List<MonitorItem> items = agentInfo.getMonitorItems();
			List<QmgrProperty> mqList = new ArrayList();
			for (int i = 0; i < items.size(); i++) {
				MonitorItem item = items.get(i);
				if (MonitorItem.ITEM_TYPE_QMGR.equals(item.getItemType())) {
					if (item.getQmgrs() != null && item.getQmgrs().size() > 0) {
						for (QmgrInfo qmgrInfo : item.getQmgrs()) {
							if (QmgrInfo.TYPE_MQ.equals(qmgrInfo.getType())
									&& qmgrInfo.getQmgrNm().equalsIgnoreCase(qmgrNm)) {
								try {
									qmanager = new PCFMessageAgent(qmgrInfo.getIp(),
											Integer.parseInt(qmgrInfo.getPort()), DEFAULT_SVRCONN_CHANNEL);
									isConn = true;
								} catch (Exception e) {
									isConn = false;
								}
							}
						} // end of for
					} // end of item if
				}
			}

		}
	}

	private void pcfDisconnect() {
		if (qmanager != null) {
			try {
				qmanager.disconnect();
			} catch (MQException e) {
			}
		}
		isConn = false;
	}

}
