package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.List;

public class QuestList extends L2GameServerPacket {
    private final List<Quest> _quests;

    private final Player _activeChar;

    public QuestList(Player player) {
        this._activeChar = player;
        this._quests = player.getAllQuests(true);
    }

    protected final void writeImpl() {
        writeC(128);
        writeH(this._quests.size());
        for (Quest q : this._quests) {
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
