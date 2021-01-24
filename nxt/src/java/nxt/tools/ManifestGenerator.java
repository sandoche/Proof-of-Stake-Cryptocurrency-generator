/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.tools;

import nxt.Nxt;
import nxt.env.service.NxtService_ServiceManagement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestGenerator {

    public static final String JAVAFX_SDK_RUNTIME_LIB = "javafx-sdk.runtime";
    public static final String JAVAFX_LIB = "../" + JAVAFX_SDK_RUNTIME_LIB + "/lib";
    public static final String JAVAFX_SDK_LIB = "javafx-sdk";
    public static final String LIB = "./lib";
    public static final String CONF = "conf/";
    public static final String SOURCE_FILE = "src.zip";

    public static final String NXT_MANIFEST = "./resource/nxt.manifest.mf";
    public static final String NXTSERVICE_MANIFEST = "./resource/nxtservice.manifest.mf";

    public static void main(String[] args) {
        ManifestGenerator manifestGenerator = new ManifestGenerator();
        manifestGenerator.generate(NXT_MANIFEST, Nxt.class.getCanonicalName(), LIB, JAVAFX_LIB);
        String serviceClassName = NxtService_ServiceManagement.class.getCanonicalName();
        serviceClassName = serviceClassName.substring(0, serviceClassName.length() - "_ServiceManagement".length());
        manifestGenerator.generate(NXTSERVICE_MANIFEST, serviceClassName, LIB);
    }

    private void generate(String fileName, String className, String... directories) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, className);
        StringBuilder classpath = new StringBuilder();

        for (String directory : directories) {
            DirListing dirListing = new DirListing();
            try {
                Files.walkFileTree(Paths.get(directory), EnumSet.noneOf(FileVisitOption.class), 2, dirListing);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            classpath.append(dirListing.getFileList().toString());
        }
        classpath.append(CONF);
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classpath.toString());
        try {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            manifest.write(Files.newOutputStream(path, StandardOpenOption.CREATE));
            System.out.println("Manifest file " + path.toAbsolutePath() + " generated");

            // Print the file content to assist debugging
            byte[] encoded = Files.readAllBytes(path);
            System.out.println(new String(encoded, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class DirListing extends SimpleFileVisitor<Path> {

        private final StringBuilder fileList = new StringBuilder();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            if (file.getFileName().toString().equals(SOURCE_FILE)) {
                return FileVisitResult.CONTINUE;
            }
            for (int i = 1; i < file.getNameCount() - 1; i++) {
                String folderName = file.getName(i).toString().replace(JAVAFX_SDK_RUNTIME_LIB, JAVAFX_SDK_LIB);
                fileList.append(folderName).append('/');
            }
            fileList.append(file.getFileName()).append(' ');
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return FileVisitResult.CONTINUE;
        }

        public StringBuilder getFileList() {
            return fileList;
        }
    }
}
