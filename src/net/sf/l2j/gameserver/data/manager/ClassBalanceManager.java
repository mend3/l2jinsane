/**/
package net.sf.l2j.gameserver.data.manager;

import mods.balancer.holder.ClassBalanceHolder;
import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.enums.AttackType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ClassBalanceManager implements IXmlReader {
    private static final Logger _log = Logger.getLogger(ClassBalanceManager.class.getName());
    private final Map<String, ClassBalanceHolder> _classes = new ConcurrentHashMap();
    private int _balanceSize = 0;
    private int _olyBalanceSize = 0;
    private boolean _hasModify = false;

    public ClassBalanceManager() {
    }

    public static ClassBalanceManager getInstance() {
        return ClassBalanceManager.SingletonHolder._instance;
    }

    public void load() {
        this.parseFile("./data/xml/balancer/classbalance/ClassBalance.xml");
        _log.info(getClass().getSimpleName() + ": Loaded " + this._classes.size() + " balanced classe(s).");
    }

    public void parseDocument(Document doc, Path path) {
        for (Node o = doc.getFirstChild(); o != null; o = o.getNextSibling()) {
            if ("list".equalsIgnoreCase(o.getNodeName())) {
                for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if (d.getNodeName().equals("balance")) {
                        int classId = Integer.parseInt(d.getAttributes().getNamedItem("classId").getNodeValue());
                        int targetClassId = Integer.parseInt(d.getAttributes().getNamedItem("targetClassId").getNodeValue());
                        ClassBalanceHolder cbh = new ClassBalanceHolder(classId, targetClassId);

                        for (Node set = d.getFirstChild(); set != null; set = set.getNextSibling()) {
                            double val;
                            AttackType atkType;
                            if (set.getNodeName().equals("set")) {
                                val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                                atkType = AttackType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                                cbh.addNormalBalance(atkType, val);
                                ++this._balanceSize;
                            } else if (set.getNodeName().equals("olyset")) {
                                val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                                atkType = AttackType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                                cbh.addOlyBalance(atkType, val);
                                ++this._olyBalanceSize;
                            }
                        }

                        this._classes.put(classId + ";" + targetClassId, cbh);
                    }
                }
            }
        }

    }

    public Map<String, ClassBalanceHolder> getAllBalances() {
        Map<String, ClassBalanceHolder> map = new TreeMap(new ClassComparator(this));
        map.putAll(this._classes);
        return map;
    }

    public List<ClassBalanceHolder> getClassBalances(int classId) {
        List<ClassBalanceHolder> list = new ArrayList();
        Iterator var3 = this._classes.entrySet().iterator();

        while (var3.hasNext()) {
            Entry<String, ClassBalanceHolder> data = (Entry) var3.next();
            if (Integer.parseInt(data.getKey().split(";")[0]) == classId) {
                list.add(data.getValue());
            }
        }

        return list;
    }

    public int getClassBalanceSize(int classId, boolean olysize) {
        int size = 0;

        ClassBalanceHolder data;
        for (Iterator var4 = this.getClassBalances(classId).iterator(); var4.hasNext(); size += !olysize ? data.getNormalBalance().size() : data.getOlyBalance().size()) {
            data = (ClassBalanceHolder) var4.next();
        }

        return size;
    }

    public ClassBalanceHolder getBalanceHolder(String key) {
        return this._classes.get(key);
    }

    public double getBalancedClass(AttackType type, Creature attacker, Creature victim) {
        if (Config.BALANCER_ALLOW) {
            int classId;
            if (attacker instanceof Player && victim instanceof Player) {
                classId = attacker.getActingPlayer().getClassId().getId();
                if (this._classes.containsKey(classId + ";-3")) {
                    return this._classes.get(classId + ";-3").getBalanceValue(type);
                }

                int targetClassId = victim.getActingPlayer().getClassId().getId();
                if (attacker.getActingPlayer().isInOlympiadMode() && victim.getActingPlayer().isInOlympiadMode()) {
                    if (attacker.getActingPlayer().getOlympiadGameId() == victim.getActingPlayer().getOlympiadGameId() && this._classes.containsKey(classId + ";" + targetClassId)) {
                        return this._classes.get(classId + ";" + targetClassId).getOlyBalanceValue(type);
                    }

                    return this._classes.containsKey(classId + ";-2") ? this._classes.get(classId + ";-2").getOlyBalanceValue(type) : 1.0D;
                }

                if (this._classes.containsKey(classId + ";" + targetClassId)) {
                    return this._classes.get(classId + ";" + targetClassId).getBalanceValue(type);
                }

                return this._classes.containsKey(classId + ";-2") ? this._classes.get(classId + ";-2").getBalanceValue(type) : 1.0D;
            }

            if (attacker instanceof Player && victim instanceof Monster) {
                classId = attacker.getActingPlayer().getClassId().getId();
                if (this._classes.containsKey(classId + ";-1")) {
                    return this._classes.get(classId + ";-1").getBalanceValue(type);
                }
            }
        }

        return 1.0D;
    }

    public void removeClassBalance(String key, AttackType type, boolean isOly) {
        if (this._classes.containsKey(key)) {
            if (!this._hasModify) {
                this._hasModify = true;
            }

            if (isOly) {
                this._classes.get(key).removeOlyBalance(type);
                --this._olyBalanceSize;
                return;
            }

            this._classes.get(key).remove(type);
            --this._balanceSize;
        }

    }

    public void addClassBalance(String key, ClassBalanceHolder cbh, boolean isEdit) {
        if (!this._hasModify) {
            this._hasModify = true;
        }

        this._classes.put(key, cbh);
        if (!isEdit) {
            if (!cbh.getOlyBalance().isEmpty()) {
                ++this._olyBalanceSize;
            } else {
                ++this._balanceSize;
            }
        }

    }

    public void store(Player player) {
        if (!this._hasModify) {
            if (player != null) {
                player.sendMessage("ClassBalanceManager: Nothing for saving!");
            }

        } else {
            try {
                File file = new File("./data/xml/balancer/classbalance/ClassBalance.xml");
                if (file.exists()) {
                    SimpleDateFormat var10003 = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss");
                    if (!file.renameTo(new File("./data/xml/balancer/classbalance/ClassBalance_Backup_[" + var10003.format(Calendar.getInstance().getTimeInMillis()) + "].xml")) && player != null) {
                        player.sendMessage("ClassBalanceManager: can't save backup file!");
                    }
                }

                file = new File("./data/xml/balancer/classbalance/ClassBalance.xml");
                file.createNewFile();
                FileWriter fstream = new FileWriter(file);

                try {
                    BufferedWriter out = new BufferedWriter(fstream);

                    try {
                        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                        out.write("<list>\n");
                        Iterator var5 = this._classes.values().iterator();

                        label84:
                        while (true) {
                            ClassBalanceHolder cbh;
                            do {
                                if (!var5.hasNext()) {
                                    out.write("</list>");
                                    break label84;
                                }

                                cbh = (ClassBalanceHolder) var5.next();
                            } while (cbh.getNormalBalance().isEmpty() && cbh.getOlyBalance().isEmpty());

                            int var10000 = cbh.getActiveClass();
                            String xml = "\t<balance classId=\"" + var10000 + "\" targetClassId=\"" + cbh.getTargetClass() + "\">\n";

                            Iterator var8;
                            Entry info;
                            for (var8 = cbh.getNormalBalance().entrySet().iterator(); var8.hasNext(); xml = xml + "\t\t<set type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n") {
                                info = (Entry) var8.next();
                            }

                            for (var8 = cbh.getOlyBalance().entrySet().iterator(); var8.hasNext(); xml = xml + "\t\t<olyset type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n") {
                                info = (Entry) var8.next();
                            }

                            xml = xml + "\t</balance>\n";
                            out.write(xml);
                        }
                    } catch (Throwable var12) {
                        try {
                            out.close();
                        } catch (Throwable var11) {
                            var12.addSuppressed(var11);
                        }

                        throw var12;
                    }

                    out.close();
                } catch (Throwable var13) {
                    try {
                        fstream.close();
                    } catch (Throwable var10) {
                        var13.addSuppressed(var10);
                    }

                    throw var13;
                }

                fstream.close();
            } catch (Exception var14) {
                var14.printStackTrace();
            }

            if (player != null) {
                player.sendMessage("ClassBalanceManager: Modified data was saved!");
            }

            this._hasModify = false;
        }
    }

    public int getSize(boolean olysize) {
        return olysize ? this._olyBalanceSize : this._balanceSize;
    }

    private static class SingletonHolder {
        protected static final ClassBalanceManager _instance = new ClassBalanceManager();
    }

    private static class ClassComparator implements Comparator<String> {
        public ClassComparator(final ClassBalanceManager param1) {
        }

        public int compare(String l, String r) {
            int left = Integer.parseInt(l.split(";")[0]);
            int right = Integer.parseInt(r.split(";")[0]);
            if (left > right) {
                return 1;
            } else if (left < right) {
                return -1;
            } else if (Integer.parseInt(l.split(";")[1]) > Integer.parseInt(r.split(";")[1])) {
                return 1;
            } else if (Integer.parseInt(r.split(";")[1]) > Integer.parseInt(l.split(";")[1])) {
                return -1;
            } else {
                Random x = new Random();
                return 1;
            }
        }

    }
}