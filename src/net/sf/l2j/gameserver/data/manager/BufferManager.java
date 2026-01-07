package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.holder.BuffSkillHolder;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BufferManager implements IXmlReader {
    private static final String LOAD_SCHEMES = "SELECT * FROM buffer_schemes";

    private static final String DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes";

    private static final String INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";

    private final Map<Integer, HashMap<String, ArrayList<Integer>>> _schemesTable = new ConcurrentHashMap<>();

    private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();

    protected BufferManager() {
    }

    public static BufferManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        parseFile("./data/xml/bufferSkills.xml");
        LOGGER.info("Loaded {} available buffs.", Integer.valueOf(this._availableBuffs.size()));
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM buffer_schemes");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            ArrayList<Integer> schemeList = new ArrayList<>();
                            String[] skills = rs.getString("skills").split(",");
                            for (String skill : skills) {
                                if (skill.isEmpty())
                                    break;
                                int skillId = Integer.valueOf(skill);
                                if (this._availableBuffs.containsKey(Integer.valueOf(skillId)))
                                    schemeList.add(Integer.valueOf(skillId));
                            }
                            setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList);
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
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
            LOGGER.error("Failed to load schemes data.", e);
        }
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "category", nnn -> {
        }));
    }

    public void saveSchemes() {
        StringBuilder sb = new StringBuilder();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE buffer_schemes");
                try {
                    ps.execute();
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
                ps = con.prepareStatement("INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)");
                try {
                    for (Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> player : this._schemesTable.entrySet()) {
                        for (Map.Entry<String, ArrayList<Integer>> scheme : (Iterable<Map.Entry<String, ArrayList<Integer>>>) ((HashMap) player.getValue()).entrySet()) {
                            for (Iterator<Integer> iterator = ((ArrayList) scheme.getValue()).iterator(); iterator.hasNext(); ) {
                                int skillId = iterator.next();
                                StringUtil.append(sb, Integer.valueOf(skillId), ",");
                            }
                            if (sb.length() > 0)
                                sb.setLength(sb.length() - 1);
                            ps.setInt(1, (Integer) player.getKey());
                            ps.setString(2, scheme.getKey());
                            ps.setString(3, sb.toString());
                            ps.addBatch();
                            sb.setLength(0);
                        }
                    }
                    ps.executeBatch();
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
            LOGGER.error("Failed to save schemes data.", e);
        }
    }

    public void setScheme(int playerId, String schemeName, ArrayList<Integer> list) {
        if (!this._schemesTable.containsKey(Integer.valueOf(playerId))) {
            this._schemesTable.put(Integer.valueOf(playerId), new HashMap<>());
        } else if (this._schemesTable.get(Integer.valueOf(playerId)).size() >= Config.BUFFER_MAX_SCHEMES) {
            return;
        }
        this._schemesTable.get(Integer.valueOf(playerId)).put(schemeName, list);
    }

    public Map<String, ArrayList<Integer>> getPlayerSchemes(int playerId) {
        return this._schemesTable.get(Integer.valueOf(playerId));
    }

    public List<Integer> getScheme(int playerId, String schemeName) {
        if (this._schemesTable.get(Integer.valueOf(playerId)) == null || ((HashMap) this._schemesTable.get(Integer.valueOf(playerId))).get(schemeName) == null)
            return Collections.emptyList();
        return (List<Integer>) ((HashMap) this._schemesTable.get(Integer.valueOf(playerId))).get(schemeName);
    }

    public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId) {
        List<Integer> skills = getScheme(playerId, schemeName);
        if (skills.isEmpty())
            return false;
        for (Iterator<Integer> iterator = skills.iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            if (id == skillId)
                return true;
        }
        return false;
    }

    public List<Integer> getSkillsIdsByType(String groupType) {
        List<Integer> skills = new ArrayList<>();
        for (BuffSkillHolder skill : this._availableBuffs.values()) {
            if (skill.getType().equalsIgnoreCase(groupType))
                skills.add(Integer.valueOf(skill.getId()));
        }
        return skills;
    }

    public List<String> getSkillTypes() {
        List<String> skillTypes = new ArrayList<>();
        for (BuffSkillHolder skill : this._availableBuffs.values()) {
            if (!skillTypes.contains(skill.getType()))
                skillTypes.add(skill.getType());
        }
        return skillTypes;
    }

    public BuffSkillHolder getAvailableBuff(int skillId) {
        return this._availableBuffs.get(Integer.valueOf(skillId));
    }

    public Map<Integer, BuffSkillHolder> getAvailableBuffs() {
        return this._availableBuffs;
    }

    private static class SingletonHolder {
        protected static final BufferManager INSTANCE = new BufferManager();
    }
}
