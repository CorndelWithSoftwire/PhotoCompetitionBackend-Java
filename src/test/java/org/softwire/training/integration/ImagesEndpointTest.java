package org.softwire.training.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.softwire.training.api.ApiImage;
import org.softwire.training.api.ApiUser;
import org.softwire.training.integration.helpers.Client;
import org.softwire.training.integration.helpers.IntegrationTestSupport;
import org.softwire.training.models.License;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.softwire.training.integration.helpers.Matchers.hasStatusCode;
import static org.softwire.training.integration.helpers.Matchers.hasStatusCodeWithEntity;

class ImagesEndpointTest {
    private static final IntegrationTestSupport SUPPORT = new IntegrationTestSupport();
    private static Client client;

    private ApiUser user;

    @BeforeAll
    static void beforeAll() throws Exception {
        SUPPORT.before();
        client = SUPPORT.getClient();
    }

    @AfterAll
    static void afterAll() throws Exception {
        SUPPORT.after();
    }

    @BeforeEach
    void beforeEach() {
        user = client.createUser("TestUser").getEntity();
    }

    @Test
    void createImageReturnsSensibleData() throws JsonProcessingException {
        String name = "image name";

        Client.ApiResponse<ApiImage> response = client.createImage(user.getUuid(), "author name", name, License.CC_BY_SA);

        assertThat(response, hasStatusCode(200));
        assertThat(response.getEntity().getName(), equalTo(name));
    }

    @Test
    void createImageCreateReasonablySmallThumbnail() throws JsonProcessingException {
        String name = "image name";

        Client.ApiResponse<ApiImage> response = client.createImage(user.getUuid(), "author name", name, License.CC_BY_SA);

        assertThat(
                response.getEntity().getThumbnail().length(),
                both(greaterThan(1024)).and(lessThan(1024 * 1024))
        );
    }

    @Test
    void createImageStoresCorrectData() throws JsonProcessingException {
        ApiImage image = client
                .createImage(
                        user.getUuid(),
                        "author name",
                        "image name",
                        License.CC_BY_SA,
                        Client.IMAGE_CONTENT,
                        ContentType.IMAGE_JPEG,
                        "cat.jpeg"
                )
                .getEntity();

        assertThat(SUPPORT.getDataFromRawImageDao(image.getUrl()), equalTo(Client.IMAGE_CONTENT));
    }

    @Test
    void returns400OnWrongContentTypeInFilename() throws JsonProcessingException {
        assertThat(client
                .createImage(
                        user.getUuid(),
                        "author name",
                        "image name",
                        License.CC_BY_SA,
                        Client.IMAGE_CONTENT,
                        ContentType.IMAGE_JPEG,
                        "cat.gif"
                ), hasStatusCode(400));
    }

    @Test
    void returns400EmptyAuthorName() throws JsonProcessingException {
        assertThat(client
                .createImage(
                        user.getUuid(),
                        "",
                        "image name",
                        License.CC_BY_SA,
                        Client.IMAGE_CONTENT,
                        ContentType.IMAGE_JPEG,
                        "cat.jpeg"
                ), hasStatusCode(422));
    }


    @Test
    void returns400OnVeryLongAuthorName() throws JsonProcessingException {
        assertThat(client
                .createImage(
                        user.getUuid(),
                        Strings.repeat("X", 256),
                        "image name",
                        License.CC_BY_SA,
                        Client.IMAGE_CONTENT,
                        ContentType.IMAGE_JPEG,
                        "cat.jpeg"
                ), hasStatusCode(422));
    }


    @Test
    void ignoresContentType() throws JsonProcessingException {
        assertThat(client.createImage(
                user.getUuid(),
                "author name",
                "image name",
                License.CC_BY_SA,
                Client.IMAGE_CONTENT,
                ContentType.APPLICATION_JSON,
                "cat.jpeg"
        ), hasStatusCode(200));
    }

    @Test
    void returns413IfImageTooLarge() throws JsonProcessingException {
        // Should fail if bigger than 1MB
        byte[] content = new byte[(10 * 1024 * 1024) + 1];

        assertThat(client.createImage(
                user.getUuid(),
                "author name",
                "image name",
                License.CC_BY_SA,
                content,
                ContentType.IMAGE_BMP,
                "cat.jpeg"
        ), hasStatusCode(413));
    }

    @Test
    void getImageReturnsCorrectImage() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();

        assertThat(client.getImage(user.getUuid(), image.getId()), hasStatusCodeWithEntity(200, equalTo(image)));
    }

    @Test
    void createImageFailsForNonExistentUser() throws JsonProcessingException {
        assertThat(
                client.createImage(UUID.randomUUID().toString(), "author name", "image name", License.CC_BY_SA),
                hasStatusCode(404)
        );
    }

    @Test
    void getReturns404ForAnotherUsersImage() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();

        ApiUser anotherUser = client.createUser("another user").getEntity();

        assertThat(client.getImage(anotherUser.getUuid(), image.getId()), hasStatusCode(404));
    }

    @Test
    void deleteImage() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();

        assertThat(client.deleteImage(user.getUuid(), image.getId()), hasStatusCode(204));
        assertThat(client.getImage(user.getUuid(), image.getId()), hasStatusCode(404));
    }

    @Test
    void getAllReturnsCreatedImages() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();

        assertThat(client.getAllImages(user.getUuid()), hasStatusCodeWithEntity(200, contains(image)));
    }

    @Test
    void randomReturnsImage() throws JsonProcessingException {
        client.createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA);

        assertThat(client.getRandomImage(user.getUuid()), hasStatusCodeWithEntity(200, notNullValue()));
    }

    @Test
    void voteUpIncrementsScore() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();

        assertThat(client.upvoteImage(user.getUuid(), image.getId()), hasStatusCode(204));

        assertThat(client.getImage(user.getUuid(), image.getId()).getEntity().getScore(), equalTo(1));
    }

    @Test
    void voteUpReturns404IfNoSuchImage() {
        assertThat(client.upvoteImage(user.getUuid(), 1), hasStatusCode(404));
    }

    @Test
    void voteUpReturns404IfNoSuchUser() {
        assertThat(client.upvoteImage(UUID.randomUUID().toString(), 1), hasStatusCode(404));
    }

    @Test
    void voteDownDecrementsScore() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();
        client.upvoteImage(user.getUuid(), image.getId());
        client.upvoteImage(user.getUuid(), image.getId());

        assertThat(client.downvoteImage(user.getUuid(), image.getId()), hasStatusCode(204));

        assertThat(client.getImage(user.getUuid(), image.getId()).getEntity().getScore(), equalTo(1));
    }

    @Test
    void scoreCannotGoNegative() throws JsonProcessingException {
        ApiImage image = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();

        assertThat(client.downvoteImage(user.getUuid(), image.getId()), hasStatusCode(204));

        assertThat(client.getImage(user.getUuid(), image.getId()).getEntity().getScore(), equalTo(0));
    }

    @Test
    void voteDownReturns404IfNoSuchImage() {
        assertThat(client.downvoteImage(user.getUuid(), 1), hasStatusCode(404));
    }

    @Test
    void voteDownReturns404IfNoSuchUser() {
        assertThat(client.downvoteImage(UUID.randomUUID().toString(), 1), hasStatusCode(404));
    }

    @Test
    void testTopRated() throws JsonProcessingException {
        ApiImage image1 = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();
        ApiImage image2 = client
                .createImage(user.getUuid(), "author name", "image name", License.CC_BY_SA)
                .getEntity();
        client.upvoteImage(user.getUuid(), image1.getId());
        client.upvoteImage(user.getUuid(), image1.getId());
        client.upvoteImage(user.getUuid(), image2.getId());

        assertThat(
                client.topRatedImage(user.getUuid()),
                hasStatusCodeWithEntity(200, equalTo(image1.withScore(2)))
        );
    }
}
