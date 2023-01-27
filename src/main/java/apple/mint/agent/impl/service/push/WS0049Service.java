package apple.mint.agent.impl.service.push;


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

import apple.mint.agent.core.channel.SendChannelWrapper;
import apple.mint.agent.core.service.PushService;
import apple.mint.agent.core.service.ServiceContext;  
import pep.per.mint.common.data.basic.ComMessage;
import pep.per.mint.common.data.basic.Extension;
import pep.per.mint.common.data.basic.agent.IIPAgentInfo;
import pep.per.mint.common.util.Util;


public class WS0049Service extends PushService {

    Logger logger = LoggerFactory.getLogger(WS0049Service.class);

    List<Map<String, String>> interfaceList;

    public WS0049Service(
            String cd, String name, ServiceContext serviceContext, SendChannelWrapper sendQueueWrapper, Map params,
            Boolean disabled) {
        super(cd, name, serviceContext, sendQueueWrapper, params, disabled);

        initialize();
    }

    private void initialize() {
        try {

            interfaceList = new ArrayList<Map<String, String>>();

            String url = (String) params.get("init.service.url");
            if (url == null)
                throw new IllegalArgumentException("WS0049Service must to have parameter value for init.service.url");

            ComMessage<Map<String, String>, List<Map<String, String>>> comMessage = new ComMessage<Map<String, String>, List<Map<String, String>>>();
            comMessage.setAppId("agent.WS0049Service");
            comMessage.setCheckSession(false);
            comMessage.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
            comMessage.setUserId(serviceContext.getAgentInfo().getAgentCd());
            Map<String, String> requestObj = new HashMap<String, String>();
            requestObj.put("agentId", serviceContext.getAgentInfo().getAgentId());
            comMessage.setRequestObject(requestObj);

            ComMessage<Map<String, String>, List<Map<String, String>>> response 
                = restServiceClient.<ComMessage<Map<String, String>, List<Map<String, String>>>>call2(
                    url,
                    comMessage,
                    new ParameterizedTypeReference<ComMessage<Map<String, String>, List<Map<String, String>>>>() {
                    });

            if (!Util.isEmpty(response)) {
                interfaceList = response.getResponseObject();
                logger.debug(Util.join("interfaceList:", Util.toJSONPrettyString(interfaceList)));
            }

        } catch (Exception e) {
            logger.error("Fail to initalize interfaceList", e);
        }

    }

    @Override
    public ComMessage<?, ?> makePushMessage() throws Exception {

        if (Util.isEmpty(interfaceList)) {
            initialize();
        }

        IIPAgentInfo agentInfo = serviceContext.getAgentInfo();
        List<Map<String, Object>> logs = new ArrayList<Map<String, Object>>();
        for (Map<String, String> fileInterface : interfaceList) {
            checkInterface(fileInterface, agentInfo.getAgentId(), logs);
        }

        if (Util.isEmpty(logs))
            return null;


        ComMessage<List<Map<String, Object>>, ?> msg = new ComMessage<>();
        msg.setId(UUID.randomUUID().toString());
        msg.setUserId(agentInfo.getAgentCd());
        msg.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
        msg.setRequestObject(logs);
        Extension ext = new Extension();
        ext.setMsgType(Extension.MSG_TYPE_PUSH);
        ext.setServiceCd("WS0049");
        msg.setExtension(ext);


        logger.debug(Util.join("msg:", Util.toJSONPrettyString(msg)));

        return msg;
    }

    final static String DIRECTION_SEND = "S";
    
    private void checkInterface(Map<String, String> interfaze, String agentId, List<Map<String, Object>> logs) throws IOException {
        String directory = interfaze.get("directory");
        String direction = interfaze.get("direction");        
        String errorDirectory = interfaze.get("errorDirectory");
        int errorFileDurationLimit = Integer.parseInt(interfaze.get("errorFileDurLimit"));
        int fileTimeLimit = Integer.parseInt(interfaze.get("fileTimeLimit"));
        String interfaceId = interfaze.get("interfaceId");
         

        // 인터페이스ID (PK)
        // 체크시작시간(초)
        // 시간미초과파일건수
        // 시간초과파일건수
        // 에러파일건수
        // 등록AGENT
        // 등록일시
        Map<String, Object> log = new LinkedHashMap<>();         
        log.put("interfaceId", interfaceId);
        log.put("checkTime", Util.getFormatedDate());
        log.put("fileCount", 0);
        log.put("lazyFileCount" , 0);
        log.put("errorFileCount" , 0);
        log.put("regAgentId", agentId);
        log.put("regDate", Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
     
        try (Stream<Path> stream = Files.walk(Paths.get(directory), 1);) {
            
            List<Path> list = Collections.emptyList();
            list = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            
            int fileCount = 0;
            int delayedFileCount = 0;
            for (Path file : list) {
                FileTime creationTime = (FileTime) Files.getAttribute(file, "creationTime");
                int elapsedSec = Math.round((System.currentTimeMillis() - creationTime.toMillis()) / 1000);
                if (elapsedSec > fileTimeLimit) {
                    delayedFileCount++;
                } else {
                    fileCount++;
                }
            }
            log.put("lazyFileCount" , delayedFileCount);             
            log.put("fileCount", fileCount);
            
        }
 
        // 송신이면 에러 폴더도 추가 확인
        if (DIRECTION_SEND.equals(direction)) {

            try (Stream<Path> stream = Files.walk(Paths.get(errorDirectory), 1);) {
            
                List<Path> list = Collections.emptyList();
                list = stream.filter(Files::isRegularFile).collect(Collectors.toList());
                
                int errorFileCount = 0;
                for (Path file : list) {
                    FileTime creationTime = (FileTime) Files.getAttribute(file, "creationTime");
                    int elapsedSec = Math.round((System.currentTimeMillis() - creationTime.toMillis()) / 1000); 
                    if (elapsedSec < errorFileDurationLimit) { // 에러파일의 보관주기 시간단위가 초가 아닐 경우 소스 수정 필요 .
                        errorFileCount ++;
                    }  
                }
                
                log.put("errorFileCount", errorFileCount);                
            }

        }  

        logs.add(log);

    }

    // 참고 리소스
    // https://velog.io/@dailylifecoding/Java-nio-package-Files-usage
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(1000);

        long elapsed = System.currentTimeMillis();
        List<Path> list = Collections.emptyList();
        try (Stream<Path> stream = Files.walk(Paths.get("/Users/whoana/DEV/workspace-vs/a9-impl/home/interfaces/a.b"),
                1);) {

            list = stream.filter(Files::isRegularFile).collect(Collectors.toList());

            list.forEach((item) -> {
                try {
                    FileTime creationTime = (FileTime) Files.getAttribute(item, "creationTime");
                    System.out.println(
                            item.toFile().getName() + " : " + ((elapsed - creationTime.toMillis()) / 1000 / 60 / 60 / 24));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            System.out.println("elapsed:" + (System.currentTimeMillis() - elapsed));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

 