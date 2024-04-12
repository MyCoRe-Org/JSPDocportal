package org.mycore.jspdocportal.diskcache.servlet;

import java.nio.file.Path;

public record FileServletData(Path file, String contentType, String fileName) {

}
