package io.github.athingx.athing.tunnel.thing.impl.client.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * 帧头
 *
 * @param frameId     帧ID
 * @param frameType   帧类型
 * @param sessionId   会话ID
 * @param serviceType 服务类型
 */
public record Header(

        @SerializedName("frame_id")
        long frameId,

        @SerializedName("frame_type")
        int frameType,

        @SerializedName("session_id")
        String sessionId,

        @SerializedName("service_type")
        String serviceType

) implements Constants {

    /**
     * 转换为响应帧头
     *
     * @return 响应帧头
     */
    public Header toReply() {
        return new Header(frameId, Constants.FRAME_TYPE_COMMON_RESPONSE, sessionId, serviceType);
    }

}

