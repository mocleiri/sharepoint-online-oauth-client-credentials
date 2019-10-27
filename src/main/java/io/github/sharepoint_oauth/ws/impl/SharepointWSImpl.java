package io.github.sharepoint_oauth.ws.impl;

import io.github.sharepoint_oauth.ws.SharepointWS;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.io.InputStream;

@Component
public class SharepointWSImpl implements SharepointWS {
    @Override
    public Response createFile(String subdirectory, InputStream uploadedInputStream, FormDataContentDisposition fileDetails) {
        return Response.ok().entity("working on the solution").build();
    }

    @Override
    public Response getFile(String subdirectory, String fileName) {
        if (subdirectory == null || fileName == null)
            return Response.serverError().entity("Missing url parameters").build();


        return Response.ok().entity("working on the solution").build();
    }
}
