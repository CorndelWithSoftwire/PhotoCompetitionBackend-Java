package org.softwire.training.components;

import com.google.common.io.ByteStreams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softwire.training.api.ApiImage;
import org.softwire.training.auth.Roles;
import org.softwire.training.core.ImageRepository;
import org.softwire.training.metadataStore.ImageMetadataDao;
import org.softwire.training.models.Image;
import org.softwire.training.rawImageStore.ImageUrlBuilder;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Path("/images")
@Produces(MediaType.APPLICATION_JSON)
public class ImageResource {

    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;  // 10 MB
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");
    private static final String UNSUPPORTED_CONTENT_TYPE_ERROR = String.format(
            "Unsupported content type: %%s, supported content types are: %s",
            StringUtils.join(ALLOWED_IMAGE_CONTENT_TYPES, ", ")
    );
    private static final Logger LOG = LoggerFactory.getLogger(ImageResource.class);

    private final ImageMetadataDao imageMetadataDao;
    private final ImageUrlBuilder imageUrlBuilder;
    private final ImageRepository imageStorage;

    public ImageResource(ImageMetadataDao imageMetadataDao,
                         ImageUrlBuilder imageUrlBuilder,
                         ImageRepository imageStorage) {
        this.imageMetadataDao = imageMetadataDao;
        this.imageUrlBuilder = imageUrlBuilder;
        this.imageStorage = imageStorage;
    }

    @Operation(
            summary = "Get all images",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )

            })
    @GET
    public List<ApiImage> getAll(@NotEmpty @QueryParam("token") String user) {
        return imageMetadataDao.get(user)
                .stream()
                .map(this::mapModelToApi)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Get random image",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found, or user has no images"
                    )

            })
    @GET
    @Path("/random")
    public ApiImage getRandomImage(@NotEmpty @QueryParam("token") String user) {
        return imageMetadataDao
                .getRandom(user)
                .map(this::mapModelToApi)
                .orElseThrow(NotFoundException::new);
    }

    @Operation(
            summary = "Get top rated",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Image or user not found"
                    )

            })
    @GET
    @Path("/top")
    public ApiImage getTop(@NotEmpty @QueryParam("token") String user) {
        return imageMetadataDao
                .getTopScoring(user)
                .map(this::mapModelToApi)
                .orElseThrow(NotFoundException::new);
    }

    @Operation(
            summary = "Get image",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Image or user not found"
                    )

            })
    @GET
    @Path("/id/{id}")
    public ApiImage get(@NotEmpty @QueryParam("token") String user,
                        @NotNull @PathParam("id") Integer id) {
        return imageMetadataDao
                .get(user, id)
                .map(this::mapModelToApi)
                .orElseThrow(NotFoundException::new);
    }

    @Operation(
            summary = "Delete image",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "On success"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Image or user not found"
                    )

            })
    @DELETE
    @Path("/id/{id}")
    @RolesAllowed(Roles.IMAGE_ADMINISTRATOR)
    public void delete(@NotEmpty @QueryParam("token") String user,
                       @NotNull @PathParam("id") Integer id) {
        imageStorage.deleteImage(user, id);
    }

    @Operation(
            summary = "Upvote image",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Image or user not found"
                    )

            })
    @POST
    @Path("/id/{id}/vote/up")
    public void voteUp(@NotEmpty @QueryParam("token") String user,
                       @NotNull @PathParam("id") Integer id) {
        if (imageMetadataDao.incrementScore(user, id) == 0) {
            throw new NotFoundException();
        }
    }

    @Operation(
            summary = "Downvote image",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Image or user not found"
                    )

            })
    @POST
    @Path("/id/{id}/vote/down")
    public void voteDown(@NotEmpty @QueryParam("token") String user,
                         @NotNull @PathParam("id") Integer id) {
        if (imageMetadataDao.decrementScore(user, id) == 0) {
            throw new NotFoundException();
        }
    }

    @Operation(
            summary = "Upload image",
            requestBody = @RequestBody(
                    description = "Image and metadata.  To contain two parts.  One part has name 'metadata', " +
                            "content type 'application/json', and is a JSON encoded ApiImage.class.  " +
                            "The second part has name 'rawdata', content type image/jpeg or image/png, and is the " +
                            "raw image data.",
                    required = true,
                    content = {
                            @Content(mediaType = "multipart/form-data")
                    }
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "On success",
                            content = @Content(schema = @Schema(implementation = ApiImage.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid arguments"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    ),
                    @ApiResponse(
                            responseCode = "413",
                            description = "Image too large"
                    )
            })
    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public ApiImage insert(@NotEmpty @QueryParam("token") String user,
                           @NotNull @Valid @FormDataParam("metadata") ApiImage apiImage,
                           @FormDataParam("rawdata") InputStream imageStream,
                           @FormDataParam("rawdata") FormDataContentDisposition contentDispositionHeader)
            throws ClientVisibleException, IOException {
        String mimeType = guessImageMimeType(contentDispositionHeader);
        byte[] imageBytes = readImageData(imageStream);
        return imageStorage.storeImage(user, apiImage, mimeType, imageBytes);
    }

    private String guessImageMimeType(FormDataContentDisposition contentDispositionHeader)
            throws ClientVisibleException {
        String mimeTypeGuess = URLConnection.guessContentTypeFromName(contentDispositionHeader.getFileName());
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(mimeTypeGuess)) {
            String unsupportedMimeType = mimeTypeGuess == null ? "unknown" : mimeTypeGuess;
            throw ClientVisibleException.construct(
                    Response.Status.BAD_REQUEST,
                    String.format(UNSUPPORTED_CONTENT_TYPE_ERROR, unsupportedMimeType)
            );
        }
        return mimeTypeGuess;
    }

    /**
     * Read everything into memory because I'm too lazy to work out how to stream into S3.
     */
    private byte[] readImageData(InputStream imageStream) throws ClientVisibleException {
        byte[] imageBytes;
        try {
            imageBytes = ByteStreams.toByteArray(ByteStreams.limit(imageStream, MAX_IMAGE_SIZE));
            if (imageStream.read() != -1) {
                throw ClientVisibleException.construct(
                        Response.Status.REQUEST_ENTITY_TOO_LARGE,
                        String.format("Image too large, max size is: %s", MAX_IMAGE_SIZE)
                );
            }
        } catch (IOException e) {
            LOG.warn("Error reading input stream", e);
            throw new BadRequestException();
        }
        return imageBytes;
    }

    private ApiImage mapModelToApi(Image image) {
        return new ApiImage(
                image.getId(),
                imageUrlBuilder.build(image.getFilename()),
                image.getScore(),
                image.getAuthor(),
                image.getName(),
                image.getLicense(),
                new String(Base64.getEncoder().encode(image.getThumbnail()))
        );
    }
}
