package org.oiavorskyi.axondemo.itest.cargotracking;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.oiavorskyi.axondemo.api.JmsDestinationsSpec;
import org.oiavorskyi.axondemo.itest.JmsRequester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Configuration
public class CargoTrackingSUT {

    @Bean
    public API api() {
        return new JmsAPIImpl();
    }

    public static interface API {

        public Future<String> startCargoTracking( String cargoId, String correlationId, String
                timestamp );

    }

    private static class JmsAPIImpl implements API {

        @SuppressWarnings( "SpringJavaAutowiringInspection" )
        @Autowired
        private JmsRequester requester;

        private static ObjectMapper om = new ObjectMapper();

        static {
            om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            om.configure(SerializationFeature.INDENT_OUTPUT, true);
            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        @Override
        public Future<String> startCargoTracking( final String cargoId, final String correlationId,
                                                  final String timestamp ) {
            Map<String, Object> requestMap = new HashMap<String, Object>() {{
                put("commandId", "START");
                put("cargoId", cargoId);
                put("correlationId", correlationId);
                put("timestamp", timestamp);
            }};

            String requestJSON;
            try {
                requestJSON = om.writeValueAsString(requestMap);
            } catch ( JsonProcessingException e ) {
                throw new RuntimeException(e);
            }

            return requester.sendRequest(requestJSON, JmsDestinationsSpec.COMMANDS);
        }
    }

}
