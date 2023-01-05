package pep.per.mint.agent.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pep.per.mint.agent.util.CommonVariables;

import pep.per.mint.agent.util.SystemResourceUtilBy3Party;
import pep.per.mint.common.data.basic.agent.ProcessInfo;
import pep.per.mint.common.data.basic.agent.ProcessStatusLog;
import pep.per.mint.common.data.basic.agent.ResourceInfo;
import pep.per.mint.common.data.basic.agent.ResourceUsageLog;
import pep.per.mint.common.util.Util;

/**
 *
 * @author Administrator
 *
 */
// @Service
public class ResourceCheckService {

	Logger logger = LoggerFactory.getLogger(ResourceCheckService.class);

	/**
	 * <pre>
	 * 변경 내용 : OSHI 5.0 라이브러리를 사용하도록 변경함. 
	 * 일단 메모리 정보 얻어오는 부문만 변경해봄  
	 * 변경일 : 20200907
	 * </pre>
	 * 
	 */
	// @Autowired
	// SystemResourceUtil systemResourceUtil;
	SystemResourceUtilBy3Party systemResourceUtil;

	public List<ResourceUsageLog> getResourceUsageLog(List<ResourceInfo> resources) throws Throwable {

		logger.debug(Util.join("resources:", Util.toJSONString(resources)));

		List<ResourceUsageLog> logs = new ArrayList<ResourceUsageLog>();
		for (int i = 0; i < resources.size(); i++) {
			ResourceInfo resource = resources.get(i);
			ResourceUsageLog log = null;
			try {
				if (ResourceInfo.TYPE_CPU.equalsIgnoreCase(resource.getType())) {
					log = systemResourceUtil.getCpuUsage(resource);
				} else if (ResourceInfo.TYPE_MEMORY.equalsIgnoreCase(resource.getType())) {
					log = systemResourceUtil.getMemoryUsage(resource);
				} else if (ResourceInfo.TYPE_DISK.equalsIgnoreCase(resource.getType())) {
					log = systemResourceUtil.getDiskUsage(resource);
				} else {
					continue;
				}
			} catch (Exception e) {
				logger.error("exception:getResourceUsageLog:to continue after exception", e);
				continue;
			}
			if (!Util.isEmpty(log)) {

				resource.setModId(log.getRegApp());
				resource.setModDate(log.getGetDate());
				double limit = Double.parseDouble(resource.getLimit());
				// double used = Double.parseDouble(log.getUsedAmt()); //limit 와 비교를 Percent 값과
				// 비료하는 것으로 변경한다.
				double used = Double.parseDouble(log.getUsedPer());

				String preStatus = resource.getStatus();

				String status = (used > limit) ? CommonVariables.Abnormal : CommonVariables.Normal;

				if (!status.equalsIgnoreCase(preStatus)) {
					resource.setStatus(status);
					log.setResourceInfo(resource);
					logs.add(log);
				} else {
					if (status.equalsIgnoreCase(CommonVariables.Abnormal)) {
						resource.setStatus(status);
						log.setAlertVal(ProcessStatusLog.ALERT_NOT_SEND);
						log.setResourceInfo(resource);
						logs.add(log);
					}
				}
				logger.debug(Util.join("resource log:", Util.toJSONString(log)));
			}

		}

		return logs;
	}

	public List<ResourceUsageLog> getResourceUsageLog(String resourceType, List<ResourceInfo> resources)
			throws Throwable {

		logger.debug(Util.join("resources:", Util.toJSONString(resources)));

		List<ResourceUsageLog> logs = new ArrayList<ResourceUsageLog>();
		for (int i = 0; i < resources.size(); i++) {

			ResourceInfo resource = resources.get(i);

			if (!resourceType.equalsIgnoreCase(resource.getType()))
				continue;

			ResourceUsageLog log = null;

			try {
				if (ResourceInfo.TYPE_CPU.equalsIgnoreCase(resource.getType())) {
					log = systemResourceUtil.getCpuUsage(resource);
				} else if (ResourceInfo.TYPE_MEMORY.equalsIgnoreCase(resource.getType())) {
					log = systemResourceUtil.getMemoryUsage(resource);
				} else if (ResourceInfo.TYPE_DISK.equalsIgnoreCase(resource.getType())) {
					log = systemResourceUtil.getDiskUsage(resource);
				} else {
					continue;
				}
			} catch (Exception e) {
				logger.error("exception:getResourceUsageLog:to continue after exception", e);
				continue;
			}
			if (!Util.isEmpty(log)) {
				resource.setModId(log.getRegApp());
				resource.setModDate(log.getGetDate());
				double limit = Double.parseDouble(resource.getLimit());
				// double used = Double.parseDouble(log.getUsedAmt()); //limit 와 비교를 Percent 값과
				// 비료하는 것으로 변경한다.
				double used = Double.parseDouble(log.getUsedPer());
				boolean isInactive = false;
				String preStatus = resource.getStatus();
				if (CommonVariables.INACTIVE.equals(preStatus)) {
					preStatus = CommonVariables.Normal;
					isInactive = true;
				}
				String status = (used > limit) ? CommonVariables.Abnormal : CommonVariables.Normal;

				if (!status.equalsIgnoreCase(preStatus)) {
					resource.setStatus(status);
					log.setResourceInfo(resource);
					logs.add(log);
				} else {
					if (status.equalsIgnoreCase(CommonVariables.Abnormal)) {
						resource.setStatus(status);
						log.setAlertVal(ProcessStatusLog.ALERT_NOT_SEND);
						log.setResourceInfo(resource);
						logs.add(log);
					} else if (status.equalsIgnoreCase(CommonVariables.Normal) && isInactive) {
						resource.setStatus(status);
						log.setAlertVal(ProcessStatusLog.ALERT_NOT_SEND);
						log.setResourceInfo(resource);
						logs.add(log);
					}
				}
				logger.debug(Util.join("resource log:", Util.toJSONString(log)));
			}

		}

		return logs;
	}

	public List<ProcessStatusLog> getProcessCheckLog(List<ProcessInfo> processes) throws Throwable {

		// logger.debug(Util.join("processes:",Util.toJSONString(processes)));

		List<ProcessStatusLog> logs = new ArrayList<ProcessStatusLog>();
		for (int i = 0; i < processes.size(); i++) {
			ProcessInfo process = processes.get(i);
			ProcessStatusLog log = null;
			try {
				log = systemResourceUtil.getProcessStatusLog(process);
			} catch (Exception e) {
				logger.error("exception:getProcessCheckLog:to continue after exception", e);
				continue;
			}

			logger.debug(Util.join("check process:", Util.toJSONString(process)));

			if (!Util.isEmpty(log)) {

				process.setModId(log.getRegApp());
				process.setModDate(log.getGetDate());
				int limit = process.getCheckCount();
				int check = log.getCnt();
				boolean isInactive = false;
				String preStatus = process.getStatus();
				if (CommonVariables.INACTIVE.equals(preStatus)) {
					preStatus = CommonVariables.Normal;
					isInactive = true;
				}
				String status = (check < limit) ? CommonVariables.Abnormal : CommonVariables.Normal;

				if (!status.equalsIgnoreCase(preStatus)) {
					process.setStatus(status);
					log.setAlertVal(ProcessStatusLog.ALERT_SEND);
					log.setProcessInfo(process);
					logs.add(log);
				} else {
					if (status.equalsIgnoreCase(CommonVariables.Abnormal)) {
						process.setStatus(status);
						log.setAlertVal(ProcessStatusLog.ALERT_NOT_SEND);
						log.setProcessInfo(process);
						logs.add(log);
					} else if (status.equalsIgnoreCase(CommonVariables.Normal) && isInactive) {
						process.setStatus(status);
						log.setAlertVal(ProcessStatusLog.ALERT_NOT_SEND);
						log.setProcessInfo(process);
						logs.add(log);
					} else {
						// Normal active, 정상이란 건가?
						// 정상인건은 로그 추가되지 않음 확인. 20220103
						logger.debug(Util.join("status:", status, "isInactive:", isInactive));
					}
				}
			}

		}

		// logger.debug(Util.join("resource log:", Util.toJSONString(logs)));

		return logs;
	}

	public void setSystemResourceUtil(SystemResourceUtilBy3Party systemResourceUtil) {
		this.systemResourceUtil = systemResourceUtil;
	}

}
