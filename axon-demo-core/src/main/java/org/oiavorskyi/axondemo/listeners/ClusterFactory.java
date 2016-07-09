/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oiavorskyi.axondemo.listeners;

import org.axonframework.eventhandling.Cluster;

public interface ClusterFactory {
    Cluster create(final String name);
}
