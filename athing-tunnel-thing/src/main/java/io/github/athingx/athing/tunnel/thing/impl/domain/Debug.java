package io.github.athingx.athing.tunnel.thing.impl.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 调试开关，只有开启调试状态才会激活隧道打开
 *
 * @param status 调试状态，1（开启），其他（关闭）
 */
public record Debug(
        @SerializedName("status") Integer status
) {

    public boolean isEnable() {
        return status() == 1;
    }

}
