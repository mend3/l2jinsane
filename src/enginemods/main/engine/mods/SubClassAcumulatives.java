package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SubClassAcumulatives extends AbstractMods {
    private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level,class_index FROM character_skills WHERE char_obj_id=?";

    public SubClassAcumulatives() {
        this.registerMod(ConfigData.ENABLE_SubClassAcumulatives);
    }

    public static void getInstance() {
    }

    public void onModState() {
    }

    public void onRestoreSkills(Player player) {
        Map<Integer, Integer> skills = new HashMap<>();

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR)
        ) {
            ps.setInt(1, player.getObjectId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("skill_id");
                    int level = rs.getInt("skill_level");
                    int classIndex = rs.getInt("class_index");
                    if (id <= 9000) {
                        if (player.getClassIndex() != classIndex) {
                            L2Skill skill = SkillTable.getInstance().getInfo(id, level);
                            if (skill == null) {
                                LOG.log(Level.SEVERE, "Skipped null skill Id: " + id + ", Level: " + level + " while restoring player skills for " + player.getName());
                                continue;
                            }

                            if (!ConfigData.ACUMULATIVE_PASIVE_SKILLS && skill.isPassive() || ConfigData.DONT_ACUMULATIVE_SKILLS_ID.contains(id)) {
                                continue;
                            }
                        }

                        skills.put(id, level);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not restore " + player.getName() + " skills:", e);
        }

        for (Map.Entry<Integer, Integer> skillLearn : skills.entrySet()) {
            int id = skillLearn.getKey();
            int level = skillLearn.getValue();
            L2Skill skill = SkillTable.getInstance().getInfo(id, level);
            if (skill == null) {
                LOG.log(Level.SEVERE, "Skipped null skill Id: " + id + ", Level: " + level + " while restoring player skills for " + player.getName());
            } else if (player.getSkillLevel(id) < level) {
                player.addSkill(skill, true);
            }
        }

    }

    private static class SingletonHolder {
        protected static final SubClassAcumulatives INSTANCE = new SubClassAcumulatives();
    }
}
