/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oiavorskyi.axondemo.kafkaterminal;



import org.axonframework.domain.EventMessage;

import java.text.Normalizer;

public class DefaultTopicStrategy implements TopicStrategy {

    @Override
    public String getTopic(final EventMessage eventMessage) {
        return normalize(eventMessage.getPayloadType().getName());
    }

    protected String normalize(final String topic) {
        return Normalizer
                .normalize(topic, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\$", "_")
                .toLowerCase();
    }
}
