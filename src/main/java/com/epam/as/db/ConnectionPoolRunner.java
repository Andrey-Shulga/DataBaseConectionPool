package com.epam.as.db;

import com.epam.as.db.connectionpool.ConnectionPool;
import com.epam.as.db.connectionpool.SelecteQueryRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test connection pool in many threads.
 */
public class ConnectionPoolRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolRunner.class);

    public static void main(String[] args) {

        final int NUMBERS_OF_THREAD = 50;

        ConnectionPool.getInstance();

        for (int i = 0; i < NUMBERS_OF_THREAD; i++) {
            Thread thread = new Thread(new SelecteQueryRunnable());
            thread.start();
        }

    }
}
