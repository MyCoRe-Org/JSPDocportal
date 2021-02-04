package org.mycore.jspdocportal.ir.tileserver;

import java.nio.file.Path;

public interface MCRTileFileProvider {
    public Path getTileFile(String derivate, String image);
}