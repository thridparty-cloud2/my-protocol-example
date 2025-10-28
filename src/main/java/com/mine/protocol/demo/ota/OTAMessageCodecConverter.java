package com.mine.protocol.demo.ota;

import com.alibaba.fastjson.JSONArray;
import com.x.iot.protocol.support.exception.MessageDecodeException;
import com.x.iot.protocol.support.message.TagType;
import com.x.iot.protocol.support.message.TagTypeLenValue;
import com.x.iot.protocol.support.message.standard.OTAMessage;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zoro.kong
 * &#064;className  OTAMessageCodecConverter
 * &#064;date  2025/8/27
 * &#064;description  TODO 内置协议OTA消息转换器
 */
public class OTAMessageCodecConverter {
    private static final Map<String, Class<? extends OTACodecMessage>> CMD_MAP = new ConcurrentHashMap<>();
    static {
        CMD_MAP.put("0111", CMD_0111.class);
        CMD_MAP.put("0112", CMD_0112.class);
        CMD_MAP.put("0113", CMD_0113.class);
        CMD_MAP.put("0114", CMD_0114.class);
        CMD_MAP.put("0115", CMD_0115.class);
    }
    interface OTACodecMessage {
        void configure(OTAMessage.OTAInfo otaInfo, String value);

        int getId();

        String getType();

        String getFiled();
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static OTAMessage convertToOTAMessage(UndefinedPayloadMessage message, List<TagTypeLenValue> list) {
        Class<?> enumClass = CMD_MAP.get(message.getCmd());
        if (enumClass == null) {
            throw new MessageDecodeException(60011,"unknown message.");
        }
        OTAMessage otaMessage = new OTAMessage();
        otaMessage.setMsgSize((long) message.getLen());
        otaMessage.setPayload(message.payload());
        otaMessage.setPayloadFormat(message.payloadFormat());
        OTAMessage.OTAInfo otaInfo = new OTAMessage.OTAInfo();
        OTAMessage.OTAInfo.FirmwareInfo firmwareInfo = new OTAMessage.OTAInfo.FirmwareInfo();
        OTAMessage.OTAInfo.TaskInfo taskInfo = new OTAMessage.OTAInfo.TaskInfo();
        OTAMessage.OTAInfo.OTAStateInfo otaStateInfo = new OTAMessage.OTAInfo.OTAStateInfo();

        otaInfo.setFirmwareInfo(firmwareInfo);
        otaInfo.setOtaTaskInfo(taskInfo);
        otaInfo.setOtaStateInfo(otaStateInfo);

        for (TagTypeLenValue moduleTypeEntity : list) {
            Enum<?> cmdE = findEnumById((Class<Enum>) enumClass, moduleTypeEntity.getId());
            if (cmdE instanceof OTACodecMessage) {
                ((OTACodecMessage) cmdE).configure(otaInfo, String.valueOf(moduleTypeEntity.getValue()));
            }
        }

        otaMessage.setMessage(otaInfo);
        otaMessage.setSubType(new OTAMessage.OTASubType(message.getCmd()));
        return otaMessage;
    }

    private static <T extends Enum<T>> T findEnumById(Class<T> enumClass, Integer id) {
        for (T cmd : enumClass.getEnumConstants()) {
            if (cmd instanceof OTACodecMessage &&
                    ((OTACodecMessage) cmd).getId() == id) {
                return cmd;
            }
        }
        return null;
    }

    enum CMD_0111 implements OTACodecMessage {
        OTA_TASK_INFO(28, TagType.STRUCT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                List<TagTypeLenValue> entityList = JSONArray.parseArray(value, TagTypeLenValue.class);
                OTAMessage.OTAInfo.TaskInfo otaTaskInfo = otaInfo.getOtaTaskInfo();
                OTAMessage.OTAInfo.FirmwareInfo firmwareInfo = otaInfo.getFirmwareInfo();
                for (TagTypeLenValue entity : entityList) {
                    switch (entity.getId()){
                        case 4:
                            otaTaskInfo.setComponentNo((String) entity.getValue());
                            break;
                        case 5:
                            otaTaskInfo.setSourceVersion((String) entity.getValue());
                            break;
                        case 6:
                            otaTaskInfo.setTargetVersion((String) entity.getValue());
                            break;
                        case 7:
                            otaTaskInfo.setComponentType((Integer) entity.getValue());
                            break;
                        case 8:
                            otaTaskInfo.setBatteryLimit((Integer) entity.getValue());
                            break;
                        case 9:
                            otaTaskInfo.setUseSpace((Integer) entity.getValue());
                            break;
                        case 10:
                            otaTaskInfo.setMinSignal((Integer) entity.getValue());
                            break;
                        case 29:
                            firmwareInfo.setFileDownToken((String) entity.getValue());
                            break;
                    }
                    otaInfo.setFirmwareInfo(firmwareInfo);
                    otaInfo.setOtaTaskInfo(otaTaskInfo);
                }
            }
        },
        COMPONENT_NO(4, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentNo(value);
            }
        },
        SOURCE_VERSION(5, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setSourceVersion(value);
            }
        },
        TARGET_VERSION(6, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setTargetVersion(value);
            }
        },
        COMPONENT_TYPE(7, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentType(Integer.parseInt(value));
            }
        },
        BATTERY_LIMIT(8, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setBatteryLimit(Integer.parseInt(value));
            }
        },
        USE_SPACE(9, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setUseSpace(Integer.parseInt(value));
            }
        },
        MIN_SIGNAL(10, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setMinSignal(Integer.parseInt(value));
            }
        },
        HANDLE_TYPE(27, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setHandleType( value);
            }
        };
        @Getter
        private final int id;
        private final String type;
        private final String field;

        CMD_0111(int id, String type, String field) {
            this.id = id;
            this.type = type;
            this.field = field;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getFiled() {
            return field;
        }
    }

    enum CMD_0112 implements OTACodecMessage {
        COMPONENT_NO(4, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentNo(value);
            }
        },
        SOURCE_VERSION(5, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setSourceVersion(value);
            }
        },
        COMPONENT_TYPE(7, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentType(Integer.parseInt(value));
            }
        },
        OTA_SUBMIT(11, TagType.BOOL, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setOtaSubmit(Boolean.parseBoolean(value));
            }
        },
        FILE_SIGN(19, TagType.TEXT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setFileSign(value);
            }
        };
        @Getter
        private final int id;
        private final String type;
        private final String field;

        CMD_0112(int id, String type, String field) {
            this.id = id;
            this.type = type;
            this.field = field;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getFiled() {
            return field;
        }
    }
    enum CMD_0113 implements OTACodecMessage {
        DOWNLOAD_URL(1, TagType.TEXT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setUrl(value);
            }
        },
        FILE_SIZE(2, TagType.NUMBER, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setSize(Integer.parseInt(value));
            }
        },
        FILE_MD5(3, TagType.TEXT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setMd5(value);
            }
        },
        COMPONENT_NO(4, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentNo(value);
            }
        },
        TARGET_VERSION(6, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setTargetVersion(value);
            }
        },
        COMPONENT_TYPE(7, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentType(Integer.parseInt(value));
            }
        },
        DELAY_TIME(12, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setDelayTime(Integer.parseInt(value));
            }
        },
        FILE_CRC(15,TagType.TEXT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setCrc(value);
            }
        },
        FILE_SHA256(16, TagType.TEXT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setSha256(value);
            }
        },
        FILE_INDEX(30, TagType.NUMBER, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setIndex(Integer.parseInt(value));
            }
        },
        FILE_DOWN_TOKEN(29, TagType.TEXT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getFirmwareInfo().setFileDownToken(value);
            }
        },
        DOWNLOAD_INFO(20, TagType.STRUCT, "firmwareInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                List<TagTypeLenValue> entityList = JSONArray.parseArray(value, TagTypeLenValue.class);
                for (TagTypeLenValue moduleTypeEntity : entityList) {
                    switch (moduleTypeEntity.getId()) {
                        case 30:
                            otaInfo.getFirmwareInfo().setIndex((Integer) moduleTypeEntity.getValue());
                            break;
                        case 1:
                            otaInfo.getFirmwareInfo().setUrl(String.valueOf(moduleTypeEntity.getValue()));
                            break;
                        case 2:
                            otaInfo.getFirmwareInfo().setSize((Integer) moduleTypeEntity.getValue());
                            break;
                        case 3:
                            otaInfo.getFirmwareInfo().setMd5(String.valueOf(moduleTypeEntity.getValue()));
                            break;
                        case 15:
                            otaInfo.getFirmwareInfo().setCrc(String.valueOf(moduleTypeEntity.getValue()));
                            break;
                        case 16:
                            otaInfo.getFirmwareInfo().setSha256(String.valueOf(moduleTypeEntity.getValue()));
                            break;
                    }
                }
            }
        };

        @Getter
        private final int id;
        private final String type;
        private final String field;

        CMD_0113(int id, String type, String field) {
            this.id = id;
            this.type = type;
            this.field = field;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getFiled() {
            return field;
        }
    }
    enum CMD_0114 implements OTACodecMessage {
        COMPONENT_NO(4, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentNo(value);
            }
        },
        SOURCE_VERSION(5, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setSourceVersion(value);
            }
        },
        TARGET_VERSION(6, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setTargetVersion(value);
            }
        },
        COMPONENT_TYPE(7, TagType.NUMBER, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setComponentType(Integer.parseInt(value));
            }
        },
        OTA_STATE(13, TagType.NUMBER, "otaStateInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaStateInfo().setOtaState(Integer.parseInt(value));
            }
        },
        OTA_MESSAGE(14, TagType.TEXT, "otaStateInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaStateInfo().setOtaMessage(value);
            }
        };
        @Getter
        private final int id;
        private final String type;
        private final String field;

        CMD_0114(int id, String type, String field) {
            this.id = id;
            this.type = type;
            this.field = field;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getFiled() {
            return field;
        }
    }
    enum CMD_0115 implements OTACodecMessage {
        MODULE_VERSION(25, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value) {
                otaInfo.getOtaTaskInfo().setModuleVersion(value);
            }
        },
        MCU_VERSION(26, TagType.TEXT, "otaTaskInfo"){
            @Override
            public void configure(OTAMessage.OTAInfo otaInfo, String value){
                otaInfo.getOtaTaskInfo().setMcuVersion(value);
            }
        };
        @Getter
        private final int id;
        private final String type;
        private final String field;

        CMD_0115(int id, String type, String field) {
            this.id = id;
            this.type = type;
            this.field = field;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getFiled() {
            return field;
        }
    }
}
