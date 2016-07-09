/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oiavorskyi.axondemo.kafkaterminal;

//package com.viadeo.axonframework.eventhandling.terminal.kafka;

import com.google.common.base.Preconditions;
import org.axonframework.domain.EventMessage;

public class PrefixTopicStrategy extends DefaultTopicStrategy {

    private final String prefix;

    public PrefixTopicStrategy(final String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix);
    }

    @Override
    public String getTopic(final EventMessage eventMessage) {
        return prefix + super.getTopic(eventMessage);
    }
}
