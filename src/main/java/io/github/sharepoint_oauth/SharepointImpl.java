package io.github.sharepoint_oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.time.Instant;

@Component
public class SharepointImpl implements SharepointAPI {

    private static final int ONE_MINUTE_IN_MILLIS = 60000;

    private static Logger log = LoggerFactory.getLogger(SharepointImpl.class);

    @Value("${sharepoint-client-id}")
    private String clientId;

    @Value("${sharepoint-client-secret}")
    private String clientSecret;

    @Value("${sharepoint-resource}")
    private String resource;

    private String sharepointBaseUrl;

    private static final String SHAREPOINT_OAUTH_TOKEN_END_POINT = "https://accounts.accesscontrol.windows.net/tokens/OAuth/2";

    private Instant accessTokenExpiration = Instant.EPOCH;

    private String accessToken;

    private RestTemplate restTemplate;

    @PostConstruct
    private void configureSharepointBaseUrl() {

        /*
         Should configure to your site url here.
        */
        sharepointBaseUrl = "https://mycompany.sharepoint.com/sites/my-site/_api/web";
    }

    protected RestTemplate getRestTemplate() {

        if (restTemplate == null) {
            restTemplate = new RestTemplate();


            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

            requestFactory.setBufferRequestBody(false);
            requestFactory.setConnectTimeout(ONE_MINUTE_IN_MILLIS);
            requestFactory.setReadTimeout(ONE_MINUTE_IN_MILLIS);

            restTemplate.setRequestFactory(requestFactory);

            restTemplate.getInterceptors().add((request, body, execution) -> {
                acquireToken();
                request.getHeaders().set("Authorization", "Bearer " + accessToken);
                request.getHeaders().set("accept", "application/json");
                return execution.execute(request, body);
            });

        }

        return restTemplate;
    }


    /**
     *
     */
    protected synchronized void acquireToken() {

        Instant now = Instant.now();

        boolean tokenExpired = accessTokenExpiration.isAfter(now);

        String formattedAccessTokenExpiry = null;
        String formattedNow = null;


        formattedAccessTokenExpiry = FastDateFormat.getInstance("YYYY-MM-dd HH:mm:ss").format(accessTokenExpiration.toEpochMilli());
        formattedNow = FastDateFormat.getInstance("YYYY-MM-dd HH:mm:ss").format(now.toEpochMilli());

        if (tokenExpired)
            log.info("need to reacquire sharepoint access token due to expiry, expiry=" + formattedAccessTokenExpiry + ", now = " + formattedNow);


        if (accessToken != null && !tokenExpired) {
            return;
        }

        accessToken = null;
        accessTokenExpiration = Instant.EPOCH;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("grant_type", "client_credentials");

        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("resource", resource);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(SHAREPOINT_OAUTH_TOKEN_END_POINT, request, String.class);

        HttpStatus status = response.getStatusCode();

        if (!status.is2xxSuccessful())
            throw new RuntimeException("Failed to acquire client_credentials token, response = " + response.getBody());

        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode node = mapper.readTree(response.getBody());

            String serverResource = node.get("resource").asText();

            if (!resource.equals(serverResource)) {
                // should not be allowed
                throw new RuntimeException("client resource("+resource+") and server resource ("+serverResource+") differ");
            }

            String accessToken = node.get("access_token").asText();

            JsonNode tokenType = node.get("token_type");

            if (tokenType == null || !tokenType.asText().equals("Bearer"))
                throw new RuntimeException("Failed to acquire sharepoint token due to invalid token type = " + tokenType);

            JsonNode expiresIn = node.get("expires_in");

            long secondsTilExpiry = Long.parseLong(expiresIn.asText());

            log.info("sharepoint access token will expire in {} hours.", secondsTilExpiry/60/60);

            JsonNode expiresOn = node.get("expires_on");

            long secondsSinceTheEpoch = Long.parseLong(expiresOn.asText());

            accessTokenExpiration = Instant.ofEpochSecond(secondsSinceTheEpoch);

            formattedAccessTokenExpiry = FastDateFormat.getInstance("YYYY-MM-dd HH:mm:ss").format(accessTokenExpiration.toEpochMilli());

            log.info("acquired sharepoint access token, expires on " + formattedAccessTokenExpiry);

            this.accessToken = accessToken;

        } catch (IOException e) {
            throw new RuntimeException("failed to process oauth json payload = " + response.getBody(), e);
        }

    }

    private String getTopLevelFilesUrl() {
        return   getTopLevelUrl() + "/Files";

    }

    private String getTopLevelUrl() {
        return sharepointBaseUrl + "/GetFolderByServerRelativeUrl('Shared Documents')";
    }

    protected JsonNode getTopLevelFolders() throws IOException {
        return this.get( getTopLevelUrl() + "/Folders");
    }

    protected JsonNode get(String url) throws IOException {

        RestTemplate restTemplate = getRestTemplate();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper.readTree(response.getBody());

        return node;


    }

    @Override
    public HttpStatus createFile(String fileName, org.springframework.core.io.Resource byteArrayResource) {

        RestTemplate restTemplate = getRestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<org.springframework.core.io.Resource> requestEntity = new HttpEntity<org.springframework.core.io.Resource>(byteArrayResource, headers);

        ResponseEntity<String> response = restTemplate.exchange(getTopLevelFilesUrl() + "/add(url='"+fileName+"',overwrite=true)", HttpMethod.POST, requestEntity, String.class);

        return response.getStatusCode();
    }

    @Override
    public ResponseEntity<InputStream> getFile(String s){

        RestTemplate restTemplate = getRestTemplate();

        ResponseEntity<InputStream> response = restTemplate.execute(getTopLevelUrl() + "/Files('" + s + "')/$value", HttpMethod.GET, request -> {

        }, innerResponse -> {

            File tempFile = File.createTempFile("sharepoint", "data");

            IOUtils.copyLarge(innerResponse.getBody(), new FileOutputStream(tempFile));

            return new ResponseEntity<InputStream>(new FileInputStream(tempFile), innerResponse.getStatusCode());

        });

        return response;
    }

    @Override
    public HttpStatus createFile(String fileName, byte[] bytes) {

        return this.createFile(fileName, new ByteArrayResource(bytes));
    }

    // for testing
    protected void setClientId(String clientId) {
        this.clientId = clientId;
    }

    protected void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    protected void setResource(String resource) {
        this.resource = resource;
    }

    protected void setSharepointBaseUrl(String sharepointBaseUrl) {
        this.sharepointBaseUrl = sharepointBaseUrl;
    }
}
