package org.oiavorskyi.axondemo.itest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.junit.After;
import org.junit.runner.RunWith;
import org.oiavorskyi.axondemo.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.ConnectionFactory;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(
        classes = { Application.Config.class },
        initializers = { ExecutionProfileAwareApplicationContextInitializer.class }
)
public abstract class AbstractITCase {

//    @Autowired
//    Broker broker;

    /**
     * Ensures that all destinations are removed between tests so no messages left on a broker
     */
    @After
    public void cleanupBroker() throws Exception {
//        ActiveMQDestination[] destinations = broker.getDestinations();
//
//        for ( ActiveMQDestination destination : destinations ) {
//            broker.removeDestination(broker.getAdminConnectionContext(), destination, 100);
//        }
    }

    @Configuration
    public static class TestUtilsConfig {

        @Autowired
        @Qualifier( "rawConnectionFactory" )
        private ConnectionFactory rawConnectionFactory;

        @Bean
        public JmsTemplate testJmsTemplate() {
            return new JmsTemplate(testConnectionFactory());
        }

        @Bean
        public ConnectionFactory testConnectionFactory() {
            CachingConnectionFactory result = new CachingConnectionFactory(rawConnectionFactory);
            result.setSessionCacheSize(2); // Don't need many connections for testing
            result.setCacheConsumers(false);
            return result;
        }
    }

    @Configuration
    @Profile( "integration" )
    public static class IntegrationEnvironmentConfig {

        @Bean
//        @DependsOn( "integrationBroker" )
        public ConnectionFactory rawConnectionFactory() {
            return new ActiveMQConnectionFactory("tcp://localhost:61616");
        }

//        @Bean
//        public Broker integrationBroker() throws Exception {
//            BrokerService brokerService = new BrokerService();
//
//            brokerService.setBrokerName("integration");
//            brokerService.setPersistent(false);
//            brokerService.addConnector("tcp://localhost:61616");
//            brokerService.setUseShutdownHook(false);
//            brokerService.start();
//
//            return brokerService.getBroker();
//        }

    }

}
