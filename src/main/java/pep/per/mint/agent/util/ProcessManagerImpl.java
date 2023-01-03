package pep.per.mint.agent.util;


import java.util.ArrayList;
import java.util.List;

import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessManagerImpl implements ProcessManager {
	Logger logger = LoggerFactory.getLogger(ProcessManagerImpl.class);

	private boolean argumentMatches(String[] arguments, String expected) {
		for (String argument : arguments) {
			if (argument.contains(expected)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public long findPidBy(ProcessQueryString query) throws Throwable {
		List<ProcessInfo> processesList = JProcesses.get().fastMode().listProcesses(query.getCommand());
		for (final ProcessInfo processInfo : processesList) {
			if(processInfo.getCommand().indexOf(query.getArgument())>0){
				return Long.parseLong(processInfo.getPid());
			}
		}


		return PID_UNKNOWN;
	}

	@Override
	public List<Long> findPidList(ProcessQueryString query) throws Throwable {
		List<Long> list = new ArrayList<Long>();
		List<ProcessInfo> processesList = JProcesses.get().fastMode().listProcesses(query.getCommand());
		for (final ProcessInfo processInfo : processesList) {
			logger.trace(processInfo.toString());
			if(processInfo.getCommand().indexOf(query.getArgument())>0){
				list.add(Long.parseLong(processInfo.getPid()));
			}
		}

		return list;

	}




}
