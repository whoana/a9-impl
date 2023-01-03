package pep.per.mint.agent.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pep.per.mint.agent.exception.AgentException;
import pep.per.mint.agent.util.CommonVariables;
import pep.per.mint.common.data.basic.SiteHomeInfo;
import pep.per.mint.common.data.basic.SiteInfo;
import pep.per.mint.common.data.basic.test.InterfaceCallDetail;
import pep.per.mint.common.util.Util;

//@Service
public class SimulatorService {

	Logger logger = LoggerFactory.getLogger(SimulatorService.class);

	final String APP_RES_TMAX ="TMAX";
	final String APP_RES_JAVA ="JAVA";
	final String APP_RES_API ="API";
	public InterfaceCallDetail testCall(InterfaceCallDetail src, SiteInfo siteInfo) throws AgentException{
		return testCallResponse(src, siteInfo);
	}

	public List<InterfaceCallDetail> testCallList(List<InterfaceCallDetail> src, SiteInfo siteInfo) throws AgentException{
		List<InterfaceCallDetail> dest  = new ArrayList<InterfaceCallDetail>();
		for(InterfaceCallDetail callDet: src){
			dest.add(testCallResponse(callDet, siteInfo));
		}
		return dest;
	}

	private InterfaceCallDetail testCallResponse(InterfaceCallDetail src, SiteInfo siteInfo) throws AgentException{
		String osname =  System.getProperty("os.name").toLowerCase();
		InputStream is;
		StringWriter w;
		int x,y;
		String outpath,line;
		BufferedReader br;

		logger.info(String.format("Test Interface [%s] [%s]", src.getIntegrationId() , src.getResourceType()));

		Process p = null;
		try {

			String scriptPath = "";

			List<SiteHomeInfo> homeInfoList = siteInfo.getSiteHomeInfoList();
			scriptPath = siteInfo.getDefInterfaceCallPath();
			if(homeInfoList.size()>=0){
				for(SiteHomeInfo siteHomeInfo : homeInfoList){
					if(siteHomeInfo.getSystemCd().equalsIgnoreCase(src.getSystemCd())){
						scriptPath = siteHomeInfo.getBinaryPath();
						break;
					}
				}
			}

			String IIPAGENT_HOME ="";
			if(System.getProperty("IIPAGENT_HOME") == null){

				IIPAGENT_HOME ="";
			}else{
				IIPAGENT_HOME =System.getProperty("IIPAGENT_HOME");
			}

			scriptPath = Util.replaceString(scriptPath, "{IIPAGENT_HOME}", IIPAGENT_HOME);

			logger.info(String.format("TestCall Script[%s] System[%s]", scriptPath, src.getSystemCd()));
			if(scriptPath != null && !scriptPath.equals("")){
				if(osname.indexOf("window")>=0){
					p = Runtime.getRuntime().exec("cmd /c " +scriptPath +" "+ src.getResourceType() +" " +src.getIntegrationId());
				}else{
					p = Runtime.getRuntime().exec(scriptPath +" "+ src.getResourceType() +" " +src.getIntegrationId());
				}
			}

			is = p.getInputStream();
			w = new StringWriter();
			while((x=is.read())!= -1) w.write(x);
			outpath = w.toString();
			br = new BufferedReader(new StringReader(outpath));
			StringBuffer sb = new StringBuffer();
			while((line = br.readLine()) != null){
				line = line.trim();
				sb.append(line);
			}
			src.setResDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
//			src.setServerNm(sb.toString());
			String resStr = sb.toString();
			logger.debug(resStr);

			if(resStr == null || resStr.length()<=0){
				src.setStatus(CommonVariables.Abnormal);
				src.setMessage("test call response invalid");
			}else{
				if(APP_RES_JAVA.equalsIgnoreCase(src.getResourceType())){
					if(resStr.toUpperCase().indexOf("SUCCESS[") >=0){
						src.setStatus(CommonVariables.Normal);
						src.setMessage("testCall ok");
					}else{
						src.setStatus(CommonVariables.Abnormal);
						src.setMessage("test call error response.");
					}

				}else if(APP_RES_API.equalsIgnoreCase(src.getResourceType())){
					if(resStr.toUpperCase().indexOf("SUCCESS[") >=0){
						src.setStatus(CommonVariables.Normal);
						src.setMessage("testCall ok");
					}else{
						src.setStatus(CommonVariables.Abnormal);
						src.setMessage("test call error response.");
					}
				}else if(APP_RES_TMAX.equalsIgnoreCase(src.getResourceType())){
					if(resStr.toUpperCase().indexOf("ERR") >=0){
						src.setStatus(CommonVariables.Abnormal);
						src.setMessage("test call error response.");
					}else{
						src.setStatus(CommonVariables.Normal);
						src.setMessage("testCall ok");
					}
				}
			}


		} catch (IOException e) {
			src.setStatus("9");
			src.setResDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
			src.setMessage(e.getMessage());
						e.printStackTrace();
			logger.warn(e.getMessage(), e);

		} catch(Exception e){
			src.setStatus("9");
			src.setResDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
			src.setMessage(e.getMessage());
			logger.warn(e.getMessage(), e);

		}finally{
			if(p != null) p.destroy();
		}
		return src;
	}



	private InterfaceCallDetail testCallResponse(InterfaceCallDetail src) throws AgentException{
		String osname =  System.getProperty("os.name").toLowerCase();
		InputStream is;
		StringWriter w;
		int x,y;
		String outpath,line;
		BufferedReader br;

		logger.info(String.format("Test Interface [%s] [%s]", src.getIntegrationId() , src.getResourceType()));

		Process p = null;
		try {

			if(APP_RES_JAVA.equalsIgnoreCase(src.getResourceType())){
				String IIPAGENT_HOME ="";
				if(System.getProperty("IIPAGENT_HOME") == null){
					IIPAGENT_HOME ="";
				}else{
					IIPAGENT_HOME =System.getProperty("IIPAGENT_HOME");
				}

				if(osname.indexOf("window")>=0){
					p = Runtime.getRuntime().exec("cmd /c " +IIPAGENT_HOME+File.separatorChar+"script" +File.separatorChar+ "interfaceCall.bat "+src.getIntegrationId());
				}else{
					p = Runtime.getRuntime().exec(IIPAGENT_HOME+File.separatorChar+"script" +File.separatorChar+ "interfaceCall.sh "+src.getIntegrationId());
				}

			}else if(APP_RES_API.equalsIgnoreCase(src.getResourceType())){
				if(osname.indexOf("window")>=0){
					p = Runtime.getRuntime().exec("cmd /c apiTest64.bat  "+src.getIntegrationId() +" 1");
				}else{
					p = Runtime.getRuntime().exec("apiTest64 "+src.getIntegrationId() +" 1");
				}
			}else if(APP_RES_TMAX.equalsIgnoreCase(src.getResourceType())){
				if(osname.indexOf("window")>=0){
					p = Runtime.getRuntime().exec("cmd /c tcall64 "+src.getIntegrationId());
				}else{
					p = Runtime.getRuntime().exec("tcall64 "+src.getIntegrationId());
				}
			}else{
				throw new AgentException("ResourceType is invalid ["+ src.getResourceType() +"]");
			}


			is = p.getInputStream();
			w = new StringWriter();
			while((x=is.read())!= -1) w.write(x);
			outpath = w.toString();
			br = new BufferedReader(new StringReader(outpath));
			StringBuffer sb = new StringBuffer();
			while((line = br.readLine()) != null){
				line = line.trim();
				sb.append(line);
			}
			src.setResDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
//			src.setServerNm(sb.toString());
			String resStr = sb.toString();
			logger.debug(resStr);

			if(APP_RES_JAVA.equalsIgnoreCase(src.getResourceType())){
				if(resStr.toUpperCase().indexOf("SUCCESS[") >=0){
					src.setStatus(CommonVariables.Normal);
					src.setMessage("testCall ok");
				}else{
					src.setStatus(CommonVariables.Abnormal);
					src.setMessage("test call error response.");
				}

			}else if(APP_RES_API.equalsIgnoreCase(src.getResourceType())){
				if(resStr.toUpperCase().indexOf("SUCCESS[") >=0){
					src.setStatus(CommonVariables.Normal);
					src.setMessage("testCall ok");
				}else{
					src.setStatus(CommonVariables.Abnormal);
					src.setMessage("test call error response.");
				}
			}else if(APP_RES_TMAX.equalsIgnoreCase(src.getResourceType())){
				if(resStr.toUpperCase().indexOf("ERR") >=0){
					src.setStatus(CommonVariables.Abnormal);
					src.setMessage("test call error response.");
				}else{
					src.setStatus(CommonVariables.Normal);
					src.setMessage("testCall ok");
				}
			}


		} catch (IOException e) {
			src.setStatus("9");
			src.setResDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
			src.setMessage(e.getMessage());
						e.printStackTrace();
			logger.warn(e.getMessage(), e);

		} catch(Exception e){
			src.setStatus("9");
			src.setResDate(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
			src.setMessage(e.getMessage());
			logger.warn(e.getMessage(), e);

		}finally{
			if(p != null) p.destroy();
		}
		return src;
	}
}
