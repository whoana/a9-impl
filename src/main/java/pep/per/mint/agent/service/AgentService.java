package pep.per.mint.agent.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

//import pep.per.mint.agent.controller.AgentController;
import pep.per.mint.agent.util.NHUtil;
import pep.per.mint.common.data.basic.ApplicationInfo;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.Extension;
// import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.msg.handler.ServiceCodeConstant;
import pep.per.mint.common.util.Util;

//@Service
public class AgentService {

	Logger logger = LoggerFactory.getLogger(AgentService.class);

	//@Autowired
	//AgentController agentController;

	public void loadClass(Object ob){
//		ac.loadClass(request);
	}
 
	/**
	 * @deprecated agentController 역참로  주석처리함
	 * @param request
	 * @return
	 */
	// public ComMessage<?,?>  loadAgentInfo(ComMessage<?,?>  request){
	// 	String serviceCd =request.getExtension().getServiceCd();

	// 	IIPAgentInfo tmpInfo = null;
	// 	if(ServiceCodeConstant.WS0025.equalsIgnoreCase(serviceCd)){
	// 		tmpInfo  = (IIPAgentInfo)request.getResponseObject();
	// 	}else{
	// 		tmpInfo  = (IIPAgentInfo)request.getRequestObject();
	// 	}

	// 	ComMessage msg = request;
	// 	if(tmpInfo != null){
	// 		agentController.setAgentInfo(tmpInfo);
	// 		agentController.updateAgentInfoByCmd();

	// 		msg.setErrorCd("0");
	// 	}else{
	// 		msg.setErrorCd("9");
	// 		msg.setErrorMsg("IIPAgentInfo null ");
	// 	}
	// 	msg.setCheckSession(false);

	// 	Extension ext = new Extension();
	// 	ext.setMsgType(Extension.MSG_TYPE_RES);
	// 	ext.setServiceCd(serviceCd);

	// 	msg.setExtension(ext);

	// 	return msg;
	// }


	public ComMessage<?,?> aliveCheck(ComMessage<?,?> request) {
		ComMessage msg = request;
		msg.setCheckSession(false);

		boolean retValue = true;
		msg.setResponseObject(retValue);

		msg.setErrorCd("0");

		Extension ext = msg.getExtension();
		if(ext == null){
			ext = new Extension();
		}
		ext.setMsgType(Extension.MSG_TYPE_RES);
		ext.setServiceCd(ServiceCodeConstant.WS0024);
		msg.setExtension(ext);

		return msg;
	}



	public void agentUpdate(){
//		ac.agentUpdate();
	}


	public List<ApplicationInfo> getVersionLogs(String serverId, String agentId, List<ApplicationInfo> versionInfo) {
		for(ApplicationInfo appInfo : versionInfo){
			 if(ApplicationInfo.TYPE_WMQ.equalsIgnoreCase(appInfo.getType())){
				 if (appInfo.getBinaryPath() !=null && appInfo.getBinaryPath().length()>0){
					 appInfo.setVersion(NHUtil.getMQVersion(appInfo.getBinaryPath()));
				 }else{
					 appInfo.setVersion(NHUtil.getMQVersion());
				 }
			 }else if(ApplicationInfo.TYPE_MTE.equalsIgnoreCase(appInfo.getType())){
				 Map params = new HashMap();
				 params.put("adtPath", appInfo.getBinaryPath());
				 appInfo.setVersion(NHUtil.getMTEVersion(params));
			 }else if(ApplicationInfo.TYPE_TMAX.equalsIgnoreCase(appInfo.getType())){
				 Map params = new HashMap();
				 params.put("adtPath", appInfo.getBinaryPath());
				 params.put("applicationNm", appInfo.getApplicationNm());
				 appInfo.setVersion(NHUtil.getTmaxVersion(params));
			 }
			 appInfo.setRegDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
			 appInfo.setServerId(serverId);
			 appInfo.setModDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
			 appInfo.setModId(agentId);
		}
		return versionInfo;
	}

}
