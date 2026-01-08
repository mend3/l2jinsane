package net.sf.l2j.util.variables;

import net.sf.l2j.commons.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MariaDB {
    private static final Logger logger = Logger.getLogger(MariaDB.class.getName());

    private MariaDB() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no puede ser instanciada");
    }

    public static boolean setEx(String query, Object... parameters) {
        Objects.requireNonNull(query, "La consulta no puede ser null");
        try {
            Connection connection = ConnectionPool.getConnection();
            try {
                boolean bool = executeUpdateWithConnection(connection, query, parameters);
                if (connection != null)
                    connection.close();
                return bool;
            } catch (Throwable throwable) {
                if (connection != null)
                    try {
                        connection.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error al ejecutar la consulta: " + query, e);
            return false;
        }
    }

    public static void set(String query, Object... parameters) {
        setEx(null, query, parameters);
    }

    public static boolean set(String query) {
        return setEx(null, query);
    }

    private static boolean executeUpdateWithConnection(Connection connection, String query, Object... parameters) throws SQLException {
        if (parameters.length == 0) {
            Statement statement = connection.createStatement();
            try {
                statement.executeUpdate(query);
                boolean bool = true;
                if (statement != null)
                    statement.close();
                return bool;
            } catch (Throwable throwable) {
                if (statement != null)
                    try {
                        statement.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        }
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        try {
            setVars(preparedStatement, parameters);
            preparedStatement.executeUpdate();
            boolean bool = true;
            if (preparedStatement != null)
                preparedStatement.close();
            return bool;
        } catch (Throwable throwable) {
            if (preparedStatement != null)
                try {
                    preparedStatement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public static void setVars(PreparedStatement statement, Object... parameters) {
        // Byte code:
        //   0: iconst_0
        //   1: istore_2
        //   2: iload_2
        //   3: aload_1
        //   4: arraylength
        //   5: if_icmpge -> 370
        //   8: aload_1
        //   9: iload_2
        //   10: aaload
        //   11: astore_3
        //   12: iload_2
        //   13: iconst_1
        //   14: iadd
        //   15: istore #4
        //   17: aload_3
        //   18: astore #5
        //   20: iconst_0
        //   21: istore #6
        //   23: aload #5
        //   25: iload #6
        //   27: <illegal opcode> typeSwitch : (Ljava/lang/Object;I)I
        //   32: tableswitch default -> 355, -1 -> 92, 0 -> 104, 1 -> 124, 2 -> 147, 3 -> 170, 4 -> 193, 5 -> 216, 6 -> 239, 7 -> 259, 8 -> 279, 9 -> 299
        //   92: aload_0
        //   93: iload #4
        //   95: iconst_0
        //   96: invokeinterface setNull : (II)V
        //   101: goto -> 364
        //   104: aload #5
        //   106: checkcast java/lang/String
        //   109: astore #7
        //   111: aload_0
        //   112: iload #4
        //   114: aload #7
        //   116: invokeinterface setString : (ILjava/lang/String;)V
        //   121: goto -> 364
        //   124: aload #5
        //   126: checkcast java/lang/Integer
        //   129: astore #8
        //   131: aload_0
        //   132: iload #4
        //   134: aload #8
        //   136: invokevirtual intValue : ()I
        //   139: invokeinterface setInt : (II)V
        //   144: goto -> 364
        //   147: aload #5
        //   149: checkcast java/lang/Long
        //   152: astore #9
        //   154: aload_0
        //   155: iload #4
        //   157: aload #9
        //   159: invokevirtual longValue : ()J
        //   162: invokeinterface setLong : (IJ)V
        //   167: goto -> 364
        //   170: aload #5
        //   172: checkcast java/lang/Double
        //   175: astore #10
        //   177: aload_0
        //   178: iload #4
        //   180: aload #10
        //   182: invokevirtual doubleValue : ()D
        //   185: invokeinterface setDouble : (ID)V
        //   190: goto -> 364
        //   193: aload #5
        //   195: checkcast java/lang/Float
        //   198: astore #11
        //   200: aload_0
        //   201: iload #4
        //   203: aload #11
        //   205: invokevirtual floatValue : ()F
        //   208: invokeinterface setFloat : (IF)V
        //   213: goto -> 364
        //   216: aload #5
        //   218: checkcast java/lang/Boolean
        //   221: astore #12
        //   223: aload_0
        //   224: iload #4
        //   226: aload #12
        //   228: invokevirtual booleanValue : ()Z
        //   231: invokeinterface setBoolean : (IZ)V
        //   236: goto -> 364
        //   239: aload #5
        //   241: checkcast java/sql/Date
        //   244: astore #13
        //   246: aload_0
        //   247: iload #4
        //   249: aload #13
        //   251: invokeinterface setDate : (ILjava/sql/Date;)V
        //   256: goto -> 364
        //   259: aload #5
        //   261: checkcast java/sql/Time
        //   264: astore #14
        //   266: aload_0
        //   267: iload #4
        //   269: aload #14
        //   271: invokeinterface setTime : (ILjava/sql/Time;)V
        //   276: goto -> 364
        //   279: aload #5
        //   281: checkcast java/sql/Timestamp
        //   284: astore #15
        //   286: aload_0
        //   287: iload #4
        //   289: aload #15
        //   291: invokeinterface setTimestamp : (ILjava/sql/Timestamp;)V
        //   296: goto -> 364
        //   299: aload #5
        //   301: checkcast java/lang/Number
        //   304: astore #16
        //   306: aload #16
        //   308: invokevirtual longValue : ()J
        //   311: lstore #17
        //   313: aload #16
        //   315: invokevirtual doubleValue : ()D
        //   318: dstore #19
        //   320: lload #17
        //   322: l2d
        //   323: dload #19
        //   325: dcmpl
        //   326: ifne -> 342
        //   329: aload_0
        //   330: iload #4
        //   332: lload #17
        //   334: invokeinterface setLong : (IJ)V
        //   339: goto -> 352
        //   342: aload_0
        //   343: iload #4
        //   345: dload #19
        //   347: invokeinterface setDouble : (ID)V
        //   352: goto -> 364
        //   355: aload_0
        //   356: iload #4
        //   358: aload_3
        //   359: invokeinterface setObject : (ILjava/lang/Object;)V
        //   364: iinc #2, 1
        //   367: goto -> 2
        //   370: return
        // Line number table:
        //   Java source line number -> byte code offset
        //   #106	-> 0
        //   #107	-> 8
        //   #108	-> 12
        //   #110	-> 17
        //   #111	-> 92
        //   #112	-> 104
        //   #113	-> 124
        //   #114	-> 147
        //   #115	-> 170
        //   #116	-> 193
        //   #117	-> 216
        //   #118	-> 239
        //   #119	-> 259
        //   #120	-> 279
        //   #121	-> 299
        //   #123	-> 306
        //   #124	-> 313
        //   #125	-> 320
        //   #126	-> 329
        //   #128	-> 342
        //   #130	-> 352
        //   #131	-> 355
        //   #106	-> 364
        //   #134	-> 370
        // Local variable table:
        //   start	length	slot	name	descriptor
        //   111	13	7	s	Ljava/lang/String;
        //   131	16	8	n	Ljava/lang/Integer;
        //   154	16	9	n	Ljava/lang/Long;
        //   177	16	10	d	Ljava/lang/Double;
        //   200	16	11	f	Ljava/lang/Float;
        //   223	16	12	b	Ljava/lang/Boolean;
        //   246	13	13	d	Ljava/sql/Date;
        //   266	13	14	t	Ljava/sql/Time;
        //   286	13	15	ts	Ljava/sql/Timestamp;
        //   313	39	17	longValue	J
        //   320	32	19	doubleValue	D
        //   306	49	16	n	Ljava/lang/Number;
        //   12	352	3	param	Ljava/lang/Object;
        //   17	347	4	paramIndex	I
        //   2	368	2	i	I
        //   0	371	0	statement	Ljava/sql/PreparedStatement;
        //   0	371	1	parameters	[Ljava/lang/Object;
    }
}
