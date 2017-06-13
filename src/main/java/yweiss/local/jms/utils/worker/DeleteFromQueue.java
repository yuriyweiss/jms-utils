package yweiss.local.jms.utils.worker;

import org.apache.activemq.ActiveMQConnectionFactory;
import yweiss.local.jms.utils.helper.DecryptorStub;
import yweiss.local.jms.utils.helper.MessagesSaver;
import yweiss.local.jms.utils.helper.SelectorUtils;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DeleteFromQueue {

    private String jndiUser;
    private String jndiPassword;

    private String queueName;
    private String deletePattern;
    private String saveFileName;
    private String jmsUser;
    private String jmsPassword;

    private Session session;
    private Destination destination;

    public DeleteFromQueue(String[] args) {
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
        if (args.length != 5) {
            throw new RuntimeException("5 arguments expected, but got " + args.length);
        }
        queueName = args[0];
        deletePattern = args[1];
        saveFileName = args[2];
        jmsUser = args[3];
        jmsPassword = DecryptorStub.decryptIfNeeded(args[4]);
    }

    public void execute() throws Exception {
        // !!!
        // specific to activemq, replace with necessary tibjms initialization
        // use app.properties to set JNDI params as in configs
        // !!!
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://127.0.0.1:61616");
        props.setProperty("queue.yweiss.test1", "yweiss.test1");

        InitialContext jndiContext = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
        ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true);

        destination = (Destination) jndiContext.lookup(queueName);
        Connection connection = connectionFactory.createConnection(jmsUser, jmsPassword);
        // !!!
        // end of connection initialization
        // !!!
        try {
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            List<Message> deletedMessages;
            if (SelectorUtils.isSelectByIds(deletePattern)) {
                deletedMessages = deleteMessagesByIds();
            } else {
                deletedMessages = deleteMessagesBySelector(SelectorUtils.buildMessagesSelector(deletePattern));
            }
            System.out.println("saving " + deletedMessages.size() + " deleted messages");
            MessagesSaver.saveMessages(saveFileName, deletedMessages);
            System.out.println("messages saved");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private List<Message> deleteMessagesByIds() throws Exception {
        List<Message> result = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        while ((s = in.readLine()) != null) {
            if (s.trim().length() > 0) {
                String selector = "JMSMessageID = '" + s.trim() + "'";
                result.addAll(deleteMessagesBySelector(selector));
            }
        }
        return result;
    }

    private List<Message> deleteMessagesBySelector(String selector) throws Exception {
        List<Message> result = new ArrayList<>();
        MessageConsumer consumer = session.createConsumer(destination, selector);
        int count = 0;
        long startTime = System.currentTimeMillis();
        long lastLoggedTime = startTime;
        Message nextMessage = consumer.receive(1000);
        while (nextMessage != null) {
            result.add(nextMessage);
            nextMessage = consumer.receive(1000);
            count++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLoggedTime > 2000L) {
                System.out.println(count + " messages deleted");
                lastLoggedTime = currentTime;
            }
        }
        long workTime = System.currentTimeMillis() - startTime;
        System.out.println(count + " messages deleted in " + workTime + " millis by selector: [" + selector + "]");
        return result;
    }
}
