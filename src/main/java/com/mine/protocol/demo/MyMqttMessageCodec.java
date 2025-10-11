package com.mine.protocol.demo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mine.protocol.demo.msg.DeviceCommandMsg;
import com.mine.protocol.demo.msg.DeviceErrorMsg;
import com.mine.protocol.demo.msg.DeviceStateMsg;
import com.mine.protocol.demo.msg.ThingsModelMsgConverter;
import com.x.iot.protocol.support.DefaultTransport;
import com.x.iot.protocol.support.Transport;
import com.x.iot.protocol.support.codec.Base64StrEncodeMessage;
import com.x.iot.protocol.support.codec.ByteEncodeMessage;
import com.x.iot.protocol.support.codec.EncodedMessage;
import com.x.iot.protocol.support.context.DeviceSessionCtx;
import com.x.iot.protocol.support.exception.MessageDecodeException;
import com.x.iot.protocol.support.exception.MessageEncodeException;
import com.x.iot.protocol.support.message.TransportMessage;
import com.x.iot.protocol.support.message.standard.ThingModelDefinitionMessage;
import com.x.iot.protocol.support.spi.DeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * @author zoro.kong
 * @className MyMqttMessageCodec
 * @date 2025/6/13
 * @description TODO
 */
@Slf4j
public class MyMqttMessageCodec implements DeviceMessageCodec {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.MQTT;
    }

    /**
     * 设备消息解码为平台消息
     * @param message       设备消息 为base64编码字符串（broker按需设置配置）
     * @param deviceSession 设备会话信息
     * @return 平台消息
     * @throws MessageDecodeException 解码异常
     */
    @Nonnull
    @Override
    public TransportMessage<?> decode(@Nonnull EncodedMessage message, DeviceSessionCtx deviceSession) throws MessageDecodeException {
        log.info("自定义解码:{}",message.payloadAsString());
        if (message instanceof Base64StrEncodeMessage){
            Base64StrEncodeMessage base64StrEncodeMessage = (Base64StrEncodeMessage) message;
            //转换为自定义消息
            String payloadStr = new String(base64StrEncodeMessage.payloadAsBytes(), Charset.defaultCharset());
            if (base64StrEncodeMessage.topic().endsWith("/state")){
                DeviceStateMsg deviceStateMsg = JSONObject.parseObject(payloadStr, DeviceStateMsg.class);
                return ThingsModelMsgConverter.convert(deviceStateMsg,payloadStr,deviceSession);
            }else if (base64StrEncodeMessage.topic().endsWith("/cmd")){
                DeviceCommandMsg commandMsg = JSONObject.parseObject(payloadStr, DeviceCommandMsg.class);
                return ThingsModelMsgConverter.convert(commandMsg,payloadStr,deviceSession);
            }else if (base64StrEncodeMessage.topic().endsWith("/errors")){
                DeviceErrorMsg deviceErrorMsg = JSONObject.parseObject(payloadStr, DeviceErrorMsg.class);
                return ThingsModelMsgConverter.convert(deviceErrorMsg,payloadStr,deviceSession);
            }
        }
        throw new MessageDecodeException(61003,"未识别的消息格式");
    }

    /**
     * 平台消息编码为设备消息
     * @param message 物模型消息
     * @param deviceSession 会话 信息
     * @param topic topic
     * @return ByteEncodeMessage
     * @throws MessageEncodeException 编码异常
     */
    @Nonnull
    @Override
    public EncodedMessage encode(@Nonnull TransportMessage<?> message, DeviceSessionCtx deviceSession, String topic) throws MessageEncodeException {
        log.info("自定义编码:{}",message);
        if (message instanceof ThingModelDefinitionMessage && topic.endsWith("/cmd")){
            ThingModelDefinitionMessage thingModelDefinitionMessage = (ThingModelDefinitionMessage) message;
            byte[] payload = null;
            try {
                payload = mapper.writeValueAsBytes(thingModelDefinitionMessage.message().getProps());
            } catch (Exception e) {
                throw new MessageEncodeException(60010,"消息编码失败",e);
            }
            return new ByteEncodeMessage(payload);
        }
        throw new MessageDecodeException(61003,"未识别的消息格式");
    }

}
