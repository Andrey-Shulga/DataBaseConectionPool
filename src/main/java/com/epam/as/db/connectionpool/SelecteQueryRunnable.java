package com.epam.as.db.connectionpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Select query runs in many thread.
 */
public class SelecteQueryRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SelecteQueryRunnable.class);

    @Override
    public void run() {


        try {
            Connection connection = ConnectionPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM authors LIMIT 1");
            ResultSet resultSet = preparedStatement.executeQuery();
            ConnectionPool.putConnectionToPool(connection);
        } catch (SQLException e) {
            logger.error("Can't get new statement or resultset from connection " + e.getMessage());
        }
    }
}
