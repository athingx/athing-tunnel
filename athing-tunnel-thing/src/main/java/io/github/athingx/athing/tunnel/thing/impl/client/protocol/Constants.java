package io.github.athingx.athing.tunnel.thing.impl.client.protocol;

public interface Constants {

    /**
     * 帧类型：通用响应
     */
    int FRAME_TYPE_COMMON_RESPONSE = 1;

    /**
     * 帧类型：创建会话
     */
    int FRAME_TYPE_CREATE_SESSION = 2;

    /**
     * 帧类型：关闭会话
     */
    int FRAME_TYPE_CLOSE_SESSION = 3;

    /**
     * 帧类型：数据传输
     */
    int FRAME_TYPE_DATA_TRANSPORT = 4;

    /**
     * 应答码：创建会话成功
     */
    int CODE_CREATE_SESSION_SUCCESS = 0;

    /**
     * 应答码：创建会话被拒
     */
    int CODE_CREATE_SESSION_REJECT = 2;

    /**
     * 应答码：创建会话失败
     */
    int CODE_CREATE_SESSION_FAILURE = 3;

    /**
     * 应答码：访问端关闭会话
     */
    int CODE_CLOSED_SESSION_BY_ACCESS = 0;

    /**
     * 应答码：客户端关闭会话
     */
    int CODE_CLOSED_SESSION_BY_CLIENT = 1;

    /**
     * 应答码：平台关闭会话（访问端失联）
     */
    int CODE_CLOSED_SESSION_BY_PLATFORM_TO_ACCESS = 2;

    /**
     * 应答码：平台关闭会话（客户端失联）
     */
    int CODE_CLOSED_SESSION_BY_PLATFORM_TO_CLIENT = 3;

    /**
     * 应答码：平台关闭会话（平台主动关闭）
     */
    int CODE_CLOSED_SESSION_BY_PLATFORM = 4;

}
