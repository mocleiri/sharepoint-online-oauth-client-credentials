package io.github.sharepoint_oauth.ws;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/sharepoint")
public interface SharepointWS {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/createFile")
    Response createFile(@FormDataParam("subdirectory") String subdirectory,
                        @FormDataParam("file") InputStream uploadedInputStream,
                        @FormDataParam("file") FormDataContentDisposition fileDetails);

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/getFile")
    Response getFile(@QueryParam("subdirectory") String subdirectory, @QueryParam("file")String fileName);
}
