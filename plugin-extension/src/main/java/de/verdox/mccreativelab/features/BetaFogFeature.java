package de.verdox.mccreativelab.features;

import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.util.io.ZipUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * This feature uses a resource pack made by Shock_Micro
 * Credits: <a href="https://www.curseforge.com/minecraft/texture-packs/foggier-fog">...</a>
 */
public class BetaFogFeature extends Feature {
    private File folder;

    @Override
    protected void onEnable() {
        ZipUtil.extractFilesFromZipFileResource("/features/BetaFog.zip", CustomResourcePack.resourcePacksFolder.toPath()
                                                                                                               .toString());
        folder = new File(CustomResourcePack.resourcePacksFolder.toPath().toString() + "/BetaFog");
    }

    @Override
    public void onDisable() {
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
