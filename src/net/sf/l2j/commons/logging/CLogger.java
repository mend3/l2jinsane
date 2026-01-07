/**/
package net.sf.l2j.commons.logging;

import net.sf.l2j.commons.lang.StringReplacer;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class CLogger {
    private final Logger _logger;

    public CLogger(String name) {
        this._logger = Logger.getLogger(name);
    }

    private static String format(String message, Object... args) {
        if (args != null && args.length != 0) {
            StringReplacer sr = new StringReplacer(message);
            sr.replaceAll(args);
            return sr.toString();
        } else {
            return message;
        }
    }

    private void log0(Level level, StackTraceElement caller, Object message, Throwable exception) {
        if (this._logger.isLoggable(level)) {
            if (caller == null) {
                caller = (new Throwable()).getStackTrace()[2];
            }

            this._logger.logp(level, caller.getClassName(), caller.getMethodName(), String.valueOf(message), exception);
        }
    }

    private void log0(Level level, StackTraceElement caller, Object message, Throwable exception, Object... args) {
        if (this._logger.isLoggable(level)) {
            if (caller == null) {
                caller = (new Throwable()).getStackTrace()[2];
            }

            this._logger.logp(level, caller.getClassName(), caller.getMethodName(), format(String.valueOf(message), args), exception);
        }
    }

    public void log(LogRecord record) {
        this._logger.log(record);
    }

    public void debug(Object message) {
        this.log0(Level.FINE, null, message, null);
    }

    public void debug(Object message, Object... args) {
        this.log0(Level.FINE, null, message, null, args);
    }

    public void debug(Object message, Throwable exception) {
        this.log0(Level.FINE, null, message, exception);
    }

    public void debug(Object message, Throwable exception, Object... args) {
        this.log0(Level.FINE, null, message, exception, args);
    }

    public void info(Object message) {
        this.log0(Level.INFO, null, message, null);
    }

    public void info(Object message, Object... args) {
        this.log0(Level.INFO, null, message, null, args);
    }

    public void info(Object message, Throwable exception) {
        this.log0(Level.INFO, null, message, exception);
    }

    public void info(Object message, Throwable exception, Object... args) {
        this.log0(Level.INFO, null, message, exception, args);
    }

    public void warn(Object message) {
        this.log0(Level.WARNING, null, message, null);
    }

    public void warn(Object message, Object... args) {
        this.log0(Level.WARNING, null, message, null, args);
    }

    public void warn(Object message, Throwable exception) {
        this.log0(Level.WARNING, null, message, exception);
    }

    public void warn(Object message, Throwable exception, Object... args) {
        this.log0(Level.WARNING, null, message, exception, args);
    }

    public void error(Object message) {
        this.log0(Level.SEVERE, null, message, null);
    }

    public void error(Object message, Object... args) {
        this.log0(Level.SEVERE, null, message, null, args);
    }

    public void error(Object message, Throwable exception) {
        this.log0(Level.SEVERE, null, message, exception);
    }

    public void error(Object message, Throwable exception, Object... args) {
        this.log0(Level.SEVERE, null, message, exception, args);
    }
}