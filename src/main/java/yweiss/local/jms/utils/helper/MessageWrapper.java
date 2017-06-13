package yweiss.local.jms.utils.helper;

import java.io.Serializable;

public class MessageWrapper implements Serializable {

    public static final int TEXT_MESSAGE = 1;
    public static final int OBJECT_MESSAGE = 2;

    private static final long serialVersionUID = -1424754511533113265L;

    private String jmsMessageId;
    private int messageType;
    private Serializable messagePayload;

    public MessageWrapper() {
    }

    public String getJmsMessageId() {
        return jmsMessageId;
    }

    public void setJmsMessageId(String jmsMessageId) {
        this.jmsMessageId = jmsMessageId;
    }

    public Serializable getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(Serializable messagePayload) {
        this.messagePayload = messagePayload;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
