/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oiavorskyi.axondemo.listeners;



import org.axonframework.eventhandling.ClusterSelector;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClassnameDynamicClusterSelectorFactory implements ClusterSelectorFactory {

    private final String prefix;
    private final ClusterFactory clusterFactory;

    public ClassnameDynamicClusterSelectorFactory(String prefix, ClusterFactory clusterFactory) {
        this.prefix = checkNotNull(prefix);
        this.clusterFactory = checkNotNull(clusterFactory);
    }

    @Override
    public ClusterSelector create() {
        return new ClassnameDynamicClusterSelector(prefix, clusterFactory);
    }
}
