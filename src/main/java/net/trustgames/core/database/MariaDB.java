package net.trustgames.core.database;

import com.zaxxer.hikari.HikariDataSource;
import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;

public class MariaDB {

    private final Core core;
    public HikariDataSource hikariDataSource;
    private Connection connection;

    public MariaDB(Core core) {
        this.core = core;
    }

    // method to check if table exists
    private static boolean tableExist(Connection connection, String tableName) throws SQLException {
        boolean tExists = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

    // create the specified database if it doesn't exist yet
    private void createDatabaseIfNotExists() {

        // get the mariadb config credentials
        MariaConfig mariaConfig = new MariaConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        String user = config.getString("mariadb.user");
        String password = config.getString("mariadb.password");
        String ip = config.getString("mariadb.ip");
        String port = config.getString("mariadb.port");
        String database = config.getString("mariadb.database");

        try {
            // try to create a connection and prepared statement with sql statement
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection("jdbc:mariadb://" + ip + ":" + port + "/", user, password); PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + database)) {
                statement.executeUpdate();
            }
        } catch (SQLException | ClassNotFoundException e) {
            core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Unable to create " + database + " database!");
            throw new RuntimeException(e);
        }

    }

    /*
     gets the connection. Checks if the connection isn't null. If it isn't, it will return connection
     if the connection is null, meaning it probably doesn't exist, it will create a new connection and return it
    */
    public Connection getConnection() {

        // create the database if it doesn't exist
        createDatabaseIfNotExists();

        // if the connection isn't null, it returns the connection
        if (connection != null) {
            return connection;
            // create new pool with new connection
        } else {

            // get the mariadb config credentials
            MariaConfig mariaConfig = new MariaConfig(core);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
            String user = config.getString("mariadb.user");
            String password = config.getString("mariadb.password");
            String ip = config.getString("mariadb.ip");
            String port = config.getString("mariadb.port");
            String database = config.getString("mariadb.database");

            // tries to connect to the database
            try {
                // set the basic stuff
                hikariDataSource = new HikariDataSource();
                HikariDataSource ds = hikariDataSource;
                ds.setDriverClassName("org.mariadb.jdbc.Driver");
                ds.setJdbcUrl("jdbc:mariadb://" + ip + ":" + port + "/" + database);
                ds.addDataSourceProperty("user", user);
                ds.addDataSourceProperty("password", password);
                ds.setMaximumPoolSize(5);
                ds.setPoolName("HikariCP-Core");

                connection = ds.getConnection();
                return connection;
            } catch (SQLException e) {
                core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Error when connecting to the database using HikariCP");
                throw new RuntimeException(e);
            }
        }
    }

    /*
     checks if the table exists, if it doesn't, it creates one using the given SQL statement
     (is run async)
    */
    public void initializeTable(String tableName, String stringStatement) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isMySQLEnabled()) {
                    try {
                        if (getConnection() != null) {
                            if (!tableExist(getConnection(), tableName)) {
                                core.getLogger().info(DebugColors.CYAN + "Database table " + tableName + " doesn't exist, creating...");
                                try (PreparedStatement statement = getConnection().prepareStatement(stringStatement)) {
                                    statement.executeUpdate();
                                    if (tableExist(getConnection(), tableName)) {
                                        core.getLogger().info(DebugColors.BLUE + "Successfully created the table " + tableName);
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "Unable to create " + tableName + " table in the database!");
                        throw new RuntimeException(e);
                    }
                } else {
                    core.getLogger().info(DebugColors.BLUE + DebugColors.RED_BACKGROUND + "MySQL is turned off. Not initializing table " + tableName);
                }
            }
        }.runTaskAsynchronously(core);
    }

    // check if mysql is enabled in the config
    public boolean isMySQLEnabled() {
        MariaConfig mariaConfig = new MariaConfig(core);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mariaConfig.getMariaFile());
        return Boolean.parseBoolean(config.getString("mariadb.enable"));
    }

    // close the hikari connection
    public void closeHikari() {
        if (isMySQLEnabled()) {
            hikariDataSource.close();
        }
    }
}
