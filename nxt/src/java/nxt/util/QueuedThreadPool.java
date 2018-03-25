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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * QueuedThreadPool creates threads to process requests until the maximum pool
 * size is reached.  Additional requests are queued until a thread becomes
 * available.  Threads that are idle for 60 seconds are terminated if the
 * pool size is greater than the core size.
 */
public class QueuedThreadPool extends ThreadPoolExecutor {

    /** Core pool size */
    private int coreSize;

    /** Maximum pool size */
    private int maxSize;

    /** Pending task queue */
    private final LinkedBlockingQueue<Runnable> pendingQueue = new LinkedBlockingQueue<>();

    /**
     * Create the queued thread pool
     *
     * @param   coreSize                Core pool size
     * @param   maxSize                 Maximum pool size
     */
    public QueuedThreadPool(int coreSize, int maxSize) {
        super(coreSize, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
        this.coreSize = coreSize;
        this.maxSize = maxSize;
    }

    /**
     * Return the core pool size
     *
     * @return                          Core pool size
     */
    @Override
    public int getCorePoolSize() {
        return coreSize;
    }

    /**
     * Set the core pool size
     *
     * @param   coreSize                Core pool size
     */
    @Override
    public void setCorePoolSize(int coreSize) {
        super.setCorePoolSize(coreSize);
        this.coreSize = coreSize;
    }

    /**
     * Return the maximum pool size
     *
     * @return                          Maximum pool size
     */
    @Override
    public int getMaximumPoolSize() {
        return maxSize;
    }

    /**
     * Set the maximum pool size
     *
     * @param   maxSize                 Maximum pool size
     */
    @Override
    public void setMaximumPoolSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Execute a task
     *
     * @param   task                            Task
     * @throws  RejectedExecutionException      Unable to execute task
     */
    @Override
    public void execute(Runnable task) throws RejectedExecutionException {
        if (task == null)
            throw new NullPointerException("Null runnable passed to execute()");
        try {
            if (getActiveCount() >= maxSize) {
                pendingQueue.put(task);
            } else {
                super.execute(task);
            }
        } catch (InterruptedException exc) {
            throw new RejectedExecutionException("Unable to queue task", exc);
        }
    }

    /**
     * Submit a task for execution
     *
     * @param   task                            Runnable task
     * @return                                  Future representing the task
     * @throws  RejectedExecutionException      Unable to execute task
     */
    @Override
    public Future<?> submit(Runnable task) throws RejectedExecutionException {
        if (task == null)
            throw new NullPointerException("Null runnable passed to submit()");
        FutureTask<Void> futureTask = new FutureTask<>(task, null);
        execute(futureTask);
        return futureTask;
    }

    /**
     * Submit a task for execution
     *
     * @param   <T>                             Result type
     * @param   task                            Runnable task
     * @param   result                          Result returned when task completes
     * @return                                  Future representing the task result
     * @throws  RejectedExecutionException      Unable to execute task
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) throws RejectedExecutionException {
        if (task == null)
            throw new NullPointerException("Null runnable passed to submit()");
        FutureTask<T> futureTask = new FutureTask<>(task, result);
        execute(futureTask);
        return futureTask;
    }

    /**
     * Submit a task for execution
     *
     * @param   <T>                             Result type
     * @param   callable                        Callable task
     * @return                                  Future representing the task
     * @throws  RejectedExecutionException      Unable to execute task
     */
    @Override
    public <T> Future<T> submit(Callable<T> callable) throws RejectedExecutionException {
        if (callable == null)
            throw new NullPointerException("Null callable passed to submit()");
        FutureTask<T> futureTask = new FutureTask<>(callable);
        execute(futureTask);
        return futureTask;
    }

    /**
     * Process task completion
     *
     * @param   task                    Runnable task
     * @param   exc                     Thrown exception
     */
    @Override
    protected void afterExecute(Runnable task, Throwable exc) {
        super.afterExecute(task, exc);
        Runnable newTask = pendingQueue.poll();
        if (newTask != null)
            super.execute(newTask);
    }
}
