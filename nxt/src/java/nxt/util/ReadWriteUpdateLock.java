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

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * A read or update lock allows shared access while a write lock enforces exclusive access.  Multiple
 * threads can hold the read lock but only one thread can hold the update or write lock.  If a thread
 * obtains a lock that it already holds, it must release the lock the same number of times that it
 * obtained the lock.
 * </p>
 * <ul>
 * <li>An attempt to obtain the read lock while another thread holds the write lock will cause
 * the thread to be suspended until the write lock is released.</li>
 * <li>An attempt to obtain the update lock while another thread holds the update or write lock
 * will cause the thread to be suspended until the blocking lock is released.  A thread
 * holding the update lock can subsequently obtain the write lock to gain exclusive access.
 * An attempt to obtain the update lock while holding either the read lock or the write lock
 * will result in an exception.</li>
 * <li>An attempt to obtain the write lock while another thread holds the read, update or write lock
 * will cause the thread to be suspended until the blocking lock is released.
 * An attempt to obtain the write lock while holding the read lock will result in an exception.</li>
 * </ul>
 */
public class ReadWriteUpdateLock {

    /** Lock shared by the read and write locks */
    private final ReentrantReadWriteLock sharedLock = new ReentrantReadWriteLock();

    /** Lock used by the update lock */
    private final ReentrantLock mutexLock = new ReentrantLock();

    /** Lock counts */
    private final ThreadLocal<LockCount> lockCount = ThreadLocal.withInitial(LockCount::new);

    /** Read lock */
    private final ReadLock readLock = new ReadLock();

    /** Update lock */
    private final UpdateLock updateLock = new UpdateLock();

    /** Write lock */
    private final WriteLock writeLock = new WriteLock();

    /**
     * Return the read lock
     *
     * @return                      Read lock
     */
    public Lock readLock() {
        return readLock;
    }

    /**
     * Return the update lock
     *
     * @return                      Update lock
     */
    public Lock updateLock() {
        return updateLock;
    }

    /**
     * Return the write lock
     *
     * @return                      Write lock
     */
    public Lock writeLock() {
        return writeLock;
    }

    /**
     * Lock interface
     */
    public interface Lock {

        /**
         * Obtain the lock
         */
        void lock();

        /**
         * Release the lock
         */
        void unlock();

        /**
         * Check if the thread holds the lock
         *
         * @return                  TRUE if the thread holds the lock
         */
        boolean hasLock();
    }

    /**
     * Read lock
     */
    private class ReadLock implements Lock {

        /**
         * Obtain the lock
         */
        @Override
        public void lock() {
            sharedLock.readLock().lock();
            lockCount.get().readCount++;
        }

        /**
         * Release the lock
         */
        @Override
        public void unlock() {
            sharedLock.readLock().unlock();
            lockCount.get().readCount--;
        }

        /**
         * Check if the thread holds the lock
         *
         * @return                  TRUE if the thread holds the lock
         */
        @Override
        public boolean hasLock() {
            return lockCount.get().readCount != 0;
        }
    }

    /**
     * Update lock
     */
    private class UpdateLock implements Lock {

        /**
         * Obtain the lock
         *
         * Caller must not hold the read or write lock
         */
        @Override
        public void lock() {
            LockCount counts = lockCount.get();
            if (counts.readCount != 0) {
                throw new IllegalStateException("Update lock cannot be obtained while holding the read lock");
            }
            if (counts.writeCount != 0) {
                throw new IllegalStateException("Update lock cannot be obtained while holding the write lock");
            }
            mutexLock.lock();
            counts.updateCount++;
        }

        /**
         * Release the lock
         */
        @Override
        public void unlock() {
            mutexLock.unlock();
            lockCount.get().updateCount--;
        }

        /**
         * Check if the thread holds the lock
         *
         * @return                  TRUE if the thread holds the lock
         */
        @Override
        public boolean hasLock() {
            return lockCount.get().updateCount != 0;
        }
    }

    /**
     * Write lock
     */
    private class WriteLock implements Lock {

        /**
         * Obtain the lock
         *
         * Caller must not hold the read lock
         */
        @Override
        public void lock() {
            LockCount counts = lockCount.get();
            if (counts.readCount != 0) {
                throw new IllegalStateException("Write lock cannot be obtained while holding the read lock");
            }
            boolean lockObtained = false;
            try {
                mutexLock.lock();
                counts.updateCount++;
                lockObtained = true;
                sharedLock.writeLock().lock();
                counts.writeCount++;
            } catch (Exception exc) {
                if (lockObtained) {
                    mutexLock.unlock();
                    counts.updateCount--;
                }
                throw exc;
            }
        }

        /**
         * Release the lock
         */
        @Override
        public void unlock() {
            LockCount counts = lockCount.get();
            sharedLock.writeLock().unlock();
            counts.writeCount--;
            mutexLock.unlock();
            counts.updateCount--;
        }

        /**
         * Check if the thread holds the lock
         *
         * @return                  TRUE if the thread holds the lock
         */
        @Override
        public boolean hasLock() {
            return lockCount.get().writeCount != 0;
        }
    }

    /**
     * Lock counts
     */
    private class LockCount {

        /** Read lock count */
        private int readCount;

        /** Update lock count */
        private int updateCount;

        /** Write lock count */
        private int writeCount;
    }
}
