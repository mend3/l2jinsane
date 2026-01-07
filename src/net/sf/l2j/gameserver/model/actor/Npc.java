package net.sf.l2j.gameserver.model.actor;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.DimensionalRiftManager;
import net.sf.l2j.gameserver.data.manager.LotteryManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.xml.*;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.events.eventengine.EventListener;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.NewbieBuff;
import net.sf.l2j.gameserver.model.Residence;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.stat.NpcStat;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.taskmanager.RandomAnimationTaskManager;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Npc extends Creature {
    public static final int INTERACTION_DISTANCE = 150;

    private static final int SOCIAL_INTERVAL = 12000;
    volatile boolean _isDecayed = false;
    private L2Spawn _spawn;
    private long _lastSocialBroadcast = 0L;

    private int _leftHandItemId;

    private int _rightHandItemId;

    private int _enchantEffect;

    private double _currentCollisionHeight;

    private double _currentCollisionRadius;

    private int _currentSsCount = 0;

    private int _currentSpsCount = 0;

    private int _shotsMask = 0;

    private int _scriptValue = 0;

    private Castle _castle;

    private FakePc _fakePc = null;
    private Residence _residence;

    public Npc(int objectId, NpcTemplate template) {
        super(objectId, template);
        this._fakePc = FakePcsTable.getInstance().getFakePc(template.getNpcId());
        for (L2Skill skill : template.getSkills(NpcTemplate.SkillType.PASSIVE))
            addStatFuncs(skill.getStatFuncs(this));
        initCharStatusUpdateValues();
        this._leftHandItemId = template.getLeftHand();
        this._rightHandItemId = template.getRightHand();
        this._enchantEffect = template.getEnchantEffect();
        this._currentCollisionHeight = template.getCollisionHeight();
        this._currentCollisionRadius = template.getCollisionRadius();
        setName(template.getName());
        setTitle(template.getTitle());
        this._castle = template.getCastle();
    }

    /**
     * @return The {@link Residence} this {@link Npc} belongs to.
     */
    public final Residence getResidence()
    {
        return _residence;
    }

    public final void setResidence(Residence residence)
    {
        _residence = residence;
    }

    public static void sendNpcDrop(Player player, int npcId, int page) {
        int ITEMS_PER_LIST = 7;
        NpcTemplate npc = NpcData.getInstance().getTemplate(npcId);
        if (npc == null)
            return;
        if (npc.getDropData().isEmpty()) {
            player.sendMessage("This target have not drop info.");
            return;
        }
        List<DropCategory> list = new ArrayList<>();
        npc.getDropData().forEach(c -> list.add(c));
        Collections.reverse(list);
        int myPage = 1;
        int i = 0;
        int shown = 0;
        boolean hasMore = false;
        StringBuilder sb = new StringBuilder();
        for (DropCategory cat : list) {
            if (shown == 7) {
                hasMore = true;
                break;
            }
            for (DropData drop : cat.getAllDrops()) {
                double chance = ((drop.getItemId() == 57) ? (drop.getChance() * Config.RATE_DROP_ADENA) : (drop.getChance() * Config.RATE_DROP_ITEMS)) / 10000.0D;
                chance = (chance > 100.0D) ? 100.0D : chance;
                String percent = null;
                if (chance <= 0.001D) {
                    DecimalFormat df = new DecimalFormat("#.####");
                    percent = df.format(chance);
                } else if (chance <= 0.01D) {
                    DecimalFormat df = new DecimalFormat("#.###");
                    percent = df.format(chance);
                } else {
                    DecimalFormat df = new DecimalFormat("##.##");
                    percent = df.format(chance);
                }
                Item item = ItemTable.getInstance().getTemplate(drop.getItemId());
                String name = item.getName();
                if (name.startsWith("Recipe: "))
                    name = "R: " + name.substring(8);
                if (name.length() >= 40)
                    name = name.substring(0, 37) + "...";
                if (myPage != page) {
                    i++;
                    if (i == 7) {
                        myPage++;
                        i = 0;
                    }
                    continue;
                }
                if (shown == 7) {
                    hasMore = true;
                    break;
                }
                String check = player.ignoredDropContain(item.getItemId()) ? "L2UI.CheckBox" : "L2UI.CheckBox_checked";
                sb.append("<table width=280 bgcolor=000000><tr>");
                sb.append("<td width=44 height=41 align=center><table bgcolor=" + (cat.isSweep() ? "FF00FF" : "FFFFFF") + " cellpadding=6 cellspacing=\"-5\"><tr><td><button width=32 height=32 back=" + IconsTable.getIcon(item.getItemId()) + " fore=" + IconsTable.getIcon(item.getItemId()) + "></td></tr></table></td>");
                sb.append("<td width=240>" + (cat.isSweep() ? ("<font color=ff00ff>" + name + "</font>") : name) + "<br1><font color=B09878>" + (cat.isSweep() ? "Spoil" : "Drop") + " Chance : " + percent + "%</font></td>");
                sb.append("<td width=20><button action=\"bypass droplist " + npcId + " " + page + " " + item.getItemId() + "\" width=12 height=12 back=\"" + check + "\" fore=\"" + check + "\"/></td>");
                sb.append("</tr></table><img src=L2UI.SquareGray width=280 height=1>");
                shown++;
            }
        }
        sb.append("<img height=" + (294 - shown * 42) + ">");
        sb.append("<img height=8><img src=L2UI.SquareGray width=280 height=1>");
        sb.append("<table width=280 bgcolor=000000><tr>");
        sb.append("<td align=center width=70>" + ((page > 1) ? ("<button value=\"< PREV\" action=\"bypass droplist " + npcId + " " + (page - 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>") : "") + "</td>");
        sb.append("<td align=center width=140>Page " + page + "</td>");
        sb.append("<td align=center width=70>" + (hasMore ? ("<button value=\"NEXT >\" action=\"bypass droplist " + npcId + " " + page + 1 + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>") : "") + "</td>");
        sb.append("</tr></table><img src=L2UI.SquareGray width=280 height=1>");
        NpcHtmlMessage html = new NpcHtmlMessage(200);
        html.setFile("data/html/droplist.htm");
        html.replace("%list%", sb.toString());
        html.replace("%name%", npc.getName());
        player.sendPacket(html);
    }

    public static void showQuestWindowGeneral(Player player, Npc npc) {
        List<Quest> quests = new ArrayList<>();
        List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.ON_TALK);
        if (scripts != null)
            for (Quest quest : scripts) {
                if (quest == null || !quest.isRealQuest() || quests.contains(quest))
                    continue;
                QuestState qs = player.getQuestState(quest.getName());
                if (qs == null || qs.isCreated())
                    continue;
                quests.add(quest);
            }
        scripts = npc.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
        if (scripts != null)
            for (Quest quest : scripts) {
                if (quest == null || !quest.isRealQuest() || quests.contains(quest))
                    continue;
                quests.add(quest);
            }
        if (quests.isEmpty()) {
            showQuestWindowSingle(player, npc, null);
        } else if (quests.size() == 1) {
            showQuestWindowSingle(player, npc, quests.get(0));
        } else {
            showQuestWindowChoose(player, npc, quests);
        }
    }

    public static void showQuestWindowSingle(Player player, Npc npc, Quest quest) {
        if (quest == null) {
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            html.setHtml(Quest.getNoQuestMsg());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (quest.isRealQuest() && (player.getWeightPenalty() > 2 || player.getInventoryLimit() * 0.8D <= player.getInventory().getSize())) {
            player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
            return;
        }
        QuestState qs = player.getQuestState(quest.getName());
        if (qs == null) {
            if (quest.isRealQuest() && player.getAllQuests(false).size() >= 25) {
                NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                html.setHtml(Quest.getTooMuchQuestsMsg());
                player.sendPacket(html);
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
            List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
            if (scripts != null && scripts.contains(quest))
                qs = quest.newQuestState(player);
        }
        if (qs != null)
            quest.notifyTalk(npc, qs.getPlayer());
    }

    public static void showQuestWindowChoose(Player player, Npc npc, List<Quest> quests) {
        StringBuilder sb = new StringBuilder("<html><body>");
        for (Quest q : quests) {
            StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Quest ", q.getName(), "\">[", q.getDescr());
            QuestState qs = player.getQuestState(q.getName());
            if (qs != null && qs.isStarted()) {
                sb.append(" (In Progress)]</a><br>");
                continue;
            }
            if (qs != null && qs.isCompleted()) {
                sb.append(" (Done)]</a><br>");
                continue;
            }
            sb.append("]</a><br>");
        }
        sb.append("</body></html>");
        NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setHtml(sb.toString());
        html.replace("%objectId%", npc.getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void initCharStat() {
        setStat(new NpcStat(this));
    }

    public NpcStat getStat() {
        return (NpcStat) super.getStat();
    }

    public void initCharStatus() {
        setStatus(new NpcStatus(this));
    }

    public NpcStatus getStatus() {
        return (NpcStatus) super.getStatus();
    }

    public final NpcTemplate getTemplate() {
        return (NpcTemplate) super.getTemplate();
    }

    public boolean isAttackable() {
        return true;
    }

    public final int getLevel() {
        return getTemplate().getLevel();
    }

    public boolean isUndead() {
        return (getTemplate().getRace() == NpcTemplate.Race.UNDEAD);
    }

    public void updateAbnormalEffect() {
        for (Player player : getKnownType(Player.class)) {
            if (getMoveSpeed() == 0) {
                player.sendPacket(new ServerObjectInfo(this, player));
                continue;
            }
            player.sendPacket(new AbstractNpcInfo.NpcInfo(this, player));
        }
    }

    public final void setTitle(String value) {
        this._title = (value == null) ? "" : value;
    }

    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else if (!canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat())
                player.getAI().setIntention(IntentionType.IDLE);
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (hasRandomAnimation())
                onRandomAnimation(Rnd.get(8));
            if (EngineModsManager.onInteract(player, this))
                return;
            List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.QUEST_START);
            if (scripts != null && !scripts.isEmpty())
                player.setLastQuestNpcObject(getObjectId());
            scripts = getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
            if (scripts != null && scripts.size() == 1) {
                scripts.get(0).notifyFirstTalk(this, player);
            } else if ((TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isEventNpc(this)) || (
                    CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isEventNpc(this)) || (
                    DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isEventNpc(this))) {
                EventListener.onInterract(player, this);
            } else {
                showChatWindow(player);
            }
        }
    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            sendNpcInfos(player);
        } else if (this instanceof Monster || this instanceof net.sf.l2j.gameserver.model.actor.instance.RaidBoss || this instanceof net.sf.l2j.gameserver.model.actor.instance.GrandBoss || this instanceof net.sf.l2j.gameserver.model.actor.instance.Chest) {
            sendNpcDrop(player, getTemplate().getNpcId(), 1);
        }
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player)) {
            if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.getAI().setIntention(IntentionType.ATTACK, this);
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
        } else if (canInteract(player)) {
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (hasRandomAnimation())
                onRandomAnimation(Rnd.get(8));
            List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.QUEST_START);
            if (scripts != null && !scripts.isEmpty())
                player.setLastQuestNpcObject(getObjectId());
            scripts = getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
            if (scripts != null && scripts.size() == 1) {
                scripts.get(0).notifyFirstTalk(this, player);
            } else {
                showChatWindow(player);
            }
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    protected final void notifyQuestEventSkillFinished(L2Skill skill, WorldObject target) {
        List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_SPELL_FINISHED);
        if (scripts != null) {
            Player player = (target == null) ? null : target.getActingPlayer();
            for (Quest quest : scripts)
                quest.notifySpellFinished(this, player, skill);
        }
    }

    public boolean isMovementDisabled() {
        return (super.isMovementDisabled() || !getTemplate().canMove() || getTemplate().getAiType().equals(NpcTemplate.AIType.CORPSE));
    }

    public boolean isCoreAIDisabled() {
        return (super.isCoreAIDisabled() || getTemplate().getAiType().equals(NpcTemplate.AIType.CORPSE));
    }

    public void sendInfo(Player activeChar) {
        if (getMoveSpeed() == 0) {
            activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
        } else {
            activeChar.sendPacket(new AbstractNpcInfo.NpcInfo(this, activeChar));
        }
    }

    public boolean isChargedShot(ShotType type) {
        return ((this._shotsMask & type.getMask()) == type.getMask());
    }

    public void setChargedShot(ShotType type, boolean charged) {
        if (charged) {
            this._shotsMask |= type.getMask();
        } else {
            this._shotsMask &= type.getMask() ^ 0xFFFFFFFF;
        }
    }

    public void rechargeShots(boolean physical, boolean magic) {
        if (physical) {
            if (this._currentSsCount <= 0)
                return;
            if (Rnd.get(100) > getTemplate().getSsRate())
                return;
            this._currentSsCount--;
            broadcastPacketInRadius(new MagicSkillUse(this, this, 2154, 1, 0, 0), 600);
            setChargedShot(ShotType.SOULSHOT, true);
        }
        if (magic) {
            if (this._currentSpsCount <= 0)
                return;
            if (Rnd.get(100) > getTemplate().getSpsRate())
                return;
            this._currentSpsCount--;
            broadcastPacketInRadius(new MagicSkillUse(this, this, 2061, 1, 0, 0), 600);
            setChargedShot(ShotType.SPIRITSHOT, true);
        }
    }

    public int getSkillLevel(int skillId) {
        for (List<L2Skill> list : getTemplate().getSkills().values()) {
            for (L2Skill skill : list) {
                if (skill.getId() == skillId)
                    return skill.getLevel();
            }
        }
        return 0;
    }

    public L2Skill getSkill(int skillId) {
        for (List<L2Skill> list : getTemplate().getSkills().values()) {
            for (L2Skill skill : list) {
                if (skill.getId() == skillId)
                    return skill;
            }
        }
        return null;
    }

    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    public Weapon getActiveWeaponItem() {
        int weaponId = getTemplate().getRightHand();
        if (weaponId <= 0)
            return null;
        Item item = ItemTable.getInstance().getTemplate(weaponId);
        if (!(item instanceof Weapon))
            return null;
        return (Weapon) item;
    }

    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    public Item getSecondaryWeaponItem() {
        int itemId = getTemplate().getLeftHand();
        if (itemId <= 0)
            return null;
        return ItemTable.getInstance().getTemplate(itemId);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer))
            return false;
        this._leftHandItemId = getTemplate().getLeftHand();
        this._rightHandItemId = getTemplate().getRightHand();
        this._enchantEffect = getTemplate().getEnchantEffect();
        this._currentCollisionHeight = getTemplate().getCollisionHeight();
        this._currentCollisionRadius = getTemplate().getCollisionRadius();
        DecayTaskManager.getInstance().add(this, getTemplate().getCorpseTime());
        return true;
    }

    public void onSpawn() {
        super.onSpawn();
        this._currentSsCount = getTemplate().getSsCount();
        this._currentSpsCount = getTemplate().getSpsCount();
        List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_SPAWN);
        if (scripts != null)
            for (Quest quest : scripts)
                quest.notifySpawn(this);
    }

    public void onDecay() {
        if (isDecayed())
            return;
        setDecayed(true);
        List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_DECAY);
        if (scripts != null)
            for (Quest quest : scripts)
                quest.notifyDecay(this);
        super.onDecay();
        if (this._spawn != null)
            this._spawn.doRespawn();
    }

    public void deleteMe() {
        onDecay();
        super.deleteMe();
    }

    public double getCollisionHeight() {
        return this._currentCollisionHeight;
    }

    public void setCollisionHeight(double height) {
        this._currentCollisionHeight = height;
    }

    public double getCollisionRadius() {
        return this._currentCollisionRadius;
    }

    public void setCollisionRadius(double radius) {
        this._currentCollisionRadius = radius;
    }

    public String toString() {
        return getName() + " - " + getName() + " (" + getNpcId() + ")";
    }

    public L2Spawn getSpawn() {
        return this._spawn;
    }

    public void setSpawn(L2Spawn spawn) {
        this._spawn = spawn;
    }

    public Npc scheduleDespawn(long delay) {
        ThreadPool.schedule(() -> {
            if (!isDecayed())
                deleteMe();
        }, delay);
        return this;
    }

    public boolean isDecayed() {
        return this._isDecayed;
    }

    public void setDecayed(boolean decayed) {
        this._isDecayed = decayed;
    }

    public void endDecayTask() {
        if (!isDecayed()) {
            DecayTaskManager.getInstance().cancel(this);
            onDecay();
        }
    }

    public void onRandomAnimation(int id) {
        long now = System.currentTimeMillis();
        if (now - this._lastSocialBroadcast > 12000L) {
            this._lastSocialBroadcast = now;
            broadcastPacket(new SocialAction(this, id));
        }
    }

    public void startRandomAnimationTimer() {
        if (!hasRandomAnimation())
            return;
        RandomAnimationTaskManager.getInstance().add(this, calculateRandomAnimationTimer());
    }

    public int calculateRandomAnimationTimer() {
        return Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION);
    }

    public boolean hasRandomAnimation() {
        return (Config.MAX_NPC_ANIMATION > 0 && !getTemplate().getAiType().equals(NpcTemplate.AIType.CORPSE));
    }

    public int getNpcId() {
        return getTemplate().getNpcId();
    }

    public boolean isAggressive() {
        return false;
    }

    public int getLeftHandItemId() {
        return this._leftHandItemId;
    }

    public void setLeftHandItemId(int itemId) {
        this._leftHandItemId = itemId;
    }

    public int getRightHandItemId() {
        return this._rightHandItemId;
    }

    public void setRightHandItemId(int id) {
        this._rightHandItemId = id;
    }

    public int getEnchantEffect() {
        return this._enchantEffect;
    }

    public void setEnchantEffect(int enchant) {
        this._enchantEffect = enchant;
    }

    public int getScriptValue() {
        return this._scriptValue;
    }

    public void setScriptValue(int val) {
        this._scriptValue = val;
    }

    public boolean isScriptValue(int val) {
        return (this._scriptValue == val);
    }

    public boolean isWarehouse() {
        return false;
    }

    public final Castle getCastle() {
        return this._castle;
    }

    public void setCastle(Castle castle) {
        this._castle = castle;
    }

    public int getExpReward() {
        return (int) (getTemplate().getRewardExp() * Config.RATE_XP);
    }

    public int getSpReward() {
        return (int) (getTemplate().getRewardSp() * Config.RATE_SP);
    }

    protected void sendNpcInfos(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/admin/npcinfo.htm");
        html.replace("%class%", getClass().getSimpleName());
        html.replace("%id%", getTemplate().getNpcId());
        html.replace("%lvl%", getTemplate().getLevel());
        html.replace("%name%", getName());
        html.replace("%race%", getTemplate().getRace().toString());
        html.replace("%tmplid%", getTemplate().getIdTemplate());
        html.replace("%script%", getScriptValue());
        html.replace("%castle%", (getCastle() != null) ? getCastle().getName() : "none");
        html.replace("%aggro%", getTemplate().getAggroRange());
        html.replace("%corpse%", StringUtil.getTimeStamp(getTemplate().getCorpseTime()));
        html.replace("%enchant%", getTemplate().getEnchantEffect());
        html.replace("%hp%", (int) getCurrentHp());
        html.replace("%hpmax%", getMaxHp());
        html.replace("%mp%", (int) getCurrentMp());
        html.replace("%mpmax%", getMaxMp());
        html.replace("%patk%", getPAtk(null));
        html.replace("%matk%", getMAtk(null, null));
        html.replace("%pdef%", getPDef(null));
        html.replace("%mdef%", getMDef(null, null));
        html.replace("%accu%", getAccuracy());
        html.replace("%evas%", getEvasionRate(null));
        html.replace("%crit%", getCriticalHit(null, null));
        html.replace("%rspd%", getMoveSpeed());
        html.replace("%aspd%", getPAtkSpd());
        html.replace("%cspd%", getMAtkSpd());
        html.replace("%str%", getSTR());
        html.replace("%dex%", getDEX());
        html.replace("%con%", getCON());
        html.replace("%int%", getINT());
        html.replace("%wit%", getWIT());
        html.replace("%men%", getMEN());
        html.replace("%loc%", getX() + " " + getX() + " " + getY());
        html.replace("%dist%", (int) Math.sqrt(player.getDistanceSq(this)));
        html.replace("%ele_fire%", getDefenseElementValue((byte) 2));
        html.replace("%ele_water%", getDefenseElementValue((byte) 3));
        html.replace("%ele_wind%", getDefenseElementValue((byte) 1));
        html.replace("%ele_earth%", getDefenseElementValue((byte) 4));
        html.replace("%ele_holy%", getDefenseElementValue((byte) 5));
        html.replace("%ele_dark%", getDefenseElementValue((byte) 6));
        if (getSpawn() != null) {
            html.replace("%spawn%", getSpawn().getLoc().toString());
            html.replace("%loc2d%", (int) Math.sqrt(getPlanDistanceSq(getSpawn().getLocX(), getSpawn().getLocY())));
            html.replace("%loc3d%", (int) Math.sqrt(getDistanceSq(getSpawn().getLocX(), getSpawn().getLocY(), getSpawn().getLocZ())));
            html.replace("%resp%", StringUtil.getTimeStamp(getSpawn().getRespawnDelay()));
            html.replace("%rand_resp%", StringUtil.getTimeStamp(getSpawn().getRespawnRandom()));
        } else {
            html.replace("%spawn%", "<font color=FF0000>null</font>");
            html.replace("%loc2d%", "<font color=FF0000>--</font>");
            html.replace("%loc3d%", "<font color=FF0000>--</font>");
            html.replace("%resp%", "<font color=FF0000>--</font>");
            html.replace("%rand_resp%", "<font color=FF0000>--</font>");
        }
        if (hasAI()) {
            html.replace("%ai_intention%", "<font color=\"LEVEL\">Intention</font><table width=\"100%\"><tr><td><font color=\"LEVEL\">Intention:</font></td><td>" + getAI().getDesire().getIntention().name() + "</td></tr>");
            html.replace("%ai%", "<tr><td><font color=\"LEVEL\">AI:</font></td><td>" + getAI().getClass().getSimpleName() + "</td></tr></table><br>");
        } else {
            html.replace("%ai_intention%", "");
            html.replace("%ai%", "");
        }
        html.replace("%ai_type%", getTemplate().getAiType().name());
        html.replace("%ai_clan%", (getTemplate().getClans() != null) ? ("<tr><td width=100><font color=\"LEVEL\">Clan:</font></td><td align=right width=170>" + Arrays.toString(getTemplate().getClans()) + " " + getTemplate().getClanRange() + "</td></tr>" + ((getTemplate().getIgnoredIds() != null) ? ("<tr><td width=100><font color=\"LEVEL\">Ignored ids:</font></td><td align=right width=170>" + Arrays.toString(getTemplate().getIgnoredIds()) + "</td></tr>") : "")) : "");
        html.replace("%ai_move%", String.valueOf(getTemplate().canMove()));
        html.replace("%ai_seed%", String.valueOf(getTemplate().isSeedable()));
        html.replace("%ai_ssinfo%", this._currentSsCount + "[" + this._currentSsCount + "] - " + getTemplate().getSsCount() + "%");
        html.replace("%ai_spsinfo%", this._currentSpsCount + "[" + this._currentSpsCount + "] - " + getTemplate().getSpsCount() + "%");
        html.replace("%shop%", (this instanceof net.sf.l2j.gameserver.model.actor.instance.Merchant) ? ("<button value=\"Shop\" action=\"bypass -h admin_show_shop " + getNpcId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">") : "");
        html.replace("%minion%", (this instanceof Monster && (((Monster) this).getMaster() != null || ((Monster) this).hasMinions())) ? "<button value=\"Minions\" action=\"bypass -h admin_show_minion\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
        player.sendPacket(html);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.equalsIgnoreCase("TerritoryStatus")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (getCastle().getOwnerId() > 0) {
                html.setFile("data/html/territorystatus.htm");
                Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
                html.replace("%clanname%", clan.getName());
                html.replace("%clanleadername%", clan.getLeaderName());
            } else {
                html.setFile("data/html/territorynoclan.htm");
            }
            html.replace("%castlename%", getCastle().getName());
            html.replace("%taxpercent%", getCastle().getTaxPercent());
            html.replace("%objectId%", getObjectId());
            if (getCastle().getCastleId() > 6) {
                html.replace("%territory%", "The Kingdom of Elmore");
            } else {
                html.replace("%territory%", "The Kingdom of Aden");
            }
            player.sendPacket(html);
        } else if (command.startsWith("Quest")) {
            String quest = "";
            try {
                quest = command.substring(5).trim();
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            }
            if (quest.isEmpty()) {
                showQuestWindowGeneral(player, this);
            } else {
                showQuestWindowSingle(player, this, ScriptData.getInstance().getQuest(quest));
            }
        } else if (command.startsWith("Chat")) {
            int val = 0;
            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {

            } catch (NumberFormatException numberFormatException) {
            }
            showChatWindow(player, val);
        } else if (command.startsWith("Link")) {
            String path = command.substring(5).trim();
            if (path.indexOf("..") != -1)
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/" + path);
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (command.startsWith("Loto")) {
            int val = 0;
            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {

            } catch (NumberFormatException numberFormatException) {
            }
            if (val == 0)
                for (int i = 0; i < 5; i++)
                    player.setLoto(i, 0);
            showLotoWindow(player, val);
        } else if (command.startsWith("CPRecovery")) {
            if (getNpcId() != 31225 && getNpcId() != 31226)
                return;
            if (player.isCursedWeaponEquipped()) {
                player.sendMessage("Go away, you're not welcome here.");
                return;
            }
            if (player.reduceAdena("RestoreCP", 100, player.getCurrentFolk(), true)) {
                setTarget(player);
                doCast(SkillTable.FrequentSkill.ARENA_CP_RECOVERY.getSkill());
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addCharName(player));
            }
        } else if (command.startsWith("SupportMagic")) {
            if (player.isCursedWeaponEquipped())
                return;
            int playerLevel = player.getLevel();
            int lowestLevel = 0;
            int higestLevel = 0;
            setTarget(player);
            if (player.isMageClass()) {
                lowestLevel = NewbieBuffData.getInstance().getMagicLowestLevel();
                higestLevel = NewbieBuffData.getInstance().getMagicHighestLevel();
            } else {
                lowestLevel = NewbieBuffData.getInstance().getPhysicLowestLevel();
                higestLevel = NewbieBuffData.getInstance().getPhysicHighestLevel();
            }
            if (playerLevel > higestLevel || !player.isNewbie()) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setHtml("<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + higestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>");
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
                return;
            }
            if (playerLevel < lowestLevel) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setHtml("<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>");
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
                return;
            }
            for (NewbieBuff buff : NewbieBuffData.getInstance().getBuffs()) {
                if (buff.isMagicClassBuff() == player.isMageClass() && playerLevel >= buff.getLowerLevel() && playerLevel <= buff.getUpperLevel()) {
                    L2Skill skill = SkillTable.getInstance().getInfo(buff.getSkillId(), buff.getSkillLevel());
                    if (skill.getSkillType() == L2SkillType.SUMMON) {
                        player.doCast(skill);
                        continue;
                    }
                    doCast(skill);
                }
            }
        } else if (command.startsWith("multisell")) {
            MultisellData.getInstance().separateAndSend(command.substring(9).trim(), player, this, false);
        } else if (command.startsWith("exc_multisell")) {
            MultisellData.getInstance().separateAndSend(command.substring(13).trim(), player, this, true);
        } else if (command.startsWith("Augment")) {
            int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
            switch (cmdChoice) {
                case 1:
                    player.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
                    player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
                    break;
                case 2:
                    player.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
                    player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
                    break;
            }
        } else if (command.startsWith("EnterRift")) {
            try {
                Byte b1 = Byte.valueOf(Byte.parseByte(command.substring(10)));
                DimensionalRiftManager.getInstance().start(player, b1, this);
            } catch (Exception exception) {
            }
        }
    }

    public String getHtmlPath(int npcId, int val) {
        String filename;
        if (val == 0) {
            filename = "data/html/default/" + npcId + ".htm";
        } else {
            filename = "data/html/default/" + npcId + "-" + val + ".htm";
        }
        if (HtmCache.getInstance().isLoadable(filename))
            return filename;
        return "data/html/npcdefault.htm";
    }

    public void broadcastNpcSay(String message) {
        broadcastPacket(new NpcSay(getObjectId(), 0, getNpcId(), message));
    }

    public void showLotoWindow(Player player, int val) {
        int npcId = getTemplate().getNpcId();
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        if (val == 0) {
            html.setFile(getHtmlPath(npcId, 1));
        } else if (val >= 1 && val <= 21) {
            if (!LotteryManager.getInstance().isStarted()) {
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
                return;
            }
            if (!LotteryManager.getInstance().isSellableTickets()) {
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
                return;
            }
            html.setFile(getHtmlPath(npcId, 5));
            int count = 0;
            int found = 0;
            int i;
            for (i = 0; i < 5; i++) {
                if (player.getLoto(i) == val) {
                    player.setLoto(i, 0);
                    found = 1;
                } else if (player.getLoto(i) > 0) {
                    count++;
                }
            }
            if (count < 5 && found == 0 && val <= 20)
                for (i = 0; i < 5; i++) {
                    if (player.getLoto(i) == 0) {
                        player.setLoto(i, val);
                        break;
                    }
                }
            count = 0;
            for (i = 0; i < 5; i++) {
                if (player.getLoto(i) > 0) {
                    count++;
                    String button = String.valueOf(player.getLoto(i));
                    if (player.getLoto(i) < 10)
                        button = "0" + button;
                    String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
                    String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
                    html.replace(search, replace);
                }
            }
            if (count == 5) {
                String search = "0\">Return";
                String replace = "22\">The winner selected the numbers above.";
                html.replace(search, replace);
            }
        } else if (val == 22) {
            if (!LotteryManager.getInstance().isStarted()) {
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
                return;
            }
            if (!LotteryManager.getInstance().isSellableTickets()) {
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
                return;
            }
            int price = Config.ALT_LOTTERY_TICKET_PRICE;
            int lotonumber = LotteryManager.getInstance().getId();
            int enchant = 0;
            int type2 = 0;
            for (int i = 0; i < 5; i++) {
                if (player.getLoto(i) == 0)
                    return;
                if (player.getLoto(i) < 17) {
                    enchant = (int) (enchant + Math.pow(2.0D, (player.getLoto(i) - 1)));
                } else {
                    type2 = (int) (type2 + Math.pow(2.0D, (player.getLoto(i) - 17)));
                }
            }
            if (!player.reduceAdena("Loto", price, this, true))
                return;
            LotteryManager.getInstance().increasePrize(price);
            ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4442);
            item.setCount(1);
            item.setCustomType1(lotonumber);
            item.setEnchantLevel(enchant);
            item.setCustomType2(type2);
            player.addItem("Loto", item, player, false);
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(4442));
            html.setFile(getHtmlPath(npcId, 3));
        } else if (val == 23) {
            html.setFile(getHtmlPath(npcId, 3));
        } else if (val == 24) {
            int lotoNumber = LotteryManager.getInstance().getId();
            StringBuilder sb = new StringBuilder();
            for (ItemInstance item : player.getInventory().getItems()) {
                if (item == null)
                    continue;
                if (item.getItemId() == 4442 && item.getCustomType1() < lotoNumber) {
                    StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Loto ", Integer.valueOf(item.getObjectId()), "\">", Integer.valueOf(item.getCustomType1()), " Event Number ");
                    int[] numbers = LotteryManager.decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
                    for (int i = 0; i < 5; i++) {
                        StringUtil.append(sb, Integer.valueOf(numbers[i]), " ");
                    }
                    int[] check = LotteryManager.checkTicket(item);
                    if (check[0] > 0) {
                        switch (check[0]) {
                            case 1:
                                sb.append("- 1st Prize");
                                break;
                            case 2:
                                sb.append("- 2nd Prize");
                                break;
                            case 3:
                                sb.append("- 3th Prize");
                                break;
                            case 4:
                                sb.append("- 4th Prize");
                                break;
                        }
                        StringUtil.append(sb, " ", Integer.valueOf(check[1]), "a.");
                    }
                    sb.append("</a><br>");
                }
            }
            if (sb.length() == 0)
                sb.append("There is no winning lottery ticket...<br>");
            html.setFile(getHtmlPath(npcId, 4));
            html.replace("%result%", sb.toString());
        } else if (val == 25) {
            html.setFile(getHtmlPath(npcId, 2));
            html.replace("%prize5%", Config.ALT_LOTTERY_5_NUMBER_RATE * 100.0D);
            html.replace("%prize4%", Config.ALT_LOTTERY_4_NUMBER_RATE * 100.0D);
            html.replace("%prize3%", Config.ALT_LOTTERY_3_NUMBER_RATE * 100.0D);
            html.replace("%prize2%", Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
        } else if (val > 25) {
            ItemInstance item = player.getInventory().getItemByObjectId(val);
            if (item == null || item.getItemId() != 4442 || item.getCustomType1() >= LotteryManager.getInstance().getId())
                return;
            if (player.destroyItem("Loto", item, this, true)) {
                int adena = LotteryManager.checkTicket(item)[1];
                if (adena > 0)
                    player.addAdena("Loto", adena, this, true);
            }
            return;
        }
        html.replace("%objectId%", getObjectId());
        html.replace("%race%", LotteryManager.getInstance().getId());
        html.replace("%adena%", LotteryManager.getInstance().getPrize());
        html.replace("%ticket_price%", Config.ALT_LOTTERY_TICKET_PRICE);
        html.replace("%enddate%", DateFormat.getDateInstance().format(Long.valueOf(LotteryManager.getInstance().getEndDate())));
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void doCast(NpcTemplate.SkillType type) {
        doCast(Rnd.get(getTemplate().getSkills(type)));
    }

    protected boolean showPkDenyChatWindow(Player player, String type) {
        String content = HtmCache.getInstance().getHtm("data/html/" + type + "/" + getNpcId() + "-pk.htm");
        if (content != null) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setHtml(content);
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return true;
        }
        return false;
    }

    public void showChatWindow(Player player) {
        showChatWindow(player, 0);
    }

    public void showChatWindow(Player player, int val) {
        showChatWindow(player, getHtmlPath(getNpcId(), val));
    }

    public final void showChatWindow(Player player, String filename) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public FakePc getFakePc() {
        return this._fakePc;
    }

    public double getMovementSpeedMultiplier() {
        return 1.0D;
    }

    public double getAttackSpeedMultiplier() {
        return 1.0D;
    }
}
