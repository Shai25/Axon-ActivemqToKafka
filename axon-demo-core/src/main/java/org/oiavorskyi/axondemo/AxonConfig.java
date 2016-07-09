package org.oiavorskyi.axondemo;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.fs.FileSystemEventStore;
import org.axonframework.eventstore.fs.SimpleEventFileResolver;
import org.oiavorskyi.axondemo.aggregates.CargoTracking;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import com.viadeo.axonframework.eventhandling.cluster.*;
//import com.viadeo.axonframework.eventhandling.terminal.kafka.*;
//import com.viadeo.axonframework.eventhandling.cluster.ClassnameDynamicClusterSelectorFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.SimpleCluster;
import org.axonframework.eventhandling.ClassNamePrefixClusterSelector;
import org.oiavorskyi.axondemo.listeners.ClassnameDynamicClusterSelectorFactory;
import org.oiavorskyi.axondemo.listeners.ClusterFactory;

@Configuration
public class AxonConfig {
    
   
     public static final ClusterFactory CLUSTER_FACTORY = new ClusterFactory() {
        @Override
        public Cluster create(String name) {
            return new SimpleCluster(name);
        }
    };

    @Bean
    public EventBus eventBus() {
          ClassnameDynamicClusterSelectorFactory dynamicclusterSelectorFactory;
        dynamicclusterSelectorFactory = new ClassnameDynamicClusterSelectorFactory("",CLUSTER_FACTORY);
               return new ClusteringEventBus(dynamicclusterSelectorFactory.create());
    }

    
    @Bean
    public EventSourcingRepository<CargoTracking> cargoTrackingRepository( EventStore eventStore ) {
        EventSourcingRepository<CargoTracking> repository =
                new EventSourcingRepository<>(CargoTracking.class, eventStore);
        repository.setEventBus(eventBus());

        return repository;
    }


    @Bean
    EventStore eventStore() throws IOException {
        // TODO: Change to the proper version of event store (e.g. RDBMS or Redis-based)
        Path tempDirectory = Files.createTempDirectory("axon-demo-events");
        return new FileSystemEventStore(new SimpleEventFileResolver(tempDirectory.toFile()));
    }

}
