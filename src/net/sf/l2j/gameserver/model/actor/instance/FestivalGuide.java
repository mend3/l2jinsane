package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.FestivalType;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.type.PeaceZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.Calendar;
import java.util.List;

public final class FestivalGuide extends Folk {
    private FestivalType _festivalType;
    private CabalType _festivalOracle;
    private int _blueStonesNeeded;
    private int _greenStonesNeeded;
    private int _redStonesNeeded;

    public FestivalGuide(int objectId, NpcTemplate template) {
        super(objectId, template);
        switch (this.getNpcId()) {
            case 31127:
            case 31132:
                this._festivalType = FestivalType.MAX_31;
                this._festivalOracle = CabalType.DAWN;
                this._blueStonesNeeded = 900;
                this._greenStonesNeeded = 540;
                this._redStonesNeeded = 270;
                break;
            case 31128:
            case 31133:
                this._festivalType = FestivalType.MAX_42;
                this._festivalOracle = CabalType.DAWN;
                this._blueStonesNeeded = 1500;
                this._greenStonesNeeded = 900;
                this._redStonesNeeded = 450;
                break;
            case 31129:
            case 31134:
                this._festivalType = FestivalType.MAX_53;
                this._festivalOracle = CabalType.DAWN;
                this._blueStonesNeeded = 3000;
                this._greenStonesNeeded = 1800;
                this._redStonesNeeded = 900;
                break;
            case 31130:
            case 31135:
                this._festivalType = FestivalType.MAX_64;
                this._festivalOracle = CabalType.DAWN;
                this._blueStonesNeeded = 4500;
                this._greenStonesNeeded = 2700;
                this._redStonesNeeded = 1350;
                break;
            case 31131:
            case 31136:
                this._festivalType = FestivalType.MAX_NONE;
                this._festivalOracle = CabalType.DAWN;
                this._blueStonesNeeded = 6000;
                this._greenStonesNeeded = 3600;
                this._redStonesNeeded = 1800;
                break;
            case 31137:
            case 31142:
                this._festivalType = FestivalType.MAX_31;
                this._festivalOracle = CabalType.DUSK;
                this._blueStonesNeeded = 900;
                this._greenStonesNeeded = 540;
                this._redStonesNeeded = 270;
                break;
            case 31138:
            case 31143:
                this._festivalType = FestivalType.MAX_42;
                this._festivalOracle = CabalType.DUSK;
                this._blueStonesNeeded = 1500;
                this._greenStonesNeeded = 900;
                this._redStonesNeeded = 450;
                break;
            case 31139:
            case 31144:
                this._festivalType = FestivalType.MAX_53;
                this._festivalOracle = CabalType.DUSK;
                this._blueStonesNeeded = 3000;
                this._greenStonesNeeded = 1800;
                this._redStonesNeeded = 900;
                break;
            case 31140:
            case 31145:
                this._festivalType = FestivalType.MAX_64;
                this._festivalOracle = CabalType.DUSK;
                this._blueStonesNeeded = 4500;
                this._greenStonesNeeded = 2700;
                this._redStonesNeeded = 1350;
                break;
            case 31141:
            case 31146:
                this._festivalType = FestivalType.MAX_NONE;
                this._festivalOracle = CabalType.DUSK;
                this._blueStonesNeeded = 6000;
                this._greenStonesNeeded = 3600;
                this._redStonesNeeded = 1800;
        }

    }

    private static String getStatsTable() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 5; ++i) {
            int dawnScore = FestivalOfDarknessManager.getInstance().getHighestScore(CabalType.DAWN, i);
            int duskScore = FestivalOfDarknessManager.getInstance().getHighestScore(CabalType.DUSK, i);
            String winningCabal = "Children of Dusk";
            if (dawnScore > duskScore) {
                winningCabal = "Children of Dawn";
            } else if (dawnScore == duskScore) {
                winningCabal = "None";
            }

            sb.append("<tr><td width=\"100\" align=\"center\">").append(FestivalType.VALUES[i].getName()).append("</td><td align=\"center\" width=\"35\">").append(duskScore).append("</td><td align=\"center\" width=\"35\">").append(dawnScore).append("</td><td align=\"center\" width=\"130\">").append(winningCabal).append("</td></tr>");
        }

        return sb.toString();
    }

    private static String getBonusTable() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 5; ++i) {
            String var10001 = FestivalType.VALUES[i].getName();
            sb.append("<tr><td align=\"center\" width=\"150\">").append(var10001).append("</td><td align=\"center\" width=\"150\">").append(FestivalOfDarknessManager.getInstance().getAccumulatedBonus(i)).append("</td></tr>");
        }

        return sb.toString();
    }

    private static String calculateDate(String milliFromEpoch) {
        long numMillis = Long.parseLong(milliFromEpoch);
        Calendar calCalc = Calendar.getInstance();
        calCalc.setTimeInMillis(numMillis);
        int var10000 = calCalc.get(Calendar.YEAR);
        return var10000 + "/" + calCalc.get(Calendar.MONTH) + "/" + calCalc.get(Calendar.DATE);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("FestivalDesc")) {
            int val = Integer.parseInt(command.substring(13));
            this.showChatWindow(player, val, null, true);
        } else if (command.startsWith("Festival")) {
            int festivalIndex = this._festivalType.ordinal();
            Party playerParty = player.getParty();
            int val = Integer.parseInt(command.substring(9, 10));
            switch (val) {
                case 0:
                    if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                        player.sendMessage("Bonuses cannot be paid during the competition period.");
                        return;
                    }

                    if (FestivalOfDarknessManager.getInstance().distribAccumulatedBonus(player) > 0) {
                        this.showChatWindow(player, 0, "a", false);
                    } else {
                        this.showChatWindow(player, 0, "b", false);
                    }
                    break;
                case 1:
                    if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                        this.showChatWindow(player, 2, "a", false);
                        return;
                    }

                    if (FestivalOfDarknessManager.getInstance().isFestivalInitialized()) {
                        player.sendMessage("You cannot sign up while a festival is in progress.");
                        return;
                    }

                    if (playerParty == null) {
                        this.showChatWindow(player, 2, "b", false);
                        return;
                    }

                    if (!playerParty.isLeader(player)) {
                        this.showChatWindow(player, 2, "c", false);
                        return;
                    }

                    if (playerParty.getMembersCount() < Config.ALT_FESTIVAL_MIN_PLAYER) {
                        this.showChatWindow(player, 2, "b", false);
                        return;
                    }

                    if (playerParty.getLevel() > this._festivalType.getMaxLevel()) {
                        this.showChatWindow(player, 2, "d", false);
                        return;
                    }

                    if (player.isFestivalParticipant()) {
                        FestivalOfDarknessManager.getInstance().setParticipants(this._festivalOracle, festivalIndex, playerParty);
                        this.showChatWindow(player, 2, "f", false);
                        return;
                    }

                    this.showChatWindow(player, 1, null, false);
                    break;
                case 2:
                    int stoneType = Integer.parseInt(command.substring(11));
                    int stonesNeeded = 0;
                    switch (stoneType) {
                        case 6360 -> stonesNeeded = this._blueStonesNeeded;
                        case 6361 -> stonesNeeded = this._greenStonesNeeded;
                        case 6362 -> stonesNeeded = this._redStonesNeeded;
                    }

                    if (!player.destroyItemByItemId("SevenSigns", stoneType, stonesNeeded, this, true)) {
                        return;
                    }

                    FestivalOfDarknessManager.getInstance().setParticipants(this._festivalOracle, festivalIndex, playerParty);
                    FestivalOfDarknessManager.getInstance().addAccumulatedBonus(festivalIndex, stoneType, stonesNeeded);
                    this.showChatWindow(player, 2, "e", false);
                    break;
                case 3:
                    if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                        this.showChatWindow(player, 3, "a", false);
                        return;
                    }

                    if (FestivalOfDarknessManager.getInstance().isFestivalInProgress()) {
                        player.sendMessage("You cannot register a score while a festival is in progress.");
                        return;
                    }

                    if (playerParty == null) {
                        this.showChatWindow(player, 3, "b", false);
                        return;
                    }

                    List<Integer> prevParticipants = FestivalOfDarknessManager.getInstance().getPreviousParticipants(this._festivalOracle, festivalIndex);
                    if (prevParticipants == null || prevParticipants.isEmpty() || !prevParticipants.contains(player.getObjectId())) {
                        this.showChatWindow(player, 3, "b", false);
                        return;
                    }

                    if (player.getObjectId() != prevParticipants.getFirst()) {
                        this.showChatWindow(player, 3, "b", false);
                        return;
                    }

                    ItemInstance bloodOfferings = player.getInventory().getItemByItemId(5901);
                    if (bloodOfferings == null) {
                        player.sendMessage("You do not have any blood offerings to contribute.");
                        return;
                    }

                    int offeringScore = bloodOfferings.getCount() * 5;
                    if (!player.destroyItem("SevenSigns", bloodOfferings, this, false)) {
                        return;
                    }

                    boolean isHighestScore = FestivalOfDarknessManager.getInstance().setFinalScore(player, this._festivalOracle, this._festivalType, offeringScore);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addNumber(offeringScore));
                    if (isHighestScore) {
                        this.showChatWindow(player, 3, "c", false);
                    } else {
                        this.showChatWindow(player, 3, "d", false);
                    }
                    break;
                case 4:
                    StringBuilder sb = new StringBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
                    StatSet dawnData = FestivalOfDarknessManager.getInstance().getHighestScoreData(CabalType.DAWN, festivalIndex);
                    StatSet duskData = FestivalOfDarknessManager.getInstance().getHighestScoreData(CabalType.DUSK, festivalIndex);
                    StatSet overallData = FestivalOfDarknessManager.getInstance().getOverallHighestScoreData(festivalIndex);
                    int dawnScore = dawnData.getInteger("score");
                    int duskScore = duskData.getInteger("score");
                    sb.append(this._festivalType.getName()).append(" festival.<br>");
                    if (dawnScore > 0) {
                        sb.append("Dawn: ").append(calculateDate(dawnData.getString("date"))).append(". Score ").append(dawnScore).append("<br>").append(dawnData.getString("members")).append("<br>");
                    } else {
                        sb.append("Dawn: No record exists. Score 0<br>");
                    }

                    if (duskScore > 0) {
                        sb.append("Dusk: ").append(calculateDate(duskData.getString("date"))).append(". Score ").append(duskScore).append("<br>").append(duskData.getString("members")).append("<br>");
                    } else {
                        sb.append("Dusk: No record exists. Score 0<br>");
                    }

                    if (overallData != null) {
                        String cabalStr = "Children of Dusk";
                        if (overallData.getString("cabal").equals("dawn")) {
                            cabalStr = "Children of Dawn";
                        }

                        String var10001 = calculateDate(overallData.getString("date"));
                        sb.append("Consecutive top scores: ").append(var10001).append(". Score ").append(overallData.getInteger("score")).append("<br>Affilated side: ").append(cabalStr).append("<br>").append(overallData.getString("members")).append("<br>");
                    } else {
                        sb.append("Consecutive top scores: No record exists. Score 0<br>");
                    }

                    sb.append("<a action=\"bypass -h npc_").append(this.getObjectId()).append("_Chat 0\">Go back.</a></body></html>");
                    NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                    html.setHtml(sb.toString());
                    player.sendPacket(html);
                    break;
                case 5:
                case 6:
                case 7:
                default:
                    this.showChatWindow(player, val, null, false);
                    break;
                case 8:
                    if (playerParty == null) {
                        return;
                    }

                    if (!FestivalOfDarknessManager.getInstance().isFestivalInProgress()) {
                        return;
                    }

                    if (!playerParty.isLeader(player)) {
                        this.showChatWindow(player, 8, "a", false);
                    } else if (FestivalOfDarknessManager.getInstance().increaseChallenge(this._festivalOracle, festivalIndex)) {
                        this.showChatWindow(player, 8, "b", false);
                    } else {
                        this.showChatWindow(player, 8, "c", false);
                    }
                    break;
                case 9:
                    if (playerParty == null) {
                        return;
                    }

                    boolean isLeader = playerParty.isLeader(player);
                    if (isLeader) {
                        FestivalOfDarknessManager.getInstance().updateParticipants(player, null);
                    } else {
                        FestivalOfDarknessManager.getInstance().updateParticipants(player, playerParty);
                        playerParty.removePartyMember(player, MessageType.EXPELLED);
                    }
            }
        } else {
            super.onBypassFeedback(player, command);
        }

    }

    public void showChatWindow(Player player, int val) {
        String filename = "data/html/seven_signs/";
        filename = switch (this.getTemplate().getNpcId()) {
            case 31127, 31128, 31129, 31130, 31131 -> filename + "festival/dawn_guide.htm";
            case 31132, 31133, 31134, 31135, 31136, 31142, 31143, 31144, 31145, 31146 ->
                    filename + "festival/festival_witch.htm";
            case 31137, 31138, 31139, 31140, 31141 -> filename + "festival/dusk_guide.htm";
            default -> "data/html/seven_signs/";
        };

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        html.replace("%festivalMins%", FestivalOfDarknessManager.getInstance().getTimeToNextFestivalStr());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private void showChatWindow(Player player, int val, String suffix, boolean isDescription) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/seven_signs/festival/" + (isDescription ? "desc_" : "festival_") + (suffix != null ? val + suffix : val) + ".htm");
        html.replace("%objectId%", this.getObjectId());
        html.replace("%festivalType%", this._festivalType.getName());
        html.replace("%cycleMins%", FestivalOfDarknessManager.getInstance().getMinsToNextCycle());
        if (!isDescription && "2b".equals(val + suffix)) {
            html.replace("%minFestivalPartyMembers%", Config.ALT_FESTIVAL_MIN_PLAYER);
        }

        if (val == 1) {
            html.replace("%blueStoneNeeded%", this._blueStonesNeeded);
            html.replace("%greenStoneNeeded%", this._greenStonesNeeded);
            html.replace("%redStoneNeeded%", this._redStonesNeeded);
        } else if (val == 5) {
            html.replace("%statsTable%", getStatsTable());
        } else if (val == 6) {
            html.replace("%bonusTable%", getBonusTable());
        }

        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onSpawn() {
        super.onSpawn();
        PeaceZone zone = ZoneManager.getInstance().getZone(this, PeaceZone.class);
        if (zone != null) {
            FestivalOfDarknessManager.getInstance().addPeaceZone(zone, this._festivalOracle == CabalType.DAWN);
        }

    }
}
