package org.mycore.jspdocportal.ir.tileserver;

import java.nio.file.Path;

public interface MCRTileFileProvider {
    Path getTileFile(String derivate, String image);
}
