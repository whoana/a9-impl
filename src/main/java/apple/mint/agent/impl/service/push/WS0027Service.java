package apple.mint.agent.impl.service.push;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apple.mint.agent.core.channel.SendChannelWrapper;
import apple.mint.agent.core.service.PushService;
import apple.mint.agent.core.service.ServiceContext;
import pep.per.mint.agent.service.ResourceCheckService;
import pep.per.mint.agent.util.SystemResourceUtil;
import pep.per.mint.agent.util.SystemResourceUtilBy3Party;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.Extension;
import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.data.basic.agent.MonitorItem;
import pep.per.mint.common.data.basic.agent.ResourceInfo;
import pep.per.mint.common.data.basic.agent.ResourceUsageLog;
import pep.per.mint.common.msg.handler.ServiceCodeConstant;
import pep.per.mint.common.util.Util;

/**
 * <pre>
 *  AS-IS CPU 체크 PUSH 서비스 
 *  serviceCD : WS0027
 * </pre>
 */
public class WS0027Service extends PushService {
    
    Logger logger = LoggerFactory.getLogger(WS0027Service.class);

    List<ResourceInfo> resourceInfos = null;
    
    ResourceCheckService resourceCheckService = new ResourceCheckService();
    
    public WS0027Service(
        String cd, 
        String name, 
        ServiceContext serviceContext, 
        SendChannelWrapper sendQueueWrapper,
        Map params, 
        Boolean disabled
    ) {
        super(cd, name, serviceContext, sendQueueWrapper, params, disabled);

        SystemResourceUtilBy3Party systemResourceUtilBy3Party = new SystemResourceUtilBy3Party();
        systemResourceUtilBy3Party.setSystemResourceUtil(new SystemResourceUtil());
        resourceCheckService.setSystemResourceUtil(systemResourceUtilBy3Party);
        
        initialize();
    }


    private void initialize() {
        IIPAgentInfo agentInfo = serviceContext.getAgentInfo();
        if (agentInfo != null) {
            List<MonitorItem> items = agentInfo.getMonitorItems();
            if(!Util.isEmpty(items)){
                for (MonitorItem item : items) {
                    if(MonitorItem.ITEM_TYPE_RESOURCE.equals(item.getItemType())){
                        resourceInfos = item.getResources();
                    }
                }
            }
        }
    }

    @Override
    public ComMessage<?, ?> makePushMessage() throws Exception {
        
        ComMessage<List<ResourceUsageLog>, Object> msg = null;

        if(Util.isEmpty(resourceInfos)) initialize();
        if (!Util.isEmpty(resourceInfos)) {
            try{
                List<ResourceUsageLog> logs = resourceCheckService.getResourceUsageLog(ResourceInfo.TYPE_CPU, resourceInfos);
                if (Util.isEmpty(logs)) {
                    logger.info(Util.join("have no any msg to push."));
                    return null;
                }            
                msg = new ComMessage<List<ResourceUsageLog>, Object>();
                msg.setId(UUID.randomUUID().toString());
                msg.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
                msg.setUserId(serviceContext.getAgentInfo().getAgentCd());
                msg.setCheckSession(false);
                msg.setRequestObject(logs);
    
                Extension ext = new Extension();
                ext.setMsgType(Extension.MSG_TYPE_PUSH);
                ext.setServiceCd(ServiceCodeConstant.WS0027);
                msg.setExtension(ext);
    
                return msg;
 
            }catch(Throwable t){
                throw new Exception(t);
            }
        }
        return msg;
    }

    @Override
    public void reset() {
        logger.info(this.getClass().getSimpleName() + " was resetted.");
    }

}
