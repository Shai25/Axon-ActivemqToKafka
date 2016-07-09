/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oiavorskyi.axondemo.kafkaterminal;

//package com.viadeo.axonframework.eventhandling.terminal.kafka;

import org.axonframework.domain.EventMessage;

public interface TopicStrategy {
    String getTopic(final EventMessage eventMessage);
}
