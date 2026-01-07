package net.sf.l2j.gsregistering;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.loginserver.GameServerManager;
import net.sf.l2j.loginserver.model.GameServerInfo;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Scanner;

public class GameServerRegister {
    private static final String DELETE_SERVER = "DELETE FROM gameservers WHERE server_id=?";

    private static final String DELETE_SERVERS = "DELETE FROM gameservers";

    private static String _choice;

    public static void main(String[] args) {
        Config.loadGameServerRegistration();
        ConnectionPool.init();
        GameServerManager.getInstance();
        Scanner _scn = new Scanner(System.in);
        try {
            System.out.println();
            System.out.println();
            System.out.println("                        aCis gameserver registering");
            System.out.println("                        ____________________________");
            System.out.println();
            System.out.println("OPTIONS : a number : register a server ID, if available and existing on list.");
            System.out.println("          list : get a list of IDs. A '*' means the id is already used.");
            System.out.println("          clean : unregister a specified gameserver.");
            System.out.println("          cleanall : unregister all gameservers.");
            System.out.println("          exit : exit the program.");
            while (true) {
                System.out.println();
                System.out.print("Your choice? ");
                _choice = _scn.next();
                if (_choice.equalsIgnoreCase("list")) {
                    System.out.println();
                    for (Map.Entry<Integer, String> entry : GameServerManager.getInstance().getServerNames().entrySet())
                        System.out.println(entry.getKey() + ": " + entry.getKey() + " " + entry.getValue());
                    continue;
                }
                if (_choice.equalsIgnoreCase("clean")) {
                    System.out.println();
                    if (GameServerManager.getInstance().getServerNames().isEmpty()) {
                        System.out.println("No server names available, be sure 'serverNames.xml' is in the LoginServer directory.");
                        continue;
                    }
                    System.out.println("UNREGISTER a specific server. Here's the current list :");
                    for (GameServerInfo entry : GameServerManager.getInstance().getRegisteredGameServers().values())
                        System.out.println(entry.getId() + ": " + entry.getId());
                    System.out.println();
                    System.out.print("Your choice? ");
                    _choice = _scn.next();
                    try {
                        int id = Integer.parseInt(_choice);
                        if (!GameServerManager.getInstance().getRegisteredGameServers().containsKey(Integer.valueOf(id))) {
                            System.out.println("This server id isn't used.");
                            continue;
                        }
                        try {
                            Connection con = ConnectionPool.getConnection();
                            try {
                                PreparedStatement ps = con.prepareStatement("DELETE FROM gameservers WHERE server_id=?");
                                try {
                                    ps.setInt(1, id);
                                    ps.executeUpdate();
                                    if (ps != null)
                                        ps.close();
                                } catch (Throwable throwable) {
                                    if (ps != null)
                                        try {
                                            ps.close();
                                        } catch (Throwable throwable1) {
                                            throwable.addSuppressed(throwable1);
                                        }
                                    throw throwable;
                                }
                                if (con != null)
                                    con.close();
                            } catch (Throwable throwable) {
                                if (con != null)
                                    try {
                                        con.close();
                                    } catch (Throwable throwable1) {
                                        throwable.addSuppressed(throwable1);
                                    }
                                throw throwable;
                            }
                        } catch (Exception e) {
                            System.out.println("SQL error while cleaning registered server: " + e);
                        }
                        GameServerManager.getInstance().getRegisteredGameServers().remove(Integer.valueOf(id));
                        System.out.println("You successfully dropped gameserver #" + id + ".");
                    } catch (NumberFormatException nfe) {
                        System.out.println("Type a valid server id.");
                    }
                    continue;
                }
                if (_choice.equalsIgnoreCase("cleanall")) {
                    System.out.println();
                    System.out.print("UNREGISTER ALL servers. Are you sure? (y/n) ");
                    _choice = _scn.next();
                    if (_choice.equals("y")) {
                        try {
                            Connection con = ConnectionPool.getConnection();
                            try {
                                PreparedStatement ps = con.prepareStatement("DELETE FROM gameservers");
                                try {
                                    ps.executeUpdate();
                                    if (ps != null)
                                        ps.close();
                                } catch (Throwable throwable) {
                                    if (ps != null)
                                        try {
                                            ps.close();
                                        } catch (Throwable throwable1) {
                                            throwable.addSuppressed(throwable1);
                                        }
                                    throw throwable;
                                }
                                if (con != null)
                                    con.close();
                            } catch (Throwable throwable) {
                                if (con != null)
                                    try {
                                        con.close();
                                    } catch (Throwable throwable1) {
                                        throwable.addSuppressed(throwable1);
                                    }
                                throw throwable;
                            }
                        } catch (Exception e) {
                            System.out.println("SQL error while cleaning registered servers: " + e);
                        }
                        GameServerManager.getInstance().getRegisteredGameServers().clear();
                        System.out.println("You successfully dropped all registered gameservers.");
                        continue;
                    }
                    System.out.println("'cleanall' processus has been aborted.");
                    continue;
                }
                if (_choice.equalsIgnoreCase("exit")) {
                    System.exit(0);
                    continue;
                }
                try {
                    System.out.println();
                    if (GameServerManager.getInstance().getServerNames().isEmpty()) {
                        System.out.println("No server names available, be sure 'serverNames.xml' is in the LoginServer directory.");
                        continue;
                    }
                    int id = Integer.parseInt(_choice);
                    if (GameServerManager.getInstance().getServerNames().get(Integer.valueOf(id)) == null) {
                        System.out.println("No name for server id: " + id + ".");
                        continue;
                    }
                    if (GameServerManager.getInstance().getRegisteredGameServers().containsKey(Integer.valueOf(id))) {
                        System.out.println("This server id is already used.");
                        continue;
                    }
                    byte[] hexId = LoginServerThread.generateHex(16);
                    GameServerManager.getInstance().getRegisteredGameServers().put(Integer.valueOf(id), new GameServerInfo(id, hexId));
                    GameServerManager.getInstance().registerServerOnDB(hexId, id, "");
                    Config.saveHexid(id, (new BigInteger(hexId)).toString(16), "hexid(server " + id + ").txt");
                    System.out.println("Server registered under 'hexid(server " + id + ").txt'.");
                    System.out.println("Put this file in /config gameserver folder and rename it 'hexid.txt'.");
                } catch (NumberFormatException nfe) {
                    System.out.println("Type a number or list|clean|cleanall commands.");
                }
            }
        } catch (Throwable throwable) {
            try {
                _scn.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
    }
}
