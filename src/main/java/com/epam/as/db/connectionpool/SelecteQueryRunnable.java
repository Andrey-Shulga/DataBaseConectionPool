package com.epam.as.db.connectionpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Select query runs in many thread.
 */
public class SelecteQueryRunnable implements Runnable {

    @Override
    public void run() {

        try (Connection connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM authors LIMIT 1");
            ResultSet resultSet = preparedStatement.executeQuery();
            //ConnectionPool.returnConnectionToPool(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
