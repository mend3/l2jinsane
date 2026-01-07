package enginemods.main.engine;

import enginemods.main.EngineModsManager;
import enginemods.main.data.ModsData;
import enginemods.main.enums.EngineStateType;
import enginemods.main.enums.WeekDayType;
import enginemods.main.holders.ModTimerHolder;
import enginemods.main.instances.NpcDropsInstance;
import enginemods.main.instances.NpcExpInstance;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractMods {
    public static final Logger LOG = Logger.getLogger(AbstractMods.class.getName());
    protected static List<Future<?>> _sheduledStateMod = new ArrayList<>();
    private final Map<Integer, List<ModTimerHolder>> _eventTimers;
    protected EngineStateType _state;

    public AbstractMods() {
        this._state = EngineStateType.END;
        this._eventTimers = new ConcurrentHashMap<>();
    }

    public static void sendHtml(Player player, Npc npc, HtmlBuilder content) {
        NpcHtmlMessage html = new NpcHtmlMessage(npc == null ? 0 : npc.getObjectId());
        html.setHtml(content.toString());
        player.sendPacket(html);
    }

    public static void sendHtml(Player player, Npc npc, String content) {
        NpcHtmlMessage html = new NpcHtmlMessage(npc == null ? 0 : npc.getObjectId());
        html.setHtml(content);
        player.sendPacket(html);
    }

    public static void sendHtmlFile(Player player, Npc npc, String htmlFile) {
        NpcHtmlMessage html = new NpcHtmlMessage(npc == null ? 0 : npc.getObjectId());
        html.setFile(htmlFile);
        player.sendPacket(html);
    }

    public static void sendCommunity(Player player, String html) {
        if (html != null && player != null) {
            if (html.length() < 4090) {
                player.sendPacket(new ShowBoard(html, "101"));
                player.sendPacket(ShowBoard.STATIC_SHOWBOARD_102);
                player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103);
            } else if (html.length() < 8180) {
                player.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
                player.sendPacket(new ShowBoard(html.substring(4090), "102"));
                player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103);
            } else if (html.length() < 12270) {
                player.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
                player.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
                player.sendPacket(new ShowBoard(html.substring(8180), "103"));
            } else {
                PrintStream var10000 = System.out;
                int var10001 = html.length();
                var10000.println("community html muy largo-> " + (var10001 - 12270));
            }

        }
    }

    public void cancelScheduledState() {
        Iterator var1 = _sheduledStateMod.iterator();

        while (var1.hasNext()) {
            Future<?> run = (Future) var1.next();
            run.cancel(true);
        }

    }

    public EngineStateType getState() {
        return this._state;
    }

    public abstract void onModState();

    public String getValueDB(int objectId, String event) {
        return ModsData.get(objectId, event, this);
    }

    public String getValueDB(Player player, String event) {
        return ModsData.get(player.getObjectId(), event, this);
    }

    public void setValueDB(Player player, String event, String value) {
        ModsData.set(player.getObjectId(), event, value, this);
    }

    public void setValueDB(int objectId, String event, String value) {
        ModsData.set(objectId, event, value, this);
    }

    public void clearValueDB() {
        ModsData.remove(this);
    }

    public void removeValueDB(int objectId, String event) {
        ModsData.remove(objectId, event, this);
    }

    public void startTimer(String name, long time, Npc npc, Player player, boolean repeating) {
        List<ModTimerHolder> timers = this._eventTimers.get(name.hashCode());
        if (timers == null) {
            timers = new CopyOnWriteArrayList<>();
            timers.add(new ModTimerHolder(this, name, npc, player, time, repeating));
            this._eventTimers.put(name.hashCode(), timers);
        } else {
            Iterator var8 = timers.iterator();

            while (var8.hasNext()) {
                ModTimerHolder timer = (ModTimerHolder) var8.next();
                if (timer != null && timer.equals(this, name, npc, player)) {
                    return;
                }
            }

            timers.add(new ModTimerHolder(this, name, npc, player, time, repeating));
        }

    }

    public ModTimerHolder getTimer(String name) {
        return this.getTimer(name, null, null);
    }

    public ModTimerHolder getTimer(String name, Player player) {
        return this.getTimer(name, null, player);
    }

    public ModTimerHolder getTimer(String name, Npc npc, Player player) {
        List<ModTimerHolder> timers = this._eventTimers.get(name.hashCode());
        if (timers != null && !timers.isEmpty()) {
            Iterator var5 = timers.iterator();

            ModTimerHolder timer;
            do {
                if (!var5.hasNext()) {
                    return null;
                }

                timer = (ModTimerHolder) var5.next();
            } while (timer == null || !timer.equals(this, name, npc, player));

            return timer;
        } else {
            return null;
        }
    }

    public void cancelTimer(String name, Npc npc, Player player) {
        ModTimerHolder timer = this.getTimer(name, npc, player);
        if (timer != null) {
            timer.cancel();
        }

    }

    public void cancelTimers(String name) {
        List<ModTimerHolder> timers = this._eventTimers.get(name.hashCode());
        if (timers != null && !timers.isEmpty()) {
            Iterator var3 = timers.iterator();

            while (var3.hasNext()) {
                ModTimerHolder timer = (ModTimerHolder) var3.next();
                if (timer != null) {
                    timer.cancel();
                }
            }

        }
    }

    public void removeTimer(ModTimerHolder timer) {
        if (timer != null) {
            List<ModTimerHolder> timers = this._eventTimers.get(timer.getName().hashCode());
            if (timers != null && !timers.isEmpty()) {
                timers.remove(timer);
            }
        }
    }

    public Npc addSpawn(int npcId, Location loc, boolean randomOffset, long despawnDelay) {
        return this.addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, randomOffset, despawnDelay);
    }

    public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay) {
        Npc npc = null;

        try {
            NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
            if (template != null) {
                if (x == 0 && y == 0) {
                    LOG.log(Level.SEVERE, "Failed to adjust bad locks for mod spawn!  Spawn aborted!");
                    return null;
                }

                if (randomOffset) {
                    x += Rnd.get(-100, 100);
                    y += Rnd.get(-100, 100);
                }

                L2Spawn spawn = new L2Spawn(template);
                spawn.setLoc(x, y, z + 20, heading);
                spawn.setRespawnState(false);
                npc = spawn.doSpawn(true);
                if (despawnDelay > 0L) {
                    npc.scheduleDespawn(despawnDelay);
                }
            }
        } catch (Exception var12) {
            LOG.warning("Could not spawn Npc " + npcId);
        }

        return npc;
    }

    public boolean isStarting() {
        return this._state == EngineStateType.START;
    }

    public void registerMod(boolean config) {
        EngineModsManager.registerMod(this);
        if (config) {
            this.startMod();
        }

    }

    public void registerMod(boolean config, List<WeekDayType> day) {
        EngineModsManager.registerMod(this);
        if (config) {
            Iterator var3 = day.iterator();

            while (var3.hasNext()) {
                WeekDayType d = (WeekDayType) var3.next();
                this.registerMod(d);
            }
        }

    }

    private void registerMod(WeekDayType day) {
        for (int weekToStartEvent = 1; weekToStartEvent >= 0; --weekToStartEvent) {
            int eventTime = -1;
            int missingDayToStart = 0;
            Calendar time = new GregorianCalendar();
            int i = time.get(7);

            while (eventTime < 0) {
                if (WeekDayType.values()[i - 1] == day) {
                    eventTime = missingDayToStart;
                } else {
                    ++i;
                    ++missingDayToStart;
                    if (i > WeekDayType.values().length) {
                        i = 1;
                    }
                }
            }

            eventTime += weekToStartEvent * 7;
            time.add(6, eventTime);
            long timeStart = time.getTimeInMillis() - System.currentTimeMillis();
            _sheduledStateMod.add(ThreadPool.schedule(new AbstractMods.ScheduleStart(), timeStart < 0L ? 0L : timeStart));
            time.add(6, eventTime + 1);
            _sheduledStateMod.add(ThreadPool.schedule(new AbstractMods.ScheduleEnd(), time.getTimeInMillis() - System.currentTimeMillis()));
        }

    }

    public void registerMod(String start, String end) {
        EngineModsManager.registerMod(this);

        try {
            StringTokenizer parse = null;
            parse = new StringTokenizer(start, "-");
            int diaStart = Integer.parseInt(parse.nextToken());
            int mesStart = Integer.parseInt(parse.nextToken());
            int anioStart = Integer.parseInt(parse.nextToken());
            parse = new StringTokenizer(end, "-");
            int diaEnd = Integer.parseInt(parse.nextToken());
            int mesEnd = Integer.parseInt(parse.nextToken());
            int anioEnd = Integer.parseInt(parse.nextToken());
            Calendar timeStart = new GregorianCalendar();
            timeStart.set(anioStart, mesStart, diaStart, 0, 0, 0);
            Calendar timeEnd = new GregorianCalendar();
            timeEnd.set(anioEnd, mesEnd, diaEnd, 0, 0, 0);
            long hoy = System.currentTimeMillis();
            Logger var10000 = LOG;
            String var10001 = this.getClass().getSimpleName();
            var10000.warning("Event " + var10001 + ": Start! -> " + timeStart.getTime());
            var10000 = LOG;
            var10001 = this.getClass().getSimpleName();
            var10000.warning("Event " + var10001 + ": End! -> " + timeEnd.getTime());
            if (timeEnd.getTimeInMillis() >= hoy) {
                if (timeStart.getTimeInMillis() >= timeEnd.getTimeInMillis()) {
                    LOG.warning("Event " + this.getClass().getSimpleName() + ": The start date of the event can not be greater than or equal to the end of the event");
                } else {
                    long time = 0L;
                    if (timeStart.getTimeInMillis() - hoy > 0L) {
                        time = timeStart.getTimeInMillis() - hoy;
                    }

                    _sheduledStateMod.add(ThreadPool.schedule(new AbstractMods.ScheduleStart(), time));
                    _sheduledStateMod.add(ThreadPool.schedule(new AbstractMods.ScheduleEnd(), timeEnd.getTimeInMillis() - hoy));
                }
            }
        } catch (Exception var16) {
            LOG.warning("Event " + this.getClass().getSimpleName() + ": The date of the event register is invalid");
        }
    }

    public void endMod() {
        this._state = EngineStateType.END;
        World.announceToOnlinePlayers("Event " + this.getClass().getSimpleName() + ": End!", true);
        LOG.info("Event " + this.getClass().getSimpleName() + ": End!");
        this.onModState();
    }

    private void startMod() {
        this._state = EngineStateType.START;
        World.announceToOnlinePlayers("Event " + this.getClass().getSimpleName() + ": Start!", true);
        LOG.info("Event " + this.getClass().getSimpleName() + ": Start!");
        this.onModState();
    }

    public boolean onCommunityBoard(Player player, String command) {
        return false;
    }

    public void onShutDown() {
    }

    public boolean onExitWorld(Player player) {
        return false;
    }

    public void onNpcExpSp(Player killer, Attackable npc, NpcExpInstance instance) {
    }

    public void onNpcDrop(Player killer, Attackable npc, NpcDropsInstance instance) {
    }

    public void onEnterZone(Creature player, ZoneType zone) {
    }

    public void onExitZone(Creature player, ZoneType zone) {
    }

    public void onCreateCharacter(Player player) {
    }

    public boolean onChat(Player player, String chat) {
        return false;
    }

    public boolean onAdminCommand(Player player, String chat) {
        return false;
    }

    public boolean onVoicedCommand(Player player, String chat) {
        return false;
    }

    public boolean onInteract(Player player, Creature character) {
        return false;
    }

    public void onEvent(Player player, Creature npc, String command) {
    }

    public void onTimer(String timerName, Npc npc, Player player) {
    }

    public String onSeeNpcTitle(int objectId) {
        return null;
    }

    public void onSpawn(Npc obj) {
    }

    public void onEnterWorld(Player player) {
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
    }

    public void onDeath(Creature player) {
    }

    public void onEnchant(Creature player) {
    }

    public void onEquip(Creature player) {
    }

    public void onUnequip(Creature player) {
    }

    public boolean onRestoreSkills(Player player) {
        return false;
    }

    public double onStats(Stats stat, Creature character, double value) {
        return value;
    }

    protected class ScheduleStart implements Runnable {
        public void run() {
            AbstractMods.this.startMod();
        }
    }

    protected class ScheduleEnd implements Runnable {
        public void run() {
            AbstractMods.this.endMod();
        }
    }
}