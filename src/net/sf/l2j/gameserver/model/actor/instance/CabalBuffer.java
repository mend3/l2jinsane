/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class CabalBuffer extends Folk {
    protected static final String[] MESSAGES_LOSER = new String[]{"%player_cabal_loser%! All is lost! Prepare to meet the goddess of death!", "%player_cabal_loser%! You bring an ill wind!", "%player_cabal_loser%! You might as well give up!", "A curse upon you!", "All is lost! Prepare to meet the goddess of death!", "All is lost! The prophecy of destruction has been fulfilled!", "The prophecy of doom has awoken!", "This world will soon be annihilated!"};
    protected static final String[] MESSAGES_WINNER = new String[]{"%player_cabal_winner%! I bestow on you the authority of the abyss!", "%player_cabal_winner%, Darkness shall be banished forever!", "%player_cabal_winner%, the time for glory is at hand!", "All hail the eternal twilight!", "As foretold in the prophecy of darkness, the era of chaos has begun!", "The day of judgment is near!", "The prophecy of darkness has been fulfilled!", "The prophecy of darkness has come to pass!"};
    protected int _step = 0;
    private ScheduledFuture<?> _aiTask = ThreadPool.scheduleAtFixedRate(new CabalBuffer.CabaleAI(this), 5000L, 5000L);

    public CabalBuffer(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (!this.canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat()) {
                player.getAI().setIntention(IntentionType.IDLE);
            }

            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void deleteMe() {
        if (this._aiTask != null) {
            this._aiTask.cancel(true);
            this._aiTask = null;
        }

        super.deleteMe();
    }

    private class CabaleAI implements Runnable {
        private final CabalBuffer _caster;

        protected CabaleAI(CabalBuffer caster) {
            this._caster = caster;
        }

        public void run() {
            boolean isBuffAWinner = false;
            boolean isBuffALoser = false;
            CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
            CabalType losingCabal = CabalType.NORMAL;
            if (winningCabal == CabalType.DAWN) {
                losingCabal = CabalType.DUSK;
            } else if (winningCabal == CabalType.DUSK) {
                losingCabal = CabalType.DAWN;
            }

            List<Player> playersList = new ArrayList();
            List<Player> gmsList = new ArrayList();
            Iterator var7 = CabalBuffer.this.getKnownTypeInRadius(Player.class, 900).iterator();

            while (var7.hasNext()) {
                Player player = (Player) var7.next();
                if (player.isGM()) {
                    gmsList.add(player);
                } else {
                    playersList.add(player);
                }

                CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
                if (playerCabal != CabalType.NORMAL) {
                    if (!isBuffAWinner && playerCabal == winningCabal && this._caster.getNpcId() == 31094) {
                        isBuffAWinner = true;
                        this.handleCast(player, !player.isMageClass() ? 4364 : 4365);
                    } else if (!isBuffALoser && playerCabal == losingCabal && this._caster.getNpcId() == 31093) {
                        isBuffALoser = true;
                        this.handleCast(player, !player.isMageClass() ? 4361 : 4362);
                    }

                    if (isBuffAWinner && isBuffALoser) {
                        break;
                    }
                }
            }

            if (CabalBuffer.this._step >= 12) {
                if (!playersList.isEmpty() || !gmsList.isEmpty()) {
                    String text;
                    if (this._caster.getCollisionHeight() > 30.0D) {
                        text = Rnd.get(CabalBuffer.MESSAGES_LOSER);
                    } else {
                        text = Rnd.get(CabalBuffer.MESSAGES_WINNER);
                    }

                    Iterator var12;
                    Player nearbyPlayer;
                    if (text.indexOf("%player_cabal_winner%") > -1) {
                        var12 = playersList.iterator();

                        while (var12.hasNext()) {
                            nearbyPlayer = (Player) var12.next();
                            if (SevenSignsManager.getInstance().getPlayerCabal(nearbyPlayer.getObjectId()) == winningCabal) {
                                text = text.replaceAll("%player_cabal_winner%", nearbyPlayer.getName());
                                break;
                            }
                        }
                    } else if (text.indexOf("%player_cabal_loser%") > -1) {
                        var12 = playersList.iterator();

                        while (var12.hasNext()) {
                            nearbyPlayer = (Player) var12.next();
                            if (SevenSignsManager.getInstance().getPlayerCabal(nearbyPlayer.getObjectId()) == losingCabal) {
                                text = text.replaceAll("%player_cabal_loser%", nearbyPlayer.getName());
                                break;
                            }
                        }
                    }

                    if (!text.contains("%player_")) {
                        CreatureSay cs = new CreatureSay(CabalBuffer.this.getObjectId(), 0, CabalBuffer.this.getName(), text);
                        Iterator var15 = playersList.iterator();

                        Player nearbyGM;
                        while (var15.hasNext()) {
                            nearbyGM = (Player) var15.next();
                            nearbyGM.sendPacket(cs);
                        }

                        var15 = gmsList.iterator();

                        while (var15.hasNext()) {
                            nearbyGM = (Player) var15.next();
                            nearbyGM.sendPacket(cs);
                        }
                    }
                }

                CabalBuffer.this._step = 0;
            } else {
                ++CabalBuffer.this._step;
            }

        }

        private void handleCast(Player player, int skillId) {
            int skillLevel = player.getLevel() > 40 ? 1 : 2;
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
            if (player.getFirstEffect(skill) == null) {
                skill.getEffects(this._caster, player);
                CabalBuffer.this.broadcastPacket(new MagicSkillUse(this._caster, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skillId));
            }

        }
    }
}