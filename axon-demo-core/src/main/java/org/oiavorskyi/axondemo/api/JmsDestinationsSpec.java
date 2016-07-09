package org.oiavorskyi.axondemo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component( "spec" )
public final class JmsDestinationsSpec implements InitializingBean {

    // Aliases for all queues used in application
    public static final String COMMANDS = "inbound.commands";
    public static final String STATUS   = "test.status";


    private final static String DEST_PREFIX         = "messaging.dest.";
    private final static String DUMP_CONTENT_SUFFIX = ".trace";

    private static final Map<String, Class<?>> destinationAliasToTargetClassMap;

    static {
        HashMap<String, Class<?>> map = new HashMap<>();
        map.put(COMMANDS, CargoTrackingCommandMessage.class);
        map.put(STATUS, String.class);

        destinationAliasToTargetClassMap = Collections.unmodifiableMap(map);
    }

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    GenericApplicationContext ctx;
    private Map<String, String>  destinationNames;
    private Map<String, Boolean> isTraceEnabled;
    private Map<String, String> destinationsToAliases = new ConcurrentHashMap<>();


    public void bindAliasToDestination( String alias, Destination destination ) {
        String realDestinationName = destination.toString();
        if ( destinationsToAliases.containsKey(realDestinationName) ) {
            // Already registered, skipping
            return;
        }

        log.debug("Binding alias {} to destination {}", alias, destination);
        Class<?> targetClass = destinationAliasToTargetClassMap.get(alias);
        if ( !destinationAliasToTargetClassMap.containsKey(alias) ) {
            throw new IllegalArgumentException(
                    "Unknown alias " + alias + " - please check configuration");
        }

        destinationsToAliases.put(realDestinationName, alias);
    }

    public boolean isTraceEnabled( String alias ) {
        return isTraceEnabled.get(alias);
    }

    public Map<String, String> getDestinationNames() {
        return destinationNames;
    }

    public Class<?> getTargetClassForDestination( Destination destination ) {
        String alias = destinationsToAliases.get(destination.toString());
        return destinationAliasToTargetClassMap.get(alias);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ConfigurableEnvironment env = ctx.getEnvironment();
        destinationNames = createDestinationsMap(env);
        isTraceEnabled = createIsTraceEnabled(env);
    }

    private Map<String, String> createDestinationsMap( ConfigurableEnvironment env ) {
        Map<String, String> result = new HashMap<>();

        for ( String alias : destinationAliasToTargetClassMap.keySet() ) {
            String destination = env.getProperty(DEST_PREFIX + alias);
            if ( destination != null ) {
                result.put(alias, destination);
            }
            // TODO: Throw exception when no destination were found
        }

        return Collections.unmodifiableMap(result);
    }

    private Map<String, Boolean> createIsTraceEnabled( ConfigurableEnvironment env ) {
        Map<String, Boolean> result = new HashMap<>();

        for ( String alias : destinationAliasToTargetClassMap.keySet() ) {
            Boolean shouldDumpContent = env.getProperty(DEST_PREFIX + alias + DUMP_CONTENT_SUFFIX,
                    Boolean.class, Boolean.FALSE);
            result.put(alias, shouldDumpContent);
        }

        return Collections.unmodifiableMap(result);
    }
}
