package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.data.manager.PetitionManager;
import net.sf.l2j.gameserver.enums.PetitionState;
import net.sf.l2j.gameserver.enums.PetitionType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class Petition {
    private final List<CreatureSay> _messageLog = new ArrayList<>();

    private final int _id;

    private final PetitionType _type;

    private final Player _petitioner;

    private final long _submitTime = System.currentTimeMillis();

    private final String _content;

    private PetitionState _state = PetitionState.PENDING;

    private Player _responder;

    public Petition(Player petitioner, String content, int type) {
        type--;
        this._id = IdFactory.getInstance().getNextId();
        this._type = PetitionType.values()[type];
        this._content = content;
        this._petitioner = petitioner;
    }

    public boolean addLogMessage(CreatureSay cs) {
        return this._messageLog.add(cs);
    }

    public List<CreatureSay> getLogMessages() {
        return this._messageLog;
    }

    public boolean endPetitionConsultation(PetitionState endState) {
        setState(endState);
        if (this._responder != null && this._responder.isOnline())
            if (endState == PetitionState.RESPONDER_REJECT) {
                this._petitioner.sendMessage("Your petition was rejected. Please try again later.");
            } else {
                this._responder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_ENDED_WITH_S1).addCharName(this._petitioner));
                if (endState == PetitionState.PETITIONER_CANCEL)
                    this._responder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED).addNumber(getId()));
            }
        if (this._petitioner != null && this._petitioner.isOnline())
            this._petitioner.sendPacket(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK);
        PetitionManager.getInstance().getCompletedPetitions().put(Integer.valueOf(getId()), this);
        return (PetitionManager.getInstance().getPendingPetitions().remove(Integer.valueOf(getId())) != null);
    }

    public String getContent() {
        return this._content;
    }

    public int getId() {
        return this._id;
    }

    public Player getPetitioner() {
        return this._petitioner;
    }

    public Player getResponder() {
        return this._responder;
    }

    public void setResponder(Player respondingAdmin) {
        if (this._responder != null)
            return;
        this._responder = respondingAdmin;
    }

    public long getSubmitTime() {
        return this._submitTime;
    }

    public PetitionState getState() {
        return this._state;
    }

    public void setState(PetitionState state) {
        this._state = state;
    }

    public String getTypeAsString() {
        return this._type.toString().replace("_", " ");
    }

    public void sendPetitionerPacket(L2GameServerPacket responsePacket) {
        if (this._petitioner == null || !this._petitioner.isOnline())
            return;
        this._petitioner.sendPacket(responsePacket);
    }

    public void sendResponderPacket(L2GameServerPacket responsePacket) {
        if (this._responder == null || !this._responder.isOnline()) {
            endPetitionConsultation(PetitionState.RESPONDER_MISSING);
            return;
        }
        this._responder.sendPacket(responsePacket);
    }
}
