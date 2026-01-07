package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.model.L2Skill;

public class SkillUseHolder {
    private L2Skill _skill;

    private boolean _ctrlPressed;

    private boolean _shiftPressed;

    public SkillUseHolder() {
    }

    public SkillUseHolder(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        this._skill = skill;
        this._ctrlPressed = ctrlPressed;
        this._shiftPressed = shiftPressed;
    }

    public L2Skill getSkill() {
        return this._skill;
    }

    public void setSkill(L2Skill skill) {
        this._skill = skill;
    }

    public int getSkillId() {
        return (getSkill() != null) ? getSkill().getId() : -1;
    }

    public boolean isCtrlPressed() {
        return this._ctrlPressed;
    }

    public void setCtrlPressed(boolean ctrlPressed) {
        this._ctrlPressed = ctrlPressed;
    }

    public boolean isShiftPressed() {
        return this._shiftPressed;
    }

    public void setShiftPressed(boolean shiftPressed) {
        this._shiftPressed = shiftPressed;
    }
}
