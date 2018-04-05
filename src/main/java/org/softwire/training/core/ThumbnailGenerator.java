package org.softwire.training.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ThumbnailGenerator {

    private static final int IMAGE_THUMBNAIL_MAX_WIDTH_HEIGHT = 200;
    private static final String THUMBNAIL_FORMAT = "png";

    public byte[] generate(byte[] input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        generate(new ByteArrayInputStream(input), outputStream);
        return outputStream.toByteArray();
    }

    private void generate(InputStream input, OutputStream outputStream) throws IOException {
        // We don't actually check that the mimetype which the client claims to have provided is the one used to parse
        // the image here.  So someone could conceivably pass a BMP and claim it is of type PNG via the mimetype, and
        // it will succeed in generating a thumbnail.  Hopefully they won't do this..
        BufferedImage source = ImageIO.read(input);

        if (source == null) {
            throw new IOException("Unable to read image file");
        }

        int width = source.getWidth();
        int height = source.getHeight();

        int targetWidth;
        int targetHeight;
        if (width < height) {
            targetHeight = IMAGE_THUMBNAIL_MAX_WIDTH_HEIGHT;
            double ratio = ((double) width) / height;
            targetWidth = (int) Math.round(ratio * IMAGE_THUMBNAIL_MAX_WIDTH_HEIGHT);
        } else {
            targetWidth = IMAGE_THUMBNAIL_MAX_WIDTH_HEIGHT;
            double ratio = ((double) height) / width;
            targetHeight = (int) Math.round(ratio * IMAGE_THUMBNAIL_MAX_WIDTH_HEIGHT);
        }

        BufferedImage target = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_RGB
        );
        Image scaledInstance = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        if (!target.createGraphics().drawImage(scaledInstance, 0, 0, null)) {
            throw new IllegalStateException("drawImage claims that image pixels are still changing - don't expect" +
                    "this to ever happen");
        }

        ImageIO.write(target, THUMBNAIL_FORMAT, outputStream);
    }
}
