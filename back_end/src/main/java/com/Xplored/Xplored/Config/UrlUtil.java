package com.Xplored.Xplored.Config;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Builds absolute photo URLs from the stored relative paths.
 * Paths that already start with "http" are returned unchanged.
 */
public final class UrlUtil {

    private UrlUtil() {}

    public static String toAbsolute(String path) {
        if (path == null || path.isEmpty()) return path;
        if (path.startsWith("http")) return path;
        String withSlash = path.startsWith("/") ? path : "/" + path;
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(withSlash).toUriString();
    }
}
