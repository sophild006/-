package com.solid.analytics.transport;

import com.solid.analytics.model.Response;

public interface Transport {

    Response transfer(byte[] data);

}
