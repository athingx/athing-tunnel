package io.github.athingx.athing.tunnel.thing.impl.domain;

import com.google.gson.annotations.SerializedName;

public record Debug(
        @SerializedName("status") Integer status
) {

    public boolean isEnable() {
        return status() == 1;
    }

}
