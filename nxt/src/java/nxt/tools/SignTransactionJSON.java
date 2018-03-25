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

import nxt.Nxt;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public final class SignTransactionJSON {

    public static void main(String[] args) {
        try {
            Logger.setLevel(Logger.Level.ERROR);
            if (args.length == 0 || args.length > 2) {
                System.out.println("Usage: SignTransactionJSON <unsigned transaction json file> <signed transaction json file>");
                System.exit(1);
            }
            File unsigned = new File(args[0]);
            if (!unsigned.exists()) {
                System.out.println("File not found: " + unsigned.getAbsolutePath());
                System.exit(1);
            }
            File signed;
            if (args.length == 2) {
                signed = new File(args[1]);
            } else if (unsigned.getName().startsWith("unsigned.")) {
                signed = new File(unsigned.getParentFile(), unsigned.getName().substring(2));
            } else {
                signed = new File(unsigned.getParentFile(), "signed." + unsigned.getName());
            }
            if (signed.exists()) {
                System.out.println("File already exists: " + signed.getAbsolutePath());
                System.exit(1);
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(unsigned));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(signed))) {
                JSONObject json = (JSONObject) JSONValue.parseWithException(reader);
                byte[] publicKeyHash = Crypto.sha256().digest(Convert.parseHexString((String) json.get("senderPublicKey")));
                String senderRS = Convert.rsAccount(Convert.fullHashToId(publicKeyHash));
                String secretPhrase;
                Console console = System.console();
                if (console == null) {
                    try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))) {
                        secretPhrase = inputReader.readLine();
                    }
                } else {
                    secretPhrase = new String(console.readPassword("Secret phrase for account " + senderRS + ": "));
                }
                Transaction.Builder builder = Nxt.newTransactionBuilder(json);
                Transaction transaction = builder.build(secretPhrase);
                writer.write(transaction.getJSONObject().toJSONString());
                writer.newLine();
                System.out.println("Signed transaction JSON saved as: " + signed.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
