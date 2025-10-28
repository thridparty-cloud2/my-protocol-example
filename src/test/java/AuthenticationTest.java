import com.mine.protocol.demo.MyAuthenticator;
import com.x.iot.protocol.support.authentication.AuthenticationRequest;
import com.x.iot.protocol.support.authentication.AuthenticationResponse;
import com.x.iot.protocol.support.authentication.MqttAuthenticationRequest;
import com.x.iot.protocol.support.context.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class AuthenticationTest {



    @Test
    public void testAuthentication() {
        /*
            模拟平台创建的request
            clientId: pk_dk
            username: pk_dk
            password: md5(password) password:1234567
         */
        AuthenticationRequest request = new MqttAuthenticationRequest("p1111z_dk001", "p1111z_dk001", "fcea920f7412b5da7be0cf42b8c93759");
        /*
            模拟平台创建的session信息
            Mqtt协议平台默认创建MqttDeviceSession
            LwM2M协议平台默认创建LwM2MDeviceSession
         */
        MqttDeviceSession deviceSessionCtx = new MqttDeviceSession("sessionId");
        //获取平台定义的topic信息用于初始化
        deviceSessionCtx.initTopic(getTopicDefinitions());
        deviceSessionCtx.setClientId("p1111z_dk001");
        deviceSessionCtx.setProductKey("p1111z");
        deviceSessionCtx.setDeviceKey("dk001");
        deviceSessionCtx.setThingsModelDefinitionService(new ThingsModelDefinitionService() {
            @Override
            public Map<Integer, ThingModelDefinition> thingsModelDefinition(String productKey) {
                return Collections.emptyMap();
            }
        });
        deviceSessionCtx.setMetaDevice(DeviceMeta.builder().deviceSecret("1234567").authMode(1).productSecret("${your product secret}").enabled(1).build());
        MyAuthenticator authenticator = new MyAuthenticator();
        AuthenticationResponse authenticate = authenticator.authenticate(request, deviceSessionCtx);
        System.out.println(authenticate.getCode());
        assertTrue(authenticate.isSuccess(), "认证成功");
    }

    private List<TopicDefinition> getTopicDefinitions() {
        //模拟获取topic信息
        List<TopicDefinition> topicDefinitions = new ArrayList<>();
        TopicDefinition topicDefinition = new TopicDefinition();
        topicDefinition.setTopic("q/2/d/p1111z/dk001/bus");
        topicDefinition.setPerm(2);
        topicDefinition.setTagCode("UP_THING_MODEL");
        topicDefinitions.add(topicDefinition);
        topicDefinitions.add(topicDefinition);
        return topicDefinitions;
    }


}
