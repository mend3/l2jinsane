/**/
package net.sf.l2j.gameserver.data.manager;

import mods.balancer.holder.SkillBalanceHolder;
import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.enums.skills.SkillChangeType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
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

public class SkillBalanceManager implements IXmlReader {
    private static final Logger _log = Logger.getLogger(SkillBalanceManager.class.getName());
    private final Map<String, SkillBalanceHolder> _skills = new ConcurrentHashMap<>();
    private int _balanceSize = 0;
    private int _olyBalanceSize = 0;
    private boolean _hasModify = false;

    public SkillBalanceManager() {
    }

    public static SkillBalanceManager getInstance() {
        return SkillBalanceManager.SingletonHolder._instance;
    }

    public void load() {
        this.parseFile("./data/xml/balancer/skillbalance/SkillBalance.xml");
        _log.info(getClass().getSimpleName() + ": Loaded " + this._skills.size() + " balanced skill(s).");
    }

    public void parseDocument(Document doc, Path path) {
        this._balanceSize = 0;
        this._olyBalanceSize = 0;
        this._hasModify = false;

        for (Node o = doc.getFirstChild(); o != null; o = o.getNextSibling()) {
            if ("list".equalsIgnoreCase(o.getNodeName())) {
                for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if (d.getNodeName().equals("balance")) {
                        int skillId = Integer.parseInt(d.getAttributes().getNamedItem("skillId").getNodeValue());
                        int target = Integer.parseInt(d.getAttributes().getNamedItem("target").getNodeValue());
                        SkillBalanceHolder cbh = new SkillBalanceHolder(skillId, target);

                        for (Node set = d.getFirstChild(); set != null; set = set.getNextSibling()) {
                            double val;
                            SkillChangeType atkType;
                            if (set.getNodeName().equals("set")) {
                                val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                                atkType = SkillChangeType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                                cbh.addSkillBalance(atkType, val);
                                ++this._balanceSize;
                            } else if (set.getNodeName().equals("olyset")) {
                                val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
                                atkType = SkillChangeType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
                                cbh.addOlySkillBalance(atkType, val);
                                ++this._olyBalanceSize;
                            }
                        }

                        this._skills.put(skillId + ";" + target, cbh);
                    }
                }
            }
        }

    }

    public void removeSkillBalance(String key, SkillChangeType type, boolean isOly) {
        if (!this._hasModify) {
            this._hasModify = true;
        }

        if (this._skills.containsKey(key)) {
            if (isOly) {
                this._skills.get(key).removeOly(type);
                --this._olyBalanceSize;
                return;
            }

            this._skills.get(key).remove(type);
            this._skills.remove(key);
            --this._balanceSize;
        }

    }

    public void addSkillBalance(String skill, SkillBalanceHolder sbh, boolean isEdit) {
        if (!this._hasModify) {
            this._hasModify = true;
        }

        this._skills.put(skill, sbh);
        if (!isEdit) {
            if (!sbh.getOlyBalance().isEmpty()) {
                ++this._olyBalanceSize;
            } else {
                ++this._balanceSize;
            }
        }

    }

    public Map<String, SkillBalanceHolder> getAllBalances() {
        Map<String, SkillBalanceHolder> map = new TreeMap<>(new SkillComparator());
        map.putAll(this._skills);
        return map;
    }

    public List<SkillBalanceHolder> getSkillBalances(int skillId) {
        List<SkillBalanceHolder> list = new ArrayList<>();

        for (Entry<String, SkillBalanceHolder> data : this._skills.entrySet()) {
            if (Integer.parseInt(data.getKey().split(";")[0]) == skillId) {
                list.add(data.getValue());
            }
        }

        return list;
    }

    public int getSkillBalanceSize(int skillId, boolean olysize) {
        int size = 0;

        SkillBalanceHolder data;
        for (Iterator<SkillBalanceHolder> var4 = this.getSkillBalances(skillId).iterator(); var4.hasNext(); size += !olysize ? data.getNormalBalance().size() : data.getOlyBalance().size()) {
            data = var4.next();
        }

        return size;
    }

    public double getSkillValue(String sk, SkillChangeType sct, Creature victim) {
        if (Config.BALANCER_ALLOW) {
            Map<String, SkillBalanceHolder> var10000 = this._skills;
            String[] var10001 = sk.split(";");
            String[] var4;
            if (var10000.containsKey(var10001[0] + ";-3")) {
                var4 = sk.split(";");
                sk = var4[0] + ";-3";
                return this._skills.get(sk).getValue(sct);
            }

            if (!this._skills.containsKey(sk)) {
                var10001 = sk.split(";");
                if (!var10000.containsKey(var10001[0] + ";-2")) {
                    return 1.0D;
                }
            }

            if (!sk.split(";")[1].equals("-2") && !this._skills.containsKey(sk)) {
                var4 = sk.split(";");
                sk = var4[0] + ";-2";
            }

            if (victim != null || sct.isForceCheck()) {
                if (victim instanceof Player && victim.getActingPlayer().isOlympiadStart() && victim.getActingPlayer().getOlympiadGameId() != -1 && this._skills.containsKey(sk)) {
                    return this._skills.get(sk).getOlyBalanceValue(sct);
                }

                return this._skills.get(sk).getValue(sct);
            }
        }

        return 1.0D;
    }

    public int getSize(boolean olysize) {
        return olysize ? this._olyBalanceSize : this._balanceSize;
    }

    public SkillBalanceHolder getSkillHolder(String key) {
        return this._skills.get(key);
    }

    public void store(Player player) {
        if (!this._hasModify) {
            if (player != null) {
                player.sendMessage("SkillBalanceManager: Nothing for saving!");
            }

        } else {
            try {
                File file = new File("./data/xml/balancer/skillbalance/SkillBalance.xml");
                if (file.exists()) {
                    SimpleDateFormat var10003 = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    if (!file.renameTo(new File("./data/xml/balancer/skillbalance/SkillBalance_Backup_[" + var10003.format(Calendar.getInstance().getTimeInMillis()) + "].xml")) && player != null) {
                        player.sendMessage("SkillBalanceManager: can't save backup file!");
                    }
                }

                file = new File("./data/xml/balancer/skillbalance/SkillBalance.xml");
                file.createNewFile();
                FileWriter fstream = new FileWriter(file);

                BufferedWriter out = new BufferedWriter(fstream);

                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                out.write("<list>\n");
                Iterator<SkillBalanceHolder> var5 = this._skills.values().iterator();

                label83:
                while (true) {
                    SkillBalanceHolder cbh;
                    do {
                        if (!var5.hasNext()) {
                            break label83;
                        }

                        cbh = var5.next();
                    } while (cbh.getNormalBalance().isEmpty() && cbh.getOlyBalance().isEmpty());

                    int skillId = cbh.getSkillId();
                    String xml = "\t<balance skillId=\"" + skillId + "\" target=\"" + cbh.getTarget() + "\">\n";

                    Iterator<Entry<SkillChangeType, Double>> var8;
                    Entry<SkillChangeType, Double> info;
                    for (var8 = cbh.getNormalBalance().entrySet().iterator(); var8.hasNext(); xml = xml + "\t\t<set type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n") {
                        info = var8.next();
                    }

                    for (var8 = cbh.getOlyBalance().entrySet().iterator(); var8.hasNext(); xml = xml + "\t\t<olyset type=\"" + info.getKey().toString() + "\" val=\"" + info.getValue() + "\"/>\n") {
                        info = var8.next();
                    }

                    xml = xml + "\t</balance>\n";
                    out.write(xml);
                    out.write("</list>");
                }

                out.close();

                fstream.close();
            } catch (Exception var14) {
                var14.printStackTrace();
            }

            if (player != null) {
                player.sendMessage("SkillBalanceManager: Modified data was saved!");
            }

            this._hasModify = false;
        }
    }

    private static class SingletonHolder {
        protected static final SkillBalanceManager _instance = new SkillBalanceManager();
    }

    private static class SkillComparator implements Comparator<String> {
        public SkillComparator() {
        }

        public int compare(String l, String r) {
            int left = Integer.parseInt(l.split(";")[0]);
            int right = Integer.parseInt(r.split(";")[0]);
            if (left > right) {
                return 1;
            } else if (left < right) {
                return -1;
            }
            return Integer.compare(Integer.parseInt(l.split(";")[1]), Integer.parseInt(r.split(";")[1]));
        }
    }
}