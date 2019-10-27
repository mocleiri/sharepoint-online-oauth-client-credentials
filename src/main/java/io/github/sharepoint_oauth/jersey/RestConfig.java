package io.github.sharepoint_oauth.jersey;

import io.github.sharepoint_oauth.ws.impl.SharepointWSImpl;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/rest")
@Configuration
public class RestConfig extends ResourceConfig {

    public RestConfig() {
        super();
        register(SharepointWSImpl.class);
        register(MultiPartFeature.class);
    }


//    public Set<Class<?>> getClasses() {
//        return new HashSet<Class<?>>(
//                Arrays.asList(
//                        SharepointWSImpl.class));
//    }
}
