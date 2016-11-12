package com.epam.as.db;

import com.epam.as.db.connectpool.ConnectionPool;
import com.epam.as.db.connectpool.SelecteQueryRunnable;

/**
 * Test connection pool in many threads.
 */
public class ConnectionPoolRunner {

    public static void main(String[] args) {

        ConnectionPool connectionPool = ConnectionPool.createConnectionPool();
        SelecteQueryRunnable selecteQueryRunnable = new SelecteQueryRunnable();
        Thread thread = new Thread(selecteQueryRunnable);
        for (int i = 0; i < 100; i++) {
            thread.start();
        }
    }
}
