/**/
package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class EchoCrystals extends Quest {
    private static final String qn = "EchoCrystals";
    private static final int ADENA = 57;
    private static final int COST = 200;
    private static final Map<Integer, EchoCrystals.ScoreData> SCORES = new HashMap();

    public EchoCrystals() {
        super(-1, "custom");
        SCORES.put(4410, new ScoreData(this, 4411, "01", "02", "03"));
        SCORES.put(4409, new ScoreData(this, 4412, "04", "05", "06"));
        SCORES.put(4408, new ScoreData(this, 4413, "07", "08", "09"));
        SCORES.put(4420, new ScoreData(this, 4414, "10", "11", "12"));
        SCORES.put(4421, new ScoreData(this, 4415, "13", "14", "15"));
        SCORES.put(4419, new ScoreData(this, 4417, "16", "05", "06"));
        SCORES.put(4418, new ScoreData(this, 4416, "17", "05", "06"));
        this.addStartNpc(31042, 31043);
        this.addTalkId(31042, 31043);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = "";
        QuestState st = player.getQuestState("EchoCrystals");
        if (st != null && StringUtil.isDigit(event)) {
            int score = Integer.parseInt(event);
            if (SCORES.containsKey(score)) {
                int crystal = SCORES.get(score).getCrystalId();
                String ok = SCORES.get(score).getOkMsg();
                String noadena = SCORES.get(score).getNoAdenaMsg();
                String noscore = SCORES.get(score).getNoScoreMsg();
                int var10000;
                if (st.getQuestItemsCount(score) == 0) {
                    var10000 = npc.getNpcId();
                    htmltext = var10000 + "-" + noscore + ".htm";
                } else if (st.getQuestItemsCount(57) < 200) {
                    var10000 = npc.getNpcId();
                    htmltext = var10000 + "-" + noadena + ".htm";
                } else {
                    st.takeItems(57, 200);
                    st.giveItems(crystal, 1);
                    var10000 = npc.getNpcId();
                    htmltext = var10000 + "-" + ok + ".htm";
                }
            }
        }

        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        return "1.htm";
    }

    private static class ScoreData {
        private final int _crystalId;
        private final String _okMsg;
        private final String _noAdenaMsg;
        private final String _noScoreMsg;

        public ScoreData(final EchoCrystals param1, int crystalId, String okMsg, String noAdenaMsg, String noScoreMsg) {
            this._crystalId = crystalId;
            this._okMsg = okMsg;
            this._noAdenaMsg = noAdenaMsg;
            this._noScoreMsg = noScoreMsg;
        }

        public int getCrystalId() {
            return this._crystalId;
        }

        public String getOkMsg() {
            return this._okMsg;
        }

        public String getNoAdenaMsg() {
            return this._noAdenaMsg;
        }

        public String getNoScoreMsg() {
            return this._noScoreMsg;
        }
    }
}