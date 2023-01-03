package pep.per.mint.agent.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;

import com.mococo.ILinkAPI.manager.ChannelProperty;
//import com.mococo.ILinkAPI.manager.ChannelProperty;
import com.mococo.ILinkAPI.manager.QManagerAPI;
import com.mococo.ILinkAPI.manager.QProperty;
import com.mococo.ILinkAPI.manager.ReceiverChannelProperty;
import com.mococo.ILinkAPI.manager.TransmitterChannelProperty;

import pep.per.mint.common.data.basic.agent.ChannelInfo;
import pep.per.mint.common.data.basic.agent.ChannelStatusLog;
import pep.per.mint.common.data.basic.agent.QmgrInfo;
import pep.per.mint.common.data.basic.agent.QmgrLogs;
import pep.per.mint.common.data.basic.agent.QmgrStatusLog;
import pep.per.mint.common.data.basic.agent.QueueInfo;
import pep.per.mint.common.data.basic.agent.QueueStatusLog;
import pep.per.mint.common.util.Util;

//@Service(value="iLinkManager")
public class QmgrManagerForILink implements QmgrManager {

	final static int QMGR_STAT_ALIVE = 0;

	Logger logger = LoggerFactory.getLogger(QmgrManagerForILink.class);

	public List<QmgrLogs> getQmgrLogs(String agentId, List<QmgrInfo> qmgrInfos) throws Exception {

		String modDate = null;
		try {
			if(Util.isEmpty(qmgrInfos)) {
				logger.debug("qmgrInfo list is null!");
				return null;
			}

			modDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
			List<QmgrLogs> list = new ArrayList<QmgrLogs>();
			for(int i = 0 ; i < qmgrInfos.size() ; i ++){

				QManagerAPI api = null;
				boolean modified = false;
				QmgrInfo qmgrInfo = qmgrInfos.get(i);
				String ip = qmgrInfo.getIp();
				int port = Integer.parseInt(qmgrInfo.getPort());
				String qmgrName = qmgrInfo.getQmgrNm();
				String version = null;

				try {



					try {
						//=================================================================================================================================
						//ilink 접속 실패시 getVersion 호출 시 NullPointerException 이 발생한다. ILinkAPI는 접속 실패에 대한 별도의 예외 발생을 정의하여 개발하지 않은 듯 싶다.
						//java.lang.NullPointerException
						//at com.mococo.ILinkAPI.manager.QManagerAPI.getVersion(QManagerAPI.java:486)
						//---------------------------------------------------------------------------------------------------------------------------------
						api = new QManagerAPI(ip, port, qmgrName);
						version = api.getVersion();
						logger.debug("qmgr(" + qmgrName + ") : QManagerAPI.getVersion ok : version:" + version);
					}catch(Exception e) {
						//연결 실패시 다음 큐매니저 접속
						//logger.error("qmgr(" + qmgrName + ") : ILink 접속 실패시 getVersion 호출 시 NullPointerException 이 발생한다. ILinkAPI는 접속 실패에 대한 별도의 예외 발생을 정의하여 개발하지 않은 듯 싶다. 정확한 분석을 위해 예외 로그를 ILink 서버 제품 담당에게 문의 필요 ", e);
						logger.error("qmgr(" + qmgrName + ") : QManagerAPI.getVersion exception 발생으로 다음 큐매니저 처리를 계속한다.:",e);
						continue;
					}

					QmgrLogs qmgrLogs = new QmgrLogs();
					String preQmgrStatus = qmgrInfo.getStatus();
					List<ChannelStatusLog> channelStatusLogs = new ArrayList<ChannelStatusLog>();
					qmgrLogs.setChannelStatusLogs(channelStatusLogs);
					List<QueueStatusLog> queueStatusLogs = new ArrayList<QueueStatusLog>();
					qmgrLogs.setQueueStatusLogs(queueStatusLogs);
					QmgrStatusLog qmgrStatusLog = new QmgrStatusLog();

					String getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
					qmgrStatusLog.setGetDate(getDate);
					qmgrStatusLog.setQmgrId(qmgrInfo.getQmgrId());
					qmgrStatusLog.setRegApp(agentId);
					qmgrStatusLog.setRegDate(getDate);



					int ping = api.ping();
					logger.debug(Util.join("iLink api ping to (",ip, ":", port, "):",ping));


					if(ping == CommonVariables.QMGR_PING_STAT){
						qmgrStatusLog.setStatus(CommonVariables.Normal);
						qmgrStatusLog.setMsg(Util.join("qmgr[",qmgrName,"] is alive!"));
						qmgrInfo.setStatus(CommonVariables.Normal);

						List<ChannelInfo> channelInfoList = qmgrInfo.getChannels();
						if(!Util.isEmpty(channelInfoList)) {

							Map<String,Integer> csm = new HashMap<String, Integer>();
							if(version.startsWith("5")) {//ILink Version 5 이하

								logger.debug("-------------------------------");
								logger.debug("--transmit channel list");
								logger.debug("-------------------------------");
								LinkedList tcl= api.getTransmitterChannelList();
								if(tcl != null) {
									for( int k = 0 ;k < tcl.size() ;  k ++){
										TransmitterChannelProperty ch = (TransmitterChannelProperty)tcl.get(k);
										csm.put(ch.getName(), ch.getStatus());
										logger.debug(Util.join(ch.getName(),":", ch.getStatus()));

									}
								}

								logger.debug("-------------------------------");
								logger.debug("--receiver channel list");
								logger.debug("-------------------------------");
								LinkedList rcl= api.getReceiverChannelList();
								if(rcl != null) {
									for( int k = 0 ;k < rcl.size() ; k ++){
										ReceiverChannelProperty ch = (ReceiverChannelProperty)rcl.get(k);
										csm.put(ch.getName(), ch.getStatus());
										logger.debug(Util.join(ch.getName(),":", ch.getStatus()));
									}
								}

							}else {//ILink version : 6.2.4.0904 JetStream 이상
								logger.debug("-------------------------------");
								logger.debug("--ILink ver 6++ channel list");
								logger.debug("-------------------------------");
								LinkedList cl= api.getChannelList();
								if(cl != null) {
									for( int k = 0 ;k < cl.size() ; k ++){
										ChannelProperty ch = (ChannelProperty)cl.get(k);
										csm.put(ch.getName(), ch.getStatus());
										logger.debug(Util.join(ch.getName(),":", ch.getStatus()));
									}
								}
							}

							for(int j = 0 ; j < channelInfoList.size() ; j ++){
								ChannelInfo channelInfo = channelInfoList.get(j);
								String channelName = channelInfo.getChannelNm();
								String channelType = channelInfo.getType();
								String channelStatus = null;
								String channelMsg = "";

								String preChannelStatus = channelInfo.getStatus();

								ChannelStatusLog log = new ChannelStatusLog();
								log.setChannelId(channelInfo.getChannelId());
								log.setGetDate(getDate);
								log.setQmgrId(qmgrInfo.getQmgrId());
								log.setRegDate(getDate);
								log.setRegApp(agentId);


								Integer cs = csm.get(channelName);
								if(cs != null) {
									int channelStatOk = version.startsWith("5") ? CommonVariables.ILINK_CH_STAT_OK : CommonVariables.ILINK_6_CH_STAT_OK;
									if(cs.intValue() == channelStatOk){
										channelStatus = CommonVariables.Normal;
										channelMsg = Util.join("channel[",channelName,"] alive!(status:", CommonVariables.Normal,")");
									}else{
										channelStatus = CommonVariables.Abnormal;
										channelMsg = Util.join("channel[",channelName,"] abnormal!(status:", CommonVariables.Abnormal,")");
									}

								} else {
									channelStatus = CommonVariables.Abnormal;
									channelMsg = Util.join("There is no channel[",channelName,"]");
								}
								logger.debug(channelMsg);

								channelInfo.setStatus(channelStatus);
								channelInfo.setQmgrId(qmgrInfo.getQmgrId());
								channelInfo.setModDate(modDate);
								channelInfo.setModId(agentId);
								log.setChannelInfo(channelInfo);
								log.setStatus(channelStatus);
								log.setMsg(channelMsg);
								if(CommonVariables.INACTIVE.equals(preChannelStatus)){
									preChannelStatus = CommonVariables.Normal;
								}
								if(!preChannelStatus.equalsIgnoreCase(channelStatus)){
									channelStatusLogs.add(log);
									modified = true;
								}
							}
						}
						//--------------------------------------------------------
						List<QueueInfo> queueInfoList = qmgrInfo.getQueues();

						if(!Util.isEmpty(queueInfoList)){

							//logger.debug("-------------------------------");
							//logger.debug("--qlist");
							//logger.debug("-------------------------------");
							LinkedList qlist = api.getQueueList();
							Map<String, Integer> qdm = new HashMap<String, Integer>();
							for(int k = 0; k < qlist.size(); k++){
								QProperty qp = (QProperty) qlist.get(k);
								qdm.put(qp.getQueueName(), qp.getQueueDepth());
								//logger.debug(Util.toJSONString(qlist.get(k)));
							}


							for(int j = 0 ; j < queueInfoList.size() ; j ++){
								QueueInfo queueInfo = queueInfoList.get(j);
								String queueName = queueInfo.getQueueNm();

								int depth = 0;
								String queueStatus = null;
								String queueMsg = null;
								String limitStr = queueInfo.getLimit();

								String preQueueStatus = queueInfo.getStatus();
								int limit = 0;
								try{
									limit = Integer.parseInt(limitStr);
								}catch(Exception e){
									e.printStackTrace();
								}



								QueueStatusLog log = new QueueStatusLog();
								log.setQueueId(queueInfo.getQueueId());
								log.setGetDate(getDate);
								log.setQmgrId(qmgrInfo.getQmgrId());
								log.setRegDate(getDate);
								log.setRegApp(agentId);

								Integer qDepth = qdm.get(queueName);

								if(qDepth != null){
									depth = qDepth.intValue();
									queueStatus = (depth < limit) ? CommonVariables.Normal : CommonVariables.Abnormal;
									queueMsg =  (depth < limit) ?  Util.join("The queue[",queueName,"] depth[",depth,"] is normal:limit[",limit,"]") : Util.join("The queue[",queueName,"] depth[",depth,"] exceed the limit[",limit,"]") ;
								}else{
									depth = 0;
									queueStatus = CommonVariables.Abnormal;
									queueMsg = Util.join("There is no queue[",queueName,"]");
								}
								queueInfo.setStatus(queueStatus);
								queueInfo.setModDate(modDate);
								queueInfo.setModId(agentId);
								queueInfo.setQmgrId(qmgrInfo.getQmgrId());
								log.setQueueInfo(queueInfo);
								log.setDepth(depth);
								int relativeDepth = (int)(100*depth/limit);
								log.setRelativeDepth(relativeDepth);
								log.setMsg(queueMsg);
								if(CommonVariables.INACTIVE.equals(preQueueStatus)){
									preQueueStatus = CommonVariables.Normal;
								}
								if(!preQueueStatus.equalsIgnoreCase(queueStatus)){
									queueStatusLogs.add(log);
									modified = true;
								}
							}

						}
					}else{
						qmgrInfo.setStatus(CommonVariables.Abnormal);
						qmgrStatusLog.setStatus(CommonVariables.Abnormal);
						qmgrStatusLog.setMsg(Util.join("qmgr[",qmgrName,"] is not alive!"));
					}
					qmgrInfo.setModDate(modDate);
					qmgrInfo.setModId(agentId);
	//				qmgrLogs.setQmgrInfo(qmgrInfo);   // 불피요한 데이타  안 올림.
					if(CommonVariables.INACTIVE.equals(preQmgrStatus)){
						preQmgrStatus = CommonVariables.Normal;
					}
					if(!preQmgrStatus.equalsIgnoreCase(qmgrStatusLog.getStatus())){
						qmgrLogs.setQmgrStatusLog(qmgrStatusLog);
						modified = true;
					}
					if(modified){
						list.add(qmgrLogs);
					}
				}finally {
					try {
						if(api != null) api.disConnect();
					}catch(Exception e) {
						logger.error("qmgr(" + qmgrName + ") : QManagerAPI.disConnect fail:", e);
					}
					logger.debug("qmgr(" + qmgrName + ") : QManagerAPI.disConnect ok");
				}
			}
			return list;
		}finally{

		}

	}



}
