package com.epam.as.db.connectionpool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {

    private static ConnectionPool connectionPool;
    private static BlockingDeque<Connection> connections;
    private static final int POOL_START_SIZE = 10;
    private static final int POOL_MAX_SIZE = 30;
    private static int connectionCount = 0;
    private static final String dbPropertyFileName = "database.properties";
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private ConnectionPool() {
        connections = new LinkedBlockingDeque<>(POOL_MAX_SIZE);
        logger.debug("Maximum limit of connections in the pool = {} connections", POOL_MAX_SIZE);
        logger.debug("Create initial connection pool = {} connections", POOL_START_SIZE);
        for (int i = 0; i < POOL_START_SIZE; i++) {
            Connection connection = null;
            try {
                connection = getNewConnection();
            } catch (SQLException e) {
                logger.error("Can't get connection from database", e);
            }
            connections.addFirst(connection);
        }
        logger.debug("Initial connection pool with = {} connections created.", connections.size());
    }

    private static Connection getNewConnection() throws SQLException {
        Connection connection;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(dbPropertyFileName))) {
            props.load(in);
        } catch (IOException e) {
            logger.error("IOException error:", e);
        }

        //String drivers = props.getProperty("jdbc.drivers");
        //if (drivers != null) System.setProperty("jdbc.drivers", drivers);

        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        connectionCount++;
        logger.debug("The current number of connections = {}", connectionCount);
        //logger.debug("Current connection's pool size = {}", connections.size());
        return DriverManager.getConnection(url, username, password);

    }

    public static ConnectionPool createConnectionPool() {
        return InstanceHolder.instance;
    }

    static Connection getConnection() {

        Connection connection = null;
        if (connectionCount < POOL_MAX_SIZE) {
            try {
                connection = connections.pollFirst();
                if (connection == null)
                    return getNewConnection();
            } catch (SQLException e) {
                logger.error("Can't get new connection", e);
            }
        } else {
            try {
                if (connectionCount == POOL_MAX_SIZE)
                    logger.debug("Connection pool reached max size {}, new connection " +
                            "will not be create, wait for release any connection!", POOL_MAX_SIZE);
                connection = connections.takeFirst();
            } catch (InterruptedException e) {
                logger.error("Can't get new connection from Connection pool", e);
            }
        }
        logger.debug("Thread took connection from pool, pool size now = {}", connections.size());

        return connection;
    }

    public static void returnConnectionToPool(Connection returnedConnection) {
        if (returnedConnection != null) connections.offerLast(returnedConnection);
    }

    private static class InstanceHolder {
        final static ConnectionPool instance = new ConnectionPool();
    }
}

