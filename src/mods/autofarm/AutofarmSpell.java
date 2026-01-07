package mods.autofarm;

public class AutofarmSpell {
    private final Integer _skillId;

    private final AutofarmSpellType _spellType;

    public AutofarmSpell(Integer skillId, AutofarmSpellType spellType) {
        this._skillId = skillId;
        this._spellType = spellType;
    }

    public Integer getSkillId() {
        return this._skillId;
    }

    public AutofarmSpellType getSpellType() {
        return this._spellType;
    }
}
