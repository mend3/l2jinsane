package mods.balancer;

import mods.balancer.holder.ClassBalanceHolder;
import net.sf.l2j.gameserver.communitybbs.manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.ClassBalanceManager;
import net.sf.l2j.gameserver.enums.AttackType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class ClassBalanceGui extends BaseBBSManager {
    private static void showMainWindow(Player activeChar) {
        String html = "<html><body><center><br><br><br>";
        html = html + "<button value=\"Class Balance\" action=\"bypass _bbs_classbalance 1 false\" width=90 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">";
        html = html + "<button value=\"Oly Balance\" action=\"bypass _bbs_classbalance 1 true\" width=90 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">";
        html = html + "<br><br>";
        html = html + "<button value=\"Skill Balance\" action=\"bypass _bbs_skillbalance 1 false\" width=90 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">";
        html = html + "<button value=\"Oly Skill Balance\" action=\"bypass _bbs_skillbalance 1 true\" width=90 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">";
        html = html + "</center></body></html>";
        separateAndSend(html, activeChar);
    }

    private static String getBalanceInfo(Collection<ClassBalanceHolder> collection, int pageId, boolean search, boolean isOly) {
        if (collection == null)
            return "";
        StringBuilder info = new StringBuilder();
        int count = 1;
        int limitInPage = 7;
        for (ClassBalanceHolder balance : collection) {
            int classId = balance.getActiveClass();
            int targetClassId = balance.getTargetClass();
            String id = classId + ";" + classId;
            if ((!ClassId.getClassById(classId).name().isEmpty() && !ClassId.getClassById(targetClassId).name().isEmpty()) || !ClassId.getClassById(classId).name().isEmpty() || targetClassId == -1) {
                Set<Map.Entry<AttackType, Double>> localCollection = isOly ? balance.getOlyBalance().entrySet() : balance.getNormalBalance().entrySet();
                for (Map.Entry<AttackType, Double> dt : localCollection) {
                    if (count > limitInPage * (pageId - 1) && count <= limitInPage * pageId) {
                        double val = dt.getValue();
                        double percents = (Math.round(val * 100.0D) - 100L);
                        double addedValue = Math.round((val + 0.1D) * 10.0D) / 10.0D;
                        double removedValue = Math.round((val - 0.1D) * 10.0D) / 10.0D;
                        String attackTypeSt = dt.getKey().name();
                        if (attackTypeSt.equalsIgnoreCase("PhysicalSkillDamage")) {
                            attackTypeSt = "PSkillDamage";
                        } else if (attackTypeSt.equalsIgnoreCase("PhysicalSkillCritical")) {
                            attackTypeSt = "PSkillCritical";
                        }
                        String content = HtmCache.getInstance().getHtm("data/html/mods/balancer/classbalance/info-template.htm");
                        content = content.replace("<?pos?>", String.valueOf(count));
                        content = content.replace("<?classId?>", String.valueOf(classId));
                        content = content.replace("<?className?>", ClassId.getClassById(classId).name());
                        content = content.replace("<?type?>", attackTypeSt);
                        content = content.replace("<?key?>", id);
                        content = content.replace("<?targetClassId?>", String.valueOf(targetClassId));
                        content = content.replace("<?editedType?>", String.valueOf(dt.getKey().getId()));
                        content = content.replace("<?removedValue?>", String.valueOf(removedValue));
                        content = content.replace("<?search?>", String.valueOf(search));
                        content = content.replace("<?isoly?>", String.valueOf(isOly));
                        content = content.replace("<?addedValue?>", String.valueOf(addedValue));
                        content = content.replace("<?pageId?>", String.valueOf(pageId));
                        content = content.replace("<?targetClassName?>", (targetClassId <= -1) ? "ALL" : ((targetClassId == -1) ? "Monster" : ClassId.getClassById(targetClassId).name()));
                        content = content.replace("<?value?>", String.valueOf(val));
                        content = content.replace("<?percents?>", (percents > 0.0D) ? "+" : "");
                        content = content.replace("<?percentValue?>", String.valueOf(percents).substring(0, String.valueOf(percents).indexOf(".")));
                        info.append(info);
                    }
                    count++;
                }
            }
        }
        return info.toString();
    }

    public static ClassBalanceGui getInstance() {
        return SingletonHolder._instance;
    }

    public void parseCmd(String command, Player activeChar) {
        if (!activeChar.isGM())
            return;
        if (command.equals("_bbs_balancer")) {
            showMainWindow(activeChar);
        } else if (command.startsWith("_bbs_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = (st.countTokens() == 2) ? Integer.parseInt(st.nextToken()) : 1;
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            showMainHtml(activeChar, pageId, isOly);
        } else if (command.startsWith("_bbs_save_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            ClassBalanceManager.getInstance().store(activeChar);
            showMainHtml(activeChar, pageId, isOly);
        } else if (command.startsWith("_bbs_remove_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String key = st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            int type = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            ClassBalanceManager.getInstance().removeClassBalance(key, AttackType.VALUES[type], isOly);
            showMainHtml(activeChar, pageId, isOly);
        } else if (command.startsWith("_bbs_modify_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String[] array = st.nextToken().split(";");
            int classId = Integer.parseInt(array[0]);
            int targetClassId = Integer.parseInt(array[1]);
            int attackType = Integer.parseInt(array[2]);
            double value = Double.parseDouble(array[3]);
            int pageId = Integer.parseInt(array[4]);
            boolean isSearch = Boolean.parseBoolean(array[5]);
            boolean isOly = Boolean.parseBoolean(array[6]);
            String key = classId + ";" + classId;
            ClassBalanceHolder cbh = ClassBalanceManager.getInstance().getBalanceHolder(key);
            if (isOly) {
                cbh.addOlyBalance(AttackType.VALUES[attackType], value);
            } else {
                cbh.addNormalBalance(AttackType.VALUES[attackType], value);
            }
            ClassBalanceManager.getInstance().addClassBalance(key, cbh, true);
            if (isSearch) {
                showSearchHtml(activeChar, pageId, classId, isOly);
            } else {
                showMainHtml(activeChar, pageId, isOly);
            }
        } else if (command.startsWith("_bbs_add_menu_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            int race = Integer.parseInt(st.nextToken());
            int tRace = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            showAddHtml(activeChar, pageId, race, tRace, isOly);
        } else if (command.startsWith("_bbs_add_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String className = st.nextToken().trim();
            String attackTypeSt = st.nextToken();
            String val = st.nextToken();
            String targetClassName = st.nextToken().trim();
            boolean isoly = Boolean.parseBoolean(st.nextToken());
            int classId = -1;
            if (!className.isEmpty()) {
                ClassId[] values = ClassId.values();
                for (ClassId cId : values) {
                    if (cId.name().equalsIgnoreCase(className))
                        classId = cId.ordinal();
                }
            }
            int targetClassId = -1;
            if (!targetClassName.isEmpty())
                if (targetClassName.equals("All_Classes")) {
                    targetClassId = -3;
                } else {
                    ClassId[] values = ClassId.values();
                    for (ClassId cId : values) {
                        if (cId.name().equalsIgnoreCase(targetClassName))
                            targetClassId = cId.ordinal();
                    }
                }
            double value = Double.parseDouble(val);
            String key = classId + ";" + classId;
            ClassBalanceHolder cbh = (ClassBalanceManager.getInstance().getBalanceHolder(key) != null) ? ClassBalanceManager.getInstance().getBalanceHolder(key) : new ClassBalanceHolder(classId, targetClassId);
            if (attackTypeSt.equalsIgnoreCase("PSkillDamage")) {
                attackTypeSt = "PhysicalSkillDamage";
            } else if (attackTypeSt.equalsIgnoreCase("PSkillCritical")) {
                attackTypeSt = "PhysicalSkillCritical";
            }
            if (isoly) {
                cbh.addOlyBalance(AttackType.valueOf(attackTypeSt), value);
            } else {
                cbh.addNormalBalance(AttackType.valueOf(attackTypeSt), value);
            }
            ClassBalanceManager.getInstance().addClassBalance(key, cbh, false);
            showMainHtml(activeChar, 1, isoly);
        } else if (command.startsWith("_bbs_search_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() == 2) {
                int classId = Integer.parseInt(st.nextToken());
                boolean isOly = Boolean.parseBoolean(st.nextToken());
                showSearchHtml(activeChar, 1, classId, isOly);
            }
        } else if (command.startsWith("_bbs_search_nav_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() == 3) {
                int classId = Integer.parseInt(st.nextToken());
                int pageID = Integer.parseInt(st.nextToken());
                boolean isOly = Boolean.parseBoolean(st.nextToken());
                showSearchHtml(activeChar, pageID, classId, isOly);
            }
        }
    }

    public void showMainHtml(Player activeChar, int pageId, boolean isolyinfo) {
        String html = HtmCache.getInstance().getHtm("data/html/mods/balancer/classbalance/index.htm");
        String info = getBalanceInfo(ClassBalanceManager.getInstance().getAllBalances().values(), pageId, false, isolyinfo);
        int count = ClassBalanceManager.getInstance().getSize(isolyinfo);
        int limitInPage = 7;
        html = html.replace("<?title?>", isolyinfo ? "Olympiad" : "");
        html = html.replace("<?isoly?>", String.valueOf(isolyinfo));
        html = html.replace("%pageID%", String.valueOf(pageId));
        int totalpages = 1;
        int tmpcount = count;
        while (tmpcount - 7 > 0) {
            totalpages++;
            tmpcount -= 7;
        }
        html = html.replace("%totalPages%", String.valueOf(totalpages));
        html = html.replace("%info%", info);
        html = html.replace("%previousPage%", String.valueOf((pageId - 1 != 0) ? (pageId - 1) : 1));
        html = html.replace("%nextPage%", String.valueOf((pageId * limitInPage >= count) ? pageId : (pageId + 1)));
        separateAndSend(html, activeChar);
    }

    public void showAddHtml(Player activeChar, int pageId, int race, int tRace, boolean isOly) {
        String html = HtmCache.getInstance().getHtm("data/html/mods/balancer/classbalance/" + (isOly ? "olyadd.htm" : "add.htm"));
        StringBuilder classes = new StringBuilder();
        ClassId[] array = ClassId.values();
        for (ClassId classId : array) {
            if (classId.getRace() != null)
                if (isOly) {
                    if (classId.level() == 3 && classId.getRace().ordinal() == race)
                        classes.append(classes).append(";");
                } else if (classId.level() >= 2 && classId.getRace().ordinal() == race) {
                    classes.append(classes).append(";");
                }
        }
        StringBuilder tClasses = new StringBuilder();
        if (tRace != 6) {
            tClasses.append("All_Classes;");
            ClassId[] array2 = ClassId.values();
            for (ClassId classId : array2) {
                if (classId.getRace() != null)
                    if (isOly) {
                        if (classId.level() == 3 && classId.getRace().ordinal() == tRace)
                            tClasses.append(tClasses).append(";");
                    } else if (classId.level() >= 2 && classId.getRace().ordinal() == tRace) {
                        tClasses.append(tClasses).append(";");
                    }
            }
        } else {
            tClasses = new StringBuilder("Monsters");
        }
        html = html.replace("<?pageId?>", String.valueOf(pageId));
        html = html.replace("<?tRace?>", String.valueOf(tRace));
        html = html.replace("<?race0Checked?>", (race == 0) ? "_checked" : "");
        html = html.replace("<?race1Checked?>", (race == 1) ? "_checked" : "");
        html = html.replace("<?race2Checked?>", (race == 2) ? "_checked" : "");
        html = html.replace("<?race3Checked?>", (race == 3) ? "_checked" : "");
        html = html.replace("<?race4Checked?>", (race == 4) ? "_checked" : "");
        html = html.replace("<?race5Checked?>", (race == 5) ? "_checked" : "");
        html = html.replace("<?classes?>", classes.toString());
        html = html.replace("<?tClasses?>", tClasses.toString());
        html = html.replace("<?race?>", String.valueOf(race));
        html = html.replace("<?trace0Checked?>", (tRace == 0) ? "_checked" : "");
        html = html.replace("<?trace1Checked?>", (tRace == 1) ? "_checked" : "");
        html = html.replace("<?trace2Checked?>", (tRace == 2) ? "_checked" : "");
        html = html.replace("<?trace3Checked?>", (tRace == 3) ? "_checked" : "");
        html = html.replace("<?trace4Checked?>", (tRace == 4) ? "_checked" : "");
        html = html.replace("<?trace5Checked?>", (tRace == 5) ? "_checked" : "");
        html = html.replace("<?trace6Checked?>", (tRace == 6) ? "_checked" : "");
        html = html.replace("<?isoly?>", String.valueOf(isOly));
        separateAndSend(html, activeChar);
    }

    public void showSearchHtml(Player activeChar, int pageId, int sclassId, boolean isolysearch) {
        String html = HtmCache.getInstance().getHtm("data/html/mods/balancer/classbalance/search.htm");
        String info = getBalanceInfo(ClassBalanceManager.getInstance().getClassBalances(sclassId), pageId, true, isolysearch);
        int count = ClassBalanceManager.getInstance().getClassBalanceSize(sclassId, isolysearch);
        int limitInPage = 7;
        html = html.replace("%pageID%", String.valueOf(pageId));
        int totalpages = 1;
        int tmpcount = count;
        while (tmpcount - 7 > 0) {
            totalpages++;
            tmpcount -= 7;
        }
        html = html.replace("<?title?>", isolysearch ? "Olympiad" : "");
        html = html.replace("<?isoly?>", String.valueOf(isolysearch));
        html = html.replace("%totalPages%", String.valueOf(totalpages));
        html = html.replace("%info%", info);
        html = html.replace("%classID%", String.valueOf(sclassId));
        html = html.replace("%previousPage%", String.valueOf((pageId - 1 != 0) ? (pageId - 1) : 1));
        html = html.replace("%nextPage%", String.valueOf((pageId * limitInPage >= count) ? pageId : (pageId + 1)));
        separateAndSend(html, activeChar);
    }

    private static class SingletonHolder {
        protected static final ClassBalanceGui _instance = new ClassBalanceGui();
    }
}
