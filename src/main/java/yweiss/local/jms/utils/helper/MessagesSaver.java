package yweiss.local.jms.utils.helper;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MessagesSaver {

    public static void saveMessages(String fileName, List<Message> messages) throws Exception {
        saveMessagesInfo(fileName, messages);
        saveMessagesWithData(fileName, messages);
    }

    private static void saveMessagesInfo(String fileName, List<Message> messages) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + ".inf", false), "UTF-8"));
        try {
            for (Message message : messages) {
                writer.write(message.toString());
                writer.newLine();
                writer.newLine();
                if (message instanceof TextMessage) {
                    writer.write(((TextMessage)message).getText());
                    writer.newLine();
                } else if (message instanceof ObjectMessage) {
                    writer.write(((ObjectMessage) message).getObject().toString());
                    writer.newLine();
                }
                writer.newLine();
                writer.newLine();
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private static void saveMessagesWithData(String fileName, List<Message> messages) throws Exception {
        List<MessageWrapper> wrappedMessages = new ArrayList<>();
        for (Message message : messages) {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setJmsMessageId(message.getJMSMessageID());
            if (message instanceof ObjectMessage) {
                wrapper.setMessageType(MessageWrapper.OBJECT_MESSAGE);
                wrapper.setMessagePayload(((ObjectMessage) message).getObject());
            } else if (message instanceof TextMessage) {
                wrapper.setMessageType(MessageWrapper.TEXT_MESSAGE);
                wrapper.setMessagePayload(((TextMessage) message).getText());
            } else {
                System.out.println("unknown message type");
                System.out.println("message data not saved for ID: " + message.getJMSMessageID());
            }
            wrappedMessages.add(wrapper);
        }
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName + ".dat", false)));
        try {
            oos.writeObject(wrappedMessages);
        } finally {
            oos.flush();
            oos.close();
        }
    }
}
