package yweiss.local.jms.utils.worker;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class SendToQueue extends AbstractSendToDestination {

    public SendToQueue(String[] args) {
        super(args);
    }

    @Override
    protected void initializeConnectionAndDestination() throws Exception {
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

        destination = (Destination) jndiContext.lookup(destinationName);
        connection = connectionFactory.createConnection(jmsUser, jmsPassword);
        // !!!
        // end of connection initialization
        // !!!
    }
}
