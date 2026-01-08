package enginemods.main.engine.mods;

import enginemods.main.data.PlayerData;
import enginemods.main.data.SkillData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.packets.PrivateCustomTitle;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

import java.util.StringTokenizer;

public class SellBuffs extends AbstractMods {
    public SellBuffs() {
        this.registerMod(true);
    }

    public static SellBuffs getInstance() {
        return SellBuffs.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public boolean onVoicedCommand(Player player, String chat) {
        if (chat.startsWith("cancelsellbuff")) {
            if (!PlayerData.get(player).isSellBuff()) {
                return true;
            } else {
                PlayerData.get(player).setSellBuff(false);
                PlayerData.get(player).setSellBuffPrice(0);
                player.standUp();
                player.setIsImmobilized(false);
                player.setTeam(TeamType.NONE);
                player.broadcastUserInfo();
                return true;
            }
        } else if (chat.startsWith("sellbuff")) {
            if (!player.isInsideZone(ZoneId.PEACE)) {
                return true;
            } else {
                if (player.getTarget() != player) {
                    player.setTarget(player);
                }

                HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
                hb.append("<html><body><center>");
                hb.append(Html.headHtml("SELL BUFF"));
                hb.append(new Object[]{"<font color=\"LEVEL\">Welcome </font><font color=\"00C3FF\">", player.getName(), "</font> system selling buffs.<br1>"});
                hb.append("You can only sell buffs if your class<br1>");
                hb.append("is the type of support.<br1>");
                hb.append("All your buffs will be sold at a single price.<br1>");
                hb.append("To cancel this state should use<br1>");
                hb.append("the command. <font color=\"LEVEL\">CancelSellBuffs</font>");
                hb.append("<br><br>");
                hb.append("<font color=\"00C3FF\">Choose the price of your services</font>");
                hb.append("<br>");
                hb.append("<edit var=\"price\" width=\"200\" height=\"15\">");
                hb.append("<br>");
                hb.append("<button value=\"Next\" action=\"bypass -h Engine SellBuffs sell $price\" width=\"80\" height=\"25\" back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.btn1_normal\">");
                hb.append("</center></body></html>");
                sendHtml(player, null, hb);
                return true;
            }
        } else {
            return false;
        }
    }

    public void onEvent(Player player, Creature character, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        switch (st.nextToken()) {
            case "sendPacket":
                if (PlayerData.get(player).isSellBuff()) {
                    character.sendPacket(new PrivateCustomTitle(player, PrivateCustomTitle.TitleType.SELL, "SellBuffs"));
                }
                break;
            case "view":
                int page = 1;
                if (st.hasMoreTokens()) {
                    page = Integer.parseInt(st.nextToken());
                }

                this.getBuffList(player, player.getTarget(), page);
                break;
            case "sell":
                int price = 0;

                try {
                    price = Integer.parseInt(st.nextToken());
                } catch (Exception ignored) {
                }

                PlayerData.get(player).setSellBuff(true);
                PlayerData.get(player).setSellBuffPrice(price);
                player.sitDown();
                player.setIsImmobilized(true);
                player.setTeam(TeamType.BLUE);
                player.broadcastUserInfo();
                player.broadcastPacket(new PrivateCustomTitle(player, PrivateCustomTitle.TitleType.SELL, "SellBuffs"));
                break;
            case "buy":
                int id = Integer.parseInt(st.nextToken());
                int lvl = Integer.parseInt(st.nextToken());
                String sellerName = st.nextToken();
                Player sellerBuff = World.getInstance().getPlayer(sellerName);
                if (sellerBuff == null) {
                    return;
                }

                if (!player.isInsideRadius(sellerBuff, 500, false, false)) {
                    return;
                }

                PlayerHolder ph = PlayerData.get(sellerBuff.getObjectId());
                if (ph == null || !ph.isSellBuff()) {
                    return;
                }

                if (sellerBuff.getSkillLevel(id) != lvl) {
                    return;
                }

                price = ph.getSellBuffPrice();
                if (!player.reduceAdena("sell buff", price, sellerBuff, true)) {
                    return;
                }

                sellerBuff.addAdena("sell buff", price, player, true);
                sellerBuff.setTarget(player);
                L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
                skill.getEffects(player, player);
                sellerBuff.setCurrentMp(sellerBuff.getMaxMp());
                page = 1;
                if (st.hasMoreTokens()) {
                    page = Integer.parseInt(st.nextToken());
                }

                this.getBuffList(player, sellerBuff, page);
        }

    }

    public boolean onInteract(Player player, Creature sellerBuff) {
        PlayerHolder ph = PlayerData.get(sellerBuff.getObjectId());
        if (ph != null && ph.isSellBuff()) {
            if (!player.isInsideRadius(sellerBuff, 500, false, false)) {
                return false;
            } else {
                HtmlBuilder tb = new HtmlBuilder();
                tb.append("<html><body>");
                tb.append(Html.headHtml("SELL BUFF"));
                tb.append("<center>");
                tb.append("<br><br>");
                tb.append(new Object[]{"Hello <font color=\"00C3FF\">", player.getName(), "</font>"});
                tb.append(new Object[]{"<br><center>My Buff Cost: <font color=\"LEVEL\">", ph.getSellBuffPrice(), "</font> adena each!</center>"});
                tb.append("<br>");
                tb.append("<center><button value=\"View my Buffs\" action=\"bypass -h Engine SellBuffs view\" width=\"80\" height=\"25\" back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.btn1_normal\">");
                tb.append("</center>");
                tb.append("</body></html>");
                sendHtml(player, null, tb);
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return true;
            }
        } else {
            return false;
        }
    }

    private void getBuffList(Player buyer, WorldObject sellerBuff, int page) {
        PlayerHolder ph = PlayerData.get(sellerBuff.getObjectId());
        if (ph != null && ph.isSellBuff()) {
            HtmlBuilder tb = new HtmlBuilder();
            tb.append("<html><body>");
            tb.append("<br><br>");
            tb.append("<center>");
            tb.append(new Object[]{"<font color=\"LEVEL\">Hello </font><font color=\"00C3FF\">", buyer.getName(), "</font><font color=\"LEVEL\"> want my Buff!</font>"});
            tb.append(new Object[]{"<br>My Buff Cost: <font color=\"00C3FF\">", ph.getSellBuffPrice(), "</font><font color=\"LEVEL\"> adena each!</font><br>"});
            int MAX_SKILL_PER_PAGE = 12;
            int searchPage = MAX_SKILL_PER_PAGE * (page - 1);
            int skillCount = 0;
            int skillBuffs = 0;

            for (L2Skill sk : ((Creature) sellerBuff).getSkills().values()) {
                if (!sk.isPassive() && sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF && !sk.isOffensive() && sk.getSkillType() == L2SkillType.BUFF) {
                    ++skillBuffs;
                    if (skillCount < searchPage) {
                        ++skillCount;
                    } else if (skillCount < searchPage + MAX_SKILL_PER_PAGE) {
                        tb.append("<table>");
                        tb.append("<tr>");
                        tb.append(new Object[]{"<td width=\"32\"><center><img src=\"", SkillData.getSkillIcon(sk.getId()), "\" width=\"32\" height=\"16\"></center></td>"});
                        tb.append(new Object[]{"<td width=\"180\"><center><a action=\"bypass -h Engine SellBuffs buy ", sk.getId(), " ", sk.getLevel(), " ", sellerBuff.getName(), " ", page, "\">", sk.getName(), "</center></td>"});
                        tb.append(new Object[]{"<td width=\"32\"><center><img src=\"", SkillData.getSkillIcon(sk.getId()), "\" width=\"32\" height=\"16\"></center></td>"});
                        tb.append("</tr>");
                        tb.append("</table>");
                        ++skillCount;
                    }
                }
            }

            tb.append("<center>");
            tb.append("<img src=\"L2UI.SquareGray\" width=\"264\" height=\"1\">");
            tb.append("<table bgcolor=CC99FF>");
            tb.append("<tr>");
            int currentPage = 1;

            for (int i = 0; i < skillBuffs; ++i) {
                if (i % MAX_SKILL_PER_PAGE == 0) {
                    tb.append("<td width=\"18\"><center><a action=\"bypass -h Engine SellBuffs view " + currentPage + "\">" + currentPage + "</center></a></td>");
                    ++currentPage;
                }
            }

            tb.append("</tr>");
            tb.append("</table>");
            tb.append("<img src=\"L2UI.SquareGray\" width=\"264\" height=\"1\">");
            tb.append("</center>");
            tb.append("</body></html>");
            sendHtml(buyer, null, tb);
        }
    }

    private static class SingletonHolder {
        protected static final SellBuffs INSTANCE = new SellBuffs();
    }
}
