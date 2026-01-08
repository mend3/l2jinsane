package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class GrandBoss extends Monster {
    public GrandBoss(int objectId, NpcTemplate template) {
        super(objectId, template);
        this.setRaid(true);
    }

    public void onSpawn() {
        this.setIsNoRndWalk(true);
        super.onSpawn();
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            Player player = killer.getActingPlayer();
            if (player != null) {
                this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
                this.broadcastPacket(new PlaySound("systemmsg_e.1209"));
                Party party = player.getParty();
                if (party != null) {
                    for (Player member : party.getMembers()) {
                        RaidPointManager.getInstance().addPoints(member, this.getNpcId(), this.getLevel() / 2 + Rnd.get(-5, 5));
                        if (member.isNoble()) {
                            HeroManager.getInstance().setRBkilled(member.getObjectId(), this.getNpcId());
                        }
                    }
                } else {
                    RaidPointManager.getInstance().addPoints(player, this.getNpcId(), this.getLevel() / 2 + Rnd.get(-5, 5));
                    if (player.isNoble()) {
                        HeroManager.getInstance().setRBkilled(player.getObjectId(), this.getNpcId());
                    }
                }
            }

            return true;
        }
    }

    public boolean returnHome() {
        return false;
    }
}
