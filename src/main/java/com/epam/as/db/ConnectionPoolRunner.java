package com.epam.as.db;

import com.epam.as.db.connectpool.ConnectionPool;
import com.epam.as.db.connectpool.SelecteQueryRunnable;

/**
 * Test connection pool in many threads.
 */
public class ConnectionPoolRunner {

    public static void main(String[] args) {

        final int NUMBERS_OF_THREAD = 100;

        ConnectionPool connectionPool = ConnectionPool.createConnectionPool();
        SelecteQueryRunnable selecteQueryRunnable = new SelecteQueryRunnable();

        for (int i = 0; i < NUMBERS_OF_THREAD; i++) {
            Thread thread = new Thread(selecteQueryRunnable);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread.start();
        }
    }
}
