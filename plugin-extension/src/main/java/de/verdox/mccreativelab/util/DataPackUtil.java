package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.util.io.ZipUtil;

import java.io.File;
import java.nio.file.Path;

public class DataPackUtil {
    public static File installDataPack(String levelName, Asset<?> dataPackAsset){
        return ZipUtil.extractFilesFromZipFileResource(dataPackAsset.assetInputStream(), String.valueOf(Path.of(levelName+"/datapacks/")));
    }
}
