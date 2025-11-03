package com.mine.protocol.demo.msg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.x.iot.protocol.support.context.DeviceSessionCtx;
import com.x.iot.protocol.support.context.ThingModelDefinition;
import com.x.iot.protocol.support.message.standard.AbstractThingModelMessage;
import com.x.iot.protocol.support.message.standard.ThingModelDefinitionMessage;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zoro.kong
 * &#064;className  ThingsModelMsgConverter
 * &#064;date  2025/6/17
 * &#064;description  自定义消息与物模型消息映射
 */
public class ThingsModelMsgConverter {
    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * 自定义消息与物模型映射
     * 由于我定义的物模型code与设备消息中json code一致，甚至可以不用获取物模型定义来转换
     */
    public static AbstractThingModelMessage convert(DeviceStateMsg deviceStateMsg, String payload, DeviceSessionCtx sessionCtx) {
        ThingModelDefinitionMessage thingModelMessage = new ThingModelDefinitionMessage();
        //防止时间戳冲突
        thingModelMessage.setMsgId(deviceStateMsg.getTimestamp() + RandomUtils.nextInt(1000,9999));
        thingModelMessage.setMsgSize((long)payload.length());
        thingModelMessage.setSubType(AbstractThingModelMessage.ThingsModelSubType.REPORT);
        thingModelMessage.setPayload(payload);
        Map<Integer, ThingModelDefinition> thingModelDefinitions = sessionCtx.thingsModelDefinition();
        //转换为code-value映射

        Map<String,ThingModelDefinition> thingModelDefinitionMap =thingModelDefinitions.entrySet().stream().collect(
               Collectors.toMap(entry -> entry.getValue().getCode(), Map.Entry::getValue));
        //获取code-value映射
        Map<String,Object> properties = new HashMap<>();
        thingModelDefinitionMap.forEach((code,thingModelDefinition) -> {
            if (code.equals("state")){
                properties.put(code,deviceStateMsg.getState());
            }
            if (code.equals("brightness")){
                properties.put(code,deviceStateMsg.getBrightness());
            }
            if (code.equals("timestamp")){
                properties.put(code,deviceStateMsg.getTimestamp());
            }
        });
        ThingModelDefinitionMessage.ThingModelDefinition definition = new ThingModelDefinitionMessage.ThingModelDefinition();
        definition.setProps(properties);
        thingModelMessage.setMessage(definition);
        return thingModelMessage;
    }
    public static AbstractThingModelMessage convert(DeviceErrorMsg deviceErrorMsg,String payload,DeviceSessionCtx sessionCtx) {
        ThingModelDefinitionMessage thingModelMessage = new ThingModelDefinitionMessage();
        //随机生成一个
        thingModelMessage.setMsgId(RandomUtils.nextInt(1000,9999)+"");
        thingModelMessage.setMsgSize((long)payload.length());
        thingModelMessage.setSubType(AbstractThingModelMessage.ThingsModelSubType.REPORT);
        thingModelMessage.setPayload(payload);
        Map<Integer, ThingModelDefinition> thingModelDefinitions = sessionCtx.thingsModelDefinition();
        //转换为code-value映射

        Map<String,ThingModelDefinition> thingModelDefinitionMap =thingModelDefinitions.entrySet().stream().collect(
                Collectors.toMap(entry -> entry.getValue().getCode(), Map.Entry::getValue));
        //获取code-value映射
        Map<String,Object> events = new HashMap<>();
        thingModelDefinitionMap.forEach((code,thingModelDefinition) -> {
            if (code.equals("ERR_OVERHEAT")){
                ObjectNode values = mapper.createObjectNode();
                values.put("severity", Objects.requireNonNull(SEVERITY.getByCode(deviceErrorMsg.getSeverity())).value);
                values.put("message",deviceErrorMsg.getMessage());
                values.put("timestamp",deviceErrorMsg.getTimestamp());
                events.put(code,values);
            }
        });
        ThingModelDefinitionMessage.ThingModelDefinition definition = new ThingModelDefinitionMessage.ThingModelDefinition();
        definition.setEvents(events);
        thingModelMessage.setMessage(definition);
        return thingModelMessage;
    }

    public static AbstractThingModelMessage convert(DeviceCommandMsg deviceCommandMsg,String payload,DeviceSessionCtx sessionCtx) {
        ThingModelDefinitionMessage thingModelMessage = new ThingModelDefinitionMessage();
        //随机生成一个
        thingModelMessage.setMsgId(RandomUtils.nextInt(1000,9999)+"");
        thingModelMessage.setMsgSize((long)payload.length());
        thingModelMessage.setSubType(AbstractThingModelMessage.ThingsModelSubType.REPORT);
        thingModelMessage.setPayload(payload);
        Map<Integer, ThingModelDefinition> thingModelDefinitions = sessionCtx.thingsModelDefinition();
        //转换为code-value映射

        Map<String,ThingModelDefinition> thingModelDefinitionMap =thingModelDefinitions.entrySet().stream().collect(
                Collectors.toMap(entry -> entry.getValue().getCode(), Map.Entry::getValue));
        //获取code-value映射
        Map<String,Object> properties = new HashMap<>();
        thingModelDefinitionMap.forEach((code,thingModelDefinition) -> {
            if (code.equals("action")){
                properties.put(code,deviceCommandMsg.getAction());
            }
            if (code.equals("payload")){
                properties.put(code,deviceCommandMsg.getPayload());
            }
        });
        ThingModelDefinitionMessage.ThingModelDefinition definition = new ThingModelDefinitionMessage.ThingModelDefinition();
        definition.setProps(properties);
        thingModelMessage.setMessage(definition);
        return thingModelMessage;
    }

    /**
     * 定义OVERHEAT事件参数SEVERITY
     */
    public enum SEVERITY{
        LOW("LOW",4,"1"),
        MEDIUM("MEDIUM",4,"2"),
        HIGH("HIGH",4,"3"),
        CRITICAL("CRITICAL",4,"4");
        private final String code;
        private final int id;
        private final String value;
        SEVERITY(String code, int id, String value) {
            this.code = code;
            this.id = id;
            this.value = value;
        }

        public static SEVERITY getByCode(String code) {
            for (SEVERITY severity : SEVERITY.values()) {
                if (severity.code.equals(code)) {
                    return severity;
                }
            }
            return null;
        }
    }
}
