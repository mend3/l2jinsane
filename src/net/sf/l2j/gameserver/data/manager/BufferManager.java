package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.holder.BuffSkillHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

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
        LOGGER.info("Loaded {} available buffs.", this._availableBuffs.size());

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_SCHEMES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final ArrayList<Integer> schemeList = new ArrayList<>();

                final String[] skills = rs.getString("skills").split(",");
                for (String skill : skills) {
                    // Don't feed the skills list if the list is empty.
                    if (skill.isEmpty())
                        break;

                    final int skillId = Integer.parseInt(skill);

                    // Integrity check to see if the skillId is available as a buff.
                    if (_availableBuffs.containsKey(skillId))
                        schemeList.add(skillId);
                }

                setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load schemes data.", e);
        }
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "category", categoryNode ->
        {
            final String category = parseString(categoryNode.getAttributes(), "type");
            forEach(categoryNode, "buff", buffNode ->
            {
                final NamedNodeMap attrs = buffNode.getAttributes();
                final int skillId = parseInteger(attrs, "id");
                final int skillLvl = parseInteger(attrs, "level", SkillTable.getInstance().getMaxLevel(skillId));
                final int price = parseInteger(attrs, "price", 0);
                final String desc = parseString(attrs, "desc", "");

                _availableBuffs.put(skillId, new BuffSkillHolder(skillId, price, category, desc));
            });
        }));
    }

    public void saveSchemes() {
        StringBuilder sb = new StringBuilder();

        try (Connection con = ConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE buffer_schemes")) {
                ps.execute();
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)")) {
                for (Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> player : this._schemesTable.entrySet()) {
                    for (Map.Entry<String, ArrayList<Integer>> scheme : (player.getValue()).entrySet()) {
                        for (int skillId : scheme.getValue()) {
                            StringUtil.append(sb, new Object[]{skillId, ","});
                        }

                        if (!sb.isEmpty()) {
                            sb.setLength(sb.length() - 1);
                        }

                        ps.setInt(1, (Integer) player.getKey());
                        ps.setString(2, (String) scheme.getKey());
                        ps.setString(3, sb.toString());
                        ps.addBatch();
                        sb.setLength(0);
                    }
                }

                ps.executeBatch();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save schemes data.", e);
        }

    }

    public void setScheme(int playerId, String schemeName, ArrayList<Integer> list) {
        if (!this._schemesTable.containsKey(playerId)) {
            this._schemesTable.put(playerId, new HashMap<>());
        } else if ((this._schemesTable.get(playerId)).size() >= Config.BUFFER_MAX_SCHEMES) {
            return;
        }

        (this._schemesTable.get(playerId)).put(schemeName, list);
    }

    public Map<String, ArrayList<Integer>> getPlayerSchemes(int playerId) {
        return this._schemesTable.get(playerId);
    }

    public List<Integer> getScheme(int playerId, String schemeName) {
        return this._schemesTable.get(playerId) != null && (this._schemesTable.get(playerId)).get(schemeName) != null ? (this._schemesTable.get(playerId)).get(schemeName) : Collections.emptyList();
    }

    public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId) {
        List<Integer> skills = this.getScheme(playerId, schemeName);
        if (skills.isEmpty()) {
            return false;
        } else {
            for (int id : skills) {
                if (id == skillId) {
                    return true;
                }
            }

            return false;
        }
    }

    public List<Integer> getSkillsIdsByType(String groupType) {
        List<Integer> skills = new ArrayList<>();

        for (BuffSkillHolder skill : this._availableBuffs.values()) {
            if (skill.getType().equalsIgnoreCase(groupType)) {
                skills.add(skill.getId());
            }
        }

        return skills;
    }

    public List<String> getSkillTypes() {
        List<String> skillTypes = new ArrayList<>();

        for (BuffSkillHolder skill : this._availableBuffs.values()) {
            if (!skillTypes.contains(skill.getType())) {
                skillTypes.add(skill.getType());
            }
        }

        return skillTypes;
    }

    public BuffSkillHolder getAvailableBuff(int skillId) {
        return this._availableBuffs.get(skillId);
    }

    public Map<Integer, BuffSkillHolder> getAvailableBuffs() {
        return this._availableBuffs;
    }

    private static class SingletonHolder {
        protected static final BufferManager INSTANCE = new BufferManager();
    }
}
