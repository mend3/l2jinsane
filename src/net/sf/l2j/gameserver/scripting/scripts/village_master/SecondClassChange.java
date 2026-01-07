package net.sf.l2j.gameserver.scripting.scripts.village_master;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class SecondClassChange extends Quest {
    public static final int[] SECONDCLASSNPCS = new int[]{
            31328, 30195, 30699, 30474, 31324, 30862, 30910, 31285, 31331, 31334,
            31974, 32096, 30513, 30681, 30704, 30865, 30913, 31288, 31326, 31977,
            30511, 30676, 30685, 30845, 30894, 31269, 31314, 31958, 30512, 30677,
            30687, 30847, 30897, 31272, 31317, 31961, 30109, 30187, 30689, 30849,
            30900, 31965, 32094, 30115, 30174, 30176, 30694, 30854, 31996, 30120,
            30191, 30857, 30905, 31276, 31321, 31279, 31755, 31968, 32095, 31336};
    private static final String qn = "SecondClassChange";
    private static final int MARK_OF_CHALLENGER = 2627;
    private static final int MARK_OF_DUTY = 2633;
    private static final int MARK_OF_SEEKER = 2673;
    private static final int MARK_OF_SCHOLAR = 2674;
    private static final int MARK_OF_PILGRIM = 2721;
    private static final int MARK_OF_DUELIST = 2762;
    private static final int MARK_OF_SEARCHER = 2809;
    private static final int MARK_OF_REFORMER = 2821;
    private static final int MARK_OF_MAGUS = 2840;
    private static final int MARK_OF_FATE = 3172;
    private static final int MARK_OF_SAGITTARIUS = 3293;
    private static final int MARK_OF_WITCHCRAFT = 3307;
    private static final int MARK_OF_SUMMONER = 3336;
    private static final int MARK_OF_WARSPIRIT = 2879;
    private static final int MARK_OF_GLORY = 3203;
    private static final int MARK_OF_CHAMPION = 3276;
    private static final int MARK_OF_LORD = 3390;
    private static final int MARK_OF_GUILDSMAN = 3119;
    private static final int MARK_OF_PROSPERITY = 3238;
    private static final int MARK_OF_MAESTRO = 2867;
    private static final int MARK_OF_TRUST = 2734;
    private static final int MARK_OF_HEALER = 2820;
    private static final int MARK_OF_LIFE = 3140;
    private static final Map<String, int[]> Classes = new HashMap<>();

    public SecondClassChange() {
        super(-1, "village_master");
        Classes.put("SK", new int[]{
                33, 32, 2, 26, 27, 28, 29, 2633, 3172, 3307,
                56});
        Classes.put("BD", new int[]{
                34, 32, 2, 30, 31, 32, 33, 2627, 3172, 2762,
                56});
        Classes.put("SE", new int[]{
                43, 42, 2, 34, 35, 36, 37, 2721, 3172, 2821,
                56});
        Classes.put("AW", new int[]{
                36, 35, 2, 38, 39, 40, 41, 2673, 3172, 2809,
                56});
        Classes.put("PR", new int[]{
                37, 35, 2, 42, 43, 44, 45, 2673, 3172, 3293,
                56});
        Classes.put("SH", new int[]{
                40, 39, 2, 46, 47, 48, 49, 2674, 3172, 2840,
                56});
        Classes.put("PS", new int[]{
                41, 39, 2, 50, 51, 52, 53, 2674, 3172, 3336,
                56});
        Classes.put("TY", new int[]{
                48, 47, 3, 16, 17, 18, 19, 2627, 3203, 2762,
                34});
        Classes.put("DE", new int[]{
                46, 45, 3, 20, 21, 22, 23, 2627, 3203, 3276,
                34});
        Classes.put("OL", new int[]{
                51, 50, 3, 24, 25, 26, 27, 2721, 3203, 3390,
                34});
        Classes.put("WC", new int[]{
                52, 50, 3, 28, 29, 30, 31, 2721, 3203, 2879,
                34});
        Classes.put("BH", new int[]{
                55, 54, 4, 109, 10, 11, 12, 3119, 3238, 2809,
                15});
        Classes.put("WS", new int[]{
                57, 56, 4, 16, 17, 18, 19, 3119, 3238, 2867,
                22});
        Classes.put("TK", new int[]{
                20, 19, 1, 36, 37, 38, 39, 2633, 3140, 2820,
                78});
        Classes.put("SS", new int[]{
                21, 19, 1, 40, 41, 42, 43, 2627, 3140, 2762,
                78});
        Classes.put("PL", new int[]{
                5, 4, 0, 44, 45, 46, 47, 2633, 2734, 2820,
                78});
        Classes.put("DA", new int[]{
                6, 4, 0, 48, 49, 50, 51, 2633, 2734, 3307,
                78});
        Classes.put("TH", new int[]{
                8, 7, 0, 52, 53, 54, 55, 2673, 2734, 2809,
                78});
        Classes.put("HE", new int[]{
                9, 7, 0, 56, 57, 58, 59, 2673, 2734, 3293,
                78});
        Classes.put("PW", new int[]{
                23, 22, 1, 60, 61, 62, 63, 2673, 3140, 2809,
                78});
        Classes.put("SR", new int[]{
                24, 22, 1, 64, 65, 66, 67, 2673, 3140, 3293,
                78});
        Classes.put("GL", new int[]{
                2, 1, 0, 68, 69, 70, 71, 2627, 2734, 2762,
                78});
        Classes.put("WL", new int[]{
                3, 1, 0, 72, 73, 74, 75, 2627, 2734, 3276,
                78});
        Classes.put("EW", new int[]{
                27, 26, 1, 18, 19, 20, 21, 2674, 3140, 2840,
                40});
        Classes.put("ES", new int[]{
                28, 26, 1, 22, 23, 24, 25, 2674, 3140, 3336,
                40});
        Classes.put("HS", new int[]{
                12, 11, 0, 26, 27, 28, 29, 2674, 2734, 2840,
                40});
        Classes.put("HN", new int[]{
                13, 11, 0, 30, 31, 32, 33, 2674, 2734, 3307,
                40});
        Classes.put("HW", new int[]{
                14, 11, 0, 34, 35, 36, 37, 2674, 2734, 3336,
                40});
        Classes.put("BI", new int[]{
                16, 15, 0, 16, 17, 18, 19, 2721, 2734, 2820,
                26});
        Classes.put("PH", new int[]{
                17, 15, 0, 20, 21, 22, 23, 2721, 2734, 2821,
                26});
        Classes.put("EE", new int[]{
                30, 29, 1, 12, 13, 14, 15, 2721, 3140, 2820,
                26});
        addStartNpc(SECONDCLASSNPCS);
        addTalkId(SECONDCLASSNPCS);
    }

    private static String getClassHtml(Player player) {
        String change = "";
        switch (player.getRace()) {
            case DARK_ELF:
                change = "master_de";
                break;
            case DWARF:
                change = "master_dwarf";
                break;
            case ORC:
                change = "master_orc";
                break;
            case HUMAN:
            case ELF:
                if (player.isMageClass()) {
                    change = (player.getClassId() == ClassId.HUMAN_WIZARD || player.getClassId() == ClassId.ELVEN_WIZARD) ? "master_human_elf_mystic" : "master_human_elf_buffer";
                    break;
                }
                change = "master_human_elf_fighter";
                break;
        }
        return change;
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("SecondClassChange");
        if (st == null)
            return htmltext;
        String suffix = "";
        if (Classes.containsKey(event)) {
            int[] array = Classes.get(event);
            if (player.getClassId().getId() == array[1] && player.getRace().ordinal() == array[2]) {
                if (player.getLevel() < 40) {
                    suffix = "-" + (st.hasQuestItems(array[7], array[8], array[9]) ? array[4] : array[3]);
                } else if (st.hasQuestItems(array[7], array[8], array[9])) {
                    st.playSound("ItemSound.quest_fanfare_2");
                    st.takeItems(array[7], -1);
                    st.takeItems(array[8], -1);
                    st.takeItems(array[9], -1);
                    player.setClassId(array[0]);
                    player.setBaseClass(array[0]);
                    player.sendPacket(new HennaInfo(player));
                    player.broadcastUserInfo();
                    suffix = "-" + array[6];
                } else {
                    suffix = "-" + array[5];
                }
                htmltext = getClassHtml(player) + getClassHtml(player) + ".htm";
                st.exitQuest(true);
            } else {
                htmltext = getClassHtml(player) + "-" + getClassHtml(player) + ".htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = Quest.getNoQuestMsg();
        QuestState st = player.getQuestState("SecondClassChange");
        if (st == null)
            return htmltext;
        if (player.isSubClassActive()) {
            st.exitQuest(true);
            return htmltext;
        }
        switch (npc.getNpcId()) {
            case 30195:
            case 30474:
            case 30699:
            case 30862:
            case 30910:
            case 31285:
            case 31324:
            case 31328:
            case 31331:
            case 31334:
            case 31974:
            case 32096:
                if (player.getRace() == ClassRace.DARK_ELF) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.PALUS_KNIGHT) {
                            htmltext = "master_de-01.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.SHILLIEN_ORACLE) {
                            htmltext = "master_de-08.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ASSASSIN) {
                            htmltext = "master_de-12.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.DARK_WIZARD)
                            htmltext = "master_de-19.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_de-55.htm" : "master_de-54.htm";
                    break;
                }
                htmltext = "master_de-56.htm";
                break;
            case 30513:
            case 30681:
            case 30704:
            case 30865:
            case 30913:
            case 31288:
            case 31326:
            case 31977:
                if (player.getRace() == ClassRace.ORC) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.MONK) {
                            htmltext = "master_orc-01.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ORC_RAIDER) {
                            htmltext = "master_orc-05.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ORC_SHAMAN)
                            htmltext = "master_orc-09.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_orc-33.htm" : "master_orc-32.htm";
                    break;
                }
                htmltext = "master_orc-34.htm";
                break;
            case 30511:
            case 30676:
            case 30685:
            case 30845:
            case 30894:
            case 31269:
            case 31314:
            case 31958:
                if (player.getRace() == ClassRace.DWARF) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.SCAVENGER) {
                            htmltext = "master_dwarf-01.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ARTISAN)
                            htmltext = "master_dwarf-15.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_dwarf-13.htm" : "master_dwarf-14.htm";
                    break;
                }
                htmltext = "master_dwarf-15.htm";
                break;
            case 30512:
            case 30677:
            case 30687:
            case 30847:
            case 30897:
            case 31272:
            case 31317:
            case 31961:
                if (player.getRace() == ClassRace.DWARF) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.SCAVENGER) {
                            htmltext = "master_dwarf-22.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ARTISAN)
                            htmltext = "master_dwarf-05.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_dwarf-20.htm" : "master_dwarf-21.htm";
                    break;
                }
                htmltext = "master_dwarf-22.htm";
                break;
            case 30109:
            case 30187:
            case 30689:
            case 30849:
            case 30900:
            case 31965:
            case 32094:
                if (player.getRace() == ClassRace.HUMAN || player.getRace() == ClassRace.ELF) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.ELVEN_KNIGHT) {
                            htmltext = "master_human_elf_fighter-01.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.KNIGHT) {
                            htmltext = "master_human_elf_fighter-08.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ROGUE) {
                            htmltext = "master_human_elf_fighter-15.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.ELVEN_SCOUT) {
                            htmltext = "master_human_elf_fighter-22.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.WARRIOR) {
                            htmltext = "master_human_elf_fighter-29.htm";
                            break;
                        }
                        htmltext = "master_human_elf_fighter-78.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_human_elf_fighter-76.htm" : "master_human_elf_fighter-77.htm";
                    break;
                }
                htmltext = "master_human_elf_fighter-78.htm";
                break;
            case 30115:
            case 30174:
            case 30176:
            case 30694:
            case 30854:
            case 31996:
                if (player.getRace() == ClassRace.ELF || player.getRace() == ClassRace.HUMAN) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.ELVEN_WIZARD) {
                            htmltext = "master_human_elf_mystic-01.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.HUMAN_WIZARD) {
                            htmltext = "master_human_elf_mystic-08.htm";
                            break;
                        }
                        htmltext = "master_human_elf_mystic-40.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_human_elf_mystic-38.htm" : "master_human_elf_mystic-39.htm";
                    break;
                }
                htmltext = "master_human_elf_mystic-40.htm";
                break;
            case 30120:
            case 30191:
            case 30857:
            case 30905:
            case 31276:
            case 31279:
            case 31321:
            case 31336:
            case 31755:
            case 31968:
            case 32095:
                if (player.getRace() == ClassRace.HUMAN || player.getRace() == ClassRace.ELF) {
                    if (player.getClassId().level() == 1) {
                        if (player.getClassId() == ClassId.ELVEN_ORACLE) {
                            htmltext = "master_human_elf_buffer-01.htm";
                            break;
                        }
                        if (player.getClassId() == ClassId.CLERIC) {
                            htmltext = "master_human_elf_buffer-05.htm";
                            break;
                        }
                        htmltext = "master_human_elf_buffer-26.htm";
                        break;
                    }
                    htmltext = (player.getClassId().level() == 0) ? "master_human_elf_buffer-24.htm" : "master_human_elf_buffer-25.htm";
                    break;
                }
                htmltext = "master_human_elf_buffer-26.htm";
                break;
        }
        st.exitQuest(true);
        return htmltext;
    }
}
