package org.mycore.jspdocportal.diskcache.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple Generator as Demonstrator for DiskLRUCache
 * It simply saves the cache key as value.
 * 
 * @author Robert Stephan
 */

public class SimpleGenerator implements BiConsumer<String, Path> {
    private static Logger LOGGER = LogManager.getLogger(SimpleGenerator.class);

    @Override
    public void accept(String t, Path p) {
        try {
            Files.writeString(p, t);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
