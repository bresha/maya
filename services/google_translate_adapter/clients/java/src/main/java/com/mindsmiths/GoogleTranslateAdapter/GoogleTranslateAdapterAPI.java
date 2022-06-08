package com.mindsmiths.GoogleTranslateAdapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import com.mindsmiths.sdk.core.api.BaseMessage;
import com.mindsmiths.sdk.core.api.CallbackResult;
import com.mindsmiths.sdk.messaging.Messaging;


public class GoogleTranslateAdapterAPI {
    private static final String topic = Messaging.getInputTopicName("google_translate_adapter");

    public static void getSomething(String userId) {
        Serializable payload = new InputDataPayload(userId);
        BaseMessage message = new BaseMessage("GET_SOMETHING", payload);
        message.send(topic);
        new CallbackResult(message.getConfiguration().getMessageId(), Result.class).save();
    }

    public static void doSomething(String userId) {
        Serializable payload = new InputDataPayload(userId);
        BaseMessage message = new BaseMessage("DO_SOMETHING", payload);
        message.send(topic);
    }
}