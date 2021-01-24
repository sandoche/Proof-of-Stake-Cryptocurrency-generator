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

package nxt.http;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class GetPlugins extends APIServlet.APIRequestHandler {

    static final GetPlugins instance = new GetPlugins();

    private GetPlugins() {
        super(new APITag[] {APITag.INFO});
    }

    private static final Path PLUGINS_HOME = Paths.get("./html/www/plugins");

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        JSONObject response = new JSONObject();
        if (!Files.isReadable(PLUGINS_HOME)) {
            return JSONResponses.fileNotFound(PLUGINS_HOME.toString());
        }
        PluginDirListing pluginDirListing = new PluginDirListing();
        try {
            Files.walkFileTree(PLUGINS_HOME, EnumSet.noneOf(FileVisitOption.class), 2, pluginDirListing);
        } catch (IOException e) {
            return JSONResponses.fileNotFound(e.getMessage());
        }
        JSONArray plugins = new JSONArray();
        pluginDirListing.getDirectories().forEach(dir -> plugins.add(Paths.get(dir.toString()).getFileName().toString()));
        response.put("plugins", plugins);
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    private static class PluginDirListing extends SimpleFileVisitor<Path> {

        private final List<Path> directories = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) {
            if (!PLUGINS_HOME.equals(dir)) {
                directories.add(dir);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getDirectories() {
            return directories;
        }
    }

}
