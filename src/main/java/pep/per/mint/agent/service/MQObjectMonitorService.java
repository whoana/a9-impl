package pep.per.mint.agent.service;



import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

import com.ibm.mq.MQException;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
//import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.ibm.mq.pcf.PCFParameter;

import pep.per.mint.agent.exception.AgentException;
import pep.per.mint.agent.util.CommonVariables;
import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.data.basic.agent.MonitorItem;
import pep.per.mint.common.data.basic.agent.QmgrInfo;
import pep.per.mint.common.data.basic.qmgr.QmgrProperty;
import pep.per.mint.common.data.basic.qmgr.QueueProperty;
import pep.per.mint.common.util.Util;

//@Service
public class MQObjectMonitorService {

	Logger logger = LoggerFactory.getLogger(MQObjectMonitorService.class);

	public static String DEFAULT_SVRCONN_CHANNEL = "SYSTEM.DEF.SVRCONN";

	public List<QmgrProperty> getMQList(IIPAgentInfo agentInfo) throws AgentException {

		List<MonitorItem> items = agentInfo.getMonitorItems();
		List<QmgrProperty> mqList = new ArrayList();
			for(int i = 0 ; i < items.size() ; i ++){
				MonitorItem item = items.get(i);
				if(MonitorItem.ITEM_TYPE_QMGR.equals(item.getItemType())){
					if(item.getQmgrs() != null && item.getQmgrs().size() > 0){
						for (QmgrInfo qmgrInfo : item.getQmgrs() ) {
							if(QmgrInfo.TYPE_MQ.equals(qmgrInfo.getType())){
								QmgrProperty qmgrProp = new QmgrProperty();
								qmgrProp.setName(qmgrInfo.getQmgrNm());
//								logger.debug(String.format("MQList-Req-getMQList-1[%s]",Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI)));
								PCFMessageAgent agent = null;
								try {

									try {
										agent = new PCFMessageAgent(qmgrInfo.getIp(), Integer.parseInt(qmgrInfo.getPort()), DEFAULT_SVRCONN_CHANNEL);
										qmgrProp.setStatus(CommonVariables.Normal);
									} catch (Exception e) {
										qmgrProp.setStatus(CommonVariables.Abnormal);
									}
//								logger.debug(String.format("MQList-Req-getMQList-2[%s]",Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI)));
								}finally{
									if(agent != null){
										try {
											agent.disconnect();
										} catch (MQException e) {
										}
									}
								}
//								logger.debug(String.format("MQList-Req-getMQList-3[%s]",Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI)));
								mqList.add(qmgrProp);
							}
						}//end of for
					}//end of item if
				}
			}

		return mqList;
	}


	public List<QmgrProperty> getMQList()  throws AgentException{
		ArrayList<QmgrProperty>  list = new ArrayList<QmgrProperty>();
		try
		{
			Process p=Runtime.getRuntime().exec("dspmq");

			String qmgrname;
			String status;

			BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line="";

			while((line=br.readLine())!=null)
			{
				if(!line.equals(""))
				{
					StringTokenizer st=new StringTokenizer(line);
					qmgrname=st.nextToken();
					status=st.nextToken("\n");

					qmgrname=qmgrname.substring(qmgrname.indexOf("(")+1,qmgrname.indexOf(")")).trim();
					status=status.substring(status.indexOf("(")+1,status.indexOf(")")).trim();

					QmgrProperty qmgrInfo = new QmgrProperty();
					if(status.equals("실행 중")||status.equals("Running"))
					{
						qmgrInfo.setName(qmgrname);
						qmgrInfo.setStatus(CommonVariables.Normal);
					}
					else
					{
						qmgrInfo.setName(qmgrname);
						qmgrInfo.setStatus(CommonVariables.Abnormal);
					}
					list.add(qmgrInfo);
				}
			}
		}
		catch(Exception e)
		{
			logger.warn(e.getMessage(),e);
			throw new AgentException(e.getMessage(), e);
		}

		return list;
	}

	public List<QueueProperty> getQList(Map agent)  throws AgentException{
		ArrayList<QueueProperty>  list = new ArrayList<QueueProperty>();
		//PCFAgent qmanager = null;
		PCFMessageAgent qmanager = null;
		try
		{

			String qmgrName= (String)agent.get("qmgrNm");
			qmanager=new PCFMessageAgent();
			qmanager.setCharacterSet(1208);

			qmanager.setWaitInterval(5);
			qmanager.connect(qmgrName);

			int[] attrs=null;
			attrs=new int[14];
			attrs[0]=CMQC.MQCA_Q_NAME;
			attrs[1]=CMQC.MQIA_CURRENT_Q_DEPTH;
			attrs[2]=CMQC.MQCA_CLUSTER_NAME;
			attrs[3]=CMQC.MQIA_MAX_MSG_LENGTH;
			attrs[4]=CMQC.MQIA_MAX_Q_DEPTH;
			attrs[5]=CMQC.MQCA_PROCESS_NAME;
			attrs[6]=CMQC.MQIA_Q_TYPE;
			attrs[7]=CMQC.MQIA_TRIGGER_CONTROL;
			attrs[8]=CMQC.MQIA_TRIGGER_TYPE;
			attrs[9]=CMQC.MQIA_DEF_PERSISTENCE;
			attrs[10]=CMQC.MQIA_Q_DEPTH_HIGH_LIMIT;
			attrs[11]=CMQC.MQCA_ALTERATION_DATE;
			attrs[12]=CMQC.MQCA_ALTERATION_TIME;
			attrs[13]=CMQC.MQIA_USAGE;



			//PCFParameter[] parameters={new MQCFST(CMQC.MQCA_Q_NAME,"*"),new MQCFIN(CMQC.MQIA_Q_TYPE,CMQC.MQQT_LOCAL),new MQCFIL(CMQCFC.MQIACF_Q_ATTRS,attrs)};
			//MQMessage[] responses;
			PCFMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
			request.addParameter(CMQC.MQCA_Q_NAME, "*");
			request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL);
			request.addParameter(MQConstants.MQIACF_Q_ATTRS, attrs);
			request.addFilterParameter(MQConstants.MQCA_Q_NAME, MQConstants.MQCFOP_NOT_LIKE, "SYSTEM.*");

//			responses=qmanager.send(CMQCFC.MQCMD_INQUIRE_Q,parameters);
			responses=qmanager.send(request);
			for(int i=0;i<responses.length;i++)
			{
				QueueProperty queueInfo= new QueueProperty();
				String name = responses[i]
						.getStringParameterValue(CMQC.MQCA_Q_NAME);
				int curDepth = responses[i]
						.getIntParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH);

				queueInfo.setName(name.trim());
				queueInfo.setCurrentDepth(curDepth);
//				cfh=new MQCFH(responses[i]);
//				QueueInfo queueInfo= new QueueInfo();
//				if(cfh.reason==0)
//				{
//					for(int j=0;j<cfh.parameterCount;j++)
//					{
//						p=PCFParameter.nextParameter(responses[i]);
//						switch(p.getParameter())
//						{
//							case CMQC.MQCA_Q_NAME:
//								queueInfo.setName(((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_CURRENT_Q_DEPTH:
//								queueInfo.setCurrentDepth(((Integer)p.getValue()));
//								break;
//							case CMQC.MQCA_CLUSTER_NAME:
//								//queue.setAttribute("ClusterName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_MAX_MSG_LENGTH:
//								//queue.setAttribute("MaxMsgLength",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_MAX_Q_DEPTH:
//								//queue.setAttribute("MaxQDepth",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQCA_PROCESS_NAME:
//								//queue.setAttribute("ProcessName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_Q_TYPE:
//								//queue.setAttribute("QType",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_TRIGGER_CONTROL:
//								//queue.setAttribute("TriggerControl",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_TRIGGER_TYPE:
//								//queue.setAttribute("TriggerType",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_DEF_PERSISTENCE:
//								//queue.setAttribute("DefPersistence",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_Q_DEPTH_HIGH_LIMIT:
//								//queue.setAttribute("QDepthHighLimit",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQCA_ALTERATION_DATE:
//								//queue.setAttribute("AlterationDate",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQCA_ALTERATION_TIME:
//								//queue.setAttribute("AlterationTime",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_USAGE:
//								//queue.setAttribute("Usage",((Integer)p.getValue()).toString().trim());
//								break;
//
//							// cluseter
//							case CMQC.MQCA_CLUSTER_Q_MGR_NAME:
//								//queue.setAttribute("ClusterQmgrName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_DEF_BIND:
//								//queue.setAttribute("DefBind",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_CLUSTER_Q_TYPE:
//								//queue.setAttribute("ClusterQType",((Integer)p.getValue()).toString().trim());
//								break;
//							// remote
//							case CMQC.MQCA_REMOTE_Q_NAME:
//								//queue.setAttribute("RemoteQName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQCA_REMOTE_Q_MGR_NAME:
//								//queue.setAttribute("RemoteQMgrName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQCA_XMIT_Q_NAME:
//								//queue.setAttribute("XmitQName",((String)p.getValue()).trim());
//								break;
//							// alias CMQC.MQCA_BASE_Q_NAME;//
//							case CMQC.MQCA_BASE_Q_NAME:
//								//queue.setAttribute("BaseQName",((String)p.getValue()).trim());
//								break;
//
//							// model MQIA_DEFINITION_TYPE;//
//							case CMQC.MQIA_DEFINITION_TYPE:
//								//queue.setAttribute("DefinitionType",((Integer)p.getValue()).toString().trim());
//								break;
//							default:
//						}
//					}
//					list.add(queueInfo);
//				}
				list.add(queueInfo);
			}
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
			throw new AgentException(e.getMessage(), e);
		}finally{
			if(qmanager!=null)
			try
			{
				qmanager.disconnect();
			}catch(MQException e){
			}
		}

		return list;
	}


	public List<QueueProperty> getQList(Map agent, IIPAgentInfo agentInfo)  throws AgentException{
		ArrayList<QueueProperty>  list = new ArrayList<QueueProperty>();
		PCFMessageAgent qmanager = null;
		try
		{
			String qmgrName= (String)agent.get("qmgrNm");
			List<MonitorItem> items = agentInfo.getMonitorItems();
			List<QmgrProperty> mqList = new ArrayList();
			for(int i = 0 ; i < items.size() ; i ++){
				MonitorItem item = items.get(i);
				if(MonitorItem.ITEM_TYPE_QMGR.equals(item.getItemType())){
					if(item.getQmgrs() != null && item.getQmgrs().size() > 0){
						for (QmgrInfo qmgrInfo : item.getQmgrs() ) {
							if(QmgrInfo.TYPE_MQ.equals(qmgrInfo.getType()) && qmgrInfo.getQmgrNm().equalsIgnoreCase(qmgrName)){
								try {
									qmanager = new PCFMessageAgent(qmgrInfo.getIp(), Integer.parseInt(qmgrInfo.getPort()), DEFAULT_SVRCONN_CHANNEL);
								} catch (Exception e) {
								}
							}
						}//end of for
					}//end of item if
				}
			}

			if(qmanager == null){
				throw new AgentException("IIPAgent QmgrInfo not invalid.");
			}

			qmanager.setCharacterSet(1208);
			qmanager.setWaitInterval(5);

			int[] attrs=null;
			attrs=new int[14];
			attrs[0]=CMQC.MQCA_Q_NAME;
			attrs[1]=CMQC.MQIA_CURRENT_Q_DEPTH;
			attrs[2]=CMQC.MQCA_CLUSTER_NAME;
			attrs[3]=CMQC.MQIA_MAX_MSG_LENGTH;
			attrs[4]=CMQC.MQIA_MAX_Q_DEPTH;
			attrs[5]=CMQC.MQCA_PROCESS_NAME;
			attrs[6]=CMQC.MQIA_Q_TYPE;
			attrs[7]=CMQC.MQIA_TRIGGER_CONTROL;
			attrs[8]=CMQC.MQIA_TRIGGER_TYPE;
			attrs[9]=CMQC.MQIA_DEF_PERSISTENCE;
			attrs[10]=CMQC.MQIA_Q_DEPTH_HIGH_LIMIT;
			attrs[11]=CMQC.MQCA_ALTERATION_DATE;
			attrs[12]=CMQC.MQCA_ALTERATION_TIME;
			attrs[13]=CMQC.MQIA_USAGE;



			PCFMessage[] responses;
			MQCFH cfh;
			PCFParameter p;

			PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
			request.addParameter(CMQC.MQCA_Q_NAME, "*");
			request.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL);
			request.addParameter(MQConstants.MQIACF_Q_ATTRS, attrs);
			// TO-DO
			// System Queue 출력 여부 판단 옵션 필요.
			String isSysObj =  (String)agent.get("isSysObj");
			if((isSysObj == null || "0".equalsIgnoreCase(isSysObj))){
				request.addFilterParameter(MQConstants.MQCA_Q_NAME, MQConstants.MQCFOP_NOT_LIKE, "SYSTEM.*");
			}
			//

//			responses=qmanager.send(CMQCFC.MQCMD_INQUIRE_Q,parameters);
			responses=qmanager.send(request);
			for(int i=0;i<responses.length;i++)
			{
				QueueProperty queueInfo= new QueueProperty();
				String name = responses[i]
						.getStringParameterValue(CMQC.MQCA_Q_NAME);
				int curDepth = responses[i]
						.getIntParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH);

				queueInfo.setName(name.trim());
				queueInfo.setCurrentDepth(curDepth);
				queueInfo.setQmgrName(qmgrName.trim());
//				cfh=new MQCFH(responses[i]);
//				QueueInfo queueInfo= new QueueInfo();
//				if(cfh.reason==0)
//				{
//					for(int j=0;j<cfh.parameterCount;j++)
//					{
//						p=PCFParameter.nextParameter(responses[i]);
//						switch(p.getParameter())
//						{
//							case CMQC.MQCA_Q_NAME:
//								queueInfo.setName(((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_CURRENT_Q_DEPTH:
//								queueInfo.setCurrentDepth(((Integer)p.getValue()));
//								break;
//							case CMQC.MQCA_CLUSTER_NAME:
//								//queue.setAttribute("ClusterName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_MAX_MSG_LENGTH:
//								//queue.setAttribute("MaxMsgLength",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_MAX_Q_DEPTH:
//								//queue.setAttribute("MaxQDepth",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQCA_PROCESS_NAME:
//								//queue.setAttribute("ProcessName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_Q_TYPE:
//								//queue.setAttribute("QType",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_TRIGGER_CONTROL:
//								//queue.setAttribute("TriggerControl",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_TRIGGER_TYPE:
//								//queue.setAttribute("TriggerType",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_DEF_PERSISTENCE:
//								//queue.setAttribute("DefPersistence",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_Q_DEPTH_HIGH_LIMIT:
//								//queue.setAttribute("QDepthHighLimit",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQCA_ALTERATION_DATE:
//								//queue.setAttribute("AlterationDate",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQCA_ALTERATION_TIME:
//								//queue.setAttribute("AlterationTime",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_USAGE:
//								//queue.setAttribute("Usage",((Integer)p.getValue()).toString().trim());
//								break;
//
//							// cluseter
//							case CMQC.MQCA_CLUSTER_Q_MGR_NAME:
//								//queue.setAttribute("ClusterQmgrName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQIA_DEF_BIND:
//								//queue.setAttribute("DefBind",((Integer)p.getValue()).toString().trim());
//								break;
//							case CMQC.MQIA_CLUSTER_Q_TYPE:
//								//queue.setAttribute("ClusterQType",((Integer)p.getValue()).toString().trim());
//								break;
//							// remote
//							case CMQC.MQCA_REMOTE_Q_NAME:
//								//queue.setAttribute("RemoteQName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQCA_REMOTE_Q_MGR_NAME:
//								//queue.setAttribute("RemoteQMgrName",((String)p.getValue()).trim());
//								break;
//							case CMQC.MQCA_XMIT_Q_NAME:
//								//queue.setAttribute("XmitQName",((String)p.getValue()).trim());
//								break;
//							// alias CMQC.MQCA_BASE_Q_NAME;//
//							case CMQC.MQCA_BASE_Q_NAME:
//								//queue.setAttribute("BaseQName",((String)p.getValue()).trim());
//								break;
//
//							// model MQIA_DEFINITION_TYPE;//
//							case CMQC.MQIA_DEFINITION_TYPE:
//								//queue.setAttribute("DefinitionType",((Integer)p.getValue()).toString().trim());
//								break;
//							default:
//						}
//					}
//					list.add(queueInfo);
//				}
				list.add(queueInfo);
			}
		}catch(Exception e){
			logger.warn(e.getMessage(),e);
			throw new AgentException(e.getMessage(), e);
		}finally{
			if(qmanager!=null)
			try
			{
				qmanager.disconnect();
			}catch(MQException e){
			}
		}

		return list;
	}

	public boolean isConnection(String qmgrNm, IIPAgentInfo iipAgentInfo) {
		boolean isConnection = false;
		List<MonitorItem> items = iipAgentInfo.getMonitorItems();
		List<QmgrProperty> mqList = new ArrayList();
		for(int i = 0 ; i < items.size() ; i ++){
			MonitorItem item = items.get(i);
			if(MonitorItem.ITEM_TYPE_QMGR.equals(item.getItemType())){
				if(item.getQmgrs() != null && item.getQmgrs().size() > 0){
					for (QmgrInfo qmgrInfo : item.getQmgrs() ) {
						if(QmgrInfo.TYPE_MQ.equals(qmgrInfo.getType()) && qmgrInfo.getQmgrNm().equalsIgnoreCase(qmgrNm)){
							PCFMessageAgent	qmanager = null;
							try {
								qmanager = new PCFMessageAgent(qmgrInfo.getIp(), Integer.parseInt(qmgrInfo.getPort()), DEFAULT_SVRCONN_CHANNEL);
								isConnection = true;
							} catch (Exception e) {
								isConnection = false;
							}finally{
								if(qmanager != null){
									try {
										qmanager.disconnect();
									} catch (MQException e) {
									}
								}
							}
						}
					}//end of for
				}//end of item if
			}
		}
		return isConnection;
	}

}
