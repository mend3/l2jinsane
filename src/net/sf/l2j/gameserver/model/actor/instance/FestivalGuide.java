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
        switch (getNpcId()) {
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
                break;
        }
    }

    private static String getStatsTable() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int dawnScore = FestivalOfDarknessManager.getInstance().getHighestScore(CabalType.DAWN, i);
            int duskScore = FestivalOfDarknessManager.getInstance().getHighestScore(CabalType.DUSK, i);
            String winningCabal = "Children of Dusk";
            if (dawnScore > duskScore) {
                winningCabal = "Children of Dawn";
            } else if (dawnScore == duskScore) {
                winningCabal = "None";
            }
            sb.append("<tr><td width=\"100\" align=\"center\">" + FestivalType.VALUES[i].getName() + "</td><td align=\"center\" width=\"35\">" + duskScore + "</td><td align=\"center\" width=\"35\">" + dawnScore + "</td><td align=\"center\" width=\"130\">" + winningCabal + "</td></tr>");
        }
        return sb.toString();
    }

    private static String getBonusTable() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++)
            sb.append("<tr><td align=\"center\" width=\"150\">" + FestivalType.VALUES[i].getName() + "</td><td align=\"center\" width=\"150\">" + FestivalOfDarknessManager.getInstance().getAccumulatedBonus(i) + "</td></tr>");
        return sb.toString();
    }

    private static String calculateDate(String milliFromEpoch) {
        long numMillis = Long.valueOf(milliFromEpoch);
        Calendar calCalc = Calendar.getInstance();
        calCalc.setTimeInMillis(numMillis);
        return calCalc.get(1) + "/" + calCalc.get(1) + "/" + calCalc.get(2);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("FestivalDesc")) {
            int val = Integer.parseInt(command.substring(13));
            showChatWindow(player, val, null, true);
        } else if (command.startsWith("Festival")) {
            int stoneType, stonesNeeded;
            List<Integer> prevParticipants;
            ItemInstance bloodOfferings;
            int offeringScore;
            boolean isHighestScore;
            StringBuilder sb;
            StatSet dawnData, duskData, overallData;
            int dawnScore, duskScore;
            NpcHtmlMessage html;
            boolean isLeader;
            int festivalIndex = this._festivalType.ordinal();
            Party playerParty = player.getParty();
            int val = Integer.parseInt(command.substring(9, 10));
            switch (val) {
                case 1:
                    if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                        showChatWindow(player, 2, "a", false);
                        return;
                    }
                    if (FestivalOfDarknessManager.getInstance().isFestivalInitialized()) {
                        player.sendMessage("You cannot sign up while a festival is in progress.");
                        return;
                    }
                    if (playerParty == null) {
                        showChatWindow(player, 2, "b", false);
                        return;
                    }
                    if (!playerParty.isLeader(player)) {
                        showChatWindow(player, 2, "c", false);
                        return;
                    }
                    if (playerParty.getMembersCount() < Config.ALT_FESTIVAL_MIN_PLAYER) {
                        showChatWindow(player, 2, "b", false);
                        return;
                    }
                    if (playerParty.getLevel() > this._festivalType.getMaxLevel()) {
                        showChatWindow(player, 2, "d", false);
                        return;
                    }
                    if (player.isFestivalParticipant()) {
                        FestivalOfDarknessManager.getInstance().setParticipants(this._festivalOracle, festivalIndex, playerParty);
                        showChatWindow(player, 2, "f", false);
                        return;
                    }
                    showChatWindow(player, 1, null, false);
                    return;
                case 2:
                    stoneType = Integer.parseInt(command.substring(11));
                    stonesNeeded = 0;
                    switch (stoneType) {
                        case 6360:
                            stonesNeeded = this._blueStonesNeeded;
                            break;
                        case 6361:
                            stonesNeeded = this._greenStonesNeeded;
                            break;
                        case 6362:
                            stonesNeeded = this._redStonesNeeded;
                            break;
                    }
                    if (!player.destroyItemByItemId("SevenSigns", stoneType, stonesNeeded, this, true))
                        return;
                    FestivalOfDarknessManager.getInstance().setParticipants(this._festivalOracle, festivalIndex, playerParty);
                    FestivalOfDarknessManager.getInstance().addAccumulatedBonus(festivalIndex, stoneType, stonesNeeded);
                    showChatWindow(player, 2, "e", false);
                    return;
                case 3:
                    if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                        showChatWindow(player, 3, "a", false);
                        return;
                    }
                    if (FestivalOfDarknessManager.getInstance().isFestivalInProgress()) {
                        player.sendMessage("You cannot register a score while a festival is in progress.");
                        return;
                    }
                    if (playerParty == null) {
                        showChatWindow(player, 3, "b", false);
                        return;
                    }
                    prevParticipants = FestivalOfDarknessManager.getInstance().getPreviousParticipants(this._festivalOracle, festivalIndex);
                    if (prevParticipants == null || prevParticipants.isEmpty() || !prevParticipants.contains(Integer.valueOf(player.getObjectId()))) {
                        showChatWindow(player, 3, "b", false);
                        return;
                    }
                    if (player.getObjectId() != prevParticipants.get(0)) {
                        showChatWindow(player, 3, "b", false);
                        return;
                    }
                    bloodOfferings = player.getInventory().getItemByItemId(5901);
                    if (bloodOfferings == null) {
                        player.sendMessage("You do not have any blood offerings to contribute.");
                        return;
                    }
                    offeringScore = bloodOfferings.getCount() * 5;
                    if (!player.destroyItem("SevenSigns", bloodOfferings, this, false))
                        return;
                    isHighestScore = FestivalOfDarknessManager.getInstance().setFinalScore(player, this._festivalOracle, this._festivalType, offeringScore);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addNumber(offeringScore));
                    if (isHighestScore) {
                        showChatWindow(player, 3, "c", false);
                    } else {
                        showChatWindow(player, 3, "d", false);
                    }
                    return;
                case 4:
                    sb = new StringBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
                    dawnData = FestivalOfDarknessManager.getInstance().getHighestScoreData(CabalType.DAWN, festivalIndex);
                    duskData = FestivalOfDarknessManager.getInstance().getHighestScoreData(CabalType.DUSK, festivalIndex);
                    overallData = FestivalOfDarknessManager.getInstance().getOverallHighestScoreData(festivalIndex);
                    dawnScore = dawnData.getInteger("score");
                    duskScore = duskData.getInteger("score");
                    sb.append(this._festivalType.getName() + " festival.<br>");
                    if (dawnScore > 0) {
                        sb.append("Dawn: " + calculateDate(dawnData.getString("date")) + ". Score " + dawnScore + "<br>" + dawnData.getString("members") + "<br>");
                    } else {
                        sb.append("Dawn: No record exists. Score 0<br>");
                    }
                    if (duskScore > 0) {
                        sb.append("Dusk: " + calculateDate(duskData.getString("date")) + ". Score " + duskScore + "<br>" + duskData.getString("members") + "<br>");
                    } else {
                        sb.append("Dusk: No record exists. Score 0<br>");
                    }
                    if (overallData != null) {
                        String cabalStr = "Children of Dusk";
                        if (overallData.getString("cabal").equals("dawn"))
                            cabalStr = "Children of Dawn";
                        sb.append("Consecutive top scores: " + calculateDate(overallData.getString("date")) + ". Score " + overallData.getInteger("score") + "<br>Affilated side: " + cabalStr + "<br>" + overallData.getString("members") + "<br>");
                    } else {
                        sb.append("Consecutive top scores: No record exists. Score 0<br>");
                    }
                    sb.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Go back.</a></body></html>");
                    html = new NpcHtmlMessage(getObjectId());
                    html.setHtml(sb.toString());
                    player.sendPacket(html);
                    return;
                case 8:
                    if (playerParty == null)
                        return;
                    if (!FestivalOfDarknessManager.getInstance().isFestivalInProgress())
                        return;
                    if (!playerParty.isLeader(player)) {
                        showChatWindow(player, 8, "a", false);
                    } else if (FestivalOfDarknessManager.getInstance().increaseChallenge(this._festivalOracle, festivalIndex)) {
                        showChatWindow(player, 8, "b", false);
                    } else {
                        showChatWindow(player, 8, "c", false);
                    }
                    return;
                case 9:
                    if (playerParty == null)
                        return;
                    isLeader = playerParty.isLeader(player);
                    if (isLeader) {
                        FestivalOfDarknessManager.getInstance().updateParticipants(player, null);
                    } else {
                        FestivalOfDarknessManager.getInstance().updateParticipants(player, playerParty);
                        playerParty.removePartyMember(player, MessageType.EXPELLED);
                    }
                    return;
                case 0:
                    if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                        player.sendMessage("Bonuses cannot be paid during the competition period.");
                        return;
                    }
                    if (FestivalOfDarknessManager.getInstance().distribAccumulatedBonus(player) > 0) {
                        showChatWindow(player, 0, "a", false);
                    } else {
                        showChatWindow(player, 0, "b", false);
                    }
                    return;
            }
            showChatWindow(player, val, null, false);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player, int val) {
        String filename = "data/html/seven_signs/";
        switch (getTemplate().getNpcId()) {
            case 31127:
            case 31128:
            case 31129:
            case 31130:
            case 31131:
                filename = filename + "festival/dawn_guide.htm";
                break;
            case 31137:
            case 31138:
            case 31139:
            case 31140:
            case 31141:
                filename = filename + "festival/dusk_guide.htm";
                break;
            case 31132:
            case 31133:
            case 31134:
            case 31135:
            case 31136:
            case 31142:
            case 31143:
            case 31144:
            case 31145:
            case 31146:
                filename = filename + "festival/festival_witch.htm";
                break;
        }
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", getObjectId());
        html.replace("%festivalMins%", FestivalOfDarknessManager.getInstance().getTimeToNextFestivalStr());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private void showChatWindow(Player player, int val, String suffix, boolean isDescription) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/seven_signs/festival/" + (isDescription ? "desc_" : "festival_") + ((suffix != null) ? ("" + val + val) : Integer.valueOf(val)) + ".htm");
        html.replace("%objectId%", getObjectId());
        html.replace("%festivalType%", this._festivalType.getName());
        html.replace("%cycleMins%", FestivalOfDarknessManager.getInstance().getMinsToNextCycle());
        if (!isDescription && "2b".equals("" + val + val))
            html.replace("%minFestivalPartyMembers%", Config.ALT_FESTIVAL_MIN_PLAYER);
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
        if (zone != null)
            FestivalOfDarknessManager.getInstance().addPeaceZone(zone, (this._festivalOracle == CabalType.DAWN));
    }
}
