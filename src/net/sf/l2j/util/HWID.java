package net.sf.l2j.util;

import net.sf.l2j.commons.math.MathUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HWID {
    private static final Logger _log = Logger.getLogger(HWID.class.getName());

    static {
        (new File("log/Player Log/HwidLog")).mkdirs();
    }

    public static void auditGMAction(String gmName, String action, String params) {
        File file = new File("log/Player Log/HwidLog/" + gmName + ".txt");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        try {
            FileWriter save = new FileWriter(file, true);
            try {
                save.write(MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >>> HWID: [" + MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + "] >>> Jogador  [" + gmName + "]\r\n");
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
            _log.log(Level.SEVERE, "HwidLog for Player " + gmName + " could not be saved: ", e);
        }
    }

    public static void auditGMAction(String gmName, String action) {
        auditGMAction(gmName, action, "");
    }
}
