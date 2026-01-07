package net.sf.l2j.commons.pool;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionPool {
    private static final CLogger LOGGER = new CLogger(ConnectionPool.class.getName());
    private static MariaDbPoolDataSource _source;

    private ConnectionPool() {
        throw new IllegalStateException("Utility class");
    }

    public static void init() {
        try {
//            _source.setDriverClassName("org.mariadb.jdbc.Driver");
            _source = new MariaDbPoolDataSource();
            // Check if username is not empty because the source checks for null only.
            if (!Config.DATABASE_LOGIN.isEmpty()) {
                _source.setUser(Config.DATABASE_LOGIN);
                _source.setPassword(Config.DATABASE_PASSWORD);
            }

//            _source.setJdbcUrl(Config.DATABASE_URL);
//            _source.setIdleTimeout(0L);
//            _source.setMaxLifetime(900000L);
            // Make sure the setUrl is called last as it initializes the pool.
            _source.setUrl(Config.DATABASE_URL);
            _source.getConnection().close();
        } catch (SQLException e) {
            LOGGER.error("Couldn't initialize connection pooler.", e);
        }
        LOGGER.info("Initializing ConnectionPool.");
    }

    public static void shutdown() {
        if (_source != null) {
            _source.close();
            _source = null;
        }
    }

    public static Connection getConnection() throws SQLException {
        return _source.getConnection();
    }
}
