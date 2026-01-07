package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.FishingChampionshipManager;
import net.sf.l2j.gameserver.data.xml.FishData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.Fish;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.PenaltyMonster;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.concurrent.Future;

public class FishingStance {
    private final Player _fisher;

    private final Location _loc = new Location(0, 0, 0);

    private int _time;

    private int _stop;

    private int _goodUse;

    private int _anim;

    private int _mode;

    private int _deceptiveMode;

    private Future<?> _lookingForFish;

    private Future<?> _fishCombat;

    private boolean _thinking;

    private Fish _fish;

    private int _fishCurHp;

    private boolean _isUpperGrade;

    private ItemInstance _lure;

    private int _lureType;

    public FishingStance(Player fisher) {
        this._fisher = fisher;
    }

    private int getRandomFishType(int group) {
        int check = Rnd.get(100);
        int type = 1;
        switch (group) {
            case 0:
                switch (this._lure.getItemId()) {
                    case 7807:
                        if (check <= 54) {
                            type = 5;
                            break;
                        }
                        if (check <= 77) {
                            type = 4;
                            break;
                        }
                        type = 6;
                        break;
                    case 7808:
                        if (check <= 54) {
                            type = 4;
                            break;
                        }
                        if (check <= 77) {
                            type = 6;
                            break;
                        }
                        type = 5;
                        break;
                    case 7809:
                        if (check <= 54) {
                            type = 6;
                            break;
                        }
                        if (check <= 77) {
                            type = 5;
                            break;
                        }
                        type = 4;
                        break;
                    case 8486:
                        if (check <= 33) {
                            type = 4;
                            break;
                        }
                        if (check <= 66) {
                            type = 5;
                            break;
                        }
                        type = 6;
                        break;
                }
                break;
            case 1:
                switch (this._lure.getItemId()) {
                    case 7610:
                    case 7611:
                    case 7612:
                    case 7613:
                        type = 3;
                        break;
                    case 6519:
                    case 6520:
                    case 6521:
                    case 8505:
                    case 8507:
                        if (check <= 54) {
                            type = 1;
                            break;
                        }
                        if (check <= 74) {
                            type = 0;
                            break;
                        }
                        if (check <= 94) {
                            type = 2;
                            break;
                        }
                        type = 3;
                        break;
                    case 6522:
                    case 6523:
                    case 6524:
                    case 8508:
                    case 8510:
                        if (check <= 54) {
                            type = 0;
                            break;
                        }
                        if (check <= 74) {
                            type = 1;
                            break;
                        }
                        if (check <= 94) {
                            type = 2;
                            break;
                        }
                        type = 3;
                        break;
                    case 6525:
                    case 6526:
                    case 6527:
                    case 8511:
                    case 8513:
                        if (check <= 55) {
                            type = 2;
                            break;
                        }
                        if (check <= 74) {
                            type = 1;
                            break;
                        }
                        if (check <= 94) {
                            type = 0;
                            break;
                        }
                        type = 3;
                        break;
                    case 8484:
                        if (check <= 33) {
                            type = 0;
                            break;
                        }
                        if (check <= 66) {
                            type = 1;
                            break;
                        }
                        type = 2;
                        break;
                }
                break;
            case 2:
                switch (this._lure.getItemId()) {
                    case 8506:
                        if (check <= 54) {
                            type = 8;
                            break;
                        }
                        if (check <= 77) {
                            type = 7;
                            break;
                        }
                        type = 9;
                        break;
                    case 8509:
                        if (check <= 54) {
                            type = 7;
                            break;
                        }
                        if (check <= 77) {
                            type = 9;
                            break;
                        }
                        type = 8;
                        break;
                    case 8512:
                        if (check <= 54) {
                            type = 9;
                            break;
                        }
                        if (check <= 77) {
                            type = 8;
                            break;
                        }
                        type = 7;
                        break;
                    case 8485:
                        if (check <= 33) {
                            type = 7;
                            break;
                        }
                        if (check <= 66) {
                            type = 8;
                            break;
                        }
                        type = 9;
                        break;
                }
                break;
        }
        return type;
    }

    private int getRandomFishLvl() {
        L2Effect effect = this._fisher.getFirstEffect(2274);
        int level = (effect != null) ? (int) effect.getSkill().getPower() : this._fisher.getSkillLevel(1315);
        if (level <= 0)
            return 1;
        int check = Rnd.get(100);
        if (check < 35) {
            level--;
        } else if (check < 50) {
            level++;
        }
        return MathUtil.limit(level, 1, 27);
    }

    public Location getLoc() {
        return this._loc;
    }

    public boolean isLookingForFish() {
        return (this._lookingForFish != null);
    }

    public boolean isUnderFishCombat() {
        return (this._fishCombat != null);
    }

    public void changeHp(int hp, int penalty) {
        this._fishCurHp -= hp;
        if (this._fishCurHp < 0)
            this._fishCurHp = 0;
        this._fisher.broadcastPacket(new ExFishingHpRegen(this._fisher, this._time, this._fishCurHp, this._mode, this._goodUse, this._anim, penalty, this._deceptiveMode));
        this._anim = 0;
        if (this._fishCurHp > this._fish.getHp() * 2) {
            this._fishCurHp = this._fish.getHp() * 2;
            end(false);
            return;
        }
        if (this._fishCurHp == 0)
            end(true);
    }

    protected void aiTask() {
        if (this._thinking)
            return;
        this._thinking = true;
        this._time--;
        try {
            if (this._mode == 1) {
                if (this._deceptiveMode == 0)
                    this._fishCurHp += this._fish.getHpRegen();
            } else if (this._deceptiveMode == 1) {
                this._fishCurHp += this._fish.getHpRegen();
            }
            if (this._stop == 0) {
                this._stop = 1;
                int check = Rnd.get(100);
                if (check >= 70)
                    this._mode = (this._mode == 0) ? 1 : 0;
                if (this._isUpperGrade) {
                    check = Rnd.get(100);
                    if (check >= 90)
                        this._deceptiveMode = (this._deceptiveMode == 0) ? 1 : 0;
                }
            } else {
                this._stop--;
            }
        } finally {
            this._thinking = false;
            if (this._anim != 0) {
                this._fisher.broadcastPacket(new ExFishingHpRegen(this._fisher, this._time, this._fishCurHp, this._mode, 0, this._anim, 0, this._deceptiveMode));
            } else {
                this._fisher.sendPacket(new ExFishingHpRegen(this._fisher, this._time, this._fishCurHp, this._mode, 0, this._anim, 0, this._deceptiveMode));
            }
        }
    }

    public void useRealing(int dmg, int penalty) {
        this._anim = 2;
        if (Rnd.get(100) > 90) {
            this._fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
            this._goodUse = 0;
            changeHp(0, penalty);
            return;
        }
        if (this._fisher == null)
            return;
        if (this._mode == 1) {
            if (this._deceptiveMode == 0) {
                this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
                if (penalty == 50)
                    this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
                this._goodUse = 1;
                changeHp(dmg, penalty);
            } else {
                this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
                this._goodUse = 2;
                changeHp(-dmg, penalty);
            }
        } else if (this._deceptiveMode == 0) {
            this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(dmg));
            this._goodUse = 2;
            changeHp(-dmg, penalty);
        } else {
            this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
            if (penalty == 50)
                this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
            this._goodUse = 1;
            changeHp(dmg, penalty);
        }
    }

    public void usePomping(int dmg, int penalty) {
        this._anim = 1;
        if (Rnd.get(100) > 90) {
            this._fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
            this._goodUse = 0;
            changeHp(0, penalty);
            return;
        }
        if (this._fisher == null)
            return;
        if (this._mode == 0) {
            if (this._deceptiveMode == 0) {
                this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
                if (penalty == 50)
                    this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
                this._goodUse = 1;
                changeHp(dmg, penalty);
            } else {
                this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
                this._goodUse = 2;
                changeHp(-dmg, penalty);
            }
        } else if (this._deceptiveMode == 0) {
            this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(dmg));
            this._goodUse = 2;
            changeHp(-dmg, penalty);
        } else {
            this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(dmg));
            if (penalty == 50)
                this._fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(penalty));
            this._goodUse = 1;
            changeHp(dmg, penalty);
        }
    }

    public void start(int x, int y, int z, ItemInstance lure) {
        byte b;
        if (this._fisher.isDead())
            return;
        this._fisher.stopMove(null);
        this._fisher.setIsImmobilized(true);
        this._loc.set(x, y, z);
        this._lure = lure;
        switch (this._lure.getItemId()) {
            case 7807:
            case 7808:
            case 7809:
            case 8486:
                b = 0;
            case 8485:
            case 8506:
            case 8509:
            case 8512:
                b = 2;
                break;
        }
        int group = 1;
        this._fish = FishData.getInstance().getFish(getRandomFishLvl(), getRandomFishType(group), group);
        if (this._fish == null) {
            end(false);
            return;
        }
        this._fisher.sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
        this._fisher.broadcastPacket(new ExFishingStart(this._fisher, this._fish.getType(this._lure.isNightLure()), this._loc, this._lure.isNightLure()));
        this._fisher.sendPacket(new PlaySound(1, "SF_P_01"));
        if (this._lookingForFish == null) {
            int lureid = this._lure.getItemId();
            boolean isNoob = (this._fish.getGroup() == 0);
            boolean isUpperGrade = (this._fish.getGroup() == 2);
            int checkDelay = 0;
            if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) {
                checkDelay = Math.round((float) (this._fish.getGutsCheckTime() * 1.33D));
            } else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || (lureid >= 8505 && lureid <= 8513) || (lureid >= 7610 && lureid <= 7613) || (lureid >= 7807 && lureid <= 7809) || (lureid >= 8484 && lureid <= 8486)) {
                checkDelay = Math.round((float) (this._fish.getGutsCheckTime() * 1.0D));
            } else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) {
                checkDelay = Math.round((float) (this._fish.getGutsCheckTime() * 0.66D));
            }
            long timer = System.currentTimeMillis() + this._fish.getWaitTime() + 10000L;
            this._lookingForFish = ThreadPool.scheduleAtFixedRate(() -> {
                if (System.currentTimeMillis() >= timer) {
                    end(false);
                    return;
                }
                if (this._fish.getType(this._lure.isNightLure()) == -1)
                    return;
                if (this._fish.getGuts() > Rnd.get(1000)) {
                    if (this._lookingForFish != null) {
                        this._lookingForFish.cancel(false);
                        this._lookingForFish = null;
                    }
                    this._fishCurHp = this._fish.getHp();
                    this._time = this._fish.getCombatTime() / 1000;
                    this._isUpperGrade = isUpperGrade;
                    this._lure = lure;
                    if (isUpperGrade) {
                        this._deceptiveMode = (Rnd.get(100) >= 90) ? 1 : 0;
                        this._lureType = 2;
                    } else {
                        this._deceptiveMode = 0;
                        this._lureType = isNoob ? 0 : 1;
                    }
                    this._mode = (Rnd.get(100) >= 80) ? 1 : 0;
                    this._fisher.broadcastPacket(new ExFishingStartCombat(this._fisher, this._time, this._fish.getHp(), this._mode, this._lureType, this._deceptiveMode));
                    this._fisher.sendPacket(new PlaySound(1, "SF_S_01"));
                    this._fisher.sendPacket(SystemMessageId.GOT_A_BITE);
                    if (this._fishCombat == null)
                        this._fishCombat = ThreadPool.scheduleAtFixedRate(() -> {
                        }, 1000L, 1000L);
                }
            }, 10000L, checkDelay);
        }
    }

    public void end(boolean win) {
        if (win)
            if (Rnd.get(100) < 5) {
                int npcId = 18319 + Math.min(this._fisher.getLevel() / 11, 7);
                PenaltyMonster npc = new PenaltyMonster(IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(npcId));
                npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
                npc.spawnMe(this._fisher.getPosition());
                npc.setPlayerToKill(this._fisher);
                this._fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
            } else {
                this._fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
                this._fisher.addItem("Fishing", this._fish.getId(), 1, null, true);
                FishingChampionshipManager.getInstance().newFish(this._fisher, this._lure.getItemId());
            }
        if (this._fish == null)
            this._fisher.sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
        this._time = 0;
        this._stop = 0;
        this._goodUse = 0;
        this._anim = 0;
        this._mode = 0;
        this._deceptiveMode = 0;
        this._thinking = false;
        this._fish = null;
        this._fishCurHp = 0;
        this._isUpperGrade = false;
        this._lure = null;
        this._lureType = 0;
        this._loc.clean();
        this._fisher.broadcastPacket(new ExFishingEnd(win, this._fisher.getObjectId()));
        this._fisher.sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
        this._fisher.setIsImmobilized(false);
        if (this._lookingForFish != null) {
            this._lookingForFish.cancel(false);
            this._lookingForFish = null;
        }
        if (this._fishCombat != null) {
            this._fishCombat.cancel(false);
            this._fishCombat = null;
        }
    }
}
