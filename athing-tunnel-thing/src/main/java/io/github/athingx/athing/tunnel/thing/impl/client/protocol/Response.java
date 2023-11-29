package io.github.athingx.athing.tunnel.thing.impl.client.protocol;

import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.common.gson.GsonFactory;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 帧应答数据
 *
 * @param code    应答码
 * @param message 应答消息
 */
public record Response(

        @SerializedName("code")
        int code,

        @SerializedName("msg")
        String message

) implements Frame.Data {
    
    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(GsonFactory.getGson().toJson(this).getBytes(UTF_8));
    }

}
