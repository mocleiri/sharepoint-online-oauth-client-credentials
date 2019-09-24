package io.github.sharepoint_oauth;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/*
 Located in the same package as SharepointImpl so we can access the protected methods.
 */
@RunWith(JUnit4.class)
public class TestSharepointImpl {

    private static SharepointImpl sharepoint = new SharepointImpl();

    @BeforeClass
    public static void acquireToken() throws UnsupportedEncodingException {

        String clientId = System.getProperty("client_id");
        String clientSecret = System.getProperty("client_secret");
        String resource = System.getProperty("resource");

        sharepoint.setClientId(clientId);
        sharepoint.setClientSecret(clientSecret);
        sharepoint.setResource(resource);

        // change this to be your sharepoint site
        sharepoint.setSharepointBaseUrl("https://mycompany.sharepoint.com/sites/my-site/_api/web");
    }

    @Test
    public void uploadFile () throws IOException {

        sharepoint.createFile("unit-test-file.txt", "some content".getBytes(Charset.forName("UTF8")));

        ResponseEntity<InputStream> response = sharepoint.getFile("unit-test-file.txt");

        List<String> lines = IOUtils.readLines(response.getBody(), "UTF8");

        String content = StringUtils.join(lines, "\n");

        MatcherAssert.assertThat("content matches", content, is ("some content"));

        File tempFile = File.createTempFile("something", "suffix");

        FileUtils.writeStringToFile(tempFile, "this is a test of an actual file", "UTF8");

        sharepoint.createFile("unit-test-actual-file.txt", new FileSystemResource(tempFile));

        response = sharepoint.getFile("unit-test-actual-file.txt");

        lines = IOUtils.readLines(response.getBody(), "UTF8");

        content = StringUtils.join(lines, "\n");

        MatcherAssert.assertThat("content matches", content, is ("this is a test of an actual file"));


    }
}
