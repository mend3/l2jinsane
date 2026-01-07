package enginemods.main.engine.stats;

import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import enginemods.main.util.builders.html.HtmlBuilder.HtmlType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.*;
import java.util.Map.Entry;

public class StatsPlayer extends AbstractMods {
    private static final Map<ClassId, StatsPlayer.StatsHolder> _classStats = new HashMap();

    public StatsPlayer() {
        this.registerMod(true);
        this.initStats();
    }

    private static void htmlIndexClass(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
        hb.append("<html><body>");
        hb.append("<br>");
        hb.append("<center>");
        hb.append("Selecciona la clase a la que quieres ajustar su balance<br>");
        hb.append("<br>");
        hb.append(Html.newFontColor("LEVEL", "HUMAN"));
        hb.append("<table bgcolor=000000>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.DUELIST));
        hb.append(buttonClassId(ClassId.DREADNOUGHT));
        hb.append(buttonClassId(ClassId.PHOENIX_KNIGHT));
        hb.append(buttonClassId(ClassId.HELL_KNIGHT));
        hb.append(buttonClassId(ClassId.SAGGITARIUS));
        hb.append(buttonClassId(ClassId.ADVENTURER));
        hb.append("</tr>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.ARCHMAGE));
        hb.append(buttonClassId(ClassId.SOULTAKER));
        hb.append(buttonClassId(ClassId.ARCANA_LORD));
        hb.append(buttonClassId(ClassId.CARDINAL));
        hb.append(buttonClassId(ClassId.HIEROPHANT));
        hb.append("<td></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newFontColor("LEVEL", "ELF"));
        hb.append("<table bgcolor=000000>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.EVAS_TEMPLAR));
        hb.append(buttonClassId(ClassId.SWORD_MUSE));
        hb.append(buttonClassId(ClassId.WIND_RIDER));
        hb.append(buttonClassId(ClassId.MOONLIGHT_SENTINEL));
        hb.append(buttonClassId(ClassId.MYSTIC_MUSE));
        hb.append(buttonClassId(ClassId.ELEMENTAL_MASTER));
        hb.append("</tr>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.EVAS_SAINT));
        hb.append("<td></td>");
        hb.append("<td></td>");
        hb.append("<td></td>");
        hb.append("<td></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newFontColor("LEVEL", "DARK ELF"));
        hb.append("<table bgcolor=000000>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.SHILLIEN_TEMPLAR));
        hb.append(buttonClassId(ClassId.SPECTRAL_DANCER));
        hb.append(buttonClassId(ClassId.GHOST_HUNTER));
        hb.append(buttonClassId(ClassId.GHOST_SENTINEL));
        hb.append(buttonClassId(ClassId.STORM_SCREAMER));
        hb.append(buttonClassId(ClassId.SPECTRAL_MASTER));
        hb.append("</tr>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.SHILLIEN_SAINT));
        hb.append("<td></td>");
        hb.append("<td></td>");
        hb.append("<td></td>");
        hb.append("<td></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newFontColor("LEVEL", "ORC"));
        hb.append("<table bgcolor=000000>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.TITAN));
        hb.append(buttonClassId(ClassId.GRAND_KHAVATARI));
        hb.append(buttonClassId(ClassId.DOMINATOR));
        hb.append(buttonClassId(ClassId.DOOMCRYER));
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newFontColor("LEVEL", "DWARF"));
        hb.append("<table bgcolor=000000>");
        hb.append("<tr>");
        hb.append(buttonClassId(ClassId.FORTUNE_SEEKER));
        hb.append(buttonClassId(ClassId.MAESTRO));
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<br>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendCommunity(player, hb.toString());
    }

    private static String buttonClassId(ClassId classId) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
        hb.append("<td><button value=", classId.toString().replace("_", " ").toLowerCase(), " action=\"bypass _bbshome,class,", classId.name(), "\" width=93 height=22 back=", "L2UI_CH3.bigbutton_down", " fore=", "L2UI_CH3.bigbutton", "></td>");
        return hb.toString();
    }

    private static void htmlIndex(Player player, ClassId classId, StatsPlayer.BonusType bonusType, int page) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
        hb.append("<html><body>");
        hb.append("<br>");
        hb.append("<center>");
        hb.append("<button value=INDEX action=\"bypass _bbshome,balance\" width=93 height=22 back=", "L2UI_CH3.bigbutton_down", " fore=", "L2UI_CH3.bigbutton", ">");
        hb.append("<br>");
        hb.append(Html.htmlHeadCommunity(classId.name()));
        hb.append("<br>");
        hb.append("<table width=460 height=22>");
        hb.append("<tr>");
        StatsPlayer.BonusType[] var5 = StatsPlayer.BonusType.values();
        int searchPage = var5.length;

        int count;
        for (count = 0; count < searchPage; ++count) {
            StatsPlayer.BonusType bt = var5[count];
            hb.append("<td><button value=", bt.name(), " action=\"bypass _bbshome,class,", classId.name(), ",", bt.name(), "\" width=93 height=22 back=", "L2UI_CH3.bigbutton_down", " fore=", "L2UI_CH3.bigbutton", "></td>");
        }

        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<br>");
        int MAX_PER_PAGE = 13;
        searchPage = MAX_PER_PAGE * (page - 1);
        count = 0;
        int color = 0;
        Stats[] var9 = Stats.values();
        int size = var9.length;

        int i;
        for (i = 0; i < size; ++i) {
            Stats stat = var9[i];
            if (count < searchPage) {
                ++count;
            } else if (count < searchPage + MAX_PER_PAGE) {
                double value = _classStats.get(classId).getBonus(bonusType, stat);
                hb.append("<table width=460 height=22 ", color % 2 == 0 ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0>");
                hb.append("<tr>");
                hb.append("<td fixwidth=16 height=22 align=center>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td>");
                hb.append("<td width=100 height=22 align=center>", Html.newFontColor("LEVEL", stat.toString().replace("_", " ").toLowerCase()), " </td>");
                hb.append("<td width=62 align=center>", value, "%</td>");
                hb.append("<td width=32><button action=\"bypass _bbshome,modified,", classId.name(), ",", bonusType.name(), ",", stat, ",add\" width=16 height=16 back=sek.cbui343 fore=sek.cbui343></td>");
                hb.append("<td width=32><button action=\"bypass _bbshome,modified,", classId.name(), ",", bonusType.name(), ",", stat, ",sub\" width=16 height=16 back=sek.cbui347 fore=sek.cbui347></td>");
                hb.append("</tr>");
                hb.append("</table>");
                hb.append(Html.newImage("L2UI.SquareGray", 460, 1));
                ++color;
                ++count;
            }
        }

        int currentPage = 1;
        size = Stats.values().length;
        hb.append("<br>");
        hb.append("<table>");
        hb.append("<tr>");

        for (i = 0; i < size; ++i) {
            if (i % MAX_PER_PAGE == 0) {
                if (currentPage == page) {
                    hb.append("<td width=20>", Html.newFontColor("LEVEL", currentPage), "</td>");
                } else {
                    hb.append("<td width=20><a action=\"bypass _bbshome,class,", classId.name(), ",", bonusType.name(), ",", currentPage, "\">", currentPage, "</a></td>");
                }

                ++currentPage;
            }
        }

        hb.append("</tr>");
        hb.append("</table>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendCommunity(player, hb.toString());
    }

    public static StatsPlayer getInstance() {
        return StatsPlayer.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    private void initStats() {
        ClassId[] var1 = ClassId.values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            ClassId cs = var1[var3];
            if (cs.level() >= 3) {
                _classStats.put(cs, new StatsHolder(this));
                StatsPlayer.BonusType[] var5 = StatsPlayer.BonusType.values();
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    StatsPlayer.BonusType bt = var5[var7];
                    String values = this.getValueDB(cs.ordinal(), bt.name());
                    if (values != null) {
                        String[] var10 = values.split(";");
                        int var11 = var10.length;

                        for (int var12 = 0; var12 < var11; ++var12) {
                            String split = var10[var12];
                            String[] parse = split.split(",");
                            Stats stat = Stats.valueOf(parse[0]);
                            int bonus = Integer.parseInt(parse[1]);
                            _classStats.get(cs).setBonus(bt, stat, bonus);
                        }
                    }
                }
            }
        }

    }

    public boolean onAdminCommand(Player player, String chat) {
        if (chat.equals("balance")) {
            htmlIndexClass(player);
            return true;
        } else {
            return false;
        }
    }

    public boolean onCommunityBoard(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command, ",");
        st.nextToken();
        if (!st.hasMoreTokens()) {
            return false;
        } else {
            String event = st.nextToken();
            if (event.equals("balance")) {
                htmlIndexClass(player);
                return true;
            } else {
                ClassId classId;
                StatsPlayer.BonusType bonusType;
                if (event.equals("class")) {
                    classId = ClassId.valueOf(st.nextToken());
                    bonusType = st.hasMoreTokens() ? StatsPlayer.BonusType.valueOf(st.nextToken()) : StatsPlayer.BonusType.NORMAL;
                    int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                    htmlIndex(player, classId, bonusType, page);
                    return true;
                } else if (!event.equals("modified")) {
                    return false;
                } else {
                    classId = ClassId.valueOf(st.nextToken());
                    bonusType = StatsPlayer.BonusType.valueOf(st.nextToken());
                    Stats stat = Stats.valueOf(st.nextToken());
                    String type = st.nextToken();
                    int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                    byte var11 = -1;
                    switch (type.hashCode()) {
                        case 96417:
                            if (type.equals("add")) {
                                var11 = 0;
                            }
                            break;
                        case 114240:
                            if (type.equals("sub")) {
                                var11 = 1;
                            }
                    }

                    switch (var11) {
                        case 0:
                            _classStats.get(classId).increaseBonus(bonusType, stat);
                            break;
                        case 1:
                            _classStats.get(classId).decreaseBonus(bonusType, stat);
                    }

                    String parse = "";

                    Entry map;
                    for (Iterator var14 = _classStats.get(classId).getAllBonus(bonusType).entrySet().iterator(); var14.hasNext(); parse = parse + ((Stats) map.getKey()).name() + "," + map.getValue() + ";") {
                        map = (Entry) var14.next();
                    }

                    this.setValueDB(classId.ordinal(), bonusType.name(), parse);
                    htmlIndex(player, classId, bonusType, page);
                    return true;
                }
            }
        }
    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Playable.class, character)) {
            return value;
        } else {
            Player player = character.getActingPlayer();
            if (!_classStats.containsKey(player.getClassId())) {
                return value;
            } else {
                StatsPlayer.BonusType bonusType = StatsPlayer.BonusType.NORMAL;
                if (player.isInOlympiadMode()) {
                    bonusType = StatsPlayer.BonusType.OLY;
                }

                if (player.isNoble()) {
                    bonusType = StatsPlayer.BonusType.NOBLE;
                }

                if (player.isHero()) {
                    bonusType = StatsPlayer.BonusType.HERO;
                }

                return value * ((double) _classStats.get(player.getClassId()).getBonus(bonusType, stat) / 10.0D + 1.0D);
            }
        }
    }

    private enum BonusType {
        NORMAL,
        HERO,
        NOBLE,
        OLY

    }

    private static class StatsHolder {
        private final Map<StatsPlayer.BonusType, LinkedHashMap<Stats, Integer>> _stats = new LinkedHashMap();

        public StatsHolder(final StatsPlayer param1) {
            this.initBonus();
        }

        private void initBonus() {
            StatsPlayer.BonusType[] var1 = StatsPlayer.BonusType.values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                StatsPlayer.BonusType bt = var1[var3];
                Stats[] var5 = Stats.values();
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    Stats sts = var5[var7];
                    if (!this._stats.containsKey(bt)) {
                        this._stats.put(bt, new LinkedHashMap());
                    }

                    ((LinkedHashMap) this._stats.get(bt)).put(sts, 1);
                }
            }

        }

        public void setBonus(StatsPlayer.BonusType type, Stats stat, int bonus) {
            ((LinkedHashMap) this._stats.get(type)).put(stat, bonus);
        }

        public int getBonus(StatsPlayer.BonusType type, Stats stat) {
            return (Integer) ((LinkedHashMap) this._stats.get(type)).get(stat);
        }

        public LinkedHashMap<Stats, Integer> getAllBonus(StatsPlayer.BonusType type) {
            return this._stats.get(type);
        }

        public void increaseBonus(StatsPlayer.BonusType type, Stats stat) {
            int oldBonus = (Integer) ((LinkedHashMap) this._stats.get(type)).get(stat);
            ((LinkedHashMap) this._stats.get(type)).put(stat, oldBonus + 1);
        }

        public void decreaseBonus(StatsPlayer.BonusType type, Stats stat) {
            int oldBonus = (Integer) ((LinkedHashMap) this._stats.get(type)).get(stat);
            ((LinkedHashMap) this._stats.get(type)).put(stat, oldBonus - 1);
        }
    }

    private static class SingletonHolder {
        protected static final StatsPlayer INSTANCE = new StatsPlayer();
    }
}