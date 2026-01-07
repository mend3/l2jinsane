package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.List;

public class GMViewQuestList extends L2GameServerPacket {
    private final Player _activeChar;

    public GMViewQuestList(Player cha) {
        this._activeChar = cha;
    }

    protected final void writeImpl() {
        writeC(147);
        writeS(this._activeChar.getName());
        List<Quest> quests = this._activeChar.getAllQuests(true);
        writeH(quests.size());
        for (Quest q : quests) {
            writeD(q.getQuestId());
            QuestState qs = this._activeChar.getQuestState(q.getName());
            if (qs == null) {
                writeD(0);
                continue;
            }
            int states = qs.getInt("__compltdStateFlags");
            if (states != 0) {
                writeD(states);
                continue;
            }
            writeD(qs.getInt("cond"));
        }
    }
}
