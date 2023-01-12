package apple.mint.agent.impl.service.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;

import apple.mint.agent.core.channel.SendChannelWrapper;
import apple.mint.agent.core.service.PushService;
import apple.mint.agent.core.service.ServiceContext;
import apple.mint.agent.impl.service.push.data.FileInterface;
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.util.Util;

public class WS0049Service extends PushService {

    List<FileInterface> interfaceList;

    public WS0049Service(
            String cd, String name, ServiceContext serviceContext, SendChannelWrapper sendQueueWrapper, Map params,
            Boolean disabled) throws Exception {
        super(cd, name, serviceContext, sendQueueWrapper, params, disabled);

        initialize();
    }

    private void initialize() throws Exception {

        interfaceList = new ArrayList<FileInterface>();

        String url = (String) params.get("init.service.url");
        if (url == null)
            throw new IllegalArgumentException("WS0049Service must to have parameter value for init.service.url");

        ComMessage<Map<String, String>, List<Map<String, Object>>> comMessage = new ComMessage<Map<String, String>, List<Map<String, Object>>>();
        comMessage.setAppId("agent.WS0049Service");
        comMessage.setCheckSession(false);
        comMessage.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
        comMessage.setUserId(serviceContext.getAgentInfo().getAgentCd());
        Map<String, String> requestObj = new HashMap<String, String>();
        requestObj.put("agentId", serviceContext.getAgentInfo().getAgentId());
        comMessage.setRequestObject(requestObj);

        ComMessage<?, ?> response = this.restServiceClient.call(
                url,
                comMessage,
                new ParameterizedTypeReference<ComMessage<Map<String, String>, List<Map<String, Object>>>>() {
                });

        if (!Util.isEmpty(response)) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) response.getResponseObject();
            if (!Util.isEmpty(list)) {
                for (Map<String, Object> data : list) {
                    String interfaceId = (String) data.get("interfaceId");
                    String direction = (String) data.get("direction");
                    String directory = (String) data.get("directory");
                    String errorDirectory = (String) data.get("errorDirectory");
                    int maxFileCountLimit = (int) data.get("maxFileCountLimit");
                    int fileTimeLimit = (int) data.get("fileTimeLimit");
                    int errorFileDurLimit = (int) data.get("errorFileDurLimit");
                    int directoryCheckDelay = (int) data.get("directoryCheckDelay");
                    interfaceList.add(
                        new FileInterface(
                            interfaceId,
                            direction,
                            directory,
                            errorDirectory,
                            maxFileCountLimit,
                            fileTimeLimit,
                            errorFileDurLimit,
                            directoryCheckDelay
                        )
                    );
                }
            }
        }

    }

    @Override
    public ComMessage<?, ?> makePushMessage() throws Exception {
        
        
        for (FileInterface fileInterface : interfaceList) {
            
        }

        return null;
    }

}
