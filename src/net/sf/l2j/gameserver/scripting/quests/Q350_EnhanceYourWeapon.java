package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.xml.SoulCrystalData;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.npc.AbsorbInfo;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.soulcrystal.LevelingInfo;
import net.sf.l2j.gameserver.model.soulcrystal.SoulCrystal;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.Iterator;

public class Q350_EnhanceYourWeapon extends Quest {
    private static final String qn = "Q350_EnhanceYourWeapon";

    public Q350_EnhanceYourWeapon() {
        super(350, "Enhance Your Weapon");
        addStartNpc(30115, 30194, 30856);
        addTalkId(30115, 30194, 30856);
        Iterator<Integer> iterator;
        for (iterator = SoulCrystalData.getInstance().getLevelingInfos().keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
        for (iterator = SoulCrystalData.getInstance().getSoulCrystals().keySet().iterator(); iterator.hasNext(); ) {
            int crystalId = iterator.next();
            addItemUse(crystalId);
        }
    }

    private static void tryToStageCrystal(Player player, Monster mob, LevelingInfo npcInfo, int chance) {
        SoulCrystal crystalData = null;
        ItemInstance crystalItem = null;
        for (ItemInstance item : player.getInventory().getItems()) {
            SoulCrystal data = SoulCrystalData.getInstance().getSoulCrystals().get(Integer.valueOf(item.getItemId()));
            if (data == null)
                continue;
            if (crystalData != null) {
                if (npcInfo.isSkillRequired()) {
                    AbsorbInfo ai = mob.getAbsorbInfo(player.getObjectId());
                    if (ai != null && ai.isRegistered())
                        player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
                } else {
                    player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
                }
                return;
            }
            crystalData = data;
            crystalItem = item;
        }
        if (crystalData == null || crystalItem == null)
            return;
        if (npcInfo.isSkillRequired()) {
            AbsorbInfo ai = mob.getAbsorbInfo(player.getObjectId());
            if (ai == null || !ai.isRegistered())
                return;
            if (!ai.isValid(crystalItem.getObjectId())) {
                player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
                return;
            }
        }
        if (!ArraysUtil.contains(npcInfo.getLevelList(), crystalData.getLevel())) {
            player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
            return;
        }
        if (player.getLevel() - mob.getLevel() > 8) {
            player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
            return;
        }
        if (chance < npcInfo.getChanceStage()) {
            exchangeCrystal(player, crystalData, true);
        } else if (chance < npcInfo.getChanceStage() + npcInfo.getChanceBreak()) {
            exchangeCrystal(player, crystalData, false);
        } else {
            player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
        }
    }

    private static void exchangeCrystal(Player player, SoulCrystal sc, boolean stage) {
        QuestState st = player.getQuestState("Q350_EnhanceYourWeapon");
        st.takeItems(sc.getInitialItemId(), 1);
        if (stage) {
            player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
            st.giveItems(sc.getStagedItemId(), 1);
            st.playSound("ItemSound.quest_itemget");
        } else {
            int broken = sc.getBrokenItemId();
            if (broken != 0) {
                player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
                st.giveItems(broken, 1);
            }
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q350_EnhanceYourWeapon");
        if (st == null)
            return htmltext;
        if (event.endsWith("-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.endsWith("-09.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4629, 1);
        } else if (event.endsWith("-10.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4640, 1);
        } else if (event.endsWith("-11.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4651, 1);
        } else if (event.endsWith("-exit.htm")) {
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q350_EnhanceYourWeapon");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 40) {
                    htmltext = npc.getNpcId() + "-lvl.htm";
                    break;
                }
                htmltext = npc.getNpcId() + "-01.htm";
                break;
            case 1:
                for (ItemInstance item : player.getInventory().getItems()) {
                    if (SoulCrystalData.getInstance().getSoulCrystals().get(Integer.valueOf(item.getItemId())) != null)
                        return npc.getNpcId() + "-03.htm";
                }
                htmltext = npc.getNpcId() + "-21.htm";
                break;
        }
        return htmltext;
    }

    public String onItemUse(ItemInstance item, Player user, WorldObject target) {
        if (user.isDead())
            return null;
        if (target == null || !(target instanceof Monster mob))
            return null;
        if (mob.isDead() || !SoulCrystalData.getInstance().getLevelingInfos().containsKey(Integer.valueOf(mob.getNpcId())))
            return null;
        mob.addAbsorber(user, item);
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        QuestState st;
        Player player = killer.getActingPlayer();
        if (player == null)
            return null;
        LevelingInfo npcInfo = SoulCrystalData.getInstance().getLevelingInfos().get(Integer.valueOf(npc.getNpcId()));
        if (npcInfo == null)
            return null;
        int chance = Rnd.get(1000);
        Monster mob = (Monster) npc;
        switch (npcInfo.getAbsorbCrystalType()) {
            case FULL_PARTY:
                for (QuestState questState : getPartyMembersState(player, npc, (byte) 1))
                    tryToStageCrystal(questState.getPlayer(), mob, npcInfo, chance);
                break;
            case PARTY_ONE_RANDOM:
                st = getRandomPartyMemberState(player, npc, (byte) 1);
                if (st != null)
                    tryToStageCrystal(st.getPlayer(), mob, npcInfo, chance);
                break;
            case LAST_HIT:
                if (checkPlayerState(player, npc, (byte) 1) != null)
                    tryToStageCrystal(player, mob, npcInfo, chance);
                break;
        }
        return null;
    }
}
