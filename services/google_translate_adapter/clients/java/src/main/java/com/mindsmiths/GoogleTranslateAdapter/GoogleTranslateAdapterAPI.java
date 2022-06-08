package com.mindsmiths.GoogleTranslateAdapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import com.mindsmiths.sdk.core.api.BaseMessage;
import com.mindsmiths.sdk.core.api.CallbackResult;
import com.mindsmiths.sdk.messaging.Messaging;


public class GoogleTranslateAdapterAPI {
    private static final String topic = Messaging.getInputTopicName("google_translate_adapter");

    public static void translateMessage(String userId, String text, String source, String target) {
        Serializable payload = new InputDataPayload(userId, text, source, target);
        BaseMessage message = new BaseMessage("TRANSLATE_MESSAGE", payload);
        message.send(topic);
        new CallbackResult(message.getConfiguration().getMessageId(), Result.class).save();
    }
}