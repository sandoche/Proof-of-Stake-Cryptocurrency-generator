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

import nxt.AccountLedger;
import nxt.AccountLedger.LedgerEntry;
import nxt.Block;
import nxt.BlockchainProcessor;
import nxt.Db;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionProcessor;
import nxt.db.TransactionalDb;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EventListener listens for peer, block, transaction and account ledger events as
 * specified by the EventRegister API.  Events are held until
 * an EventWait API request is received.  All pending events
 * are then returned to the application.
 *
 * Event registrations are discarded if an EventWait API request
 * has not been received within nxt.apiEventTimeout seconds.
 *
 * The maximum number of event users is specified by nxt.apiMaxEventUsers.
 */
class EventListener implements Runnable, AsyncListener, TransactionalDb.TransactionCallback {

    /** Maximum event users */
    static final int maxEventUsers = Nxt.getIntProperty("nxt.apiMaxEventUsers");

    /** Event registration timeout (seconds) */
    static final int eventTimeout = Math.max(Nxt.getIntProperty("nxt.apiEventTimeout"), 15);

    /** Blockchain processor */
    static final BlockchainProcessor blockchainProcessor = Nxt.getBlockchainProcessor();

    /** Transaction processor */
    static final TransactionProcessor transactionProcessor = Nxt.getTransactionProcessor();

    /** Active event users */
    static final Map<String, EventListener> eventListeners = new ConcurrentHashMap<>();

    /** Thread to clean up inactive event registrations */
    private static final Timer eventTimer = new Timer();
    static {
        eventTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long oldestTime = System.currentTimeMillis() - eventTimeout*1000;
                eventListeners.values().forEach(listener -> {
                    if (listener.getTimestamp() < oldestTime) {
                        listener.deactivateListener();
                    }
                });
            }
        }, eventTimeout*1000/2, eventTimeout*1000/2);
    }

    /** Thread pool for asynchronous completions */
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    /** Peer events - update API comments for EventRegister and EventWait if changed */
    static final List<Peers.Event> peerEvents = new ArrayList<>();
    static {
        peerEvents.add(Peers.Event.ADD_INBOUND);
        peerEvents.add(Peers.Event.ADDED_ACTIVE_PEER);
        peerEvents.add(Peers.Event.BLACKLIST);
        peerEvents.add(Peers.Event.CHANGED_ACTIVE_PEER);
        peerEvents.add(Peers.Event.DEACTIVATE);
        peerEvents.add(Peers.Event.NEW_PEER);
        peerEvents.add(Peers.Event.REMOVE);
        peerEvents.add(Peers.Event.REMOVE_INBOUND);
        peerEvents.add(Peers.Event.UNBLACKLIST);
    }

    /** Block events - update API comments for EventRegister and EventWait if changed */
    static final List<BlockchainProcessor.Event> blockEvents = new ArrayList<>();
    static {
        blockEvents.add(BlockchainProcessor.Event.BLOCK_GENERATED);
        blockEvents.add(BlockchainProcessor.Event.BLOCK_POPPED);
        blockEvents.add(BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    /** Transaction events - update API comments for EventRegister and EventWait if changed */
    static final List<TransactionProcessor.Event> txEvents = new ArrayList<>();
    static {
        txEvents.add(TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS);
        txEvents.add(TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
        txEvents.add(TransactionProcessor.Event.REJECT_PHASED_TRANSACTION);
        txEvents.add(TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        txEvents.add(TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS);
    }

    /** Account ledger events - update API comments for EventRegister and EventWait if changed */
    static final List<AccountLedger.Event> ledgerEvents = new ArrayList<>();
    static {
        ledgerEvents.add(AccountLedger.Event.ADD_ENTRY);
    }

    /** Application IP address */
    private final String address;

    /** Activity timestamp */
    private long timestamp;

    /** Activity lock */
    private final ReentrantLock lock = new ReentrantLock();

    /** Event listener has been deactivated */
    private volatile boolean deactivated;

    /** Event wait aborted */
    private boolean aborted;

    /** Event thread dispatched */
    private boolean dispatched;

    /** Nxt event listeners */
    private final List<NxtEventListener> nxtEventListeners = new ArrayList<>();

    /** Pending events */
    private final List<PendingEvent> pendingEvents = new ArrayList<>();

    /** Database events */
    private final List<PendingEvent> dbEvents = new ArrayList<>();

    /** Pending waits */
    private final List<AsyncContext> pendingWaits = new ArrayList<>();

    /**
     * Create an event listener
     *
     * @param   address             Application IP address
     */
    EventListener(String address) {
        this.address = address;
    }

    /**
     * Activate the event listener
     *
     * Nxt event listeners will be added for the specified events
     *
     * @param   eventRegistrations      List of Nxt event registrations
     * @throws  EventListenerException  Unable to activate event listeners
     */
    void activateListener(List<EventRegistration> eventRegistrations) throws EventListenerException {
        if (deactivated)
            throw new EventListenerException("Event listener deactivated");
        if (eventListeners.size() >= maxEventUsers && eventListeners.get(address) == null)
            throw new EventListenerException(String.format("Too many API event users: Maximum %d", maxEventUsers));
        //
        // Start listening for events
        //
        addEvents(eventRegistrations);
        //
        // Add this event listener to the active list
        //
        EventListener oldListener = eventListeners.put(address, this);
        if (oldListener != null)
            oldListener.deactivateListener();
        Logger.logDebugMessage(String.format("Event listener activated for %s", address));
    }

    /**
     * Add events to the event list
     *
     * @param   eventRegistrations      Nxt event registrations
     * @throws  EventListenerException  Invalid Nxt event
     */
    void addEvents(List<EventRegistration> eventRegistrations) throws EventListenerException {
        lock.lock();
        try {
            if (deactivated)
                return;
            //
            // A listener with account identifier 0 accepts events for all accounts.
            // This listener supersedes  listeners for a single account.
            //
            for (EventRegistration event : eventRegistrations) {
                boolean addListener = true;
                Iterator<NxtEventListener> it = nxtEventListeners.iterator();
                while (it.hasNext()) {
                    NxtEventListener listener = it.next();
                    if (listener.getEvent() == event.getEvent()) {
                        long accountId = listener.getAccountId();
                        if (accountId == event.getAccountId() || accountId == 0) {
                            addListener = false;
                            break;
                        }
                        if (event.getAccountId() == 0) {
                            listener.removeListener();
                            it.remove();
                        }
                    }
                }
                if (addListener) {
                    NxtEventListener listener = new NxtEventListener(event);
                    listener.addListener();
                    nxtEventListeners.add(listener);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove events from the event list
     *
     * @param   eventRegistrations      Nxt event registrations
     */
    void removeEvents(List<EventRegistration> eventRegistrations) {
        lock.lock();
        try {
            if (deactivated)
                return;
            //
            // Specifying an account identifier of 0 results in removing all
            // listeners for the specified event.  Otherwise, only the listener
            // for the specified account is removed.
            //
            for (EventRegistration event : eventRegistrations) {
                Iterator<NxtEventListener> it = nxtEventListeners.iterator();
                while (it.hasNext()) {
                    NxtEventListener listener = it.next();
                    if (listener.getEvent() == event.getEvent() &&
                            (listener.getAccountId() == event.getAccountId() || event.getAccountId() == 0)) {
                        listener.removeListener();
                        it.remove();
                    }
                }
            }
            //
            // Deactivate the listeners if there are no events remaining
            //
            if (nxtEventListeners.isEmpty())
                deactivateListener();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Deactivate the event listener
     */
    void deactivateListener() {
        lock.lock();
        try {
            if (deactivated)
                return;
            deactivated = true;
            //
            // Cancel all pending wait requests
            //
            if (!pendingWaits.isEmpty() && !dispatched) {
                dispatched = true;
                threadPool.submit(this);
            }
            //
            // Remove this event listener from the active list
            //
            eventListeners.remove(address);
            //
            // Stop listening for events
            //
            nxtEventListeners.forEach(NxtEventListener::removeListener);
        } finally {
            lock.unlock();
        }
        Logger.logDebugMessage(String.format("Event listener deactivated for %s", address));
    }

    /**
     * Wait for an event
     *
     * @param   req                     HTTP request
     * @param   timeout                 Wait timeout in seconds
     * @return                          List of pending events or null if wait incomplete
     * @throws  EventListenerException  Unable to wait for an event
     */
    List<PendingEvent> eventWait(HttpServletRequest req, long timeout) throws EventListenerException {
        List<PendingEvent> events = null;
        lock.lock();
        try {
            if (deactivated)
                throw new EventListenerException("Event listener deactivated");
            if (!pendingWaits.isEmpty()) {
                //
                // We want only one waiter at a time.  This can happen if the
                // application issues an event wait while it already has an event
                // wait outstanding.  In this case, we will cancel the current wait
                // and replace it with the new wait.
                //
                aborted = true;
                if (!dispatched) {
                    dispatched = true;
                    threadPool.submit(this);
                }
                AsyncContext context = req.startAsync();
                context.addListener(this);
                context.setTimeout(timeout*1000);
                pendingWaits.add(context);
            } else if (!pendingEvents.isEmpty()) {
                //
                // Return immediately if we have a pending event
                //
                events = new ArrayList<>(pendingEvents);
                pendingEvents.clear();
                timestamp = System.currentTimeMillis();
            } else {
                //
                // Wait for an event
                //
                aborted = false;
                AsyncContext context = req.startAsync();
                context.addListener(this);
                context.setTimeout(timeout*1000);
                pendingWaits.add(context);
                timestamp = System.currentTimeMillis();
            }
        } finally {
            lock.unlock();
        }
        return events;
    }

    /**
     * Complete the current event wait (Runnable interface)
     */
    @Override
    public void run() {
        lock.lock();
        try {
            dispatched = false;
            while (!pendingWaits.isEmpty() && (aborted || deactivated || !pendingEvents.isEmpty())) {
                AsyncContext context = pendingWaits.remove(0);
                List<PendingEvent> events = new ArrayList<>();
                if (!aborted && !deactivated) {
                    events.addAll(pendingEvents);
                    pendingEvents.clear();
                }
                HttpServletResponse resp = (HttpServletResponse)context.getResponse();
                JSONObject response = EventWait.formatResponse(events);
                response.put("requestProcessingTime", System.currentTimeMillis()-timestamp);
                try (Writer writer = resp.getWriter()) {
                    response.writeJSONString(writer);
                } catch (IOException exc) {
                    Logger.logDebugMessage(String.format("Unable to return API response to %s: %s",
                                                         address, exc.toString()));
                }
                context.complete();
                aborted = false;
                timestamp = System.currentTimeMillis();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the activity timestamp
     *
     * @return                      Activity timestamp (milliseconds)
     */
    long getTimestamp() {
        return timestamp;
    }

    /**
     * Async operation completed (AsyncListener interface)
     *
     * @param   event               Async event
     */
    @Override
    public void onComplete(AsyncEvent event) {
    }

    /**
     * Async error detected (AsyncListener interface)
     *
     * @param   event               Async event
     */
    @Override
    public void onError(AsyncEvent event) {
        AsyncContext context = event.getAsyncContext();
        lock.lock();
        try {
            pendingWaits.remove(context);
            context.complete();
            timestamp = System.currentTimeMillis();
            Logger.logDebugMessage("Error detected during event wait for "+address, event.getThrowable());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Async operation started (AsyncListener interface)
     *
     * @param   event               Async event
     */
    @Override
    public void onStartAsync(AsyncEvent event) {
    }

    /**
     * Async operation timeout (AsyncListener interface)
     *
     * @param   event               Async event
     */
    @Override
    public void onTimeout(AsyncEvent event) {
        AsyncContext context = event.getAsyncContext();
        lock.lock();
        try {
            pendingWaits.remove(context);
            JSONObject response = new JSONObject();
            response.put("events", new JSONArray());
            response.put("requestProcessingTime", System.currentTimeMillis()-timestamp);
            try (Writer writer = context.getResponse().getWriter()) {
                response.writeJSONString(writer);
            } catch (IOException exc) {
                Logger.logDebugMessage(String.format("Unable to return API response to %s: %s",
                                                     address, exc.toString()));
            }
            context.complete();
            timestamp = System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transaction has been committed
     *
     * Dispatch the pending events for this database transaction
     */
    @Override
    public void commit() {
        Thread thread = Thread.currentThread();
        lock.lock();
        try {
            Iterator<PendingEvent> it = dbEvents.iterator();
            while (it.hasNext()) {
                PendingEvent pendingEvent = it.next();
                if (pendingEvent.getThread() == thread) {
                    it.remove();
                    pendingEvents.add(pendingEvent);
                    if (!pendingWaits.isEmpty() && !dispatched) {
                        dispatched = true;
                        threadPool.submit(EventListener.this);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transaction has been rolled back
     *
     * Discard the pending events for this database transaction
     */
    @Override
    public void rollback() {
        Thread thread = Thread.currentThread();
        lock.lock();
        try {
            dbEvents.removeIf(pendingEvent -> pendingEvent.getThread() == thread);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Pending event
     */
    static class PendingEvent {

        /** Event name */
        private final String name;

        /** Event identifier */
        private final String id;

        /** Event identifier list */
        private final List<String> idList;

        /** Database thread */
        private Thread thread;

        /**
         * Create a pending event
         *
         * @param   name            Event name
         * @param   id              Event identifier
         */
        public PendingEvent(String name, String id) {
            this.name = name;
            this.id = id;
            this.idList = null;
        }

        /**
         * Create a pending event
         *
         * @param   name            Event name
         * @param   idList          Event identifier list
         */
        public PendingEvent(String name, List<String> idList) {
            this.name = name;
            this.idList = idList;
            this.id = null;
        }

        /**
         * Return the event name
         *
         * @return                  Event name
         */
        public String getName() {
            return name;
        }

        /**
         * Check if the identifier is a list
         *
         * @return                  TRUE if the identifier is a list
         */
        public boolean isList() {
            return (idList != null);
        }

        /**
         * Return the event identifier
         *
         * @return                  Event identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Return the event identifier list
         *
         * @return                  Event identifier list
         */
        public List<String> getIdList() {
            return idList;
        }

        /**
         * Return the database thread
         *
         * @return                  Database thread
         */
        public Thread getThread() {
            return thread;
        }

        /**
         * Set the database thread
         *
         * @param   thread          Database thread
         */
        public void setThread(Thread thread) {
            this.thread = thread;
        }
    }

    /**
     * Nxt event listener
     */
    private class NxtEventListener {

        /** Event handler */
        private final NxtEventHandler eventHandler;

        /**
         * Create the Nxt event listener
         *
         * @param   eventRegistration           Event registration
         * @throws  EventListenerException      Invalid event
         */
        public NxtEventListener(EventRegistration eventRegistration) throws EventListenerException {
            Enum<? extends Enum> event = eventRegistration.getEvent();
            if (event instanceof Peers.Event) {
                eventHandler = new PeerEventHandler(eventRegistration);
            } else if (event instanceof BlockchainProcessor.Event) {
                eventHandler = new BlockEventHandler(eventRegistration);
            } else if (event instanceof TransactionProcessor.Event) {
                eventHandler = new TransactionEventHandler(eventRegistration);
            } else if (event instanceof AccountLedger.Event) {
                eventHandler = new LedgerEventHandler(eventRegistration);
            } else {
                throw new EventListenerException("Unsupported listener event");
            }
        }

        /**
         * Return the Nxt event
         *
         * @return                  Nxt event
         */
        public Enum<? extends Enum> getEvent() {
            return eventHandler.getEvent();
        }

        /**
         * Return the account identifier
         *
         * @return                  Account identifier
         */
        public long getAccountId() {
            return eventHandler.getAccountId();
        }

        /**
         * Add the Nxt listener for this event
         */
        public void addListener() {
            eventHandler.addListener();
        }

        /**
         * Remove the Nxt listener for this event
         */
        public void removeListener() {
            eventHandler.removeListener();
        }

        /**
         * Return the hash code for this Nxt event listener
         *
         * @return                  Hash code
         */
        @Override
        public int hashCode() {
            return eventHandler.hashCode();
        }

        /**
         * Check if two Nxt events listeners are equal
         *
         * @param   obj             Comparison listener
         * @return                  TRUE if the listeners are equal
         */
        @Override
        public boolean equals(Object obj) {
            return (obj != null && (obj instanceof NxtEventListener) &&
                    eventHandler.equals(((NxtEventListener)obj).eventHandler));
        }

        /**
         * Nxt listener event handler
         */
        private abstract class NxtEventHandler {

            /** Owning event listener */
            protected final EventListener owner;

            /** Account identifier */
            protected final long accountId;

            /** Nxt listener event */
            protected final Enum<? extends Enum> event;

            /**
             * Create the Nxt event handler
             *
             * @param   eventRegistration   Event registration
             */
            public NxtEventHandler(EventRegistration eventRegistration) {
                this.owner = EventListener.this;
                this.accountId = eventRegistration.getAccountId();
                this.event = eventRegistration.getEvent();
            }

            /**
             * Return the Nxt event
             *
             * @return                  Nxt event
             */
            public Enum<? extends Enum> getEvent() {
                return event;
            }

            /**
             * Return the account identifier
             *
             * @return                  Account identifier
             */
            public long getAccountId() {
                return accountId;
            }

            /**
             * Add the Nxt listener for this event
             */
            public abstract void addListener();

            /**
             * Remove the Nxt listener for this event
             */
            public abstract void removeListener();

            /**
             * Check if need to wait for end of transaction
             *
             * @return                  TRUE if need to wait for transaction to commit/rollback
             */
            protected boolean waitTransaction() {
                return true;
            }

            /**
             * Dispatch the event
             */
            protected void dispatch(PendingEvent pendingEvent) {
                lock.lock();
                try {
                    if (waitTransaction() && Db.db.isInTransaction()) {
                        pendingEvent.setThread(Thread.currentThread());
                        dbEvents.add(pendingEvent);
                        Db.db.registerCallback(owner);
                    } else {
                        pendingEvents.add(pendingEvent);
                        if (!pendingWaits.isEmpty() && !dispatched) {
                            dispatched = true;
                            threadPool.submit(owner);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }

            /**
             * Return the hash code for this event listener
             *
             * @return                  Hash code
             */
            @Override
            public int hashCode() {
                return event.hashCode();
            }

            /**
             * Check if two events listeners are equal
             *
             * @param   obj             Comparison listener
             * @return                  TRUE if the listeners are equal
             */
            @Override
            public boolean equals(Object obj) {
                return (obj != null && (obj instanceof NxtEventHandler) &&
                                        owner == ((NxtEventHandler)obj).owner &&
                                        accountId == ((NxtEventHandler)obj).accountId &&
                                        event == ((NxtEventHandler)obj).event);
            }
        }

        /**
         * Peer event handler
         */
        private class PeerEventHandler extends NxtEventHandler implements Listener<Peer> {

            /**
             * Create the peer event handler
             *
             * @param   eventRegistration   Event registration
             */
            public PeerEventHandler(EventRegistration eventRegistration) {
                super(eventRegistration);
            }

            /**
             * Add the Nxt listener for this event
             */
            @Override
            public void addListener() {
                Peers.addListener(this, (Peers.Event)event);
            }

            /**
             * Remove the Nxt listener for this event
             */
            @Override
            public void removeListener() {
                Peers.removeListener(this, (Peers.Event)event);
            }

            /**
             * Event notification
             *
             * @param   peer        Peer
             */
            @Override
            public void notify(Peer peer) {
                dispatch(new PendingEvent("Peer." + event.name(), peer.getHost()));
            }

            /**
             * Check if need to wait for end of transaction
             *
             * @return                  TRUE if need to wait for transaction to commit/rollback
             */
            @Override
            protected boolean waitTransaction() {
                return false;
            }
        }

        /**
         * Blockchain processor event handler
         */
        private class BlockEventHandler extends NxtEventHandler implements Listener<Block> {

            /**
             * Create the blockchain processor event handler
             *
             * @param   eventRegistration   Event registration
             */
            public BlockEventHandler(EventRegistration eventRegistration) {
                super(eventRegistration);
            }

            /**
             * Add the Nxt listener for this event
             */
            @Override
            public void addListener() {
                blockchainProcessor.addListener(this, (BlockchainProcessor.Event)event);
            }

            /**
             * Remove the Nxt listener for this event
             */
            @Override
            public void removeListener() {
                blockchainProcessor.removeListener(this, (BlockchainProcessor.Event)event);
            }

            /**
             * Event notification
             *
             * @param   block       Block
             */
            @Override
            public void notify(Block block) {
                dispatch(new PendingEvent("Block." + event.name(), block.getStringId()));
            }
        }

        /**
         * Transaction processor event handler
         */
        private class TransactionEventHandler extends NxtEventHandler implements Listener<List<? extends Transaction>> {

            /**
             * Create the transaction processor event handler
             *
             * @param   eventRegistration   Event registration
             */
            public TransactionEventHandler(EventRegistration eventRegistration) {
                super(eventRegistration);
            }

            /**
             * Add the Nxt listener for this event
             */
            @Override
            public void addListener() {
                transactionProcessor.addListener(this, (TransactionProcessor.Event)event);
            }

            /**
             * Remove the Nxt listener for this event
             */
            @Override
            public void removeListener() {
                transactionProcessor.removeListener(this, (TransactionProcessor.Event)event);
            }

            /**
             * Event notification
             *
             * @param   txList      Transaction list
             */
            @Override
            public void notify(List<? extends Transaction> txList) {
                List<String> idList = new ArrayList<>();
                txList.forEach((tx) -> idList.add(tx.getStringId()));
                dispatch(new PendingEvent("Transaction." + event.name(), idList));
            }
        }

        /**
         * Account ledger event handler
         */
        private class LedgerEventHandler extends NxtEventHandler implements Listener<LedgerEntry> {

            /**
             * Create the account ledger event handler
             *
             * @param   eventRegistration   Event registration
             */
            public LedgerEventHandler(EventRegistration eventRegistration) {
                super(eventRegistration);
            }

            /**
             * Add the Nxt listener for this event
             */
            @Override
            public void addListener() {
                AccountLedger.addListener(this, (AccountLedger.Event)event);
            }

            /**
             * Remove the Nxt listener for this event
             */
            @Override
            public void removeListener() {
                AccountLedger.removeListener(this, (AccountLedger.Event)event);
            }

            /**
             * Event notification
             *
             * @param   entry       Ledger entry
             */
            @Override
            public void notify(LedgerEntry entry) {
                if (entry.getAccountId() == accountId || accountId == 0)
                    dispatch(new PendingEvent(String.format("Ledger.%s.%s",
                                event.name(), Convert.rsAccount(entry.getAccountId())),
                                Long.toUnsignedString(entry.getLedgerId())));
            }
        }
    }

    /**
     * Event registration
     */
    static class EventRegistration {

        /** Nxt listener event */
        private final Enum<? extends Enum> event;

        /** Account identifier */
        private final long accountId;

        /**
         * Create the event registration
         *
         * @param   event           Nxt listener event
         * @param   accountId       Account identifier
         */
        EventRegistration(Enum<? extends Enum> event, long accountId) {
            this.event = event;
            this.accountId = accountId;
        }

        /**
         * Return the Nxt listener event
         *
         * @return                  Nxt listener event
         */
        public Enum<? extends Enum> getEvent() {
            return event;
        }

        /**
         * Return the account identifier
         *
         * @return                  Account identifier
         */
        public long getAccountId() {
            return accountId;
        }
    }

    /**
     * Event exception
     */
    static class EventListenerException extends Exception {

        /**
         * Create an event exception with a message
         *
         * @param   message         Exception message
         */
        public EventListenerException(String message) {
            super(message);
        }

        /**
         * Create an event exception with a message and a cause
         *
         * @param   message         Exception message
         * @param   cause           Exception cause
         */
        public EventListenerException(String message, Exception cause) {
            super(message, cause);
        }
    }
}
