package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.enums.PetitionState;
import net.sf.l2j.gameserver.model.Petition;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PetitionManager {
    private final Map<Integer, Petition> _pendingPetitions = new ConcurrentHashMap<>();
    private final Map<Integer, Petition> _completedPetitions = new ConcurrentHashMap<>();

    private PetitionManager() {
    }

    public static PetitionManager getInstance() {
        return PetitionManager.SingletonHolder.INSTANCE;
    }

    public Map<Integer, Petition> getCompletedPetitions() {
        return this._completedPetitions;
    }

    public Map<Integer, Petition> getPendingPetitions() {
        return this._pendingPetitions;
    }

    public int getPlayerTotalPetitionCount(Player player) {
        if (player == null) {
            return 0;
        } else {
            int petitionCount = 0;

            for (Petition petition : this._pendingPetitions.values()) {
                if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId()) {
                    ++petitionCount;
                }
            }

            for (Petition petition : this._completedPetitions.values()) {
                if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId()) {
                    ++petitionCount;
                }
            }

            return petitionCount;
        }
    }

    public boolean isPetitionInProcess() {
        for (Petition petition : this._pendingPetitions.values()) {
            if (petition.getState() == PetitionState.IN_PROCESS) {
                return true;
            }
        }

        return false;
    }

    public boolean isPetitionInProcess(int id) {
        Petition petition = this._pendingPetitions.get(id);
        return petition != null && petition.getState() == PetitionState.IN_PROCESS;
    }

    public boolean isPlayerInConsultation(Player player) {
        if (player == null) {
            return false;
        } else {
            for (Petition petition : this._pendingPetitions.values()) {
                if (petition.getState() == PetitionState.IN_PROCESS && (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId() || petition.getResponder() != null && petition.getResponder().getObjectId() == player.getObjectId())) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isPlayerPetitionPending(Player player) {
        if (player == null) {
            return false;
        } else {
            for (Petition petition : this._pendingPetitions.values()) {
                if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId()) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean rejectPetition(Player player, int id) {
        Petition petition = this._pendingPetitions.get(id);
        if (petition != null && petition.getResponder() == null) {
            petition.setResponder(player);
            return petition.endPetitionConsultation(PetitionState.RESPONDER_REJECT);
        } else {
            return false;
        }
    }

    public void sendActivePetitionMessage(Player player, String messageText) {
        for (Petition petition : this._pendingPetitions.values()) {
            if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId()) {
                CreatureSay cs = new CreatureSay(player.getObjectId(), 6, player.getName(), messageText);
                petition.addLogMessage(cs);
                petition.sendResponderPacket(cs);
                petition.sendPetitionerPacket(cs);
                return;
            }

            if (petition.getResponder() != null && petition.getResponder().getObjectId() == player.getObjectId()) {
                CreatureSay cs = new CreatureSay(player.getObjectId(), 7, player.getName(), messageText);
                petition.addLogMessage(cs);
                petition.sendResponderPacket(cs);
                petition.sendPetitionerPacket(cs);
                return;
            }
        }

    }

    public void sendPendingPetitionList(Player player) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        StringBuilder sb = new StringBuilder("<html><body><center><font color=\"LEVEL\">Current Petitions</font><br><table width=\"300\">");
        if (this._pendingPetitions.isEmpty()) {
            sb.append("<tr><td colspan=\"4\">There are no currently pending petitions.</td></tr>");
        } else {
            sb.append("<tr><td></td><td><font color=\"999999\">Petitioner</font></td><td><font color=\"999999\">Petition Type</font></td><td><font color=\"999999\">Submitted</font></td></tr>");
        }

        for (Petition petition : this._pendingPetitions.values()) {
            sb.append("<tr><td>");
            if (petition.getState() != PetitionState.IN_PROCESS) {
                StringUtil.append(sb, "<button value=\"View\" action=\"bypass -h admin_view_petition ", petition.getId(), "\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            } else {
                sb.append("<font color=\"999999\">In Process</font>");
            }

            StringUtil.append(sb, "</td><td>", petition.getPetitioner().getName(), "</td><td>", petition.getTypeAsString(), "</td><td>", sdf.format(petition.getSubmitTime()), "</td></tr>");
        }

        sb.append("</table><br><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><button value=\"Back\" action=\"bypass -h admin_admin\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }

    public int submitPetition(Player player, String content, int type) {
        Petition petition = new Petition(player, content, type);
        this._pendingPetitions.put(petition.getId(), petition);
        AdminData.getInstance().broadcastToGMs(new CreatureSay(player.getObjectId(), 17, "Petition System", player.getName() + " has submitted a new petition."));
        return petition.getId();
    }

    public void viewPetition(Player player, int id) {
        if (player.isGM()) {
            Petition petition = this._pendingPetitions.get(id);
            if (petition != null) {
                StringBuilder sb = new StringBuilder("<html><body>");
                sb.append("<center><br><font color=\"LEVEL\">Petition #").append(petition.getId()).append("</font><br1>");
                sb.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"></center><br>");
                sb.append("Submit Time: ").append((new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(petition.getSubmitTime())).append("<br1>");
                sb.append("Petitioner: ").append(petition.getPetitioner().getName()).append("<br1>");
                String var10001 = petition.getTypeAsString();
                sb.append("Petition Type: ").append(var10001).append("<br>").append(petition.getContent()).append("<br>");
                sb.append("<center><button value=\"Accept\" action=\"bypass -h admin_accept_petition ").append(petition.getId()).append("\"width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>");
                sb.append("<button value=\"Reject\" action=\"bypass -h admin_reject_petition ").append(petition.getId()).append("\" width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
                sb.append("<button value=\"Back\" action=\"bypass -h admin_view_petitions\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
                sb.append("</body></html>");
                NpcHtmlMessage html = new NpcHtmlMessage(0);
                html.setHtml(sb.toString());
                player.sendPacket(html);
            }
        }
    }

    public boolean acceptPetition(Player player, int id) {
        Petition petition = this._pendingPetitions.get(id);
        if (petition != null && petition.getResponder() == null) {
            petition.setResponder(player);
            petition.setState(PetitionState.IN_PROCESS);
            petition.sendPetitionerPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));
            petition.sendResponderPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(petition.getId()));
            petition.sendResponderPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_WITH_S1_UNDER_WAY).addCharName(petition.getPetitioner()));
            return true;
        } else {
            return false;
        }
    }

    public boolean cancelActivePetition(Player player) {
        for (Petition currPetition : this._pendingPetitions.values()) {
            if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
                return currPetition.endPetitionConsultation(PetitionState.PETITIONER_CANCEL);
            }

            if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
                return currPetition.endPetitionConsultation(PetitionState.RESPONDER_CANCEL);
            }
        }

        return false;
    }

    public void checkPetitionMessages(Player player) {
        if (player != null) {
            for (Petition currPetition : this._pendingPetitions.values()) {
                if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) {
                    for (CreatureSay logMessage : currPetition.getLogMessages()) {
                        player.sendPacket(logMessage);
                    }

                    return;
                }
            }

        }
    }

    public void endActivePetition(Player player) {
        if (!player.isGM()) {
            return;
        }
        for (Petition currPetition : this._pendingPetitions.values()) {
            if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()) {
                currPetition.endPetitionConsultation(PetitionState.COMPLETED);
                return;
            }
        }

    }

    private static class SingletonHolder {
        protected static final PetitionManager INSTANCE = new PetitionManager();
    }
}
