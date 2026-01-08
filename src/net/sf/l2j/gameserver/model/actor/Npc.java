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
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Merchant;
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

    public Npc(int objectId, NpcTemplate template) {
        super(objectId, template);
        this._fakePc = FakePcsTable.getInstance().getFakePc(template.getNpcId());

        for (L2Skill skill : template.getSkills(NpcTemplate.SkillType.PASSIVE)) {
            this.addStatFuncs(skill.getStatFuncs(this));
        }

        this.initCharStatusUpdateValues();
        this._leftHandItemId = template.getLeftHand();
        this._rightHandItemId = template.getRightHand();
        this._enchantEffect = template.getEnchantEffect();
        this._currentCollisionHeight = template.getCollisionHeight();
        this._currentCollisionRadius = template.getCollisionRadius();
        this.setName(template.getName());
        this.setTitle(template.getTitle());
        this._castle = template.getCastle();
    }

    public static void sendNpcDrop(Player player, int npcId, int page) {
        int ITEMS_PER_LIST = 7;
        NpcTemplate npc = NpcData.getInstance().getTemplate(npcId);
        if (npc != null) {
            if (npc.getDropData().isEmpty()) {
                player.sendMessage("This target have not drop info.");
            } else {
                List<DropCategory> list = new ArrayList<>(npc.getDropData());
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
                        double chance = (drop.getItemId() == 57 ? (double) drop.getChance() * Config.RATE_DROP_ADENA : (double) drop.getChance() * Config.RATE_DROP_ITEMS) / (double) 10000.0F;
                        chance = Math.min(chance, 100.0F);
                        String percent = null;
                        if (chance <= 0.001) {
                            DecimalFormat df = new DecimalFormat("#.####");
                            percent = df.format(chance);
                        } else if (chance <= 0.01) {
                            DecimalFormat df = new DecimalFormat("#.###");
                            percent = df.format(chance);
                        } else {
                            DecimalFormat df = new DecimalFormat("##.##");
                            percent = df.format(chance);
                        }

                        Item item = ItemTable.getInstance().getTemplate(drop.getItemId());
                        String name = item.getName();
                        if (name.startsWith("Recipe: ")) {
                            name = "R: " + name.substring(8);
                        }

                        if (name.length() >= 40) {
                            name = name.substring(0, 37) + "...";
                        }

                        if (myPage != page) {
                            ++i;
                            if (i == 7) {
                                ++myPage;
                                i = 0;
                            }
                        } else {
                            if (shown == 7) {
                                hasMore = true;
                                break;
                            }

                            String check = player.ignoredDropContain(item.getItemId()) ? "L2UI.CheckBox" : "L2UI.CheckBox_checked";
                            sb.append("<table width=280 bgcolor=000000><tr>");
                            String var10001 = cat.isSweep() ? "FF00FF" : "FFFFFF";
                            sb.append("<td width=44 height=41 align=center><table bgcolor=").append(var10001).append(" cellpadding=6 cellspacing=\"-5\"><tr><td><button width=32 height=32 back=").append(IconsTable.getIcon(item.getItemId())).append(" fore=").append(IconsTable.getIcon(item.getItemId())).append("></td></tr></table></td>");
                            var10001 = cat.isSweep() ? "<font color=ff00ff>" + name + "</font>" : name;
                            sb.append("<td width=240>").append(var10001).append("<br1><font color=B09878>").append(cat.isSweep() ? "Spoil" : "Drop").append(" Chance : ").append(percent).append("%</font></td>");
                            sb.append("<td width=20><button action=\"bypass droplist ").append(npcId).append(" ").append(page).append(" ").append(item.getItemId()).append("\" width=12 height=12 back=\"").append(check).append("\" fore=\"").append(check).append("\"/></td>");
                            sb.append("</tr></table><img src=L2UI.SquareGray width=280 height=1>");
                            ++shown;
                        }
                    }
                }

                sb.append("<img height=").append(294 - shown * 42).append(">");
                sb.append("<img height=8><img src=L2UI.SquareGray width=280 height=1>");
                sb.append("<table width=280 bgcolor=000000><tr>");
                String var28 = page > 1 ? "<button value=\"< PREV\" action=\"bypass droplist " + npcId + " " + (page - 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "";
                sb.append("<td align=center width=70>").append(var28).append("</td>");
                sb.append("<td align=center width=140>Page ").append(page).append("</td>");
                var28 = hasMore ? "<button value=\"NEXT >\" action=\"bypass droplist " + npcId + " " + (page + 1) + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "";
                sb.append("<td align=center width=70>").append(var28).append("</td>");
                sb.append("</tr></table><img src=L2UI.SquareGray width=280 height=1>");
                NpcHtmlMessage html = new NpcHtmlMessage(200);
                html.setFile("data/html/droplist.htm");
                html.replace("%list%", sb.toString());
                html.replace("%name%", npc.getName());
                player.sendPacket(html);
            }
        }
    }

    public static void showQuestWindowGeneral(Player player, Npc npc) {
        List<Quest> quests = new ArrayList<>();
        List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.ON_TALK);
        if (scripts != null) {
            for (Quest quest : scripts) {
                if (quest != null && quest.isRealQuest() && !quests.contains(quest)) {
                    QuestState qs = player.getQuestState(quest.getName());
                    if (qs != null && !qs.isCreated()) {
                        quests.add(quest);
                    }
                }
            }
        }

        scripts = npc.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
        if (scripts != null) {
            for (Quest quest : scripts) {
                if (quest != null && quest.isRealQuest() && !quests.contains(quest)) {
                    quests.add(quest);
                }
            }
        }

        if (quests.isEmpty()) {
            showQuestWindowSingle(player, npc, null);
        } else if (quests.size() == 1) {
            showQuestWindowSingle(player, npc, quests.getFirst());
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
        } else if (!quest.isRealQuest() || player.getWeightPenalty() <= 2 && !((double) player.getInventoryLimit() * 0.8 <= (double) player.getInventory().getSize())) {
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
                if (scripts != null && scripts.contains(quest)) {
                    qs = quest.newQuestState(player);
                }
            }

            if (qs != null) {
                quest.notifyTalk(npc, qs.getPlayer());
            }

        } else {
            player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
        }
    }

    public static void showQuestWindowChoose(Player player, Npc npc, List<Quest> quests) {
        StringBuilder sb = new StringBuilder("<html><body>");

        for (Quest q : quests) {
            StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Quest ", q.getName(), "\">[", q.getDescr());
            QuestState qs = player.getQuestState(q.getName());
            if (qs != null && qs.isStarted()) {
                sb.append(" (In Progress)]</a><br>");
            } else if (qs != null && qs.isCompleted()) {
                sb.append(" (Done)]</a><br>");
            } else {
                sb.append("]</a><br>");
            }
        }

        sb.append("</body></html>");
        NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setHtml(sb.toString());
        html.replace("%objectId%", npc.getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void initCharStat() {
        this.setStat(new NpcStat(this));
    }

    public NpcStat getStat() {
        return (NpcStat) super.getStat();
    }

    public void initCharStatus() {
        this.setStatus(new NpcStatus(this));
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
        return this.getTemplate().getLevel();
    }

    public boolean isUndead() {
        return this.getTemplate().getRace() == NpcTemplate.Race.UNDEAD;
    }

    public void updateAbnormalEffect() {
        for (Player player : this.getKnownType(Player.class)) {
            if (this.getMoveSpeed() == 0) {
                player.sendPacket(new ServerObjectInfo(this, player));
            } else {
                player.sendPacket(new AbstractNpcInfo.NpcInfo(this, player));
            }
        }

    }

    public final void setTitle(String value) {
        this._title = value == null ? "" : value;
    }

    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (this.isAutoAttackable(player)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else if (!this.canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat()) {
                player.getAI().setIntention(IntentionType.IDLE);
            }

            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (this.hasRandomAnimation()) {
                this.onRandomAnimation(Rnd.get(8));
            }

            if (EngineModsManager.onInteract(player, this)) {
                return;
            }

            List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
            if (scripts != null && !scripts.isEmpty()) {
                player.setLastQuestNpcObject(this.getObjectId());
            }

            scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
            if (scripts != null && scripts.size() == 1) {
                scripts.getFirst().notifyFirstTalk(this, player);
            } else if ((TvTEventManager.getInstance().getActiveEvent() == null || !TvTEventManager.getInstance().getActiveEvent().isEventNpc(this)) && (CtfEventManager.getInstance().getActiveEvent() == null || !CtfEventManager.getInstance().getActiveEvent().isEventNpc(this)) && (DmEventManager.getInstance().getActiveEvent() == null || !DmEventManager.getInstance().getActiveEvent().isEventNpc(this))) {
                this.showChatWindow(player);
            } else {
                EventListener.onInterract(player, this);
            }
        }

    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            this.sendNpcInfos(player);
        } else if (this instanceof Monster) {
            sendNpcDrop(player, this.getTemplate().getNpcId(), 1);
        }

        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (this.isAutoAttackable(player)) {
            if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.getAI().setIntention(IntentionType.ATTACK, this);
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
        } else if (this.canInteract(player)) {
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (this.hasRandomAnimation()) {
                this.onRandomAnimation(Rnd.get(8));
            }

            List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
            if (scripts != null && !scripts.isEmpty()) {
                player.setLastQuestNpcObject(this.getObjectId());
            }

            scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
            if (scripts != null && scripts.size() == 1) {
                scripts.getFirst().notifyFirstTalk(this, player);
            } else {
                this.showChatWindow(player);
            }
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    protected final void notifyQuestEventSkillFinished(L2Skill skill, WorldObject target) {
        List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_SPELL_FINISHED);
        if (scripts != null) {
            Player player = target == null ? null : target.getActingPlayer();

            for (Quest quest : scripts) {
                quest.notifySpellFinished(this, player, skill);
            }
        }

    }

    public boolean isMovementDisabled() {
        return super.isMovementDisabled() || !this.getTemplate().canMove() || this.getTemplate().getAiType().equals(NpcTemplate.AIType.CORPSE);
    }

    public boolean isCoreAIDisabled() {
        return super.isCoreAIDisabled() || this.getTemplate().getAiType().equals(NpcTemplate.AIType.CORPSE);
    }

    public void sendInfo(Player activeChar) {
        if (this.getMoveSpeed() == 0) {
            activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
        } else {
            activeChar.sendPacket(new AbstractNpcInfo.NpcInfo(this, activeChar));
        }

    }

    public boolean isChargedShot(ShotType type) {
        return (this._shotsMask & type.getMask()) == type.getMask();
    }

    public void setChargedShot(ShotType type, boolean charged) {
        if (charged) {
            this._shotsMask |= type.getMask();
        } else {
            this._shotsMask &= ~type.getMask();
        }

    }

    public void rechargeShots(boolean physical, boolean magic) {
        if (physical) {
            if (this._currentSsCount <= 0) {
                return;
            }

            if (Rnd.get(100) > this.getTemplate().getSsRate()) {
                return;
            }

            --this._currentSsCount;
            this.broadcastPacketInRadius(new MagicSkillUse(this, this, 2154, 1, 0, 0), 600);
            this.setChargedShot(ShotType.SOULSHOT, true);
        }

        if (magic) {
            if (this._currentSpsCount <= 0) {
                return;
            }

            if (Rnd.get(100) > this.getTemplate().getSpsRate()) {
                return;
            }

            --this._currentSpsCount;
            this.broadcastPacketInRadius(new MagicSkillUse(this, this, 2061, 1, 0, 0), 600);
            this.setChargedShot(ShotType.SPIRITSHOT, true);
        }

    }

    public int getSkillLevel(int skillId) {
        for (List<L2Skill> list : this.getTemplate().getSkills().values()) {
            for (L2Skill skill : list) {
                if (skill.getId() == skillId) {
                    return skill.getLevel();
                }
            }
        }

        return 0;
    }

    public L2Skill getSkill(int skillId) {
        for (List<L2Skill> list : this.getTemplate().getSkills().values()) {
            for (L2Skill skill : list) {
                if (skill.getId() == skillId) {
                    return skill;
                }
            }
        }

        return null;
    }

    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    public Weapon getActiveWeaponItem() {
        int weaponId = this.getTemplate().getRightHand();
        if (weaponId <= 0) {
            return null;
        } else {
            Item item = ItemTable.getInstance().getTemplate(weaponId);
            return !(item instanceof Weapon) ? null : (Weapon) item;
        }
    }

    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    public Item getSecondaryWeaponItem() {
        int itemId = this.getTemplate().getLeftHand();
        return itemId <= 0 ? null : ItemTable.getInstance().getTemplate(itemId);
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
        this._currentSsCount = this.getTemplate().getSsCount();
        this._currentSpsCount = this.getTemplate().getSpsCount();
        List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_SPAWN);
        if (scripts != null) {
            for (Quest quest : scripts) {
                quest.notifySpawn(this);
            }
        }

    }

    public void onDecay() {
        if (!this.isDecayed()) {
            this.setDecayed(true);
            List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_DECAY);
            if (scripts != null) {
                for (Quest quest : scripts) {
                    quest.notifyDecay(this);
                }
            }

            super.onDecay();
            if (this._spawn != null) {
                this._spawn.doRespawn();
            }

        }
    }

    public void deleteMe() {
        this.onDecay();
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
        String var10000 = this.getName();
        return var10000 + " - " + this.getNpcId() + " (" + this.getObjectId() + ")";
    }

    public L2Spawn getSpawn() {
        return this._spawn;
    }

    public void setSpawn(L2Spawn spawn) {
        this._spawn = spawn;
    }

    public Npc scheduleDespawn(long delay) {
        ThreadPool.schedule(() -> {
            if (!this.isDecayed()) {
                this.deleteMe();
            }

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
        if (!this.isDecayed()) {
            DecayTaskManager.getInstance().cancel(this);
            this.onDecay();
        }

    }

    public void onRandomAnimation(int id) {
        long now = System.currentTimeMillis();
        if (now - this._lastSocialBroadcast > 12000L) {
            this._lastSocialBroadcast = now;
            this.broadcastPacket(new SocialAction(this, id));
        }

    }

    public void startRandomAnimationTimer() {
        if (this.hasRandomAnimation()) {
            RandomAnimationTaskManager.getInstance().add(this, this.calculateRandomAnimationTimer());
        }
    }

    public int calculateRandomAnimationTimer() {
        return Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION);
    }

    public boolean hasRandomAnimation() {
        return Config.MAX_NPC_ANIMATION > 0 && !this.getTemplate().getAiType().equals(NpcTemplate.AIType.CORPSE);
    }

    public int getNpcId() {
        return this.getTemplate().getNpcId();
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
        return this._scriptValue == val;
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
        return (int) ((double) this.getTemplate().getRewardExp() * Config.RATE_XP);
    }

    public int getSpReward() {
        return (int) ((double) this.getTemplate().getRewardSp() * Config.RATE_SP);
    }

    protected void sendNpcInfos(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/admin/npcinfo.htm");
        html.replace("%class%", this.getClass().getSimpleName());
        html.replace("%id%", this.getTemplate().getNpcId());
        html.replace("%lvl%", this.getTemplate().getLevel());
        html.replace("%name%", this.getName());
        html.replace("%race%", this.getTemplate().getRace().toString());
        html.replace("%tmplid%", this.getTemplate().getIdTemplate());
        html.replace("%script%", this.getScriptValue());
        html.replace("%castle%", this.getCastle() != null ? this.getCastle().getName() : "none");
        html.replace("%aggro%", this.getTemplate().getAggroRange());
        html.replace("%corpse%", StringUtil.getTimeStamp(this.getTemplate().getCorpseTime()));
        html.replace("%enchant%", this.getTemplate().getEnchantEffect());
        html.replace("%hp%", (int) this.getCurrentHp());
        html.replace("%hpmax%", this.getMaxHp());
        html.replace("%mp%", (int) this.getCurrentMp());
        html.replace("%mpmax%", this.getMaxMp());
        html.replace("%patk%", this.getPAtk(null));
        html.replace("%matk%", this.getMAtk(null, null));
        html.replace("%pdef%", this.getPDef(null));
        html.replace("%mdef%", this.getMDef(null, null));
        html.replace("%accu%", this.getAccuracy());
        html.replace("%evas%", this.getEvasionRate(null));
        html.replace("%crit%", this.getCriticalHit(null, null));
        html.replace("%rspd%", this.getMoveSpeed());
        html.replace("%aspd%", this.getPAtkSpd());
        html.replace("%cspd%", this.getMAtkSpd());
        html.replace("%str%", this.getSTR());
        html.replace("%dex%", this.getDEX());
        html.replace("%con%", this.getCON());
        html.replace("%int%", this.getINT());
        html.replace("%wit%", this.getWIT());
        html.replace("%men%", this.getMEN());
        int var10002 = this.getX();
        html.replace("%loc%", var10002 + " " + this.getY() + " " + this.getZ());
        html.replace("%dist%", (int) Math.sqrt(player.getDistanceSq(this)));
        html.replace("%ele_fire%", this.getDefenseElementValue((byte) 2));
        html.replace("%ele_water%", this.getDefenseElementValue((byte) 3));
        html.replace("%ele_wind%", this.getDefenseElementValue((byte) 1));
        html.replace("%ele_earth%", this.getDefenseElementValue((byte) 4));
        html.replace("%ele_holy%", this.getDefenseElementValue((byte) 5));
        html.replace("%ele_dark%", this.getDefenseElementValue((byte) 6));
        if (this.getSpawn() != null) {
            html.replace("%spawn%", this.getSpawn().getLoc().toString());
            html.replace("%loc2d%", (int) Math.sqrt(this.getPlanDistanceSq(this.getSpawn().getLocX(), this.getSpawn().getLocY())));
            html.replace("%loc3d%", (int) Math.sqrt(this.getDistanceSq(this.getSpawn().getLocX(), this.getSpawn().getLocY(), this.getSpawn().getLocZ())));
            html.replace("%resp%", StringUtil.getTimeStamp(this.getSpawn().getRespawnDelay()));
            html.replace("%rand_resp%", StringUtil.getTimeStamp(this.getSpawn().getRespawnRandom()));
        } else {
            html.replace("%spawn%", "<font color=FF0000>null</font>");
            html.replace("%loc2d%", "<font color=FF0000>--</font>");
            html.replace("%loc3d%", "<font color=FF0000>--</font>");
            html.replace("%resp%", "<font color=FF0000>--</font>");
            html.replace("%rand_resp%", "<font color=FF0000>--</font>");
        }

        if (this.hasAI()) {
            html.replace("%ai_intention%", "<font color=\"LEVEL\">Intention</font><table width=\"100%\"><tr><td><font color=\"LEVEL\">Intention:</font></td><td>" + this.getAI().getDesire().getIntention().name() + "</td></tr>");
            html.replace("%ai%", "<tr><td><font color=\"LEVEL\">AI:</font></td><td>" + this.getAI().getClass().getSimpleName() + "</td></tr></table><br>");
        } else {
            html.replace("%ai_intention%", "");
            html.replace("%ai%", "");
        }

        html.replace("%ai_type%", this.getTemplate().getAiType().name());
        html.replace("%ai_clan%", this.getTemplate().getClans() != null ? "<tr><td width=100><font color=\"LEVEL\">Clan:</font></td><td align=right width=170>" + Arrays.toString(this.getTemplate().getClans()) + " " + this.getTemplate().getClanRange() + "</td></tr>" + (this.getTemplate().getIgnoredIds() != null ? "<tr><td width=100><font color=\"LEVEL\">Ignored ids:</font></td><td align=right width=170>" + Arrays.toString(this.getTemplate().getIgnoredIds()) + "</td></tr>" : "") : "");
        html.replace("%ai_move%", String.valueOf(this.getTemplate().canMove()));
        html.replace("%ai_seed%", String.valueOf(this.getTemplate().isSeedable()));
        var10002 = this._currentSsCount;
        html.replace("%ai_ssinfo%", var10002 + "[" + this.getTemplate().getSsCount() + "] - " + this.getTemplate().getSsRate() + "%");
        var10002 = this._currentSpsCount;
        html.replace("%ai_spsinfo%", var10002 + "[" + this.getTemplate().getSpsCount() + "] - " + this.getTemplate().getSpsRate() + "%");
        html.replace("%shop%", this instanceof Merchant ? "<button value=\"Shop\" action=\"bypass -h admin_show_shop " + this.getNpcId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : "");
        html.replace("%minion%", !(this instanceof Monster) || ((Monster) this).getMaster() == null && !((Monster) this).hasMinions() ? "" : "<button value=\"Minions\" action=\"bypass -h admin_show_minion\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">");
        player.sendPacket(html);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.equalsIgnoreCase("TerritoryStatus")) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            if (this.getCastle().getOwnerId() > 0) {
                html.setFile("data/html/territorystatus.htm");
                Clan clan = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
                html.replace("%clanname%", clan.getName());
                html.replace("%clanleadername%", clan.getLeaderName());
            } else {
                html.setFile("data/html/territorynoclan.htm");
            }

            html.replace("%castlename%", this.getCastle().getName());
            html.replace("%taxpercent%", this.getCastle().getTaxPercent());
            html.replace("%objectId%", this.getObjectId());
            if (this.getCastle().getCastleId() > 6) {
                html.replace("%territory%", "The Kingdom of Elmore");
            } else {
                html.replace("%territory%", "The Kingdom of Aden");
            }

            player.sendPacket(html);
        } else if (command.startsWith("Quest")) {
            String quest = "";

            try {
                quest = command.substring(5).trim();
            } catch (IndexOutOfBoundsException ignored) {
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
            } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            }

            this.showChatWindow(player, val);
        } else if (command.startsWith("Link")) {
            String path = command.substring(5).trim();
            if (path.contains("..")) {
                return;
            }

            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/" + path);
            html.replace("%objectId%", this.getObjectId());
            player.sendPacket(html);
        } else if (command.startsWith("Loto")) {
            int val = 0;

            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            }

            if (val == 0) {
                for (int i = 0; i < 5; ++i) {
                    player.setLoto(i, 0);
                }
            }

            this.showLotoWindow(player, val);
        } else if (command.startsWith("CPRecovery")) {
            if (this.getNpcId() != 31225 && this.getNpcId() != 31226) {
                return;
            }

            if (player.isCursedWeaponEquipped()) {
                player.sendMessage("Go away, you're not welcome here.");
                return;
            }

            if (player.reduceAdena("RestoreCP", 100, player.getCurrentFolk(), true)) {
                this.setTarget(player);
                this.doCast(SkillTable.FrequentSkill.ARENA_CP_RECOVERY.getSkill());
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addCharName(player));
            }
        } else if (command.startsWith("SupportMagic")) {
            if (player.isCursedWeaponEquipped()) {
                return;
            }

            int playerLevel = player.getLevel();
            int lowestLevel = 0;
            int higestLevel = 0;
            this.setTarget(player);
            if (player.isMageClass()) {
                lowestLevel = NewbieBuffData.getInstance().getMagicLowestLevel();
                higestLevel = NewbieBuffData.getInstance().getMagicHighestLevel();
            } else {
                lowestLevel = NewbieBuffData.getInstance().getPhysicLowestLevel();
                higestLevel = NewbieBuffData.getInstance().getPhysicHighestLevel();
            }

            if (playerLevel > higestLevel || !player.isNewbie()) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setHtml("<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + higestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>");
                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
                return;
            }

            if (playerLevel < lowestLevel) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setHtml("<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>");
                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
                return;
            }

            for (NewbieBuff buff : NewbieBuffData.getInstance().getBuffs()) {
                if (buff.isMagicClassBuff() == player.isMageClass() && playerLevel >= buff.getLowerLevel() && playerLevel <= buff.getUpperLevel()) {
                    L2Skill skill = SkillTable.getInstance().getInfo(buff.getSkillId(), buff.getSkillLevel());
                    if (skill.getSkillType() == L2SkillType.SUMMON) {
                        player.doCast(skill);
                    } else {
                        this.doCast(skill);
                    }
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
            }
        } else if (command.startsWith("EnterRift")) {
            try {
                byte b1 = Byte.parseByte(command.substring(10));
                DimensionalRiftManager.getInstance().start(player, b1, this);
            } catch (Exception ignored) {
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

        return HtmCache.getInstance().isLoadable(filename) ? filename : "data/html/npcdefault.htm";
    }

    public void broadcastNpcSay(String message) {
        this.broadcastPacket(new NpcSay(this.getObjectId(), 0, this.getNpcId(), message));
    }

    public void showLotoWindow(Player player, int val) {
        int npcId = this.getTemplate().getNpcId();
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        if (val == 0) {
            html.setFile(this.getHtmlPath(npcId, 1));
        } else if (val >= 1 && val <= 21) {
            if (!LotteryManager.getInstance().isStarted()) {
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
                return;
            }

            if (!LotteryManager.getInstance().isSellableTickets()) {
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
                return;
            }

            html.setFile(this.getHtmlPath(npcId, 5));
            int count = 0;
            int found = 0;

            for (int i = 0; i < 5; ++i) {
                if (player.getLoto(i) == val) {
                    player.setLoto(i, 0);
                    found = 1;
                } else if (player.getLoto(i) > 0) {
                    ++count;
                }
            }

            if (count < 5 && found == 0 && val <= 20) {
                for (int i = 0; i < 5; ++i) {
                    if (player.getLoto(i) == 0) {
                        player.setLoto(i, val);
                        break;
                    }
                }
            }

            count = 0;

            for (int i = 0; i < 5; ++i) {
                if (player.getLoto(i) > 0) {
                    ++count;
                    String button = String.valueOf(player.getLoto(i));
                    if (player.getLoto(i) < 10) {
                        button = "0" + button;
                    }

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

            for (int i = 0; i < 5; ++i) {
                if (player.getLoto(i) == 0) {
                    return;
                }

                if (player.getLoto(i) < 17) {
                    enchant = (int) ((double) enchant + Math.pow(2.0F, player.getLoto(i) - 1));
                } else {
                    type2 = (int) ((double) type2 + Math.pow(2.0F, player.getLoto(i) - 17));
                }
            }

            if (!player.reduceAdena("Loto", price, this, true)) {
                return;
            }

            LotteryManager.getInstance().increasePrize(price);
            ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4442);
            item.setCount(1);
            item.setCustomType1(lotonumber);
            item.setEnchantLevel(enchant);
            item.setCustomType2(type2);
            player.addItem("Loto", item, player, false);
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(4442));
            html.setFile(this.getHtmlPath(npcId, 3));
        } else if (val == 23) {
            html.setFile(this.getHtmlPath(npcId, 3));
        } else if (val == 24) {
            int lotoNumber = LotteryManager.getInstance().getId();
            StringBuilder sb = new StringBuilder();

            for (ItemInstance item : player.getInventory().getItems()) {
                if (item != null && item.getItemId() == 4442 && item.getCustomType1() < lotoNumber) {
                    StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Loto ", item.getObjectId(), "\">", item.getCustomType1(), " Event Number ");
                    int[] numbers = LotteryManager.decodeNumbers(item.getEnchantLevel(), item.getCustomType2());

                    for (int i = 0; i < 5; ++i) {
                        StringUtil.append(sb, numbers[i], " ");
                    }

                    int[] check = LotteryManager.checkTicket(item);
                    if (check[0] > 0) {
                        switch (check[0]) {
                            case 1 -> sb.append("- 1st Prize");
                            case 2 -> sb.append("- 2nd Prize");
                            case 3 -> sb.append("- 3th Prize");
                            case 4 -> sb.append("- 4th Prize");
                        }

                        StringUtil.append(sb, " ", check[1], "a.");
                    }

                    sb.append("</a><br>");
                }
            }

            if (sb.isEmpty()) {
                sb.append("There is no winning lottery ticket...<br>");
            }

            html.setFile(this.getHtmlPath(npcId, 4));
            html.replace("%result%", sb.toString());
        } else if (val == 25) {
            html.setFile(this.getHtmlPath(npcId, 2));
            html.replace("%prize5%", Config.ALT_LOTTERY_5_NUMBER_RATE * (double) 100.0F);
            html.replace("%prize4%", Config.ALT_LOTTERY_4_NUMBER_RATE * (double) 100.0F);
            html.replace("%prize3%", Config.ALT_LOTTERY_3_NUMBER_RATE * (double) 100.0F);
            html.replace("%prize2%", Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
        } else if (val > 25) {
            ItemInstance item = player.getInventory().getItemByObjectId(val);
            if (item != null && item.getItemId() == 4442 && item.getCustomType1() < LotteryManager.getInstance().getId()) {
                if (player.destroyItem("Loto", item, this, true)) {
                    int adena = LotteryManager.checkTicket(item)[1];
                    if (adena > 0) {
                        player.addAdena("Loto", adena, this, true);
                    }
                }

                return;
            }

            return;
        }

        html.replace("%objectId%", this.getObjectId());
        html.replace("%race%", LotteryManager.getInstance().getId());
        html.replace("%adena%", LotteryManager.getInstance().getPrize());
        html.replace("%ticket_price%", Config.ALT_LOTTERY_TICKET_PRICE);
        html.replace("%enddate%", DateFormat.getDateInstance().format(LotteryManager.getInstance().getEndDate()));
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void doCast(NpcTemplate.SkillType type) {
        super.doCast(Rnd.get(this.getTemplate().getSkills(type)));
    }

    protected boolean showPkDenyChatWindow(Player player, String type) {
        String content = HtmCache.getInstance().getHtm("data/html/" + type + "/" + this.getNpcId() + "-pk.htm");
        if (content != null) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setHtml(content);
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return true;
        } else {
            return false;
        }
    }

    public void showChatWindow(Player player) {
        this.showChatWindow(player, 0);
    }

    public void showChatWindow(Player player, int val) {
        this.showChatWindow(player, this.getHtmlPath(this.getNpcId(), val));
    }

    public final void showChatWindow(Player player, String filename) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public FakePc getFakePc() {
        return this._fakePc;
    }

    public double getMovementSpeedMultiplier() {
        return 1.0F;
    }

    public double getAttackSpeedMultiplier() {
        return 1.0F;
    }
}
