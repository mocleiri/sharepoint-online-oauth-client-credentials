package io.github.sharepoint_oauth.ws.impl;

import io.github.sharepoint_oauth.SharepointAPI;
import io.github.sharepoint_oauth.ws.SharepointWS;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.io.InputStream;

@Component("sharepoint-ws")
public class SharepointWSImpl implements SharepointWS {

    private static final Logger log = LoggerFactory.getLogger(SharepointWSImpl.class);

    @Autowired
    private SharepointAPI sharepointAPI;

    @Override
    public Response createFile(String subdirectory, InputStream uploadedInputStream, FormDataContentDisposition fileDetails) {
        log.info("createFile(subdirectory=" + subdirectory + "): ");
        return Response.ok().entity("working on the solution").build();
    }

    @Override
    public Response getFile(String subdirectory, String fileName) {

        log.info("getFile (subdirectory=" + subdirectory + ", file="+fileName+"): ");

        if (subdirectory == null || fileName == null)
            return Response.serverError().entity("Missing url parameters").build();

        sharepointAPI.getFile(fileName);

        return Response.ok().entity("working on the solution").build();
    }
}
