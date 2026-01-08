package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.DayNightManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GameTimeTaskManager implements Runnable {
    public static final int HOURS_PER_GAME_DAY = 4;
    public static final int MINUTES_PER_GAME_DAY = 240;
    public static final int SECONDS_PER_GAME_DAY = 14400;
    private static final int MINUTES_PER_DAY = 1440;
    private static final int MILLISECONDS_PER_GAME_MINUTE = 10000;
    private static final int TAKE_BREAK_HOURS = 2;
    private static final int TAKE_BREAK_GAME_MINUTES = 720;
    private final Map<Creature, Integer> _players = new ConcurrentHashMap<>();
    private boolean _isNight;
    private List<Quest> _questEvents = Collections.emptyList();
    private int _time;

    private GameTimeTaskManager() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this._time = (int) (System.currentTimeMillis() - cal.getTimeInMillis()) / 10000;
        this._isNight = this.isNight();
        ThreadPool.scheduleAtFixedRate(this, 10000L, 10000L);
    }

    public static GameTimeTaskManager getInstance() {
        return GameTimeTaskManager.SingletonHolder.INSTANCE;
    }

    public void run() {
        ++this._time;

        for (Quest quest : this._questEvents) {
            quest.onGameTime();
        }

        L2Skill skill = null;
        if (this._isNight != this.isNight()) {
            this._isNight = !this._isNight;
            DayNightManager.getInstance().notifyChangeMode();
            skill = SkillTable.getInstance().getInfo(294, 1);
        }

        if (!this._players.isEmpty()) {
            for (Map.Entry<Creature, Integer> entry : this._players.entrySet()) {
                Player player = (Player) entry.getKey();
                if (player.isOnline()) {
                    if (skill != null && player.hasSkill(294)) {
                        player.removeSkill(294, false);
                        player.addSkill(skill, false);
                        player.sendPacket(SystemMessage.getSystemMessage(this._isNight ? SystemMessageId.NIGHT_S1_EFFECT_APPLIES : SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(294));
                    }

                    if (this._time >= entry.getValue()) {
                        player.sendPacket(SystemMessageId.PLAYING_FOR_LONG_TIME);
                        entry.setValue(this._time + 720);
                    }
                }
            }

        }
    }

    public void addQuestEvent(Quest quest) {
        if (this._questEvents.isEmpty()) {
            this._questEvents = new ArrayList<>(3);
        }

        this._questEvents.add(quest);
    }

    public int getGameDay() {
        return this._time / 1440;
    }

    public int getGameTime() {
        return this._time % 1440;
    }

    public int getGameHour() {
        return this._time % 1440 / 60;
    }

    public int getGameMinute() {
        return this._time % 60;
    }

    public String getGameTimeFormated() {
        return String.format("%02d:%02d", this.getGameHour(), this.getGameMinute());
    }

    public boolean isNight() {
        return this.getGameTime() < 360;
    }

    public void add(Player player) {
        this._players.put(player, this._time + 720);
    }

    public void remove(Creature player) {
        this._players.remove(player);
    }

    private static class SingletonHolder {
        protected static final GameTimeTaskManager INSTANCE = new GameTimeTaskManager();
    }
}
