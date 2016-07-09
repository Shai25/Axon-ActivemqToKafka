/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oiavorskyi.axondemo.kafkaterminal;


//package com.viadeo.axonframework.eventhandling.terminal.kafka;

import com.google.common.base.Optional;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProducerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerFactory.class);
    private static Optional<String> clientId = Optional.absent();

    private final Properties baseProperties;

    public ProducerFactory(final ProducerConfig producerConfig) {
        this.baseProperties = checkNotNull(producerConfig).props().props();
    }

    public <K, V> Producer<K, V> create() {
        final Properties properties = getPropertiesFor(getClientId());
        return new Producer<>(new ProducerConfig(properties));
    }

    protected Properties getPropertiesFor(final String clientId) {
        final Properties properties = new Properties();

        properties.put("serializer.class", EventMessageSerializer.class.getName());
        properties.put("key.serializer.class", StringEncoder.class.getName());

        for (final Object key : baseProperties.keySet()) {
            properties.put(key, baseProperties.get(key));
        }

        properties.put("client.id", clientId);

        LOGGER.debug("Get properties for '{}' : {}", clientId, properties);

        return properties;
    }

    protected String getClientId() {
        if ( ! clientId.isPresent()) {
            try {
                clientId = Optional.fromNullable(InetAddress.getLocalHost().getCanonicalHostName());
            } catch (UnknownHostException e) {
                clientId = Optional.of(UUID.randomUUID().toString());
                LOGGER.warn("Unable to define 'client.id' property. Generating default : {}", clientId, e);
            }
        }
        return clientId.get();
    }

}
