package apple.mint.agent.impl.service.push;

import org.junit.Test;
import apple.mint.agent.core.service.ServiceContext;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.util.Util;
import java.util.Map;
import java.util.HashMap;

public class WS0049ServiceTest {
    @Test
    public void testMakePushMessage() {
        try{
            ServiceContext serviceContext = new ServiceContext();            
            IIPAgentInfo agentInfo = new IIPAgentInfo();
            agentInfo.setAgentCd("AGENT01");
            agentInfo.setAgentId("AG00000001");
            serviceContext.setAgentInfo(agentInfo);
            serviceContext.setServerAddress("localhost");
            serviceContext.setServerPort("8080");

            Map<String, String> params = new HashMap<String, String>();
            params.put("init.service.url", "/mint/op/agents/services/v4/moel/init?method=GET");
            WS0049Service service = new WS0049Service("WS0049", "interface file check", serviceContext, null, params, null);
            ComMessage<?, ?> comMessage = service.makePushMessage();
            System.out.println(Util.toJSONPrettyString(comMessage));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
