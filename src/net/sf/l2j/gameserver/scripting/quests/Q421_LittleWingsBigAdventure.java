package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q421_LittleWingsBigAdventure extends Quest {
    private static final String qn = "Q421_LittleWingsBigAdventure";

    private static final int CRONOS = 30610;

    private static final int MIMYU = 30747;

    private static final int FAIRY_LEAF = 4325;

    public Q421_LittleWingsBigAdventure() {
        super(421, "Little Wing's Big Adventure");
        setItemsIds(4325);
        addStartNpc(30610);
        addTalkId(30610, 30747);
        addAttackId(27185, 27186, 27187, 27188);
        addKillId(27185, 27186, 27187, 27188);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q421_LittleWingsBigAdventure");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30610-06.htm")) {
            if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) == 1)
                for (int i = 3500; i < 3503; i++) {
                    ItemInstance item = player.getInventory().getItemByItemId(i);
                    if (item != null && item.getEnchantLevel() >= 55) {
                        st.setState((byte) 1);
                        st.set("cond", "1");
                        st.set("iCond", "1");
                        st.set("summonOid", String.valueOf(item.getObjectId()));
                        st.playSound("ItemSound.quest_accept");
                        return "30610-05.htm";
                    }
                }
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30747-02.htm")) {
            Summon summon = player.getSummon();
            if (summon != null)
                htmltext = (summon.getControlItemId() == st.getInt("summonOid")) ? "30747-04.htm" : "30747-03.htm";
        } else if (event.equalsIgnoreCase("30747-05.htm")) {
            Summon summon = player.getSummon();
            if (summon == null || summon.getControlItemId() != st.getInt("summonOid")) {
                htmltext = "30747-06.htm";
            } else {
                st.set("cond", "2");
                st.set("iCond", "3");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(4325, 4);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int i, id;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q421_LittleWingsBigAdventure");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 45) {
                    htmltext = "30610-01.htm";
                    break;
                }
                if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) != 1) {
                    htmltext = "30610-02.htm";
                    break;
                }
                for (i = 3500; i < 3503; i++) {
                    ItemInstance item = player.getInventory().getItemByItemId(i);
                    if (item != null && item.getEnchantLevel() >= 55)
                        return "30610-04.htm";
                }
                htmltext = "30610-03.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30610:
                        htmltext = "30610-07.htm";
                        break;
                    case 30747:
                        id = st.getInt("iCond");
                        if (id == 1) {
                            htmltext = "30747-01.htm";
                            st.set("iCond", "2");
                            break;
                        }
                        if (id == 2) {
                            Summon summon = player.getSummon();
                            htmltext = (summon != null) ? ((summon.getControlItemId() == st.getInt("summonOid")) ? "30747-04.htm" : "30747-03.htm") : "30747-02.htm";
                            break;
                        }
                        if (id == 3) {
                            htmltext = "30747-07.htm";
                            break;
                        }
                        if (id > 3 && id < 63) {
                            htmltext = "30747-11.htm";
                            break;
                        }
                        if (id == 63) {
                            Summon summon = player.getSummon();
                            if (summon == null)
                                return "30747-12.htm";
                            if (summon.getControlItemId() != st.getInt("summonOid"))
                                return "30747-14.htm";
                            htmltext = "30747-13.htm";
                            st.set("iCond", "100");
                            break;
                        }
                        if (id == 100) {
                            Summon summon = player.getSummon();
                            if (summon != null && summon.getControlItemId() == st.getInt("summonOid"))
                                return "30747-15.htm";
                            if (st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) > 1)
                                return "30747-17.htm";
                            for (int j = 3500; j < 3503; j++) {
                                ItemInstance item = player.getInventory().getItemByItemId(j);
                                if (item != null && item.getObjectId() == st.getInt("summonOid")) {
                                    st.takeItems(j, 1);
                                    st.giveItems(j + 922, 1, item.getEnchantLevel());
                                    st.playSound("ItemSound.quest_finish");
                                    st.exitQuest(true);
                                    return "30747-16.htm";
                                }
                            }
                            htmltext = "30747-18.htm";
                            L2Skill skill = SkillTable.getInstance().getInfo(4167, 1);
                            if (skill != null && player.getFirstEffect(skill) == null)
                                skill.getEffects(npc, player);
                        }
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (((Monster) npc).hasMinions())
            for (Monster ghost : ((Monster) npc).getMinionList().getSpawnedMinions()) {
                if (!ghost.isDead() && Rnd.get(100) < 1)
                    ghost.broadcastNpcSay("We must protect the fairy tree!");
            }
        if (attacker instanceof Pet) {
            Player player = attacker.getActingPlayer();
            QuestState st = checkPlayerCondition(player, npc, "cond", "2");
            if (st == null)
                return null;
            if (((Pet) attacker).getControlItemId() == st.getInt("summonOid") && Rnd.get(100) < 1 && st.hasQuestItems(4325)) {
                int idMask = (int) Math.pow(2.0D, (npc.getNpcId() - 27182 - 1));
                int iCond = st.getInt("iCond");
                if ((iCond | idMask) != iCond) {
                    st.set("iCond", String.valueOf(iCond | idMask));
                    npc.broadcastNpcSay("Give me a Fairy Leaf...!");
                    st.takeItems(4325, 1);
                    npc.broadcastNpcSay("Leave now, before you incur the wrath of the guardian ghost...");
                    if (st.getInt("iCond") == 63) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        if (Rnd.get(100) < 30) {
            L2Skill skill = SkillTable.getInstance().getInfo(4243, 1);
            if (skill != null && killer.getFirstEffect(skill) == null)
                skill.getEffects(npc, killer);
        }
        for (int i = 0; i < 20; i++) {
            Attackable newNpc = (Attackable) addSpawn(27189, npc, true, 300000L, false);
            newNpc.setRunning();
            newNpc.addDamageHate(killer, 0, 999);
            newNpc.getAI().setIntention(IntentionType.ATTACK, killer);
        }
        return null;
    }
}
