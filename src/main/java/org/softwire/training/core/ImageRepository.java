package org.softwire.training.core;

import com.google.common.io.ByteStreams;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softwire.training.api.ApiImage;
import org.softwire.training.metadataStore.ImageMetadataDao;
import org.softwire.training.models.Image;
import org.softwire.training.rawImageStore.ImageUrlBuilder;
import org.softwire.training.rawImageStore.RawImageDao;

import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

public class ImageRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRepository.class);

    private static final int DEFAULT_SCORE = 0;

    private final RawImageDao rawImageDao;
    private final ImageMetadataDao imageMetadataDao;
    private final ImageUrlBuilder imageUrlBuilder;
    private final ThumbnailGenerator thumbnailGenerator;

    public ImageRepository(RawImageDao rawImageDao,
                           ImageMetadataDao imageMetadataDao,
                           ImageUrlBuilder imageUrlBuilder,
                           ThumbnailGenerator thumbnailGenerator) {
        this.rawImageDao = rawImageDao;
        this.imageMetadataDao = imageMetadataDao;
        this.imageUrlBuilder = imageUrlBuilder;
        this.thumbnailGenerator = thumbnailGenerator;
    }

    public ApiImage storeImage(String user, ApiImage apiImage, String mimeType, byte[] imageBytes) throws IOException {
        String filename = UUID.randomUUID().toString();
        LOGGER.debug("Storing image: {}, {}, {}", user, apiImage.getName(), filename);
        rawImageDao.put(filename, new ByteArrayInputStream(imageBytes), mimeType, imageBytes.length);

        int imageId;
        byte[] thumbnail;
        try {
            thumbnail = thumbnailGenerator.generate(imageBytes);
            imageId = storeImageMetadata(user, apiImage, filename, thumbnail);
        } catch (Exception e) {
            try {
                rawImageDao.delete(filename);
            } catch (Exception inner) {
                e.addSuppressed(inner);
            }
            throw e;
        }
        return apiImage
                .withId(imageId)
                .withUrl(imageUrlBuilder.build(filename))
                .withScore(DEFAULT_SCORE)
                .withThumbnail(new String(Base64.getEncoder().encode(thumbnail)));
    }

    public void deleteImage(String user, int id) {
        imageMetadataDao.get(user, id).ifPresent(image -> {
            LOGGER.debug("Deleting image: {}, {}, {}", user, id, image.getFilename());
            imageMetadataDao.delete(user, id);
            rawImageDao.delete(image.getFilename());
        });
    }

    private int storeImageMetadata(String user,
                                   ApiImage apiImage,
                                   String filename,
                                   byte[] thumbnail) {
        Image image = new Image(
                null,
                user,
                apiImage.getAuthor(),
                apiImage.getName(),
                filename,
                apiImage.getLicense(),
                DEFAULT_SCORE,
                thumbnail);
        int imageId;
        try {
            imageId = imageMetadataDao.insert(image);
        } catch (UnableToExecuteStatementException e) {
            // Needs to work with MySQL (for live) and h2 (for testing)
            if (e.getMessage().toLowerCase().contains("fk_images_users")) {
                // No such user
                throw new NotFoundException();
            }
            throw e;
        }
        return imageId;
    }

    public void addBootstrapImages(String uuid) throws IOException {
        for (BootstrapImage image : BootstrapImages.ALL) {
            try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(image.getResourceName())) {
                if (resourceAsStream == null) {
                    throw new IOException("Missing bootstrap resource: " + image.toString());
                }
                storeImage(
                        uuid,
                        image.getApiImage(),
                        image.getMimeType(),
                        ByteStreams.toByteArray(resourceAsStream)
                );
            }
        }
    }
}
