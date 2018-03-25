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

package nxt.util;

import nxt.Nxt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ThreadPool {

    private static volatile ScheduledExecutorService scheduledThreadPool;
    private static Map<Runnable,Long> backgroundJobs = new HashMap<>();
    private static List<Runnable> beforeStartJobs = new ArrayList<>();
    private static List<Runnable> lastBeforeStartJobs = new ArrayList<>();
    private static List<Runnable> afterStartJobs = new ArrayList<>();

    public static synchronized void runBeforeStart(Runnable runnable, boolean runLast) {
        if (scheduledThreadPool != null) {
            throw new IllegalStateException("Executor service already started");
        }
        if (runLast) {
            lastBeforeStartJobs.add(runnable);
        } else {
            beforeStartJobs.add(runnable);
        }
    }

    public static synchronized void runAfterStart(Runnable runnable) {
        afterStartJobs.add(runnable);
    }

    public static synchronized void scheduleThread(String name, Runnable runnable, int delay) {
        scheduleThread(name, runnable, delay, TimeUnit.SECONDS);
    }

    public static synchronized void scheduleThread(String name, Runnable runnable, int delay, TimeUnit timeUnit) {
        if (scheduledThreadPool != null) {
            throw new IllegalStateException("Executor service already started, no new jobs accepted");
        }
        if (! Nxt.getBooleanProperty("nxt.disable" + name + "Thread")) {
            backgroundJobs.put(runnable, timeUnit.toMillis(delay));
        } else {
            Logger.logMessage("Will not run " + name + " thread");
        }
    }

    public static synchronized void start(int timeMultiplier) {
        if (scheduledThreadPool != null) {
            throw new IllegalStateException("Executor service already started");
        }

        Logger.logDebugMessage("Running " + beforeStartJobs.size() + " tasks...");
        runAll(beforeStartJobs);
        beforeStartJobs = null;

        Logger.logDebugMessage("Running " + lastBeforeStartJobs.size() + " final tasks...");
        runAll(lastBeforeStartJobs);
        lastBeforeStartJobs = null;

        Logger.logDebugMessage("Starting " + backgroundJobs.size() + " background jobs");
        scheduledThreadPool = Executors.newScheduledThreadPool(backgroundJobs.size());
        for (Map.Entry<Runnable,Long> entry : backgroundJobs.entrySet()) {
            scheduledThreadPool.scheduleWithFixedDelay(entry.getKey(), 0, Math.max(entry.getValue() / timeMultiplier, 1), TimeUnit.MILLISECONDS);
        }
        backgroundJobs = null;

        Logger.logDebugMessage("Starting " + afterStartJobs.size() + " delayed tasks");
        Thread thread = new Thread(() -> {
            runAll(afterStartJobs);
            afterStartJobs = null;
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void shutdown() {
        if (scheduledThreadPool != null) {
	        Logger.logShutdownMessage("Stopping background jobs...");
            shutdownExecutor("scheduledThreadPool", scheduledThreadPool, 10);
            scheduledThreadPool = null;
        	Logger.logShutdownMessage("...Done");
        }
    }

    public static void shutdownExecutor(String name, ExecutorService executor, int timeout) {
        Logger.logShutdownMessage("shutting down " + name);
        executor.shutdown();
        try {
            executor.awaitTermination(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (! executor.isTerminated()) {
            Logger.logShutdownMessage("some threads in " + name + " didn't terminate, forcing shutdown");
            executor.shutdownNow();
        }
    }

    private static void runAll(List<Runnable> jobs) {
        List<Thread> threads = new ArrayList<>();
        final StringBuffer errors = new StringBuffer();
        for (final Runnable runnable : jobs) {
            Thread thread = new Thread(() -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    errors.append(t.getMessage()).append('\n');
                    throw t;
                }
            });
            thread.setDaemon(true);
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (errors.length() > 0) {
            throw new RuntimeException("Errors running startup tasks:\n" + errors.toString());
        }
    }

    private ThreadPool() {} //never

}
