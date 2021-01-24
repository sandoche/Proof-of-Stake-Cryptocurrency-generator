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

package nxt.util;

import nxt.env.RuntimeEnvironment;

import java.io.InputStream;
import java.net.URL;

public class ResourceLookup {
    public final static boolean USE_SYSTEM_CLASS_LOADER = !RuntimeEnvironment.isAndroidRuntime();

    public static InputStream getSystemResourceAsStream(String resourceName) {
        if (USE_SYSTEM_CLASS_LOADER) {
            return ClassLoader.getSystemResourceAsStream(resourceName);
        } else {
            return ResourceLookup.class.getClassLoader().getResourceAsStream(resourceName);
        }
    }

    public static URL getSystemResource(String resourceName) {
        if (USE_SYSTEM_CLASS_LOADER) {
            return ClassLoader.getSystemResource(resourceName);
        } else {
            return ResourceLookup.class.getClassLoader().getResource(resourceName);
        }
    }
}
