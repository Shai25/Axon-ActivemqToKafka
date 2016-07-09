package org.oiavorskyi.axondemo.framework;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.oiavorskyi.axondemo.api.JmsDestinationsSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

@Component
public class DestinationBasedJackson2MessageConverter extends MappingJackson2MessageConverter {

    private final TypeFactory typeFactory;

    @Autowired
    private JmsDestinationsSpec destinationsSpec;

    public DestinationBasedJackson2MessageConverter() {
        super();
        typeFactory = new ObjectMapper().getTypeFactory();
    }

    @Override
    protected JavaType getJavaTypeForMessage( Message jmsMessage ) throws JMSException {
        Destination destination = jmsMessage.getJMSDestination();
        Class<?> targetClass = destinationsSpec.getTargetClassForDestination(destination);

        if ( targetClass == null ) {
            throw new IllegalStateException("No configured class found for destination " +
                    destination + ". Conversion from JSON to objects is not possible");
        }

        return typeFactory.constructType(targetClass);
    }
}
