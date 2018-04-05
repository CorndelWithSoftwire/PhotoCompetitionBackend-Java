package org.softwire.training.components;

import io.swagger.v3.oas.annotations.Hidden;
import org.hibernate.validator.constraints.NotEmpty;
import org.softwire.training.api.ApiUser;
import org.softwire.training.auth.Roles;
import org.softwire.training.core.ImageRepository;
import org.softwire.training.core.UserRepository;
import org.softwire.training.rawImageStore.RawImageDao;
import org.softwire.training.metadataStore.UserDao;
import org.softwire.training.models.User;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Produces(MediaType.APPLICATION_JSON)
@Path("/tokens")
@RolesAllowed(Roles.USER_ADMINISTRATOR)
@Hidden
public class UserResource {
    private final UserDao userDao;
    private final RawImageDao rawImageDao;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public UserResource(UserDao userDao,
                        RawImageDao rawImageDao,
                        UserRepository userRepository,
                        ImageRepository imageRepository) {
        this.userDao = userDao;
        this.rawImageDao = rawImageDao;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    /**
     * To be RESTful, this shouldn't be a GET request, but to make it easier for the trainer, I want them to be able
     * to copy-paste it into the address bar.
     */
    @GET
    @Path("/bootstrap")
    public ApiUser create(@NotEmpty @QueryParam("name") String name) throws IOException {
        String uuid = userRepository.createUser(name);
        imageRepository.addBootstrapImages(uuid);
        return new ApiUser(uuid, name);
    }

    @POST
    public ApiUser create(@NotNull @Valid ApiUser apiUser) {
        return apiUser.withUuid(userRepository.createUser(apiUser.getName()));
    }

    @GET
    public List<ApiUser> getAll() {
        return userDao.getAll()
                .stream()
                .map(this::mapModelToApi)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/id/{user}")
    public ApiUser getByUuid(@NotEmpty @PathParam("user") String uuid) {
        return userDao.getByUuid(uuid)
                .map(this::mapModelToApi)
                .orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/id/{user}")
    public void deleteByUuid(@NotEmpty @PathParam("user") String uuid) {
        userRepository.deleteUser(rawImageDao, uuid);
    }

    private ApiUser mapModelToApi(User user) {
        return new ApiUser(user.getUuid(), user.getName());
    }
}
