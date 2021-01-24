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

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Delegate json array operations to json simple and wrap it with convenience methods
 * This class does not really keep a list, but it implements a list in order to delegate iteration to the underlying JSONArray
 * in order to support streaming into String.
 */
public class JA extends AbstractList {

    private final JSONArray ja;

    public JA() {
        this.ja = new JSONArray();
    }

    public JA(JSONArray ja) {
        if (ja == null) {
            throw new IllegalArgumentException("Attempt to initialize JA with null JSONArray");
        }
        this.ja = ja;
    }

    public JA(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Attempt to initialize JA with null Object");
        }
        this.ja = (JSONArray)obj;
    }

    public JSONArray toJSONArray() {
        return ja;
    }

    public int size() {
        return ja.size();
    }

    public Iterator<JO> iterator() {
        List<Object> lo = new ArrayList<>(ja);
        List<JO> list = lo.stream().map(JO::valueOf).collect(Collectors.toList());
        return list.iterator();
    }

    public List<String> values() {
        List<Object> lo = (List)ja;
        return lo.stream().map(e -> (String)e).collect(Collectors.toList());
    }

    public List<JO> objects() {
        List<Object> lo = (List)ja;
        return lo.stream().map(e -> e instanceof JO ? (JO)e : new JO(e)).collect(Collectors.toList());
    }

    public boolean add(JO jo) {
        return ja.add(jo.toJSONObject());
    }

    public JO get(int i) {
        return new JO(ja.get(i));
    }

    public Object getObject(int i) {
        return ja.get(i);
    }

    /**
     * Required by JSON#encodeArray()
     * @return iterator
     */
    @Override
    public ListIterator listIterator() {
        return ja.listIterator();
    }

    public static JA parse(String s) {
        try {
            return new JA(JSONValue.parseWithException(s));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JA parse(Reader r) {
        try {
            return new JA(JSONValue.parseWithException(r));
        } catch (ParseException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean addAllJO(Collection<JO> c) {
        boolean modified = false;
        for (JO e : c)
            if (add(e))
                modified = true;
        return modified;
    }
}
