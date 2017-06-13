package yweiss.local.jms.utils.worker;

import org.apache.activemq.ActiveMQConnectionFactory;
import yweiss.local.jms.utils.helper.DecryptorStub;
import yweiss.local.jms.utils.helper.MessagesSaver;
import yweiss.local.jms.utils.helper.SelectorUtils;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class DeleteFromDurable {

    private String jndiUser;
    private String jndiPassword;

    private String topicName;
    private String durableName;
    private String deletePattern;
    private String saveFileName;
    private String jmsUser;
    private String jmsPassword;

    private Session session;
    private Destination destination;

    public DeleteFromDurable(String[] args) {
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
        jndiUser = properties.getProperty("jndi.user");
        jndiPassword = DecryptorStub.decryptIfNeeded(properties.getProperty("jndi.user"));
        System.out.println("properties loaded: " + properties);
    }

    private void initParameters(String[] args) {
        if (args.length != 6) {
            throw new RuntimeException("6 arguments expected, but got " + args.length);
        }
        topicName = args[0];
        durableName = args[1];
        deletePattern = args[2];
        saveFileName = args[3];
        jmsUser = args[4];
        jmsPassword = DecryptorStub.decryptIfNeeded(args[5]);
    }

    public void execute() throws Exception {
        // !!!
        // specific to activemq, replace with necessary tibjms initialization
        // use app.properties to set JNDI params as in configs
        // !!!
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://127.0.0.1:61616");
        props.setProperty("topic.yweiss.testDurable", "yweiss.testDurable");

        InitialContext jndiContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
        ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true);

        destination = (Destination) jndiContext.lookup(topicName);
        Connection connection = connectionFactory.createConnection(jmsUser, jmsPassword);
        connection.setClientID("jms-utils");
        // !!!
        // end of connection initialization
        // !!!
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            List<Message> deletedMessages;
            deletedMessages = deleteMessages(SelectorUtils.buildFilterDate(deletePattern));
            System.out.println("saving " + deletedMessages.size() + " deleted messages");
            MessagesSaver.saveMessages(saveFileName, deletedMessages);
            System.out.println("messages saved");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private List<Message> deleteMessages(Date filterDate) throws Exception {
        List<Message> result = new ArrayList<>();
        MessageConsumer consumer = session.createDurableSubscriber((Topic) destination, durableName);
        int count = 0;
        long startTime = System.currentTimeMillis();
        long lastLoggedTime = startTime;
        Message nextMessage = consumer.receive(1000);
        while (nextMessage != null) {
            if (stopProcessing(nextMessage, filterDate)) {
                session.rollback();
                System.out.println("message date " + nextMessage.getJMSTimestamp() + " is greater than filter date " + filterDate.getTime());
                System.out.println("message deletion stopped");
                break;
            } else {
                result.add(nextMessage);
                session.commit();
            }
            count++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLoggedTime > 2000L) {
                System.out.println(count + " messages deleted");
                lastLoggedTime = currentTime;
            }
            nextMessage = consumer.receive(1000);
        }
        long workTime = System.currentTimeMillis() - startTime;
        String filterDateStr = (filterDate == null) ? "null" : filterDate.toString();
        System.out.println(count + " messages deleted in " + workTime + " millis by filterDate: " + filterDateStr);
        return result;
    }

    private boolean stopProcessing(Message nextMessage, Date filterDate) throws Exception {
        return (filterDate != null) && (nextMessage.getJMSTimestamp() > filterDate.getTime());
    }
}
