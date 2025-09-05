package com.example.petapp.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ThumbnailUtil {

    /**
     * Create a thumbnail as JPEG bytes. maxDim = max(width,height)
     */
    public static byte[] createThumbnail(byte[] originalBytes, int maxDim) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(originalBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Could not read image bytes for thumbnail.");
            }

            int origW = img.getWidth();
            int origH = img.getHeight();
            if (origW <= maxDim && origH <= maxDim) {
                // already small — re-encode as jpg
                ImageIO.write(img, "jpg", out);
                return out.toByteArray();
            }

            float scale = Math.min((float) maxDim / origW, (float) maxDim / origH);
            int newW = Math.max(1, Math.round(origW * scale));
            int newH = Math.max(1, Math.round(origH * scale));

            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            BufferedImage buffered = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = buffered.createGraphics();
            g2d.setComposite(AlphaComposite.Src);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();

            ImageIO.write(buffered, "jpg", out);
            return out.toByteArray();
        }
    }
}

