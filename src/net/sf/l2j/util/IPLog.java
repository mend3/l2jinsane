package net.sf.l2j.util;

import net.sf.l2j.commons.math.MathUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IPLog {
    private static final Logger _log = Logger.getLogger(IPLog.class.getName());

    static {
        (new File("log/Player Log/IPLog")).mkdirs();
    }

    public static void auditGMAction(String gmName, String action, String Hwid, String params) {
        File file = new File("log/Player Log/IPLog/" + gmName + ".txt");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException iOException) {
            }
        try {
            FileWriter save = new FileWriter(file, true);
            try {
                save.write(MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >> IP: [" + MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + "] >> HWID: [" + action + "]\r\n");
                save.close();
            } catch (Throwable throwable) {
                try {
                    save.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException e) {
            _log.log(Level.SEVERE, "IPLog for Player " + gmName + " could not be saved: ", e);
        }
    }

    public static void auditGMAction(String gmName, String action, String Hwid) {
        auditGMAction(gmName, action, Hwid, "");
    }
}
