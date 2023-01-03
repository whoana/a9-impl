/**
 * Copyright 2020 Mocomsys Inc.  All Rights Reserved.
 */
package pep.per.mint.agent.util;


import java.net.InetAddress;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;

import pep.per.mint.common.data.basic.agent.ProcessInfo;
import pep.per.mint.common.data.basic.agent.ProcessStatusLog;
import pep.per.mint.common.data.basic.agent.ResourceInfo;
import pep.per.mint.common.data.basic.agent.ResourceUsageLog;
import pep.per.mint.common.util.Util;

//import oshi.SystemInfo;
//import oshi.hardware.GlobalMemory;
//import oshi.hardware.HardwareAbstractionLayer;




/**
 * @author whoana
 * @since Sep 7, 2020
 */
//@Component
public class SystemResourceUtilBy3Party {


	final static String LIB_OSHI 	= "oshi";
	final static String javaVersion = System.getProperty("java.version");
	final static String libName 	=  System.getProperty("system.check.lib", "none");//OSHI 라이브러리 사용을 기본값으로 한다.

	//@Autowired
	SystemResourceUtil systemResourceUtil;



	/**
	 * <pre>
	 * java libary를 이용한 CPU 사용량 계산
	 * </pre>
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	public ResourceUsageLog getCpuUsage(ResourceInfo resource) throws Throwable {
		return systemResourceUtil.getCpuUsage(resource);
	}

	/**
	 * <pre>
	 * OSHI 라이브러리를 이용한 메모리 사용량 측정
	 * 자바 1.8 이상 환경일 경우에만 OSHI라이브러리 이용
	 * 자바 1.7, 1.6 환경에서는 java 라이브러리 이용
	 * 자바 1.5 이하에서는 에이전트 실행을 허용하지 않음.
	 * </pre>
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public ResourceUsageLog getMemoryUsage(ResourceInfo resource)throws Exception {
		try {


			ResourceUsageLog log = null;

			if(libName.indexOf(LIB_OSHI) >= 0){

				oshi.hardware.HardwareAbstractionLayer hal = null;
				if(hal == null) {
					oshi.SystemInfo si = new oshi.SystemInfo();
					hal = si.getHardware();
				}

				oshi.hardware.GlobalMemory gm = hal.getMemory();
				long total = gm.getTotal();
				long free = gm.getAvailable();
				log = new ResourceUsageLog();
				log.setResourceInfo(resource);
				String getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
				log.setGetDate(getDate);
				log.setRegApp(Util.join("IIPAgent@",InetAddress.getLocalHost().getHostName()));
				log.setRegDate(getDate);
				long used = total - free;
				double usedPer = (double)used /  (double)total;
				log.setTotalAmt(Long.toString(total));
				log.setUsedAmt(Long.toString(used));
				log.setUsedPer(String.format("%.2f",(usedPer*100)));
			}else {
				log = systemResourceUtil.getMemoryUsage(resource);
			}

			return log;
		}finally {

		}
	}

	/**
	 * <pre>
	 * java libary를 이용한 디스크 사용량 계산
	 * </pre>
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	public ResourceUsageLog getDiskUsage(ResourceInfo resource) throws Throwable {
		return systemResourceUtil.getDiskUsage(resource);
	}

	/**
	 * <pre>
	 * java libary를 이용한 프로세스 상태 확인
	 * </pre>
	 * @param process
	 * @return
	 * @throws Throwable
	 */
	public ProcessStatusLog getProcessStatusLog(ProcessInfo process) throws Throwable {
		return systemResourceUtil.getProcessStatusLog(process);
	}

	public void setSystemResourceUtil(SystemResourceUtil systemResourceUtil) {
		this.systemResourceUtil = systemResourceUtil;
	}

	
}
