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
        int price;
        price = 0;
        label36:
        switch (type) {
            case 1:
                switch (level) {
                    case 2:
                        price = 300000;
                        break label36;
                    case 3:
                        price = 400000;
                    case 4:
                    default:
                        break label36;
                    case 5:
                        price = 500000;
                        break label36;
                }
            case 2:
                switch (level) {
                    case 2:
                        price = 750000;
                        break label36;
                    case 3:
                        price = 900000;
                    case 4:
                    default:
                        break label36;
                    case 5:
                        price = 1000000;
                        break label36;
                }
            case 3:
                switch (level) {
                    case 2:
                        price = 1600000;
                        break;
                    case 3:
                        price = 1800000;
                    case 4:
                    default:
                        break;
                    case 5:
                        price = 2000000;
                }
        }

        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DAWN -> price = (int) ((double) price * 0.8);
            case DUSK -> price *= 3;
        }

        return price;
    }

    private static int getTrapCost(int level) {
        int price = 0;
        switch (level) {
            case 1 -> price = 3000000;
            case 2 -> price = 4000000;
            case 3 -> price = 5000000;
            case 4 -> price = 6000000;
        }

        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DAWN -> price = (int) ((double) price * 0.8);
            case DUSK -> price *= 3;
        }

        return price;
    }

    public void onBypassFeedback(Player player, String command) {
        int cond = this.validateCondition(player);
        if (cond < 2) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(cond == 1 ? "data/html/chamberlain/busy.htm" : "data/html/chamberlain/noprivs.htm");
            player.sendPacket(html);
        } else {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken();
            String val = "";
            if (st.hasMoreTokens()) {
                val = st.nextToken();
            }

            if (actualCommand.equalsIgnoreCase("banish_foreigner")) {
                if (!this.validatePrivileges(player, 524288)) {
                    return;
                }

                this.getCastle().banishForeigners();
                this.sendFileMessage(player, "data/html/chamberlain/banishafter.htm");
            } else if (actualCommand.equalsIgnoreCase("banish_foreigner_show")) {
                if (!this.validatePrivileges(player, 524288)) {
                    return;
                }

                this.sendFileMessage(player, "data/html/chamberlain/banishfore.htm");
            } else if (actualCommand.equalsIgnoreCase("manage_functions")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                this.sendFileMessage(player, "data/html/chamberlain/manage.htm");
            } else if (actualCommand.equalsIgnoreCase("products")) {
                if (!this.validatePrivileges(player, 262144)) {
                    return;
                }

                this.sendFileMessage(player, "data/html/chamberlain/products.htm");
            } else if (actualCommand.equalsIgnoreCase("list_siege_clans")) {
                if (!this.validatePrivileges(player, 131072)) {
                    return;
                }

                player.sendPacket(new SiegeInfo(this.getCastle()));
            } else if (actualCommand.equalsIgnoreCase("receive_report")) {
                if (cond == 3) {
                    this.sendFileMessage(player, "data/html/chamberlain/noprivs.htm");
                } else {
                    Clan clan = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
                    NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                    html.setFile("data/html/chamberlain/report.htm");
                    html.replace("%objectId%", this.getObjectId());
                    html.replace("%clanname%", clan.getName());
                    html.replace("%clanleadername%", clan.getLeaderName());
                    html.replace("%castlename%", this.getCastle().getName());
                    html.replace("%ss_event%", SevenSignsManager.getInstance().getCurrentPeriod().getName());
                    switch (SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE)) {
                        case NORMAL -> html.replace("%ss_avarice%", "Not in Possession");
                        case DAWN -> html.replace("%ss_avarice%", "Lords of Dawn");
                        case DUSK -> html.replace("%ss_avarice%", "Revolutionaries of Dusk");
                    }

                    switch (SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS)) {
                        case NORMAL -> html.replace("%ss_gnosis%", "Not in Possession");
                        case DAWN -> html.replace("%ss_gnosis%", "Lords of Dawn");
                        case DUSK -> html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
                    }

                    switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
                        case NORMAL -> html.replace("%ss_strife%", "Not in Possession");
                        case DAWN -> html.replace("%ss_strife%", "Lords of Dawn");
                        case DUSK -> html.replace("%ss_strife%", "Revolutionaries of Dusk");
                    }

                    player.sendPacket(html);
                }
            } else if (actualCommand.equalsIgnoreCase("items")) {
                if (!this.validatePrivileges(player, 262144)) {
                    return;
                }

                if (val.isEmpty()) {
                    return;
                }

                this.showBuyWindow(player, Integer.parseInt(val + "1"));
            } else if (actualCommand.equalsIgnoreCase("manage_siege_defender")) {
                if (!this.validatePrivileges(player, 131072)) {
                    return;
                }

                player.sendPacket(new SiegeInfo(this.getCastle()));
            } else if (actualCommand.equalsIgnoreCase("manage_vault")) {
                if (!this.validatePrivileges(player, 1048576)) {
                    return;
                }

                String filename = "data/html/chamberlain/vault.htm";
                int amount = 0;
                if (val.equalsIgnoreCase("deposit")) {
                    try {
                        amount = Integer.parseInt(st.nextToken());
                    } catch (NoSuchElementException ignored) {
                    }

                    if (amount > 0 && this.getCastle().getTreasury() + (long) amount < 2147483647L && player.reduceAdena("Castle", amount, this, true)) {
                        this.getCastle().addToTreasuryNoTax(amount);
                    }
                } else if (val.equalsIgnoreCase("withdraw")) {
                    try {
                        amount = Integer.parseInt(st.nextToken());
                    } catch (NoSuchElementException ignored) {
                    }

                    if (amount > 0) {
                        if (this.getCastle().getTreasury() < (long) amount) {
                            filename = "data/html/chamberlain/vault-no.htm";
                        } else if (this.getCastle().addToTreasuryNoTax(-1 * amount)) {
                            player.addAdena("Castle", amount, this, true);
                        }
                    }
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile(filename);
                html.replace("%objectId%", this.getObjectId());
                html.replace("%tax_income%", StringUtil.formatNumber(this.getCastle().getTreasury()));
                html.replace("%withdraw_amount%", StringUtil.formatNumber(amount));
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("operate_door")) {
                if (!this.validatePrivileges(player, 32768)) {
                    return;
                }

                if (val.isEmpty()) {
                    NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                    html.setFile("data/html/chamberlain/" + this.getNpcId() + "-d.htm");
                    html.replace("%objectId%", this.getObjectId());
                    player.sendPacket(html);
                    return;
                }

                boolean open = Integer.parseInt(val) == 1;

                while (st.hasMoreTokens()) {
                    this.getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile(open ? "data/html/chamberlain/doors-open.htm" : "data/html/chamberlain/doors-close.htm");
                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("tax_set")) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (!this.validatePrivileges(player, 1048576)) {
                    html.setFile("data/html/chamberlain/tax.htm");
                } else {
                    if (!val.isEmpty()) {
                        this.getCastle().setTaxPercent(player, Integer.parseInt(val));
                    }

                    html.setFile("data/html/chamberlain/tax-adjust.htm");
                }

                html.replace("%objectId%", this.getObjectId());
                html.replace("%tax%", this.getCastle().getTaxPercent());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("manor")) {
                if (!this.validatePrivileges(player, 65536)) {
                    return;
                }

                String filename = "";
                if (!Config.ALLOW_MANOR) {
                    filename = "data/html/npcdefault.htm";
                } else {
                    int cmd = Integer.parseInt(val);
                    switch (cmd) {
                        case 0 -> filename = "data/html/chamberlain/manor/manor.htm";
                        case 4 -> filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
                        default -> filename = "data/html/chamberlain/no.htm";
                    }
                }

                if (filename.length() != 0) {
                    NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                    html.setFile(filename);
                    html.replace("%objectId%", this.getObjectId());
                    player.sendPacket(html);
                }
            } else if (command.startsWith("manor_menu_select")) {
                if (!this.validatePrivileges(player, 65536)) {
                    return;
                }

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
                int castleId = state == -1 ? this.getCastle().getCastleId() : state;
                switch (ask) {
                    case 3:
                        player.sendPacket(new ExShowSeedInfo(castleId, time, true));
                        break;
                    case 4:
                        player.sendPacket(new ExShowCropInfo(castleId, time, true));
                        break;
                    case 5:
                        player.sendPacket(new ExShowManorDefaultInfo(true));
                    case 6:
                    default:
                        break;
                    case 7:
                        if (manor.isManorApproved()) {
                            player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
                        } else {
                            player.sendPacket(new ExShowSeedSetting(castleId));
                        }
                        break;
                    case 8:
                        if (manor.isManorApproved()) {
                            player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
                        } else {
                            player.sendPacket(new ExShowCropSetting(castleId));
                        }
                }
            } else if (actualCommand.equalsIgnoreCase("siege_change")) {
                if (!this.validatePrivileges(player, 131072)) {
                    return;
                }

                if (this.getCastle().getSiege().getSiegeRegistrationEndDate() < Calendar.getInstance().getTimeInMillis()) {
                    this.sendFileMessage(player, "data/html/chamberlain/siegetime1.htm");
                } else if (this.getCastle().getSiege().isTimeRegistrationOver()) {
                    this.sendFileMessage(player, "data/html/chamberlain/siegetime2.htm");
                } else {
                    this.sendFileMessage(player, "data/html/chamberlain/siegetime3.htm");
                }
            } else if (actualCommand.equalsIgnoreCase("siege_time_set")) {
                switch (Integer.parseInt(val)) {
                    case 1:
                        this._preHour = Integer.parseInt(st.nextToken());
                    default:
                        if (this._preHour != 6) {
                            this.getCastle().getSiegeDate().set(11, this._preHour + 12);
                            this.getCastle().getSiege().endTimeRegistration(false);
                            this.sendFileMessage(player, "data/html/chamberlain/siegetime8.htm");
                            return;
                        }

                        this.sendFileMessage(player, "data/html/chamberlain/siegetime6.htm");
                }
            } else if (actualCommand.equals("give_crown")) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (cond == 2) {
                    if (player.getInventory().getItemByItemId(6841) == null) {
                        player.addItem("Castle Crown", 6841, 1, player, true);
                        html.setFile("data/html/chamberlain/gavecrown.htm");
                        html.replace("%CharName%", player.getName());
                        html.replace("%FeudName%", this.getCastle().getName());
                    } else {
                        html.setFile("data/html/chamberlain/hascrown.htm");
                    }
                } else {
                    html.setFile("data/html/chamberlain/noprivs.htm");
                }

                player.sendPacket(html);
            } else if (actualCommand.equals("manor_certificate")) {
                if (!this.validatePrivileges(player, 262144)) {
                    return;
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.DUSK) {
                        html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
                    } else if (this.getCastle().getLeftCertificates() == 0) {
                        html.setFile("data/html/chamberlain/not-enough-ticket.htm");
                    } else {
                        html.setFile("data/html/chamberlain/sell-dawn-ticket.htm");
                        html.replace("%left%", this.getCastle().getLeftCertificates());
                        html.replace("%bundle%", 10);
                        html.replace("%price%", 1000);
                    }
                } else {
                    html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equals("validate_certificate")) {
                if (!this.validatePrivileges(player, 262144)) {
                    return;
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.DUSK) {
                        html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
                    } else if (this.getCastle().getLeftCertificates() == 0) {
                        html.setFile("data/html/chamberlain/not-enough-ticket.htm");
                    } else if (player.reduceAdena("Certificate", 10000, this, true)) {
                        player.addItem("Certificate", 6388, 10, this, true);
                        this.getCastle().setLeftCertificates(this.getCastle().getLeftCertificates() - 10, true);
                        html.setFile("data/html/chamberlain/sell-dawn-ticket.htm");
                        html.replace("%left%", this.getCastle().getLeftCertificates());
                        html.replace("%bundle%", 10);
                        html.replace("%price%", 1000);
                    } else {
                        html.setFile("data/html/chamberlain/not-enough-adena.htm");
                    }
                } else {
                    html.setFile("data/html/chamberlain/not-dawn-or-event.htm");
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("castle_devices")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                this.sendFileMessage(player, "data/html/chamberlain/devices.htm");
            } else if (actualCommand.equalsIgnoreCase("doors_update")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (val.isEmpty()) {
                    html.setFile("data/html/chamberlain/" + this.getNpcId() + "-gu.htm");
                } else {
                    html.setFile("data/html/chamberlain/doors-update.htm");
                    html.replace("%id%", val);
                    html.replace("%type%", st.nextToken());
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("doors_choose_upgrade")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                String type = st.nextToken();
                String level = st.nextToken();
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile("data/html/chamberlain/doors-confirm.htm");
                html.replace("%objectId%", this.getObjectId());
                html.replace("%id%", val);
                html.replace("%level%", level);
                html.replace("%type%", type);
                html.replace("%price%", getDoorCost(Integer.parseInt(type), Integer.parseInt(level)));
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("doors_confirm_upgrade")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                int type = Integer.parseInt(st.nextToken());
                int level = Integer.parseInt(st.nextToken());
                int price = getDoorCost(type, level);
                if (price == 0) {
                    return;
                }

                int id = Integer.parseInt(val);
                Door door = this.getCastle().getDoor(id);
                if (door == null) {
                    return;
                }

                int currentHpRatio = door.getStat().getUpgradeHpRatio();
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (currentHpRatio >= level) {
                    html.setFile("data/html/chamberlain/doors-already-updated.htm");
                    html.replace("%level%", currentHpRatio * 100);
                } else if (!player.reduceAdena("doors_upgrade", price, player, true)) {
                    html.setFile("data/html/chamberlain/not-enough-adena.htm");
                } else {
                    this.getCastle().upgradeDoor(id, level, true);
                    html.setFile("data/html/chamberlain/doors-success.htm");
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("traps_update")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (val.isEmpty()) {
                    html.setFile("data/html/chamberlain/" + this.getNpcId() + "-tu.htm");
                } else {
                    String var10001 = this.getCastle().getName();
                    html.setFile("data/html/chamberlain/traps-update" + (var10001.equalsIgnoreCase("aden") ? "1" : "") + ".htm");
                    html.replace("%trapIndex%", val);
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("traps_choose_upgrade")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                String level = st.nextToken();
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile("data/html/chamberlain/traps-confirm.htm");
                html.replace("%objectId%", this.getObjectId());
                html.replace("%trapIndex%", val);
                html.replace("%level%", level);
                html.replace("%price%", getTrapCost(Integer.parseInt(level)));
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("traps_confirm_upgrade")) {
                if (!this.validatePrivileges(player, 4194304)) {
                    return;
                }

                int level = Integer.parseInt(st.nextToken());
                int price = getTrapCost(level);
                if (price == 0) {
                    return;
                }

                int trapIndex = Integer.parseInt(val);
                int currentLevel = this.getCastle().getTrapUpgradeLevel(trapIndex);
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (currentLevel >= level) {
                    html.setFile("data/html/chamberlain/traps-already-updated.htm");
                    html.replace("%level%", currentLevel);
                } else if (!player.reduceAdena("traps_upgrade", price, player, true)) {
                    html.setFile("data/html/chamberlain/not-enough-adena.htm");
                } else {
                    this.getCastle().setTrapUpgrade(trapIndex, level, true);
                    html.setFile("data/html/chamberlain/traps-success.htm");
                }

                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            } else {
                super.onBypassFeedback(player, command);
            }

        }
    }

    public void showChatWindow(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/chamberlain/no.htm";
        int condition = this.validateCondition(player);
        if (condition > 0) {
            if (condition == 1) {
                filename = "data/html/chamberlain/busy.htm";
            } else if (condition >= 2) {
                filename = "data/html/chamberlain/chamberlain.htm";
            }
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    protected int validateCondition(Player player) {
        if (this.getCastle() != null && player.getClan() != null) {
            if (this.getCastle().getSiege().isInProgress()) {
                return 1;
            }

            if (this.getCastle().getOwnerId() == player.getClanId()) {
                if (player.isClanLeader()) {
                    return 2;
                }

                return 3;
            }
        }

        return 0;
    }

    private boolean validatePrivileges(Player player, int privilege) {
        if ((player.getClanPrivileges() & privilege) != privilege) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/chamberlain/noprivs.htm");
            player.sendPacket(html);
            return false;
        } else {
            return true;
        }
    }

    private void sendFileMessage(Player player, String htmlMessage) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(htmlMessage);
        html.replace("%objectId%", this.getObjectId());
        html.replace("%npcId%", this.getNpcId());
        html.replace("%time%", this.getCastle().getSiegeDate().getTime().toString());
        player.sendPacket(html);
    }
}
