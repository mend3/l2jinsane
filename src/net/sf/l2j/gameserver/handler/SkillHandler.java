package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.skillhandlers.*;

import java.util.HashMap;
import java.util.Map;

public class SkillHandler {
    private final Map<Integer, ISkillHandler> _entries = new HashMap<>();

    protected SkillHandler() {
        registerHandler(new BalanceLife());
        registerHandler(new Blow());
        registerHandler(new Cancel());
        registerHandler(new CombatPointHeal());
        registerHandler(new Continuous());
        registerHandler(new CpDamPercent());
        registerHandler(new Craft());
        registerHandler(new Disablers());
        registerHandler(new DrainSoul());
        registerHandler(new Dummy());
        registerHandler(new Extractable());
        registerHandler(new Fishing());
        registerHandler(new FishingSkill());
        registerHandler(new GetPlayer());
        registerHandler(new GiveSp());
        registerHandler(new Harvest());
        registerHandler(new Heal());
        registerHandler(new HealPercent());
        registerHandler(new InstantJump());
        registerHandler(new Manadam());
        registerHandler(new ManaHeal());
        registerHandler(new Mdam());
        registerHandler(new Pdam());
        registerHandler(new Resurrect());
        registerHandler(new Sow());
        registerHandler(new Spoil());
        registerHandler(new StrSiegeAssault());
        registerHandler(new SummonFriend());
        registerHandler(new Sweep());
        registerHandler(new TakeCastle());
        registerHandler(new Unlock());
    }

    public static SkillHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void registerHandler(ISkillHandler handler) {
        for (L2SkillType t : handler.getSkillIds())
            this._entries.put(t.ordinal(), handler);
    }

    public ISkillHandler getHandler(L2SkillType skillType) {
        return this._entries.get(skillType.ordinal());
    }

    public int size() {
        return this._entries.size();
    }

    private static class SingletonHolder {
        protected static final SkillHandler INSTANCE = new SkillHandler();
    }
}
