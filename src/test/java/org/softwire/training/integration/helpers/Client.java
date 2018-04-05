package org.softwire.training.integration.helpers;

import com.amazonaws.services.s3.Headers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.softwire.training.api.ApiImage;
import org.softwire.training.api.ApiUser;
import org.softwire.training.models.License;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class Client implements Closeable {
    private static final String ADMIN_USERNAME = "admin";

    public static final byte[] IMAGE_CONTENT;
    static {
        try {
            IMAGE_CONTENT = ByteStreams.toByteArray(
                    Client.class.getClassLoader().getResourceAsStream("LargeImage.jpg"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final CloseableHttpClient httpClient;
    private final HttpHost httpHost;

    Client(int port, String adminPassword) {
        httpHost = new HttpHost("localhost", port, "http");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(ADMIN_USERNAME, adminPassword));

        httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    private <T> ApiResponse<T> execute(HttpRequest request, TypeReference<T> entityClassOnSuccess) {
        try (CloseableHttpResponse response = httpClient.execute(httpHost, request)) {

            String entity = null;
            if (response.getEntity() != null) {
                entity = EntityUtils.toString(response.getEntity());
            }

            return new ApiResponse<>(
                    response.getStatusLine().getStatusCode(),
                    entity,
                    entityClassOnSuccess
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpPost buildPostRequest(String path, String json) {
        HttpPost request = new HttpPost(path);

        ContentType contentType = ContentType.APPLICATION_JSON;
        request.setEntity(new StringEntity(json, contentType.getCharset()));
        request.setHeader(Headers.CONTENT_TYPE, contentType.toString());

        return request;
    }

    private HttpPost buildPostRequest(String path) {
        return new HttpPost(path);
    }

    private HttpGet buildGetRequest(String path) {
        return new HttpGet(path);
    }

    private HttpDelete buildDeleteRequest(String uuid) {
        return new HttpDelete(uuid);
    }

    public ApiResponse<ApiUser> createUser(String name) {
        return execute(buildPostRequest("/tokens/", "{\"name\": \"" + name + "\"}"), new TypeReference<ApiUser>() {
        });
    }

    public ApiResponse<ApiUser> getUser(String uuid) {
        return execute(buildGetRequest("/tokens/id/" + uuid), new TypeReference<ApiUser>() {
        });
    }

    public ApiResponse<Void> deleteUser(String uuid) {
        return execute(buildDeleteRequest(MessageFormat.format("/tokens/id/{0}", uuid)), new TypeReference<Void>() {
        });
    }

    public ApiResponse<Iterable<ApiUser>> getAllUsers() {
        return execute(buildGetRequest("/tokens/"), new TypeReference<Iterable<ApiUser>>() {
        });
    }

    public ApiResponse<ApiUser> bootstrapUser(String name) {
        return execute(
                buildGetRequest(MessageFormat.format("/tokens/bootstrap?name={0}", name)),
                new TypeReference<ApiUser>() {
                }
        );
    }

    public ApiResponse<ApiImage> createImage(String user, String author, String name, License license) throws JsonProcessingException {
        return createImage(
                user,
                author,
                name,
                license,
                IMAGE_CONTENT,
                ContentType.IMAGE_JPEG,
                "example.jpeg");
    }

    public ApiResponse<ApiImage> createImage(String user,
                                             String author,
                                             String name,
                                             License license,
                                             byte[] data,
                                             ContentType contentType,
                                             String filename) throws JsonProcessingException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", author);
        metadata.put("name", name);
        metadata.put("license", license);

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addTextBody("metadata", mapper.writeValueAsString(metadata), ContentType.APPLICATION_JSON)
                .addBinaryBody("rawdata", data, contentType, filename)
                .build();

        HttpPost request = new HttpPost(MessageFormat.format("/images?token={0}", user));
        request.setEntity(entity);

        return execute(request, new TypeReference<ApiImage>() {
        });
    }

    public ApiResponse<ApiImage> getImage(String user, Integer id) {
        return execute(
                buildGetRequest(MessageFormat.format("/images/id/{0}?token={1}", id, user)),
                new TypeReference<ApiImage>() {
                }
        );
    }

    public ApiResponse<Void> deleteImage(String user, Integer id) {
        return execute(
                buildDeleteRequest(MessageFormat.format("/images/id/{0}?token={1}", id, user)),
                new TypeReference<Void>() {
                }
        );
    }

    public ApiResponse<Iterable<ApiImage>> getAllImages(String user) {
        return execute(
                buildGetRequest(MessageFormat.format("/images?token={0}", user)),
                new TypeReference<Iterable<ApiImage>>() {
                }
        );
    }

    public ApiResponse<ApiImage> getRandomImage(String user) {
        return execute(
                buildGetRequest(MessageFormat.format("/images/random?token={0}", user)),
                new TypeReference<ApiImage>() {
                }
        );
    }

    public ApiResponse<Void> upvoteImage(String user, Integer id) {
        return execute(
                buildPostRequest(MessageFormat.format("/images/id/{0}/vote/up?token={1}", id, user)),
                new TypeReference<Void>() {
                }
        );
    }

    public ApiResponse<Void> downvoteImage(String user, Integer id) {
        return execute(
                buildPostRequest(MessageFormat.format("/images/id/{0}/vote/down?token={1}", id, user)),
                new TypeReference<Void>() {
                }
        );
    }

    public ApiResponse<ApiImage> topRatedImage(String user) {
        return execute(
                buildGetRequest(MessageFormat.format("/images/top?token={0}", user)),
                new TypeReference<ApiImage>() {
                }
        );
    }

    public static class ApiResponse<T> {
        private final ObjectMapper mapper = new ObjectMapper();

        private final int statusCode;
        @Nullable private final String entity;
        private final TypeReference<T> entityClassOnSuccess;

        ApiResponse(int statusCode,
                    @Nullable String entity,
                    TypeReference<T> entityClassOnSuccess) {
            this.statusCode = statusCode;
            this.entity = entity;
            this.entityClassOnSuccess = entityClassOnSuccess;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public T getEntity() {
            try {
                return mapper.readValue(entity, entityClassOnSuccess);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
