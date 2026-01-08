/**/
package net.sf.l2j.gameserver.data.cache;

import net.sf.l2j.commons.logging.CLogger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class CrestCache {
    private static final CLogger LOGGER = new CLogger(CrestCache.class.getName());
    private static final String CRESTS_DIR = "./data/crests/";
    private final Map<Integer, byte[]> _crests = new HashMap<>();
    private final FileFilter _ddsFilter = new DdsFilter(this);

    public CrestCache() {
    }

    public static CrestCache getInstance() {
        return CrestCache.SingletonHolder.INSTANCE;
    }

    public final void load() {
        File[] var1 = (new File("./data/crests/")).listFiles();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            File file = var1[var3];
            String fileName = file.getName();
            if (!this._ddsFilter.accept(file)) {
                file.delete();
                LOGGER.warn("Invalid file {} has been deleted while loading crests.", fileName);
            } else {
                byte[] data;
                try {
                    RandomAccessFile f = new RandomAccessFile(file, "r");

                    try {
                        data = new byte[(int) f.length()];
                        f.readFully(data);
                    } catch (Throwable var12) {
                        try {
                            f.close();
                        } catch (Throwable var11) {
                            var12.addSuppressed(var11);
                        }

                        throw var12;
                    }

                    f.close();
                } catch (Exception var13) {
                    LOGGER.error("Error loading crest file: {}.", var13, fileName);
                    continue;
                }

                CrestCache.CrestType[] var14 = CrestCache.CrestType.values();
                int var8 = var14.length;

                for (int var9 = 0; var9 < var8; ++var9) {
                    CrestCache.CrestType type = var14[var9];
                    if (fileName.startsWith(type.getPrefix())) {
                        if (data.length != type.getSize()) {
                            file.delete();
                            LOGGER.warn("The data for crest {} is invalid. The crest has been deleted.", fileName);
                        } else {
                            this._crests.put(Integer.valueOf(fileName.substring(type.getPrefix().length(), fileName.length() - 4)), data);
                        }
                    }
                }
            }
        }

        LOGGER.info("Loaded {} crests.", this._crests.size());
    }

    public final void reload() {
        this._crests.clear();
        this.load();
    }

    public final byte[] getCrest(CrestCache.CrestType type, int id) {
        byte[] data = this._crests.get(id);
        return data != null && data.length == type.getSize() ? data : null;
    }

    public final void removeCrest(CrestCache.CrestType type, int id) {
        byte[] data = this._crests.get(id);
        if (data != null && data.length == type.getSize()) {
            this._crests.remove(id);
            String var10002 = type.getPrefix();
            File file = new File("./data/crests/" + var10002 + id + ".dds");
            if (!file.delete()) {
                LOGGER.warn("Error deleting crest file: {}.", file.getName());
            }

        }
    }

    public final boolean saveCrest(CrestCache.CrestType type, int id, byte[] data) {
        String var10002 = type.getPrefix();
        File file = new File("./data/crests/" + var10002 + id + ".dds");
        if (data.length != type.getSize()) {
            LOGGER.warn("The data for crest {} is invalid. Saving process is aborted.", file.getName());
            return false;
        } else {
            try {
                FileOutputStream out = new FileOutputStream(file);

                try {
                    out.write(data);
                } catch (Throwable var9) {
                    try {
                        out.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                out.close();
            } catch (Exception var10) {
                LOGGER.error("Error saving crest file: {}.", var10, file.getName());
                return false;
            }

            this._crests.put(id, data);
            return true;
        }
    }

    public enum CrestType {
        PLEDGE("Crest_", 256),
        PLEDGE_LARGE("LargeCrest_", 2176),
        ALLY("AllyCrest_", 192);

        private final String _prefix;
        private final int _size;

        CrestType(String prefix, int size) {
            this._prefix = prefix;
            this._size = size;
        }

        // $FF: synthetic method
        private static CrestCache.CrestType[] $values() {
            return new CrestCache.CrestType[]{PLEDGE, PLEDGE_LARGE, ALLY};
        }

        public final String getPrefix() {
            return this._prefix;
        }

        public final int getSize() {
            return this._size;
        }
    }

    private static class SingletonHolder {
        protected static final CrestCache INSTANCE = new CrestCache();
    }

    protected static class DdsFilter implements FileFilter {
        protected DdsFilter(final CrestCache param1) {
        }

        public boolean accept(File file) {
            String fileName = file.getName();
            return (fileName.startsWith("Crest_") || fileName.startsWith("LargeCrest_") || fileName.startsWith("AllyCrest_")) && fileName.endsWith(".dds");
        }
    }
}