package yweiss.local.jms.utils.worker;

import yweiss.local.jms.utils.helper.DecryptorStub;
import yweiss.local.jms.utils.helper.MessageWrapper;
import yweiss.local.jms.utils.helper.MessagesLoader;
import yweiss.local.jms.utils.helper.SendConstants;

import javax.jms.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public abstract class AbstractSendToDestination {

    protected String destinationName;
    protected String sendMode;
    protected String fileType;
    protected String fileName;
    protected long delay;
    protected String jmsUser;
    protected String jmsPassword;

    protected Connection connection;
    protected Destination destination;
    protected Session session;

    protected abstract void initializeConnectionAndDestination() throws Exception;

    public AbstractSendToDestination(String[] args) {
        loadProperties();
        initParameters(args);
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("app.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("properties loaded: " + properties);
    }

    private void initParameters(String[] args) {
        if (args.length != 7) {
            throw new RuntimeException("7 arguments expected, but got " + args.length);
        }
        destinationName = args[0];
        sendMode = args[1];
        fileType = args[2];
        fileName = args[3];
        delay = Long.parseLong(args[4]);
        jmsUser = args[5];
        jmsPassword = DecryptorStub.decryptIfNeeded(args[6]);
    }

    public void execute() throws Exception {
        initializeConnectionAndDestination();
        try {
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sendMessages();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void sendMessages() throws Exception {
        List<MessageWrapper> messagesToSend = prepareMessagesToSend();
        MessageProducer producer = session.createProducer(destination);
        int count = 0;
        long startTime = System.currentTimeMillis();
        long lastLoggedTime = startTime;
        for (MessageWrapper wrapper : messagesToSend) {
            Message message = null;
            if (wrapper.getMessageType() == MessageWrapper.TEXT_MESSAGE) {
                message = session.createTextMessage((String) wrapper.getMessagePayload());
            } else if (wrapper.getMessageType() == MessageWrapper.OBJECT_MESSAGE) {
                message = session.createObjectMessage(wrapper.getMessagePayload());
            } else {
                System.out.println("unknown message type: " + wrapper.getMessageType() + " for message: " + wrapper.getJmsMessageId());
            }
            producer.send(message);
            count++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLoggedTime > 2000L) {
                System.out.println(count + " messages sent");
                lastLoggedTime = currentTime;
            }
            Thread.sleep(delay);
        }
        long workTime = System.currentTimeMillis() - startTime;
        System.out.println("send finished; " + count + " messages sent in " + workTime + " millis");
    }

    private List<MessageWrapper> prepareMessagesToSend() throws Exception {
        if (SendConstants.TYPE_BINARY.equals(fileType)) {
            return MessagesLoader.loadMessagesFromBinFile(sendMode, fileName);
        } else {
            return MessagesLoader.loadMessagesFromTextFile(fileName);
        }
    }
}
