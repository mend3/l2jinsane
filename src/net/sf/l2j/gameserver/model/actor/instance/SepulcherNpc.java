package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.FourSepulchersManager;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.Calendar;
import java.util.List;

public class SepulcherNpc extends Folk {
    private static final String HTML_FILE_PATH = "data/html/sepulchers/";
    private static final int HALLS_KEY = 7260;

    public SepulcherNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (this.isAutoAttackable(player)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else if (!this.isAutoAttackable(player)) {
            if (!this.canInteract(player)) {
                player.getAI().setIntention(IntentionType.INTERACT, this);
            } else {
                if (player.isMoving() || player.isInCombat()) {
                    player.getAI().setIntention(IntentionType.IDLE);
                }

                player.sendPacket(new MoveToPawn(player, this, 150));
                player.sendPacket(ActionFailed.STATIC_PACKET);
                if (this.hasRandomAnimation()) {
                    this.onRandomAnimation(Rnd.get(8));
                }

                this.doAction(player);
            }
        }

    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            this.sendNpcInfos(player);
        }

        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (this.isAutoAttackable(player)) {
            if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.getAI().setIntention(IntentionType.ATTACK, this);
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
        } else if (this.canInteract(player)) {
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (this.hasRandomAnimation()) {
                this.onRandomAnimation(Rnd.get(8));
            }

            this.doAction(player);
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    private void doAction(Player player) {
        if (this.isDead()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            switch (this.getNpcId()) {
                case 31455:
                case 31456:
                case 31457:
                case 31458:
                case 31459:
                case 31460:
                case 31461:
                case 31462:
                case 31463:
                case 31464:
                case 31465:
                case 31466:
                case 31467:
                    if (player.isInParty() && !player.getParty().isLeader(player)) {
                        player = player.getParty().getLeader();
                    }

                    player.addItem("Quest", 7260, 1, player, true);
                    this.deleteMe();
                    break;
                case 31468:
                case 31469:
                case 31470:
                case 31471:
                case 31472:
                case 31473:
                case 31474:
                case 31475:
                case 31476:
                case 31477:
                case 31478:
                case 31479:
                case 31480:
                case 31481:
                case 31482:
                case 31483:
                case 31484:
                case 31485:
                case 31486:
                case 31487:
                    if (Calendar.getInstance().get(Calendar.MINUTE) >= 50) {
                        this.broadcastNpcSay("You can start at the scheduled time.");
                        return;
                    }

                    FourSepulchersManager.getInstance().spawnMonster(this.getNpcId());
                    this.deleteMe();
                    break;
                default:
                    List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
                    if (scripts != null && !scripts.isEmpty()) {
                        player.setLastQuestNpcObject(this.getObjectId());
                    }

                    scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
                    if (scripts != null && scripts.size() == 1) {
                        scripts.getFirst().notifyFirstTalk(this, player);
                    } else {
                        this.showChatWindow(player);
                    }
            }

            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }

        return "data/html/sepulchers/" + filename + ".htm";
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("open_gate")) {
            ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
            if (hallsKey == null) {
                this.showHtmlFile(player, "Gatekeeper-no.htm");
            } else if (FourSepulchersManager.getInstance().isAttackTime()) {
                switch (this.getNpcId()) {
                    case 31929:
                    case 31934:
                    case 31939:
                    case 31944:
                        FourSepulchersManager.getInstance().spawnShadow(this.getNpcId());
                }

                this.openNextDoor(this.getNpcId());
                Party party = player.getParty();
                if (party != null) {
                    for (Player member : player.getParty().getMembers()) {
                        ItemInstance key = member.getInventory().getItemByItemId(7260);
                        if (key != null) {
                            member.destroyItemByItemId("Quest", 7260, key.getCount(), member, true);
                        }
                    }
                } else {
                    player.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), player, true);
                }
            }
        } else {
            super.onBypassFeedback(player, command);
        }

    }

    public void openNextDoor(int npcId) {
        int doorId = FourSepulchersManager.getInstance().getHallGateKeepers().get(npcId);
        Door door = DoorData.getInstance().getDoor(doorId);
        door.openMe();
        ThreadPool.schedule(door::closeMe, 10000L);
        FourSepulchersManager.getInstance().spawnMysteriousBox(npcId);
        this.sayInShout("The monsters have spawned!");
    }

    public void sayInShout(String msg) {
        if (msg != null && !msg.isEmpty()) {
            CreatureSay sm = new CreatureSay(this.getObjectId(), 1, this.getName(), msg);

            for (Player player : this.getKnownType(Player.class)) {
                player.sendPacket(sm);
            }

        }
    }

    public void showHtmlFile(Player player, String file) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(HTML_FILE_PATH + file);
        html.replace("%npcname%", this.getName());
        player.sendPacket(html);
    }
}
