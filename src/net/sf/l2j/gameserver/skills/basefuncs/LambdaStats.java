package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.enums.skills.StatsType;
import net.sf.l2j.gameserver.skills.Env;

public final class LambdaStats extends Lambda {
    private final StatsType _stat;

    public LambdaStats(StatsType stat) {
        this._stat = stat;
    }

    public double calc(Env env) {
        switch (this._stat) {
            case PLAYER_LEVEL -> {
                return env.getCharacter() == null ? (double)1.0F : (double)env.getCharacter().getLevel();
            }
            case TARGET_LEVEL -> {
                return env.getTarget() == null ? (double)1.0F : (double)env.getTarget().getLevel();
            }
            case PLAYER_MAX_HP -> {
                return env.getCharacter() == null ? (double)1.0F : (double)env.getCharacter().getMaxHp();
            }
            case PLAYER_MAX_MP -> {
                return env.getCharacter() == null ? (double)1.0F : (double)env.getCharacter().getMaxMp();
            }
            default -> {
                return 0.0F;
            }
        }
    }
}
