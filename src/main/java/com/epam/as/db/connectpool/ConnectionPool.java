package com.epam.as.db.connectpool;


import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {

    private static ConnectionPool connectionPool;
    private static BlockingDeque connections;
    private static final int POOL_MAX_SIZE = 50;

    public static void createConnectionPool() {
        connectionPool = new ConnectionPool();
        connections = new LinkedBlockingDeque(POOL_MAX_SIZE);
    }

}
