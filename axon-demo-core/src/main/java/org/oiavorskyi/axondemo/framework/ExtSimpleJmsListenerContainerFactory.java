package org.oiavorskyi.axondemo.framework;

import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

import java.util.concurrent.Executor;

public class ExtSimpleJmsListenerContainerFactory extends SimpleJmsListenerContainerFactory {

    private boolean connectLazily = false;
    private String concurrency;
    private int concurrentConsumers = 1;
    private Executor taskExecutor;

    public void setConnectLazily( boolean connectLazily ) {
        this.connectLazily = connectLazily;
    }

    public void setConcurrency( String concurrency ) {
        this.concurrency = concurrency;
    }


    public void setConcurrentConsumers( int concurrentConsumers ) {
        this.concurrentConsumers = concurrentConsumers;
    }


    public void setTaskExecutor( Executor taskExecutor ) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    protected void initializeContainer( SimpleMessageListenerContainer instance ) {
        super.initializeContainer(instance);
        instance.setConnectLazily(this.connectLazily);
        if ( this.concurrency != null ) {
            instance.setConcurrency(this.concurrency);
        }
        instance.setConcurrentConsumers(this.concurrentConsumers);
        if ( this.taskExecutor != null ) {
            instance.setTaskExecutor(this.taskExecutor);
        }
    }
}
