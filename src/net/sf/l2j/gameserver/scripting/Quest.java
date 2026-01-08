/**/
package net.sf.l2j.gameserver.scripting;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Quest {
    public static final byte STATE_CREATED = 0;
    public static final byte STATE_STARTED = 1;
    public static final byte STATE_COMPLETED = 2;
    protected static final CLogger LOGGER = new CLogger(Quest.class.getName());
    protected static final Map<Integer, Integer> DF_REWARD_35 = new HashMap<>();
    protected static final Map<Integer, Integer> DF_REWARD_37 = new HashMap<>();
    protected static final Map<Integer, Integer> DF_REWARD_39 = new HashMap<>();
    private static final String HTML_NONE_AVAILABLE = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
    private static final String HTML_ALREADY_COMPLETED = "<html><body>This quest has already been completed.</body></html>";
    private static final String HTML_TOO_MUCH_QUESTS = "<html><body>You have already accepted the maximum number of quests. No more than 25 quests may be undertaken simultaneously.<br>For quest information, enter Alt+U.</body></html>";
    private final Map<Integer, List<QuestTimer>> _eventTimers = new ConcurrentHashMap<>();
    private final int _id;
    private final String _descr;
    private boolean _onEnterWorld;
    private int[] _itemsIds;

    public Quest(int questId, String descr) {
        DF_REWARD_35.put(1, 61);
        DF_REWARD_35.put(4, 45);
        DF_REWARD_35.put(7, 128);
        DF_REWARD_35.put(11, 168);
        DF_REWARD_35.put(15, 49);
        DF_REWARD_35.put(19, 61);
        DF_REWARD_35.put(22, 128);
        DF_REWARD_35.put(26, 168);
        DF_REWARD_35.put(29, 49);
        DF_REWARD_35.put(32, 61);
        DF_REWARD_35.put(35, 128);
        DF_REWARD_35.put(39, 168);
        DF_REWARD_35.put(42, 49);
        DF_REWARD_35.put(45, 61);
        DF_REWARD_35.put(47, 61);
        DF_REWARD_35.put(50, 49);
        DF_REWARD_35.put(54, 85);
        DF_REWARD_35.put(56, 85);
        DF_REWARD_37.put(0, 96);
        DF_REWARD_37.put(1, 102);
        DF_REWARD_37.put(2, 98);
        DF_REWARD_37.put(3, 109);
        DF_REWARD_37.put(4, 50);
        DF_REWARD_39.put(1, 72);
        DF_REWARD_39.put(4, 104);
        DF_REWARD_39.put(7, 96);
        DF_REWARD_39.put(11, 122);
        DF_REWARD_39.put(15, 60);
        DF_REWARD_39.put(19, 72);
        DF_REWARD_39.put(22, 96);
        DF_REWARD_39.put(26, 122);
        DF_REWARD_39.put(29, 45);
        DF_REWARD_39.put(32, 104);
        DF_REWARD_39.put(35, 96);
        DF_REWARD_39.put(39, 122);
        DF_REWARD_39.put(42, 60);
        DF_REWARD_39.put(45, 64);
        DF_REWARD_39.put(47, 72);
        DF_REWARD_39.put(50, 92);
        DF_REWARD_39.put(54, 82);
        DF_REWARD_39.put(56, 23);
        this._id = questId;
        this._descr = descr;
    }

    public static boolean getSponsor(Player player) {
        int sponsorId = player.getSponsor();
        if (sponsorId == 0) {
            return false;
        } else {
            Clan clan = player.getClan();
            if (clan == null) {
                return false;
            } else {
                ClanMember member = clan.getClanMember(sponsorId);
                if (member != null && member.isOnline()) {
                    Player sponsor = member.getPlayerInstance();
                    return sponsor != null && player.isInsideRadius(sponsor, 1500, true, false);
                }

                return false;
            }
        }
    }

    public static Player getApprentice(Player player) {
        int apprenticeId = player.getApprentice();
        if (apprenticeId == 0) {
            return null;
        } else {
            Clan clan = player.getClan();
            if (clan == null) {
                return null;
            } else {
                ClanMember member = clan.getClanMember(apprenticeId);
                if (member != null && member.isOnline()) {
                    Player academic = member.getPlayerInstance();
                    if (academic != null && player.isInsideRadius(academic, 1500, true, false)) {
                        return academic;
                    }
                }

                return null;
            }
        }
    }

    public static String getNoQuestMsg() {
        return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
    }

    public static String getAlreadyCompletedMsg() {
        return "<html><body>This quest has already been completed.</body></html>";
    }

    public static String getTooMuchQuestsMsg() {
        return "<html><body>You have already accepted the maximum number of quests. No more than 25 quests may be undertaken simultaneously.<br>For quest information, enter Alt+U.</body></html>";
    }

    public String toString() {
        return this._id + " " + this._descr;
    }

    public final String getName() {
        return this.getClass().getSimpleName();
    }

    public int getQuestId() {
        return this._id;
    }

    public boolean isRealQuest() {
        return this._id > 0;
    }

    public String getDescr() {
        return this._descr;
    }

    public boolean getOnEnterWorld() {
        return this._onEnterWorld;
    }

    public void setOnEnterWorld(boolean val) {
        this._onEnterWorld = val;
    }

    public int[] getItemsIds() {
        return this._itemsIds;
    }

    public void setItemsIds(int... itemIds) {
        this._itemsIds = itemIds;
    }

    public QuestState newQuestState(Player player) {
        return new QuestState(player, this, (byte) 0);
    }

    public QuestState checkPlayerCondition(Player player, Npc npc, String var, String value) {
        if (player != null && npc != null) {
            QuestState st = player.getQuestState(this.getName());
            if (st == null) {
                return null;
            } else if (st.get(var) != null && value.equalsIgnoreCase(st.get(var))) {
                return !player.isInsideRadius(npc, Config.PARTY_RANGE, true, false) ? null : st;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<QuestState> getPartyMembers(Player player, Npc npc, String var, String value) {
        if (player == null) {
            return Collections.emptyList();
        } else {
            Party party = player.getParty();
            if (party == null) {
                QuestState st = this.checkPlayerCondition(player, npc, var, value);
                return st != null ? List.of(st) : Collections.emptyList();
            } else {
                List<QuestState> list = new ArrayList<>();

                for (Player member : party.getMembers()) {
                    QuestState st = this.checkPlayerCondition(member, npc, var, value);
                    if (st != null) {
                        list.add(st);
                    }
                }

                return list;
            }
        }
    }

    public QuestState getRandomPartyMember(Player player, Npc npc, String var, String value) {
        return player == null ? null : Rnd.get(this.getPartyMembers(player, npc, var, value));
    }

    public QuestState getRandomPartyMember(Player player, Npc npc, String value) {
        return this.getRandomPartyMember(player, npc, "cond", value);
    }

    public QuestState checkPlayerState(Player player, Npc npc, byte state) {
        if (player != null && npc != null) {
            QuestState st = player.getQuestState(this.getName());
            if (st == null) {
                return null;
            } else if (st.getState() != state) {
                return null;
            } else {
                return !player.isInsideRadius(npc, Config.PARTY_RANGE, true, false) ? null : st;
            }
        } else {
            return null;
        }
    }

    public List<QuestState> getPartyMembersState(Player player, Npc npc, byte state) {
        if (player == null) {
            return Collections.emptyList();
        } else {
            Party party = player.getParty();
            if (party == null) {
                QuestState st = this.checkPlayerState(player, npc, state);
                return st != null ? List.of(st) : Collections.emptyList();
            } else {
                List<QuestState> list = new ArrayList<>();

                for (Player member : party.getMembers()) {
                    QuestState st = this.checkPlayerState(member, npc, state);
                    if (st != null) {
                        list.add(st);
                    }
                }

                return list;
            }
        }
    }

    public QuestState getRandomPartyMemberState(Player player, Npc npc, byte state) {
        return player == null ? null : Rnd.get(this.getPartyMembersState(player, npc, state));
    }

    public QuestState getClanLeaderQuestState(Player player, Npc npc) {
        if (player == null) {
            return null;
        } else if (player.isClanLeader() && player.isInsideRadius(npc, Config.PARTY_RANGE, true, false)) {
            return player.getQuestState(this.getName());
        } else {
            Clan clan = player.getClan();
            if (clan == null) {
                return null;
            } else {
                Player leader = clan.getLeader().getPlayerInstance();
                if (leader == null) {
                    return null;
                } else {
                    return leader.isInsideRadius(npc, Config.PARTY_RANGE, true, false) ? leader.getQuestState(this.getName()) : null;
                }
            }
        }
    }

    public void startQuestTimer(String name, long time, Npc npc, Player player, boolean repeating) {
        List<QuestTimer> timers = this._eventTimers.get(name.hashCode());
        if (timers == null) {
            timers = new CopyOnWriteArrayList<>();
            timers.add(new QuestTimer(this, name, npc, player, time, repeating));
            this._eventTimers.put(name.hashCode(), timers);
        } else {

            for (QuestTimer timer : timers) {
                if (timer != null && timer.equals(this, name, npc, player)) {
                    return;
                }
            }

            timers.add(new QuestTimer(this, name, npc, player, time, repeating));
        }

    }

    public QuestTimer getQuestTimer(String name, Npc npc, Player player) {
        List<QuestTimer> timers = this._eventTimers.get(name.hashCode());
        if (timers != null && !timers.isEmpty()) {
            Iterator<QuestTimer> var5 = timers.iterator();

            QuestTimer timer;
            do {
                if (!var5.hasNext()) {
                    return null;
                }

                timer = var5.next();
            } while (timer == null || !timer.equals(this, name, npc, player));

            return timer;
        } else {
            return null;
        }
    }

    public void cancelQuestTimer(String name, Npc npc, Player player) {
        QuestTimer timer = this.getQuestTimer(name, npc, player);
        if (timer != null) {
            timer.cancel();
        }

    }

    public void cancelQuestTimers(String name) {
        List<QuestTimer> timers = this._eventTimers.get(name.hashCode());
        if (timers != null && !timers.isEmpty()) {

            for (QuestTimer timer : timers) {
                if (timer != null) {
                    timer.cancel();
                }
            }

        }
    }

    void removeQuestTimer(QuestTimer timer) {
        if (timer != null) {
            List<QuestTimer> timers = this._eventTimers.get(timer.toString().hashCode());
            if (timers != null && !timers.isEmpty()) {
                timers.remove(timer);
            }
        }
    }

    public Npc addSpawn(int npcId, Creature cha, boolean randomOffset, long despawnDelay, boolean isSummonSpawn) {
        return this.addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay, isSummonSpawn);
    }

    public Npc addSpawn(int npcId, SpawnLocation loc, boolean randomOffset, long despawnDelay, boolean isSummonSpawn) {
        return this.addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay, isSummonSpawn);
    }

    public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn) {
        try {
            NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
            if (template == null) {
                return null;
            } else {
                if (randomOffset) {
                    x += Rnd.get(-100, 100);
                    y += Rnd.get(-100, 100);
                }

                L2Spawn spawn = new L2Spawn(template);
                spawn.setLoc(x, y, z + 20, heading);
                spawn.setRespawnState(false);
                Npc npc = spawn.doSpawn(isSummonSpawn);
                if (despawnDelay > 0L) {
                    npc.scheduleDespawn(despawnDelay);
                }

                return npc;
            }
        } catch (Exception var13) {
            LOGGER.error("Couldn't spawn npcId {} for {}.", npcId, this.toString());
            return null;
        }
    }

    protected void showResult(Npc npc, Creature creature, String result) {
        if (creature != null) {
            Player player = creature.getActingPlayer();
            if (player != null && result != null && !result.isEmpty()) {
                NpcHtmlMessage npcReply;
                if (!result.endsWith(".htm") && !result.endsWith(".html")) {
                    if (result.startsWith("<html>")) {
                        npcReply = new NpcHtmlMessage(npc == null ? 0 : npc.getNpcId());
                        npcReply.setHtml(result);
                        if (npc != null) {
                            npcReply.replace("%objectId%", npc.getObjectId());
                        }

                        player.sendPacket(npcReply);
                        player.sendPacket(ActionFailed.STATIC_PACKET);
                    } else {
                        player.sendMessage(result);
                    }
                } else {
                    npcReply = new NpcHtmlMessage(npc == null ? 0 : npc.getNpcId());
                    String var10001;
                    if (this.isRealQuest()) {
                        var10001 = this.getName();
                        npcReply.setFile("./data/html/scripts/quests/" + var10001 + "/" + result);
                    } else {
                        var10001 = this.getDescr();
                        npcReply.setFile("./data/html/scripts/" + var10001 + "/" + this.getName() + "/" + result);
                    }

                    if (npc != null) {
                        npcReply.replace("%objectId%", npc.getObjectId());
                    }

                    player.sendPacket(npcReply);
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                }

            }
        }
    }

    public String getHtmlText(String fileName) {
        HtmCache var10000;
        String var10001;
        if (this.isRealQuest()) {
            var10000 = HtmCache.getInstance();
            var10001 = this.getName();
            return var10000.getHtmForce("./data/html/scripts/quests/" + var10001 + "/" + fileName);
        } else {
            var10000 = HtmCache.getInstance();
            var10001 = this.getDescr();
            return var10000.getHtmForce("./data/html/scripts/" + var10001 + "/" + this.getName() + "/" + fileName);
        }
    }

    public void addEventId(int npcId, ScriptEventType eventType) {
        NpcTemplate t = NpcData.getInstance().getTemplate(npcId);
        if (t != null) {
            t.addQuestEvent(eventType, this);
        }

    }

    public void addEventIds(int npcId, ScriptEventType... eventTypes) {
        NpcTemplate t = NpcData.getInstance().getTemplate(npcId);
        if (t != null) {
            ScriptEventType[] var4 = eventTypes;
            int var5 = eventTypes.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                ScriptEventType eventType = var4[var6];
                t.addQuestEvent(eventType, this);
            }
        }

    }

    public void addEventIds(int[] npcIds, ScriptEventType... eventTypes) {
        int[] var3 = npcIds;
        int var4 = npcIds.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            int id = var3[var5];
            this.addEventIds(id, eventTypes);
        }

    }

    public void addEventIds(Iterable<Integer> npcIds, ScriptEventType... eventTypes) {

        for (int id : npcIds) {
            this.addEventIds(id, eventTypes);
        }

    }

    public void addStartNpc(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.QUEST_START);
        }

    }

    public void addAttackId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_ATTACK);
        }

    }

    public final void notifyAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        String res = null;

        try {
            res = this.onAttack(npc, attacker, damage, skill);
        } catch (Exception var7) {
            LOGGER.warn(this.toString(), var7);
            return;
        }

        this.showResult(npc, attacker, res);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        return null;
    }

    public void addAttackActId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_ATTACK_ACT);
        }

    }

    public final void notifyAttackAct(Npc npc, Player victim) {
        String res = null;

        try {
            res = this.onAttackAct(npc, victim);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        this.showResult(npc, victim, res);
    }

    public String onAttackAct(Npc npc, Player victim) {
        return null;
    }

    public void addAggroRangeEnterId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_AGGRO);
        }

    }

    public final void notifyAggro(Npc npc, Player player, boolean isPet) {
        ThreadPool.execute(new Quest.OnAggroEnter(npc, player, isPet));
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        return null;
    }

    public void addCreatureSeeId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_CREATURE_SEE);
        }

    }

    public final void notifyCreatureSee(Npc npc, Creature creature) {
        String res = null;

        try {
            res = this.onCreatureSee(npc, creature);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        this.showResult(npc, creature, res);
    }

    public String onCreatureSee(Npc npc, Creature creature) {
        return null;
    }

    public final void notifyDeath(Creature killer, Player player) {
        String res = null;

        try {
            res = this.onDeath(killer, player);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        this.showResult(killer instanceof Npc ? (Npc) killer : null, player, res);
    }

    public String onDeath(Creature killer, Player player) {
        return this.onAdvEvent("", killer instanceof Npc ? (Npc) killer : null, player);
    }

    public final void notifyEvent(String event, Npc npc, Player player) {
        String res = null;

        try {
            res = this.onAdvEvent(event, npc, player);
        } catch (Exception var6) {
            LOGGER.warn(this.toString(), var6);
            return;
        }

        this.showResult(npc, player, res);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (player != null) {
            QuestState qs = player.getQuestState(this.getName());
            if (qs != null) {
                return this.onEvent(event, qs);
            }
        }

        return null;
    }

    public String onEvent(String event, QuestState qs) {
        return null;
    }

    public final void notifyEnterWorld(Player player) {
        String res = null;

        try {
            res = this.onEnterWorld(player);
        } catch (Exception var4) {
            LOGGER.warn(this.toString(), var4);
            return;
        }

        this.showResult(null, player, res);
    }

    public String onEnterWorld(Player player) {
        return null;
    }

    public void addEnterZoneId(int... zoneIds) {
        int[] var2 = zoneIds;
        int var3 = zoneIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int zoneId = var2[var4];
            ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
            if (zone != null) {
                zone.addQuestEvent(ScriptEventType.ON_ENTER_ZONE, this);
            }
        }

    }

    public final void notifyEnterZone(Creature character, ZoneType zone) {
        String res = null;

        try {
            res = this.onEnterZone(character, zone);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        this.showResult(null, character, res);
    }

    public String onEnterZone(Creature character, ZoneType zone) {
        return null;
    }

    public void addExitZoneId(int... zoneIds) {
        int[] var2 = zoneIds;
        int var3 = zoneIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int zoneId = var2[var4];
            ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
            if (zone != null) {
                zone.addQuestEvent(ScriptEventType.ON_EXIT_ZONE, this);
            }
        }

    }

    public final void notifyExitZone(Creature character, ZoneType zone) {
        String res = null;

        try {
            res = this.onExitZone(character, zone);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        this.showResult(null, character, res);
    }

    public String onExitZone(Creature character, ZoneType zone) {
        return null;
    }

    public void addFactionCallId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_FACTION_CALL);
        }

    }

    public final void notifyFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
        String res = null;

        try {
            res = this.onFactionCall(npc, caller, attacker, isPet);
        } catch (Exception var7) {
            LOGGER.warn(this.toString(), var7);
            return;
        }

        this.showResult(npc, attacker, res);
    }

    public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
        return null;
    }

    public void addFirstTalkId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_FIRST_TALK);
        }

    }

    public final void notifyFirstTalk(Npc npc, Player player) {
        String res = null;

        try {
            res = this.onFirstTalk(npc, player);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        if (res != null && !res.isEmpty()) {
            this.showResult(npc, player, res);
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public String onFirstTalk(Npc npc, Player player) {
        return null;
    }

    public void addItemUse(int... itemIds) {
        int[] var2 = itemIds;
        int var3 = itemIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int itemId = var2[var4];
            Item t = ItemTable.getInstance().getTemplate(itemId);
            if (t != null) {
                t.addQuestEvent(this);
            }
        }

    }

    public final void notifyItemUse(ItemInstance item, Player player, WorldObject target) {
        String res = null;

        try {
            res = this.onItemUse(item, player, target);
        } catch (Exception var6) {
            LOGGER.warn(this.toString(), var6);
            return;
        }

        this.showResult(null, player, res);
    }

    public String onItemUse(ItemInstance item, Player player, WorldObject target) {
        return null;
    }

    public void addKillId(int... killIds) {
        int[] var2 = killIds;
        int var3 = killIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int killId = var2[var4];
            this.addEventId(killId, ScriptEventType.ON_KILL);
        }

    }

    public final void notifyKill(Npc npc, Creature killer) {
        String res = null;

        try {
            res = this.onKill(npc, killer);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        this.showResult(npc, killer, res);
    }

    public String onKill(Npc npc, Creature killer) {
        return null;
    }

    public void addSpawnId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_SPAWN);
        }

    }

    public final void notifySpawn(Npc npc) {
        try {
            this.onSpawn(npc);
        } catch (Exception var3) {
            LOGGER.error(this.toString(), var3);
        }

    }

    public String onSpawn(Npc npc) {
        return null;
    }

    public void addDecayId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_DECAY);
        }

    }

    public final void notifyDecay(Npc npc) {
        try {
            this.onDecay(npc);
        } catch (Exception var3) {
            LOGGER.error(this.toString(), var3);
        }

    }

    public void onDecay(Npc npc) {
    }

    public void addSkillSeeId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_SKILL_SEE);
        }

    }

    public final void notifySkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        ThreadPool.execute(new Quest.OnSkillSee(npc, caster, skill, targets, isPet));
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        return null;
    }

    public void addSpellFinishedId(int... npcIds) {
        int[] var2 = npcIds;
        int var3 = npcIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int npcId = var2[var4];
            this.addEventId(npcId, ScriptEventType.ON_SPELL_FINISHED);
        }

    }

    public final void notifySpellFinished(Npc npc, Player player, L2Skill skill) {
        String res = null;

        try {
            res = this.onSpellFinished(npc, player, skill);
        } catch (Exception var6) {
            LOGGER.warn(this.toString(), var6);
            return;
        }

        this.showResult(npc, player, res);
    }

    public String onSpellFinished(Npc npc, Player player, L2Skill skill) {
        return null;
    }

    public void addTalkId(int... talkIds) {
        int[] var2 = talkIds;
        int var3 = talkIds.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            int talkId = var2[var4];
            this.addEventId(talkId, ScriptEventType.ON_TALK);
        }

    }

    public final void notifyTalk(Npc npc, Player player) {
        String res = null;

        try {
            res = this.onTalk(npc, player);
        } catch (Exception var5) {
            LOGGER.warn(this.toString(), var5);
            return;
        }

        player.setLastQuestNpcObject(npc.getObjectId());
        this.showResult(npc, player, res);
    }

    public String onTalk(Npc npc, Player talker) {
        return null;
    }

    public final Siege addSiegeNotify(int castleId) {
        Siege siege = CastleManager.getInstance().getCastleById(castleId).getSiege();
        siege.addQuestEvent(this);
        return siege;
    }

    public void onSiegeEvent() {
    }

    public final void addGameTimeNotify() {
        GameTimeTaskManager.getInstance().addQuestEvent(this);
    }

    public void onGameTime() {
    }

    public boolean equals(Object o) {
        if (o instanceof L2AttackableAIScript && this instanceof L2AttackableAIScript) {
            return true;
        } else if (o instanceof Quest q) {
            return this._id > 0 && this._id == q._id ? this.getName().equals(q.getName()) : this.getClass().getName().equals(q.getClass().getName());
        } else {
            return false;
        }
    }

    private class OnAggroEnter implements Runnable {
        private final Npc _npc;
        private final Player _pc;
        private final boolean _isPet;

        public OnAggroEnter(Npc npc, Player pc, boolean isPet) {
            this._npc = npc;
            this._pc = pc;
            this._isPet = isPet;
        }

        public void run() {
            String res = null;

            try {
                res = Quest.this.onAggro(this._npc, this._pc, this._isPet);
            } catch (Exception var3) {
                Quest.LOGGER.warn(this.toString(), var3);
                return;
            }

            Quest.this.showResult(this._npc, this._pc, res);
        }
    }

    public class OnSkillSee implements Runnable {
        private final Npc _npc;
        private final Player _caster;
        private final L2Skill _skill;
        private final WorldObject[] _targets;
        private final boolean _isPet;

        public OnSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
            this._npc = npc;
            this._caster = caster;
            this._skill = skill;
            this._targets = targets;
            this._isPet = isPet;
        }

        public void run() {
            String res = null;

            try {
                res = Quest.this.onSkillSee(this._npc, this._caster, this._skill, this._targets, this._isPet);
            } catch (Exception var3) {
                Quest.LOGGER.warn(this.toString(), var3);
                return;
            }

            Quest.this.showResult(this._npc, this._caster, res);
        }
    }
}