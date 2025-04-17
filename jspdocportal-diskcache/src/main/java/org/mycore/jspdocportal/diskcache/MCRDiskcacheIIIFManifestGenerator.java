package org.mycore.jspdocportal.diskcache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.iiif.presentation.MCRIIIFPresentationManifestQuickAccess;
import org.mycore.iiif.presentation.MCRIIIFPresentationUtil;
import org.mycore.iiif.presentation.impl.MCRIIIFPresentationImpl;
import org.mycore.iiif.presentation.model.basic.MCRIIIFManifest;
import org.mycore.jspdocportal.diskcache.generator.SimpleGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MCRDiskcacheIIIFManifestGenerator extends SimpleGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void accept(String id, Path p) {
        long startTime = new Date().getTime();
        MCRIIIFManifest manifest = MCRIIIFPresentationImpl.obtainInstance(null).getManifest(id);
        long endTime = new Date().getTime();
        long timeNeeded = endTime - startTime;
        LOGGER.info("IIIF Manifest / id:{} generation needed: {}ms", id, timeNeeded);

        MCRIIIFPresentationManifestQuickAccess quickAccess = new MCRIIIFPresentationManifestQuickAccess(manifest);
        MCRIIIFPresentationUtil.correctIDs(manifest, "", manifest.getId());

        try {
            String manifestAsJSON = getGson().toJson(quickAccess.getManifest());
            Files.deleteIfExists(p);
            Files.writeString(p, manifestAsJSON);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    protected Gson getGson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

}
