package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.TamedBeast;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeedableBeasts extends L2AttackableAIScript {
    private static final int GOLDEN_SPICE = 6643;

    private static final int CRYSTAL_SPICE = 6644;

    private static final int SKILL_GOLDEN_SPICE = 2188;

    private static final int SKILL_CRYSTAL_SPICE = 2189;

    private static final int[] TAMED_BEASTS = new int[]{16013, 16014, 16015, 16016, 16017, 16018};

    private static final int[] FEEDABLE_BEASTS = new int[]{
            21451, 21452, 21453, 21454, 21455, 21456, 21457, 21458, 21459, 21460,
            21461, 21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, 21470,
            21471, 21472, 21473, 21474, 21475, 21476, 21477, 21478, 21479, 21480,
            21481, 21482, 21483, 21484, 21485, 21486, 21487, 21488, 21489, 21490,
            21491, 21492, 21493, 21494, 21495, 21496, 21497, 21498, 21499, 21500,
            21501, 21502, 21503, 21504, 21505, 21506, 21507, 21824, 21825, 21826,
            21827, 21828, 21829};

    private static final Map<Integer, Integer> MAD_COW_POLYMORPH = new HashMap<>();

    private static final String[][] TEXT = new String[][]{{"What did you just do to me?", "You want to tame me, huh?", "Do not give me this. Perhaps you will be in danger.", "Bah bah. What is this unpalatable thing?", "My belly has been complaining. This hit the spot.", "What is this? Can I eat it?", "You don't need to worry about me.", "Delicious food, thanks.", "I am starting to like you!", "Gulp!"}, {"I do not think you have given up on the idea of taming me.", "That is just food to me. Perhaps I can eat your hand too.", "Will eating this make me fat? Ha ha.", "Why do you always feed me?", "Do not trust me. I may betray you."}, {"Destroy!", "Look what you have done!", "Strange feeling...! Evil intentions grow in my heart...!", "It is happening!", "This is sad...Good is sad...!"}};

    private static final String[] SPAWN_CHATS = new String[]{"$s1, will you show me your hideaway?", "$s1, whenever I look at spice, I think about you.", "$s1, you do not need to return to the village. I will give you strength.", "Thanks, $s1. I hope I can help you.", "$s1, what can I do to help you?"};

    private static final Map<Integer, Integer> FEED_INFO = new ConcurrentHashMap<>();

    private static final Map<Integer, GrowthCapableMob> GROWTH_CAPABLE_MOBS = new HashMap<>();

    public FeedableBeasts() {
        super("ai/group");
        MAD_COW_POLYMORPH.put(21824, 21468);
        MAD_COW_POLYMORPH.put(21825, 21469);
        MAD_COW_POLYMORPH.put(21826, 21487);
        MAD_COW_POLYMORPH.put(21827, 21488);
        MAD_COW_POLYMORPH.put(21828, 21506);
        MAD_COW_POLYMORPH.put(21829, 21507);
        int[][] Kookabura_0_Gold = {{21452, 21453, 21454, 21455}};
        int[][] Kookabura_0_Crystal = {{21456, 21457, 21458, 21459}};
        int[][] Kookabura_1_Gold_1 = {{21460, 21462}};
        int[][] Kookabura_1_Gold_2 = {{21461, 21463}};
        int[][] Kookabura_1_Crystal_1 = {{21464, 21466}};
        int[][] Kookabura_1_Crystal_2 = {{21465, 21467}};
        int[][] Kookabura_2_1 = {{21468, 21824}, {16017, 16018}};
        int[][] Kookabura_2_2 = {{21469, 21825}, {16017, 16018}};
        int[][] Buffalo_0_Gold = {{21471, 21472, 21473, 21474}};
        int[][] Buffalo_0_Crystal = {{21475, 21476, 21477, 21478}};
        int[][] Buffalo_1_Gold_1 = {{21479, 21481}};
        int[][] Buffalo_1_Gold_2 = {{21481, 21482}};
        int[][] Buffalo_1_Crystal_1 = {{21483, 21485}};
        int[][] Buffalo_1_Crystal_2 = {{21484, 21486}};
        int[][] Buffalo_2_1 = {{21487, 21826}, {16013, 16014}};
        int[][] Buffalo_2_2 = {{21488, 21827}, {16013, 16014}};
        int[][] Cougar_0_Gold = {{21490, 21491, 21492, 21493}};
        int[][] Cougar_0_Crystal = {{21494, 21495, 21496, 21497}};
        int[][] Cougar_1_Gold_1 = {{21498, 21500}};
        int[][] Cougar_1_Gold_2 = {{21499, 21501}};
        int[][] Cougar_1_Crystal_1 = {{21502, 21504}};
        int[][] Cougar_1_Crystal_2 = {{21503, 21505}};
        int[][] Cougar_2_1 = {{21506, 21828}, {16015, 16016}};
        int[][] Cougar_2_2 = {{21507, 21829}, {16015, 16016}};
        GrowthCapableMob temp = new GrowthCapableMob(0, 100);
        temp.addMobs(6643, Kookabura_0_Gold);
        temp.addMobs(6644, Kookabura_0_Crystal);
        GROWTH_CAPABLE_MOBS.put(21451, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6643, Kookabura_1_Gold_1);
        GROWTH_CAPABLE_MOBS.put(21452, temp);
        GROWTH_CAPABLE_MOBS.put(21454, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6643, Kookabura_1_Gold_2);
        GROWTH_CAPABLE_MOBS.put(21453, temp);
        GROWTH_CAPABLE_MOBS.put(21455, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6644, Kookabura_1_Crystal_1);
        GROWTH_CAPABLE_MOBS.put(21456, temp);
        GROWTH_CAPABLE_MOBS.put(21458, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6644, Kookabura_1_Crystal_2);
        GROWTH_CAPABLE_MOBS.put(21457, temp);
        GROWTH_CAPABLE_MOBS.put(21459, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6643, Kookabura_2_1);
        GROWTH_CAPABLE_MOBS.put(21460, temp);
        GROWTH_CAPABLE_MOBS.put(21462, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6643, Kookabura_2_2);
        GROWTH_CAPABLE_MOBS.put(21461, temp);
        GROWTH_CAPABLE_MOBS.put(21463, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6644, Kookabura_2_1);
        GROWTH_CAPABLE_MOBS.put(21464, temp);
        GROWTH_CAPABLE_MOBS.put(21466, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6644, Kookabura_2_2);
        GROWTH_CAPABLE_MOBS.put(21465, temp);
        GROWTH_CAPABLE_MOBS.put(21467, temp);
        temp = new GrowthCapableMob(0, 100);
        temp.addMobs(6643, Buffalo_0_Gold);
        temp.addMobs(6644, Buffalo_0_Crystal);
        GROWTH_CAPABLE_MOBS.put(21470, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6643, Buffalo_1_Gold_1);
        GROWTH_CAPABLE_MOBS.put(21471, temp);
        GROWTH_CAPABLE_MOBS.put(21473, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6643, Buffalo_1_Gold_2);
        GROWTH_CAPABLE_MOBS.put(21472, temp);
        GROWTH_CAPABLE_MOBS.put(21474, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6644, Buffalo_1_Crystal_1);
        GROWTH_CAPABLE_MOBS.put(21475, temp);
        GROWTH_CAPABLE_MOBS.put(21477, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6644, Buffalo_1_Crystal_2);
        GROWTH_CAPABLE_MOBS.put(21476, temp);
        GROWTH_CAPABLE_MOBS.put(21478, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6643, Buffalo_2_1);
        GROWTH_CAPABLE_MOBS.put(21479, temp);
        GROWTH_CAPABLE_MOBS.put(21481, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6643, Buffalo_2_2);
        GROWTH_CAPABLE_MOBS.put(21480, temp);
        GROWTH_CAPABLE_MOBS.put(21482, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6644, Buffalo_2_1);
        GROWTH_CAPABLE_MOBS.put(21483, temp);
        GROWTH_CAPABLE_MOBS.put(21485, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6644, Buffalo_2_2);
        GROWTH_CAPABLE_MOBS.put(21484, temp);
        GROWTH_CAPABLE_MOBS.put(21486, temp);
        temp = new GrowthCapableMob(0, 100);
        temp.addMobs(6643, Cougar_0_Gold);
        temp.addMobs(6644, Cougar_0_Crystal);
        GROWTH_CAPABLE_MOBS.put(21489, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6643, Cougar_1_Gold_1);
        GROWTH_CAPABLE_MOBS.put(21490, temp);
        GROWTH_CAPABLE_MOBS.put(21492, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6643, Cougar_1_Gold_2);
        GROWTH_CAPABLE_MOBS.put(21491, temp);
        GROWTH_CAPABLE_MOBS.put(21493, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6644, Cougar_1_Crystal_1);
        GROWTH_CAPABLE_MOBS.put(21494, temp);
        GROWTH_CAPABLE_MOBS.put(21496, temp);
        temp = new GrowthCapableMob(1, 40);
        temp.addMobs(6644, Cougar_1_Crystal_2);
        GROWTH_CAPABLE_MOBS.put(21495, temp);
        GROWTH_CAPABLE_MOBS.put(21497, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6643, Cougar_2_1);
        GROWTH_CAPABLE_MOBS.put(21498, temp);
        GROWTH_CAPABLE_MOBS.put(21500, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6643, Cougar_2_2);
        GROWTH_CAPABLE_MOBS.put(21499, temp);
        GROWTH_CAPABLE_MOBS.put(21501, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6644, Cougar_2_1);
        GROWTH_CAPABLE_MOBS.put(21502, temp);
        GROWTH_CAPABLE_MOBS.put(21504, temp);
        temp = new GrowthCapableMob(2, 25);
        temp.addMobs(6644, Cougar_2_2);
        GROWTH_CAPABLE_MOBS.put(21503, temp);
        GROWTH_CAPABLE_MOBS.put(21505, temp);
    }

    protected void registerNpcs() {
        addEventIds(FEEDABLE_BEASTS, ScriptEventType.ON_KILL, ScriptEventType.ON_SKILL_SEE);
    }

    public void spawnNext(Npc npc, int growthLevel, Player player, int food) {
        int npcId = npc.getNpcId();
        int nextNpcId = 0;
        if (growthLevel == 2) {
            if (Rnd.get(2) == 0) {
                if (player.isMageClass()) {
                    nextNpcId = GROWTH_CAPABLE_MOBS.get(npcId).getMob(food, 1, 1);
                } else {
                    nextNpcId = GROWTH_CAPABLE_MOBS.get(npcId).getMob(food, 1, 0);
                }
            } else if (Rnd.get(5) == 0) {
                nextNpcId = GROWTH_CAPABLE_MOBS.get(npcId).getMob(food, 0, 1);
            } else {
                nextNpcId = GROWTH_CAPABLE_MOBS.get(npcId).getMob(food, 0, 0);
            }
        } else {
            nextNpcId = GROWTH_CAPABLE_MOBS.get(npcId).getRandomMob(food);
        }
        if (FEED_INFO.getOrDefault(npc.getObjectId(), 0) == player.getObjectId())
            FEED_INFO.remove(npc.getObjectId());
        npc.deleteMe();
        if (ArraysUtil.contains(TAMED_BEASTS, nextNpcId)) {
            if (player.getTrainedBeast() != null)
                player.getTrainedBeast().deleteMe();
            NpcTemplate template = NpcData.getInstance().getTemplate(nextNpcId);
            TamedBeast nextNpc = new TamedBeast(IdFactory.getInstance().getNextId(), template, player, food, npc.getPosition());
            nextNpc.setRunning();
            QuestState st = player.getQuestState("Q020_BringUpWithLove");
            if (st != null && Rnd.get(100) < 5 && !st.hasQuestItems(7185)) {
                st.giveItems(7185, 1);
                st.set("cond", "2");
            }
            int rand = Rnd.get(20);
            if (rand < 5)
                npc.broadcastPacket(new NpcSay(nextNpc.getObjectId(), 0, nextNpc.getNpcId(), SPAWN_CHATS[rand].replace("$s1", player.getName())));
        } else {
            Attackable nextNpc = (Attackable) addSpawn(nextNpcId, npc, false, 0L, false);
            if (MAD_COW_POLYMORPH.containsKey(nextNpcId))
                startQuestTimer("polymorph Mad Cow", 10000L, nextNpc, player, false);
            FEED_INFO.put(nextNpc.getObjectId(), player.getObjectId());
            attack(nextNpc, player);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("polymorph Mad Cow") && npc != null && player != null)
            if (MAD_COW_POLYMORPH.containsKey(npc.getNpcId())) {
                if (FEED_INFO.getOrDefault(npc.getObjectId(), 0) == player.getObjectId())
                    FEED_INFO.remove(npc.getObjectId());
                npc.deleteMe();
                Attackable nextNpc = (Attackable) addSpawn(MAD_COW_POLYMORPH.get(npc.getNpcId()), npc, false, 0L, false);
                FEED_INFO.put(nextNpc.getObjectId(), player.getObjectId());
                attack(nextNpc, player);
            }
        return super.onAdvEvent(event, npc, player);
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (!ArraysUtil.contains((Object[]) targets, npc))
            return super.onSkillSee(npc, caster, skill, targets, isPet);
        int npcId = npc.getNpcId();
        int skillId = skill.getId();
        if (!ArraysUtil.contains(FEEDABLE_BEASTS, npcId) || (skillId != 2188 && skillId != 2189))
            return super.onSkillSee(npc, caster, skill, targets, isPet);
        int objectId = npc.getObjectId();
        int growthLevel = 3;
        if (GROWTH_CAPABLE_MOBS.containsKey(npcId))
            growthLevel = GROWTH_CAPABLE_MOBS.get(npcId).getGrowthLevel();
        if (growthLevel == 0 && FEED_INFO.containsKey(objectId))
            return super.onSkillSee(npc, caster, skill, targets, isPet);
        FEED_INFO.put(objectId, caster.getObjectId());
        int food = 0;
        if (skillId == 2188) {
            food = 6643;
        } else if (skillId == 2189) {
            food = 6644;
        }
        npc.broadcastPacket(new SocialAction(npc, 2));
        if (GROWTH_CAPABLE_MOBS.containsKey(npcId)) {
            if (GROWTH_CAPABLE_MOBS.get(npcId).getMob(food, 0, 0) == null)
                return super.onSkillSee(npc, caster, skill, targets, isPet);
            if (Rnd.get(20) == 0)
                npc.broadcastPacket(new NpcSay(objectId, 0, npc.getNpcId(), (String) Rnd.get((Object[]) TEXT[growthLevel])));
            if (growthLevel > 0 && FEED_INFO.getOrDefault(objectId, 0) != caster.getObjectId())
                return super.onSkillSee(npc, caster, skill, targets, isPet);
            if (Rnd.get(100) < GROWTH_CAPABLE_MOBS.get(npcId).getChance())
                spawnNext(npc, growthLevel, caster, food);
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }

    public String onKill(Npc npc, Creature killer) {
        FEED_INFO.remove(npc.getObjectId());
        return super.onKill(npc, killer);
    }

    private static class GrowthCapableMob {
        private final int _growthLevel;

        private final int _chance;

        private final Map<Integer, int[][]> _spiceToMob = new HashMap<>();

        public GrowthCapableMob(int growthLevel, int chance) {
            this._growthLevel = growthLevel;
            this._chance = chance;
        }

        public void addMobs(int spice, int[][] Mobs) {
            this._spiceToMob.put(spice, Mobs);
        }

        public Integer getMob(int spice, int mobType, int classType) {
            if (this._spiceToMob.containsKey(spice))
                return ((int[][]) this._spiceToMob.get(spice))[mobType][classType];
            return null;
        }

        public Integer getRandomMob(int spice) {
            int[][] temp = this._spiceToMob.get(spice);
            int rand = Rnd.get((temp[0]).length);
            return temp[0][rand];
        }

        public Integer getChance() {
            return this._chance;
        }

        public Integer getGrowthLevel() {
            return this._growthLevel;
        }
    }
}
