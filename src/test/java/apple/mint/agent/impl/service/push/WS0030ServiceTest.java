package apple.mint.agent.impl.service.push;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apple.mint.agent.core.service.ServiceContext;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.data.basic.agent.MonitorItem;
import pep.per.mint.common.data.basic.agent.ProcessInfo;
import pep.per.mint.common.util.Util;

public class WS0030ServiceTest {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testMakePushMessage() throws Exception {
        
        ServiceContext context = new ServiceContext();
        
        ProcessInfo processInfo = new ProcessInfo();         
        processInfo.setCheckCount(1); // 필수값 
        processInfo.setCheckValue("check value1"); // 필수값 
        processInfo.setProcessNm("command1");  //필수값 
        //pi.setProcessId("java");// processId는 필수값 아님 

        List<ProcessInfo> processes = new ArrayList<ProcessInfo>();
        processes.add(processInfo);
        
        MonitorItem monitorItem = new MonitorItem();
        monitorItem.setItemType(MonitorItem.ITEM_TYPE_PROCESS);
        monitorItem.setProcesses(processes);
        
        
        List<MonitorItem> monitorItems = new ArrayList<MonitorItem>();
        monitorItems.add(monitorItem);


        IIPAgentInfo agentInfo = new IIPAgentInfo();
        agentInfo.setAgentCd("test");
        agentInfo.setMonitorItems(monitorItems);
        
        context.setAgentInfo(agentInfo);
        
        WS0030Service service = 
            new WS0030Service("WS0030", "ProcessCheckService", context, null, null, null);
        
        ComMessage<?,?> message =   service.makePushMessage();

        Assert.assertNotNull(message);

        logger.debug(Util.toJSONPrettyString(message));
    }
}
