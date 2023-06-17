package com.bearlycattable.bait.utility;

import java.io.File;
import java.nio.file.Paths;

public class PathUtils {

    public static synchronized boolean isAccessibleToReadWrite(String pathToFile) {
        File file = Paths.get(pathToFile).toFile();
        return file.canRead() && file.canWrite();
    }

    public static synchronized boolean isAccessibleToRead(String pathToFile) {
        File file = Paths.get(pathToFile).toFile();
        return file.canRead();
    }

    public static synchronized boolean isAccessibleToWrite(String pathToFile) {
        File file = Paths.get(pathToFile).toFile();
        return file.canWrite();
    }
}
