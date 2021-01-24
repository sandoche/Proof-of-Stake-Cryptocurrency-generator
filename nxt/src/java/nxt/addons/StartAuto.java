/*
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.addons;

import nxt.Nxt;
import nxt.util.ThreadPool;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public abstract class StartAuto implements AddOn {

    public final void init() {
        String filename = Nxt.getStringProperty(getFilenameProperty());
        if (filename != null) {
            ThreadPool.runAfterStart(() -> {
                try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                    processFile(reader);
                } catch (ParseException | IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        }
    }

    protected abstract String getFilenameProperty();

    protected abstract void processFile(BufferedReader reader) throws IOException, ParseException;

}

