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

package nxt.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Listeners<T,E extends Enum<E>> {

    private final ConcurrentHashMap<Enum<E>, List<Listener<T>>> listenersMap = new ConcurrentHashMap<>();

    public boolean addListener(Listener<T> listener, Enum<E> eventType) {
        synchronized (eventType) {
            List<Listener<T>> listeners = listenersMap.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
            return listeners.add(listener);
        }
    }

    public boolean removeListener(Listener<T> listener, Enum<E> eventType) {
        synchronized (eventType) {
            List<Listener<T>> listeners = listenersMap.get(eventType);
            if (listeners != null) {
                return listeners.remove(listener);
            }
        }
        return false;
    }

    public void notify(T t, Enum<E> eventType) {
        List<Listener<T>> listeners = listenersMap.get(eventType);
        if (listeners != null) {
            for (Listener<T> listener : listeners) {
                listener.notify(t);
            }
        }
    }

}
