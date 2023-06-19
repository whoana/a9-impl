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
import pep.per.mint.common.data.basic.agent.ResourceInfo;
import pep.per.mint.common.util.Util;

public class WS0027ServiceTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void testMakePushMessage() throws Exception {
        ServiceContext context = new ServiceContext();
        
        ResourceInfo resourceInfo = new ResourceInfo();         
        resourceInfo.setType(ResourceInfo.TYPE_CPU); 
        resourceInfo.setLimit("80"); 
 
        List<ResourceInfo> resourceInfos = new ArrayList<ResourceInfo>();
        resourceInfos.add(resourceInfo);
        
        MonitorItem monitorItem = new MonitorItem();
        monitorItem.setItemType(MonitorItem.ITEM_TYPE_RESOURCE);
        monitorItem.setResources(resourceInfos);
        
        
        List<MonitorItem> monitorItems = new ArrayList<MonitorItem>();
        monitorItems.add(monitorItem);


        IIPAgentInfo agentInfo = new IIPAgentInfo();
        agentInfo.setAgentCd("test");
        agentInfo.setMonitorItems(monitorItems);
        
        context.setAgentInfo(agentInfo);
        
        WS0027Service service = 
            new WS0027Service("WS0027", "CPUCheckService", context, null, null, null);
        
        ComMessage<?,?> message =   service.makePushMessage();

        Assert.assertNotNull(message);

        logger.debug(Util.toJSONPrettyString(message));
    }
}
