package apple.mint.agent.impl.service.response;

import java.util.Map;

import apple.mint.agent.core.channel.SendChannelWrapper;
import apple.mint.agent.core.service.ResponseService;
import apple.mint.agent.core.service.ServiceContext;
import pep.per.mint.common.data.basic.ComMessage;

/** 
 * <pre>
 *  AS-IS 에이전트클래스리로드 서비스  
 *  serviceCd : WS0030
 * 
 * </pre>
 * 
 */
public class WS0021Service extends ResponseService{

    public WS0021Service(String cd, String name, ServiceContext serviceContext, SendChannelWrapper sendQueueWrapper, Map params, Boolean disabled) {
        super(cd, name, serviceContext, sendQueueWrapper, params, disabled);
    }

    @Override
    public ComMessage<?, ?> response(ComMessage<?, ?> request) throws Exception {
        serviceContext.getRestartAgentService().restart();
        return null;
    }

    @Override
    public void reset(){
        logger.info(this.getClass().getSimpleName() + " was resetted.");
    }
    
}
