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
import pep.per.mint.common.data.basic.agent.ProcessInfo;
import pep.per.mint.common.data.basic.agent.ProcessStatusLog;
import pep.per.mint.common.msg.handler.ServiceCodeConstant;
import pep.per.mint.common.util.Util;

/**
 * <pre>
 *  AS-IS 프로세스 체크  PUSH 서비스  
 *  serviceCd : WS0030
 * 
 * </pre>
 */
public class WS0030Service extends PushService {

    Logger logger = LoggerFactory.getLogger(WS0030Service.class);

    ResourceCheckService resourceCheckService = new ResourceCheckService();

    public WS0030Service(String cd, String name, ServiceContext serviceContext, SendChannelWrapper sendQueueWrapper,
            Map params, Boolean disabled) {
        super(cd, name, serviceContext, sendQueueWrapper, params, disabled);
        initialize();
    }

    private void initialize() {
        SystemResourceUtilBy3Party systemResourceUtilBy3Party = new SystemResourceUtilBy3Party();
        systemResourceUtilBy3Party.setSystemResourceUtil(new SystemResourceUtil());
        resourceCheckService.setSystemResourceUtil(systemResourceUtilBy3Party);
    }

    @Override
    public ComMessage<?, ?> makePushMessage() throws Exception {

        ComMessage<Object, IIPAgentInfo> msg = null;

        IIPAgentInfo agentInfo = serviceContext.getAgentInfo();

        if (agentInfo != null) {
            List<ProcessInfo> processInfos = null;
            List<MonitorItem> items = agentInfo.getMonitorItems();
            for (MonitorItem item : items) {
                if (MonitorItem.ITEM_TYPE_PROCESS.equals(item.getItemType())) {
                    processInfos = item.getProcesses();
                    break;
                }
            }

            if (!Util.isEmpty(processInfos)) {
                try {
                    List<ProcessStatusLog> logs = resourceCheckService.getProcessCheckLog(processInfos);
                    if (Util.isEmpty(logs)) {
                        logger.info(Util.join("have no any msg to push."));
                        return null;
                    }

                    msg = new ComMessage<Object, IIPAgentInfo>();
                    msg.setId(UUID.randomUUID().toString());
                    msg.setUserId(agentInfo.getAgentCd());
                    msg.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
                    msg.setRequestObject(logs);
                    Extension ext = new Extension();
                    ext.setMsgType(Extension.MSG_TYPE_PUSH);
                    ext.setServiceCd(ServiceCodeConstant.WS0030);
                    msg.setExtension(ext);
                    return msg;
                } catch (Throwable e) {
                    throw new Exception(e);
                }
            }
        }
        return msg;
    }
}
