package com.epam.as.db.connectpool;


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
    private static final int POOL_MAX_SIZE = 50;
    private static int connectionCount = 0;
    private static final String dbPropertyFileName = "database.properties";
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private ConnectionPool() {
        connections = new LinkedBlockingDeque<>(POOL_MAX_SIZE);
        for (int i = 0; i < POOL_START_SIZE; i++) {
            Connection connection = null;
            try {
                connection = getNewConnection();
            } catch (SQLException e) {
                logger.error("Can't get connection from database", e);
            }
            connections.addFirst(connection);
        }
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
        return DriverManager.getConnection(url, username, password);

    }

    public static ConnectionPool createConnectionPool() {
        if (connectionPool == null) {
            connectionPool = new ConnectionPool();
        }
        return connectionPool;
    }

    static Connection getConnection() {
        Connection connection = null;
        if (connectionCount <= POOL_MAX_SIZE) {
            try {
                connection = connections.pollFirst();
                if (connection == null)
                    return getNewConnection();
            } catch (SQLException e) {
                logger.error("Can't get new connection", e);
            }
        } else {
            try {
                connection = connections.takeFirst();
            } catch (InterruptedException e) {
                logger.error("Can't get new connection from Connection pool", e);
            }
        }
        return connection;
    }


}
