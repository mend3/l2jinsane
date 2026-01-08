package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.npc.MinionList;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.List;
import java.util.Map;

public class PartyFarm extends Monster {
    private PartyFarm _master;
    private MinionList _minionList;

    public PartyFarm(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean isAutoAttackable(Creature attacker) {
        return !(attacker instanceof PartyFarm);
    }

    public void onSpawn() {
        super.onSpawn();
    }

    public void onTeleported() {
        super.onTeleported();
        if (this._minionList != null) {
            this.getMinionList().onMasterTeleported();
        }

    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (this._master != null) {
                this._master.getMinionList().onMinionDie(this, this._master.getSpawn().getRespawnDelay() * 1000 / 2);
            }

            return true;
        }
    }

    public void deleteMe() {
        super.deleteMe();
    }

    public boolean hasMinions() {
        return this._minionList != null;
    }

    public MinionList getMinionList() {
        if (this._minionList == null) {
            this._minionList = new MinionList(this);
        }

        return this._minionList;
    }

    public void doItemDrop(NpcTemplate npcTemplate, Creature mainDamageDealer) {
        if (mainDamageDealer != null) {
            Player player = mainDamageDealer.getActingPlayer();
            if (player != null) {
                if (Config.ENABLE_DROP_PARTYFARM) {
                    if (player.isInParty()) {
                        for (Map.Entry<Integer, List<Integer>> entry : Config.PARTY_DROP_LIST.entrySet()) {
                            int rewardItem = Rnd.get((entry.getValue()).get(1), (entry.getValue()).get(2));
                            int dropChance = (entry.getValue()).get(0);
                            if (Rnd.get(100) < dropChance) {
                                IntIntHolder item = new IntIntHolder(entry.getKey(), rewardItem);
                                if (Config.AUTO_LOOT) {
                                    for (Player p : player.getParty().getMembers()) {
                                        if (p.isInsideRadius(player, 9000, false, false)) {
                                            p.addItem("dropCustom", item.getId(), item.getValue(), this, true);
                                        }
                                    }
                                } else {
                                    for (Player p : player.getParty().getMembers()) {
                                        if (p.isInsideRadius(player, 9000, false, false)) {
                                            this.dropItem(p, item);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        player.sendMessage("You are not in a party! or Party Farm is disabled.");
                    }
                }

                if (!net.sf.l2j.gameserver.events.partyfarm.PartyFarm.is_started()) {
                    player.sendMessage("Party Farm event is not active. You donn't get any drop !!");
                }

            }
        }
    }
}
