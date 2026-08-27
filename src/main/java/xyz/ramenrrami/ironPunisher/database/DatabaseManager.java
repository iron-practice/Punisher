package xyz.ramenrrami.ironPunisher.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import xyz.ramenrrami.ironPunisher.IronPunisher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private final IronPunisher plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(IronPunisher plugin) { this.plugin = plugin; }

    public void connect() {
        String host = plugin.getConfig().getString("database.host", "127.0.0.1");
        int port = plugin.getConfig().getInt("database.port", 3306);
        String database = plugin.getConfig().getString("database.name", "ironpunisher");
        String username = plugin.getConfig().getString("database.username", "root");
        String password = plugin.getConfig().getString("database.password", "");
        boolean ssl = plugin.getConfig().getBoolean("database.ssl", false);
        int maximumPoolSize = Math.max(2, plugin.getConfig().getInt("database.pool-size", 10));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + ssl + "&serverTimezone=UTC&characterEncoding=utf8mb4&useUnicode=true");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(Math.min(2, maximumPoolSize));
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(3000);
        config.setInitializationFailTimeout(5000);
        config.setPoolName("IronPunisher-Pool");

        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS punishments (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    uuid CHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    reason VARCHAR(255) NOT NULL,
                    punishment_type VARCHAR(32) NOT NULL DEFAULT 'BAN',
                    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                    expires_at TIMESTAMP(3) NULL,
                    created_by VARCHAR(64) NOT NULL,
                    server_id VARCHAR(64) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    PRIMARY KEY (id),
                    INDEX idx_uuid_active (uuid, active),
                    INDEX idx_active_created (active, created_at),
                    INDEX idx_expires_at (expires_at),
                    INDEX idx_server_created (server_id, created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            close();
            throw new IllegalStateException("Could not create the punishments table", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) throw new SQLException("Database is not connected");
        return dataSource.getConnection();
    }

    public boolean isConnected() { return dataSource != null && !dataSource.isClosed(); }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }
}
