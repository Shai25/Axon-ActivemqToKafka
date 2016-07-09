package org.oiavorskyi.axondemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Application {

    public static final  String   DEFAULT_PROFILE = "default";
    private static final String[] VALID_PROFILES  = new String[] { "production" };
    private static       Logger   log             = LoggerFactory.getLogger(Application.class);

    public static void main( String[] args ) throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Config.class);

        String executionProfile = identifyCurrentExecutionProfile();
        applyExecutionProfileToApplicationContext(executionProfile, context);

        context.refresh();
        context.registerShutdownHook();

        log.info("Application has successfully started");
    }

    /**
     * Identifies execution profile to be used. Only Spring beans configured within this profile or
     * no profile at all will be loaded. This opens possibility to switch between different
     * environments without any code changes.
     *
     * This method looks for a file with name "runtime.profile" in the directory from where process
     * was started and if it exists assumes first line in this file as a name of profile.
     *
     * @return name of Spring profile to be used for execution of application
     */
    public static String identifyCurrentExecutionProfile() {
        String result = DEFAULT_PROFILE;

        log.debug("Identifying execution profile: working directory is {}",
                Paths.get("").toAbsolutePath().normalize().toString());

        Path pathToRuntimeProfileMarkerFile = FileSystems.getDefault().getPath("runtime.profile");
        boolean markerExists = Files.exists(pathToRuntimeProfileMarkerFile);

        if ( markerExists ) {
            try {
                List<String> values = Files.readAllLines(pathToRuntimeProfileMarkerFile,
                        Charset.defaultCharset());
                String profileName = values.get(0);
                log.debug("Identifying execution profile: found runtime.profile file with value " +
                        profileName);
                if ( Arrays.binarySearch(VALID_PROFILES, profileName) >= 0 ) {
                    result = profileName;
                }
            } catch ( IOException e ) {
                // Ignore exception and assume default profile
            }
        } else {
            log.debug("Identifying execution profile: no runtime.profile file was found");
        }

        return result;
    }

    /**
     * Applies execution profile to Spring Application Context and registers profile property so it
     * could be used to add profile-specific property files to the Environment.
     *
     * For property files to be actually added append PropertySource to the @Configuration component
     * like this:
     * <pre>
     *    @PropertySources( {
     *            @PropertySource( "/my.properties" ),
     *            @PropertySource( value = "/my-${execution.profile}.properties",
     *                    ignoreResourceNotFound = true )
     *    } )
     *    public class MyConfig {
     *        ...
     *    }
     * </pre>
     *
     * Make sure to use {code}ignoreResourceNotFound=true{code} as otherwise Spring will throw
     * exception when profile-specific property file is not found.
     */
    public static void applyExecutionProfileToApplicationContext( String executionProfile,
                                                                  GenericApplicationContext ctx ) {
        log.info("Identifying execution profile: {} execution profile was selected",
                executionProfile);
        ConfigurableEnvironment env = ctx.getEnvironment();
        env.setActiveProfiles(executionProfile);
        Map<String, Object> customProperties =
                Collections.singletonMap("execution.profile", (Object) executionProfile);
        env.getPropertySources().addFirst(new MapPropertySource("custom", customProperties));
        log.info("Identifying execution profile: *-{}.propeties files will be added to properties" +
                " resolution process", executionProfile);
    }

    @Configuration
    @ComponentScan( { "org.oiavorskyi.axondemo" } )
    @PropertySources( {
            @PropertySource( "/application.properties" ),
            @PropertySource( value = "/application-${execution.profile}.properties",
                    ignoreResourceNotFound = true )
    } )
    public static class Config {

        @Bean
        public LocalValidatorFactoryBean validatorFactoryBean() {
            return new LocalValidatorFactoryBean();
        }

    }
}
