package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.GrandBossManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.data.xml.ScriptData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.List;

public class GrandBossTeleporters extends Quest {
    private static final String qn = "GrandBossTeleporters";

    private static final Location BAIUM_IN = new Location(113100, 14500, 10077);

    private static final Location[] BAIUM_OUT = new Location[]{new Location(108784, 16000, -4928), new Location(113824, 10448, -5164), new Location(115488, 22096, -5168)};

    private static final Location SAILREN_IN = new Location(27333, -6835, -1970);

    private static final Location[] SAILREN_OUT = new Location[]{new Location(10610, -24035, -3676), new Location(10703, -24041, -3673), new Location(10769, -24107, -3672)};

    private static int _valakasPlayersCount = 0;

    public GrandBossTeleporters() {
        super(-1, "teleports");
        addFirstTalkId(29055, 31862);
        addStartNpc(13001, 29055, 31859, 31384, 31385, 31540, 31686, 31687, 31759, 31862,
                32107, 32109);
        addTalkId(13001, 29055, 31859, 31384, 31385, 31540, 31686, 31687, 31759, 31862,
                32107, 32109);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = "";
        QuestState st = player.getQuestState("GrandBossTeleporters");
        if (st == null)
            st = newQuestState(player);
        st.setState((byte) 1);
        if (event.equalsIgnoreCase("baium")) {
            if (player.isFlying()) {
                htmltext = "31862-05.htm";
            } else if (!st.hasQuestItems(4295)) {
                htmltext = "31862-03.htm";
            } else {
                st.takeItems(4295, 1);
                ZoneManager.getInstance().getZoneById(110002, BossZone.class).allowPlayerEntry(player, 30);
                player.teleportTo(BAIUM_IN, 0);
            }
        } else if (event.equalsIgnoreCase("baium_story")) {
            htmltext = "31862-02.htm";
        } else if (event.equalsIgnoreCase("baium_exit")) {
            player.teleportTo((Location) Rnd.get((Object[]) BAIUM_OUT), 100);
        } else if (event.equalsIgnoreCase("31540")) {
            if (st.hasQuestItems(7267)) {
                st.takeItems(7267, 1);
                player.teleportTo(183813, -115157, -3303, 0);
                st.set("allowEnter", "1");
            } else {
                htmltext = "31540-06.htm";
            }
        }
        return htmltext;
    }

    public String onFirstTalk(Npc npc, Player player) {
        int status;
        String htmltext = "";
        QuestState st = player.getQuestState("GrandBossTeleporters");
        if (st == null)
            st = newQuestState(player);
        st.setState((byte) 1);
        switch (npc.getNpcId()) {
            case 29055:
                htmltext = "29055-01.htm";
                break;
            case 31862:
                status = GrandBossManager.getInstance().getBossStatus(29020);
                if (status == 1) {
                    htmltext = "31862-01.htm";
                    break;
                }
                if (status == 2) {
                    htmltext = "31862-04.htm";
                    break;
                }
                htmltext = "31862-00.htm";
                break;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int status;
        String htmltext = "";
        QuestState st = player.getQuestState(getName());
        if (st == null)
            return null;
        st.setState((byte) 1);
        switch (npc.getNpcId()) {
            case 13001:
                status = GrandBossManager.getInstance().getBossStatus(29019);
                if (status == 2) {
                    htmltext = "13001-02.htm";
                    break;
                }
                if (status == 3) {
                    htmltext = "13001-01.htm";
                    break;
                }
                if (status == 0 || status == 1) {
                    if (st.hasQuestItems(3865)) {
                        st.takeItems(3865, 1);
                        ZoneManager.getInstance().getZoneById(110001, BossZone.class).allowPlayerEntry(player, 30);
                        player.teleportTo(175300 + Rnd.get(-350, 350), 115180 + Rnd.get(-1000, 1000), -7709, 0);
                        if (status == 0) {
                            GrandBossManager.getInstance().setBossStatus(29019, 1);
                            ScriptData.getInstance().getQuest("Antharas").startQuestTimer("beginning", Config.WAIT_TIME_ANTHARAS, null, null, false);
                        }
                        break;
                    }
                    htmltext = "13001-03.htm";
                }
                break;
            case 31859:
                player.teleportTo(79800 + Rnd.get(600), 151200 + Rnd.get(1100), -3534, 0);
                break;
            case 31385:
                status = GrandBossManager.getInstance().getBossStatus(29028);
                if (status == 0 || status == 1) {
                    if (_valakasPlayersCount >= 200) {
                        htmltext = "31385-03.htm";
                        break;
                    }
                    if (st.getInt("allowEnter") == 1) {
                        st.unset("allowEnter");
                        ZoneManager.getInstance().getZoneById(110010, BossZone.class).allowPlayerEntry(player, 30);
                        player.teleportTo(204328, -111874, 70, 300);
                        _valakasPlayersCount++;
                        if (status == 0) {
                            GrandBossManager.getInstance().setBossStatus(29028, 1);
                            ScriptData.getInstance().getQuest("Valakas").startQuestTimer("beginning", Config.WAIT_TIME_VALAKAS, null, null, false);
                        }
                        break;
                    }
                    htmltext = "31385-04.htm";
                    break;
                }
                if (status == 2) {
                    htmltext = "31385-02.htm";
                    break;
                }
                htmltext = "31385-01.htm";
                break;
            case 31384:
                DoorData.getInstance().getDoor(24210004).openMe();
                break;
            case 31686:
                DoorData.getInstance().getDoor(24210006).openMe();
                break;
            case 31687:
                DoorData.getInstance().getDoor(24210005).openMe();
                break;
            case 31540:
                if (_valakasPlayersCount < 50) {
                    htmltext = "31540-01.htm";
                    break;
                }
                if (_valakasPlayersCount < 100) {
                    htmltext = "31540-02.htm";
                    break;
                }
                if (_valakasPlayersCount < 150) {
                    htmltext = "31540-03.htm";
                    break;
                }
                if (_valakasPlayersCount < 200) {
                    htmltext = "31540-04.htm";
                    break;
                }
                htmltext = "31540-05.htm";
                break;
            case 31759:
                player.teleportTo(150037, -57720, -2976, 250);
                break;
            case 32107:
                player.teleportTo((Location) Rnd.get((Object[]) SAILREN_OUT), 100);
                break;
            case 32109:
                if (!player.isInParty()) {
                    htmltext = "32109-03.htm";
                    break;
                }
                if (!player.getParty().isLeader(player)) {
                    htmltext = "32109-01.htm";
                    break;
                }
                if (st.hasQuestItems(8784)) {
                    status = GrandBossManager.getInstance().getBossStatus(29065);
                    if (status == 0) {
                        List<Player> party = player.getParty().getMembers();
                        for (Player member : party) {
                            if (member.getLevel() < 70)
                                return "32109-06.htm";
                            if (!MathUtil.checkIfInRange(1000, player, member, true))
                                return "32109-07.htm";
                        }
                        st.takeItems(8784, 1);
                        BossZone nest = ZoneManager.getInstance().getZoneById(110011, BossZone.class);
                        for (Player member : party) {
                            if (nest != null) {
                                nest.allowPlayerEntry(member, 30);
                                member.teleportTo(SAILREN_IN, 100);
                            }
                        }
                        GrandBossManager.getInstance().setBossStatus(29065, 1);
                        ScriptData.getInstance().getQuest("Sailren").startQuestTimer("beginning", 60000L, null, null, false);
                        break;
                    }
                    if (status == 2) {
                        htmltext = "32109-04.htm";
                        break;
                    }
                    htmltext = "32109-05.htm";
                    break;
                }
                htmltext = "32109-02.htm";
                break;
        }
        return htmltext;
    }
}
