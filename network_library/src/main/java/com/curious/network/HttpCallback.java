package com.curious.network;


import com.curious.network.base.SAResponse;

import java.io.IOException;

public interface HttpCallback {
    void onFailure(SAResponse response, IOException e);

    void onResponse(SAResponse response) throws IOException;
}
