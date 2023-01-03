package pep.per.mint.agent.util;

import java.io.File;
import java.lang.management.ManagementFactory;
//import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
//import org.springframework.stereotype.Component;

import pep.per.mint.common.data.basic.agent.ProcessInfo;
import pep.per.mint.common.data.basic.agent.ProcessStatusLog;
import pep.per.mint.common.data.basic.agent.ResourceInfo;
import pep.per.mint.common.data.basic.agent.ResourceUsageLog;
import pep.per.mint.common.util.Util;

//@Component
public class SystemResourceUtil {
	Logger logger = LoggerFactory.getLogger(SystemResourceUtil.class);

	public ResourceUsageLog getCpuUsage(ResourceInfo resource) throws Throwable {
		try {
			String osname = System.getProperty("java.vendor").toLowerCase();
			double cpuload = 0.0;
			if (osname.indexOf("ibm") >= 0) {
				com.ibm.lang.management.OperatingSystemMXBean osMXbean = (com.ibm.lang.management.OperatingSystemMXBean) ManagementFactory
						.getOperatingSystemMXBean();
				cpuload = osMXbean.getSystemCpuLoad();
			} else {
				com.sun.management.OperatingSystemMXBean osMXbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
						.getOperatingSystemMXBean();
				cpuload = osMXbean.getSystemCpuLoad();
			}

			ResourceUsageLog log = new ResourceUsageLog();
			log.setResourceInfo(resource);
			String getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
			log.setGetDate(getDate);
			log.setRegApp(Util.join("IIPAgent@", InetAddress.getLocalHost().getHostName()));
			log.setRegDate(getDate);

			log.setTotalAmt("100.0");
			String usedAmt = String.format("%.2f", (cpuload * 100));
			log.setUsedAmt(usedAmt);
			log.setUsedPer(usedAmt);
			return log;
		} finally {
		}
	}

	/**
	 * 메모리 단위 바이트(byte)
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public ResourceUsageLog getMemoryUsage(ResourceInfo resource) throws Exception {
		try {

			String osname = System.getProperty("java.vendor").toLowerCase();
			long total = 0;
			long free = 0;
			if (osname.indexOf("ibm") >= 0) {
				com.ibm.lang.management.OperatingSystemMXBean osMXbean = (com.ibm.lang.management.OperatingSystemMXBean) ManagementFactory
						.getOperatingSystemMXBean();
				total = osMXbean.getTotalPhysicalMemory();
				free = osMXbean.getFreePhysicalMemorySize();
			} else {
				com.sun.management.OperatingSystemMXBean osMXbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
						.getOperatingSystemMXBean();
				total = osMXbean.getTotalPhysicalMemorySize();
				free = osMXbean.getFreePhysicalMemorySize();
			}

			// OperatingSystemMXBean osMXbean = (com.sun.management.OperatingSystemMXBean)
			// ManagementFactory.getOperatingSystemMXBean();
			ResourceUsageLog log = new ResourceUsageLog();
			log.setResourceInfo(resource);
			String getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
			log.setGetDate(getDate);
			log.setRegApp(Util.join("IIPAgent@", InetAddress.getLocalHost().getHostName()));
			log.setRegDate(getDate);

			long used = total - free;

			double usedPer = (double) used / (double) total;
			log.setTotalAmt(Long.toString(total));
			log.setUsedAmt(Long.toString(used));
			log.setUsedPer(String.format("%.2f", (usedPer * 100)));
			return log;
		} finally {
		}
	}

	/**
	 * 디스크 단위 :메가바이트(MB, 시가의 기본 단위 KB에서 1024로 나눔)
	 * 
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	public ResourceUsageLog getDiskUsage(ResourceInfo resource) throws Throwable {
		try {
			String mountpoint = resource.getInfo1();
			if (Util.isEmpty(mountpoint)) {
				throw new Exception(
						"SystemResourceUtil.getDiskUsage: disk mountpoint value is null!\ncheck disk mountpoint!");
			}

			if (!new File(mountpoint).exists()) {
				throw new Exception(Util.join("SystemResourceUtil.getDiskUsage: disk mountpoint(", mountpoint,
						") value is invalid!\ncheck disk mountpoint!"));
			}

			ResourceUsageLog log = new ResourceUsageLog();
			log.setResourceInfo(resource);
			String getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
			log.setGetDate(getDate);
			log.setRegApp(Util.join("IIPAgent@", InetAddress.getLocalHost().getHostName()));
			log.setRegDate(getDate);

			File c = new File(mountpoint);
			long total = c.getTotalSpace() / 1024;
			long free = c.getUsableSpace() / 1024;
			long used = total - free;
			double usedPer = (double) used / (double) total;
			log.setTotalAmt(Long.toString(total));
			log.setUsedAmt(Long.toString(used));
			// log.setUsedPer(Util.join(Double.toString(Math.round(usedPer*100)/100.0),"%"));
			log.setUsedPer(String.format("%.2f", (usedPer * 100)));

			return log;
		} finally {
		}
	}

	// 작성중 , 파라메터 여러개 체크 가능한 버전
	/*
	 * private long findProcess(String cmd, String[] cmdArgs) throws Exception{
	 * 
	 * ProcessQuery query =
	 * ProcessQueryFactory.getInstance().getQuery(Util.join("State.Name.sw=",cmd));
	 * 
	 * long[] pids = query.find(sigar);
	 * if(Util.isEmpty(pids)) {
	 * return -1;
	 * }else{
	 * for (int i = 0; i < pids.length; i++) {
	 * String[] arguments = sigar.getProcArgs(pids[i]);
	 * System.out.println(Util.toJSONString(arguments));
	 * if(Util.isEmpty(arguments)) {
	 * return pids[i];
	 * }else if(Util.isEmpty(cmdArgs)) {
	 * return pids[i];
	 * }else{
	 * 
	 * for(int j = 0 ; j < cmdArgs.length ; j ++){
	 * for(int k = 0 ; k < arguments.length ; k ++){
	 * if(arguments[k].contains(cmdArgs[j])) return pids[i];
	 * }
	 * }
	 * }
	 * }
	 * return -1;
	 * }
	 * 
	 * }
	 */

	public ProcessStatusLog getProcessStatusLog(ProcessInfo process) throws Throwable {
		try {

			

			if (Util.isEmpty(process.getCheckValue())) {
				throw new Exception("SystemResourceUtil.getProcessStatusLog: check value is null!");
			}

			ProcessManager pm = new ProcessManagerImpl();

			ProcessQueryString query = new ProcessQueryString(process.getProcessNm(),
					process.getCheckValue());
					
			logger.info(Util.toJSONPrettyString(query));

			List<Long> pids = pm.findPidList(query);
			//
			ProcessStatusLog log = new ProcessStatusLog();
			String getDate = Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI);
			log.setGetDate(getDate);
			log.setProcessId(process.getProcessId());

			log.setCnt(pids.size());
			log.setRegApp(Util.join("IIPAgent@", InetAddress.getLocalHost().getHostName()));
			log.setRegDate(getDate);

			if (pids.size() == 0) {
				log.setStatus(ProcessStatusLog.Abnormal);
				log.setProcessNo(CommonVariables.NotFoundProcessNo);
				log.setMsg(Util.join("IIPAgent can't find ", process.getProcessNm()));
			} else {
				log.setProcessNo(pids.get(0).toString());
				if (Util.isEmpty(log.getStatus())) {
					// log.setStatus(ProcessStatusLog.Normal);
					if (process.getCheckCount() > pids.size()) {
						log.setStatus(CommonVariables.Abnormal);
						log.setMsg(Util.join(log.getProcessId(), "'status(", log.getStatus(), ") is Abnormal! CheckCnt("
								+ process.getCheckCount() + ") ProcessListCnt(" + pids.size() + ")"));
					} else {
						log.setStatus(CommonVariables.Normal);
						log.setMsg(Util.join(log.getProcessId(), "'status(", log.getStatus(), ") is Normal!"));
					}
				}
			}
			return log;
		} finally {
		}
	}

}
