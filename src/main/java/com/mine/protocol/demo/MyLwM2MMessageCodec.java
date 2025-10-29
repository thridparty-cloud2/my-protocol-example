package com.mine.protocol.demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.mine.protocol.demo.ota.*;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import com.x.iot.protocol.support.DefaultTransport;
import com.x.iot.protocol.support.Transport;
import com.x.iot.protocol.support.codec.ByteEncodeMessage;
import com.x.iot.protocol.support.codec.EncodedMessage;
import com.x.iot.protocol.support.context.DeviceSessionCtx;
import com.x.iot.protocol.support.exception.MessageDecodeException;
import com.x.iot.protocol.support.exception.MessageEncodeException;
import com.x.iot.protocol.support.message.MessageType;
import com.x.iot.protocol.support.message.TagTypeLenValue;
import com.x.iot.protocol.support.message.TransportMessage;
import com.x.iot.protocol.support.message.standard.OTAMessage;
import com.x.iot.protocol.support.message.standard.RawMessage;
import com.x.iot.protocol.support.message.standard.ThingModelDefinitionMessage;
import com.x.iot.protocol.support.spi.DeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * @author zoro.kong
 * &#064;className  MyLwM2MMessageCodec
 * &#064;date  2025/6/13
 * &#064;description  LwM2M协议消息编解码example
 */
@Slf4j
public class MyLwM2MMessageCodec implements DeviceMessageCodec {
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
        byte[] payload = message.payloadAsBytes();
        if (message.topicOrRes().equalsIgnoreCase("19/0/0")){
            RawMessage rawMessage = new RawMessage();
            // TODO object 19定义为透传消息，透传消息解码逻辑自定义
            // 参考：https://devtoolkit.openmobilealliance.org/OEditor/LWMOView
            return rawMessage;
        }
        if(message.topicOrRes().equals("32769/0/0")){
            /**
             * Object definition
             *
             * | Name              | Object ID | Object Version | LwM2M Version |
             * |-------------------|-----------|----------------|---------------|
             * | Builtin IoT Data  | 32769     | 1.0            | 1.1           |
             *
             * | Object URN                    | Instances | Mandatory |
             * |-------------------------------|-----------|-----------|
             * | urn:oma:lwm2m:x:32769:1.0    | Single    | Optional  |
             */
            /**
             * Resource Definitions
             *
             * | ID | Name | Operations | Instances | Mandatory | Type   | Range or Enumeration | Units | Description                                                                                             |
             * |----|------|------------|-----------|-----------|--------|----------------------|-------|---------------------------------------------------------------------------------------------------------|
             * | 0  | Data | RW         | Single    | Mandatory | Opaque |                      |       | Indicates the downlink data content, in Builtin Binary Protocol format: <AAAA, Len, Sum, PacketId, Cmd, Data/TTLV> |
             */

            try {
                //ota消息为平台内置数据包协议，暂不支持OTA消息自定义，需要保留此代码
                UndefinedPayloadMessage undefinedMessage = ProtocolTransformer.decodeHexPayload(HexBin.encode(payload));
                sessionCtx.setMsgId(undefinedMessage.getPacketId()+"");
                List<TagTypeLenValue> values;
                if (Objects.requireNonNull(XMessageTypeUtil.messageType(undefinedMessage.getCmd())) == MessageType.OTA) {
                    values = TTLVTransformer.hexToTTLV(undefinedMessage.message());
                    return OTAMessageCodecConverter.convertToOTAMessage(undefinedMessage, values);
                }
            }catch (Exception e){
                log.error("Unexpected error occurred while decoding message: {}", message.payloadAsString(), e);
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
        if (message instanceof ThingModelDefinitionMessage ){
            ThingModelDefinitionMessage thingModelDefinitionMessage = (ThingModelDefinitionMessage) message;
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
