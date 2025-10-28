package com.mine.protocol.demo;

import com.x.iot.protocol.support.DefaultTransport;
import com.x.iot.protocol.support.authentication.AuthenticationRequest;
import com.x.iot.protocol.support.authentication.AuthenticationResponse;
import com.x.iot.protocol.support.authentication.MqttAuthenticationRequest;
import com.x.iot.protocol.support.context.DeviceSessionCtx;
import com.x.iot.protocol.support.exception.AuthenticationException;
import com.x.iot.protocol.support.spi.Authenticator;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zoro.kong
 * &#064;className  MyAuthenticator
 * &#064;date  2025/6/13
 * &#064;description  自定义认证器示例
 */
@Slf4j
public class MyAuthenticator implements Authenticator {
    /**
     * 设备使用MD5算法对设备秘钥加密传输
     * @param request 认证请求消息
     * @param deviceSessionCtx -初始化session信息
     * @return 认证响应消息-携带Session
     * @throws AuthenticationException 认证失败异常
     */
    @Override
    public AuthenticationResponse authenticate(@Nonnull AuthenticationRequest request, @Nonnull DeviceSessionCtx deviceSessionCtx) throws AuthenticationException {
        if (request.getTransport().isSame(DefaultTransport.MQTT)){
            log.info("MQTT authenticate start.");
            //MQTT协议身份认证 request可以强制转换为MqttAuthenticationRequest对象。
            MqttAuthenticationRequest mqttAuthenticationRequest = (MqttAuthenticationRequest) request;
            //todo 自定义身份认证校验逻辑
            String password = mqttAuthenticationRequest.getPassword();
            String platformPwd  = deviceSessionCtx.getMetaDevice().getDeviceSecret();
            if (platformPwd == null){
                /*
                 * 异常消息错误码推荐使用61000-61999，具体错误码请咨询平台开发人员。避免与平台错误码重复。
                 */
                throw new AuthenticationException(61002, "device secret is empty.");
            }
                platformPwd = encryptMD5(platformPwd);
            if (!platformPwd.equals(password)){
                throw new AuthenticationException(61001, "device secret invalidate.");
            }
            /*
             * @ deviceSessionCtx -由平台生成，如有自定义字段需要保存到会话信息可以直接使用customized()字段保存，不要生成新的会话信息对象。
             * eg：deviceSessionCtx.customized().put("test", "test info");
             */
            return AuthenticationResponse.success(deviceSessionCtx);
        }

        throw new AuthenticationException(61000, "不支持的消息协议");
    }

    // MD5加密方法
    private String encryptMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(new MyAuthenticator().encryptMD5("ys0001"));
    }
}
