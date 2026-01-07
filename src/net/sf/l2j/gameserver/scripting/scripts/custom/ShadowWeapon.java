package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.village_master.FirstClassChange;
import net.sf.l2j.gameserver.scripting.scripts.village_master.SecondClassChange;

public class ShadowWeapon extends Quest {
    private static final String qn = "ShadowWeapon";

    private static final int D_COUPON = 8869;

    private static final int C_COUPON = 8870;

    public ShadowWeapon() {
        super(-1, "custom");
        addStartNpc(FirstClassChange.FIRSTCLASSNPCS);
        addTalkId(FirstClassChange.FIRSTCLASSNPCS);
        addStartNpc(SecondClassChange.SECONDCLASSNPCS);
        addTalkId(SecondClassChange.SECONDCLASSNPCS);
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("ShadowWeapon");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        boolean hasD = st.hasQuestItems(8869);
        boolean hasC = st.hasQuestItems(8870);
        if (hasD || hasC) {
            String multisell = "306893003";
            if (!hasD) {
                multisell = "306893002";
            } else if (!hasC) {
                multisell = "306893001";
            }
            htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell);
        } else {
            htmltext = "exchange-no.htm";
        }
        st.exitQuest(true);
        return htmltext;
    }
}
