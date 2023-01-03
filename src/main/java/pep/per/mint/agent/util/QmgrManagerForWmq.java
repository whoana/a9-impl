package pep.per.mint.agent.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;

import pep.per.mint.common.data.basic.agent.ChannelInfo;
import pep.per.mint.common.data.basic.agent.ChannelStatusLog;
import pep.per.mint.common.data.basic.agent.QmgrInfo;
import pep.per.mint.common.data.basic.agent.QmgrLogs;
import pep.per.mint.common.data.basic.agent.QmgrStatusLog;
import pep.per.mint.common.data.basic.agent.QueueInfo;
import pep.per.mint.common.data.basic.agent.QueueStatusLog;
import pep.per.mint.common.util.Util;

//@Service(value="wmqManager")
public class QmgrManagerForWmq implements QmgrManager{

	Logger logger = LoggerFactory.getLogger(QmgrManagerForWmq.class);
	public static String DEFAULT_SVRCONN_CHANNEL = "SYSTEM.DEF.SVRCONN";
	public List<QmgrLogs> getQmgrLogs(String agentId, List<QmgrInfo> qmgrInfos)  {

		String modDate = null;
		String getDate = null;
		try {
			if(Util.isEmpty(qmgrInfos)) {
				logger.debug("qmgrInfo list is null!");
				return null;
			}

			getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
			modDate = getDate;

			List<QmgrLogs> list = new ArrayList<QmgrLogs>();
			for(int i = 0 ; i < qmgrInfos.size() ; i ++){

				boolean modified = false;
				QmgrInfo qmgrInfo = qmgrInfos.get(i);
				String qmgrName = qmgrInfo.getQmgrNm();
				String hostAddress = qmgrInfo.getIp();
				int listeningPort = Integer.parseInt(qmgrInfo.getPort());

				PCFMessageAgent agent = null;

				try{

					QmgrLogs qmgrLogs = new QmgrLogs();
					qmgrInfo.setModDate(modDate);
					qmgrInfo.setModId(agentId);
//					qmgrLogs.setQmgrInfo(qmgrInfo);  (불필요한 데이타 샂게

					//-------------------------------------------------
					//qmgr log setting
					//-------------------------------------------------
					{

						String preQmgrStatus = qmgrInfo.getStatus();
						QmgrStatusLog qmgrStatusLog = new QmgrStatusLog();

						qmgrStatusLog.setGetDate(getDate);
						qmgrStatusLog.setQmgrId(qmgrInfo.getQmgrId());
						qmgrStatusLog.setRegApp(agentId);
						qmgrStatusLog.setRegDate(getDate);


						try {
							agent = new PCFMessageAgent(hostAddress, listeningPort, DEFAULT_SVRCONN_CHANNEL);
						} catch (MQDataException e) {
							qmgrStatusLog.setStatus(CommonVariables.Abnormal);
							qmgrStatusLog.setMsg(Util.join("qmgr[",qmgrName,"] is abnormal!"));
							qmgrInfo.setStatus(CommonVariables.Abnormal);
							logger.error("",e);

							if(!preQmgrStatus.equalsIgnoreCase(qmgrStatusLog.getStatus())){
								qmgrStatusLog.setAlertVal(QmgrStatusLog.ALERT_SEND);
								qmgrLogs.setQmgrStatusLog(qmgrStatusLog);
								modified = true;
								list.add(qmgrLogs);
							}
							continue;
						}

						qmgrStatusLog.setStatus(CommonVariables.Normal);
						qmgrStatusLog.setMsg(Util.join("qmgr[",qmgrName,"] is alive!"));
						qmgrInfo.setStatus(CommonVariables.Normal);
						if(CommonVariables.INACTIVE.equals(preQmgrStatus)){
							preQmgrStatus = CommonVariables.Normal;
							qmgrStatusLog.setAlertVal(QmgrStatusLog.ALERT_NOT_SEND);
							qmgrLogs.setQmgrStatusLog(qmgrStatusLog);
							modified = true;
						}
						if(!preQmgrStatus.equalsIgnoreCase(qmgrStatusLog.getStatus())){
							qmgrLogs.setQmgrStatusLog(qmgrStatusLog);
							qmgrStatusLog.setAlertVal(QmgrStatusLog.ALERT_SEND);
							modified = true;
						}

					}

					//-------------------------------------------------
					//channel log setting
					//-------------------------------------------------
					{
						List<ChannelStatusLog> channelStatusLogs = new ArrayList<ChannelStatusLog>();
						qmgrLogs.setChannelStatusLogs(channelStatusLogs);
						List<ChannelInfo> channelInfoList = qmgrInfo.getChannels();

						if(!Util.isEmpty(channelInfoList)) {
							for(int j = 0 ; j < channelInfoList.size() ; j ++){
								ChannelInfo channelInfo = channelInfoList.get(j);
								String channelName = channelInfo.getChannelNm();
								//String channelType = channelInfo.getType();
								String channelStatus = null;
								String channelMsg = "";
								String preChannelStatus = channelInfo.getStatus();

								ChannelStatusLog log = new ChannelStatusLog();
								log.setChannelId(channelInfo.getChannelId());
								log.setGetDate(getDate);
								log.setQmgrId(qmgrInfo.getQmgrId());
								log.setRegDate(getDate);
								log.setRegApp(agentId);

								MQResult channelRes =  channelRes = getChannelStatus(agent, channelName);


								if(MQResult.RESULT_OK.equals(channelRes.getErrcd())){
									channelStatus = CommonVariables.Normal;
									channelMsg = Util.join("The channel[",channelName,"] is normal!");
								}else{
									channelStatus = CommonVariables.Abnormal;
									channelMsg = Util.join("The channel[",channelName,"] is abnormal!(msg:",channelRes.getMsg(),")");
								}

								channelInfo.setStatus(channelStatus);
								channelInfo.setQmgrId(qmgrInfo.getQmgrId());
								channelInfo.setModDate(modDate);
								channelInfo.setModId(agentId);
								log.setChannelInfo(channelInfo);
								log.setStatus(channelStatus);
								log.setMsg(channelMsg);
								boolean isInactive = false;
								if(CommonVariables.INACTIVE.equals(preChannelStatus)){
									preChannelStatus = CommonVariables.Normal;
									isInactive = true;
								}
								if(!preChannelStatus.equalsIgnoreCase(channelStatus)){
									log.setAlertVal(ChannelStatusLog.ALERT_SEND);
									channelStatusLogs.add(log);
									modified = true;
								}else{
									if(channelStatus.equalsIgnoreCase(CommonVariables.Abnormal)){
										channelStatusLogs.add(log);
										log.setAlertVal(ChannelStatusLog.ALERT_NOT_SEND);
										modified = true;
									}else if(channelStatus.equalsIgnoreCase(CommonVariables.Normal) && isInactive){
										log.setAlertVal(ChannelStatusLog.ALERT_NOT_SEND);
										channelStatusLogs.add(log);
										modified = true;
									}
								}
								logger.debug(channelMsg);
							}
						}
					}

					//-------------------------------------------------
					//queue log setting
					//-------------------------------------------------
					{
						List<QueueStatusLog> queueStatusLogs = new ArrayList<QueueStatusLog>();
						qmgrLogs.setQueueStatusLogs(queueStatusLogs);
						List<QueueInfo> queueInfoList = qmgrInfo.getQueues();

						if(!Util.isEmpty(queueInfoList)){
							for(int j = 0 ; j < queueInfoList.size() ; j ++){
								QueueInfo queueInfo = queueInfoList.get(j);
								String queueName = queueInfo.getQueueNm();
								int depth = 0;
								String queueStatus = null;
								String queueMsg = null;
								String preQueueStauts = queueInfo.getStatus();
								String limitStr = queueInfo.getLimit();
								int limit = 0;
								try{
									limit = Integer.parseInt(Util.isEmpty(limitStr) ? "0" : limitStr);
								}catch(Exception e){
									logger.debug("ignorable exception:",e);
								}

								MQResult queueRes = getQueueDepth(agent, queueName);

								if(MQResult.RESULT_OK.equals(queueRes.getErrcd())){
									depth = queueRes.getQueueDepth();
									queueStatus = (depth < limit) ? CommonVariables.Normal : CommonVariables.Abnormal;
									queueMsg =  (depth < limit) ?  Util.join("The queue[",queueName,"] depth[",depth,"] is normal:limit[",limit,"]") : Util.join("The queue[",queueName,"] depth[",depth,"] exceed the limit[",limit,"]") ;
								}else{
									depth = 0;
									queueStatus = CommonVariables.Abnormal;
									queueMsg = queueRes.getMsg();
								}

								QueueStatusLog log = new QueueStatusLog();
								log.setQueueId(queueInfo.getQueueId());
								log.setGetDate(getDate);
								log.setQmgrId(qmgrInfo.getQmgrId());
								log.setRegDate(getDate);
								log.setRegApp(agentId);
								queueInfo.setStatus(queueStatus);
								queueInfo.setModDate(modDate);
								queueInfo.setModId(agentId);
								queueInfo.setQmgrId(qmgrInfo.getQmgrId());
								log.setQueueInfo(queueInfo);
								log.setDepth(depth);
								int relativeDepth = limit == 0 ? 0 : (int)(100*depth/limit);
								log.setRelativeDepth(relativeDepth);
								log.setMsg(queueMsg);
								boolean isInactive = false;
								if(CommonVariables.INACTIVE.equals(preQueueStauts)){
									preQueueStauts = CommonVariables.Normal;
									isInactive = true;
								}
								if(!preQueueStauts.equalsIgnoreCase(queueStatus)){
									queueStatusLogs.add(log);
									log.setAlertVal(QueueStatusLog.ALERT_SEND);
									modified = true;
								}else{
									if(queueStatus.equalsIgnoreCase(CommonVariables.Abnormal)){
										queueStatusLogs.add(log);
										log.setAlertVal(QueueStatusLog.ALERT_NOT_SEND);
										modified = true;
									}else if(queueStatus.equalsIgnoreCase(CommonVariables.Normal) && isInactive ){
										queueStatusLogs.add(log);
										log.setAlertVal(QueueStatusLog.ALERT_NOT_SEND);
										modified = true;
									}
								}
							}
						}
					}

					if(modified){
						list.add(qmgrLogs);
					}


				}finally{
					if(agent != null){
						try {
							agent.disconnect();
						} catch (MQDataException e) {
							logger.error("",e);
						}
					}
				}
			}//end of qmgrInfos list
			return list;
		}finally{

		}
	}

	/**
	 *
	 * @param agent
	 * @param queueName
	 * @return
	 * @throws IOException
	 * @throws MQDataException
	 */
	private MQResult getQueueDepth(PCFMessageAgent agent, String queueName) {
		MQResult res = new MQResult();


		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);

	    pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, queueName);

	    pcfCmd.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);

	    //current queue depth filter
	    //pcfCmd.addFilterParameter(MQConstants.MQIA_CURRENT_Q_DEPTH, MQConstants.MQCFOP_GREATER, 0);

	    try{
		    PCFMessage[] pcfResponse = agent.send(pcfCmd);

		    if(pcfResponse == null || pcfResponse.length == 0){
				res.setErrcd(MQResult.RESULT_FAIL);
				res.setMsg(Util.join("The queue ", queueName, " does not exist"));
		    }else{
		    	PCFMessage response = pcfResponse[0];
		    	//String qName = (String) response.getParameterValue(MQConstants.MQCA_Q_NAME);
			    int depth = response.getIntParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH);
			    res.setErrcd(MQResult.RESULT_OK);
			    res.setQueueDepth(depth);
		    }
	    }catch(PCFException pcfe){

	    	res.setMqrc(pcfe.reasonCode);
	    	if (pcfe.reasonCode != MQConstants.MQRC_UNKNOWN_OBJECT_NAME) {
				res.setErrcd(MQResult.RESULT_FAIL);
				res.setMsg(pcfe.getMessage());
			}else{
				res.setErrcd(MQResult.RESULT_FAIL);
				res.setMsg(Util.join("The queueManger ", agent.getQManagerName(), " does not have the queue [", queueName, "]."));
			}
	    } catch(Exception e){
			res.setErrcd(MQResult.RESULT_FAIL);
			res.setMsg(e.getMessage());
		}

	    return res;
	}

	/**
	 *
	 * @param agent
	 * @param channelName
	 * @return
	 * @throws IOException
	 * @throws MQDataException
	 */
	private MQResult getChannelStatus(PCFMessageAgent agent, String channelName)  {

		MQResult res = new MQResult();

		PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL_STATUS);

		pcfCmd.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channelName);

		try {

			PCFMessage[] pcfResponse = agent.send(pcfCmd);
			int chStatus = ((Integer) (pcfResponse[0].getParameterValue(MQConstants.MQIACH_CHANNEL_STATUS))).intValue();
			res.setStatus(chStatus);

			if(chStatus == 3){
				res.setErrcd(MQResult.RESULT_OK);
			}else{
				res.setErrcd(MQResult.RESULT_FAIL);
			}
			res.setMsg(Util.join("STATUS:",chStatus, ", MSG:", MQResult.STATUS_TEXT[chStatus]));

		} catch (PCFException pcfe) {

			res.setMqrc(pcfe.reasonCode);
			if (pcfe.reasonCode != MQConstants.MQRCCF_CHL_STATUS_NOT_FOUND) {
				res.setErrcd(MQResult.RESULT_FAIL);
				res.setMsg(pcfe.getMessage());

			}else{
				res.setErrcd(MQResult.RESULT_FAIL);
				res.setMsg(Util.join("The queue manager ", agent.getQManagerName(), " does not exist or the channel [", channelName, "] does not exist on the queue manager."));
			}
		} catch(Exception e){
			res.setErrcd(MQResult.RESULT_FAIL);
			res.setMsg(e.getMessage());
		}
		return res;
	}

}
