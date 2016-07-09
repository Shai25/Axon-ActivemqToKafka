package org.oiavorskyi.axondemo.itest;


import org.oiavorskyi.axondemo.api.JmsDestinationsSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Based on code and ideas from http://codedependents
 * .com/2010/03/04/synchronous-request-response-with-activemq-and-spring/
 *
 * The major difference is using of non-standard headers for CorrelationId and ReplyTo so it will
 * not interfere with production code which might use these headers for other reasons.
 */
@Component
public class JmsRequester {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final JmsTemplate jmsTemplate;
    private final DestinationResolver destinationResolver;

    @Autowired
    public JmsRequester( final JmsTemplate jmsTemplate,
                         final DestinationResolver destinationResolver ) {
        this.jmsTemplate = jmsTemplate;

        this.destinationResolver = destinationResolver;
    }

    public Future<String> sendRequest( final Object message, final String requestDestName ) {
        return executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return jmsTemplate.execute(new ProducerConsumer(message, requestDestName,
                        destinationResolver), true);
            }
        });
    }

    private static final class ProducerConsumer implements SessionCallback<String> {

        private final Object msg;

        private final String requestDestinationName;
        private final DestinationResolver destinationResolver;


        public ProducerConsumer( Object msg, String requestDestinationName,
                                 DestinationResolver destinationResolver ) {
            this.msg = msg;
            this.requestDestinationName = requestDestinationName;
            this.destinationResolver = destinationResolver;
        }

        public String doInJms( final Session session ) throws JMSException {
            MessageConsumer consumer = null;
            MessageProducer producer = null;
            try {
                final String testCorrelationID = UUID.randomUUID().toString();


                Destination requestDestination = destinationResolver.resolveDestinationName(session,
                        requestDestinationName, false);
                Destination statusReplyDestination = destinationResolver.resolveDestinationName(session,
                        JmsDestinationsSpec.STATUS, false);

                // Create the consumer first!
                consumer = session.createConsumer(statusReplyDestination,
                        "TestCorrelationID = '" + testCorrelationID + "'");

                final TextMessage textMessage = session.createTextMessage((String) msg);
                textMessage.setStringProperty("TestCorrelationID", testCorrelationID);

                // Send the request second!
                producer = session.createProducer(requestDestination);
                producer.send(requestDestination, textMessage);

                // Block on receiving the response
                Message response = consumer.receive();

                if ( response instanceof TextMessage ) {
                    return ((TextMessage) response).getText();
                } else {
                    throw new ClassCastException("Expected javax.jms.TextMessage but got" +
                            response.getClass().getName());
                }
            } finally {
                // Don't forget to close your resources
                JmsUtils.closeMessageConsumer(consumer);
                JmsUtils.closeMessageProducer(producer);
            }
        }
    }

}
