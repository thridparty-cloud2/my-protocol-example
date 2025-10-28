import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mine.protocol.demo.MyMqttMessageCodec;
import com.x.iot.protocol.support.codec.EncodeMessageFactory;
import com.x.iot.protocol.support.codec.EncodedMessage;
import com.x.iot.protocol.support.codec.TextStrEncodeMessage;
import com.x.iot.protocol.support.context.*;
import com.x.iot.protocol.support.message.TransportMessage;
import com.x.iot.protocol.support.message.standard.AbstractThingModelMessage;
import com.x.iot.protocol.support.message.standard.ThingModelDefinitionMessage;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zoro.kong
 * @className MessageCodecTest
 * @date 2025/10/25
 * @description TODO
 */
public class MessageCodecTest {
    public static final ObjectMapper objMapper = new ObjectMapper();
    @Test
    public void testEncode() {
        /*
          模拟设备下发控制指令
         */
        ThingModelDefinitionMessage message = new ThingModelDefinitionMessage();
        ThingModelDefinitionMessage.ThingModelDefinition thingModel = new ThingModelDefinitionMessage.ThingModelDefinition();
        ObjectNode data = objMapper.createObjectNode();
        data.put("action","SET_BRIGHTNESS");
        ObjectNode cmd = objMapper.createObjectNode();
        cmd.put("brightness",50);
        data.putPOJO("payload",cmd);
        thingModel.setProps(data);

        message.setMessage(thingModel);
        message.setSubType(AbstractThingModelMessage.ThingsModelSubType.WRITE_REQ);

        /*
          模拟设备会话信息
         */
        MqttDeviceSession deviceSessionCtx = new MqttDeviceSession("sessionId");
        //获取平台定义的topic信息用于初始化
        deviceSessionCtx.initTopic(getTopicDefinitions());
        deviceSessionCtx.setClientId("p1111A_dk001");
        deviceSessionCtx.setProductKey("p1111A");
        deviceSessionCtx.setDeviceKey("dk001");
        deviceSessionCtx.setThingsModelDefinitionService(new ThingsModelDefinitionService() {
            @Override
            public Map<Integer, ThingModelDefinition> thingsModelDefinition(String productKey) {
                /*
                  模拟获取物模型定义信息
                 */
                return Collections.emptyMap();
            }
        });
        deviceSessionCtx.setMetaDevice(DeviceMeta.builder().deviceSecret("1234567").authMode(1).productSecret("${your product secret}").enabled(1).build());
        MyMqttMessageCodec myMqttMessageCodec = new MyMqttMessageCodec();
        EncodedMessage encode = myMqttMessageCodec.encode(message, deviceSessionCtx, "user/business/p1111A/dk001/cmd");
        System.out.println(encode.payloadAsString());
        assertEquals("eyJhY3Rpb24iOiJTRVRfQlJJR0hUTkVTUyIsInBheWxvYWQiOnsiYnJpZ2h0bmVzcyI6NTB9fQ==",encode.payloadAsString());
    }

    @Test
    public void testDecode() {
        /*
          模拟设备上报状态信息
          {
            "timestamp": "1750064986151",
            "state": "ON",
            "brightness": 75
           }
           该消息从设备上报发到broker会转为base64编码字符串
         */
        TextStrEncodeMessage encodedMessage = EncodeMessageFactory.create("ewogICAgICAgICJ0aW1lc3RhbXAiOiAiMTc1MDA2NDk4NjE1MSIsCiAgICAgICAgInN0YXRlIjogIk9OIiwKICAgICAgICAiYnJpZ2h0bmVzcyI6IDc1Cn0=",
                "base64");
        encodedMessage.setPayloadType("user/business/p1111A/dk001/state");

        /*
          模拟设备会话信息
         */
        MqttDeviceSession deviceSessionCtx = new MqttDeviceSession("sessionId");
        //获取平台定义的topic信息用于初始化
        deviceSessionCtx.initTopic(getTopicDefinitions());
        deviceSessionCtx.setClientId("p1111A_dk001");
        deviceSessionCtx.setProductKey("p1111A");
        deviceSessionCtx.setDeviceKey("dk001");
        deviceSessionCtx.setThingsModelDefinitionService(new ThingsModelDefinitionService() {
            @Override
            public Map<Integer, ThingModelDefinition> thingsModelDefinition(String productKey) {
                /*
                  模拟获取物模型定义信息
                 */
                Map<Integer, ThingModelDefinition> thingModelDefinitions = new HashMap<>();
                ThingModelDefinition timestamp = new ThingModelDefinition();
                timestamp.setId(6);//功能定义Id
                timestamp.setCode("timestamp");
                timestamp.setType("PROPERTY");
                timestamp.setDataType("DATE");
                timestamp.setSubType("R");
                timestamp.setSpecs("{\"dataType\":\"DATE\",\"txtlen\":13,\"length\":13}");
                thingModelDefinitions.put(6, timestamp);
                ThingModelDefinition brightness = new ThingModelDefinition();
                brightness.setId(2);
                brightness.setCode("brightness");
                brightness.setType("PROPERTY");
                brightness.setDataType("NUMBER");
                brightness.setSubType("RW");
                brightness.setSpecs("{\"id\":2,\"unit\":\"%\",\"scale\":\"1\",\"step\":\"1\",\"min\":\"0\",\"max\":\"100\"}");
                thingModelDefinitions.put(2, brightness);
                ThingModelDefinition state = new ThingModelDefinition();
                state.setId(1);
                state.setCode("state");
                state.setType("PROPERTY");
                state.setDataType("ENUM");
                state.setSubType("RW");
                state.setSpecs("[{\"name\":\"ON\",\"value\":\"1\",\"dataType\":\"ENUM\"},{\"name\":\"OFF\",\"value\":\"0\",\"dataType\":\"ENUM\"}]");
                thingModelDefinitions.put(1, state);
                //more thingModelDefinition...
                return thingModelDefinitions;
            }
        });
        deviceSessionCtx.setMetaDevice(DeviceMeta.builder().deviceSecret("1234567").authMode(1).productSecret("${your product secret}").enabled(1).build());
        MyMqttMessageCodec myMqttMessageCodec = new MyMqttMessageCodec();
        TransportMessage<?> decode = myMqttMessageCodec.decode(encodedMessage, deviceSessionCtx);
        System.out.println(decode.message());
        assertInstanceOf(ThingModelDefinitionMessage.class, decode);
    }
    private List<TopicDefinition> getTopicDefinitions() {
        //模拟获取topic信息
        List<TopicDefinition> topicDefinitions = new ArrayList<>();
        TopicDefinition topicDefinition = new TopicDefinition();
        topicDefinition.setTopic("user/business/p1111A/dk001/state");
        topicDefinition.setPerm(1);
        topicDefinition.setTagCode("UP_THING_MODEL");
        topicDefinition.setType("BUILTIN");
        topicDefinitions.add(topicDefinition);
        topicDefinitions.add(topicDefinition);

        topicDefinition = new TopicDefinition();
        topicDefinition.setTopic("user/business/p1111A/dk001/cmd");
        topicDefinition.setPerm(2);
        topicDefinition.setTagCode("DW_THING_MODEL");
        topicDefinition.setType("BUILTIN");
        topicDefinitions.add(topicDefinition);

        topicDefinition = new TopicDefinition();
        topicDefinition.setTopic("user/custom/p1111A/dk001/errors");
        topicDefinition.setPerm(1);
        topicDefinition.setType("CUSTOM");
        topicDefinitions.add(topicDefinition);
        return topicDefinitions;
    }
}
