/**/
package net.sf.l2j.gameserver.scripting.scripts.ai.individual;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Benom extends L2AttackableAIScript {
    private static final int BENOM = 29054;
    private static final int TELEPORT_CUBE = 29055;
    private static final int DUNGEON_KEEPER = 35506;
    private static final SpawnLocation[] TARGET_TELEPORTS = new SpawnLocation[]{new SpawnLocation(12860, -49158, -976, 650), new SpawnLocation(14878, -51339, 1024, 100), new SpawnLocation(15674, -49970, 864, 100), new SpawnLocation(15696, -48326, 864, 100), new SpawnLocation(14873, -46956, 1024, 100), new SpawnLocation(12157, -49135, -1088, 650), new SpawnLocation(12875, -46392, -288, 200), new SpawnLocation(14087, -46706, -288, 200), new SpawnLocation(14086, -51593, -288, 200), new SpawnLocation(12864, -51898, -288, 200), new SpawnLocation(15538, -49153, -1056, 200), new SpawnLocation(17001, -49149, -1064, 650)};
    private static final SpawnLocation THRONE_LOC = new SpawnLocation(11025, -49152, -537, 0);
    private static final SpawnLocation PRISON_LOC = new SpawnLocation(11882, -49216, -3008, 0);
    private final Siege _siege = this.addSiegeNotify(8);
    private Npc _benom;
    private boolean _isPrisonOpened;
    private final List<Player> _targets = new ArrayList();

    public Benom() {
        super("ai/individual");
        this.addStartNpc(35506, 29055);
        this.addTalkId(35506, 29055);
    }

    private static void teleportTarget(Player player) {
        if (player != null) {
            SpawnLocation loc = Rnd.get(TARGET_TELEPORTS);
            player.teleportTo(loc, loc.getHeading());
        }

    }

    protected void registerNpcs() {
        this.addEventIds(29054, ScriptEventType.ON_AGGRO, ScriptEventType.ON_SPELL_FINISHED, ScriptEventType.ON_ATTACK, ScriptEventType.ON_KILL);
    }

    public String onTalk(Npc npc, Player talker) {
        switch (npc.getNpcId()) {
            case 29055:
                talker.teleportTo(TeleportType.TOWN);
                break;
            case 35506:
                if (!this._isPrisonOpened) {
                    return HtmCache.getInstance().getHtm("data/html/doormen/35506-2.htm");
                }

                talker.teleportTo(12589, -49044, -3008, 0);
        }

        return super.onTalk(npc, talker);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        byte var5 = -1;
        switch (event.hashCode()) {
            case -2124020654:
                if (event.equals("tower_check")) {
                    var5 = 1;
                }
                break;
            case -475184109:
                if (event.equals("raid_check")) {
                    var5 = 2;
                }
                break;
            case 1329383269:
                if (event.equals("benom_spawn")) {
                    var5 = 0;
                }
        }

        switch (var5) {
            case 0:
                this._isPrisonOpened = true;
                this._benom = this.addSpawn(29054, PRISON_LOC, false, 0L, false);
                this._benom.broadcastNpcSay("Who dares to covet the throne of our castle! Leave immediately or you will pay the price of your audacity with your very own blood!");
                break;
            case 1:
                if (this._siege.getControlTowerCount() < 2) {
                    npc.teleportTo(THRONE_LOC, 0);
                    this._siege.getCastle().getSiegeZone().broadcastPacket(new NpcSay(0, 0, 35506, "Oh no! The defenses have failed. It is too dangerous to remain inside the castle. Flee! Every man for himself!"));
                    this.cancelQuestTimer("tower_check", npc, null);
                    this.startQuestTimer("raid_check", 10000L, npc, null, true);
                }
                break;
            case 2:
                if (!npc.isInsideZone(ZoneId.SIEGE) && !npc.isTeleporting()) {
                    npc.teleportTo(THRONE_LOC, 0);
                }
        }

        return event;
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        if (isPet) {
            return super.onAggro(npc, player, isPet);
        } else {
            if (this._targets.size() < 10 && Rnd.get(3) < 1) {
                this._targets.add(player);
            }

            return super.onAggro(npc, player, isPet);
        }
    }

    public void onSiegeEvent() {
        if (this._siege.getCastle().getOwnerId() > 0) {
            switch (this._siege.getStatus()) {
                case IN_PROGRESS:
                    this._isPrisonOpened = false;
                    if (this._benom != null && !this._benom.isDead()) {
                        this.startQuestTimer("tower_check", 30000L, this._benom, null, true);
                    }
                    break;
                case REGISTRATION_OPENED:
                    this._isPrisonOpened = false;
                    if (this._benom != null) {
                        this.cancelQuestTimer("tower_check", this._benom, null);
                        this.cancelQuestTimer("raid_check", this._benom, null);
                        this._benom.deleteMe();
                    }

                    this.startQuestTimer("benom_spawn", this._siege.getSiegeDate().getTimeInMillis() - 8640000L - System.currentTimeMillis(), null, null, false);
                    break;
                case REGISTRATION_OVER:
                    this.startQuestTimer("benom_spawn", 0L, null, null, false);
            }

        }
    }

    public String onSpellFinished(Npc npc, Player player, L2Skill skill) {
        switch (skill.getId()) {
            case 4995:
                teleportTarget(player);
                ((Attackable) npc).stopHating(player);
                break;
            case 4996:
                teleportTarget(player);
                ((Attackable) npc).stopHating(player);
                if (!this._targets.isEmpty()) {
                    Iterator var4 = this._targets.iterator();

                    while (var4.hasNext()) {
                        Player target = (Player) var4.next();
                        long x = player.getX() - target.getX();
                        long y = player.getY() - target.getY();
                        long z = player.getZ() - target.getZ();
                        long range = 250L;
                        if (x * x + y * y + z * z <= 62500L) {
                            teleportTarget(target);
                            ((Attackable) npc).stopHating(target);
                        }
                    }

                    this._targets.clear();
                }
        }

        return null;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (attacker instanceof Playable) {
            if (Rnd.get(100) <= 25) {
                npc.setTarget(attacker);
                npc.doCast(SkillTable.getInstance().getInfo(4995, 1));
            } else if (!npc.isCastingNow()) {
                if (npc.getCurrentHp() < (double) (npc.getMaxHp() / 3) && Rnd.get(500) < 1) {
                    npc.setTarget(attacker);
                    npc.doCast(SkillTable.getInstance().getInfo(4996, 1));
                } else if (!npc.isInsideRadius(attacker, 300, true, false) && Rnd.get(100) < 1) {
                    npc.setTarget(attacker);
                    npc.doCast(SkillTable.getInstance().getInfo(4993, 1));
                } else if (Rnd.get(100) < 1) {
                    npc.setTarget(attacker);
                    npc.doCast(SkillTable.getInstance().getInfo(4994, 1));
                }
            }
        }

        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onKill(Npc npc, Creature killer) {
        npc.broadcastNpcSay("It's not over yet... It won't be... over... like this... Never...");
        this.cancelQuestTimer("raid_check", npc, null);
        this.addSpawn(29055, 12589, -49044, -3008, 0, false, 120000L, false);
        return null;
    }
}