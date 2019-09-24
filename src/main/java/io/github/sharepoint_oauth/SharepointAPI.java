package io.github.sharepoint_oauth;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;

/**
 * Represents the API we allow to be used to access Sharepoint.
 */
public interface SharepointAPI {

    HttpStatus createFile(String fileName, byte[] bytes);

    HttpStatus createFile(String fileName, Resource byteArrayResource);

    ResponseEntity<InputStream> getFile(String fileName);
}
