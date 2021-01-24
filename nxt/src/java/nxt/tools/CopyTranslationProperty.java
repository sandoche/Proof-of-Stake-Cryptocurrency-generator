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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CopyTranslationProperty {

    private static final String JSON_FILENAME = "translation.json";
    private static final String EN = "en";

    private static Path sourceDir;
    private static String propertyName;
    private static Path targetDir;
    private static int lineNumber;

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Copies one translation key between sets of locales (i.e.: different projects).");
            System.out.println("Modifies translation.json files inside target subdirectories.");
            System.out.println();
            System.out.println("Usage: CopyTranslationProperty <source-locales-dir> <property-name> <target-locales-dir> <line-number>");
            System.exit(1);
        }

        sourceDir = Paths.get(args[0]);
        if (!Files.isDirectory(sourceDir) || !Files.isRegularFile(sourceDir.resolve(EN).resolve(JSON_FILENAME))) {
            System.out.println("First parameter must be the source locales directory.");
            System.exit(1);
        }

        propertyName = args[1];
        if (!readProperty(sourceDir.resolve(EN).resolve(JSON_FILENAME)).isPresent()) {
            System.out.println("Second parameter must be a valid property name (checked against english locale).");
            System.exit(1);
        }

        targetDir = Paths.get(args[2]);
        if (!Files.isDirectory(targetDir) || !Files.isRegularFile(targetDir.resolve(EN).resolve(JSON_FILENAME))) {
            System.out.println("Third parameter must be the target locales directory.");
            System.exit(1);
        }

        lineNumber = Integer.parseInt(args[3]);
        if (lineNumber <= 0 || lineNumber > Files.lines(targetDir.resolve(EN).resolve(JSON_FILENAME)).count()) {
            System.out.println("Last parameter must be the line number from target files where you want the new line.");
            System.exit(1);
        }

        Files.list(sourceDir)
             .filter(Files::isDirectory)
             .forEach(localeDir -> copyProperty(localeDir.resolve(JSON_FILENAME),
                                                targetDir.resolve(localeDir.getFileName()).resolve(JSON_FILENAME)));

    }

    private static Optional<String> readProperty(Path json) {
        if (!Files.isReadable(json)) {
            System.err.println(json + " ignored, as I cannot read it.");
            return Optional.empty();
        }

        try (Stream<String> lines = Files.lines(json)) {
            return lines.filter(line -> line.trim().startsWith('"' + propertyName + "\":")).findAny();
        } catch (IOException e) {
            System.err.println(json + " ignored, as per the exception:");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static void copyProperty(Path sourceJSON, Path targetJSON) {
        if (!Files.isWritable(targetJSON)) {
            System.err.println("Can't write target: " + targetJSON);
            return;
        }

        readProperty(sourceJSON).ifPresent(property -> {
            try {
                List<String> lines = Files.readAllLines(targetJSON);
                if (lineNumber > lines.size()) {
                    System.err.println("Not writing to " + targetJSON + ", not enough lines.");
                } else {
                    lines.add(lineNumber - 1, property);
                    byte[] bytes = String.join("\r\n", lines).getBytes(StandardCharsets.UTF_8);
                    Files.write(targetJSON, bytes);
                }
            } catch (IOException e) {
                System.err.println("Not writing to " + targetJSON + ", as per the exception:");
                e.printStackTrace();
            }
        });
    }
}
