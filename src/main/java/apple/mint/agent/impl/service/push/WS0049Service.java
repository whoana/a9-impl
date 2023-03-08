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

/**
 * <pre>
 *  고용노동부(MOEL) 파일인터페이스 모니터링 서비스
 * 
 *  에러코드 처리는 일단 보류  
 *  checkFileCd 
 *      0 : OK
 *      1 : 전송지연 
 *      9 : 파일체크 예외 발생 
 * 
 *  checkErrorFileCd
 *      0 : OK
 *      2 : 에러  
 *      9 : 에러파일체크 예외 발생
 * </pre>
 * 
 * @since 2023.01
 * @author whoana
 * 
 */
public class WS0049Service extends PushService {

    Logger logger = LoggerFactory.getLogger(WS0049Service.class);

    List<Map<String, String>> interfaceList;

    public WS0049Service(
            String cd, String name, ServiceContext serviceContext, SendChannelWrapper sendQueueWrapper, Map params,
            Boolean disabled) {
        super(cd, name, serviceContext, sendQueueWrapper, params, disabled);
    }

    private void initialize() {
        try {

            interfaceList = new ArrayList<Map<String, String>>();

            if (params == null || !params.containsKey("init.service.url")) {
                throw new IllegalArgumentException("WS0049Service must to have parameter value for init.service.url");
            }

            if(serviceContext == null || serviceContext.getAgentInfo() == null){
                throw new IllegalArgumentException("WS0049Service's serviceContext.getAgentInfo may be null.");
            }

            String url = (String) params.get("init.service.url");
            String address = serviceContext.getServerAddress();
            String port = serviceContext.getServerPort();
            port = Util.isEmpty(port) ? "80" : port;
            url = "http://" + address + ":" + port + url;

            ComMessage<Map<String, String>, List<Map<String, String>>> comMessage = new ComMessage<Map<String, String>, List<Map<String, String>>>();
            comMessage.setAppId("agent.WS0049Service");
            comMessage.setCheckSession(false);
            comMessage.setStartTime(Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));
            comMessage.setUserId(serviceContext.getAgentInfo().getAgentNm());
            Map<String, String> requestObj = new HashMap<String, String>();
            requestObj.put("agentId", serviceContext.getAgentInfo().getAgentId());
            comMessage.setRequestObject(requestObj);

            ComMessage<Map<String, String>, List<Map<String, String>>> response = restServiceClient
                    .<ComMessage<Map<String, String>, List<Map<String, String>>>>call2(
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
            Map<String, Object> log = checkInterface(fileInterface, agentInfo.getAgentId());
            if (log != null)
                logs.add(log);
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

    Map<String, Object> checkInterface(Map<String, String> interfaze, String agentId) throws IOException {
        String directory = interfaze.get("directory");
        String direction = interfaze.get("direction");
        String errorDirectory = interfaze.get("errorDirectory");
        int errorFileDurationLimit = Integer.parseInt(interfaze.get("errorFileDurLimit"));
        int maxFileCountLimit = Integer.parseInt(interfaze.get("maxFileCountLimit"));
        int fileTimeLimit = Integer.parseInt(interfaze.get("fileTimeLimit"));
        String interfaceId = interfaze.get("interfaceId");

        Map<String, Object> log = new LinkedHashMap<>();

        String checkFileCd = "0";
        String checkFileMsg = "OK";
        String checkErrorFileCd = "0";
        String checkErrorFileMsg = "OK";

        log.put("interfaceId", interfaceId); // 인터페이스ID (PK)
        log.put("agentId", agentId); // 등록AGENT
        log.put("checkTime", Util.getFormatedDate()); // 체크시작시간(초)
        log.put("checkFileCd", checkFileCd); // 체크에러코드
        log.put("checkFileMsg", checkFileMsg); // 체크에러코드
        log.put("checkErrorFileCd", checkErrorFileCd); // 체크에러코드
        log.put("checkErrorFileMsg", checkErrorFileMsg); // 체크에러코드
        log.put("fileCount", 0); // 시간미초과파일건수
        log.put("lazyFileCount", 0); // 시간초과파일건수
        log.put("errorFileCount", 0); // 에러파일건수
        log.put("regDate", Util.getFormatedDate(Util.DEFAULT_DATE_FORMAT_MI));// 등록일시

        if (Files.exists(Paths.get(directory))) {
            try (Stream<Path> stream = Files.walk(Paths.get(directory), 1);) {

                List<Path> list = Collections.emptyList();
                list = stream.filter(Files::isRegularFile).collect(Collectors.toList());

                int fileCount = 0;
                int delayedFileCount = 0;
                for (Path file : list) {
                    FileTime creationTime = (FileTime) Files.getAttribute(file, "creationTime");
                    int elapsedMin = Math.round((System.currentTimeMillis() - creationTime.toMillis()) / 1000 / 60);
                    if (elapsedMin > fileTimeLimit) {
                        delayedFileCount++;
                    } else {
                        fileCount++;
                    }
                }
                log.put("lazyFileCount", delayedFileCount);
                log.put("fileCount", fileCount);

                if (delayedFileCount >= maxFileCountLimit) {
                    checkFileCd = "1";
                    checkFileMsg = "전송지연";
                }

            } catch (IOException e) {
                logger.error(interfaceId.concat(" check error:"), e);
                String errorMsg = e.getMessage();
                checkFileCd = "9";
                checkFileMsg = errorMsg;
            }
        } else {
            checkFileCd = "9";
            checkFileMsg = "체크할 인터페이스폴더를 찾을 수 없습니다.";
        }

        log.put("checkFileCd", checkFileCd);// 체크에러코드
        log.put("checkFileMsg", checkFileMsg);// 체크에러메시지

        // 송신이면 에러 폴더도 추가 확인
        if (DIRECTION_SEND.equals(direction)) {

            if (Files.exists(Paths.get(errorDirectory))) {
                try (Stream<Path> stream = Files.walk(Paths.get(errorDirectory), 1);) {
                    List<Path> list = Collections.emptyList();
                    list = stream.filter(Files::isRegularFile).collect(Collectors.toList());

                    int errorFileCount = 0;
                    for (Path file : list) {
                        FileTime creationTime = (FileTime) Files.getAttribute(file, "creationTime");
                        int elapsedMin = Math.round((System.currentTimeMillis() - creationTime.toMillis()) / 1000 / 60);
                        if (elapsedMin < errorFileDurationLimit) { // 에러파일의 보관주기 시간단위가 초가 아닐 경우 소스 수정 필요 .
                            errorFileCount++;
                        }
                    }
                    log.put("errorFileCount", errorFileCount);

                    if (errorFileCount > 0) {
                        checkErrorFileCd = "2";
                        checkErrorFileMsg = "전송에러";
                    }

                } catch (IOException e) {
                    logger.error(interfaceId.concat(" check error:"), e);
                    String errorMsg = e.getMessage();
                    checkErrorFileCd = "9";
                    checkErrorFileMsg = errorMsg;
                }
            } else {
                checkErrorFileCd = "9";
                checkErrorFileMsg = "체크할 에러폴더를 찾을 수 없습니다.";
            }

            log.put("checkErrorFileCd", checkErrorFileCd);// 체크에러코드
            log.put("checkErrorFileMsg", checkErrorFileMsg);// 체크에러메시지
        }

        return log;

    }

    // 참고 리소스
    // https://velog.io/@dailylifecoding/Java-nio-package-Files-usage
    public static void main(String[] args) throws Exception {

        try {
            ServiceContext serviceContext = new ServiceContext();
            IIPAgentInfo agentInfo = new IIPAgentInfo();
            agentInfo.setAgentCd("AGENT001");
            agentInfo.setAgentId("AG00000001");
            serviceContext.setAgentInfo(agentInfo);
            Map<String, String> params = new HashMap<String, String>();
            params.put("init.service.url", "http://127.0.0.1:8080/mint/op/agents/services/v4/moel/init?method=GET");
            WS0049Service service = new WS0049Service("WS0049", "interface file check", serviceContext, null, params,
                    null);
            ComMessage<?, ?> comMessage = service.makePushMessage();
            System.out.println(Util.toJSONPrettyString(comMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset(){

        initialize();
        logger.info(this.getClass().getSimpleName() + " was resetted.");

    }

}
