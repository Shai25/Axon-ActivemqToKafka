package org.oiavorskyi.axondemo;

import com.ibm.mq.jms.MQConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.oiavorskyi.axondemo.api.JmsDestinationsSpec;
import org.oiavorskyi.axondemo.framework.ExtSimpleJmsListenerContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolutionException;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.*;

@Configuration
@PropertySources( {
        @PropertySource( "/messaging.properties" ),
        @PropertySource( value = "/messaging-${execution.profile}.properties",
                ignoreResourceNotFound = true )
} )
@EnableJms
public class MessagingConfig {

    private static Logger log = LoggerFactory.getLogger(MessagingConfig.class);

    @Value( "#{environment['messaging.connection.pool.size']}" )
    private int connectionPoolSize;

    @Bean
    public JmsTemplate jmsTemplate( ConnectionFactory jmsConnectionFactory,
                                    DestinationResolver destinationResolver ) {
        JmsTemplate jmsTemplate = new JmsTemplate(jmsConnectionFactory);
        jmsTemplate.setDestinationResolver(destinationResolver);
        return jmsTemplate;
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory( ConnectionFactory rawConnectionFactory ) {
        CachingConnectionFactory connFactory = new CachingConnectionFactory(rawConnectionFactory);
        connFactory.setSessionCacheSize(connectionPoolSize);

        return connFactory;
    }

    /**
     * Creates JmsListenerContainers when needed for specific listeners
     */
    @Bean
    public ExtSimpleJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory jmsConnectionFactory,
            DestinationResolver destinationResolver ) {

        ExtSimpleJmsListenerContainerFactory factory =
                new ExtSimpleJmsListenerContainerFactory();

        factory.setConnectionFactory(jmsConnectionFactory);
        factory.setDestinationResolver(destinationResolver);
        factory.setMessageConverter(null);

        return factory;
    }

    @Bean
    public DestinationResolver externalizableDestinationResolver(
            final GenericApplicationContext ctx,
            final JmsDestinationsSpec spec ) {
        return new DestinationResolver() {
            @Override
            public Destination resolveDestinationName( Session session, String destinationAlias,
                                                       boolean pubSubDomain ) throws JMSException {
                String actualDestinationName = spec.getDestinationNames().get(destinationAlias);

                if ( actualDestinationName == null ) {
                    log.error("Cannot resolve destination for alias {}", destinationAlias);
                    throw new DestinationResolutionException("Destination with alias " +
                            destinationAlias + " cannot be resolved");
                }

                Queue queue = session.createQueue(actualDestinationName);

                spec.bindAliasToDestination(destinationAlias, queue);
                return queue;
            }
        };
    }

    @Profile( "default" )
    @Configuration
    public static class DevelopmentConfig {

        @Bean
        public ConnectionFactory rawConnectionFactory() {
            return new ActiveMQConnectionFactory();
        }

    }

    @Profile( "production" )
    @Configuration
    public static class ProductionConfig {

        @Bean
        public ConnectionFactory rawConnectionFactory() {
            return new MQConnectionFactory();
        }

    }

}
