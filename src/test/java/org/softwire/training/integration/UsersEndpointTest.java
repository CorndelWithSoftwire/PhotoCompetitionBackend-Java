package org.softwire.training.integration;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.softwire.training.api.ApiUser;
import org.softwire.training.integration.helpers.Client;
import org.softwire.training.integration.helpers.IntegrationTestSupport;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.softwire.training.integration.helpers.Matchers.hasStatusCode;
import static org.softwire.training.integration.helpers.Matchers.hasStatusCodeWithEntity;


class UsersEndpointTest {
    private final static IntegrationTestSupport SUPPORT = new IntegrationTestSupport();
    private static Client client;

    @BeforeAll
    static void beforeAll() throws Exception {
        SUPPORT.before();
        client = SUPPORT.getClient();
    }

    @AfterAll
    static void afterAll() throws Exception {
        SUPPORT.after();
    }

    @Test
    void createUserReturns200() {
        String name = "The King of Carrot Flowers";

        Client.ApiResponse<ApiUser> response = client.createUser(name);

        assertThat(response, hasStatusCode(200));
        assertThat(response.getEntity().getName(), equalTo(name));
    }

    @Test
    void createUserReturnsUnprocessableEntityOnEmptyName() {
        assertThat(client.createUser(""), hasStatusCode(422));
    }

    @Test
    void getUserReturnsCorrectEntity() {
        ApiUser user = client.createUser("The King of Carrot Flowers").getEntity();

        assertThat(client.getUser(user.getUuid()), hasStatusCodeWithEntity(200, equalTo(user)));
    }

    @Test
    void roundtripNonAsciiStrings() {
        String weirdChar = new String(new int[]{0x1f419}, 0, 1);
        ApiUser user = client.createUser(weirdChar).getEntity();

        assertThat(user.getName(), equalTo(weirdChar));
        assertThat(client.getUser(user.getUuid()), hasStatusCodeWithEntity(200, equalTo(user)));
    }

    @Test
    void getNonexistentUserReturns404() {
        assertThat(client.getUser(UUID.randomUUID().toString()), hasStatusCode(404));
    }

    @Test
    void getBadUuidReturns404() {
        assertThat(client.getUser("1"), hasStatusCode(404));
    }

    @Test
    void deleteUserActuallyDeletesUser() {
        String uuid = client.createUser("The King of Carrot Flowers").getEntity().getUuid();
        assertThat(client.deleteUser(uuid), hasStatusCode(204));
        assertThat(client.getUser(uuid), hasStatusCode(404));
    }

    @Test
    void getAllReturnsCreatedUsers() {
        ApiUser user = client.createUser("The aeroplane over the sea").getEntity();

        assertThat(client.getAllUsers(), hasStatusCodeWithEntity(200, Matchers.hasItem(user)));
    }

    @Test
    void bootstrapReturns200() {
        assertThat(client.bootstrapUser("bootstrappedUser"), hasStatusCode(200));
    }

    @Test
    void bootstrapCreatesAtLeastOneImage() {
        ApiUser bootstappedUser = client.bootstrapUser("bootstappedUser").getEntity();

        assertThat(client.getRandomImage(bootstappedUser.getUuid()), hasStatusCode(200));
    }
}
