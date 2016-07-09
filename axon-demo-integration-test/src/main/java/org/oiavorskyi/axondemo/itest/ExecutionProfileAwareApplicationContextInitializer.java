package org.oiavorskyi.axondemo.itest;

import org.oiavorskyi.axondemo.Application;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class ExecutionProfileAwareApplicationContextInitializer implements
        ApplicationContextInitializer<GenericApplicationContext> {

    private static final String DEFAULT_INTEGRATION_PROFILE = "integration";

    @Override
    public void initialize( GenericApplicationContext ctx ) {
        String profile = DEFAULT_INTEGRATION_PROFILE;
        String executionProfile = Application.identifyCurrentExecutionProfile();

        if ( !Application.DEFAULT_PROFILE.equals(executionProfile) ) {
            // Overriding default integration profile to allow running smoke tests in
            // production environment(s)
            profile = executionProfile;
        }

        Application.applyExecutionProfileToApplicationContext(profile, ctx);
    }
}
