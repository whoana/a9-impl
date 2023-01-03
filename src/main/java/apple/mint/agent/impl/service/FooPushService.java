package apple.mint.agent.impl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import apple.mint.agent.core.channel.SendChannelWrapper;
import apple.mint.agent.core.service.PushService;
import apple.mint.agent.core.service.ServiceContext;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.Extension;
import pep.per.mint.common.msg.handler.ServiceCodeConstant;
import pep.per.mint.common.util.Util;

/**
 * example service
 */
public class FooPushService extends PushService {

    String agentCd;

    public FooPushService(String cd, String name, ServiceContext serviceContext, SendChannelWrapper sendChannelWrapper, Map params, Boolean disabled) {
        super(cd, name, serviceContext, sendChannelWrapper, params, disabled);
        this.agentCd = (String) params.get("agentCd");
    }

    @Override
    public ComMessage<?, ?> makePushMessage() throws Exception {
        ComMessage<List<Map<String, String>>, ?> msg = new ComMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
        msg.setUserId(serviceContext.getAgentInfo().getAgentCd());
        msg.setCheckSession(false);
        msg.setRequestObject(new ArrayList<Map<String, String>>());

        Extension ext = new Extension();
        ext.setMsgType(Extension.MSG_TYPE_PUSH);
        ext.setServiceCd(ServiceCodeConstant.WS0043);
        msg.setExtension(ext);

        return msg;
    }

}
