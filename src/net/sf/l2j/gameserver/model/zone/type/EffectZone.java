package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class EffectZone extends ZoneType {
    private final List<IntIntHolder> _skills = new ArrayList<>(5);

    private int _chance = 100;

    private int _initialDelay = 0;

    private int _reuseDelay = 30000;

    private boolean _isEnabled = true;

    private Future<?> _task;

    private String _target = "Playable";

    public EffectZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("chance")) {
            this._chance = Integer.parseInt(value);
        } else if (name.equals("initialDelay")) {
            this._initialDelay = Integer.parseInt(value);
        } else if (name.equals("reuseDelay")) {
            this._reuseDelay = Integer.parseInt(value);
        } else if (name.equals("defaultStatus")) {
            this._isEnabled = Boolean.parseBoolean(value);
        } else if (name.equals("skill")) {
            String[] skills = value.split(";");
            for (String skill : skills) {
                String[] skillSplit = skill.split("-");
                if (skillSplit.length != 2) {
                    LOGGER.warn("Invalid skill format {} for {}.", skill, toString());
                } else {
                    try {
                        this._skills.add(new IntIntHolder(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1])));
                    } catch (NumberFormatException nfe) {
                        LOGGER.warn("Invalid skill format {} for {}.", skill, toString());
                    }
                }
            }
        } else if (name.equals("targetType")) {
            this._target = value;
        } else {
            super.setParameter(name, value);
        }
    }

    protected boolean isAffected(Creature character) {
        try {
            if (!Class.forName("net.sf.l2j.gameserver.model.actor." + this._target).isInstance(character))
                return false;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error for {} on invalid target type {}.", e, toString(), this._target);
        }
        return true;
    }

    protected void onEnter(Creature character) {
        if (this._task == null)
            synchronized (this) {
                if (this._task == null)
                    this._task = ThreadPool.scheduleAtFixedRate(() -> {
                        if (!this._isEnabled)
                            return;
                        if (this._characters.isEmpty()) {
                            this._task.cancel(true);
                            this._task = null;
                            return;
                        }
                        for (Creature temp : this._characters.values()) {
                            if (temp.isDead() || Rnd.get(100) >= this._chance)
                                continue;
                            for (IntIntHolder entry : this._skills) {
                                L2Skill skill = entry.getSkill();
                                if (skill != null && skill.checkCondition(temp, temp, false) && temp.getFirstEffect(entry.getId()) == null)
                                    skill.getEffects(temp, temp);
                            }
                        }
                    }, this._initialDelay, this._reuseDelay);
            }
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.DANGER_AREA, true);
            character.sendPacket(new EtcStatusUpdate((Player) character));
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.DANGER_AREA, false);
            if (!character.isInsideZone(ZoneId.DANGER_AREA))
                character.sendPacket(new EtcStatusUpdate((Player) character));
        }
    }

    public void editStatus(boolean state) {
        this._isEnabled = state;
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
