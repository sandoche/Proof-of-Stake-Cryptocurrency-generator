/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class CompareTraceFiles {

    public static void main(String[] args) {
        String testFile = args.length > 0 ? args[0] : "nxt-trace.csv";
        String defaultFile = args.length > 1 ? args[1] : "nxt-trace-default.csv";
        try (BufferedReader defaultReader = new BufferedReader(new FileReader(defaultFile));
             BufferedReader testReader = new BufferedReader(new FileReader(testFile))) {
            System.out.println(defaultReader.readLine());
            testReader.readLine();
            String testLine = testReader.readLine();
            if (testLine == null) {
                System.out.println("Empty trace file, nothing to compare");
                return;
            }
            int height = parseHeight(testLine);
            String defaultLine;
            while ((defaultLine = defaultReader.readLine()) != null) {
                if (parseHeight(defaultLine) >= height) {
                    break;
                }
            }
            if (defaultLine == null) {
                System.out.println("End of default trace file, can't compare further");
                return;
            }
            int endHeight = height;
            assertEquals(defaultLine, testLine);
            while ((testLine = testReader.readLine()) != null) {
                defaultLine = defaultReader.readLine();
                if (defaultLine == null) {
                    System.out.println("End of default trace file, can't compare further");
                    return;
                }
                endHeight = parseHeight(testLine);
                assertEquals(defaultLine, testLine);
            }
            if ((defaultLine = defaultReader.readLine()) != null) {
                if (parseHeight(defaultLine) <= endHeight) {
                    System.out.println("default height: " + parseHeight(defaultLine) + " end height: " + endHeight);
                }
            }
            System.out.println("Comparison with default trace file done from height " + height + " to " + endHeight);
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private static int parseHeight(String line) {
        return Integer.parseInt(line.substring(1, line.indexOf('\t') - 1));
    }

    private static void assertEquals(String defaultLine, String testLine) {
        if (!defaultLine.equals(testLine)) {
            System.out.println("Lines don't match:");
            System.out.println("default:\n" + defaultLine);
            System.out.println("test:\n" + testLine);
        }
    }

}
