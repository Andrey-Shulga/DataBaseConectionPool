package com.epam.as.db.connectionpool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {

    private static final int POOL_START_SIZE = 10;
    private static final int POOL_MAX_SIZE = 30;
    private static final BlockingQueue<Connection> connections = new ArrayBlockingQueue<>(POOL_MAX_SIZE);
    private static final String dbPropertyFileName = "database.properties";
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static int connectionCount = 0;
    private static String url;
    private static String username;
    private static String password;

    private ConnectionPool() {

        Properties props = new Properties();
        try (InputStream in = ConnectionPool.class.getClassLoader().getResourceAsStream(dbPropertyFileName)) {
            props.load(in);
        } catch (IOException e) {
            logger.error("Can't open file {} for reading properties of database.", e.getMessage());
        }

        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) System.setProperty("jdbc.drivers", drivers);
        url = props.getProperty("jdbc.url");
        username = props.getProperty("jdbc.username");
        password = props.getProperty("jdbc.password");

        logger.debug("Maximum limit of connections in the pool = {} connections", POOL_MAX_SIZE);
        logger.debug("Trying to create initial connection pool = {} connections...", POOL_START_SIZE);
        for (int i = 0; i < POOL_START_SIZE; i++) {
            Connection connection = getNewConnection(url, username, password);
            if (connection != null)
                connections.offer(connection);
        }
        logger.debug("Initial connection pool with {} connections was created.", connections.size());
    }

    public static ConnectionPool getInstance() {
        return InstanceHolder.instance;
    }

    private static Connection getNewConnection(String url, String username, String password) {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.error("Can't get new connection from database. " + e.getMessage());
        }
        connectionCount++;
        logger.debug("The current number of connections = {}", connectionCount);
        return connection;
    }

    public static synchronized Connection getConnection() {

        Connection connection = null;
        logger.debug("Thread trying to take connection from pool...");

        if (connectionCount < POOL_MAX_SIZE) {
            connection = connections.poll();
            if (connection == null) {
                logger.debug("No connections in pool! Trying to get new connection...");
                return getNewConnection(url, username, password);
            }
            logger.debug("Thread take connection from pool, total connections in pool now = {}", connections.size());
        } else {
            logger.debug("Number of connections reached max pool's size = {}, No new connection " +
                    "will be create, waiting for release any connection...", POOL_MAX_SIZE);
            try {
                connection = connections.take();
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting new connection from Connection pool. " + e.getMessage());
            }
        }
        return connection;
    }

    static void putConnectionToPool(Connection returnedConnection) {
        if (returnedConnection != null) {
            connections.offer(returnedConnection);
            logger.debug("Thread return connection back to pool, now total connections in pool = {}", connections.size());
        }
    }

    public static void close() {
        for (Connection con : connections)
            try {
                if (!con.isClosed())
                    con.close();
            } catch (SQLException e) {
                logger.debug("Error with database access occur. " + e.getMessage());
            }
        logger.debug("Connection pool was closed.");
    }

    private static class InstanceHolder {
        final static ConnectionPool instance = new ConnectionPool();
    }


}

