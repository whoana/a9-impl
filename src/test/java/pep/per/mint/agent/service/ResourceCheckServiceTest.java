package pep.per.mint.agent.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pep.per.mint.agent.util.SystemResourceUtil;
import pep.per.mint.agent.util.SystemResourceUtilBy3Party;
import pep.per.mint.common.data.basic.agent.ProcessInfo;
import pep.per.mint.common.data.basic.agent.ProcessStatusLog;
import pep.per.mint.common.util.Util;

public class ResourceCheckServiceTest {

    Logger logger = LoggerFactory.getLogger(ResourceCheckServiceTest.class);

    
    
    @Test
    public void testGetProcessCheckLog() throws Throwable {
        ResourceCheckService rcs = new ResourceCheckService();
        SystemResourceUtilBy3Party sru3 = new SystemResourceUtilBy3Party();
        sru3.setSystemResourceUtil(new SystemResourceUtil());
        rcs.setSystemResourceUtil(sru3);
        
        List<ProcessInfo> infos = new ArrayList<ProcessInfo>();

        ProcessInfo pi = new ProcessInfo();
        infos.add(pi);    
        pi.setCheckCount(1); // 필수값 
        pi.setCheckValue("catalina"); // 필수값 
        pi.setProcessNm("java");  //필수값 
        //pi.setProcessId("java");// processId는 필수값 아님 


        List<ProcessStatusLog> logs = rcs.getProcessCheckLog(infos);
        logger.info(Util.toJSONPrettyString(logs));            
        
    }

}
