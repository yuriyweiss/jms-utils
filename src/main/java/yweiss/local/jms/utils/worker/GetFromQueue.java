package yweiss.local.jms.utils.worker;

import org.apache.activemq.ActiveMQConnectionFactory;
import yweiss.local.jms.utils.helper.DecryptorStub;
import yweiss.local.jms.utils.helper.MessagesSaver;
import yweiss.local.jms.utils.helper.SelectorUtils;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class GetFromQueue {

    private String queueName;
    private String selectPattern;
    private String saveFileName;
    private String jmsUser;
    private String jmsPassword;

    private Session session;
    private Destination destination;

    public GetFromQueue(String[] args) {
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
        if (args.length != 5) {
            throw new RuntimeException("5 arguments expected, but got " + args.length);
        }
        queueName = args[0];
        selectPattern = args[1];
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
            List<Message> loadedMessages = loadMessagesBySelector(SelectorUtils.buildMessagesSelector(selectPattern));
            System.out.println("saving " + loadedMessages.size() + " loaded messages");
            MessagesSaver.saveMessages(saveFileName, loadedMessages);
            System.out.println("messages saved");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private List<Message> loadMessagesBySelector(String selector) throws Exception {
        List<Message> result = new ArrayList<>();
        System.out.println("starting queue browser");
        long startTime = System.currentTimeMillis();
        QueueBrowser browser = session.createBrowser((Queue) destination, selector);
        Enumeration messages = browser.getEnumeration();
        long workTime = System.currentTimeMillis() - startTime;
        System.out.println("queue browser got messages by selector: [" + selector + "] in " + workTime + " millis");
        int count = 0;
        while (messages.hasMoreElements()) {
            result.add((Message) messages.nextElement());
            count++;
        }
        workTime = System.currentTimeMillis() - startTime;
        System.out.println(count + " messages loaded from queue browser in " + workTime + " millis");
        return result;
    }
}
