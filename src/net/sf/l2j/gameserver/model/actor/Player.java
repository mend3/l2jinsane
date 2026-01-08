/**/
package net.sf.l2j.gameserver.model.actor;

import enginemods.main.EngineModsManager;
import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import mods.achievement.AchievementsManager;
import mods.autofarm.AutofarmPlayerRoutine;
import mods.dressme.DressMeData;
import mods.dungeon.Dungeon;
import mods.pvpZone.RandomZoneManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.data.AgathionData;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.manager.*;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.*;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.*;
import net.sf.l2j.gameserver.enums.actors.*;
import net.sf.l2j.gameserver.enums.items.*;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.events.bossevent.BossEvent;
import net.sf.l2j.gameserver.events.bossevent.BossEvent.EventState;
import net.sf.l2j.gameserver.events.eventengine.EventListener;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.events.tournament.arenas.Arena2x2;
import net.sf.l2j.gameserver.events.tournament.arenas.Arena4x4;
import net.sf.l2j.gameserver.events.tournament.arenas.Arena9x9;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.PlayerAI;
import net.sf.l2j.gameserver.model.actor.ai.type.SummonAI;
import net.sf.l2j.gameserver.model.actor.instance.*;
import net.sf.l2j.gameserver.model.actor.npc.RewardInfo;
import net.sf.l2j.gameserver.model.actor.player.*;
import net.sf.l2j.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2j.gameserver.model.actor.status.PlayerStatus;
import net.sf.l2j.gameserver.model.actor.template.PetTemplate;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.craft.ManufactureList;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.holder.Timestamp;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.SkillNode;
import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemState;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.itemcontainer.*;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.ItemPassiveSkillsListener;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.memo.PlayerMemo;
import net.sf.l2j.gameserver.model.multisell.PreparedListContainer;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.model.zone.type.L2MultiFunctionZone;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.*;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSiegeFlag;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;
import net.sf.l2j.gameserver.taskmanager.*;
import net.sf.l2j.util.variables.PlayerVar;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class Player extends Playable {
    public static final int REQUEST_TIMEOUT = 15;
    private static final String INSERT_OR_UPDATE_CHARACTER_DRESSME_DATA = "INSERT INTO characters_dressme_data (obj_Id, armor_skins, armor_skin_option, weapon_skins, weapon_skin_option, hair_skins, hair_skin_option, face_skins, face_skin_option) VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE obj_Id=?, armor_skins=?, armor_skin_option=?, weapon_skins=?, weapon_skin_option=?, hair_skins=?, hair_skin_option=?, face_skins=?, face_skin_option=?, shield_skins=?, shield_skin_option=?";
    private static final String RESTORE_CHARACTER_DRESSME_DATA = "SELECT obj_Id, armor_skins, armor_skin_option, weapon_skins, weapon_skin_option, hair_skins, hair_skin_option, face_skins, face_skin_option, shield_skins, shield_skin_option FROM characters_dressme_data WHERE obj_id=?";
    private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
    private static final String ADD_OR_UPDATE_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)";
    private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
    private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
    private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC";
    private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
    private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,pc_point=?,newbie_armor=? WHERE obj_id=?";
    private static final String RESTORE_CHARACTER = "SELECT * FROM characters WHERE obj_id=?";
    private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
    private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
    private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
    private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
    private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
    private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
    private static final String UPDATE_TARGET_RECOM_HAVE = "UPDATE characters SET rec_have=? WHERE obj_Id=?";
    private static final String UPDATE_CHAR_RECOM_LEFT = "UPDATE characters SET rec_left=? WHERE obj_Id=?";
    private static final String UPDATE_NOBLESS = "UPDATE characters SET nobless=? WHERE obj_Id=?";
    private static final String DELETE_RECIPEBOOK = "DELETE FROM character_recipebook WHERE charId=?";
    private static final String SAVE_RECIPEBOOK = "INSERT INTO character_recipebook (charId, recipeId) values(?,?)";
    private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_MIN_LVL = Comparator.comparing(SkillNode::getMinLvl);
    private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_LVL = Comparator.comparing(IntIntHolder::getValue);
    private static final int FALLING_VALIDATION_DELAY = 10000;
    private final Map<Integer, String> _chars = new HashMap<>();
    private final Map<Integer, SubClass> _subClasses = new ConcurrentSkipListMap<>();
    private final ReentrantLock _subclassLock = new ReentrantLock();
    private final Map<Integer, Recipe> _dwarvenRecipeBook;
    private final Map<Integer, Recipe> _commonRecipeBook;
    private final Location _savedLocation;
    private final List<Integer> _recomChars;
    private final PcInventory _inventory;
    private final List<PcFreight> _depositedFreight;
    private final List<QuestState> _quests;
    private final List<QuestState> _notifyQuestOfDeathList;
    private final PlayerMemo _vars;
    private final Map<String, PlayerVar> _dungeonvars;
    private final ShortcutList _shortcutList;
    private final MacroList _macroList;
    private final HennaList _hennaList;
    private final RadarList _radarList;
    private final AtomicInteger _charges;
    private final Request _request;
    private final int[] _loto;
    private final int[] _race;
    private final BlockList _blockList;
    private final List<String> _validBypass;
    private final List<String> _validBypass2;
    private final Map<Integer, L2Skill> _skills;
    private final SkillUseHolder _currentSkill;
    private final SkillUseHolder _currentPetSkill;
    private final SkillUseHolder _queuedSkill;
    private final List<Integer> _friendList;
    private final List<Integer> _selectedFriendList;
    private final List<Integer> _selectedBlocksList;
    private final Map<Integer, Timestamp> _reuseTimeStamps;
    private final Map<String, PlayerVar> _variables;
    private final List<Integer> _armorSkins = new CopyOnWriteArrayList<>();
    private final List<Integer> _weaponSkins = new CopyOnWriteArrayList<>();
    private final List<Integer> _hairSkins = new CopyOnWriteArrayList<>();
    private final List<Integer> _faceSkins = new CopyOnWriteArrayList<>();
    private final List<Integer> _shieldSkins = new CopyOnWriteArrayList<>();
    private final String _accountName;
    private final Appearance _appearance;
    private final Punishment _punishment = new Punishment(this);
    private final SpawnLocation _boatPosition;
    private final FishingStance _fishingStance;
    private final List<Integer> _ignored;
    private final List<Integer> _ignoredMonster;
    private final List<Integer> _completedAchievements;
    private final Map<Integer, Cubic> _cubics;
    private final Set<Integer> _activeSoulShots;
    public int _lastX;
    public int _lastY;
    public int _lastZ;
    public int _originalTitleColorTournament;
    public String _originalTitleTournament;
    public int duelist_cont;
    public int dreadnought_cont;
    public int tanker_cont;
    public int dagger_cont;
    public int archer_cont;
    public int bs_cont;
    public int archmage_cont;
    public int soultaker_cont;
    public int mysticMuse_cont;
    public int stormScreamer_cont;
    public int titan_cont;
    public int grandKhauatari_cont;
    public int dominator_cont;
    public int doomcryer_cont;
    int buff;
    private int _baseClass;
    private int _activeClass;
    private int _classIndex;
    private PetTemplate _petTemplate;
    private Future<?> _mountFeedTask;
    private int _throneId;
    private boolean _inventoryDisable;
    private int _armorSkinOption = 0;
    private int _weaponSkinOption = 0;
    private int _hairSkinOption = 0;
    private int _faceSkinOption = 0;
    private int _shieldSkinOption = 0;
    private boolean isTryingSkin = false;
    private boolean isTryingSkinPremium = false;
    private GameClient _client;
    private long _deleteTimer;
    private boolean _isOnline;
    private long _onlineTime;
    private long _onlineBeginTime;
    private long _lastAccess;
    private long _uptime;
    private long _expBeforeDeath;
    private int _karma;
    private int _pvpKills;
    private int _pkKills;
    private byte _pvpFlag;
    private byte _siegeState;
    private int _curWeightPenalty;
    private int _lastCompassZone;
    private boolean _isIn7sDungeon;
    private int heroConsecutiveKillCount = 0;
    private boolean isPVPHero = false;
    private boolean _isInOlympiadMode;
    private boolean _isInOlympiadStart;
    private int _olympiadGameId = -1;
    private int _olympiadSide = -1;
    private DuelState _duelState;
    private int _duelId;
    private SystemMessageId _noDuelReason;
    private Boat _boat;
    private boolean _canFeed;
    private PetDataEntry _petData;
    private int _controlItemId;
    private int _curFeed;
    private ScheduledFuture<?> _dismountTask;
    private int _mountType;
    private int _mountNpcId;
    private int _mountLevel;
    private int _mountObjectId;
    private int _teleMode;
    private boolean _isCrystallizing;
    private boolean _isCrafting;
    private boolean _isSitting;
    private int _recomHave;
    private int _recomLeft;
    private PcWarehouse _warehouse;
    private PcFreight _freight;
    private StoreType _storeType;
    private TradeList _activeTradeList;
    private ItemContainer _activeWarehouse;
    private ManufactureList _createList;
    private TradeList _sellList;
    private TradeList _buyList;
    private PreparedListContainer _currentMultiSell;
    private boolean _isNoble;
    private boolean _isHero;
    private Folk _currentFolk;
    private int _questNpcObject;
    private Summon _summon;
    private TamedBeast _tamedBeast;
    private int _partyroom;
    private int _clanId;
    private Clan _clan;
    private int _apprentice;
    private int _sponsor;
    private long _clanJoinExpiryTime;
    private long _clanCreateExpiryTime;
    private int _powerGrade;
    private int _clanPrivileges;
    private int _pledgeClass;
    private int _pledgeType;
    private int _lvlJoinedAcademy;
    private boolean _wantsPeace;
    private int _deathPenaltyBuffLevel;
    private ScheduledFuture<?> _chargeTask;
    private Location _currentSkillWorldPosition;
    private AccessLevel _accessLevel;
    private boolean _messageRefusal;
    private boolean _tradeRefusal;
    private boolean _exchangeRefusal;
    private boolean _isPartyInRefuse;
    private Party _party;
    private LootRule _lootRule;
    private Player _activeRequester;
    private long _requestExpireTime;
    private ScheduledFuture<?> _protectTask;
    private long _recentFakeDeathEndTime;
    private boolean _isFakeDeath;
    private int _expertiseArmorPenalty;
    private boolean _expertiseWeaponPenalty;
    private ItemInstance _activeEnchantItem;
    private TeamType _team;
    private int _alliedVarkaKetra;
    private Forum _forumMemo;
    private boolean _isInSiege;
    private int _cursedWeaponEquippedId;
    private int _reviveRequested;
    private double _revivePower;
    private boolean _revivePet;
    private double _cpUpdateIncCheck;
    private double _cpUpdateDecCheck;
    private double _cpUpdateInterval;
    private double _mpUpdateIncCheck;
    private double _mpUpdateDecCheck;
    private double _mpUpdateInterval;
    private int _clientX;
    private int _clientY;
    private int _clientZ;
    private int _mailPosition;
    private volatile long _fallingTimestamp;
    private ScheduledFuture<?> _shortBuffTask;
    private int _shortBuffTaskSkillId;
    private int _coupleId;
    private boolean _isUnderMarryRequest;
    private int _requesterId;
    private Player _summonTargetRequest;
    private L2Skill _summonSkillRequest;
    private Door _requestedGate;
    private long _nextAgathionUseTime;
    private Npc _agathion;
    private int _lastAgathionNpcId;
    private long _lastAgathionUse;
    private boolean _autoFarm;
    private int autoFarmRadius;
    private int autoFarmShortCut;
    private int autoFarmHealPercente;
    private boolean autoFarmBuffProtection;
    private boolean autoAntiKsProtection;
    private boolean autoFarmSummonAttack;
    private int autoFarmSummonSkillPercente;
    private int hpPotionPercent;
    private int mpPotionPercent;
    private AutofarmPlayerRoutine _bot;
    private int pcBangPoint;
    private boolean receiveNewbieArmor;
    private int bossEventDamage;
    private boolean _TournamentTeleport;
    private boolean _isCMultisell;
    private boolean _isSellItemCommunity;
    private String _hwid;
    private Dungeon dungeon;
    private boolean isCommunityWarehouse;

    private Player(int objectId, PlayerTemplate template, String accountName, Appearance app) {
        super(objectId, template);
        this._duelState = DuelState.NO_DUEL;
        this._noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
        this._boatPosition = new SpawnLocation(0, 0, 0, 0);
        this._dwarvenRecipeBook = new HashMap<>();
        this._commonRecipeBook = new HashMap<>();
        this._savedLocation = new Location(0, 0, 0);
        this._recomChars = new ArrayList<>();
        this._inventory = new PcInventory(this);
        this._depositedFreight = new ArrayList<>();
        this._storeType = StoreType.NONE;
        this._quests = new ArrayList<>();
        this._notifyQuestOfDeathList = new ArrayList<>();
        this._vars = new PlayerMemo(this.getObjectId());
        this._dungeonvars = new ConcurrentHashMap<>();
        this._shortcutList = new ShortcutList(this);
        this._macroList = new MacroList(this);
        this._hennaList = new HennaList(this);
        this._radarList = new RadarList(this);
        this._charges = new AtomicInteger();
        this._isPartyInRefuse = false;
        this._request = new Request(this);
        this._cubics = new ConcurrentSkipListMap<>();
        this._activeSoulShots = ConcurrentHashMap.newKeySet(1);
        this._loto = new int[5];
        this._race = new int[2];
        this._blockList = new BlockList(this);
        this._team = TeamType.NONE;
        this._fishingStance = new FishingStance(this);
        this._validBypass = new ArrayList<>();
        this._validBypass2 = new ArrayList<>();
        this._skills = new ConcurrentSkipListMap<>();
        this._currentSkill = new SkillUseHolder();
        this._currentPetSkill = new SkillUseHolder();
        this._queuedSkill = new SkillUseHolder();
        this._revivePower = 0.0F;
        this._cpUpdateIncCheck = 0.0F;
        this._cpUpdateDecCheck = 0.0F;
        this._cpUpdateInterval = 0.0F;
        this._mpUpdateIncCheck = 0.0F;
        this._mpUpdateDecCheck = 0.0F;
        this._mpUpdateInterval = 0.0F;
        this._friendList = new ArrayList<>();
        this._selectedFriendList = new ArrayList<>();
        this._selectedBlocksList = new ArrayList<>();
        this._nextAgathionUseTime = 0L;
        this._agathion = null;
        this._ignored = new ArrayList<>();
        this._reuseTimeStamps = new ConcurrentHashMap<>();
        this.autoFarmRadius = 1200;
        this.autoFarmShortCut = 9;
        this.autoFarmHealPercente = 30;
        this.autoFarmBuffProtection = false;
        this.autoAntiKsProtection = false;
        this.autoFarmSummonAttack = false;
        this.autoFarmSummonSkillPercente = 0;
        this.hpPotionPercent = 60;
        this.mpPotionPercent = 60;
        this._ignoredMonster = new ArrayList<>();
        this._bot = new AutofarmPlayerRoutine(this);
        this.pcBangPoint = 0;
        this.bossEventDamage = 0;
        this._originalTitleColorTournament = 0;
        this.duelist_cont = 0;
        this.dreadnought_cont = 0;
        this.tanker_cont = 0;
        this.dagger_cont = 0;
        this.archer_cont = 0;
        this.bs_cont = 0;
        this.archmage_cont = 0;
        this.soultaker_cont = 0;
        this.mysticMuse_cont = 0;
        this.stormScreamer_cont = 0;
        this.titan_cont = 0;
        this.grandKhauatari_cont = 0;
        this.dominator_cont = 0;
        this.doomcryer_cont = 0;
        this.buff = 0;
        this._isCMultisell = false;
        this._isSellItemCommunity = false;
        this._variables = new ConcurrentHashMap<>();
        this._completedAchievements = new ArrayList<>();
        this.dungeon = null;
        this.initCharStatusUpdateValues();
        this._accountName = accountName;
        this._appearance = app;
        this._lastAgathionNpcId = 0;
        this._lastAgathionUse = 0L;
        this._ai = new PlayerAI(this);
        this.getInventory().restore();
        this.getWarehouse();
        this.getFreight();
    }

    public static Player create(int objectId, PlayerTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, Sex sex) {
        PlayerData.add(objectId, name, accountName);
        Appearance app = new Appearance(face, hairColor, hairStyle, sex);
        Player player = new Player(objectId, template, accountName, app);
        player.setName(name);
        player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL);
        PlayerInfoTable.getInstance().addPlayer(objectId, accountName, name, player.getAccessLevel().getLevel());
        player.setBaseClass(player.getClassId());

        try {
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");) {
                ps.setString(1, accountName);
                ps.setInt(2, player.getObjectId());
                ps.setString(3, player.getName());
                ps.setInt(4, player.getLevel());
                ps.setInt(5, player.getMaxHp());
                ps.setDouble(6, player.getCurrentHp());
                ps.setInt(7, player.getMaxCp());
                ps.setDouble(8, player.getCurrentCp());
                ps.setInt(9, player.getMaxMp());
                ps.setDouble(10, player.getCurrentMp());
                ps.setInt(11, player.getAppearance().getFace());
                ps.setInt(12, player.getAppearance().getHairStyle());
                ps.setInt(13, player.getAppearance().getHairColor());
                ps.setInt(14, player.getAppearance().getSex().ordinal());
                ps.setLong(15, player.getExp());
                ps.setInt(16, player.getSp());
                ps.setInt(17, player.getKarma());
                ps.setInt(18, player.getPvpKills());
                ps.setInt(19, player.getPkKills());
                ps.setInt(20, player.getClanId());
                ps.setInt(21, player.getRace().ordinal());
                ps.setInt(22, player.getClassId().getId());
                ps.setLong(23, player.getDeleteTimer());
                ps.setInt(24, player.hasDwarvenCraft() ? 1 : 0);
                ps.setString(25, player.getTitle());
                ps.setInt(26, player.getAccessLevel().getLevel());
                ps.setInt(27, player.isOnlineInt());
                ps.setInt(28, player.isIn7sDungeon() ? 1 : 0);
                ps.setInt(29, player.getClanPrivileges());
                ps.setInt(30, player.wantsPeace() ? 1 : 0);
                ps.setInt(31, player.getBaseClass());
                ps.setInt(32, player.isNoble() ? 1 : 0);
                ps.setLong(33, 0L);
                ps.executeUpdate();
            }

            return player;
        } catch (Exception e) {
            LOGGER.error("Couldn't create player {} for {} account.", e, name, accountName);
            return null;
        }
    }

    public static Player restore(int objectId) {
        Player player = null;

        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE obj_id=?");) {
            ps.setInt(1, objectId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int activeClassId = rs.getInt("classid");
                    PlayerTemplate template = PlayerClassData.getInstance().getTemplate(activeClassId);
                    Appearance app = new Appearance(rs.getByte("face"), rs.getByte("hairColor"), rs.getByte("hairStyle"), Sex.values()[rs.getInt("sex")]);
                    player = new Player(objectId, template, rs.getString("account_name"), app);
                    player.setName(rs.getString("char_name"));
                    player._lastAccess = rs.getLong("lastAccess");
                    player.getStat().setExp(rs.getLong("exp"));
                    player.getStat().setLevel(rs.getByte("level"));
                    player.getStat().setSp(rs.getInt("sp"));
                    player.setExpBeforeDeath(rs.getLong("expBeforeDeath"));
                    player.setWantsPeace(rs.getInt("wantspeace") == 1);
                    player.setKarma(rs.getInt("karma"));
                    player.setPvpKills(rs.getInt("pvpkills"));
                    player.setPkKills(rs.getInt("pkkills"));
                    player.setOnlineTime(rs.getLong("onlinetime"));
                    player.setNoble(rs.getInt("nobless") == 1, false);
                    player.setClanJoinExpiryTime(rs.getLong("clan_join_expiry_time"));
                    if (player.getClanJoinExpiryTime() < System.currentTimeMillis()) {
                        player.setClanJoinExpiryTime(0L);
                    }

                    player.setClanCreateExpiryTime(rs.getLong("clan_create_expiry_time"));
                    if (player.getClanCreateExpiryTime() < System.currentTimeMillis()) {
                        player.setClanCreateExpiryTime(0L);
                    }

                    player.setPowerGrade(rs.getInt("power_grade"));
                    player.setPledgeType(rs.getInt("subpledge"));
                    int clanId = rs.getInt("clanid");
                    if (clanId > 0) {
                        player.setClan(ClanTable.getInstance().getClan(clanId));
                    }

                    if (player.getClan() != null) {
                        if (player.getClan().getLeaderId() != player.getObjectId()) {
                            if (player.getPowerGrade() == 0) {
                                player.setPowerGrade(5);
                            }

                            player.setClanPrivileges(player.getClan().getPriviledgesByRank(player.getPowerGrade()));
                        } else {
                            player.setClanPrivileges(8388606);
                            player.setPowerGrade(1);
                        }
                    } else {
                        player.setClanPrivileges(0);
                    }

                    player.setDeleteTimer(rs.getLong("deletetime"));
                    player.setTitle(rs.getString("title"));
                    player.setAccessLevel(rs.getInt("accesslevel"));
                    player.setUptime(System.currentTimeMillis());
                    player.setRecomHave(rs.getInt("rec_have"));
                    player.setRecomLeft(rs.getInt("rec_left"));
                    player._classIndex = 0;

                    try {
                        player.setBaseClass(rs.getInt("base_class"));
                    } catch (Exception var21) {
                        player.setBaseClass(activeClassId);
                    }

                    if (restoreSubClassData(player) && activeClassId != player.getBaseClass()) {
                        for (SubClass subClass : player.getSubClasses().values()) {
                            if (subClass.getClassId() == activeClassId) {
                                player._classIndex = subClass.getClassIndex();
                            }
                        }
                    }

                    if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass()) {
                        player.setClassId(player.getBaseClass());
                    } else {
                        player._activeClass = activeClassId;
                    }

                    player.setApprentice(rs.getInt("apprentice"));
                    player.setSponsor(rs.getInt("sponsor"));
                    player.setLvlJoinedAcademy(rs.getInt("lvl_joined_academy"));
                    player.setIsIn7sDungeon(rs.getInt("isin7sdungeon") == 1);
                    player.getPunishment().load(rs.getInt("punish_level"), rs.getLong("punish_timer"));
                    CursedWeaponManager.getInstance().checkPlayer(player);
                    player.setAllianceWithVarkaKetra(rs.getInt("varka_ketra_ally"));
                    player.setDeathPenaltyBuffLevel(rs.getInt("death_penalty_level"));
                    player.setPcBang(rs.getInt("pc_point"));
                    player.setReceiveNewbieArmor(rs.getInt("newbie_armor") == 1);
                    player.getPosition().set(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("heading"));
                    if (HeroManager.getInstance().isActiveHero(objectId)) {
                        player.setHero(true);
                    }

                    player.setPledgeClass(ClanMember.calculatePledgeClass(player));
                    player.restoreCharData();
                    player.giveSkills();
                    if (Config.STORE_SKILL_COOLTIME) {
                        player.restoreEffects();
                    }

                    double currentHp = rs.getDouble("curHp");
                    player.setCurrentCp(rs.getDouble("curCp"));
                    player.setCurrentHp(currentHp);
                    player.setCurrentMp(rs.getDouble("curMp"));
                    if (currentHp < (double) 0.5F) {
                        player.setIsDead(true);
                        player.stopHpMpRegeneration();
                    }

                    Pet pet = World.getInstance().getPet(player.getObjectId());
                    if (pet != null) {
                        player.setSummon(pet);
                        pet.setOwner(player);
                    }

                    player.refreshOverloaded();
                    player.refreshExpertisePenalty();
                    player.restoreFriendList();
                    player.restoreDressMeData();

                    try (PreparedStatement ps2 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?")) {
                        ps2.setString(1, player._accountName);
                        ps2.setInt(2, objectId);

                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                player.getAccountChars().put(rs2.getInt("obj_Id"), rs2.getString("char_name"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore player data.", e);
        }

        return player;
    }

    private static boolean restoreSubClassData(Player player) {
        try {
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC");) {
                ps.setInt(1, player.getObjectId());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SubClass subClass = new SubClass(rs.getInt("class_id"), rs.getInt("class_index"), rs.getLong("exp"), rs.getInt("sp"), rs.getByte("level"));
                        player.getSubClasses().put(subClass.getClassIndex(), subClass);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Couldn't restore subclasses for {}.", e, player.getName());
            return false;
        }
    }

    private static String getCastleName(int id) {
        switch (id) {
            case 1 -> {
                return "Gludio";
            }
            case 2 -> {
                return "Dion";
            }
            case 3 -> {
                return "Giran";
            }
            case 4 -> {
                return "Oren";
            }
            case 5 -> {
                return "Aden";
            }
            case 6 -> {
                return "Innadril";
            }
            case 7 -> {
                return "Goddard";
            }
            case 8 -> {
                return "Rune";
            }
            case 9 -> {
                return "Schuttgart";
            }
            default -> {
                return "Not found";
            }
        }
    }

    public static int getQuestInventoryLimit() {
        return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
    }

    public static Player restoreCharInfo(int objectId) {
        Player player = null;

        try {
            Connection con = ConnectionPool.getConnection();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE obj_id=?");

            ps.setInt(1, objectId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int activeClassId = rs.getInt("classid");
                PlayerTemplate template = PlayerClassData.getInstance().getTemplate(activeClassId);
                Appearance app = new Appearance(rs.getByte("face"), rs.getByte("hairColor"), rs.getByte("hairStyle"), Sex.values()[rs.getInt("sex")]);
                player = new Player(objectId, template, rs.getString("account_name"), app);
                player.restoreDressMeData();
            }

            rs.close();

            ps.close();

            con.close();
        } catch (Exception var27) {
            LOGGER.error("Couldn't restore player data.", var27);
        }

        return player;
    }

    public void addFuncsToNewCharacter() {
        super.addFuncsToNewCharacter();
        this.addStatFunc(FuncMaxCpMul.getInstance());
        this.addStatFunc(FuncHennaSTR.getInstance());
        this.addStatFunc(FuncHennaDEX.getInstance());
        this.addStatFunc(FuncHennaINT.getInstance());
        this.addStatFunc(FuncHennaMEN.getInstance());
        this.addStatFunc(FuncHennaCON.getInstance());
        this.addStatFunc(FuncHennaWIT.getInstance());
    }

    protected void initCharStatusUpdateValues() {
        super.initCharStatusUpdateValues();
        this._cpUpdateInterval = (double) this.getMaxCp() / (double) 352.0F;
        this._cpUpdateIncCheck = this.getMaxCp();
        this._cpUpdateDecCheck = (double) this.getMaxCp() - this._cpUpdateInterval;
        this._mpUpdateInterval = (double) this.getMaxMp() / (double) 352.0F;
        this._mpUpdateIncCheck = this.getMaxMp();
        this._mpUpdateDecCheck = (double) this.getMaxMp() - this._mpUpdateInterval;
    }

    public void initCharStat() {
        this.setStat(new PlayerStat(this));
    }

    public final PlayerStat getStat() {
        return (PlayerStat) super.getStat();
    }

    public void initCharStatus() {
        this.setStatus(new PlayerStatus(this));
    }

    public final PlayerStatus getStatus() {
        return (PlayerStatus) super.getStatus();
    }

    public final Appearance getAppearance() {
        return this._appearance;
    }

    public final PlayerTemplate getBaseTemplate() {
        return PlayerClassData.getInstance().getTemplate(this._baseClass);
    }

    public final PlayerTemplate getTemplate() {
        return (PlayerTemplate) super.getTemplate();
    }

    public void setTemplate(ClassId newclass) {
        super.setTemplate(PlayerClassData.getInstance().getTemplate(newclass));
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null) {
            synchronized (this) {
                if (this._ai == null) {
                    this._ai = new PlayerAI(this);
                }

                return this._ai;
            }
        } else {
            return ai;
        }
    }

    public final int getLevel() {
        return this.getStat().getLevel();
    }

    public boolean isNewbie() {
        return this.getClassId().level() <= 1 && this.getLevel() >= 6 && this.getLevel() <= 25;
    }

    public boolean isInStoreMode() {
        return this._storeType != StoreType.NONE;
    }

    public boolean isCrafting() {
        return this._isCrafting;
    }

    public void setCrafting(boolean state) {
        this._isCrafting = state;
    }

    public Collection<Recipe> getCommonRecipeBook() {
        return this._commonRecipeBook.values();
    }

    public Collection<Recipe> getDwarvenRecipeBook() {
        return this._dwarvenRecipeBook.values();
    }

    public void registerCommonRecipeList(Recipe recipe) {
        this._commonRecipeBook.put(recipe.getId(), recipe);
    }

    public void registerDwarvenRecipeList(Recipe recipe) {
        this._dwarvenRecipeBook.put(recipe.getId(), recipe);
    }

    public boolean hasRecipeList(int recipeId) {
        return this._dwarvenRecipeBook.containsKey(recipeId) || this._commonRecipeBook.containsKey(recipeId);
    }

    public void unregisterRecipeList(int recipeId) {
        if (this._dwarvenRecipeBook.containsKey(recipeId)) {
            this._dwarvenRecipeBook.remove(recipeId);
        } else if (this._commonRecipeBook.containsKey(recipeId)) {
            this._commonRecipeBook.remove(recipeId);
        }

        this.getShortcutList().deleteShortcuts(recipeId, ShortcutType.RECIPE);
    }

    public int getLastQuestNpcObject() {
        return this._questNpcObject;
    }

    public void setLastQuestNpcObject(int npcId) {
        this._questNpcObject = npcId;
    }

    public QuestState getQuestState(String name) {
        for (QuestState qs : this._quests) {
            if (name.equals(qs.getQuest().getName())) {
                return qs;
            }
        }

        return null;
    }

    public void setQuestState(QuestState qs) {
        this._quests.add(qs);
    }

    public void delQuestState(QuestState qs) {
        this._quests.remove(qs);
    }

    public List<Quest> getAllQuests(boolean completed) {
        List<Quest> quests = new ArrayList<>();

        for (QuestState qs : this._quests) {
            if (qs != null && (!completed || !qs.isCreated()) && (completed || qs.isStarted())) {
                Quest quest = qs.getQuest();
                if (quest != null && quest.isRealQuest()) {
                    quests.add(quest);
                }
            }
        }

        return quests;
    }

    public void processQuestEvent(String questName, String event) {
        Quest quest = ScriptData.getInstance().getQuest(questName);
        if (quest != null) {
            QuestState qs = this.getQuestState(questName);
            if (qs != null) {
                WorldObject object = World.getInstance().getObject(this.getLastQuestNpcObject());
                if (object instanceof Npc && this.isInsideRadius(object, 150, false, false)) {
                    Npc npc = (Npc) object;
                    List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.ON_TALK);
                    if (scripts != null) {
                        for (Quest script : scripts) {
                            if (script != null && script.equals(quest)) {
                                quest.notifyEvent(event, npc, this);
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    public void addNotifyQuestOfDeath(QuestState qs) {
        if (qs != null) {
            if (!this._notifyQuestOfDeathList.contains(qs)) {
                this._notifyQuestOfDeathList.add(qs);
            }

        }
    }

    public void removeNotifyQuestOfDeath(QuestState qs) {
        if (qs != null) {
            this._notifyQuestOfDeathList.remove(qs);
        }
    }

    public final List<QuestState> getNotifyQuestOfDeath() {
        return this._notifyQuestOfDeathList;
    }

    public PlayerMemo getMemos() {
        return this._vars;
    }

    public Map<String, PlayerVar> getDungeonVars() {
        return this._dungeonvars;
    }

    public ShortcutList getShortcutList() {
        return this._shortcutList;
    }

    public MacroList getMacroList() {
        return this._macroList;
    }

    public byte getSiegeState() {
        return this._siegeState;
    }

    public void setSiegeState(byte siegeState) {
        this._siegeState = siegeState;
    }

    public byte getPvpFlag() {
        return this._pvpFlag;
    }

    public void setPvpFlag(int pvpFlag) {
        this._pvpFlag = (byte) pvpFlag;
    }

    public void updatePvPFlag(int value) {
        if (this.getPvpFlag() != value) {
            if ((value == 0 || TvTEventManager.getInstance().getActiveEvent() == null || !TvTEventManager.getInstance().getActiveEvent().isInEvent(this)) && (value == 0 || CtfEventManager.getInstance().getActiveEvent() == null || !CtfEventManager.getInstance().getActiveEvent().isInEvent(this)) && (value == 0 || DmEventManager.getInstance().getActiveEvent() == null || !DmEventManager.getInstance().getActiveEvent().isInEvent(this))) {
                this.setPvpFlag(value);
                this.sendPacket(new UserInfo(this));
                if (this._summon != null) {
                    this.sendPacket(new RelationChanged(this._summon, this.getRelation(this), false));
                }

                this.broadcastRelationsChanges();
            } else {
                this.setPvpFlag(0);
                this.sendPacket(new UserInfo(this));
                if (this._summon != null) {
                    this.sendPacket(new RelationChanged(this._summon, this.getRelation(this), false));
                }

                this.broadcastRelationsChanges();
            }
        }
    }

    public int getRelation(Player target) {
        int result = 0;
        if (this.getPvpFlag() != 0) {
            result |= 2;
        }

        if (this.getKarma() > 0) {
            result |= 4;
        }

        if (this.isClanLeader()) {
            result |= 128;
        }

        if (this.getSiegeState() != 0) {
            result |= 512;
            if (this.getSiegeState() != target.getSiegeState()) {
                result |= 4096;
            } else {
                result |= 2048;
            }

            if (this.getSiegeState() == 1) {
                result |= 1024;
            }
        }

        if (this.getClan() != null && target.getClan() != null && target.getPledgeType() != -1 && this.getPledgeType() != -1 && target.getClan().isAtWarWith(this.getClan().getClanId())) {
            result |= 65536;
            if (this.getClan().isAtWarWith(target.getClan().getClanId())) {
                result |= 32768;
            }
        }

        return result;
    }

    public void revalidateZone(boolean force) {
        super.revalidateZone(force);
        if (Config.ALLOW_WATER) {
            if (this.isInsideZone(ZoneId.WATER)) {
                WaterTaskManager.getInstance().add(this);
            } else {
                WaterTaskManager.getInstance().remove(this);
            }
        }

        if (this.isInsideZone(ZoneId.SIEGE)) {
            if (this._lastCompassZone == 11) {
                return;
            }

            this._lastCompassZone = 11;
            this.sendPacket(new ExSetCompassZoneCode(11));
        } else if (this.isInsideZone(ZoneId.PVP)) {
            if (this._lastCompassZone == 14) {
                return;
            }

            this._lastCompassZone = 14;
            this.sendPacket(new ExSetCompassZoneCode(14));
        } else if (this.isIn7sDungeon()) {
            if (this._lastCompassZone == 13) {
                return;
            }

            this._lastCompassZone = 13;
            this.sendPacket(new ExSetCompassZoneCode(13));
        } else if (this.isInsideZone(ZoneId.PEACE)) {
            if (this._lastCompassZone == 12) {
                return;
            }

            this._lastCompassZone = 12;
            this.sendPacket(new ExSetCompassZoneCode(12));
        } else {
            if (this._lastCompassZone == 15) {
                return;
            }

            if (this._lastCompassZone == 11) {
                this.updatePvPStatus();
            }

            this._lastCompassZone = 15;
            this.sendPacket(new ExSetCompassZoneCode(15));
        }

    }

    public int getPkKills() {
        return this._pkKills;
    }

    public void setPkKills(int pkKills) {
        this._pkKills = pkKills;
    }

    public long getDeleteTimer() {
        return this._deleteTimer;
    }

    public void setDeleteTimer(long deleteTimer) {
        this._deleteTimer = deleteTimer;
    }

    public int getCurrentLoad() {
        return this._inventory.getTotalWeight();
    }

    public int getRecomHave() {
        return this._recomHave;
    }

    public void setRecomHave(int value) {
        this._recomHave = MathUtil.limit(value, 0, 255);
    }

    public void editRecomHave(int value) {
        this._recomHave = MathUtil.limit(this._recomHave + value, 0, 255);
    }

    public int getRecomLeft() {
        return this._recomLeft;
    }

    public void setRecomLeft(int value) {
        this._recomLeft = MathUtil.limit(value, 0, 9);
    }

    private void decRecomLeft() {
        if (this._recomLeft > 0) {
            --this._recomLeft;
        }

    }

    public List<Integer> getRecomChars() {
        return this._recomChars;
    }

    public void giveRecom(Player target) {
        target.editRecomHave(1);
        this.decRecomLeft();
        this._recomChars.add(target.getObjectId());

        try (Connection con = ConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)")) {
                ps.setInt(1, this.getObjectId());
                ps.setInt(2, target.getObjectId());
                ps.execute();
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET rec_have=? WHERE obj_Id=?")) {
                ps.setInt(1, target.getRecomHave());
                ps.setInt(2, target.getObjectId());
                ps.execute();
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET rec_left=? WHERE obj_Id=?")) {
                ps.setInt(1, this.getRecomLeft());
                ps.setInt(2, this.getObjectId());
                ps.execute();
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't update player recommendations.", e);
        }

    }

    public boolean canRecom(Player target) {
        return !this._recomChars.contains(target.getObjectId());
    }

    public long getExpBeforeDeath() {
        return this._expBeforeDeath;
    }

    public void setExpBeforeDeath(long exp) {
        this._expBeforeDeath = exp;
    }

    public int getKarma() {
        return this._karma;
    }

    public void setKarma(int karma) {
        if (karma < 0) {
            karma = 0;
        }

        if (this._karma > 0 && karma == 0) {
            this.sendPacket(new UserInfo(this));
            this.broadcastRelationsChanges();
        }

        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(karma));
        this._karma = karma;
        this.broadcastKarma();
    }

    public int getMaxLoad() {
        int con = this.getCON();
        if (con < 1) {
            return 31000;
        } else if (con > 59) {
            return 176000;
        } else {
            double baseLoad = Math.pow(1.029993928, con) * 30495.627366;
            return (int) this.calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
        }
    }

    public int getExpertiseArmorPenalty() {
        return this._expertiseArmorPenalty;
    }

    public boolean getExpertiseWeaponPenalty() {
        return this._expertiseWeaponPenalty;
    }

    public int getWeightPenalty() {
        return this._curWeightPenalty;
    }

    public void refreshOverloaded() {
        int maxLoad = this.getMaxLoad();
        if (maxLoad > 0) {
            int weightproc = this.getCurrentLoad() * 1000 / maxLoad;
            int newWeightPenalty;
            if (weightproc < 500) {
                newWeightPenalty = 0;
            } else if (weightproc < 666) {
                newWeightPenalty = 1;
            } else if (weightproc < 800) {
                newWeightPenalty = 2;
            } else if (weightproc < 1000) {
                newWeightPenalty = 3;
            } else {
                newWeightPenalty = 4;
            }

            if (this._curWeightPenalty != newWeightPenalty) {
                this._curWeightPenalty = newWeightPenalty;
                if (newWeightPenalty > 0) {
                    this.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty), false);
                    this.setIsOverloaded(this.getCurrentLoad() > maxLoad);
                } else {
                    this.removeSkill(4270, false);
                    this.setIsOverloaded(false);
                }

                this.sendPacket(new UserInfo(this));
                this.sendPacket(new EtcStatusUpdate(this));
                this.broadcastCharInfo();
            }
        }

    }

    public void refreshExpertisePenalty() {
        int expertiseLevel = this.getSkillLevel(239);
        int armorPenalty = 0;
        boolean weaponPenalty = false;

        for (ItemInstance item : this.getInventory().getPaperdollItems()) {
            if (item.getItemType() != EtcItemType.ARROW && item.getItem().getCrystalType().getId() > expertiseLevel) {
                if (item.isWeapon()) {
                    weaponPenalty = true;
                } else {
                    armorPenalty += item.getItem().getBodyPart() == 32768 ? 2 : 1;
                }
            }
        }

        armorPenalty = Math.min(armorPenalty, 4);
        if (this._expertiseWeaponPenalty != weaponPenalty || this._expertiseArmorPenalty != armorPenalty) {
            this._expertiseWeaponPenalty = weaponPenalty;
            this._expertiseArmorPenalty = armorPenalty;
            if (!this._expertiseWeaponPenalty && this._expertiseArmorPenalty <= 0) {
                this.removeSkill(4267, false);
            } else {
                this.addSkill(SkillTable.getInstance().getInfo(4267, 1), false);
            }

            this.sendSkillList();
            this.sendPacket(new EtcStatusUpdate(this));
            ItemInstance weapon = this.getInventory().getPaperdollItem(7);
            if (weapon != null) {
                if (this._expertiseWeaponPenalty) {
                    ItemPassiveSkillsListener.getInstance().onUnequip(0, weapon, this);
                } else {
                    ItemPassiveSkillsListener.getInstance().onEquip(0, weapon, this);
                }
            }
        }

    }

    public void useEquippableItem(ItemInstance item, boolean abortAttack) {
        ItemInstance[] items = null;
        boolean isEquipped = item.isEquipped();
        int oldInvLimit = this.getInventoryLimit();
        if (item.getItem() instanceof Weapon) {
            item.unChargeAllShots();
        }

        if (this.getWeaponSkinOption() > 0 && DressMeData.getInstance().getWeaponSkinsPackage(this.getWeaponSkinOption()) != null) {
            this.sendMessage("At first you must to remove a skin of weapon.");
            this.sendPacket(new ExShowScreenMessage("At first you must to remove a skin of weapon.", 2000));
            this.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
        } else if (this.getShieldSkinOption() > 0 && DressMeData.getInstance().getShieldSkinsPackage(this.getShieldSkinOption()) != null) {
            this.sendMessage("At first you must to remove a skin of weapon.");
            this.sendPacket(new ExShowScreenMessage("At first you must to remove a skin of weapon.", 2000));
            this.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
        } else {
            if (isEquipped) {
                if (item.getEnchantLevel() > 0) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
                } else {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));
                }

                items = this.getInventory().unEquipItemInBodySlotAndRecord(item);
            } else {
                items = this.getInventory().equipItemAndRecord(item);
                if (item.isEquipped()) {
                    if (item.getEnchantLevel() > 0) {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item));
                    } else {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(item));
                    }

                    if ((item.getItem().getBodyPart() & 16512) != 0) {
                        this.rechargeShots(true, true);
                    }
                } else {
                    this.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                }
            }

            this.refreshExpertisePenalty();
            this.broadcastUserInfo();
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItems(Arrays.asList(items));
            this.sendPacket(iu);
            if (abortAttack) {
                this.abortAttack();
            }

            if (this.getInventoryLimit() != oldInvLimit) {
                this.sendPacket(new ExStorageMaxCount(this));
            }

        }
    }

    public int getPvpKills() {
        return this._pvpKills;
    }

    public void setPvpKills(int pvpKills) {
        this._pvpKills = pvpKills;
    }

    public ClassId getClassId() {
        return this.getTemplate().getClassId();
    }

    public void setClassId(int Id) {
        if (this._subclassLock.tryLock()) {
            try {
                if (this.getLvlJoinedAcademy() != 0 && this._clan != null && ClassId.VALUES[Id].level() == 2) {
                    if (this.getLvlJoinedAcademy() <= 16) {
                        this._clan.addReputationScore(400);
                    } else if (this.getLvlJoinedAcademy() >= 39) {
                        this._clan.addReputationScore(170);
                    } else {
                        this._clan.addReputationScore(400 - (this.getLvlJoinedAcademy() - 16) * 10);
                    }

                    this.setLvlJoinedAcademy(0);
                    this._clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(this.getName()), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(this.getName()));
                    this._clan.removeClanMember(this.getObjectId(), 0L);
                    this.sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
                    this.addItem("Gift", 8181, 1, this, true);
                }

                if (this.isSubClassActive()) {
                    this._subClasses.get(this._classIndex).setClassId(Id);
                }

                this.broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
                this.setClassTemplate(Id);
                if (this.getClassId().level() == 3) {
                    this.sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
                } else {
                    this.sendPacket(SystemMessageId.CLASS_TRANSFER);
                }

                if (this._party != null) {
                    this._party.broadcastPacket(new PartySmallWindowUpdate(this));
                }

                if (this._clan != null) {
                    this._clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
                }

                if (Config.AUTO_LEARN_SKILLS) {
                    this.rewardSkills();
                }
            } finally {
                this._subclassLock.unlock();
            }

        }
    }

    public long getExp() {
        return this.getStat().getExp();
    }

    public ItemInstance getActiveEnchantItem() {
        return this._activeEnchantItem;
    }

    public void setActiveEnchantItem(ItemInstance scroll) {
        this._activeEnchantItem = scroll;
    }

    public ClassRace getRace() {
        return this.isSubClassActive() ? this.getBaseTemplate().getRace() : this.getTemplate().getRace();
    }

    public RadarList getRadarList() {
        return this._radarList;
    }

    public int getSp() {
        return this.getStat().getSp();
    }

    public boolean isCastleLord(int castleId) {
        Clan clan = this.getClan();
        if (clan != null && clan.getLeader().getPlayerInstance() == this) {
            Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
            return castle != null && castle.getCastleId() == castleId;
        } else {
            return false;
        }
    }

    public int getClanId() {
        return this._clanId;
    }

    public int getClanCrestId() {
        return this._clan != null ? this._clan.getCrestId() : 0;
    }

    public int getClanCrestLargeId() {
        return this._clan != null ? this._clan.getCrestLargeId() : 0;
    }

    public long getClanJoinExpiryTime() {
        return this._clanJoinExpiryTime;
    }

    public void setClanJoinExpiryTime(long time) {
        this._clanJoinExpiryTime = time;
    }

    public long getClanCreateExpiryTime() {
        return this._clanCreateExpiryTime;
    }

    public void setClanCreateExpiryTime(long time) {
        this._clanCreateExpiryTime = time;
    }

    public PcInventory getInventory() {
        return this._inventory;
    }

    public boolean isSitting() {
        return this._isSitting;
    }

    public void setSitting(boolean state) {
        this._isSitting = state;
    }

    public void sitDown() {
        this.sitDown(true);
    }

    public void sitDown(boolean checkCast) {
        if (!checkCast || !this.isCastingNow()) {
            if (!this._isSitting && !this.isAttackingDisabled() && !this.isOutOfControl() && !this.isImmobilized()) {
                this.breakAttack();
                this.setSitting(true);
                this.broadcastPacket(new ChangeWaitType(this, 0));
                this.getAI().setIntention(IntentionType.REST);
                ThreadPool.schedule(() -> this.setIsParalyzed(false), 2500L);
                this.setIsParalyzed(true);
            }

        }
    }

    public void standUp() {
        if (this._isSitting && !this.isInStoreMode() && !this.isAlikeDead() && !this.isParalyzed()) {
            if (this._effects.isAffected(L2EffectFlag.RELAXING)) {
                this.stopEffects(L2EffectType.RELAXING);
            }

            this.broadcastPacket(new ChangeWaitType(this, 1));
            ThreadPool.schedule(() -> {
                this.setSitting(false);
                this.setIsParalyzed(false);
                this.getAI().setIntention(IntentionType.IDLE);
            }, 2500L);
            this.setIsParalyzed(true);
        }

    }

    public void forceStandUp() {
        if (this.isInStoreMode()) {
            this.setStoreType(StoreType.NONE);
            this.broadcastUserInfo();
        }

        this.standUp();
    }

    public void tryToSitOrStand(WorldObject target, boolean sittingState) {
        if (this.isFakeDeath()) {
            this.stopFakeDeath(true);
        } else {
            boolean isThrone = target instanceof StaticObject && ((StaticObject) target).getType() == 1;
            if (isThrone && !sittingState && !this.isInsideRadius(target, 150, false, false)) {
                this.getAI().setIntention(IntentionType.MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));
                NextAction nextAction = new NextAction(AiEventType.ARRIVED, IntentionType.MOVE_TO, () -> {
                    if (this.getMountType() == 0) {
                        this.sitDown();
                        if (!((StaticObject) target).isBusy()) {
                            this._throneId = target.getObjectId();
                            ((StaticObject) target).setBusy(true);
                            this.broadcastPacket(new ChairSit(this.getObjectId(), ((StaticObject) target).getStaticObjectId()));
                        }

                    }
                });
                this.getAI().setNextAction(nextAction);
            } else {
                if (!this.isMoving()) {
                    if (this.getMountType() != 0) {
                        return;
                    }

                    if (sittingState) {
                        if (this._throneId != 0) {
                            WorldObject object = World.getInstance().getObject(this._throneId);
                            if (object instanceof StaticObject) {
                                ((StaticObject) object).setBusy(false);
                            }

                            this._throneId = 0;
                        }

                        this.standUp();
                    } else {
                        this.sitDown();
                        if (isThrone && !((StaticObject) target).isBusy() && this.isInsideRadius(target, 150, false, false)) {
                            this._throneId = target.getObjectId();
                            ((StaticObject) target).setBusy(true);
                            this.broadcastPacket(new ChairSit(this.getObjectId(), ((StaticObject) target).getStaticObjectId()));
                        }
                    }
                } else {
                    NextAction nextAction = new NextAction(AiEventType.ARRIVED, IntentionType.MOVE_TO, () -> {
                        if (this.getMountType() == 0) {
                            if (sittingState) {
                                if (this._throneId != 0) {
                                    WorldObject object = World.getInstance().getObject(this._throneId);
                                    if (object instanceof StaticObject) {
                                        ((StaticObject) object).setBusy(false);
                                    }

                                    this._throneId = 0;
                                }

                                this.standUp();
                            } else {
                                this.sitDown();
                                if (isThrone && !((StaticObject) target).isBusy() && this.isInsideRadius(target, 150, false, false)) {
                                    this._throneId = target.getObjectId();
                                    ((StaticObject) target).setBusy(true);
                                    this.broadcastPacket(new ChairSit(this.getObjectId(), ((StaticObject) target).getStaticObjectId()));
                                }
                            }

                        }
                    });
                    this.getAI().setNextAction(nextAction);
                }

            }
        }
    }

    public PcWarehouse getWarehouse() {
        if (this._warehouse == null) {
            this._warehouse = new PcWarehouse(this);
            this._warehouse.restore();
        }

        return this._warehouse;
    }

    public void clearWarehouse() {
        if (this._warehouse != null) {
            this._warehouse.deleteMe();
        }

        this._warehouse = null;
    }

    public PcFreight getFreight() {
        if (this._freight == null) {
            this._freight = new PcFreight(this);
            this._freight.restore();
        }

        return this._freight;
    }

    public void clearFreight() {
        if (this._freight != null) {
            this._freight.deleteMe();
        }

        this._freight = null;
    }

    public PcFreight getDepositedFreight(int objectId) {
        for (PcFreight freight : this._depositedFreight) {
            if (freight != null && freight.getOwnerId() == objectId) {
                return freight;
            }
        }

        PcFreight freight = new PcFreight(null);
        freight.doQuickRestore(objectId);
        this._depositedFreight.add(freight);
        return freight;
    }

    public void clearDepositedFreight() {
        for (PcFreight freight : this._depositedFreight) {
            if (freight != null) {
                freight.deleteMe();
            }
        }

        this._depositedFreight.clear();
    }

    public int getAdena() {
        return this._inventory.getAdena();
    }

    public int getAncientAdena() {
        return this._inventory.getAncientAdena();
    }

    public void addAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (sendMessage) {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));
        }

        if (count > 0) {
            this._inventory.addAdena(process, count, this, reference);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(this._inventory.getAdenaInstance());
            this.sendPacket(iu);
        }

    }

    public boolean reduceAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (count > this.getAdena()) {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            }

            return false;
        } else {
            if (count > 0) {
                ItemInstance adenaItem = this._inventory.getAdenaInstance();
                if (!this._inventory.reduceAdena(process, count, this, reference)) {
                    return false;
                }

                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(adenaItem);
                this.sendPacket(iu);
                if (sendMessage) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addNumber(count));
                }
            }

            return true;
        }
    }

    public void addAncientAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (sendMessage) {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(5575).addNumber(count));
        }

        if (count > 0) {
            this._inventory.addAncientAdena(process, count, this, reference);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(this._inventory.getAncientAdenaInstance());
            this.sendPacket(iu);
        }

    }

    public boolean reduceAncientAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (count > this.getAncientAdena()) {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            }

            return false;
        } else {
            if (count > 0) {
                ItemInstance ancientAdenaItem = this._inventory.getAncientAdenaInstance();
                if (!this._inventory.reduceAncientAdena(process, count, this, reference)) {
                    return false;
                }

                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(ancientAdenaItem);
                this.sendPacket(iu);
                if (sendMessage) {
                    if (count > 1) {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(5575).addItemNumber(count));
                    } else {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(5575));
                    }
                }
            }

            return true;
        }
    }

    public void addItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
        if (item.getCount() > 0) {
            if (sendMessage) {
                if (item.getCount() > 1) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(item).addNumber(item.getCount()));
                } else if (item.getEnchantLevel() > 0) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item));
                } else {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
                }
            }

            ItemInstance newitem = this._inventory.addItem(process, item, this, reference);
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(newitem);
            this.sendPacket(playerIU);
            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(14, this.getCurrentLoad());
            this.sendPacket(su);
            if (CursedWeaponManager.getInstance().isCursed(newitem.getItemId())) {
                CursedWeaponManager.getInstance().activate(this, newitem);
            } else if (item.getItem().getItemType() == EtcItemType.ARROW && this.getAttackType() == WeaponType.BOW && this.getInventory().getPaperdollItem(8) == null) {
                this.checkAndEquipArrows();
            }
        }

    }

    public ItemInstance addItem(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        if (count > 0) {
            Item item = ItemTable.getInstance().getTemplate(itemId);
            if (item == null) {
                return null;
            }

            if (sendMessage && (!this.isCastingNow() && item.getItemType() == EtcItemType.HERB || item.getItemType() != EtcItemType.HERB)) {
                if (count > 1) {
                    if (!process.equalsIgnoreCase("Sweep") && !process.equalsIgnoreCase("Quest")) {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(itemId).addItemNumber(count));
                    } else {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addItemNumber(count));
                    }
                } else if (!process.equalsIgnoreCase("Sweep") && !process.equalsIgnoreCase("Quest")) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId));
                } else {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
                }
            }

            if (item.getItemType() != EtcItemType.HERB) {
                ItemInstance createdItem = this._inventory.addItem(process, itemId, count, this, reference);
                if (CursedWeaponManager.getInstance().isCursed(createdItem.getItemId())) {
                    CursedWeaponManager.getInstance().activate(this, createdItem);
                } else if (item.getItemType() == EtcItemType.ARROW && this.getAttackType() == WeaponType.BOW && this.getInventory().getPaperdollItem(8) == null) {
                    this.checkAndEquipArrows();
                }

                return createdItem;
            }

            ItemInstance herb = new ItemInstance(0, itemId);
            IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getEtcItem());
            if (handler != null) {
                handler.useItem(this, herb, false);
            }
        }

        return null;
    }

    public boolean destroyItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
        return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
    }

    public boolean destroyItem(String process, ItemInstance item, int count, WorldObject reference, boolean sendMessage) {
        item = this._inventory.destroyItem(process, item, count, this, reference);
        if (item == null) {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
        } else {
            InventoryUpdate iu = new InventoryUpdate();
            if (item.getCount() == 0) {
                iu.addRemovedItem(item);
            } else {
                iu.addModifiedItem(item);
            }

            this.sendPacket(iu);
            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(14, this.getCurrentLoad());
            this.sendPacket(su);
            if (sendMessage) {
                if (count > 1) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addItemNumber(count));
                } else {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item));
                }
            }

            return true;
        }
    }

    public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        ItemInstance item = this._inventory.getItemByObjectId(objectId);
        if (item == null) {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
        } else {
            return this.destroyItem(process, item, count, reference, sendMessage);
        }
    }

    public boolean destroyItemWithoutTrace(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        ItemInstance item = this._inventory.getItemByObjectId(objectId);
        if (item != null && item.getCount() >= count) {
            return this.destroyItem(null, item, count, reference, sendMessage);
        } else {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
        }
    }

    public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        if (itemId == 57) {
            return this.reduceAdena(process, count, reference, sendMessage);
        } else {
            ItemInstance item = this._inventory.getItemByItemId(itemId);
            if (item != null && item.getCount() >= count && this._inventory.destroyItemByItemId(process, itemId, count, this, reference) != null) {
                InventoryUpdate playerIU = new InventoryUpdate();
                playerIU.addItem(item);
                this.sendPacket(playerIU);
                StatusUpdate su = new StatusUpdate(this);
                su.addAttribute(14, this.getCurrentLoad());
                this.sendPacket(su);
                if (sendMessage) {
                    if (count > 1) {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count));
                    } else {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
                    }
                }

                return true;
            } else {
                if (sendMessage) {
                    this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                }

                return false;
            }
        }
    }

    public ItemInstance transferItem(String process, int objectId, int count, Inventory target, WorldObject reference) {
        ItemInstance oldItem = this.checkItemManipulation(objectId, count);
        if (oldItem == null) {
            return null;
        } else {
            ItemInstance newItem = this.getInventory().transferItem(process, objectId, count, target, this, reference);
            if (newItem == null) {
                return null;
            } else {
                InventoryUpdate playerIU = new InventoryUpdate();
                if (oldItem.getCount() > 0 && oldItem != newItem) {
                    playerIU.addModifiedItem(oldItem);
                } else {
                    playerIU.addRemovedItem(oldItem);
                }

                this.sendPacket(playerIU);
                StatusUpdate playerSU = new StatusUpdate(this);
                playerSU.addAttribute(14, this.getCurrentLoad());
                this.sendPacket(playerSU);
                if (target instanceof PcInventory) {
                    Player targetPlayer = ((PcInventory) target).getOwner();
                    InventoryUpdate playerIU2 = new InventoryUpdate();
                    if (newItem.getCount() > count) {
                        playerIU2.addModifiedItem(newItem);
                    } else {
                        playerIU2.addNewItem(newItem);
                    }

                    targetPlayer.sendPacket(playerIU2);
                    playerSU = new StatusUpdate(targetPlayer);
                    playerSU.addAttribute(14, targetPlayer.getCurrentLoad());
                    targetPlayer.sendPacket(playerSU);
                } else if (target instanceof PetInventory) {
                    PetInventoryUpdate petIU = new PetInventoryUpdate();
                    if (newItem.getCount() > count) {
                        petIU.addModifiedItem(newItem);
                    } else {
                        petIU.addNewItem(newItem);
                    }

                    ((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
                }

                return newItem;
            }
        }
    }

    public boolean dropItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
        item = this._inventory.dropItem(process, item, this, reference);
        if (item == null) {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
        } else {
            item.dropMe(this, this.getX() + Rnd.get(-25, 25), this.getY() + Rnd.get(-25, 25), this.getZ() + 20);
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            this.sendPacket(playerIU);
            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(14, this.getCurrentLoad());
            this.sendPacket(su);
            if (sendMessage) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
            }

            return true;
        }
    }

    public ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, WorldObject reference, boolean sendMessage) {
        ItemInstance invItem = this._inventory.getItemByObjectId(objectId);
        ItemInstance item = this._inventory.dropItem(process, objectId, count, this, reference);
        if (item == null) {
            if (sendMessage) {
                this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return null;
        } else {
            if (this.getInstance() != null) {
                item.setInstance(this.getInstance(), true);
            } else {
                item.setInstance(this.getInstance(), false);
            }

            item.dropMe(this, x, y, z);
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(invItem);
            this.sendPacket(playerIU);
            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(14, this.getCurrentLoad());
            this.sendPacket(su);
            if (sendMessage) {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
            }

            return item;
        }
    }

    public ItemInstance checkItemManipulation(int objectId, int count) {
        if (World.getInstance().getObject(objectId) == null) {
            return null;
        } else {
            ItemInstance item = this.getInventory().getItemByObjectId(objectId);
            if (item != null && item.getOwnerId() == this.getObjectId()) {
                if (count >= 1 && (count <= 1 || item.isStackable())) {
                    if (count > item.getCount()) {
                        return null;
                    } else if ((this._summon == null || this._summon.getControlItemId() != objectId) && this._mountObjectId != objectId) {
                        if (this.getActiveEnchantItem() != null && this.getActiveEnchantItem().getObjectId() == objectId) {
                            return null;
                        } else {
                            return !item.isAugmented() || !this.isCastingNow() && !this.isCastingSimultaneouslyNow() ? item : null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public void setSpawnProtection(boolean protect) {
        if (protect) {
            if (this._protectTask == null) {
                this._protectTask = ThreadPool.schedule(() -> {
                    this.setSpawnProtection(false);
                    this.sendMessage("The spawn protection has ended.");
                }, Config.PLAYER_SPAWN_PROTECTION * 1000);
            }
        } else {
            this._protectTask.cancel(true);
            this._protectTask = null;
        }

        this.broadcastUserInfo();
    }

    public boolean isSpawnProtected() {
        return this._protectTask != null;
    }

    public void setRecentFakeDeath() {
        this._recentFakeDeathEndTime = System.currentTimeMillis() + (long) (Config.PLAYER_FAKEDEATH_UP_PROTECTION * 1000);
    }

    public void clearRecentFakeDeath() {
        this._recentFakeDeathEndTime = 0L;
    }

    public boolean isRecentFakeDeath() {
        return this._recentFakeDeathEndTime > System.currentTimeMillis();
    }

    public final boolean isFakeDeath() {
        return this._isFakeDeath;
    }

    public final void setIsFakeDeath(boolean value) {
        this._isFakeDeath = value;
    }

    public final boolean isAlikeDead() {
        return super.isAlikeDead() ? true : this.isFakeDeath();
    }

    public GameClient getClient() {
        return this._client;
    }

    public void setClient(GameClient client) {
        this._client = client;
    }

    public String getAccountName() {
        return this.getClient() == null ? this.getAccountNamePlayer() : this.getClient().getAccountName();
    }

    public String getAccountNamePlayer() {
        return this._accountName;
    }

    public Map<Integer, String> getAccountChars() {
        return this._chars;
    }

    public void logout(boolean closeClient) {
        if (this.getAgathion() != null) {
            this.despawnAgathion();
        }

        GameClient client = this._client;
        if (client != null) {
            if (client.isDetached()) {
                client.cleanMe(true);
            } else if (!client.getConnection().isClosed()) {
                client.close(closeClient ? LeaveWorld.STATIC_PACKET : ServerClose.STATIC_PACKET);
            }

        }
    }

    public Location getCurrentSkillWorldPosition() {
        return this._currentSkillWorldPosition;
    }

    public void setCurrentSkillWorldPosition(Location worldPosition) {
        this._currentSkillWorldPosition = worldPosition;
    }

    public void enableSkill(L2Skill skill) {
        super.enableSkill(skill);
        this._reuseTimeStamps.remove(skill.getReuseHashCode());
    }

    public boolean checkDoCastConditions(L2Skill skill) {
        if (!super.checkDoCastConditions(skill)) {
            return false;
        } else {
            if (skill.getSkillType() == L2SkillType.SUMMON) {
                if (!((L2SkillSummon) skill).isCubic() && (this._summon != null || this.isMounted())) {
                    this.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
                    return false;
                }
            } else if (skill.getSkillType() == L2SkillType.RESURRECT) {
                Siege siege = CastleManager.getInstance().getActiveSiege(this);
                if (siege != null) {
                    if (this.getClan() == null) {
                        this.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                        return false;
                    }

                    SiegeSide side = siege.getSide(this.getClan());
                    if (side != SiegeSide.DEFENDER && side != SiegeSide.OWNER) {
                        if (side != SiegeSide.ATTACKER) {
                            this.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                            return false;
                        }

                        if (this.getClan().getFlag() == null) {
                            this.sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP);
                            return false;
                        }
                    } else if (siege.getControlTowerCount() == 0) {
                        this.sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION);
                        return false;
                    }
                }
            } else if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME) {
                WorldRegion region = this.getRegion();
                if (region == null) {
                    return false;
                }

                if (!region.checkEffectRangeInsidePeaceZone(skill, skill.getTargetType() == SkillTargetType.TARGET_GROUND ? this.getCurrentSkillWorldPosition() : this.getPosition())) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
                    return false;
                }
            }

            if (!this.isInOlympiadMode() || !skill.isHeroSkill() && skill.getSkillType() != L2SkillType.RESURRECT) {
                if (skill.getMaxCharges() == 0 && this.getCharges() < skill.getNumCharges()) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
                    return false;
                } else {
                    return true;
                }
            } else {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
                return false;
            }
        }
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else {
            if (this.isInStoreMode()) {
                player.getAI().setIntention(IntentionType.INTERACT, this);
                return;
            }

            if (EngineModsManager.onInteract(player, (Creature) player.getTarget())) {
                return;
            }

            if (this.isAutoAttackable(player)) {
                if (this.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && this.getLevel() < 21) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }

                if (GeoEngine.getInstance().canSeeTarget(player, this)) {
                    player.getAI().setIntention(IntentionType.ATTACK, this);
                    player.onActionRequest();
                }
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                if (player != this && GeoEngine.getInstance().canSeeTarget(player, this)) {
                    player.getAI().setIntention(IntentionType.FOLLOW, this);
                }
            }
        }

    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            AdminEditChar.showCharacterInfo(player, this);
        }

        super.onActionShift(player);
    }

    private boolean needCpUpdate(int barPixels) {
        double currentCp = this.getCurrentCp();
        if (!(currentCp <= (double) 1.0F) && this.getMaxCp() >= barPixels) {
            if (!(currentCp <= this._cpUpdateDecCheck) && !(currentCp >= this._cpUpdateIncCheck)) {
                return false;
            } else {
                if (currentCp == (double) this.getMaxCp()) {
                    this._cpUpdateIncCheck = currentCp + (double) 1.0F;
                    this._cpUpdateDecCheck = currentCp - this._cpUpdateInterval;
                } else {
                    double doubleMulti = currentCp / this._cpUpdateInterval;
                    int intMulti = (int) doubleMulti;
                    this._cpUpdateDecCheck = this._cpUpdateInterval * (double) (doubleMulti < (double) intMulti ? intMulti-- : intMulti);
                    this._cpUpdateIncCheck = this._cpUpdateDecCheck + this._cpUpdateInterval;
                }

                return true;
            }
        } else {
            return true;
        }
    }

    private boolean needMpUpdate(int barPixels) {
        double currentMp = this.getCurrentMp();
        if (!(currentMp <= (double) 1.0F) && this.getMaxMp() >= barPixels) {
            if (!(currentMp <= this._mpUpdateDecCheck) && !(currentMp >= this._mpUpdateIncCheck)) {
                return false;
            } else {
                if (currentMp == (double) this.getMaxMp()) {
                    this._mpUpdateIncCheck = currentMp + (double) 1.0F;
                    this._mpUpdateDecCheck = currentMp - this._mpUpdateInterval;
                } else {
                    double doubleMulti = currentMp / this._mpUpdateInterval;
                    int intMulti = (int) doubleMulti;
                    this._mpUpdateDecCheck = this._mpUpdateInterval * (double) (doubleMulti < (double) intMulti ? intMulti-- : intMulti);
                    this._mpUpdateIncCheck = this._mpUpdateDecCheck + this._mpUpdateInterval;
                }

                return true;
            }
        } else {
            return true;
        }
    }

    public void broadcastStatusUpdate() {
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(9, (int) this.getCurrentHp());
        su.addAttribute(11, (int) this.getCurrentMp());
        su.addAttribute(33, (int) this.getCurrentCp());
        su.addAttribute(34, this.getMaxCp());
        this.sendPacket(su);
        boolean needCpUpdate = this.needCpUpdate(352);
        boolean needHpUpdate = this.needHpUpdate(352);
        if (this._party != null && (needCpUpdate || needHpUpdate || this.needMpUpdate(352))) {
            this._party.broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
        }

        if (this.isInOlympiadMode() && this.isOlympiadStart() && (needCpUpdate || needHpUpdate)) {
            OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(this.getOlympiadGameId());
            if (game != null && game.isBattleStarted()) {
                game.getZone().broadcastStatusUpdate(this);
            }
        }

        if (this.isInDuel() && (needCpUpdate || needHpUpdate)) {
            ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
            DuelManager.getInstance().broadcastToOppositeTeam(this, update);
        }

    }

    public void broadcastPacket(L2GameServerPacket packet, boolean selfToo) {
        if (selfToo) {
            this.sendPacket(packet);
        }

        super.broadcastPacket(packet, selfToo);
    }

    public void broadcastPacketInRadius(L2GameServerPacket packet, int radius) {
        this.sendPacket(packet);
        super.broadcastPacketInRadius(packet, radius);
    }

    public final void mikadoPlayerUpdate() {
        this.updateAbnormalEffect();
        this.sendPacket(new ItemList(this, false));
        this.sendPacket(new InventoryUpdate());
        this.sendPacket(new EtcStatusUpdate(this));
        this.refreshOverloaded();
        this.refreshExpertisePenalty();
        this.broadcastTitleInfo();
        this.sendPacket(new UserInfo(this));
        this.broadcastUserInfo();
    }

    public final void broadcastUserInfo() {
        this.sendPacket(new UserInfo(this));
        if (this.getPolyType() == PolyType.NPC) {
            this.broadcastPacket(new AbstractNpcInfo.PcMorphInfo(this, this.getPolyTemplate()), false);
        } else {
            this.broadcastCharInfo();
        }

    }

    public final void broadcastCharInfo() {
        for (Player player : this.getKnownType(Player.class)) {
            player.sendPacket(new CharInfo(this));
            int relation = this.getRelation(player);
            boolean isAutoAttackable = this.isAutoAttackable(player);
            player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
            if (this._summon != null) {
                player.sendPacket(new RelationChanged(this._summon, relation, isAutoAttackable));
            }
        }

    }

    public final void broadcastTitleInfo() {
        this.sendPacket(new UserInfo(this));
        this.broadcastPacket(new TitleUpdate(this));
    }

    public int getAllyId() {
        return this._clan == null ? 0 : this._clan.getAllyId();
    }

    public int getAllyCrestId() {
        if (this.getClanId() == 0) {
            return 0;
        } else {
            return this.getClan().getAllyId() == 0 ? 0 : this.getClan().getAllyCrestId();
        }
    }

    public void sendPacket(L2GameServerPacket packet) {
        if (this._client != null) {
            this._client.sendPacket(packet);
        }

    }

    public void sendPacket(SystemMessageId id) {
        this.sendPacket(SystemMessage.getSystemMessage(id));
    }

    public void doInteract(Creature target) {
        if (target instanceof Player) {
            Player temp = (Player) target;
            this.sendPacket(new MoveToPawn(this, temp, 150));
            switch (temp.getStoreType()) {
                case SELL:
                case PACKAGE_SELL:
                    this.sendPacket(new PrivateStoreListSell(this, temp));
                    break;
                case BUY:
                    this.sendPacket(new PrivateStoreListBuy(this, temp));
                    break;
                case MANUFACTURE:
                    this.sendPacket(new RecipeShopSellList(this, temp));
            }
        } else if (target != null) {
            target.onAction(this);
        }

    }

    public void doAutoLoot(Attackable target, IntIntHolder item) {
        if (this.isInParty()) {
            this.getParty().distributeItem(this, item, false, target);
        } else if (item.getId() == 57) {
            this.addAdena("Loot", item.getValue(), target, true);
        } else {
            this.addItem("Loot", item.getId(), item.getValue(), target, true);
        }

    }

    public void doPickupItem(WorldObject object) {
        if (!this.isAlikeDead() && !this.isFakeDeath()) {
            this.getAI().setIntention(IntentionType.IDLE);
            if (object instanceof ItemInstance) {
                ItemInstance item = (ItemInstance) object;
                this.sendPacket(ActionFailed.STATIC_PACKET);
                this.sendPacket(new StopMove(this));
                synchronized (item) {
                    if (!item.isVisible()) {
                        return;
                    }

                    if (this.isInStoreMode()) {
                        return;
                    }

                    if (!this._inventory.validateWeight(item.getCount() * item.getItem().getWeight())) {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
                        return;
                    }

                    if ((this.isInParty() && this.getParty().getLootRule() == LootRule.ITEM_LOOTER || !this.isInParty()) && !this._inventory.validateCapacity(item)) {
                        this.sendPacket(SystemMessageId.SLOTS_FULL);
                        return;
                    }

                    if (this.getActiveTradeList() != null) {
                        this.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
                        return;
                    }

                    if (item.getOwnerId() != 0 && !this.isLooterOrInLooterParty(item.getOwnerId())) {
                        if (item.getItemId() == 57) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(item.getCount()));
                        } else if (item.getCount() > 1) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(item).addNumber(item.getCount()));
                        } else {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item));
                        }

                        return;
                    }

                    if (item.hasDropProtection()) {
                        item.removeDropProtection();
                    }

                    item.pickupMe(this);
                    ItemsOnGroundTaskManager.getInstance().remove(item);
                }

                if (item.getItemType() == EtcItemType.HERB) {
                    IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                    if (handler != null) {
                        handler.useItem(this, item, false);
                    }

                    item.destroyMe("Consume", this, null);
                } else if (CursedWeaponManager.getInstance().isCursed(item.getItemId())) {
                    this.addItem("Pickup", item, null, true);
                } else {
                    if (item.getItemType() instanceof ArmorType || item.getItemType() instanceof WeaponType) {
                        SystemMessage msg;
                        if (item.getEnchantLevel() > 0) {
                            msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(this.getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
                        } else {
                            msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(this.getName()).addItemName(item.getItemId());
                        }

                        this.broadcastPacketInRadius(msg, 1400);
                    }

                    if (this.isInParty()) {
                        this.getParty().distributeItem(this, item);
                    } else if (item.getItemId() == 57 && this.getInventory().getAdenaInstance() != null) {
                        this.addAdena("Pickup", item.getCount(), null, true);
                        item.destroyMe("Pickup", this, null);
                    } else {
                        this.addItem("Pickup", item, null, true);
                    }
                }

                ThreadPool.schedule(() -> this.setIsParalyzed(false), (int) (700.0F / this.getStat().getMovementSpeedMultiplier()));
                this.setIsParalyzed(true);
            }
        }
    }

    public void doAttack(Creature target) {
        super.doAttack(target);
        this.clearRecentFakeDeath();
    }

    public void doCast(L2Skill skill) {
        super.doCast(skill);
        this.clearRecentFakeDeath();
    }

    public boolean canOpenPrivateStore() {
        if (this.getActiveTradeList() != null) {
            this.cancelActiveTrade();
        }

        return !this.isAlikeDead() && !this.isInOlympiadMode() && !this.isMounted() && !this.isInsideZone(ZoneId.NO_STORE) && !this.isCastingNow();
    }

    public void tryOpenPrivateBuyStore() {
        if (this.canOpenPrivateStore()) {
            if (this.getStoreType() == StoreType.BUY || this.getStoreType() == StoreType.BUY_MANAGE) {
                this.setStoreType(StoreType.NONE);
            }

            if (this.getStoreType() == StoreType.NONE) {
                this.standUp();
                this.setStoreType(StoreType.BUY_MANAGE);
                this.sendPacket(new PrivateStoreManageListBuy(this));
            }
        } else {
            if (this.isInsideZone(ZoneId.NO_STORE)) {
                this.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
            }

            this.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void tryOpenPrivateSellStore(boolean isPackageSale) {
        if (this.canOpenPrivateStore()) {
            if (this.getStoreType() == StoreType.SELL || this.getStoreType() == StoreType.SELL_MANAGE || this.getStoreType() == StoreType.PACKAGE_SELL) {
                this.setStoreType(StoreType.NONE);
            }

            if (this.getStoreType() == StoreType.NONE) {
                this.standUp();
                this.setStoreType(StoreType.SELL_MANAGE);
                this.sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
            }
        } else {
            if (this.isInsideZone(ZoneId.NO_STORE)) {
                this.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
            }

            this.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void tryOpenWorkshop(boolean isDwarven) {
        if (this.canOpenPrivateStore()) {
            if (this.isInStoreMode()) {
                this.setStoreType(StoreType.NONE);
            }

            if (this.getStoreType() == StoreType.NONE) {
                this.standUp();
                if (this.getCreateList() == null) {
                    this.setCreateList(new ManufactureList());
                }

                this.sendPacket(new RecipeShopManageList(this, isDwarven));
            }
        } else {
            if (this.isInsideZone(ZoneId.NO_STORE)) {
                this.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
            }

            this.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public final PreparedListContainer getMultiSell() {
        return this._currentMultiSell;
    }

    public final void setMultiSell(PreparedListContainer list) {
        this._currentMultiSell = list;
    }

    public void setTarget(WorldObject newTarget) {
        if (newTarget != null && !newTarget.isVisible() && (!(newTarget instanceof Player) || !this.isInParty() || !this._party.containsPlayer(newTarget))) {
            newTarget = null;
        }

        if (newTarget instanceof FestivalMonster && !this.isFestivalParticipant()) {
            newTarget = null;
        } else if (newTarget != null && this.isInParty() && this.getParty().isInDimensionalRift() && !this.getParty().getDimensionalRift().isInCurrentRoomZone(newTarget)) {
            newTarget = null;
        }

        WorldObject oldTarget = this.getTarget();
        if (oldTarget != null) {
            if (oldTarget.equals(newTarget)) {
                return;
            }

            if (oldTarget instanceof Creature) {
                ((Creature) oldTarget).removeStatusListener(this);
            }
        }

        if (newTarget instanceof StaticObject) {
            this.sendPacket(new MyTargetSelected(newTarget.getObjectId(), 0));
            this.sendPacket(new StaticObjectInfo((StaticObject) newTarget));
        } else if (newTarget instanceof Creature) {
            Creature target = (Creature) newTarget;
            if (newTarget.getObjectId() != this.getObjectId()) {
                this.sendPacket(new ValidateLocation(target));
            }

            this.sendPacket(new MyTargetSelected(target.getObjectId(), !target.isAutoAttackable(this) && !(target instanceof Summon) ? 0 : this.getLevel() - target.getLevel()));
            target.addStatusListener(this);
            StatusUpdate su = new StatusUpdate(target);
            su.addAttribute(10, target.getMaxHp());
            su.addAttribute(9, (int) target.getCurrentHp());
            this.sendPacket(su);
            this.broadcastPacket(new TargetSelected(this.getObjectId(), newTarget.getObjectId(), this.getX(), this.getY(), this.getZ()), false);
        }

        if (newTarget instanceof Folk) {
            this.setCurrentFolk((Folk) newTarget);
        } else if (newTarget == null) {
            this.sendPacket(ActionFailed.STATIC_PACKET);
            if (this.getTarget() != null) {
                this.broadcastPacket(new TargetUnselected(this));
                this.setCurrentFolk(null);
            }
        }

        super.setTarget(newTarget);
    }

    public ItemInstance getActiveWeaponInstance() {
        return this.getInventory().getPaperdollItem(7);
    }

    public Weapon getActiveWeaponItem() {
        ItemInstance weapon = this.getInventory().getPaperdollItem(7);
        return weapon == null ? this.getTemplate().getFists() : (Weapon) weapon.getItem();
    }

    public WeaponType getAttackType() {
        return this.getActiveWeaponItem().getItemType();
    }

    public boolean isWearingArmorType(ArmorType type) {
        ItemInstance armor = this.getInventory().getPaperdollItem(type == ArmorType.SHIELD ? 8 : 10);
        if (armor == null) {
            return type == ArmorType.NONE;
        } else {
            return armor.getItemType() instanceof ArmorType && armor.getItemType() == type;
        }
    }

    public boolean isUnderMarryRequest() {
        return this._isUnderMarryRequest;
    }

    public void setUnderMarryRequest(boolean state) {
        this._isUnderMarryRequest = state;
    }

    public int getCoupleId() {
        return this._coupleId;
    }

    public void setCoupleId(int coupleId) {
        this._coupleId = coupleId;
    }

    public void setRequesterId(int requesterId) {
        this._requesterId = requesterId;
    }

    public void engageAnswer(int answer) {
        if (this._isUnderMarryRequest && this._requesterId != 0) {
            Player requester = World.getInstance().getPlayer(this._requesterId);
            if (requester != null) {
                if (answer == 1) {
                    CoupleManager.getInstance().addCouple(requester, this);
                    WeddingManagerNpc.justMarried(requester, this);
                } else {
                    this.setUnderMarryRequest(false);
                    this.sendMessage("You declined your partner's marriage request.");
                    requester.setUnderMarryRequest(false);
                    requester.sendMessage("Your partner declined your marriage request.");
                }
            }

        }
    }

    public ItemInstance getSecondaryWeaponInstance() {
        return this.getInventory().getPaperdollItem(8);
    }

    public Item getSecondaryWeaponItem() {
        ItemInstance item = this.getInventory().getPaperdollItem(8);
        return item != null ? item.getItem() : null;
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (this.isMounted()) {
                this.stopFeed();
            }

            synchronized (this) {
                if (this.isFakeDeath()) {
                    this.stopFakeDeath(true);
                }
            }

            if (killer != null) {
                Player pk = killer.getActingPlayer();
                if (pk != null) {
                    EventListener.onKill(pk, this);
                }

                this.setExpBeforeDeath(0L);
                if (this.isCursedWeaponEquipped()) {
                    CursedWeaponManager.getInstance().drop(this._cursedWeaponEquippedId, killer);
                } else if (pk == null || !pk.isCursedWeaponEquipped()) {
                    this.onDieDropItem(killer);
                    if (!this.isInArena() && pk != null && pk.getClan() != null && this.getClan() != null && !this.isAcademyMember() && !pk.isAcademyMember() && this._clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(this._clan.getClanId())) {
                        if (this.getClan().getReputationScore() > 0) {
                            pk.getClan().addReputationScore(1);
                        }

                        if (pk.getClan().getReputationScore() > 0) {
                            this._clan.takeReputationScore(1);
                        }
                    }

                    if (Config.ALT_GAME_DELEVEL && (!this.hasSkill(194) || this.getStat().getLevel() > 9)) {
                        this.deathPenalty(pk != null && this.getClan() != null && pk.getClan() != null && (this.getClan().isAtWarWith(pk.getClanId()) || pk.getClan().isAtWarWith(this.getClanId())), pk != null, killer instanceof SiegeGuard);
                    }
                }
            }

            if (!this._cubics.isEmpty()) {
                for (Cubic cubic : this._cubics.values()) {
                    cubic.stopAction();
                    cubic.cancelDisappear();
                }

                this._cubics.clear();
            }

            if (this._fusionSkill != null) {
                this.abortCast();
            }

            for (Creature character : this.getKnownType(Creature.class)) {
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
                    character.abortCast();
                }
            }

            this.calculateDeathPenaltyBuffLevel(killer);
            WaterTaskManager.getInstance().remove(this);
            this.heroConsecutiveKillCount = 0;
            if (Config.WAR_LEGEND_AURA && !this.isHero() && this.isPVPHero) {
                this.setHeroAura(false);
                this.sendMessage("You leaved War Legend State");
            }

            if (Config.ENABLE_EFFECT_ON_DIE) {
                ExRedSky packet = new ExRedSky(5);
                this.sendPacket(packet);
                this.sendPacket(new Earthquake(this.getX(), this.getY(), this.getZ(), 80, 5));
                this.sendPacket(new PlaySound("skillsound7.pig_skill"));
            }

            if (this.isPhoenixBlessed() || this.isAffected(L2EffectFlag.CHARM_OF_COURAGE) && this.isInSiege()) {
                this.reviveRequest(this, null, false);
            }

            this.updateEffectIcons();
            return true;
        }
    }

    private void onDieDropItem(Creature killer) {
        if (killer != null) {
            Player pk = killer.getActingPlayer();
            if (this.getKarma() > 0 || pk == null || pk.getClan() == null || this.getClan() == null || !pk.getClan().isAtWarWith(this.getClanId())) {
                if ((!this.isInsideZone(ZoneId.PVP) || pk == null) && (!this.isGM() || Config.KARMA_DROP_GM)) {
                    boolean isKillerNpc = killer instanceof Npc;
                    int pkLimit = Config.KARMA_PK_LIMIT;
                    int dropEquip = 0;
                    int dropEquipWeapon = 0;
                    int dropItem = 0;
                    int dropLimit = 0;
                    int dropPercent = 0;
                    if (this.getKarma() > 0 && this.getPkKills() >= pkLimit) {
                        dropPercent = Config.KARMA_RATE_DROP;
                        dropEquip = Config.KARMA_RATE_DROP_EQUIP;
                        dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
                        dropItem = Config.KARMA_RATE_DROP_ITEM;
                        dropLimit = Config.KARMA_DROP_LIMIT;
                    } else if (isKillerNpc && this.getLevel() > 4 && !this.isFestivalParticipant()) {
                        dropPercent = Config.PLAYER_RATE_DROP;
                        dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
                        dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
                        dropItem = Config.PLAYER_RATE_DROP_ITEM;
                        dropLimit = Config.PLAYER_DROP_LIMIT;
                    }

                    if (dropPercent > 0 && Rnd.get(100) < dropPercent) {
                        int dropCount = 0;
                        int itemDropPercent = 0;

                        for (ItemInstance itemDrop : this.getInventory().getItems()) {
                            if (itemDrop.isDropable() && !itemDrop.isShadowItem() && itemDrop.getItemId() != 57 && itemDrop.getItem().getType2() != 3 && (this._summon == null || this._summon.getControlItemId() != itemDrop.getItemId()) && Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getItemId()) < 0 && Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) < 0) {
                                if (itemDrop.isEquipped()) {
                                    itemDropPercent = itemDrop.getItem().getType2() == 0 ? dropEquipWeapon : dropEquip;
                                    this.getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
                                } else {
                                    itemDropPercent = dropItem;
                                }

                                if (Rnd.get(100) < itemDropPercent) {
                                    this.dropItem("DieDrop", itemDrop, killer, true);
                                    ++dropCount;
                                    if (dropCount >= dropLimit) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    public void updateKarmaLoss(long exp) {
        if (!this.isCursedWeaponEquipped() && this.getKarma() > 0) {
            int karmaLost = Formulas.calculateKarmaLost(this.getLevel(), exp);
            if (karmaLost > 0) {
                this.setKarma(this.getKarma() - karmaLost);
            }
        }

    }

    public void onKillUpdatePvPKarma(Playable target) {
        if (target != null) {
            Player targetPlayer = target.getActingPlayer();
            if (targetPlayer != null && targetPlayer != this) {
                if (this.isCursedWeaponEquipped() && target instanceof Player) {
                    CursedWeaponManager.getInstance().increaseKills(this._cursedWeaponEquippedId);
                } else if (!this.isInDuel() || !targetPlayer.isInDuel()) {
                    if (this.isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)) {
                        if (target instanceof Player && this.getSiegeState() > 0 && targetPlayer.getSiegeState() > 0 && this.getSiegeState() != targetPlayer.getSiegeState()) {
                            Clan killerClan = this.getClan();
                            if (killerClan != null) {
                                killerClan.setSiegeKills(killerClan.getSiegeKills() + 1);
                            }

                            Clan targetClan = targetPlayer.getClan();
                            if (targetClan != null) {
                                targetClan.setSiegeDeaths(targetClan.getSiegeDeaths() + 1);
                            }
                        }

                    } else {
                        if (this.isInsideZone(ZoneId.RANDOMZONE)) {
                            RandomZoneManager.addKillsInZone(this);
                        }

                        if (this.checkIfPvP(target) || targetPlayer.getClan() != null && this.getClan() != null && this.getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(this.getClanId()) && targetPlayer.getPledgeType() != -1 && this.getPledgeType() != -1 || targetPlayer.getKarma() > 0 && Config.KARMA_AWARD_PK_KILL) {
                            if (target instanceof Player) {
                                this.setPvpKills(this.getPvpKills() + 1);
                                if (PvPEvent.getInstance().isActive() && this.isInsideZone(ZoneId.PVPEVENT)) {
                                    if (Config.ALLOW_SPECIAL_PVP_REWARD) {
                                        PvPEvent.pvpSpecialReward(this);
                                    }

                                    PvPEvent.addEventPvp(this);
                                }

                                L2MultiFunctionZone.givereward(this);
                                if (this.heroConsecutiveKillCount == Config.KILLS_TO_GET_WAR_LEGEND_AURA && Config.WAR_LEGEND_AURA) {
                                    this.setHeroAura(true);
                                    World.announceToOnlinePlayers("Player " + this.getName() + " becames War Legend with " + Config.KILLS_TO_GET_WAR_LEGEND_AURA + " PvP!!", true);
                                    this.sendPacket(new ExShowScreenMessage("you've earned hero aura", 3000, 2, true));
                                }

                                this.sendPacket(new UserInfo(this));
                            }
                        } else if (targetPlayer.getKarma() == 0 && targetPlayer.getPvpFlag() == 0) {
                            if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(this)) {
                                return;
                            }

                            if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(this)) {
                                return;
                            }

                            if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(this)) {
                                return;
                            }

                            if (target instanceof Player) {
                                this.setPkKills(this.getPkKills() + 1);
                            }

                            this.setKarma(this.getKarma() + Formulas.calculateKarmaGain(this.getPkKills(), target instanceof Summon));
                            this.sendPacket(new UserInfo(this));
                        }

                    }
                }
            }
        }
    }

    public void updatePvPStatus() {
        if (!this.isInsideZone(ZoneId.PVP)) {
            PvpFlagTaskManager.getInstance().add(this, Config.PVP_NORMAL_TIME);
            if (this.getPvpFlag() == 0) {
                this.updatePvPFlag(1);
            }

        }
    }

    public void updatePvPStatus(Creature target) {
        Player player = target.getActingPlayer();
        if (player != null) {
            if (!this.isInDuel() || player.getDuelId() != this.getDuelId()) {
                if ((!this.isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && player.getKarma() == 0) {
                    PvpFlagTaskManager.getInstance().add(this, this.checkIfPvP(player) ? (long) Config.PVP_PVP_TIME : (long) Config.PVP_NORMAL_TIME);
                    if (this.getPvpFlag() == 0) {
                        this.updatePvPFlag(1);
                    }
                }

            }
        }
    }

    public void restoreExp(double restorePercent) {
        if (this.getExpBeforeDeath() > 0L) {
            this.getStat().addExp((int) Math.round((double) (this.getExpBeforeDeath() - this.getExp()) * restorePercent / (double) 100.0F));
            this.setExpBeforeDeath(0L);
        }

    }

    public void deathPenalty(boolean atWar, boolean killedByPlayable, boolean killedBySiegeNpc) {
        if (this.isInsideZone(ZoneId.PVP)) {
            if (this.isInsideZone(ZoneId.SIEGE)) {
                if (this.isInSiege() && (killedByPlayable || killedBySiegeNpc)) {
                    return;
                }
            } else if (killedByPlayable) {
                return;
            }
        }

        int lvl = this.getLevel();
        double percentLost = 7.0F;
        if (this.getLevel() >= 76) {
            percentLost = 2.0F;
        } else if (this.getLevel() >= 40) {
            percentLost = 4.0F;
        }

        if (this.getKarma() > 0) {
            percentLost *= Config.RATE_KARMA_EXP_LOST;
        }

        if (this.isFestivalParticipant() || atWar || this.isInsideZone(ZoneId.SIEGE)) {
            percentLost /= 4.0F;
        }

        long lostExp = 0L;
        if (lvl < 81) {
            lostExp = Math.round((double) (this.getStat().getExpForLevel(lvl + 1) - this.getStat().getExpForLevel(lvl)) * percentLost / (double) 100.0F);
        } else {
            lostExp = Math.round((double) (this.getStat().getExpForLevel(81) - this.getStat().getExpForLevel(80)) * percentLost / (double) 100.0F);
        }

        this.setExpBeforeDeath(this.getExp());
        this.updateKarmaLoss(lostExp);
        this.getStat().addExp(-lostExp);
    }

    public int getPartyRoom() {
        return this._partyroom;
    }

    public void setPartyRoom(int id) {
        this._partyroom = id;
    }

    public boolean isInPartyMatchRoom() {
        return this._partyroom > 0;
    }

    public void removeMeFromPartyMatch() {
        PartyMatchWaitingList.getInstance().removePlayer(this);
        if (this._partyroom != 0) {
            PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(this._partyroom);
            if (room != null) {
                room.deleteMember(this);
            }
        }

    }

    public Summon getSummon() {
        return this._summon;
    }

    public void setSummon(Summon summon) {
        this._summon = summon;
    }

    public boolean hasPet() {
        return this._summon instanceof Pet;
    }

    public boolean hasServitor() {
        return this._summon instanceof Servitor;
    }

    public TamedBeast getTrainedBeast() {
        return this._tamedBeast;
    }

    public void setTrainedBeast(TamedBeast tamedBeast) {
        this._tamedBeast = tamedBeast;
    }

    public Request getRequest() {
        return this._request;
    }

    public Player getActiveRequester() {
        if (this._activeRequester != null && this._activeRequester.isRequestExpired() && this._activeTradeList == null) {
            this._activeRequester = null;
        }

        return this._activeRequester;
    }

    public void setActiveRequester(Player requester) {
        this._activeRequester = requester;
    }

    public boolean isProcessingRequest() {
        return this.getActiveRequester() != null || this._requestExpireTime > System.currentTimeMillis();
    }

    public boolean isProcessingTransaction() {
        return this.getActiveRequester() != null || this._activeTradeList != null || this._requestExpireTime > System.currentTimeMillis();
    }

    public void onTransactionRequest(Player partner) {
        this._requestExpireTime = System.currentTimeMillis() + 15000L;
        partner.setActiveRequester(this);
    }

    public boolean isRequestExpired() {
        return this._requestExpireTime <= System.currentTimeMillis();
    }

    public void onTransactionResponse() {
        this._requestExpireTime = 0L;
    }

    public ItemContainer getActiveWarehouse() {
        return this._activeWarehouse;
    }

    public void setActiveWarehouse(ItemContainer warehouse) {
        this._activeWarehouse = warehouse;
    }

    public TradeList getActiveTradeList() {
        return this._activeTradeList;
    }

    public void setActiveTradeList(TradeList tradeList) {
        this._activeTradeList = tradeList;
    }

    public void onTradeStart(Player partner) {
        this._activeTradeList = new TradeList(this);
        this._activeTradeList.setPartner(partner);
        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
        this.sendPacket(new TradeStart(this));
    }

    public void onTradeConfirm(Player partner) {
        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));
        partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
        this.sendPacket(TradePressOtherOk.STATIC_PACKET);
    }

    public void onTradeCancel(Player partner) {
        if (this._activeTradeList != null) {
            this._activeTradeList.lock();
            this._activeTradeList = null;
            this.sendPacket(new SendTradeDone(0));
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE).addString(partner.getName()));
        }
    }

    public void onTradeFinish(boolean successfull) {
        this._activeTradeList = null;
        this.sendPacket(new SendTradeDone(1));
        if (successfull) {
            this.sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
        }

    }

    public void startTrade(Player partner) {
        this.onTradeStart(partner);
        partner.onTradeStart(this);
    }

    public void cancelActiveTrade() {
        if (this._activeTradeList != null) {
            Player partner = this._activeTradeList.getPartner();
            if (partner != null) {
                partner.onTradeCancel(this);
            }

            this.onTradeCancel(this);
        }
    }

    public ManufactureList getCreateList() {
        return this._createList;
    }

    public void setCreateList(ManufactureList list) {
        this._createList = list;
    }

    public TradeList getSellList() {
        if (this._sellList == null) {
            this._sellList = new TradeList(this);
        }

        return this._sellList;
    }

    public TradeList getBuyList() {
        if (this._buyList == null) {
            this._buyList = new TradeList(this);
        }

        return this._buyList;
    }

    public StoreType getStoreType() {
        return this._storeType;
    }

    public void setStoreType(StoreType type) {
        this._storeType = type;
        if (ConfigData.OFFLINE_TRADE_ENABLE && type == StoreType.NONE && PlayerData.get(this).isOffline()) {
            PlayerData.get(this).setOffline(false);
            this.deleteMe();
        }

    }

    public boolean hasDwarvenCraft() {
        return this.hasSkill(172);
    }

    public boolean hasCommonCraft() {
        return this.hasSkill(1320);
    }

    public void giveSkills() {
        if (Config.AUTO_LEARN_SKILLS) {
            this.rewardSkills();
        } else {
            for (GeneralSkillNode skill : this.getAvailableAutoGetSkills()) {
                this.addSkill(skill.getSkill(), false);
            }

            if (this.getLevel() >= 10 && this.hasSkill(194)) {
                this.removeSkill(194, false);
            }

            this.removeInvalidSkills();
            this.sendSkillList();
        }

    }

    public void rewardSkills() {
        for (GeneralSkillNode skill : this.getAllAvailableSkills()) {
            this.addSkill(skill.getSkill(), skill.getCost() != 0);
        }

        if (this.getLevel() >= 10 && this.hasSkill(194)) {
            this.removeSkill(194, false);
        }

        this.removeInvalidSkills();
        this.sendSkillList();
    }

    private void removeInvalidSkills() {
        if (!this.getSkills().isEmpty()) {
            Map<Integer, Optional<GeneralSkillNode>> availableSkills = this.getTemplate().getSkills().stream().filter((s) -> s.getMinLvl() <= this.getLevel() + (s.getId() == 239 ? 0 : 9)).collect(Collectors.groupingBy(IntIntHolder::getId, Collectors.maxBy(COMPARE_SKILLS_BY_LVL)));

            for (L2Skill skill : this.getSkills().values()) {
                if (this.getTemplate().getSkills().stream().filter((s) -> s.getId() == skill.getId()).count() != 0L) {
                    Optional<GeneralSkillNode> tempSkill = availableSkills.get(skill.getId());
                    if (tempSkill == null) {
                        this.removeSkill(skill.getId(), true);
                    } else {
                        GeneralSkillNode availableSkill = tempSkill.get();
                        int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getId());
                        if (skill.getLevel() > maxLevel) {
                            if ((this.getLevel() < 76 || availableSkill.getValue() < maxLevel) && skill.getLevel() > availableSkill.getValue()) {
                                this.addSkill(availableSkill.getSkill(), true);
                            }
                        } else if (skill.getLevel() > availableSkill.getValue()) {
                            this.addSkill(availableSkill.getSkill(), true);
                        }
                    }
                }
            }

        }
    }

    private void regiveTemporarySkills() {
        if (this.isNoble()) {
            this.setNoble(true, false);
        }

        if (this.isHero()) {
            this.setHero(true);
        }

        if (this.getClan() != null) {
            this.getClan().addSkillEffects(this);
            if (this.getClan().getLevel() >= Config.MINIMUM_CLAN_LEVEL && this.isClanLeader()) {
                this.addSiegeSkills();
            }
        }

        this.getInventory().reloadEquippedItems();
        this.restoreDeathPenaltyBuffLevel();
    }

    public void addSiegeSkills() {
        for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(this.isNoble())) {
            this.addSkill(sk, false);
        }

    }

    public void removeSiegeSkills() {
        for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(this.isNoble())) {
            this.removeSkill(sk.getId(), false);
        }

    }

    public List<GeneralSkillNode> getAvailableAutoGetSkills() {
        List<GeneralSkillNode> result = new ArrayList<>();
        this.getTemplate().getSkills().stream().filter((s) -> s.getMinLvl() <= this.getLevel() && s.getCost() == 0).collect(Collectors.groupingBy(IntIntHolder::getId, Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) -> {
            if (this.getSkillLevel(i) < s.get().getValue()) {
                result.add(s.get());
            }

        });
        return result;
    }

    public List<GeneralSkillNode> getAvailableSkills() {
        List<GeneralSkillNode> result = new ArrayList<>();
        this.getTemplate().getSkills().stream().filter((s) -> s.getMinLvl() <= this.getLevel() && s.getCost() != 0).forEach((s) -> {
            if (this.getSkillLevel(s.getId()) == s.getValue() - 1) {
                result.add(s);
            }

        });
        return result;
    }

    public List<GeneralSkillNode> getAllAvailableSkills() {
        List<GeneralSkillNode> result = new ArrayList<>();
        (this.getTemplate().getSkills().stream().filter((s) -> s.getMinLvl() <= this.getLevel()).collect(Collectors.groupingBy(IntIntHolder::getId, Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))).forEach((i, s) -> {
            if (this.getSkillLevel(i) < s.get().getValue()) {
                result.add(s.get());
            }

        });
        return result;
    }

    public int getRequiredLevelForNextSkill() {
        return this.getTemplate().getSkills().stream().filter((s) -> s.getMinLvl() > this.getLevel() && s.getCost() != 0).min(COMPARE_SKILLS_BY_MIN_LVL).map(SkillNode::getMinLvl).orElse(0);
    }

    public Clan getClan() {
        return this._clan;
    }

    public void setClan(Clan clan) {
        this._clan = clan;
        this.setTitle("");
        if (clan == null) {
            this._clanId = 0;
            this._clanPrivileges = 0;
            this._pledgeType = 0;
            this._powerGrade = 0;
            this._lvlJoinedAcademy = 0;
            this._apprentice = 0;
            this._sponsor = 0;
        } else if (!clan.isMember(this.getObjectId())) {
            this.setClan(null);
        } else {
            this._clanId = clan.getClanId();
        }
    }

    public boolean isClanLeader() {
        return this._clan != null && this.getObjectId() == this._clan.getLeaderId();
    }

    protected void reduceArrowCount() {
        ItemInstance arrows = this.getInventory().getPaperdollItem(8);
        if (arrows != null) {
            InventoryUpdate iu = new InventoryUpdate();
            if (arrows.getCount() > 1) {
                synchronized (arrows) {
                    arrows.changeCount(null, -1, this, null);
                    arrows.setLastChange(ItemState.MODIFIED);
                    iu.addModifiedItem(arrows);
                    if (Rnd.get(10) < 1) {
                        arrows.updateDatabase();
                    }

                    this._inventory.refreshWeight();
                }
            } else {
                iu.addRemovedItem(arrows);
                this._inventory.destroyItem("Consume", arrows, this, null);
            }

            this.sendPacket(iu);
        }
    }

    protected boolean checkAndEquipArrows() {
        ItemInstance arrows = this.getInventory().findArrowForBow(this.getActiveWeaponItem());
        if (arrows == null) {
            return false;
        } else if (arrows.getLocation() == ItemLocation.PAPERDOLL) {
            return true;
        } else {
            this.getInventory().setPaperdollItem(8, arrows);
            this.sendPacket(new ItemList(this, false));
            return true;
        }
    }

    public boolean disarmWeapons() {
        if (this.isCursedWeaponEquipped()) {
            return false;
        } else {
            ItemInstance wpn = this.getInventory().getPaperdollItem(7);
            if (wpn != null) {
                ItemInstance[] unequipped = this.getInventory().unEquipItemInBodySlotAndRecord(wpn);
                InventoryUpdate iu = new InventoryUpdate();

                for (ItemInstance itm : unequipped) {
                    iu.addModifiedItem(itm);
                }

                this.sendPacket(iu);
                this.abortAttack();
                this.broadcastUserInfo();
                if (unequipped.length > 0) {
                    SystemMessage sm;
                    if (unequipped[0].getEnchantLevel() > 0) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
                    } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);
                    }

                    this.sendPacket(sm);
                }
            }

            ItemInstance sld = this.getInventory().getPaperdollItem(8);
            if (sld != null) {
                ItemInstance[] unequipped = this.getInventory().unEquipItemInBodySlotAndRecord(sld);
                InventoryUpdate iu = new InventoryUpdate();

                for (ItemInstance itm : unequipped) {
                    iu.addModifiedItem(itm);
                }

                this.sendPacket(iu);
                this.abortAttack();
                this.broadcastUserInfo();
                if (unequipped.length > 0) {
                    SystemMessage sm;
                    if (unequipped[0].getEnchantLevel() > 0) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
                    } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);
                    }

                    this.sendPacket(sm);
                }
            }

            return true;
        }
    }

    public boolean mount(Summon pet) {
        if (!this.disarmWeapons()) {
            return false;
        } else {
            this.setRunning();
            this.stopAllToggles();
            Ride mount = new Ride(this.getObjectId(), 1, pet.getTemplate().getNpcId());
            this.setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
            this._petTemplate = (PetTemplate) pet.getTemplate();
            this._petData = this._petTemplate.getPetDataEntry(pet.getLevel());
            this._mountObjectId = pet.getControlItemId();
            this.startFeed(pet.getNpcId());
            this.broadcastPacket(mount);
            this.broadcastUserInfo();
            pet.unSummon(this);
            return true;
        }
    }

    public boolean mount(int npcId, int controlItemId) {
        if (!this.disarmWeapons()) {
            return false;
        } else {
            this.setRunning();
            this.stopAllToggles();
            Ride mount = new Ride(this.getObjectId(), 1, npcId);
            if (this.setMount(npcId, this.getLevel(), mount.getMountType())) {
                this._petTemplate = (PetTemplate) NpcData.getInstance().getTemplate(npcId);
                this._petData = this._petTemplate.getPetDataEntry(this.getLevel());
                this._mountObjectId = controlItemId;
                this.broadcastPacket(mount);
                this.broadcastUserInfo();
                this.startFeed(npcId);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean mountPlayer(Summon summon) {
        if (summon instanceof Pet && summon.isMountable() && !this.isMounted() && !this.isBetrayed()) {
            if (this.isDead()) {
                this.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
                return false;
            }

            if (summon.isDead()) {
                this.sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
                return false;
            }

            if (summon.isInCombat() || summon.isRooted()) {
                this.sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
                return false;
            }

            if (this.isInCombat()) {
                this.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
                return false;
            }

            if (this.isSitting()) {
                this.sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
                return false;
            }

            if (this.isFishing()) {
                this.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
                return false;
            }

            if (this.isCursedWeaponEquipped()) {
                this.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
                return false;
            }

            if (!MathUtil.checkIfInRange(200, this, summon, true)) {
                this.sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT);
                return false;
            }

            if (((Pet) summon).checkHungryState()) {
                this.sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
                return false;
            }

            if (!summon.isDead() && !this.isMounted()) {
                this.mount(summon);
            }
        } else if (this.isMounted()) {
            if (this.getMountType() == 2 && this.isInsideZone(ZoneId.NO_LANDING)) {
                this.sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
                return false;
            }

            if (this.checkFoodState(this._petTemplate.getHungryLimit())) {
                this.sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
                return false;
            }

            this.dismount();
        }

        return true;
    }

    public boolean dismount() {
        this.sendPacket(new SetupGauge(GaugeColor.GREEN, 0));
        int petId = this._mountNpcId;
        if (this.setMount(0, 0, 0)) {
            this.stopFeed();
            this.broadcastPacket(new Ride(this.getObjectId(), 0, 0));
            this._petTemplate = null;
            this._petData = null;
            this._mountObjectId = 0;
            this.storePetFood(petId);
            this.broadcastUserInfo();
            return true;
        } else {
            return false;
        }
    }

    public void storePetFood(int petId) {
        if (this._controlItemId != 0 && petId != 0) {
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?");) {
                ps.setInt(1, this.getCurrentFeed());
                ps.setInt(2, this._controlItemId);
                ps.executeUpdate();
                this._controlItemId = 0;
            } catch (Exception e) {
                LOGGER.error("Couldn't store pet food data for {}.", e, this._controlItemId);
            }
        }

    }

    private synchronized void startFeed(int npcId) {
        this._canFeed = npcId > 0;
        if (this.isMounted()) {
            if (this._summon != null) {
                this.setCurrentFeed(((Pet) this._summon).getCurrentFed());
                this._controlItemId = this._summon.getControlItemId();
                this.sendPacket(new SetupGauge(GaugeColor.GREEN, this.getCurrentFeed() * 10000 / this.getFeedConsume(), this._petData.getMaxMeal() * 10000 / this.getFeedConsume()));
                if (!this.isDead()) {
                    this._mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000L, 10000L);
                }
            } else if (this._canFeed) {
                this.setCurrentFeed(this._petData.getMaxMeal());
                this.sendPacket(new SetupGauge(GaugeColor.GREEN, this.getCurrentFeed() * 10000 / this.getFeedConsume(), this._petData.getMaxMeal() * 10000 / this.getFeedConsume()));
                if (!this.isDead()) {
                    this._mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000L, 10000L);
                }
            }

        }
    }

    private synchronized void stopFeed() {
        if (this._mountFeedTask != null) {
            this._mountFeedTask.cancel(false);
            this._mountFeedTask = null;
        }

    }

    public PetTemplate getPetTemplate() {
        return this._petTemplate;
    }

    public PetDataEntry getPetDataEntry() {
        return this._petData;
    }

    public int getCurrentFeed() {
        return this._curFeed;
    }

    public void setCurrentFeed(int num) {
        this._curFeed = Math.min(num, this._petData.getMaxMeal());
        this.sendPacket(new SetupGauge(GaugeColor.GREEN, this.getCurrentFeed() * 10000 / this.getFeedConsume(), this._petData.getMaxMeal() * 10000 / this.getFeedConsume()));
    }

    private int getFeedConsume() {
        return this.isAttackingNow() ? this._petData.getMountMealInBattle() : this._petData.getMountMealInNormal();
    }

    public boolean checkFoodState(double state) {
        return this._canFeed ? (double) this.getCurrentFeed() < (double) this._petData.getMaxMeal() * state : false;
    }

    public long getUptime() {
        return System.currentTimeMillis() - this._uptime;
    }

    public void setUptime(long time) {
        this._uptime = time;
    }

    public boolean isInvul() {
        return super.isInvul() || this.isSpawnProtected();
    }

    public boolean isInParty() {
        return this._party != null;
    }

    public Party getParty() {
        return this._party;
    }

    public void setParty(Party party) {
        this._party = party;
    }

    public LootRule getLootRule() {
        return this._lootRule;
    }

    public void setLootRule(LootRule lootRule) {
        this._lootRule = lootRule;
    }

    public boolean isGM() {
        return this.getAccessLevel().isGm();
    }

    public void setAccountAccesslevel(int level) {
        LoginServerThread.getInstance().sendAccessLevel(this.getAccountName(), level);
    }

    public AccessLevel getAccessLevel() {
        return this._accessLevel;
    }

    public void setAccessLevel(int level) {
        AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level);
        if (accessLevel == null) {
            LOGGER.warn("An invalid access level {} has been granted for {}, therefore it has been reset.", level, this.toString());
            accessLevel = AdminData.getInstance().getAccessLevel(0);
        }

        this._accessLevel = accessLevel;
        if (level > 0) {
            this.setTitle(accessLevel.getName());
            if (level == AdminData.getInstance().getMasterAccessLevel()) {
                LOGGER.info("{} has logged in with Master access level.", this.getName());
            }
        }

        if (accessLevel.isGm()) {
            if (!AdminData.getInstance().isRegisteredAsGM(this)) {
                AdminData.getInstance().addGm(this, false);
            }
        } else {
            AdminData.getInstance().deleteGm(this);
        }

        this.getAppearance().setNameColor(accessLevel.getNameColor());
        this.getAppearance().setTitleColor(accessLevel.getTitleColor());
        this.broadcastUserInfo();
        PlayerInfoTable.getInstance().updatePlayerData(this, true);
    }

    public void updateAndBroadcastStatus(int broadcastType) {
        this.refreshOverloaded();
        this.refreshExpertisePenalty();
        if (broadcastType == 1) {
            this.sendPacket(new UserInfo(this));
        } else if (broadcastType == 2) {
            this.broadcastUserInfo();
        }

    }

    public void broadcastKarma() {
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(27, this.getKarma());
        this.sendPacket(su);
        if (this._summon != null) {
            this.sendPacket(new RelationChanged(this._summon, this.getRelation(this), false));
        }

        this.broadcastRelationsChanges();
    }

    public void setOnlineStatus(boolean isOnline, boolean updateInDb) {
        if (this._isOnline != isOnline) {
            this._isOnline = isOnline;
        }

        if (updateInDb) {
            this.updateOnlineStatus();
        }

    }

    public void setIsIn7sDungeon(boolean isIn7sDungeon) {
        this._isIn7sDungeon = isIn7sDungeon;
    }

    public void updateOnlineStatus() {
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");) {
            ps.setInt(1, this.isOnlineInt());
            ps.setLong(2, System.currentTimeMillis());
            ps.setInt(3, this.getObjectId());
            ps.execute();
        } catch (Exception e) {
            LOGGER.error("Couldn't set player online status.", e);
        }

    }

    public Forum getMemo() {
        if (this._forumMemo == null) {
            Forum forum = ForumsBBSManager.getInstance().getForumByName("MemoRoot");
            if (forum != null) {
                this._forumMemo = forum.getChildByName(this._accountName);
                if (this._forumMemo == null) {
                    this._forumMemo = ForumsBBSManager.getInstance().createNewForum(this._accountName, forum, 3, 3, this.getObjectId());
                }
            }
        }

        return this._forumMemo;
    }

    private void restoreCharData() {
        this.restoreSkills();
        this._macroList.restore();
        if (!this.isSubClassActive()) {
            this.restoreRecipeBook();
        }

        this._shortcutList.restore();
        this._hennaList.restore();
        this.restoreRecom();
    }

    private void storeRecipeBook() {
        if (!this.isSubClassActive()) {
            try (Connection con = ConnectionPool.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?")) {
                    ps.setInt(1, this.getObjectId());
                    ps.execute();
                }

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO character_recipebook (charId, recipeId) values(?,?)")) {
                    for (Recipe recipe : this.getCommonRecipeBook()) {
                        ps.setInt(1, this.getObjectId());
                        ps.setInt(2, recipe.getId());
                        ps.addBatch();
                    }

                    for (Recipe recipe : this.getDwarvenRecipeBook()) {
                        ps.setInt(1, this.getObjectId());
                        ps.setInt(2, recipe.getId());
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't store recipe book data.", e);
            }

        }
    }

    private void restoreRecipeBook() {
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT recipeId FROM character_recipebook WHERE charId=?");) {
            ps.setInt(1, this.getObjectId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = RecipeData.getInstance().getRecipeList(rs.getInt("recipeId"));
                    if (recipe.isDwarven()) {
                        this.registerDwarvenRecipeList(recipe);
                    } else {
                        this.registerCommonRecipeList(recipe);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore recipe book data.", e);
        }

    }

    public synchronized void store(boolean storeActiveEffects) {
        if (!this.isTryingSkin() || !this.isTryingSkinPremium()) {
            this.storeDressMeData();
        }

        this.storeCharBase();
        this.storeCharSub();
        this.storeEffect(storeActiveEffects);
        this.storeRecipeBook();
        this._vars.storeMe();
    }

    public void store() {
        this.store(true);
    }

    public void storeCharBase() {
        int currentClassIndex = this.getClassIndex();
        this._classIndex = 0;
        long exp = this.getStat().getExp();
        int level = this.getStat().getLevel();
        int sp = this.getStat().getSp();
        this._classIndex = currentClassIndex;

        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,pc_point=?,newbie_armor=? WHERE obj_id=?");) {
            ps.setInt(1, level);
            ps.setInt(2, this.getMaxHp());
            ps.setDouble(3, this.getCurrentHp());
            ps.setInt(4, this.getMaxCp());
            ps.setDouble(5, this.getCurrentCp());
            ps.setInt(6, this.getMaxMp());
            ps.setDouble(7, this.getCurrentMp());
            ps.setInt(8, this.getAppearance().getFace());
            ps.setInt(9, this.getAppearance().getHairStyle());
            ps.setInt(10, this.getAppearance().getHairColor());
            ps.setInt(11, this.getAppearance().getSex().ordinal());
            ps.setInt(12, this.getHeading());
            if (!this.isInObserverMode()) {
                ps.setInt(13, this.getX());
                ps.setInt(14, this.getY());
                ps.setInt(15, this.getZ());
            } else {
                ps.setInt(13, this._savedLocation.getX());
                ps.setInt(14, this._savedLocation.getY());
                ps.setInt(15, this._savedLocation.getZ());
            }

            ps.setLong(16, exp);
            ps.setLong(17, this.getExpBeforeDeath());
            ps.setInt(18, sp);
            ps.setInt(19, this.getKarma());
            ps.setInt(20, this.getPvpKills());
            ps.setInt(21, this.getPkKills());
            ps.setInt(22, this.getClanId());
            ps.setInt(23, this.getRace().ordinal());
            ps.setInt(24, this.getClassId().getId());
            ps.setLong(25, this.getDeleteTimer());
            ps.setString(26, this.getTitle());
            ps.setInt(27, this.getAccessLevel().getLevel());
            ps.setInt(28, this.isOnlineInt());
            ps.setInt(29, this.isIn7sDungeon() ? 1 : 0);
            ps.setInt(30, this.getClanPrivileges());
            ps.setInt(31, this.wantsPeace() ? 1 : 0);
            ps.setInt(32, this.getBaseClass());
            ps.setLong(33, this.getOnlineTime());
            ps.setInt(34, this._punishment.getType().ordinal());
            ps.setLong(35, this._punishment.getTimer());
            ps.setInt(36, this.isNoble() ? 1 : 0);
            ps.setLong(37, (long) this.getPowerGrade());
            ps.setInt(38, this.getPledgeType());
            ps.setInt(39, this.getLvlJoinedAcademy());
            ps.setLong(40, (long) this.getApprentice());
            ps.setLong(41, (long) this.getSponsor());
            ps.setInt(42, this.getAllianceWithVarkaKetra());
            ps.setLong(43, this.getClanJoinExpiryTime());
            ps.setLong(44, this.getClanCreateExpiryTime());
            ps.setString(45, this.getName());
            ps.setLong(46, (long) this.getDeathPenaltyBuffLevel());
            ps.setInt(47, this.getPcBangScore());
            ps.setInt(48, this.getReceiveNewbieArmor() ? 1 : 0);
            ps.setInt(49, this.getObjectId());
            ps.execute();
        } catch (Exception e) {
            LOGGER.error("Couldn't store player base data.", e);
        }

    }

    public long getOnlineTime() {
        long totalOnlineTime = this._onlineTime;
        if (this._onlineBeginTime > 0L) {
            totalOnlineTime += (System.currentTimeMillis() - this._onlineBeginTime) / 1000L;
        }

        return totalOnlineTime;
    }

    public void setOnlineTime(long time) {
        this._onlineTime = time;
        this._onlineBeginTime = System.currentTimeMillis();
    }

    private void storeCharSub() {
        if (!this._subClasses.isEmpty()) {
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?");) {
                for (SubClass subClass : this._subClasses.values()) {
                    ps.setLong(1, subClass.getExp());
                    ps.setInt(2, subClass.getSp());
                    ps.setInt(3, subClass.getLevel());
                    ps.setInt(4, subClass.getClassId());
                    ps.setInt(5, this.getObjectId());
                    ps.setInt(6, subClass.getClassIndex());
                    ps.addBatch();
                }

                ps.executeBatch();
            } catch (Exception e) {
                LOGGER.error("Couldn't store subclass data.", e);
            }

        }
    }

    private void storeEffect(boolean storeEffects) {
        if (Config.STORE_SKILL_COOLTIME) {
            try (Connection con = ConnectionPool.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?")) {
                    ps.setInt(1, this.getObjectId());
                    ps.setInt(2, this.getClassIndex());
                    ps.executeUpdate();
                }

                int buff_index = 0;
                List<Integer> storedSkills = new ArrayList<>();

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                    if (storeEffects) {
                        for (L2Effect effect : this.getAllEffects()) {
                            if (effect != null) {
                                switch (effect.getEffectType()) {
                                    case HEAL_OVER_TIME:
                                    case COMBAT_POINT_HEAL_OVER_TIME:
                                        break;
                                    default:
                                        L2Skill skill = effect.getSkill();
                                        if (!storedSkills.contains(skill.getReuseHashCode())) {
                                            storedSkills.add(skill.getReuseHashCode());
                                            if (!effect.isHerbEffect() && effect.getInUse() && !skill.isToggle()) {
                                                ps.setInt(1, this.getObjectId());
                                                ps.setInt(2, skill.getId());
                                                ps.setInt(3, skill.getLevel());
                                                ps.setInt(4, effect.getCount());
                                                ps.setInt(5, effect.getTime());
                                                Timestamp t = this._reuseTimeStamps.get(skill.getReuseHashCode());
                                                if (t != null && t.hasNotPassed()) {
                                                    ps.setLong(6, t.getReuse());
                                                    ps.setDouble(7, (double) t.getStamp());
                                                } else {
                                                    ps.setLong(6, 0L);
                                                    ps.setDouble(7, (double) 0.0F);
                                                }

                                                ps.setInt(8, 0);
                                                ps.setInt(9, this.getClassIndex());
                                                ++buff_index;
                                                ps.setInt(10, buff_index);
                                                ps.addBatch();
                                            }
                                        }
                                }
                            }
                        }
                    }

                    for (Map.Entry<Integer, Timestamp> entry : this._reuseTimeStamps.entrySet()) {
                        int hash = entry.getKey();
                        if (!storedSkills.contains(hash)) {
                            Timestamp t = entry.getValue();
                            if (t != null && t.hasNotPassed()) {
                                storedSkills.add(hash);
                                ps.setInt(1, this.getObjectId());
                                ps.setInt(2, t.getId());
                                ps.setInt(3, t.getValue());
                                ps.setInt(4, -1);
                                ps.setInt(5, -1);
                                ps.setLong(6, t.getReuse());
                                ps.setDouble(7, (double) t.getStamp());
                                ps.setInt(8, 1);
                                ps.setInt(9, this.getClassIndex());
                                ++buff_index;
                                ps.setInt(10, buff_index);
                                ps.addBatch();
                            }
                        }
                    }

                    ps.executeBatch();
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't store player effects.", e);
            }

        }
    }

    public boolean isOnline() {
        return this._isOnline;
    }

    public int isOnlineInt() {
        if (this._isOnline && this.getClient() != null) {
            return this.getClient().isDetached() ? 2 : 1;
        } else {
            return 0;
        }
    }

    public boolean isIn7sDungeon() {
        return this._isIn7sDungeon;
    }

    public boolean addSkill(L2Skill newSkill, boolean store) {
        return this.addSkill(newSkill, store, false);
    }

    public boolean addSkill(L2Skill newSkill, boolean store, boolean updateShortcuts) {
        if (newSkill == null) {
            return false;
        } else {
            L2Skill oldSkill = this.getSkills().get(newSkill.getId());
            if (oldSkill != null && oldSkill.equals(newSkill)) {
                return false;
            } else {
                this.getSkills().put(newSkill.getId(), newSkill);
                if (oldSkill != null) {
                    if (oldSkill.triggerAnotherSkill()) {
                        this.removeSkill(oldSkill.getTriggeredId(), false);
                    }

                    this.removeStatsByOwner(oldSkill);
                }

                this.addStatFuncs(newSkill.getStatFuncs(this));
                if (oldSkill != null && this.getChanceSkills() != null) {
                    this.removeChanceSkill(oldSkill.getId());
                }

                if (newSkill.isChance()) {
                    this.addChanceTrigger(newSkill);
                }

                if (store) {
                    this.storeSkill(newSkill, -1);
                }

                if (updateShortcuts) {
                    this.getShortcutList().refreshShortcuts(newSkill.getId(), newSkill.getLevel(), ShortcutType.SKILL);
                }

                return true;
            }
        }
    }

    public L2Skill removeSkill(int skillId, boolean store) {
        return this.removeSkill(skillId, store, true);
    }

    public L2Skill removeSkill(int skillId, boolean store, boolean removeEffect) {
        L2Skill oldSkill = this.getSkills().remove(skillId);
        if (oldSkill == null) {
            return null;
        } else {
            if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0) {
                this.removeSkill(oldSkill.getTriggeredId(), false);
            }

            if (this.getLastSkillCast() != null && this.isCastingNow() && skillId == this.getLastSkillCast().getId()) {
                this.abortCast();
            }

            if (this.getLastSimultaneousSkillCast() != null && this.isCastingSimultaneouslyNow() && skillId == this.getLastSimultaneousSkillCast().getId()) {
                this.abortCast();
            }

            if (removeEffect) {
                this.removeStatsByOwner(oldSkill);
                this.stopSkillEffects(skillId);
            }

            if (oldSkill.isChance() && this.getChanceSkills() != null) {
                this.removeChanceSkill(skillId);
            }

            if (store) {
                try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");) {
                    ps.setInt(1, skillId);
                    ps.setInt(2, this.getObjectId());
                    ps.setInt(3, this.getClassIndex());
                    ps.execute();
                } catch (Exception e) {
                    LOGGER.error("Couldn't delete player skill.", e);
                }

                if (!oldSkill.isPassive()) {
                    this.getShortcutList().deleteShortcuts(skillId, ShortcutType.SKILL);
                }
            }

            return oldSkill;
        }
    }

    private void storeSkill(L2Skill skill, int classIndex) {
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)");) {
            ps.setInt(1, this.getObjectId());
            ps.setInt(2, skill.getId());
            ps.setInt(3, skill.getLevel());
            ps.setInt(4, classIndex > -1 ? classIndex : this._classIndex);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Couldn't store player skill.", e);
        }

    }

    private void restoreSkills() {
        EngineModsManager.onRestoreSkills(this);
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");) {
            ps.setInt(1, this.getObjectId());
            ps.setInt(2, this.getClassIndex());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    this.addSkill(SkillTable.getInstance().getInfo(rs.getInt("skill_id"), rs.getInt("skill_level")), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore player skills.", e);
        }

    }

    public void restoreEffects() {
        try (Connection con = ConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC")) {
                ps.setInt(1, this.getObjectId());
                ps.setInt(2, this.getClassIndex());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int effectCount = rs.getInt("effect_count");
                        int effectCurTime = rs.getInt("effect_cur_time");
                        long reuseDelay = rs.getLong("reuse_delay");
                        long systime = rs.getLong("systime");
                        int restoreType = rs.getInt("restore_type");
                        L2Skill skill = SkillTable.getInstance().getInfo(rs.getInt("skill_id"), rs.getInt("skill_level"));
                        if (skill != null) {
                            long remainingTime = systime - System.currentTimeMillis();
                            if (remainingTime > 10L) {
                                this.disableSkill(skill, remainingTime);
                                this.addTimeStamp(skill, reuseDelay, systime);
                            }

                            if (restoreType <= 0 && skill.hasEffects()) {
                                Env env = new Env();
                                env.setCharacter(this);
                                env.setTarget(this);
                                env.setSkill(skill);

                                for (EffectTemplate et : skill.getEffectTemplates()) {
                                    L2Effect ef = et.getEffect(env);
                                    if (ef != null) {
                                        ef.setCount(effectCount);
                                        ef.setFirstTime(effectCurTime);
                                        ef.scheduleEffect();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?")) {
                ps.setInt(1, this.getObjectId());
                ps.setInt(2, this.getClassIndex());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore effects.", e);
        }

    }

    private void restoreRecom() {
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT char_id,target_id FROM character_recommends WHERE char_id=?");) {
            ps.setInt(1, this.getObjectId());

            try (ResultSet rset = ps.executeQuery()) {
                while (rset.next()) {
                    this._recomChars.add(rset.getInt("target_id"));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore recommendations.", e);
        }

    }

    public HennaList getHennaList() {
        return this._hennaList;
    }

    public boolean isAutoAttackable(Creature attacker) {
        if (attacker != this && attacker != this._summon) {
            if (attacker instanceof Monster) {
                return true;
            } else if (attacker instanceof Player && EventListener.isAutoAttackable((Player) attacker, this)) {
                return true;
            } else if (this._party != null && this._party.containsPlayer(attacker)) {
                return false;
            } else {
                if (attacker instanceof Playable) {
                    if (this.isInsideZone(ZoneId.PEACE)) {
                        return false;
                    }

                    Player cha = attacker.getActingPlayer();
                    if (attacker instanceof Player && cha.isInOlympiadMode()) {
                        return this.isInOlympiadMode() && this.isOlympiadStart() && cha.getOlympiadGameId() == this.getOlympiadGameId();
                    }

                    if (this.getDuelState() == DuelState.DUELLING && this.getDuelId() == cha.getDuelId()) {
                        return true;
                    }

                    if (this.getClan() != null) {
                        Siege siege = CastleManager.getInstance().getActiveSiege(this);
                        if (siege != null) {
                            if (siege.checkSides(cha.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER) && siege.checkSides(this.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER)) {
                                return false;
                            }

                            if (siege.checkSide(cha.getClan(), SiegeSide.ATTACKER) && siege.checkSide(this.getClan(), SiegeSide.ATTACKER)) {
                                return false;
                            }
                        }

                        if (this.getClan().isAtWarWith(cha.getClanId()) && !this.wantsPeace() && !cha.wantsPeace() && !this.isAcademyMember()) {
                            return true;
                        }
                    }

                    if (this.isInArena() && attacker.isInArena()) {
                        return true;
                    }

                    if (this.getAllyId() != 0 && this.getAllyId() == cha.getAllyId()) {
                        return false;
                    }

                    if (this.getClan() != null && this.getClan().isMember(cha.getObjectId())) {
                        return false;
                    }

                    if (this.isInsideZone(ZoneId.PVP) && attacker.isInsideZone(ZoneId.PVP)) {
                        return true;
                    }
                } else if (attacker instanceof SiegeGuard && this.getClan() != null) {
                    Siege siege = CastleManager.getInstance().getActiveSiege(this);
                    return siege != null && siege.checkSide(this.getClan(), SiegeSide.ATTACKER);
                }

                return this.getKarma() > 0 || this.getPvpFlag() > 0;
            }
        } else {
            return false;
        }
    }

    public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove) {
        if (skill.isPassive()) {
            this.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        } else if (this.isSkillDisabled(skill)) {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
            return false;
        } else if (this.isWearingFormalWear()) {
            this.sendPacket(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
            this.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        } else if (this.isCastingNow()) {
            if (this._currentSkill.getSkill() != null && skill.getId() != this._currentSkill.getSkillId()) {
                this.setQueuedSkill(skill, forceUse, dontMove);
            }

            this.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        } else {
            this.setIsCastingNow(true);
            this.setCurrentSkill(skill, forceUse, dontMove);
            if (this._queuedSkill.getSkill() != null) {
                this.setQueuedSkill(null, false, false);
            }

            if (!this.checkUseMagicConditions(skill, forceUse, dontMove)) {
                this.setIsCastingNow(false);
                return false;
            } else {
                WorldObject target = null;
                Object var5;
                switch (skill.getTargetType()) {
                    case TARGET_AURA:
                    case TARGET_FRONT_AURA:
                    case TARGET_BEHIND_AURA:
                    case TARGET_GROUND:
                    case TARGET_SELF:
                    case TARGET_CORPSE_ALLY:
                    case TARGET_AURA_UNDEAD:
                        var5 = this;
                        break;
                    default:
                        var5 = skill.getFirstOfTargetList(this);
                }

                this.getAI().setIntention(IntentionType.CAST, skill, var5);
                return true;
            }
        }
    }

    private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove) {
        if (!this.isDead() && !this.isOutOfControl()) {
            L2SkillType sklType = skill.getSkillType();
            if (this.isFishing() && sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING) {
                this.sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
                return false;
            } else if (this.isInObserverMode()) {
                this.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
                this.abortCast();
                this.sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            } else if (this.isSitting()) {
                this.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
                this.sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            } else {
                if (skill.isToggle()) {
                    L2Effect effect = this.getFirstEffect(skill.getId());
                    if (effect != null) {
                        if (skill.getId() != 60) {
                            effect.exit();
                        }

                        this.sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                    }
                }

                if (this.isFakeDeath()) {
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                } else {
                    WorldObject target = null;
                    L2Skill.SkillTargetType sklTargetType = skill.getTargetType();
                    Location worldPosition = this.getCurrentSkillWorldPosition();
                    if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null) {
                        this.sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                    } else {
                        switch (sklTargetType) {
                            case TARGET_AURA:
                            case TARGET_FRONT_AURA:
                            case TARGET_BEHIND_AURA:
                            case TARGET_GROUND:
                            case TARGET_SELF:
                            case TARGET_CORPSE_ALLY:
                            case TARGET_AURA_UNDEAD:
                            case TARGET_PARTY:
                            case TARGET_ALLY:
                            case TARGET_CLAN:
                            case TARGET_AREA_SUMMON:
                                target = this;
                                break;
                            case TARGET_PET:
                            case TARGET_SUMMON:
                                target = this._summon;
                                break;
                            default:
                                target = this.getTarget();
                        }

                        if (target == null) {
                            this.sendPacket(ActionFailed.STATIC_PACKET);
                            return false;
                        } else if (!(target instanceof Door) || target.isAutoAttackable(this) && (!((Door) target).isUnlockable() || skill.getSkillType() == L2SkillType.UNLOCK)) {
                            if (this.isInDuel() && target instanceof Playable) {
                                Player cha = target.getActingPlayer();
                                if (cha.getDuelId() != this.getDuelId()) {
                                    this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    this.sendPacket(ActionFailed.STATIC_PACKET);
                                    return false;
                                }
                            }

                            if (skill.isSiegeSummonSkill()) {
                                Siege siege = CastleManager.getInstance().getActiveSiege(this);
                                if (siege == null || !siege.checkSide(this.getClan(), SiegeSide.ATTACKER) || this.isInSiege() && this.isInsideZone(ZoneId.CASTLE)) {
                                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_CALL_PET_FROM_THIS_LOCATION));
                                    return false;
                                }

                                if (SevenSignsManager.getInstance().isSealValidationPeriod() && SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE) == CabalType.DAWN && SevenSignsManager.getInstance().getPlayerCabal(this.getObjectId()) == CabalType.DUSK) {
                                    this.sendPacket(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING);
                                    return false;
                                }
                            }

                            if (!skill.checkCondition(this, target, false)) {
                                this.sendPacket(ActionFailed.STATIC_PACKET);
                                return false;
                            } else {
                                if (skill.isOffensive()) {
                                    if (isInsidePeaceZone(this, target)) {
                                        this.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
                                        this.sendPacket(ActionFailed.STATIC_PACKET);
                                        return false;
                                    }

                                    if (this.isInOlympiadMode() && !this.isOlympiadStart()) {
                                        this.sendPacket(ActionFailed.STATIC_PACKET);
                                        return false;
                                    }

                                    if (!target.isAttackable() && !this.getAccessLevel().allowPeaceAttack()) {
                                        this.sendPacket(ActionFailed.STATIC_PACKET);
                                        return false;
                                    }

                                    if (!target.isAutoAttackable(this) && !forceUse) {
                                        switch (sklTargetType) {
                                            case TARGET_AURA:
                                            case TARGET_FRONT_AURA:
                                            case TARGET_BEHIND_AURA:
                                            case TARGET_GROUND:
                                            case TARGET_SELF:
                                            case TARGET_CORPSE_ALLY:
                                            case TARGET_AURA_UNDEAD:
                                            case TARGET_PARTY:
                                            case TARGET_ALLY:
                                            case TARGET_CLAN:
                                            case TARGET_AREA_SUMMON:
                                                break;
                                            default:
                                                this.sendPacket(ActionFailed.STATIC_PACKET);
                                                return false;
                                        }
                                    }

                                    if (dontMove) {
                                        if (sklTargetType == SkillTargetType.TARGET_GROUND) {
                                            if (!this.isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), (int) ((double) skill.getCastRange() + this.getCollisionRadius()), false, false)) {
                                                this.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                                                this.sendPacket(ActionFailed.STATIC_PACKET);
                                                return false;
                                            }
                                        } else if (skill.getCastRange() > 0 && !this.isInsideRadius(target, (int) ((double) skill.getCastRange() + this.getCollisionRadius()), false, false)) {
                                            this.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                                            this.sendPacket(ActionFailed.STATIC_PACKET);
                                            return false;
                                        }
                                    }
                                }

                                if (!skill.isOffensive() && target instanceof Monster && !forceUse) {
                                    switch (sklTargetType) {
                                        case TARGET_AURA:
                                        case TARGET_FRONT_AURA:
                                        case TARGET_BEHIND_AURA:
                                        case TARGET_GROUND:
                                        case TARGET_SELF:
                                        case TARGET_CORPSE_ALLY:
                                        case TARGET_AURA_UNDEAD:
                                        case TARGET_PARTY:
                                        case TARGET_ALLY:
                                        case TARGET_CLAN:
                                        case TARGET_PET:
                                        case TARGET_SUMMON:
                                        case TARGET_CORPSE_MOB:
                                        case TARGET_AREA_CORPSE_MOB:
                                            break;
                                        case TARGET_AREA_SUMMON:
                                        default:
                                            switch (sklType) {
                                                case BEAST_FEED:
                                                case DELUXE_KEY_UNLOCK:
                                                case UNLOCK:
                                                    break;
                                                default:
                                                    this.sendPacket(ActionFailed.STATIC_PACKET);
                                                    return false;
                                            }
                                    }
                                }

                                if (sklType == L2SkillType.SPOIL && !(target instanceof Monster)) {
                                    this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    this.sendPacket(ActionFailed.STATIC_PACKET);
                                    return false;
                                } else {
                                    if (sklType == L2SkillType.SWEEP && target instanceof Monster && ((Monster) target).isDead()) {
                                        int spoilerId = ((Monster) target).getSpoilerId();
                                        if (spoilerId == 0) {
                                            this.sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
                                            this.sendPacket(ActionFailed.STATIC_PACKET);
                                            return false;
                                        }

                                        if (!this.isLooterOrInLooterParty(spoilerId)) {
                                            this.sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
                                            this.sendPacket(ActionFailed.STATIC_PACKET);
                                            return false;
                                        }
                                    }

                                    if (sklType == L2SkillType.DRAIN_SOUL && !(target instanceof Monster)) {
                                        this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                        this.sendPacket(ActionFailed.STATIC_PACKET);
                                        return false;
                                    } else {
                                        switch (sklTargetType) {
                                            case TARGET_AURA:
                                            case TARGET_FRONT_AURA:
                                            case TARGET_BEHIND_AURA:
                                            case TARGET_GROUND:
                                            case TARGET_SELF:
                                            case TARGET_CORPSE_ALLY:
                                            case TARGET_AURA_UNDEAD:
                                            case TARGET_PARTY:
                                            case TARGET_ALLY:
                                            case TARGET_CLAN:
                                            case TARGET_AREA_SUMMON:
                                                break;
                                            default:
                                                if (!this.checkPvpSkill(target, skill) && !this.getAccessLevel().allowPeaceAttack()) {
                                                    this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                                                    this.sendPacket(ActionFailed.STATIC_PACKET);
                                                    return false;
                                                }
                                        }

                                        if ((sklTargetType != SkillTargetType.TARGET_HOLY || this.checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill, target)) && (sklType != L2SkillType.SIEGEFLAG || L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false)) && (sklType != L2SkillType.STRSIEGEASSAULT || this.checkIfOkToUseStriderSiegeAssault(skill)) && (sklType != L2SkillType.SUMMON_FRIEND || this.checkSummonerStatus() && this.checkSummonTargetStatus(target))) {
                                            if (skill.getCastRange() > 0) {
                                                if (sklTargetType == SkillTargetType.TARGET_GROUND) {
                                                    if (!GeoEngine.getInstance().canSeeTarget(this, worldPosition)) {
                                                        this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                                                        this.sendPacket(ActionFailed.STATIC_PACKET);
                                                        return false;
                                                    }
                                                } else if (!GeoEngine.getInstance().canSeeTarget(this, target)) {
                                                    this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                                                    this.sendPacket(ActionFailed.STATIC_PACKET);
                                                    return false;
                                                }
                                            }

                                            return true;
                                        } else {
                                            this.sendPacket(ActionFailed.STATIC_PACKET);
                                            this.abortCast();
                                            return false;
                                        }
                                    }
                                }
                            }
                        } else {
                            this.sendPacket(SystemMessageId.INCORRECT_TARGET);
                            this.sendPacket(ActionFailed.STATIC_PACKET);
                            return false;
                        }
                    }
                }
            }
        } else {
            this.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }
    }

    public boolean checkIfOkToUseStriderSiegeAssault(L2Skill skill) {
        Siege siege = CastleManager.getInstance().getActiveSiege(this);
        SystemMessage sm;
        if (!this.isRiding()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        } else if (!(this.getTarget() instanceof Door)) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
        } else {
            if (siege != null && siege.checkSide(this.getClan(), SiegeSide.ATTACKER)) {
                return true;
            }

            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        }

        this.sendPacket(sm);
        return false;
    }

    public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill, WorldObject target) {
        SystemMessage sm;
        if (castle != null && castle.getCastleId() > 0) {
            if (!castle.isGoodArtifact(target)) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
            } else if (!castle.getSiege().isInProgress()) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
            } else if (!MathUtil.checkIfInRange(200, this, target, true)) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
            } else if (!this.isInsideZone(ZoneId.CAST_ON_ARTIFACT)) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
            } else {
                if (castle.getSiege().checkSide(this.getClan(), SiegeSide.ATTACKER)) {
                    if (!isCheckOnly) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
                        castle.getSiege().announceToPlayers(sm, false);
                    }

                    return true;
                }

                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
            }
        } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        }

        this.sendPacket(sm);
        return false;
    }

    public boolean isLooterOrInLooterParty(int objectId) {
        if (objectId == this.getObjectId()) {
            return true;
        } else {
            Player looter = World.getInstance().getPlayer(objectId);
            if (looter == null) {
                return false;
            } else if (this._party == null) {
                return false;
            } else {
                CommandChannel channel = this._party.getCommandChannel();
                return channel != null ? channel.containsPlayer(looter) : this._party.containsPlayer(looter);
            }
        }
    }

    public boolean checkPvpSkill(WorldObject target, L2Skill skill) {
        if (skill != null && target != null) {
            if (!(target instanceof Playable)) {
                return true;
            } else if (target instanceof Player && !EventListener.canAttack(this, (Player) target)) {
                return false;
            } else if (target instanceof Player && EventListener.canAttack(this, (Player) target)) {
                return true;
            } else if (!skill.isDebuff() && !skill.isOffensive()) {
                return true;
            } else {
                Player targetPlayer = target.getActingPlayer();
                if (targetPlayer != null && this != target) {
                    if (target.isInsideZone(ZoneId.PEACE)) {
                        return false;
                    } else if (this.isInDuel() && targetPlayer.isInDuel() && this.getDuelId() == targetPlayer.getDuelId()) {
                        return true;
                    } else {
                        boolean isCtrlPressed = this.getCurrentSkill() != null && this.getCurrentSkill().isCtrlPressed() || this.getCurrentPetSkill() != null && this.getCurrentPetSkill().isCtrlPressed();
                        if (this.isInParty() && targetPlayer.isInParty()) {
                            if (this.getParty().getLeader() == targetPlayer.getParty().getLeader()) {
                                if (skill.getEffectRange() > 0 && isCtrlPressed && this.getTarget() == target && skill.isDamage()) {
                                    return true;
                                }

                                return false;
                            }

                            if (this.getParty().getCommandChannel() != null && this.getParty().getCommandChannel().containsPlayer(targetPlayer)) {
                                if (skill.getEffectRange() > 0 && isCtrlPressed && this.getTarget() == target && skill.isDamage()) {
                                    return true;
                                }

                                return false;
                            }
                        }

                        if (this.isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)) {
                            return true;
                        } else if (this.isInOlympiadMode() && targetPlayer.isInOlympiadMode() && this.getOlympiadGameId() == targetPlayer.getOlympiadGameId()) {
                            return true;
                        } else {
                            Clan aClan = this.getClan();
                            Clan tClan = targetPlayer.getClan();
                            if (aClan != null && tClan != null) {
                                if (aClan.isAtWarWith(tClan.getClanId()) && tClan.isAtWarWith(aClan.getClanId())) {
                                    if (skill.getEffectRange() > 0 && isCtrlPressed && this.getTarget() == target && skill.isAOE()) {
                                        return true;
                                    }

                                    return isCtrlPressed;
                                }

                                if (this.getClanId() == targetPlayer.getClanId() || this.getAllyId() > 0 && this.getAllyId() == targetPlayer.getAllyId()) {
                                    return skill.getEffectRange() > 0 && isCtrlPressed && this.getTarget() == target && skill.isDamage();
                                }
                            }

                            if (targetPlayer.getPvpFlag() == 0 && targetPlayer.getKarma() == 0) {
                                if (skill.getEffectRange() > 0 && isCtrlPressed && this.getTarget() == target && skill.isDamage()) {
                                    return true;
                                } else {
                                    return false;
                                }
                            } else if (targetPlayer.getPvpFlag() <= 0 && targetPlayer.getKarma() <= 0) {
                                return false;
                            } else {
                                return true;
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean isMageClass() {
        return this.getClassId().getType() != ClassType.FIGHTER;
    }

    public boolean isMounted() {
        return this._mountType > 0;
    }

    public boolean setMount(int npcId, int npcLevel, int mountType) {
        switch (mountType) {
            case 0:
                if (this.isFlying()) {
                    this.removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
                }
                break;
            case 2:
                this.addSkill(FrequentSkill.WYVERN_BREATH.getSkill(), false);
        }

        this._mountNpcId = npcId;
        this._mountType = mountType;
        this._mountLevel = npcLevel;
        this.sendSkillList();
        return true;
    }

    public boolean isSeated() {
        return this._throneId > 0;
    }

    public boolean isRiding() {
        return this._mountType == 1;
    }

    public boolean isFlying() {
        return this._mountType == 2;
    }

    public int getMountType() {
        return this._mountType;
    }

    public final void stopAllEffects() {
        super.stopAllEffects();
        this.updateAndBroadcastStatus(2);
    }

    public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
        super.stopAllEffectsExceptThoseThatLastThroughDeath();
        this.updateAndBroadcastStatus(2);
    }

    public final void stopAllToggles() {
        this._effects.stopAllToggles();
    }

    public final void stopCubics() {
        if (this.getCubics() != null) {
            boolean removed = false;

            for (Cubic cubic : this.getCubics().values()) {
                cubic.stopAction();
                this.delCubic(cubic.getId());
                removed = true;
            }

            if (removed) {
                this.broadcastUserInfo();
            }
        }

    }

    public final void stopCubicsByOthers() {
        if (this.getCubics() != null) {
            boolean removed = false;

            for (Cubic cubic : this.getCubics().values()) {
                if (cubic.givenByOther()) {
                    cubic.stopAction();
                    this.delCubic(cubic.getId());
                    removed = true;
                }
            }

            if (removed) {
                this.broadcastUserInfo();
            }
        }

    }

    public void updateAbnormalEffect() {
        this.broadcastUserInfo();
    }

    public void tempInventoryDisable() {
        this._inventoryDisable = true;
        ThreadPool.schedule(() -> this._inventoryDisable = false, 1500L);
    }

    public boolean isInventoryDisabled() {
        return this._inventoryDisable;
    }

    public Map<Integer, Cubic> getCubics() {
        return this._cubics;
    }

    public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifetime, boolean givenByOther) {
        this._cubics.put(id, new Cubic(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther));
    }

    public void delCubic(int id) {
        this._cubics.remove(id);
    }

    public Cubic getCubic(int id) {
        return this._cubics.get(id);
    }

    public String toString() {
        String var10000 = this.getName();
        return var10000 + " (" + this.getObjectId() + ")";
    }

    public int getEnchantEffect() {
        ItemInstance wpn = this.getActiveWeaponInstance();
        return wpn == null ? 0 : Math.min(127, wpn.getEnchantLevel());
    }

    public Folk getCurrentFolk() {
        return this._currentFolk;
    }

    public void setCurrentFolk(Folk folk) {
        this._currentFolk = folk;
    }

    public boolean isFestivalParticipant() {
        return FestivalOfDarknessManager.getInstance().isParticipant(this);
    }

    public void addAutoSoulShot(int itemId) {
        this._activeSoulShots.add(itemId);
    }

    public boolean removeAutoSoulShot(int itemId) {
        return this._activeSoulShots.remove(itemId);
    }

    public Set<Integer> getAutoSoulShot() {
        return this._activeSoulShots;
    }

    public boolean isChargedShot(ShotType type) {
        ItemInstance weapon = this.getActiveWeaponInstance();
        return weapon != null && weapon.isChargedShot(type);
    }

    public void setChargedShot(ShotType type, boolean charged) {
        ItemInstance weapon = this.getActiveWeaponInstance();
        if (weapon != null) {
            weapon.setChargedShot(type, charged);
        }

    }

    public void rechargeShots(boolean physical, boolean magic) {
        if (!this._activeSoulShots.isEmpty()) {
            for (int itemId : this._activeSoulShots) {
                ItemInstance item = this.getInventory().getItemByItemId(itemId);
                if (item != null) {
                    if (magic && item.getItem().getDefaultAction() == ActionType.spiritshot) {
                        IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                        if (handler != null) {
                            handler.useItem(this, item, false);
                        }
                    }

                    if (physical && item.getItem().getDefaultAction() == ActionType.soulshot) {
                        IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                        if (handler != null) {
                            handler.useItem(this, item, false);
                        }
                    }
                } else {
                    this.removeAutoSoulShot(itemId);
                }
            }

        }
    }

    public boolean disableAutoShot(int itemId) {
        if (this._activeSoulShots.contains(itemId)) {
            this.removeAutoSoulShot(itemId);
            this.sendPacket(new ExAutoSoulShot(itemId, 0));
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
            return true;
        } else {
            return false;
        }
    }

    public void disableAutoShotsAll() {
        for (int itemId : this._activeSoulShots) {
            this.sendPacket(new ExAutoSoulShot(itemId, 0));
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
        }

        this._activeSoulShots.clear();
    }

    public int getClanPrivileges() {
        return this._clanPrivileges;
    }

    public void setClanPrivileges(int n) {
        this._clanPrivileges = n;
    }

    public int getPledgeClass() {
        return this._pledgeClass;
    }

    public void setPledgeClass(int classId) {
        this._pledgeClass = classId;
    }

    public int getPledgeType() {
        return this._pledgeType;
    }

    public void setPledgeType(int typeId) {
        this._pledgeType = typeId;
    }

    public int getApprentice() {
        return this._apprentice;
    }

    public void setApprentice(int id) {
        this._apprentice = id;
    }

    public int getSponsor() {
        return this._sponsor;
    }

    public void setSponsor(int id) {
        this._sponsor = id;
    }

    public void sendMessage(String message) {
        this.sendPacket(SystemMessage.sendString(message));
    }

    public void dropAllSummons() {
        if (this._summon != null) {
            this._summon.unSummon(this);
        }

        if (this._tamedBeast != null) {
            this._tamedBeast.deleteMe();
        }

        this.stopCubics();
    }

    public void enterObserverMode(int x, int y, int z) {
        this.dropAllSummons();
        if (this.getParty() != null) {
            this.getParty().removePartyMember(this, MessageType.EXPELLED);
        }

        this.standUp();
        this._savedLocation.set(this.getPosition());
        this.setTarget(null);
        this.setIsInvul(true);
        this.getAppearance().setInvisible();
        this.setIsParalyzed(true);
        this.startParalyze();
        this.teleportTo(x, y, z, 0);
        this.sendPacket(new ObservationMode(x, y, z));
    }

    public void enterOlympiadObserverMode(int id) {
        OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(id);
        if (task != null) {
            this.dropAllSummons();
            if (this.getParty() != null) {
                this.getParty().removePartyMember(this, MessageType.EXPELLED);
            }

            this._olympiadGameId = id;
            this.standUp();
            if (!this.isInObserverMode()) {
                this._savedLocation.set(this.getPosition());
            }

            this.setTarget(null);
            this.setIsInvul(true);
            this.getAppearance().setInvisible();
            this.teleportTo(task.getZone().getLocs().get(2), 0);
            this.sendPacket(new ExOlympiadMode(3));
        }
    }

    public void leaveObserverMode() {
        if (this.hasAI()) {
            this.getAI().setIntention(IntentionType.IDLE);
        }

        this.setTarget(null);
        this.getAppearance().setVisible();
        this.setIsInvul(false);
        this.setIsParalyzed(false);
        this.stopParalyze();
        this.sendPacket(new ObservationReturn(this._savedLocation));
        this.teleportTo(this._savedLocation, 0);
        this._savedLocation.clean();
    }

    public void leaveOlympiadObserverMode() {
        if (this._olympiadGameId != -1) {
            this._olympiadGameId = -1;
            if (this.hasAI()) {
                this.getAI().setIntention(IntentionType.IDLE);
            }

            this.setTarget(null);
            this.getAppearance().setVisible();
            this.setIsInvul(false);
            this.sendPacket(new ExOlympiadMode(0));
            this.teleportTo(this._savedLocation, 0);
            this._savedLocation.clean();
        }
    }

    public int getOlympiadSide() {
        return this._olympiadSide;
    }

    public void setOlympiadSide(int i) {
        this._olympiadSide = i;
    }

    public int getOlympiadGameId() {
        return this._olympiadGameId;
    }

    public void setOlympiadGameId(int id) {
        this._olympiadGameId = id;
    }

    public Location getSavedLocation() {
        return this._savedLocation;
    }

    public boolean isInObserverMode() {
        return !this._isInOlympiadMode && !this._savedLocation.equals(Location.DUMMY_LOC);
    }

    public int getTeleMode() {
        return this._teleMode;
    }

    public void setTeleMode(int mode) {
        this._teleMode = mode;
    }

    public int getLoto(int i) {
        return this._loto[i];
    }

    public void setLoto(int i, int val) {
        this._loto[i] = val;
    }

    public int getRace(int i) {
        return this._race[i];
    }

    public void setRace(int i, int val) {
        this._race[i] = val;
    }

    public boolean isInRefusalMode() {
        return this._messageRefusal;
    }

    public void setInRefusalMode(boolean mode) {
        this._messageRefusal = mode;
        this.sendPacket(new EtcStatusUpdate(this));
    }

    public boolean getTradeRefusal() {
        return this._tradeRefusal;
    }

    public void setTradeRefusal(boolean mode) {
        this._tradeRefusal = mode;
    }

    public boolean getExchangeRefusal() {
        return this._exchangeRefusal;
    }

    public void setExchangeRefusal(boolean mode) {
        this._exchangeRefusal = mode;
    }

    public BlockList getBlockList() {
        return this._blockList;
    }

    public boolean isPartyInRefuse() {
        return this._isPartyInRefuse;
    }

    public void setIsPartyInRefuse(boolean value) {
        this._isPartyInRefuse = value;
    }

    public boolean isHero() {
        return this._isHero;
    }

    public void setHero(boolean hero) {
        if (hero && this._baseClass == this._activeClass) {
            for (L2Skill skill : SkillTable.getHeroSkills()) {
                this.addSkill(skill, false);
            }
        } else {
            for (L2Skill skill : SkillTable.getHeroSkills()) {
                this.removeSkill(skill.getId(), false);
            }
        }

        this._isHero = hero;
        this.sendSkillList();
    }

    public boolean isOlympiadStart() {
        return this._isInOlympiadStart;
    }

    public void setOlympiadStart(boolean b) {
        this._isInOlympiadStart = b;
    }

    public boolean isInOlympiadMode() {
        return this._isInOlympiadMode;
    }

    public void setOlympiadMode(boolean b) {
        this._isInOlympiadMode = b;
    }

    public boolean isInDuel() {
        return this._duelId > 0;
    }

    public void setInDuel(int duelId) {
        if (duelId > 0) {
            this._duelState = DuelState.ON_COUNTDOWN;
            this._duelId = duelId;
        } else {
            if (this._duelState == DuelState.DEAD) {
                this.enableAllSkills();
                this.getStatus().startHpMpRegeneration();
            }

            this._duelState = DuelState.NO_DUEL;
            this._duelId = 0;
        }

    }

    public int getDuelId() {
        return this._duelId;
    }

    public DuelState getDuelState() {
        return this._duelState;
    }

    public void setDuelState(DuelState state) {
        this._duelState = state;
    }

    public SystemMessage getNoDuelReason() {
        SystemMessage sm = SystemMessage.getSystemMessage(this._noDuelReason).addCharName(this);
        this._noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
        return sm;
    }

    public boolean canDuel() {
        if (!this.isInCombat() && this._punishment.getType() != PunishmentType.JAIL) {
            if (!this.isDead() && !this.isAlikeDead() && !(this.getCurrentHp() < (double) (this.getMaxHp() / 2)) && !(this.getCurrentMp() < (double) (this.getMaxMp() / 2))) {
                if (this.isInDuel()) {
                    this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
                } else if (this.isInOlympiadMode()) {
                    this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
                } else if (!this.isCursedWeaponEquipped() && this.getKarma() == 0) {
                    if (this.isInStoreMode()) {
                        this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
                    } else if (!this.isMounted() && !this.isInBoat()) {
                        if (this.isFishing()) {
                            this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
                        } else {
                            if (!this.isInsideZone(ZoneId.PVP) && !this.isInsideZone(ZoneId.PEACE) && !this.isInsideZone(ZoneId.SIEGE)) {
                                return true;
                            }

                            this._noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
                        }
                    } else {
                        this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
                    }
                } else {
                    this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
                }
            } else {
                this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT;
            }
        } else {
            this._noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
        }

        return false;
    }

    public boolean isNoble() {
        return this._isNoble;
    }

    public void setNoble(boolean val, boolean store) {
        if (val) {
            for (L2Skill skill : SkillTable.getNobleSkills()) {
                this.addSkill(skill, false);
            }
        } else {
            for (L2Skill skill : SkillTable.getNobleSkills()) {
                this.removeSkill(skill.getId(), false);
            }
        }

        this._isNoble = val;
        this.sendSkillList();
        if (store) {
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET nobless=? WHERE obj_Id=?");) {
                ps.setBoolean(1, val);
                ps.setInt(2, this.getObjectId());
                ps.executeUpdate();
            } catch (Exception e) {
                LOGGER.error("Couldn't update nobless status for {}.", e, this.getName());
            }
        }

    }

    public int getLvlJoinedAcademy() {
        return this._lvlJoinedAcademy;
    }

    public void setLvlJoinedAcademy(int lvl) {
        this._lvlJoinedAcademy = lvl;
    }

    public boolean isAcademyMember() {
        return this._lvlJoinedAcademy > 0;
    }

    public TeamType getTeam() {
        return this._team;
    }

    public void setTeam(TeamType team) {
        this._team = team;
    }

    public boolean isAio() {
        return PlayerData.get(this).isAio();
    }

    public void setWantsPeace(boolean wantsPeace) {
        this._wantsPeace = wantsPeace;
    }

    public boolean wantsPeace() {
        return this._wantsPeace;
    }

    public boolean isFishing() {
        return this._fishingStance.isUnderFishCombat() || this._fishingStance.isLookingForFish();
    }

    public int getAllianceWithVarkaKetra() {
        return this._alliedVarkaKetra;
    }

    public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance) {
        this._alliedVarkaKetra = sideAndLvlOfAlliance;
    }

    public boolean isAlliedWithVarka() {
        return this._alliedVarkaKetra < 0;
    }

    public boolean isAlliedWithKetra() {
        return this._alliedVarkaKetra > 0;
    }

    public void sendSkillList() {
        boolean isWearingFormalWear = this.isWearingFormalWear();
        boolean isClanDisabled = this.getClan() != null && this.getClan().getReputationScore() < 0;
        SkillList sl = new SkillList();

        for (L2Skill skill : this.getSkills().values()) {
            sl.addSkill(skill.getId(), skill.getLevel(), skill.isPassive(), isWearingFormalWear || skill.isClanSkill() && isClanDisabled);
        }

        this.sendPacket(sl);
    }

    public boolean addSubClass(int classId, int classIndex) {
        if (!this._subclassLock.tryLock()) {
            return false;
        } else {
            boolean var3;
            try {
                if (this._subClasses.size() != Config.ALLOWED_SUBCLASS && classIndex != 0 && !this._subClasses.containsKey(classIndex)) {
                    SubClass subclass = new SubClass(classId, classIndex);

                    try {
                        Connection con = ConnectionPool.getConnection();

                        try {
                            PreparedStatement ps = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)");

                            try {
                                ps.setInt(1, this.getObjectId());
                                ps.setInt(2, subclass.getClassId());
                                ps.setLong(3, subclass.getExp());
                                ps.setInt(4, subclass.getSp());
                                ps.setInt(5, subclass.getLevel());
                                ps.setInt(6, subclass.getClassIndex());
                                ps.execute();
                            } catch (Throwable var17) {
                                if (ps != null) {
                                    try {
                                        ps.close();
                                    } catch (Throwable var16) {
                                        var17.addSuppressed(var16);
                                    }
                                }

                                throw var17;
                            }

                            ps.close();
                        } catch (Throwable var18) {
                            if (con != null) {
                                try {
                                    con.close();
                                } catch (Throwable var15) {
                                    var18.addSuppressed(var15);
                                }
                            }

                            throw var18;
                        }

                        con.close();
                    } catch (Exception e) {
                        LOGGER.error("Couldn't add subclass for {}.", e, this.getName());
                        boolean ps = false;
                        return ps;
                    }

                    this._subClasses.put(subclass.getClassIndex(), subclass);
                    PlayerClassData.getInstance().getTemplate(classId).getSkills().stream().filter((s) -> s.getMinLvl() <= 40).collect(Collectors.groupingBy(IntIntHolder::getId, Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) -> this.storeSkill(s.get().getSkill(), classIndex));
                    boolean e = true;
                    return e;
                }

                var3 = false;
            } finally {
                this._subclassLock.unlock();
            }

            return false;
        }
    }

    public boolean modifySubClass(int classIndex, int newClassId) {
        if (!this._subclassLock.tryLock()) {
            return false;
        } else {
            try {
                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");

                        try {
                            ps.setInt(1, this.getObjectId());
                            ps.setInt(2, classIndex);
                            ps.execute();
                        } catch (Throwable var32) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var27) {
                                    var32.addSuppressed(var27);
                                }
                            }

                            throw var32;
                        }

                        ps.close();

                        ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");

                        try {
                            ps.setInt(1, this.getObjectId());
                            ps.setInt(2, classIndex);
                            ps.execute();
                        } catch (Throwable var31) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var26) {
                                    var31.addSuppressed(var26);
                                }
                            }

                            throw var31;
                        }

                        ps.close();

                        ps = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");

                        try {
                            ps.setInt(1, this.getObjectId());
                            ps.setInt(2, classIndex);
                            ps.execute();
                        } catch (Throwable var30) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var25) {
                                    var30.addSuppressed(var25);
                                }
                            }

                            throw var30;
                        }

                        ps.close();

                        ps = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");

                        try {
                            ps.setInt(1, this.getObjectId());
                            ps.setInt(2, classIndex);
                            ps.execute();
                        } catch (Throwable var29) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var24) {
                                    var29.addSuppressed(var24);
                                }
                            }

                            throw var29;
                        }

                        ps.close();

                        ps = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?");

                        try {
                            ps.setInt(1, this.getObjectId());
                            ps.setInt(2, classIndex);
                            ps.execute();
                        } catch (Throwable var28) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var23) {
                                    var28.addSuppressed(var23);
                                }
                            }

                            throw var28;
                        }

                        ps.close();
                    } catch (Throwable var33) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var22) {
                                var33.addSuppressed(var22);
                            }
                        }

                        throw var33;
                    }

                    con.close();
                } catch (Exception e) {
                    LOGGER.error("Couldn't modify subclass for {} to class index {}.", e, this.getName(), classIndex);
                    this._subClasses.remove(classIndex);
                    boolean ps = false;
                    return ps;
                }

                this._subClasses.remove(classIndex);
                return this.addSubClass(newClassId, classIndex);
            } finally {
                this._subclassLock.unlock();
            }
        }
    }

    public boolean isSubClassActive() {
        return this._classIndex > 0;
    }

    public Map<Integer, SubClass> getSubClasses() {
        return this._subClasses;
    }

    public int getBaseClass() {
        return this._baseClass;
    }

    public void setBaseClass(int baseClass) {
        this._baseClass = baseClass;
    }

    public void setBaseClass(ClassId classId) {
        this._baseClass = classId.ordinal();
    }

    public int getActiveClass() {
        return this._activeClass;
    }

    public int getClassIndex() {
        return this._classIndex;
    }

    private void setClassTemplate(int classId) {
        this._activeClass = classId;
        this.setTemplate(PlayerClassData.getInstance().getTemplate(classId));
    }

    public boolean setActiveClass(int classIndex) {
        if (!this._subclassLock.tryLock()) {
            return false;
        } else {
            try {
                for (ItemInstance item : this.getInventory().getAugmentedItems()) {
                    if (item != null && item.isEquipped()) {
                        item.getAugmentation().removeBonus(this);
                    }
                }

                this.abortCast();

                for (Creature character : this.getKnownType(Creature.class)) {
                    if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
                        character.abortCast();
                    }
                }

                this.store();
                this._reuseTimeStamps.clear();
                this._charges.set(0);
                this.stopChargeTask();
                if (classIndex == 0) {
                    this.setClassTemplate(this.getBaseClass());
                } else {
                    try {
                        this.setClassTemplate(this._subClasses.get(classIndex).getClassId());
                    } catch (Exception e) {
                        LOGGER.error("Could not switch {}'s subclass to class index {}.", e, this.getName(), classIndex);
                        boolean item = false;
                        return item;
                    }
                }

                this._classIndex = classIndex;
                if (this._party != null) {
                    this._party.recalculateLevel();
                }

                if (this._summon instanceof Servitor) {
                    this._summon.unSummon(this);
                }

                for (L2Skill skill : this.getSkills().values()) {
                    this.removeSkill(skill.getId(), false);
                }

                this.stopAllEffectsExceptThoseThatLastThroughDeath();
                this.stopCubics();
                if (this.isSubClassActive()) {
                    this._dwarvenRecipeBook.clear();
                    this._commonRecipeBook.clear();
                } else {
                    this.restoreRecipeBook();
                }

                this.restoreSkills();
                this.giveSkills();
                this.regiveTemporarySkills();
                this.getDisabledSkills().clear();
                this.restoreEffects();
                this.updateEffectIcons();
                this.sendPacket(new EtcStatusUpdate(this));
                QuestState st = this.getQuestState("Q422_RepentYourSins");
                if (st != null) {
                    st.exitQuest(true);
                }

                this._hennaList.restore();
                this.sendPacket(new HennaInfo(this));
                if (this.getCurrentHp() > (double) this.getMaxHp()) {
                    this.setCurrentHp(this.getMaxHp());
                }

                if (this.getCurrentMp() > (double) this.getMaxMp()) {
                    this.setCurrentMp(this.getMaxMp());
                }

                if (this.getCurrentCp() > (double) this.getMaxCp()) {
                    this.setCurrentCp(this.getMaxCp());
                }

                this.refreshOverloaded();
                this.refreshExpertisePenalty();
                this.broadcastUserInfo();
                this.setExpBeforeDeath(0L);
                this.disableAutoShotsAll();
                ItemInstance item = this.getActiveWeaponInstance();
                if (item != null) {
                    item.unChargeAllShots();
                }

                this._shortcutList.restore();
                this.sendPacket(new ShortCutInit(this));
                this.broadcastPacket(new SocialAction(this, 15));
                this.sendPacket(new SkillCoolTime(this));
                boolean var18 = true;
                return var18;
            } finally {
                this._subclassLock.unlock();
            }
        }
    }

    public boolean isLocked() {
        return this._subclassLock.isLocked();
    }

    private void sendSiegeInfo() {
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            this.sendMessage(String.format("Castle : %s will have its siege on: %s", getCastleName(castle.getCastleId()), castle.getSiegeDate().getTime()));
        }

    }

    public void onPlayerEnter() {
        if (this.isCursedWeaponEquipped()) {
            CursedWeaponManager.getInstance().getCursedWeapon(this.getCursedWeaponEquippedId()).cursedOnLogin();
        }

        GameTimeTaskManager.getInstance().add(this);
        if (this.isIn7sDungeon() && !this.isGM()) {
            if (!SevenSignsManager.getInstance().isSealValidationPeriod() && !SevenSignsManager.getInstance().isCompResultsPeriod()) {
                if (SevenSignsManager.getInstance().getPlayerCabal(this.getObjectId()) == CabalType.NORMAL) {
                    this.teleportTo(TeleportType.TOWN);
                    this.setIsIn7sDungeon(false);
                }
            } else if (SevenSignsManager.getInstance().getPlayerCabal(this.getObjectId()) != SevenSignsManager.getInstance().getCabalHighestScore()) {
                this.teleportTo(TeleportType.TOWN);
                this.setIsIn7sDungeon(false);
            }
        }

        this._punishment.handle();
        if (this.isGM()) {
            if (this.isInvul()) {
                this.sendMessage("Entering world in Invulnerable mode.");
            }

            if (this.getAppearance().getInvisible()) {
                this.sendMessage("Entering world in Invisible mode.");
            }

            if (this.isInRefusalMode()) {
                this.sendMessage("Entering world in Message Refusal mode.");
            }
        }

        this.revalidateZone(true);
        this.notifyFriends(true);
        this.sendSiegeInfo();
    }

    public long getLastAccess() {
        return this._lastAccess;
    }

    public void doRevive() {
        super.doRevive();
        this.stopEffects(L2EffectType.CHARMOFCOURAGE);
        this.sendPacket(new EtcStatusUpdate(this));
        this._reviveRequested = 0;
        this._revivePower = 0.0F;
        if (this.isMounted()) {
            this.startFeed(this._mountNpcId);
        }

        ThreadPool.schedule(() -> this.setIsParalyzed(false), (int) (2000.0F / this.getStat().getMovementSpeedMultiplier()));
        this.setIsParalyzed(true);
    }

    public void doRevive(double revivePower) {
        this.restoreExp(revivePower);
        this.doRevive();
    }

    public void reviveRequest(Player Reviver, L2Skill skill, boolean Pet) {
        if (this._reviveRequested == 1) {
            if (this._revivePet == Pet) {
                Reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
            } else if (Pet) {
                Reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
            } else {
                Reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
            }

        } else {
            if (Pet && this._summon != null && this._summon.isDead() || !Pet && this.isDead()) {
                this._reviveRequested = 1;
                if (this.isPhoenixBlessed()) {
                    this._revivePower = 100.0F;
                } else if (this.isAffected(L2EffectFlag.CHARM_OF_COURAGE)) {
                    this._revivePower = 0.0F;
                } else {
                    this._revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), Reviver);
                }

                this._revivePet = Pet;
                if (this.isAffected(L2EffectFlag.CHARM_OF_COURAGE)) {
                    this.sendPacket((new ConfirmDlg(SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED)).addTime(60000));
                    return;
                }

                this.sendPacket((new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1)).addCharName(Reviver));
            }

        }
    }

    public void reviveAnswer(int answer) {
        if (this._reviveRequested == 1 && (this.isDead() || this._revivePet) && (!this._revivePet || this._summon == null || this._summon.isDead())) {
            if (answer == 0 && this.isPhoenixBlessed()) {
                this.stopPhoenixBlessing(null);
            } else if (answer == 1) {
                if (!this._revivePet) {
                    if (this._revivePower != (double) 0.0F) {
                        this.doRevive(this._revivePower);
                    } else {
                        this.doRevive();
                    }
                } else if (this._summon != null) {
                    if (this._revivePower != (double) 0.0F) {
                        this._summon.doRevive(this._revivePower);
                    } else {
                        this._summon.doRevive();
                    }
                }
            }

            this._reviveRequested = 0;
            this._revivePower = 0.0F;
        }
    }

    public boolean isReviveRequested() {
        return this._reviveRequested == 1;
    }

    public boolean isRevivingPet() {
        return this._revivePet;
    }

    public void removeReviving() {
        this._reviveRequested = 0;
        this._revivePower = 0.0F;
    }

    public void onActionRequest() {
        if (this.isSpawnProtected()) {
            this.sendMessage("As you acted, you are no longer under spawn protection.");
            this.setSpawnProtection(false);
        }

    }

    public final void onTeleported() {
        super.onTeleported();
        if (Config.PLAYER_SPAWN_PROTECTION > 0) {
            this.setSpawnProtection(true);
        }

        if (!this.isGM()) {
            this.stopAllToggles();
        }

        if (this._tamedBeast != null) {
            this._tamedBeast.getAI().stopFollow();
            this._tamedBeast.teleportTo(this.getPosition(), 0);
            this._tamedBeast.getAI().startFollow(this);
        }

        if (this._summon != null) {
            this._summon.setFollowStatus(false);
            this._summon.teleportTo(this.getPosition(), 0);
            ((SummonAI) this._summon.getAI()).setStartFollowController(true);
            this._summon.setFollowStatus(true);
        }

    }

    public void addExpAndSp(long addToExp, int addToSp) {
        this.getStat().addExpAndSp(addToExp, addToSp);
    }

    public void addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards) {
        this.getStat().addExpAndSp(addToExp, addToSp, rewards);
    }

    public void removeExpAndSp(long removeExp, int removeSp) {
        this.getStat().removeExpAndSp(removeExp, removeSp);
    }

    public void reduceCurrentHp(double value, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (skill != null) {
            this.getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
        } else {
            this.getStatus().reduceHp(value, attacker, awake, isDOT, false, false);
        }

        if (this._tamedBeast != null) {
            this._tamedBeast.onOwnerGotAttacked(attacker);
        }

    }

    public synchronized void addBypass(String bypass) {
        if (bypass != null) {
            this._validBypass.add(bypass);
        }
    }

    public synchronized void addBypass2(String bypass) {
        if (bypass != null) {
            this._validBypass2.add(bypass);
        }
    }

    public synchronized boolean validateBypass(String cmd) {
        for (String bp : this._validBypass) {
            if (bp != null && bp.equals(cmd)) {
                return true;
            }
        }

        for (String bp : this._validBypass2) {
            if (bp != null && cmd.startsWith(bp)) {
                return true;
            }
        }

        return false;
    }

    public ItemInstance validateItemManipulation(int objectId) {
        ItemInstance item = this.getInventory().getItemByObjectId(objectId);
        if (item != null && item.getOwnerId() == this.getObjectId()) {
            if ((this._summon == null || this._summon.getControlItemId() != objectId) && this._mountObjectId != objectId) {
                if (this.getActiveEnchantItem() != null && this.getActiveEnchantItem().getObjectId() == objectId) {
                    return null;
                } else {
                    return CursedWeaponManager.getInstance().isCursed(item.getItemId()) ? null : item;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public synchronized void clearBypass() {
        this._validBypass.clear();
        this._validBypass2.clear();
    }

    public boolean isInBoat() {
        return this._boat != null;
    }

    public Boat getBoat() {
        return this._boat;
    }

    public void setBoat(Boat v) {
        if (v == null && this._boat != null) {
            this._boat.removePassenger(this);
        }

        this._boat = v;
    }

    public boolean isCrystallizing() {
        return this._isCrystallizing;
    }

    public void setCrystallizing(boolean mode) {
        this._isCrystallizing = mode;
    }

    public SpawnLocation getBoatPosition() {
        return this._boatPosition;
    }

    public void deleteMe() {
        this._bot.stop();
        this.cleanup();
        this.store();
        super.deleteMe();
    }

    private synchronized void cleanup() {
        try {
            this.setOnlineStatus(false, true);
            this.abortAttack();
            this.abortCast();
            this.stopMove(null);
            this.setTarget(null);
            this.removeMeFromPartyMatch();
            if (this.isFlying()) {
                this.removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
            }

            if (this.isMounted()) {
                this.dismount();
            } else if (this._summon != null) {
                this._summon.unSummon(this);
            }

            this.stopHpMpRegeneration();
            this.stopChargeTask();
            this._punishment.stopTask(true);
            WaterTaskManager.getInstance().remove(this);
            AttackStanceTaskManager.getInstance().remove(this);
            PvpFlagTaskManager.getInstance().remove(this);
            GameTimeTaskManager.getInstance().remove(this);
            ShadowItemTaskManager.getInstance().remove(this);

            for (Creature character : this.getKnownType(Creature.class)) {
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
                    character.abortCast();
                }
            }

            for (L2Effect effect : this.getAllEffects()) {
                if (effect.getSkill().isToggle()) {
                    effect.exit();
                } else {
                    switch (effect.getEffectType()) {
                        case SIGNET_GROUND:
                        case SIGNET_EFFECT:
                            effect.exit();
                    }
                }
            }

            this.decayMe();
            if (this.getAgathion() != null) {
                this.despawnAgathion();
            }

            this._bot.stop();
            if (this._party != null) {
                this._party.removePartyMember(this, MessageType.DISCONNECTED);
            }

            if (OlympiadManager.getInstance().isRegistered(this) || this.getOlympiadGameId() != -1) {
                OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
            }

            if (this.getClan() != null) {
                ClanMember clanMember = this.getClan().getClanMember(this.getObjectId());
                if (clanMember != null) {
                    clanMember.setPlayerInstance(null);
                }
            }

            if (this.getActiveRequester() != null) {
                this.setActiveRequester(null);
                this.cancelActiveTrade();
            }

            if (this.isGM()) {
                AdminData.getInstance().deleteGm(this);
            }

            if (this.isInObserverMode()) {
                this.setXYZInvisible(this._savedLocation);
            }

            if (this.getBoat() != null) {
                this.getBoat().oustPlayer(this, true, Location.DUMMY_LOC);
            }

            this.getInventory().deleteMe();
            this.clearWarehouse();
            this.clearFreight();
            this.clearDepositedFreight();
            if (this.isCursedWeaponEquipped()) {
                CursedWeaponManager.getInstance().getCursedWeapon(this._cursedWeaponEquippedId).setPlayer(null);
            }

            if (this.getClanId() > 0) {
                this.getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
            }

            if (this.isSeated()) {
                WorldObject object = World.getInstance().getObject(this._throneId);
                if (object instanceof StaticObject) {
                    ((StaticObject) object).setBusy(false);
                }
            }

            World.getInstance().removePlayer(this);
            this.notifyFriends(false);
            this.getBlockList().playerLogout();
        } catch (Exception e) {
            LOGGER.error("Couldn't disconnect correctly the player.", e);
        }

    }

    public FishingStance getFishingStance() {
        return this._fishingStance;
    }

    public int getInventoryLimit() {
        return (this.getRace() == ClassRace.DWARF ? Config.INVENTORY_MAXIMUM_DWARF : Config.INVENTORY_MAXIMUM_NO_DWARF) + (int) this.getStat().calcStat(Stats.INV_LIM, 0.0F, null, null);
    }

    public int getWareHouseLimit() {
        return (this.getRace() == ClassRace.DWARF ? Config.WAREHOUSE_SLOTS_DWARF : Config.WAREHOUSE_SLOTS_NO_DWARF) + (int) this.getStat().calcStat(Stats.WH_LIM, 0.0F, null, null);
    }

    public int getPrivateSellStoreLimit() {
        return (this.getRace() == ClassRace.DWARF ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) this.getStat().calcStat(Stats.P_SELL_LIM, 0.0F, null, null);
    }

    public int getPrivateBuyStoreLimit() {
        return (this.getRace() == ClassRace.DWARF ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) this.getStat().calcStat(Stats.P_BUY_LIM, 0.0F, null, null);
    }

    public int getFreightLimit() {
        return Config.FREIGHT_SLOTS + (int) this.getStat().calcStat(Stats.FREIGHT_LIM, 0.0F, null, null);
    }

    public int getDwarfRecipeLimit() {
        return Config.DWARF_RECIPE_LIMIT + (int) this.getStat().calcStat(Stats.REC_D_LIM, 0.0F, null, null);
    }

    public int getCommonRecipeLimit() {
        return Config.COMMON_RECIPE_LIMIT + (int) this.getStat().calcStat(Stats.REC_C_LIM, 0.0F, null, null);
    }

    public int getMountNpcId() {
        return this._mountNpcId;
    }

    public int getMountLevel() {
        return this._mountLevel;
    }

    public int getMountObjectId() {
        return this._mountObjectId;
    }

    public void setMountObjectId(int id) {
        this._mountObjectId = id;
    }

    public Map<Integer, L2Skill> getSkills() {
        return this._skills;
    }

    public SkillUseHolder getCurrentSkill() {
        return this._currentSkill;
    }

    public void setCurrentSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        this._currentSkill.setSkill(skill);
        this._currentSkill.setCtrlPressed(ctrlPressed);
        this._currentSkill.setShiftPressed(shiftPressed);
    }

    public SkillUseHolder getCurrentPetSkill() {
        return this._currentPetSkill;
    }

    public void setCurrentPetSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        this._currentPetSkill.setSkill(skill);
        this._currentPetSkill.setCtrlPressed(ctrlPressed);
        this._currentPetSkill.setShiftPressed(shiftPressed);
    }

    public SkillUseHolder getQueuedSkill() {
        return this._queuedSkill;
    }

    public void setQueuedSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        this._queuedSkill.setSkill(skill);
        this._queuedSkill.setCtrlPressed(ctrlPressed);
        this._queuedSkill.setShiftPressed(shiftPressed);
    }

    public Punishment getPunishment() {
        return this._punishment;
    }

    public boolean isInJail() {
        return this._punishment.getType() == PunishmentType.JAIL;
    }

    public boolean isChatBanned() {
        return this._punishment.getType() == PunishmentType.CHAT;
    }

    public int getPowerGrade() {
        return this._powerGrade;
    }

    public void setPowerGrade(int power) {
        this._powerGrade = power;
    }

    public boolean isCursedWeaponEquipped() {
        return this._cursedWeaponEquippedId != 0;
    }

    public int getCursedWeaponEquippedId() {
        return this._cursedWeaponEquippedId;
    }

    public void setCursedWeaponEquippedId(int value) {
        this._cursedWeaponEquippedId = value;
    }

    public void shortBuffStatusUpdate(int magicId, int level, int time) {
        if (this._shortBuffTask != null) {
            this._shortBuffTask.cancel(false);
            this._shortBuffTask = null;
        }

        this._shortBuffTask = ThreadPool.schedule(() -> {
            this.sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
            this.setShortBuffTaskSkillId(0);
        }, time * 1000);
        this.setShortBuffTaskSkillId(magicId);
        this.sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
    }

    public int getShortBuffTaskSkillId() {
        return this._shortBuffTaskSkillId;
    }

    public void setShortBuffTaskSkillId(int id) {
        this._shortBuffTaskSkillId = id;
    }

    public int getDeathPenaltyBuffLevel() {
        return this._deathPenaltyBuffLevel;
    }

    public void setDeathPenaltyBuffLevel(int level) {
        this._deathPenaltyBuffLevel = level;
    }

    public void calculateDeathPenaltyBuffLevel(Creature killer) {
        if (this._deathPenaltyBuffLevel < 15) {
            if ((this.getKarma() > 0 || Rnd.get(1, 100) <= Config.DEATH_PENALTY_CHANCE) && !(killer instanceof Player) && !this.isGM() && (!this.getCharmOfLuck() || killer != null && !killer.isRaidRelated()) && !this.isPhoenixBlessed() && !this.isInsideZone(ZoneId.PVP) && !this.isInsideZone(ZoneId.SIEGE)) {
                if (this._deathPenaltyBuffLevel != 0) {
                    this.removeSkill(5076, false);
                }

                ++this._deathPenaltyBuffLevel;
                this.addSkill(SkillTable.getInstance().getInfo(5076, this._deathPenaltyBuffLevel), false);
                this.sendPacket(new EtcStatusUpdate(this));
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(this._deathPenaltyBuffLevel));
            }

        }
    }

    public void reduceDeathPenaltyBuffLevel() {
        if (this._deathPenaltyBuffLevel > 0) {
            this.removeSkill(5076, false);
            --this._deathPenaltyBuffLevel;
            if (this._deathPenaltyBuffLevel > 0) {
                this.addSkill(SkillTable.getInstance().getInfo(5076, this._deathPenaltyBuffLevel), false);
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(this._deathPenaltyBuffLevel));
            } else {
                this.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
            }

            this.sendPacket(new EtcStatusUpdate(this));
        }
    }

    public void restoreDeathPenaltyBuffLevel() {
        if (this._deathPenaltyBuffLevel > 0) {
            this.addSkill(SkillTable.getInstance().getInfo(5076, this._deathPenaltyBuffLevel), false);
        }

    }

    public Collection<Timestamp> getReuseTimeStamps() {
        return this._reuseTimeStamps.values();
    }

    public Map<Integer, Timestamp> getReuseTimeStamp() {
        return this._reuseTimeStamps;
    }

    public void addTimeStamp(L2Skill skill, long reuse) {
        this._reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse));
    }

    public void addTimeStamp(L2Skill skill, long reuse, long systime) {
        this._reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse, systime));
    }

    public Player getActingPlayer() {
        return this;
    }

    public final void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
        if (miss) {
            this.sendPacket(SystemMessageId.MISSED_TARGET);
        } else {
            if (BossEvent.getInstance().getState() == EventState.FIGHTING && BossEvent.getInstance().eventPlayers.contains(this) && target.getObjectId() == BossEvent.getInstance().objectId) {
                this.bossEventDamage += damage;
            }

            if (pcrit) {
                this.sendPacket(SystemMessageId.CRITICAL_HIT);
            }

            if (mcrit) {
                this.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
            }

            if (target.isInvul()) {
                if (target.isParalyzed()) {
                    this.sendPacket(SystemMessageId.OPPONENT_PETRIFIED);
                } else {
                    this.sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
                }
            } else {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage));
            }

            if (this.isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == this.getOlympiadGameId()) {
                OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
            }

        }
    }

    public void checkItemRestriction() {
        for (ItemInstance equippedItem : this.getInventory().getPaperdollItems()) {
            if (!equippedItem.getItem().checkCondition(this, this, false)) {
                this.useEquippableItem(equippedItem, equippedItem.isWeapon());
            }
        }

    }

    public void enterOnNoLandingZone() {
        if (this.getMountType() == 2) {
            if (this._dismountTask == null) {
                this._dismountTask = ThreadPool.schedule(this::dismount, 5000L);
            }

            this.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
        }

    }

    public void enteredNoLanding(int delay) {
        if (this._dismountTask == null) {
            this._dismountTask = ThreadPool.schedule(this::dismount, delay * 1000);
        }

    }

    public void exitOnNoLandingZone() {
        if (this.getMountType() == 2 && this._dismountTask != null) {
            this._dismountTask.cancel(true);
            this._dismountTask = null;
        }

    }

    public void setIsInSiege(boolean b) {
        this._isInSiege = b;
    }

    public boolean isInSiege() {
        return this._isInSiege;
    }

    public void removeFromBossZone() {
        for (BossZone zone : ZoneManager.getInstance().getAllZones(BossZone.class)) {
            zone.removePlayer(this);
        }

    }

    public int getCharges() {
        return this._charges.get();
    }

    public void increaseCharges(int count, int max) {
        if (this._charges.get() >= max) {
            this.sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
        } else {
            this.restartChargeTask();
            if (this._charges.addAndGet(count) >= max) {
                this._charges.set(max);
                this.sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
            } else {
                this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(this._charges.get()));
            }

            this.sendPacket(new EtcStatusUpdate(this));
        }
    }

    public boolean decreaseCharges(int count) {
        if (this._charges.get() < count) {
            return false;
        } else {
            if (this._charges.addAndGet(-count) == 0) {
                this.stopChargeTask();
            } else {
                this.restartChargeTask();
            }

            this.sendPacket(new EtcStatusUpdate(this));
            return true;
        }
    }

    public void clearCharges() {
        this._charges.set(0);
        this.sendPacket(new EtcStatusUpdate(this));
    }

    private void restartChargeTask() {
        if (this._chargeTask != null) {
            this._chargeTask.cancel(false);
            this._chargeTask = null;
        }

        this._chargeTask = ThreadPool.schedule(this::clearCharges, 600000L);
    }

    public void stopChargeTask() {
        if (this._chargeTask != null) {
            this._chargeTask.cancel(false);
            this._chargeTask = null;
        }

    }

    public boolean canAttackCharacter(Creature cha) {
        if (cha instanceof Attackable) {
            return true;
        } else {
            if (cha instanceof Playable) {
                if (cha.isInArena()) {
                    return true;
                }

                Player target = cha.getActingPlayer();
                if (this.isInDuel() && target.isInDuel() && target.getDuelId() == this.getDuelId()) {
                    return true;
                }

                if (this.isInParty() && target.isInParty()) {
                    if (this.getParty() == target.getParty()) {
                        return false;
                    }

                    if ((this.getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null) && this.getParty().getCommandChannel() == target.getParty().getCommandChannel()) {
                        return false;
                    }
                }

                if (this.getClan() != null && target.getClan() != null) {
                    if (this.getClanId() == target.getClanId()) {
                        return false;
                    }

                    if ((this.getAllyId() > 0 || target.getAllyId() > 0) && this.getAllyId() == target.getAllyId()) {
                        return false;
                    }

                    if (this.getClan().isAtWarWith(target.getClanId())) {
                        return true;
                    }
                } else if (target.getPvpFlag() == 0 && target.getKarma() == 0) {
                    return false;
                }
            }

            return true;
        }
    }

    public void teleportToFriend(Player player, L2Skill skill) {
        if (player != null && skill != null) {
            if (player.checkSummonerStatus() && player.checkSummonTargetStatus(this)) {
                int itemConsumeId = skill.getTargetConsumeId();
                int itemConsumeCount = skill.getTargetConsume();
                if (itemConsumeId != 0 && itemConsumeCount != 0) {
                    if (this.getInventory().getInventoryItemCount(itemConsumeId, -1) < itemConsumeCount) {
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(skill.getTargetConsumeId()));
                        return;
                    }

                    this.destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, this, true);
                }

                this.teleportTo(player.getX(), player.getY(), player.getZ(), 20);
            }
        }
    }

    public boolean checkSummonerStatus() {
        if (this.isMounted()) {
            return false;
        } else if (!this.isInOlympiadMode() && !this.isInObserverMode() && !this.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            return true;
        } else {
            this.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
            return false;
        }
    }

    public boolean checkSummonTargetStatus(WorldObject target) {
        if (!(target instanceof Player player)) {
            return false;
        } else if (player.isAlikeDead()) {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addCharName(player));
            return false;
        } else if (player.isInStoreMode()) {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addCharName(player));
            return false;
        } else if (!player.isRooted() && !player.isInCombat()) {
            if (player.isInOlympiadMode()) {
                this.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
                return false;
            } else if (!player.isFestivalParticipant() && !player.isMounted()) {
                if (!player.isInObserverMode() && !player.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
                    return true;
                } else {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA).addCharName(player));
                    return false;
                }
            } else {
                this.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
                return false;
            }
        } else {
            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addCharName(player));
            return false;
        }
    }

    public final int getClientX() {
        return this._clientX;
    }

    public final void setClientX(int val) {
        this._clientX = val;
    }

    public final int getClientY() {
        return this._clientY;
    }

    public final void setClientY(int val) {
        this._clientY = val;
    }

    public final int getClientZ() {
        return this._clientZ;
    }

    public final void setClientZ(int val) {
        this._clientZ = val;
    }

    public int getMailPosition() {
        return this._mailPosition;
    }

    public void setMailPosition(int mailPosition) {
        this._mailPosition = mailPosition;
    }

    public final boolean isFalling(int z) {
        if (!this.isDead() && !this.isFlying() && !this.isInsideZone(ZoneId.WATER)) {
            if (System.currentTimeMillis() < this._fallingTimestamp) {
                return true;
            } else {
                int deltaZ = this.getZ() - z;
                if (deltaZ <= this.getBaseTemplate().getFallHeight()) {
                    return false;
                } else {
                    int damage = (int) Formulas.calcFallDam(this, deltaZ);
                    if (damage > 0) {
                        this.reduceCurrentHp(Math.min(damage, this.getCurrentHp() - (double) 1.0F), null, false, true, null);
                        this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
                    }

                    this.setFalling();
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public final void setFalling() {
        this._fallingTimestamp = System.currentTimeMillis() + 10000L;
    }

    public boolean isAllowedToEnchantSkills() {
        if (this.isLocked()) {
            return false;
        } else if (AttackStanceTaskManager.getInstance().isInAttackStance(this)) {
            return false;
        } else if (!this.isCastingNow() && !this.isCastingSimultaneouslyNow()) {
            return !this.isInBoat();
        } else {
            return false;
        }
    }

    public List<Integer> getFriendList() {
        return this._friendList;
    }

    public void selectFriend(Integer friendId) {
        if (!this._selectedFriendList.contains(friendId)) {
            this._selectedFriendList.add(friendId);
        }

    }

    public void deselectFriend(Integer friendId) {
        if (this._selectedFriendList.contains(friendId)) {
            this._selectedFriendList.remove(friendId);
        }

    }

    public List<Integer> getSelectedFriendList() {
        return this._selectedFriendList;
    }

    private void restoreFriendList() {
        this._friendList.clear();

        try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 0");) {
            ps.setInt(1, this.getObjectId());

            try (ResultSet rset = ps.executeQuery()) {
                while (rset.next()) {
                    int friendId = rset.getInt("friend_id");
                    if (friendId != this.getObjectId()) {
                        this._friendList.add(friendId);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore {}'s friendlist.", e, this.getName());
        }

    }

    private void notifyFriends(boolean login) {
        for (int id : this._friendList) {
            Player friend = World.getInstance().getPlayer(id);
            if (friend != null) {
                friend.sendPacket(new FriendList(friend));
                if (login) {
                    friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addCharName(this));
                }
            }
        }

    }

    public void selectBlock(Integer friendId) {
        if (!this._selectedBlocksList.contains(friendId)) {
            this._selectedBlocksList.add(friendId);
        }

    }

    public void deselectBlock(Integer friendId) {
        if (this._selectedBlocksList.contains(friendId)) {
            this._selectedBlocksList.remove(friendId);
        }

    }

    public List<Integer> getSelectedBlocksList() {
        return this._selectedBlocksList;
    }

    public void broadcastRelationsChanges() {
        for (Player player : this.getKnownType(Player.class)) {
            int relation = this.getRelation(player);
            boolean isAutoAttackable = this.isAutoAttackable(player);
            player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
            if (this._summon != null) {
                player.sendPacket(new RelationChanged(this._summon, relation, isAutoAttackable));
            }
        }

    }

    public void sendInfo(Player player) {
        if (this.isInBoat()) {
            this.getPosition().set(this.getBoat().getPosition());
        }

        if (this.getPolyType() == PolyType.NPC) {
            player.sendPacket(new AbstractNpcInfo.PcMorphInfo(this, this.getPolyTemplate()));
        } else {
            player.sendPacket(new CharInfo(this));
            if (this.isSeated()) {
                WorldObject object = World.getInstance().getObject(this._throneId);
                if (object instanceof StaticObject) {
                    player.sendPacket(new ChairSit(this.getObjectId(), ((StaticObject) object).getStaticObjectId()));
                }
            }
        }

        int relation = this.getRelation(player);
        boolean isAutoAttackable = this.isAutoAttackable(player);
        player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
        if (this._summon != null) {
            player.sendPacket(new RelationChanged(this._summon, relation, isAutoAttackable));
        }

        relation = player.getRelation(this);
        isAutoAttackable = player.isAutoAttackable(this);
        this.sendPacket(new RelationChanged(player, relation, isAutoAttackable));
        if (player.getSummon() != null) {
            this.sendPacket(new RelationChanged(player.getSummon(), relation, isAutoAttackable));
        }

        if (this.isInBoat()) {
            player.sendPacket(new GetOnVehicle(this.getObjectId(), this.getBoat().getObjectId(), this.getBoatPosition()));
        }

        switch (this.getStoreType()) {
            case SELL:
            case PACKAGE_SELL:
                player.sendPacket(new PrivateStoreMsgSell(this));
                break;
            case BUY:
                player.sendPacket(new PrivateStoreMsgBuy(this));
                break;
            case MANUFACTURE:
                player.sendPacket(new RecipeShopMsg(this));
        }

    }

    public double getCollisionRadius() {
        return this.getBaseTemplate().getCollisionRadiusBySex(this.getAppearance().getSex());
    }

    public double getCollisionHeight() {
        return this.getBaseTemplate().getCollisionHeightBySex(this.getAppearance().getSex());
    }

    public boolean teleportRequest(Player requester, L2Skill skill) {
        if (this._summonTargetRequest != null && requester != null) {
            return false;
        } else {
            this._summonTargetRequest = requester;
            this._summonSkillRequest = skill;
            return true;
        }
    }

    public void teleportAnswer(int answer, int requesterId) {
        if (this._summonTargetRequest != null) {
            if (answer == 1 && this._summonTargetRequest.getObjectId() == requesterId) {
                this.teleportToFriend(this._summonTargetRequest, this._summonSkillRequest);
            }

            this._summonTargetRequest = null;
            this._summonSkillRequest = null;
        }
    }

    public void activateGate(int answer, int type) {
        if (this._requestedGate != null) {
            if (answer == 1 && this.getTarget() == this._requestedGate) {
                if (type == 1) {
                    this._requestedGate.openMe();
                } else if (type == 0) {
                    this._requestedGate.closeMe();
                }
            }

            this._requestedGate = null;
        }
    }

    public void setRequestedGate(Door door) {
        this._requestedGate = door;
    }

    public void ignored(Integer itemId) {
        if (this._ignored.contains(itemId)) {
            this._ignored.remove(itemId);
        } else {
            this._ignored.add(itemId);
        }

    }

    public boolean ignoredDropContain(int itemId) {
        return this._ignored.contains(itemId);
    }

    public boolean polymorph(PolyType type, int npcId) {
        if (super.polymorph(type, npcId)) {
            this.sendPacket(new UserInfo(this));
            return true;
        } else {
            return false;
        }
    }

    public void unpolymorph() {
        super.unpolymorph();
        this.sendPacket(new UserInfo(this));
    }

    public void addKnownObject(WorldObject object) {
        this.sendInfoFrom(object);
    }

    public void removeKnownObject(WorldObject object) {
        super.removeKnownObject(object);
        this.sendPacket(new DeleteObject(object, object instanceof Player && ((Player) object).isSeated()));
    }

    public final void refreshInfos() {
        for (WorldObject object : this.getKnownType(WorldObject.class)) {
            if (!(object instanceof Player) || !((Player) object).isInObserverMode()) {
                this.sendInfoFrom(object);
            }
        }

    }

    public final void teleToLocation(Location loc) {
        super.teleportTo(loc, 0);
        this.respawnAgathion();
    }

    public final void teleportTo(Location loc, int randomOffset) {
        if (DimensionalRiftManager.getInstance().checkIfInRiftZone(this.getX(), this.getY(), this.getZ(), true)) {
            this.sendMessage("You have been sent to the waiting room.");
            if (this.isInParty() && this.getParty().isInDimensionalRift()) {
                this.getParty().getDimensionalRift().usedTeleport(this);
            }

            loc = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportLoc();
        }

        super.teleportTo(loc, randomOffset);
        this.respawnAgathion();
    }

    private final void sendInfoFrom(WorldObject object) {
        if (object.getPolyType() == PolyType.ITEM) {
            this.sendPacket(new SpawnItem(object));
        } else {
            object.sendInfo(this);
            if (object instanceof Creature) {
                Creature obj = (Creature) object;
                if (obj.hasAI()) {
                    obj.getAI().describeStateToPlayer(this);
                }
            }
        }

    }

    public void checkEquipmentXPvps() {
        if (Config.ENABLE_KILLS_LIMIT_A && this.getPvpKills() < Config.PVP_A_GRADE) {
            boolean send = false;

            for (int i = 0; i < 17; ++i) {
                ItemInstance equippedItem = this.getInventory().getPaperdollItem(i);
                if (equippedItem != null && equippedItem.getItem().getCrystalType() == CrystalType.A && equippedItem.isEquipped()) {
                    this.getInventory().unEquipItemInSlot(i);
                    send = true;
                }
            }

            if (send) {
                this.getInventory().reloadEquippedItems();
                this.getInventory().updateDatabase();
                this.broadcastUserInfo();
                this.sendMessage("You need " + Config.PVP_A_GRADE + " pvp kills to equip A Grade item.");
            }
        }

        if (Config.ENABLE_KILLS_LIMIT_S && this.getPvpKills() < Config.PVP_S_GRADE) {
            boolean send = false;

            for (int i = 0; i < 17; ++i) {
                ItemInstance equippedItem = this.getInventory().getPaperdollItem(i);
                if (equippedItem != null && equippedItem.getItem().getCrystalType() == CrystalType.S && equippedItem.isEquipped()) {
                    this.getInventory().unEquipItemInSlot(i);
                    send = true;
                }
            }

            if (send) {
                this.getInventory().reloadEquippedItems();
                this.getInventory().updateDatabase();
                this.broadcastUserInfo();
                this.sendMessage("You need " + Config.PVP_A_GRADE + " pvp kills to equip S Grade item.");
            }
        }

    }

    public boolean isWearingFormalWear() {
        ItemInstance formal = this.getInventory().getPaperdollItem(10);
        return formal != null && formal.getItem().getBodyPart() == 131072;
    }

    public boolean isVip() {
        return PlayerData.get(this).isVip();
    }

    public boolean isAutoFarm() {
        return this._autoFarm;
    }

    public void setAutoFarm(boolean comm) {
        this._autoFarm = comm;
    }

    public int getRadius() {
        return this.autoFarmRadius;
    }

    public void setRadius(int value) {
        this.autoFarmRadius = MathUtil.limit(value, 200, 3000);
    }

    public int getPage() {
        return this.autoFarmShortCut;
    }

    public void setPage(int value) {
        this.autoFarmShortCut = MathUtil.limit(value, 0, 9);
    }

    public int getHealPercent() {
        return this.autoFarmHealPercente;
    }

    public void setHealPercent(int value) {
        this.autoFarmHealPercente = MathUtil.limit(value, 20, 90);
    }

    public void setNoBuffProtection(boolean val) {
        this.autoFarmBuffProtection = val;
    }

    public boolean isNoBuffProtected() {
        return this.autoFarmBuffProtection;
    }

    public void setAntiKsProtection(boolean val) {
        this.autoAntiKsProtection = val;
    }

    public boolean isAntiKsProtected() {
        return this.autoAntiKsProtection;
    }

    public boolean isSummonAttack() {
        return this.autoFarmSummonAttack;
    }

    public void setSummonAttack(boolean val) {
        this.autoFarmSummonAttack = val;
    }

    public int getSummonSkillPercent() {
        return this.autoFarmSummonSkillPercente;
    }

    public void setSummonSkillPercent(int value) {
        this.autoFarmSummonSkillPercente = MathUtil.limit(value, 0, 90);
    }

    public int getHpPotionPercentage() {
        return this.hpPotionPercent;
    }

    public void setHpPotionPercentage(int value) {
        this.hpPotionPercent = MathUtil.limit(value, 0, 100);
    }

    public int getMpPotionPercentage() {
        return this.mpPotionPercent;
    }

    public void setMpPotionPercentage(int value) {
        this.mpPotionPercent = MathUtil.limit(value, 0, 100);
    }

    public void ignoredMonster(Integer npcId) {
        this._ignoredMonster.add(npcId);
    }

    public void activeMonster(Integer npcId) {
        if (this._ignoredMonster.contains(npcId)) {
            this._ignoredMonster.remove(npcId);
        }

    }

    public boolean ignoredMonsterContain(int npcId) {
        return this._ignoredMonster.contains(npcId);
    }

    public AutofarmPlayerRoutine getBot() {
        if (this._bot == null) {
            this._bot = new AutofarmPlayerRoutine(this);
        }

        return this._bot;
    }

    public void saveAutoFarmSettings() {
        try (Connection con = ConnectionPool.getConnection()) {
            String updateSql = "REPLACE INTO character_autofarm (char_id, char_name,  radius, short_cut, heal_percent, buff_protection, anti_ks_protection, summon_attack, summon_skill_percent, hp_potion_percent, mp_potion_percent) VALUES (?, ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement updateStatement = con.prepareStatement(updateSql)) {
                updateStatement.setInt(1, this.getObjectId());
                updateStatement.setString(2, this.getName());
                updateStatement.setInt(3, this.autoFarmRadius);
                updateStatement.setInt(4, this.autoFarmShortCut);
                updateStatement.setInt(5, this.autoFarmHealPercente);
                updateStatement.setBoolean(6, this.autoFarmBuffProtection);
                updateStatement.setBoolean(7, this.autoAntiKsProtection);
                updateStatement.setBoolean(8, this.autoFarmSummonAttack);
                updateStatement.setInt(9, this.autoFarmSummonSkillPercente);
                updateStatement.setInt(10, this.hpPotionPercent);
                updateStatement.setInt(11, this.mpPotionPercent);
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void loadAutoFarmSettings() {
        try (Connection con = ConnectionPool.getConnection()) {
            String selectSql = "SELECT * FROM character_autofarm WHERE char_id = ?";

            try (PreparedStatement selectStatement = con.prepareStatement(selectSql)) {
                selectStatement.setInt(1, this.getObjectId());

                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        this.autoFarmRadius = resultSet.getInt("radius");
                        this.autoFarmShortCut = resultSet.getInt("short_cut");
                        this.autoFarmHealPercente = resultSet.getInt("heal_percent");
                        this.autoFarmBuffProtection = resultSet.getBoolean("buff_protection");
                        this.autoAntiKsProtection = resultSet.getBoolean("anti_ks_protection");
                        this.autoFarmSummonAttack = resultSet.getBoolean("summon_attack");
                        this.autoFarmSummonSkillPercente = resultSet.getInt("summon_skill_percent");
                        this.hpPotionPercent = resultSet.getInt("hp_potion_percent");
                        this.mpPotionPercent = resultSet.getInt("mp_potion_percent");
                    } else {
                        this.autoFarmRadius = 1200;
                        this.autoFarmShortCut = 9;
                        this.autoFarmHealPercente = 30;
                        this.autoFarmBuffProtection = false;
                        this.autoAntiKsProtection = false;
                        this.autoFarmSummonAttack = false;
                        this.autoFarmSummonSkillPercente = 0;
                        this.hpPotionPercent = 60;
                        this.mpPotionPercent = 60;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int getPcBang() {
        return this.pcBangPoint;
    }

    public void setPcBang(int val) {
        this.pcBangPoint = val;
        ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, 0, false, 24, false);
        this.sendPacket(wnd);
    }

    public int getPcBangScore() {
        return this.pcBangPoint;
    }

    public void reducePcBangScore(int to) {
        this.pcBangPoint -= to;
        this.updatePcBangWnd(to, false, false);
    }

    public void addPcBangScore(int to) {
        this.pcBangPoint += to;
    }

    public void updatePcBangWnd(int score, boolean add, boolean duble) {
        ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, score, add, 24, duble);
        this.sendPacket(wnd);
    }

    public void showPcBangWindow() {
        ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, 0, false, 24, false);
        this.sendPacket(wnd);
    }

    public boolean getReceiveNewbieArmor() {
        return this.receiveNewbieArmor;
    }

    public void setReceiveNewbieArmor(boolean receive) {
        this.receiveNewbieArmor = receive;
    }

    public int getBossEventDamage() {
        return this.bossEventDamage;
    }

    public void setBossEventDamage(int bossEventDamage) {
        this.bossEventDamage = bossEventDamage;
    }

    public int getLastX() {
        return this._lastX;
    }

    public int getLastY() {
        return this._lastY;
    }

    public int getLastZ() {
        return this._lastZ;
    }

    public void setLastCords(int x, int y, int z) {
        this._lastX = x;
        this._lastY = y;
        this._lastZ = z;
    }

    public boolean isTournamentTeleport() {
        return this._TournamentTeleport;
    }

    public void setTournamentTeleport(boolean comm) {
        this._TournamentTeleport = comm;
    }

    public int getBuff() {
        return this.buff;
    }

    public void setBuff(int val) {
        this.buff = val;
    }

    public boolean isCMultisell() {
        return this._isCMultisell;
    }

    public void setIsUsingCMultisell(boolean b) {
        this._isCMultisell = b;
    }

    public boolean isSellItemCommunity() {
        return this._isSellItemCommunity;
    }

    public void setIsUsingSellItemCommunity(boolean b) {
        this._isSellItemCommunity = b;
    }

    public boolean isNewChar() {
        return this._onlineTime < 500L;
    }

    public boolean isInEvent() {
        if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(this)) {
            return true;
        } else if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(this)) {
            return true;
        } else if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(this)) {
            return true;
        } else {
            if (ArenaTask.is_started()) {
                if (Arena2x2.getInstance().isRegistered(this) || this.isInsideZone(ZoneId.ARENA_EVENT)) {
                    return true;
                }

                if (Arena4x4.getInstance().isRegistered(this) || this.isInsideZone(ZoneId.ARENA_EVENT)) {
                    return true;
                }

                if (Arena9x9.getInstance().isRegistered(this) || this.isInsideZone(ZoneId.ARENA_EVENT)) {
                    return true;
                }
            }

            if (BossEvent.getInstance().getState() != EventState.INACTIVE && BossEvent.getInstance().isRegistered(this)) {
                return true;
            } else return this.getDungeon() != null;
        }
    }

    public String getHWID() {
        if (this.getClient() == null) {
            return this._hwid;
        } else {
            this._hwid = this.getClient().getHWID();
            return this._hwid;
        }
    }

    public void addSpoilSkillinZone() {
        if (this.getRace() != ClassRace.DWARF) {
            L2Skill skill_sweeper = SkillTable.getInstance().getInfo(42, 1);
            L2Skill skill_spoil = SkillTable.getInstance().getInfo(254, 11);
            if (skill_sweeper != null && skill_spoil != null) {
                String name_sweeper = skill_sweeper.getName();
                String name_spoil = skill_spoil.getName();
                this.addSkill(skill_sweeper, true);
                this.addSkill(skill_spoil, true);
                this.sendMessage("You have two skills which are available for use in this zone: " + name_sweeper + " and " + name_spoil + ".");
                this.sendPacket(new ExShowScreenMessage("You have two skills which are available for use in this zone: " + name_sweeper + " and " + name_spoil + ".", 5000));
                this.sendSkillList();
            }

        }
    }

    public void removeSpoilSkillinZone() {
        if (this.getRace() != ClassRace.DWARF) {
            L2Skill skill_sweeper = this.removeSkill(42, true);
            L2Skill skill_spoil = this.removeSkill(254, true);
            if (skill_sweeper != null && skill_spoil != null) {
                String name_sweeper = skill_sweeper.getName();
                String name_spoil = skill_spoil.getName();
                this.sendMessage("you lost " + name_sweeper + " and " + name_spoil + ".");
                this.sendPacket(new ExShowScreenMessage("you lost " + name_sweeper + " and " + name_spoil + ".", 5000));
                this.sendSkillList();
            }

        }
    }

    public Map<String, PlayerVar> getVariables() {
        return this._variables;
    }

    private synchronized void storeDressMeData() {
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO characters_dressme_data (obj_Id, armor_skins, armor_skin_option, weapon_skins, weapon_skin_option, hair_skins, hair_skin_option, face_skins, face_skin_option) VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE obj_Id=?, armor_skins=?, armor_skin_option=?, weapon_skins=?, weapon_skin_option=?, hair_skins=?, hair_skin_option=?, face_skins=?, face_skin_option=?, shield_skins=?, shield_skin_option=?");) {
            statement.setInt(1, this.getObjectId());
            if (this._armorSkins.isEmpty()) {
                statement.setString(2, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._armorSkins) {
                    s.append(i).append(",");
                }

                statement.setString(2, s.toString());
            }

            statement.setInt(3, this.getArmorSkinOption());
            if (this._weaponSkins.isEmpty()) {
                statement.setString(4, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._weaponSkins) {
                    s.append(i).append(",");
                }

                statement.setString(4, s.toString());
            }

            statement.setInt(5, this.getWeaponSkinOption());
            if (this._hairSkins.isEmpty()) {
                statement.setString(6, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._hairSkins) {
                    s.append(i).append(",");
                }

                statement.setString(6, s.toString());
            }

            statement.setInt(7, this.getHairSkinOption());
            if (this._faceSkins.isEmpty()) {
                statement.setString(8, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._faceSkins) {
                    s.append(i).append(",");
                }

                statement.setString(8, s.toString());
            }

            statement.setInt(9, this.getFaceSkinOption());
            if (this._shieldSkins.isEmpty()) {
                statement.setString(10, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._shieldSkins) {
                    s.append(i).append(",");
                }

                statement.setString(10, s.toString());
            }

            statement.setInt(11, this.getShieldSkinOption());
            statement.setInt(10, this.getObjectId());
            if (this._armorSkins.isEmpty()) {
                statement.setString(11, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._armorSkins) {
                    s.append(i).append(",");
                }

                statement.setString(11, s.toString());
            }

            statement.setInt(12, this.getArmorSkinOption());
            if (this._weaponSkins.isEmpty()) {
                statement.setString(13, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._weaponSkins) {
                    s.append(i).append(",");
                }

                statement.setString(13, s.toString());
            }

            statement.setInt(14, this.getWeaponSkinOption());
            if (this._hairSkins.isEmpty()) {
                statement.setString(15, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._hairSkins) {
                    s.append(i).append(",");
                }

                statement.setString(15, s.toString());
            }

            statement.setInt(16, this.getHairSkinOption());
            if (this._faceSkins.isEmpty()) {
                statement.setString(17, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._faceSkins) {
                    s.append(i).append(",");
                }

                statement.setString(17, s.toString());
            }

            statement.setInt(18, this.getFaceSkinOption());
            if (this._shieldSkins.isEmpty()) {
                statement.setString(19, "");
            } else {
                StringBuilder s = new StringBuilder();

                for (int i : this._shieldSkins) {
                    s.append(i).append(",");
                }

                statement.setString(19, s.toString());
            }

            statement.setInt(20, this.getShieldSkinOption());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("Could not store storeDressMeData():");
            e.printStackTrace();
        }

    }

    private void restoreDressMeData() {
        try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT obj_Id, armor_skins, armor_skin_option, weapon_skins, weapon_skin_option, hair_skins, hair_skin_option, face_skins, face_skin_option, shield_skins, shield_skin_option FROM characters_dressme_data WHERE obj_id=?");) {
            statement.setInt(1, this.getObjectId());

            try (ResultSet rset = statement.executeQuery()) {
                for (; rset.next(); this.setShieldSkinOption(rset.getInt("shield_skin_option"))) {
                    String armorskinIds = rset.getString("armor_skins");
                    if (armorskinIds != null && !armorskinIds.isEmpty()) {
                        for (String s : armorskinIds.split(",")) {
                            this.buyArmorSkin(Integer.parseInt(s));
                        }
                    }

                    this.setArmorSkinOption(rset.getInt("armor_skin_option"));
                    String weaponskinIds = rset.getString("weapon_skins");
                    if (weaponskinIds != null && !weaponskinIds.isEmpty()) {
                        for (String s : weaponskinIds.split(",")) {
                            this.buyWeaponSkin(Integer.parseInt(s));
                        }
                    }

                    this.setWeaponSkinOption(rset.getInt("weapon_skin_option"));
                    String hairskinIds = rset.getString("hair_skins");
                    if (hairskinIds != null && !hairskinIds.isEmpty()) {
                        for (String s : hairskinIds.split(",")) {
                            this.buyHairSkin(Integer.parseInt(s));
                        }
                    }

                    this.setHairSkinOption(rset.getInt("hair_skin_option"));
                    String faceskinIds = rset.getString("face_skins");
                    if (faceskinIds != null && !faceskinIds.isEmpty()) {
                        for (String s : faceskinIds.split(",")) {
                            this.buyFaceSkin(Integer.parseInt(s));
                        }
                    }

                    this.setFaceSkinOption(rset.getInt("face_skin_option"));
                    String shieldkinIds = rset.getString("shield_skins");
                    if (shieldkinIds != null && !shieldkinIds.isEmpty()) {
                        for (String s : shieldkinIds.split(",")) {
                            this.buyShieldSkin(Integer.parseInt(s));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not restore char data:");
            e.printStackTrace();
        }

    }

    public boolean isTryingSkin() {
        return this.isTryingSkin;
    }

    public void setIsTryingSkin(boolean b) {
        this.isTryingSkin = b;
    }

    public boolean isTryingSkinPremium() {
        return this.isTryingSkinPremium;
    }

    public void setIsTryingSkinPremium(boolean b) {
        this.isTryingSkinPremium = b;
    }

    public boolean hasArmorSkin(int skin) {
        return this._armorSkins.contains(skin);
    }

    public boolean hasWeaponSkin(int skin) {
        return this._weaponSkins.contains(skin);
    }

    public boolean hasHairSkin(int skin) {
        return this._hairSkins.contains(skin);
    }

    public boolean hasFaceSkin(int skin) {
        return this._faceSkins.contains(skin);
    }

    public boolean hasShieldSkin(int skin) {
        return this._shieldSkins.contains(skin);
    }

    public boolean hasEquippedArmorSkin(String skin) {
        return String.valueOf(this._armorSkinOption).contains(String.valueOf(skin));
    }

    public boolean hasEquippedWeaponSkin(String skin) {
        return String.valueOf(this._weaponSkinOption).contains(String.valueOf(skin));
    }

    public boolean hasEquippedHairSkin(String skin) {
        return String.valueOf(this._hairSkinOption).contains(String.valueOf(skin));
    }

    public boolean hasEquippedFaceSkin(String skin) {
        return String.valueOf(this._faceSkinOption).contains(String.valueOf(skin));
    }

    public boolean hasEquippedShieldSkin(String skin) {
        return String.valueOf(this._shieldSkinOption).contains(String.valueOf(skin));
    }

    public void buyArmorSkin(int id) {
        if (!this._armorSkins.contains(id)) {
            this._armorSkins.add(id);
        }

    }

    public void buyWeaponSkin(int id) {
        if (!this._weaponSkins.contains(id)) {
            this._weaponSkins.add(id);
        }

    }

    public void buyHairSkin(int id) {
        if (!this._hairSkins.contains(id)) {
            this._hairSkins.add(id);
        }

    }

    public void buyFaceSkin(int id) {
        if (!this._faceSkins.contains(id)) {
            this._faceSkins.add(id);
        }

    }

    public void buyShieldSkin(int id) {
        if (!this._shieldSkins.contains(id)) {
            this._shieldSkins.add(id);
        }

    }

    public int getArmorSkinOption() {
        return this._armorSkinOption;
    }

    public void setArmorSkinOption(int armorSkinOption) {
        this._armorSkinOption = armorSkinOption;
    }

    public int getWeaponSkinOption() {
        return this._weaponSkinOption;
    }

    public void setWeaponSkinOption(int weaponSkinOption) {
        this._weaponSkinOption = weaponSkinOption;
    }

    public int getHairSkinOption() {
        return this._hairSkinOption;
    }

    public void setHairSkinOption(int hairSkinOption) {
        this._hairSkinOption = hairSkinOption;
    }

    public int getFaceSkinOption() {
        return this._faceSkinOption;
    }

    public void setFaceSkinOption(int faceSkinOption) {
        this._faceSkinOption = faceSkinOption;
    }

    public int getShieldSkinOption() {
        return this._shieldSkinOption;
    }

    public void setShieldSkinOption(int shieldSkinOption) {
        this._shieldSkinOption = shieldSkinOption;
    }

    public void setHeroAura(boolean heroAura) {
        this.isPVPHero = heroAura;
    }

    public boolean getIsPVPHero() {
        return this.isPVPHero;
    }

    public boolean readyAchievementsList() {
        return !this._completedAchievements.isEmpty();
    }

    public void refreshCompletedAchievements() {
        this._completedAchievements.clear();
        try {
            Connection con = ConnectionPool.getConnection();

            PreparedStatement statement = con.prepareStatement("SELECT * from character_achievements WHERE owner_id = ? AND achieved_at IS NOT NULL");
            statement.setInt(1, this.getObjectId());

            ResultSet rs = statement.executeQuery();
            List<Integer> knownAchievements = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("achievement_id");
                knownAchievements.add(id);
                if (!this._completedAchievements.contains(id)) {
                    this._completedAchievements.add(id);
                }
            }
            rs.close();

            List<Integer> mustReplace = new ArrayList<>();
            for (int id : AchievementsManager.getInstance().getAchievementList().keySet()) {
                if (!knownAchievements.contains(id)) {
                    mustReplace.add(id);
                }
            }

            if (!mustReplace.isEmpty()) {
                for (int aid : mustReplace) {
                    statement = con.prepareStatement("REPLACE INTO character_achievements VALUES (?,?,?)");
                    statement.setInt(1, this.getObjectId());
                    statement.setInt(2, aid);
                    statement.setObject(3, null);
                    statement.execute();
                }
            }
            statement.close();

            con.close();
        } catch (SQLException var20) {
            LOGGER.error("[ACHIEVEMENTS ENGINE GETDATA]" + var20);
        }

    }

    public void saveAchievementData(int achievementID) {
        try {
            Connection con = ConnectionPool.getConnection();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO character_achievements VALUES (?,?,?)");
            statement.setInt(1, this.getObjectId());
            statement.setInt(2, achievementID);
            statement.setDate(3, new Date(Calendar.getInstance().getTimeInMillis()));
            statement.execute();

            statement.close();

            con.close();
        } catch (SQLException var11) {
            LOGGER.error("[ACHIEVEMENTS SAVE GETDATA]" + var11);
        }


    }

    public List<Integer> getCompletedAchievements() {
        return this._completedAchievements;
    }

    public boolean canUseAgathion() {
        return System.currentTimeMillis() >= this._nextAgathionUseTime;
    }

    public void setNextAgathionUseDelay(long delayMillis) {
        this._nextAgathionUseTime = System.currentTimeMillis() + delayMillis;
    }

    public Npc getAgathion() {
        return this._agathion;
    }

    public void setAgathion(Npc npc) {
        if (npc instanceof Agathion) {
            this.setAgathion(npc);
        }

    }

    public void setAgathion(Agathion agathion) {
        this._agathion = agathion;
        if (agathion != null) {
            this._lastAgathionNpcId = agathion.getNpcId();
        }

    }

    public void despawnAgathion() {
        if (this._agathion != null) {
            this._agathion.deleteMe();
            this._agathion = null;
        }

    }

    public long getLastAgathionUse() {
        return this._lastAgathionUse;
    }

    public void setLastAgathionUse(long time) {
        this._lastAgathionUse = time;
    }

    public void respawnAgathion() {
        if (this._lastAgathionNpcId > 0) {
            if (this.getAgathion() != null) {
                this.getAgathion().deleteMe();
                this.setAgathion(null);
            }

            AgathionData.spawnAgathion(this, this._lastAgathionNpcId);
        }

    }

    public void deleteTempItem(int itemObjectID) {
        boolean destroyed = false;
        if (this.getInventory().getItemByObjectId(itemObjectID) != null) {
            this.sendMessage("Your " + this.getInventory().getItemByObjectId(itemObjectID).getName() + " has expired.");
            this.destroyItem("tempItemDestroy", itemObjectID, 1, this, true);
            this.getInventory().updateDatabase();
            this.sendPacket(new ItemList(this, true));
            destroyed = true;
        }

        if (!destroyed) {
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");) {
                statement.setInt(1, itemObjectID);
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Dungeon getDungeon() {
        return this.dungeon;
    }

    public void setDungeon(Dungeon val) {
        this.dungeon = val;
    }

    public boolean isCommunityWarehouse() {
        return this.isCommunityWarehouse;
    }

    public void setCommunityWarehouse(boolean usingComsWH) {
        this.isCommunityWarehouse = usingComsWH;
    }

    protected class FeedTask implements Runnable {
        public void run() {
            if (!Player.this.isMounted()) {
                Player.this.stopFeed();
            } else if (Player.this.getCurrentFeed() > Player.this.getFeedConsume()) {
                Player.this.setCurrentFeed(Player.this.getCurrentFeed() - Player.this.getFeedConsume());
                ItemInstance food = Player.this.getInventory().getItemByItemId(Player.this._petTemplate.getFood1());
                if (food == null) {
                    food = Player.this.getInventory().getItemByItemId(Player.this._petTemplate.getFood2());
                }

                if (food != null && Player.this.checkFoodState(Player.this._petTemplate.getAutoFeedLimit())) {
                    IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
                    if (handler != null) {
                        handler.useItem(Player.this, food, false);
                        Player.this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food));
                    }
                }

            } else {
                Player.this.setCurrentFeed(0);
                Player.this.stopFeed();
                Player.this.dismount();
                Player.this.sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
            }
        }
    }
}
