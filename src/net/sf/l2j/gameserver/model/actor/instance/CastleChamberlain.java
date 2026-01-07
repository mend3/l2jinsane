package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class CastleChamberlain extends Merchant {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    protected static final int COND_OWNER = 2;
    protected static final int COND_CLAN_MEMBER = 3;
    private static final int CERTIFICATES_BUNDLE = 10;
    private static final int CERTIFICATES_PRICE = 1000;
    private int _preHour = 6;

    public CastleChamberlain(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static int getDoorCost(int type, int level) {
        int price = 0;
        switch (type) {
            case 1:
                switch (level) {
                    case 2:
                        price = 300000;
                        break;
                    case 3:
                        price = 400000;
                        break;
                    case 5:
                        price = 500000;
                        break;
                }
                break;
            case 2:
                switch (level) {
                    case 2:
                        price = 750000;
                        break;
                    case 3:
                        price = 900000;
                        break;
                    case 5:
                        price = 1000000;
                        break;
                }
                break;
            case 3:
                switch (level) {
                    case 2:
                        price = 1600000;
                        break;
                    case 3:
                        price = 1800000;
                        break;
                    case 5:
                        price = 2000000;
                        break;
                }
                break;
        }
        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DUSK:
                price *= 3;
                break;
            case DAWN:
                price = (int) (price * 0.8D);
                break;
        }
        return price;
    }

    private static int getTrapCost(int level) {
        int price = 0;
        switch (level) {
            case 1:
                price = 3000000;
                break;
            case 2:
                price = 4000000;
                break;
            case 3:
                price = 5000000;
                break;
            case 4:
                price = 6000000;
                break;
        }
        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DUSK:
                price *= 3;
                break;
            case DAWN:
                price = (int) (price * 0.8D);
                break;
        }
        return price;
    }

    public void onBypassFeedback(Player player, String command) {
        int cond = validateCondition(player);
        if (cond < 2) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile((cond == 1) ? "data/html/chamberlain/busy.htm" : "data/html/chamberlain/noprivs.htm");
            player.sendPacket(html);
            return;
        }
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();
        String val = "";
        if (st.hasMoreTokens())
            val = st.nextToken();
        if (actualCommand.equalsIgnoreCase("banish_foreigner")) {
            if (!validatePrivileges(player, 524288))
                return;
            getCastle().banishForeigners();
            sendFileMessage(player, "data/html/chamberlain/banishafter.htm");
        } else if (actualCommand.equalsIgnoreCase("banish_foreigner_show")) {
            if (!validatePrivileges(player, 524288))
                return;
            sendFileMessage(player, "data/html/chamberlain/banishfore.htm");
        } else if (actualCommand.equalsIgnoreCase("manage_functions")) {
            if (!validatePrivileges(player, 4194304))
                return;
            sendFileMessage(player, "data/html/chamberlain/manage.htm");
        } else if (actualCommand.equalsIgnoreCase("products")) {
            if (!validatePrivileges(player, 262144))
                return;
            sendFileMessage(player, "data/html/chamberlain/products.htm");
        } else if (actualCommand.equalsIgnoreCase("list_siege_clans")) {
            if (!validatePrivileges(player, 131072))
                return;
            player.sendPacket(new SiegeInfo(getCastle()));
        } else if (actualCommand.equalsIgnoreCase("receive_report")) {
            if (cond == 3) {
                sendFileMessage(player, "data/html/chamberlain/noprivs.htm");
            } else {
                Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/chamberlain/report.htm");
                html.replace("%objectId%", getObjectId());
                html.replace("%clanname%", clan.getName());
                html.replace("%clanleadername%", clan.getLeaderName());
                html.replace("%castlename%", getCastle().getName());
                html.replace("%ss_event%", SevenSignsManager.getInstance().getCurrentPeriod().getName());
                switch (SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE)) {
                    case NORMAL:
                        html.replace("%ss_avarice%", "Not in Possession");
                        break;
                    case DAWN:
                        html.replace("%ss_avarice%", "Lords of Dawn");
                        break;
                    case DUSK:
                        html.replace("%ss_avarice%", "Revolutionaries of Dusk");
                        break;
                }
                switch (SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS)) {
                    case NORMAL:
                        html.replace("%ss_gnosis%", "Not in Possession");
                        break;
                    case DAWN:
                        html.replace("%ss_gnosis%", "Lords of Dawn");
                        break;
                    case DUSK:
                        html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
                        break;
                }
                switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
                    case NORMAL:
                        html.replace("%ss_strife%", "Not in Possession");
                        break;
                    case DAWN:
                        html.replace("%ss_strife%", "Lords of Dawn");
                        break;
                    case DUSK:
                        html.replace("%ss_strife%", "Revolutionaries of Dusk");
                        break;
                }
                player.sendPacket(html);
            }
        } else if (actualCommand.equalsIgnoreCase("items")) {
            if (!validatePrivileges(player, 262144))
                return;
            if (val.isEmpty())
                return;
            showBuyWindow(player, Integer.parseInt(val + "1"));
        } else if (actualCommand.equalsIgnoreCase("manage_siege_defender")) {
            if (!validatePrivileges(player, 131072))
                return;
            player.sendPacket(new SiegeInfo(getCastle()));
        } else if (actualCommand.equalsIgnoreCase("manage_vault")) {
            if (!validatePrivileges(player, 1048576))
                return;
            String filename = "data/html/chamberlain/vault.htm";
            int amount = 0;
            if (val.equalsIgnoreCase("deposit")) {
                try {
                    amount = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException noSuchElementException) {
                }
                if (amount > 0 && getCastle().getTreasury() + amount < 2147483647L)
                    if (player.reduceAdena("Castle", amount, this, true))
                        getCastle().addToTreasuryNoTax(amount);
            } else if (val.equalsIgnoreCase("withdraw")) {
                try {
                    amount = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException noSuchElementException) {
                }
                if (amount > 0)
                    if (getCastle().getTreasury() < amount) {
                        filename = "data/html/chamberlain/vault-no.htm";
                    } else if (getCastle().addToTreasuryNoTax((-1 * amount))) {
                        player.addAdena("Castle", amount, this, true);
                    }
            }
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(filename);
            html.replace("%objectId%", getObjectId());
            html.replace("%tax_income%", StringUtil.formatNumber(getCastle().getTreasury()));
            html.replace("%withdraw_amount%", StringUtil.formatNumber(amount));
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("operate_door")) {
            if (!validatePrivileges(player, 32768))
                return;
            if (val.isEmpty()) {
                NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
                npcHtmlMessage.setFile("data/html/chamberlain/" + getNpcId() + "-d.htm");
                npcHtmlMessage.replace("%objectId%", getObjectId());
                player.sendPacket(npcHtmlMessage);
                return;
            }
            boolean open = (Integer.parseInt(val) == 1);
            while (st.hasMoreTokens())
                getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(open ? "data/html/chamberlain/doors-open.htm" : "data/html/chamberlain/doors-close.htm");
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("tax_set")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (!validatePrivileges(player, 1048576)) {
                html.setFile("data/html/chamberlain/tax.htm");
            } else {
                if (!val.isEmpty())
                    getCastle().setTaxPercent(player, Integer.parseInt(val));
                html.setFile("data/html/chamberlain/tax-adjust.htm");
            }
            html.replace("%objectId%", getObjectId());
            html.replace("%tax%", getCastle().getTaxPercent());
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("manor")) {
            if (!validatePrivileges(player, 65536))
                return;
            String filename = "";
            if (!Config.ALLOW_MANOR) {
                filename = "data/html/npcdefault.htm";
            } else {
                int cmd = Integer.parseInt(val);
                switch (cmd) {
                    case 0:
                        filename = "data/html/chamberlain/manor/manor.htm";
                        break;
                    case 4:
                        filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
                        break;
                    default:
                        filename = "data/html/chamberlain/no.htm";
                        break;
                }
            }
            if (filename.length() != 0) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile(filename);
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            }
        } else if (command.startsWith("manor_menu_select")) {
            if (!validatePrivileges(player, 65536))
                return;
            CastleManorManager manor = CastleManorManager.getInstance();
            if (manor.isUnderMaintenance()) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
                return;
            }
            String params = command.substring(command.indexOf("?") + 1);
            StringTokenizer str = new StringTokenizer(params, "&");
            int ask = Integer.parseInt(str.nextToken().split("=")[1]);
            int state = Integer.parseInt(str.nextToken().split("=")[1]);
            boolean time = str.nextToken().split("=")[1].equals("1");
            int castleId = (state == -1) ? getCastle().getCastleId() : state;
            switch (ask) {
                case 3:
                    player.sendPacket(new ExShowSeedInfo(castleId, time, true));
                    break;
                case 4:
                    player.sendPacket(new ExShowCropInfo(castleId, time, true));
                    break;
                case 5:
                    player.sendPacket(new ExShowManorDefaultInfo(true));
                    break;
                case 7:
                    if (manor.isManorApproved()) {
                        player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
                        break;
                    }
                    player.sendPacket(new ExShowSeedSetting(castleId));
                    break;
                case 8:
                    if (manor.isManorApproved()) {
                        player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
                        break;
                    }
                    player.sendPacket(new ExShowCropSetting(castleId));
                    break;
            }
        } else if (actualCommand.equalsIgnoreCase("siege_change")) {
            if (!validatePrivileges(player, 131072))
                return;
            if (getCastle().getSiege().getSiegeRegistrationEndDate() < Calendar.getInstance().getTimeInMillis()) {
                sendFileMessage(player, "data/html/chamberlain/siegetime1.htm");
            } else if (getCastle().getSiege().isTimeRegistrationOver()) {
                sendFileMessage(player, "data/html/chamberlain/siegetime2.htm");
            } else {
                sendFileMessage(player, "data/html/chamberlain/siegetime3.htm");
            }
        } else if (actualCommand.equalsIgnoreCase("siege_time_set")) {
            switch (Integer.parseInt(val)) {
                case 1:
                    this._preHour = Integer.parseInt(st.nextToken());
                    break;
            }
            if (this._preHour != 6) {
                getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, this._preHour + 12);
                getCastle().getSiege().endTimeRegistration(false);
                sendFileMessage(player, "data/html/chamberlain/siegetime8.htm");
                return;
            }
            sendFileMessage(player, "data/html/chamberlain/siegetime6.htm");
        } else if (actualCommand.equals("give_crown")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (cond == 2) {
                if (player.getInventory().getItemByItemId(6841) == null) {
                    player.addItem("Castle Crown", 6841, 1, player, true);
                    html.setFile("data/html/chamberlain/gavecrown.htm");
                    html.replace("%CharName%", player.getName());
                    html.replace("%FeudName%", getCastle().getName());
                } else {
                    html.setFile("data/html/chamberlain/hascrown.htm");
                }
            } else {
                html.setFile("data/html/chamberlain/noprivs.htm");
            }
            player.sendPacket(html);
        } else if (actualCommand.equals("manor_certificate")) {
            if (!validatePrivileges(player, 262144))
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.DUSK) {
                    html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
                } else if (getCastle().getLeftCertificates() == 0) {
                    html.setFile("data/html/chamberlain/not-enough-ticket.htm");
                } else {
                    html.setFile("data/html/chamberlain/sell-dawn-ticket.htm");
                    html.replace("%left%", getCastle().getLeftCertificates());
                    html.replace("%bundle%", 10);
                    html.replace("%price%", 1000);
                }
            } else {
                html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (actualCommand.equals("validate_certificate")) {
            if (!validatePrivileges(player, 262144))
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.DUSK) {
                    html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
                } else if (getCastle().getLeftCertificates() == 0) {
                    html.setFile("data/html/chamberlain/not-enough-ticket.htm");
                } else if (player.reduceAdena("Certificate", 10000, this, true)) {
                    player.addItem("Certificate", 6388, 10, this, true);
                    getCastle().setLeftCertificates(getCastle().getLeftCertificates() - 10, true);
                    html.setFile("data/html/chamberlain/sell-dawn-ticket.htm");
                    html.replace("%left%", getCastle().getLeftCertificates());
                    html.replace("%bundle%", 10);
                    html.replace("%price%", 1000);
                } else {
                    html.setFile("data/html/chamberlain/not-enough-adena.htm");
                }
            } else {
                html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("castle_devices")) {
            if (!validatePrivileges(player, 4194304))
                return;
            sendFileMessage(player, "data/html/chamberlain/devices.htm");
        } else if (actualCommand.equalsIgnoreCase("doors_update")) {
            if (!validatePrivileges(player, 4194304))
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (val.isEmpty()) {
                html.setFile("data/html/chamberlain/" + getNpcId() + "-gu.htm");
            } else {
                html.setFile("data/html/chamberlain/doors-update.htm");
                html.replace("%id%", val);
                html.replace("%type%", st.nextToken());
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("doors_choose_upgrade")) {
            if (!validatePrivileges(player, 4194304))
                return;
            String id = val;
            String type = st.nextToken();
            String level = st.nextToken();
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/chamberlain/doors-confirm.htm");
            html.replace("%objectId%", getObjectId());
            html.replace("%id%", id);
            html.replace("%level%", level);
            html.replace("%type%", type);
            html.replace("%price%", getDoorCost(Integer.parseInt(type), Integer.parseInt(level)));
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("doors_confirm_upgrade")) {
            if (!validatePrivileges(player, 4194304))
                return;
            int type = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            int price = getDoorCost(type, level);
            if (price == 0)
                return;
            int id = Integer.parseInt(val);
            Door door = getCastle().getDoor(id);
            if (door == null)
                return;
            int currentHpRatio = door.getStat().getUpgradeHpRatio();
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (currentHpRatio >= level) {
                html.setFile("data/html/chamberlain/doors-already-updated.htm");
                html.replace("%level%", currentHpRatio * 100);
            } else if (!player.reduceAdena("doors_upgrade", price, player, true)) {
                html.setFile("data/html/chamberlain/not-enough-adena.htm");
            } else {
                getCastle().upgradeDoor(id, level, true);
                html.setFile("data/html/chamberlain/doors-success.htm");
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("traps_update")) {
            if (!validatePrivileges(player, 4194304))
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (val.isEmpty()) {
                html.setFile("data/html/chamberlain/" + getNpcId() + "-tu.htm");
            } else {
                html.setFile("data/html/chamberlain/traps-update" + (getCastle().getName().equalsIgnoreCase("aden") ? "1" : "") + ".htm");
                html.replace("%trapIndex%", val);
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("traps_choose_upgrade")) {
            if (!validatePrivileges(player, 4194304))
                return;
            String trapIndex = val;
            String level = st.nextToken();
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/chamberlain/traps-confirm.htm");
            html.replace("%objectId%", getObjectId());
            html.replace("%trapIndex%", trapIndex);
            html.replace("%level%", level);
            html.replace("%price%", getTrapCost(Integer.parseInt(level)));
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("traps_confirm_upgrade")) {
            if (!validatePrivileges(player, 4194304))
                return;
            int level = Integer.parseInt(st.nextToken());
            int price = getTrapCost(level);
            if (price == 0)
                return;
            int trapIndex = Integer.parseInt(val);
            int currentLevel = getCastle().getTrapUpgradeLevel(trapIndex);
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (currentLevel >= level) {
                html.setFile("data/html/chamberlain/traps-already-updated.htm");
                html.replace("%level%", currentLevel);
            } else if (!player.reduceAdena("traps_upgrade", price, player, true)) {
                html.setFile("data/html/chamberlain/not-enough-adena.htm");
            } else {
                getCastle().setTrapUpgrade(trapIndex, level, true);
                html.setFile("data/html/chamberlain/traps-success.htm");
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/chamberlain/no.htm";
        int condition = validateCondition(player);
        if (condition > 0)
            if (condition == 1) {
                filename = "data/html/chamberlain/busy.htm";
            } else if (condition >= 2) {
                filename = "data/html/chamberlain/chamberlain.htm";
            }
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
    }

    protected int validateCondition(Player player) {
        if (getCastle() != null && player.getClan() != null) {
            if (getCastle().getSiege().isInProgress())
                return 1;
            if (getCastle().getOwnerId() == player.getClanId()) {
                if (player.isClanLeader())
                    return 2;
                return 3;
            }
        }
        return 0;
    }

    private boolean validatePrivileges(Player player, int privilege) {
        if ((player.getClanPrivileges() & privilege) != privilege) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/chamberlain/noprivs.htm");
            player.sendPacket(html);
            return false;
        }
        return true;
    }

    private void sendFileMessage(Player player, String htmlMessage) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(htmlMessage);
        html.replace("%objectId%", getObjectId());
        html.replace("%npcId%", getNpcId());
        html.replace("%time%", getCastle().getSiegeDate().getTime().toString());
        player.sendPacket(html);
    }
}
