package net.sf.l2j.gameserver.scripting.scripts.feature;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialQuest extends Quest {
    private static final String QUEST_NAME = "TutorialQuest";

    private static final int ADENA_REWARD = 20000;

    public TutorialQuest() {
        super(-1, "feature");
        for (int monsterId = 18000; monsterId <= 21000; monsterId++) {
            NpcTemplate template = NpcData.getInstance().getTemplate(monsterId);
            if (template != null && template.getLevel() < 20)
                addKillId(monsterId);
        }
    }

    public static void onCreate(Player player) {
        QuestState st = player.getQuestState("TutorialQuest");
        int cond = st.getInt("cond");
        if (cond == 0 && player.getLevel() == 1)
            player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050.htm")));
    }

    public static void onStartTutorial(Player player) {
        ThreadPool.schedule(() -> player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-03.htm"))), 1000L);
        QuestState st = player.getQuestState("TutorialQuest");
        int cond = st.getInt("cond");
        if (cond == 0 && player.getLevel() == 1) {
            st.setState((byte) 1);
            st.playSound("ItemSound.quest_accept");
            st.set("cond", "1");
        }
    }

    public static void onExitTutorial(Player player) {
        QuestState st = player.getQuestState("TutorialQuest");
        st.setState((byte) 2);
        st.playSound("ItemSound.quest_finish");
        st.set("cond", "0");
    }

    private static void equip(List<Integer> armorIds, Player player) {
        for (Integer id : armorIds) {
            player.getInventory().addItem("Armors", id, 1, player, null);
            player.getInventory().equipItemAndRecord(player.getInventory().getItemByItemId(id));
        }
        player.getInventory().reloadEquippedItems();
        player.sendPacket(new ItemList(player, false));
        player.sendPacket(new InventoryUpdate());
        player.sendPacket(new EtcStatusUpdate(player));
        player.refreshOverloaded();
        player.refreshExpertisePenalty();
        player.sendPacket(new UserInfo(player));
        player.broadcastUserInfo();
    }

    public static void equipClassWeapon(ClassId classId, Player player) {
        List<Integer> weaponIds = new ArrayList<>();
        switch (player.getClassId().getId()) {
            case 0:
            case 31:
            case 36:
            case 53:
                weaponIds.add(6354);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(6354));
                break;
            case 44:
                weaponIds.add(257);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(257));
                break;
            case 10:
            case 25:
            case 38:
            case 49:
                weaponIds.add(100);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(100));
                break;
        }
        equip(weaponIds, player);
    }

    public static void equipClassArmor(ClassId classId, Player player) {
        List<Integer> armorIds = new ArrayList<>();
        switch (player.getClassId().getId()) {
            case 0:
            case 31:
            case 36:
            case 53:
                armorIds.addAll(Arrays.asList(44, 24, 31, 51, 38, 625, 908, 877, 877, 115,
                        115));
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(24));
                break;
            case 44:
                armorIds.addAll(Arrays.asList(44, 24, 31, 51, 38, 625, 908, 877, 877, 115,
                        115));
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(24));
                break;
            case 10:
            case 25:
            case 38:
            case 49:
                armorIds.addAll(Arrays.asList(44, 1101, 1104, 51, 38, 625, 908, 877, 877, 115,
                        115));
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(1101));
                break;
        }
        equip(armorIds, player);
    }

    public String onKill(Npc npc, Creature killer) {
        List<Integer> mageBuffs, fighterBuffs, newbieBuffs;
        Player player = killer.getActingPlayer();
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("TutorialQuest");
        if (st == null)
            return htmltext;
        int cond = st.getInt("cond");
        if (cond == 0)
            return null;
        switch (cond) {
            case 1:
                st.set("cond", "2");
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-pickup.htm")));
                break;
            case 2:
                st.set("cond", "3");
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-view.htm")));
                break;
            case 3:
                st.set("cond", "4");
                mageBuffs = Arrays.asList(4342, 4355, 4356, 4344);
                fighterBuffs = Arrays.asList(4342, 4357, 4345, 4344);
                newbieBuffs = (player.isMageClass() || player.getClassId() == ClassId.ORC_MYSTIC) ? mageBuffs : fighterBuffs;
                for (Integer id : newbieBuffs) {
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                    L2Skill buff = SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id));
                    buff.getEffects(player, player);
                    player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-buff.htm")));
                }
                break;
            case 4:
                st.set("cond", "5");
                st.playSound("ItemSound.quest_itemget");
                st.giveItems((player.isMageClass() || player.getClassId() == ClassId.ORC_MYSTIC) ? 3947 : 1835, 300);
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce((player.isMageClass() || player.getClassId() == ClassId.ORC_MYSTIC) ? "data/html/scripts/feature/TutorialQuest/60050-spiritshot.htm" : "data/html/scripts/feature/TutorialQuest/60050-soulshot.htm")));
                break;
            case 5:
                st.set("cond", "6");
                st.playSound("ItemSound.quest_itemget");
                equipClassWeapon(null, player);
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-06.htm")));
                break;
            case 6:
                st.set("cond", "7");
                st.playSound("ItemSound.quest_itemget");
                equipClassArmor(null, player);
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-07.htm")));
                break;
            case 7:
                st.set("cond", "8");
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-sp.htm")));
                break;
            case 8:
                st.set("cond", "9");
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-skills.htm")));
                break;
            case 9:
                st.set("cond", "0");
                player.addExpAndSp(12000L, 2000);
                player.sendMessage("Quest completed!");
                st.giveItems(57, 20000);
                st.setState((byte) 2);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/feature/TutorialQuest/60050-08.htm")));
                break;
        }
        return null;
    }
}
