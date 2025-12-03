package com.mine.protocol.demo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mine.protocol.demo.msg.DeviceCommandMsg;
import com.mine.protocol.demo.msg.DeviceErrorMsg;
import com.mine.protocol.demo.msg.DeviceStateMsg;
import com.mine.protocol.demo.msg.ThingsModelMsgConverter;
import com.mine.protocol.demo.ota.OTAMessageCodecConverter;
import com.mine.protocol.demo.ota.ProtocolTransformer;
import com.mine.protocol.demo.ota.TTLVTransformer;
import com.mine.protocol.demo.ota.UndefinedPayloadMessage;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import com.x.iot.protocol.support.DefaultTransport;
import com.x.iot.protocol.support.Transport;
import com.x.iot.protocol.support.codec.ByteEncodeMessage;
import com.x.iot.protocol.support.codec.EncodedMessage;
import com.x.iot.protocol.support.context.DeviceSessionCtx;
import com.x.iot.protocol.support.context.MqttDeviceSession;
import com.x.iot.protocol.support.context.TopicDefinition;
import com.x.iot.protocol.support.exception.MessageDecodeException;
import com.x.iot.protocol.support.exception.MessageEncodeException;
import com.x.iot.protocol.support.message.TagTypeLenValue;
import com.x.iot.protocol.support.message.TransportMessage;
import com.x.iot.protocol.support.message.standard.AbstractThingModelMessage;
import com.x.iot.protocol.support.message.standard.OTAMessage;
import com.x.iot.protocol.support.message.standard.ThingModelDefinitionMessage;
import com.x.iot.protocol.support.spi.DeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

/**
 * @author zoro.kong
 * &#064;className  MyMqttMessageCodec
 * &#064;date  2025/6/13
 * &#064;description  MQTT协议 消息编解码example
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
     * @param sessionCtx 设备会话信息
     * @return 平台消息
     * @throws MessageDecodeException 解码异常
     */
    @Nonnull
    @Override
    public TransportMessage<?> decode(@Nonnull EncodedMessage message, DeviceSessionCtx sessionCtx) throws MessageDecodeException {
        log.info("custom decode:{}",message.payloadAsString());
        if (sessionCtx instanceof MqttDeviceSession) {
            MqttDeviceSession mqttDeviceSession = (MqttDeviceSession) sessionCtx;
            TopicDefinition topicDefinition = mqttDeviceSession.setTopic(message.topicOrRes());
            //平台暂不支持OTA消息扩展，若要使用OTA功能，这段代码不可以修改!!!
            if (topicDefinition != null &&  topicDefinition.getTagCode()!=null && topicDefinition.getTagCode().equals("UP_OTA")) {
                UndefinedPayloadMessage undefinedMessage = ProtocolTransformer.decodeHexPayload(HexBin.encode(message.payloadAsBytes()));
                //设置packetId
                sessionCtx.setMsgId(undefinedMessage.getPacketId()+"");
                List<TagTypeLenValue>  values = TTLVTransformer.hexToTTLV(undefinedMessage.message());
                return OTAMessageCodecConverter.convertToOTAMessage(undefinedMessage, values);
            }else {
                //todo 其他消息自定义处理逻辑
                String payloadStr = new String(message.payloadAsBytes(), Charset.defaultCharset());
                if(message.topicOrRes().endsWith("/state")){
                    DeviceStateMsg deviceStateMsg = JSONObject.parseObject(payloadStr, DeviceStateMsg.class);
                    return ThingsModelMsgConverter.convert(deviceStateMsg,payloadStr,sessionCtx);
                }
                if (message.topicOrRes().endsWith("/cmd")){
                    DeviceCommandMsg commandMsg = JSONObject.parseObject(payloadStr, DeviceCommandMsg.class);
                    return ThingsModelMsgConverter.convert(commandMsg,payloadStr,sessionCtx);
                }
                if (message.topicOrRes().endsWith("/errors")){
                    DeviceErrorMsg deviceErrorMsg = JSONObject.parseObject(payloadStr, DeviceErrorMsg.class);
                    return ThingsModelMsgConverter.convert(deviceErrorMsg,payloadStr,sessionCtx);
                }
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
        log.info("custom encode:{}",message);
        byte[] payload = null;
        //自定义编码逻辑
        if (message instanceof AbstractThingModelMessage && topic.endsWith("/cmd")){
            ThingModelDefinitionMessage thingModelDefinitionMessage = (ThingModelDefinitionMessage) message;
            log.info("thingModelDefinitionMessage:{}",thingModelDefinitionMessage);
            try {
                payload = mapper.writeValueAsBytes(thingModelDefinitionMessage.message().getProps());
            } catch (Exception e) {
                throw new MessageEncodeException(60010,"message encode error.",e);
            }

        }
        //OTA消息 平台暂不支持OTA消息扩展，若要使用OTA功能，这段代码不可以修改!!!
        if (message instanceof OTAMessage){
            try {
                payload = Base64.getDecoder().decode(message.payload());
            }catch (Exception e){
                throw new MessageEncodeException(60010,"message encode error.",e);
            }
        }
        return new ByteEncodeMessage(payload);
    }

}
