package net.sf.l2j;

import enginemods.main.holders.RewardHolder;
import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.olympiad.enums.OlympiadPeriod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;

public final class Config {
    public static final String CLANS_FILE = "./config/clans.properties";
    public static final String CUSTOM_FILE = "./config/custom.properties";
    public static final String EVENTS_FILE = "./config/events.properties";
    public static final String GEOENGINE_FILE = "./config/geoengine.properties";
    public static final String HEXID_FILE = "./config/hexid.txt";
    public static final String LOGIN_CONFIGURATION_FILE = "./config/loginserver.properties";
    public static final String MULTIFUNCTIONZONE_FILE = "./config/multiFunctionzone.properties";
    public static final String NPCS_FILE = "./config/npcs.properties";
    public static final String PLAYERS_FILE = "./config/players.properties";
    public static final String D_GUARD_FILE = "./config/hwid.properties";
    public static final String SERVER_FILE = "./config/server.properties";
    public static final String SIEGE_FILE = "./config/siege.properties";
    public static final String EVENTENGINE_FILE = "./config/events/eventengine.properties";
    public static final String PCBANGEVENT = "./config/events/pcBangEvent.properties";
    public static final String PVP_EVENT_FILE = "./config/events/pvpEvent.properties";
    public static final String BOSS_EVENT_INSTANCED = "./config/events/killTheBossEvent.properties";
    public static final String TOUR_FILE = "./config/events/tournament.properties";
    public static final String PARTYFARM_FILE = "./config/events/partyfarm.properties";
    private static final CLogger LOGGER = new CLogger(Config.class.getName());
    public static int ALT_CLAN_JOIN_DAYS;

    public static int ALT_CLAN_CREATE_DAYS;

    public static int ALT_CLAN_DISSOLVE_DAYS;

    public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;

    public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;

    public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;

    public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;

    public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;

    public static int ALT_CLAN_MEMBERS_FOR_WAR;

    public static int ALT_CLAN_WAR_PENALTY_WHEN_ENDED;

    public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;

    public static int ALT_MANOR_REFRESH_TIME;

    public static int ALT_MANOR_REFRESH_MIN;

    public static int ALT_MANOR_APPROVE_TIME;

    public static int ALT_MANOR_APPROVE_MIN;

    public static int ALT_MANOR_MAINTENANCE_MIN;

    public static int ALT_MANOR_SAVE_PERIOD_RATE;

    public static long CH_TELE_FEE_RATIO;

    public static int CH_TELE1_FEE;

    public static int CH_TELE2_FEE;

    public static long CH_ITEM_FEE_RATIO;

    public static int CH_ITEM1_FEE;

    public static int CH_ITEM2_FEE;

    public static int CH_ITEM3_FEE;

    public static long CH_MPREG_FEE_RATIO;

    public static int CH_MPREG1_FEE;

    public static int CH_MPREG2_FEE;

    public static int CH_MPREG3_FEE;

    public static int CH_MPREG4_FEE;

    public static int CH_MPREG5_FEE;

    public static long CH_HPREG_FEE_RATIO;

    public static int CH_HPREG1_FEE;

    public static int CH_HPREG2_FEE;

    public static int CH_HPREG3_FEE;

    public static int CH_HPREG4_FEE;

    public static int CH_HPREG5_FEE;

    public static int CH_HPREG6_FEE;

    public static int CH_HPREG7_FEE;

    public static int CH_HPREG8_FEE;

    public static int CH_HPREG9_FEE;

    public static int CH_HPREG10_FEE;

    public static int CH_HPREG11_FEE;

    public static int CH_HPREG12_FEE;

    public static int CH_HPREG13_FEE;

    public static long CH_EXPREG_FEE_RATIO;

    public static int CH_EXPREG1_FEE;

    public static int CH_EXPREG2_FEE;

    public static int CH_EXPREG3_FEE;

    public static int CH_EXPREG4_FEE;

    public static int CH_EXPREG5_FEE;

    public static int CH_EXPREG6_FEE;

    public static int CH_EXPREG7_FEE;

    public static long CH_SUPPORT_FEE_RATIO;

    public static int CH_SUPPORT1_FEE;

    public static int CH_SUPPORT2_FEE;

    public static int CH_SUPPORT3_FEE;

    public static int CH_SUPPORT4_FEE;

    public static int CH_SUPPORT5_FEE;

    public static int CH_SUPPORT6_FEE;

    public static int CH_SUPPORT7_FEE;

    public static int CH_SUPPORT8_FEE;

    public static long CH_CURTAIN_FEE_RATIO;

    public static int CH_CURTAIN1_FEE;

    public static int CH_CURTAIN2_FEE;

    public static long CH_FRONT_FEE_RATIO;

    public static int CH_FRONT1_FEE;

    public static int CH_FRONT2_FEE;

    public static boolean ENABLE_MODIFY_SKILL_DURATION;

    public static HashMap<Integer, Integer> SKILL_DURATION_LIST;

    public static int ADENA_TO_GOLDBAR;

    public static boolean ENABLE_KILLS_LIMIT_A;

    public static int PVP_A_GRADE;

    public static boolean ENABLE_KILLS_LIMIT_S;

    public static int PVP_S_GRADE;

    public static boolean ENABLE_EFFECT_ON_LOGIN;

    public static boolean ENABLE_EFFECT_ON_DIE;

    public static boolean CANCEL_RETURN;

    public static int CANCEL_SECONDS;

    public static boolean INFINITY_SS;

    public static boolean INFINITY_ARROWS;

    public static int MAX_PATK_SPEED;

    public static int MAX_MATK_SPEED;

    public static boolean SOLOBOSS_EVENT_ENABLE;

    public static int SOLOBOSS_REWARD_ID;

    public static boolean OLY_USE_CUSTOM_PERIOD_SETTINGS;

    public static OlympiadPeriod OLY_PERIOD;

    public static int OLY_PERIOD_MULTIPLIER;

    public static boolean ENABLE_RAIDBOSS_NOBLES;

    public static int RAIDBOSS_NOBLES_ID;

    public static int RAID_BOSS_INFO_PAGE_LIMIT;

    public static int RAID_BOSS_DROP_PAGE_LIMIT;

    public static String RAID_BOSS_DATE_FORMAT;

    public static String RAID_BOSS_IDS;

    public static List<Integer> LIST_RAID_BOSS_IDS;

    public static boolean ALLOW_DIRECT_TP_TO_BOSS_ROOM;

    public static boolean ENABLE_AUTO_PVP_ZONE;

    public static int UNSTUCK_TIME;

    public static int REWARD_BOX_ID;

    public static List<RewardHolder> REWARD_BOX_REWARDS = new ArrayList<>();

    public static int REWARD_BOX_ID_1;

    public static List<RewardHolder> REWARD_BOX_REWARDS_1 = new ArrayList<>();

    public static int REWARD_BOX_ID_2;

    public static List<RewardHolder> REWARD_BOX_REWARDS_2 = new ArrayList<>();

    public static int REWARD_BOX_ID_3;

    public static List<RewardHolder> REWARD_BOX_REWARDS_3 = new ArrayList<>();

    public static boolean ALLOW_DAILY_REWARD;

    public static List<RewardHolder> DAILY_LOG_REWARDS = new ArrayList<>();

    public static String[] DAILY_REWARD_RESET_TIME;

    public static boolean ALLOW_DRESS_ME_SYSTEM;

    public static String DRESS_ME_COMMAND;

    public static boolean ALLOW_DRESS_ME_FOR_PREMIUM;

    public static boolean ALLOW_DRESS_ME_IN_OLY;

    public static boolean WAR_LEGEND_AURA;

    public static int KILLS_TO_GET_WAR_LEGEND_AURA;

    public static int NEWBIE_LVL;

    public static String NEWBIE_MAGE_SET;

    public static int[] NEWBIE_MAGE_BUFFS;

    public static String NEWBIE_FIGHTER_SET;

    public static int[] NEWBIE_FIGHTER_BUFFS;

    public static int SETX;

    public static int SETY;

    public static int SETZ;

    public static long AGATHION_USE_DELAY = 5000L;

    public static boolean ENABLE_AGATHION_SYSTEM;

    public static int AGATHION_RESPAWN_DELAY;

    public static boolean AGATHION_BUFF;

    public static boolean BALANCER_ALLOW;

    public static int CLAN_ITEM_ID_LEVEL_UP;

    public static int CLAN_COUNT_LEVEL_UP;

    public static int CLAN_ITEM_ID_REPUTATION;

    public static int CLAN_COUNT_REPUTATION;

    public static boolean AUTOFARM_WITHOUT_RESTRICTION;

    public static boolean AUTOFARM_L2AutoFarmZone;

    public static boolean AUTOFARM_L2PartyFarmZone;

    public static boolean AUTOFARM_PeaceZone;

    public static Location TUTORIAL_QUEST_FARM_LOCATION;

    public static Location TUTORIAL_QUEST_TOWN_LOCATION;

    public static boolean WELLCOME_MESSAGE_ACTIVE;

    public static String WELLCOME_SERVER_NAME;

    public static String WELLCOME_SERVER_SECOND_MESSAGE;

    public static boolean LEVEL_REWARDS_ENABLE;

    public static HashMap<Integer, IntIntHolder> LEVEL_REWARDS;

    public static boolean PVP_ENABLED;

    public static int[][] SPAWN_LOC;

    public static int REVIVE_DELAY;

    public static boolean REVIVE;

    public static boolean GIVE_NOBLES;

    public static List<String> ITEMS = new ArrayList<>();

    public static List<String> CLASSES = new ArrayList<>();

    public static List<String> GRADES = new ArrayList<>();

    public static Map<Integer, Integer> REWARDS = new HashMap<>();

    public static int RADIUS;

    public static int ENCHANT;

    public static boolean REMOVE_BUFFS;

    public static boolean REMOVE_PETS;

    public static boolean RESTART_ZONE;

    public static boolean STORE_ZONE;

    public static boolean LOGOUT_ZONE;

    public static boolean REVIVE_NOBLES;

    public static boolean REVIVE_HEAL;

    public static int ALT_OLY_START_TIME;

    public static int ALT_OLY_MIN;

    public static long ALT_OLY_CPERIOD;

    public static long ALT_OLY_BATTLE;

    public static long ALT_OLY_WPERIOD;

    public static long ALT_OLY_VPERIOD;

    public static int ALT_OLY_WAIT_TIME;

    public static int ALT_OLY_WAIT_BATTLE;

    public static int ALT_OLY_WAIT_END;

    public static int ALT_OLY_START_POINTS;

    public static int ALT_OLY_WEEKLY_POINTS;

    public static int ALT_OLY_MIN_MATCHES;

    public static int ALT_OLY_CLASSED;

    public static int ALT_OLY_NONCLASSED;

    public static IntIntHolder[] ALT_OLY_CLASSED_REWARD;

    public static IntIntHolder[] ALT_OLY_NONCLASSED_REWARD;

    public static int ALT_OLY_GP_PER_POINT;

    public static int ALT_OLY_HERO_POINTS;

    public static int ALT_OLY_RANK1_POINTS;

    public static int ALT_OLY_RANK2_POINTS;

    public static int ALT_OLY_RANK3_POINTS;

    public static int ALT_OLY_RANK4_POINTS;

    public static int ALT_OLY_RANK5_POINTS;

    public static int ALT_OLY_MAX_POINTS;

    public static int ALT_OLY_DIVIDER_CLASSED;

    public static int ALT_OLY_DIVIDER_NON_CLASSED;

    public static boolean ALT_OLY_ANNOUNCE_GAMES;

    public static boolean ALT_GAME_CASTLE_DAWN;

    public static boolean ALT_GAME_CASTLE_DUSK;

    public static int ALT_FESTIVAL_MIN_PLAYER;

    public static int ALT_MAXIMUM_PLAYER_CONTRIB;

    public static long ALT_FESTIVAL_MANAGER_START;

    public static long ALT_FESTIVAL_LENGTH;

    public static long ALT_FESTIVAL_CYCLE_LENGTH;

    public static long ALT_FESTIVAL_FIRST_SPAWN;

    public static long ALT_FESTIVAL_FIRST_SWARM;

    public static long ALT_FESTIVAL_SECOND_SPAWN;

    public static long ALT_FESTIVAL_SECOND_SWARM;

    public static long ALT_FESTIVAL_CHEST_SPAWN;

    public static int FS_TIME_ENTRY;

    public static int FS_TIME_END;

    public static int FS_PARTY_MEMBER_COUNT;

    public static int RIFT_MIN_PARTY_SIZE;

    public static int RIFT_SPAWN_DELAY;

    public static int RIFT_MAX_JUMPS;

    public static int RIFT_AUTO_JUMPS_TIME_MIN;

    public static int RIFT_AUTO_JUMPS_TIME_MAX;

    public static int RIFT_ENTER_COST_RECRUIT;

    public static int RIFT_ENTER_COST_SOLDIER;

    public static int RIFT_ENTER_COST_OFFICER;

    public static int RIFT_ENTER_COST_CAPTAIN;

    public static int RIFT_ENTER_COST_COMMANDER;

    public static int RIFT_ENTER_COST_HERO;

    public static double RIFT_BOSS_ROOM_TIME_MUTIPLY;

    public static boolean ALLOW_WEDDING;

    public static int WEDDING_PRICE;

    public static boolean WEDDING_SAMESEX;

    public static boolean WEDDING_FORMALWEAR;

    public static int ALT_LOTTERY_PRIZE;

    public static int ALT_LOTTERY_TICKET_PRICE;

    public static double ALT_LOTTERY_5_NUMBER_RATE;

    public static double ALT_LOTTERY_4_NUMBER_RATE;

    public static double ALT_LOTTERY_3_NUMBER_RATE;

    public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;

    public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;

    public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;

    public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;

    public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;

    public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;

    public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;

    public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;

    public static String GEODATA_PATH;

    public static int PART_OF_CHARACTER_HEIGHT;

    public static int MAX_OBSTACLE_HEIGHT;

    public static String PATHFIND_BUFFERS;

    public static int BASE_WEIGHT;

    public static int DIAGONAL_WEIGHT;

    public static int HEURISTIC_WEIGHT;

    public static int OBSTACLE_MULTIPLIER;

    public static int MAX_ITERATIONS;

    public static boolean DEBUG_PATH;

    public static boolean DEBUG_GEO_NODE;

    public static int SERVER_ID;

    public static byte[] HEX_ID;

    public static String LOGIN_BIND_ADDRESS;

    public static int PORT_LOGIN;

    public static int LOGIN_TRY_BEFORE_BAN;

    public static int LOGIN_BLOCK_AFTER_BAN;

    public static boolean ACCEPT_NEW_GAMESERVER;

    public static boolean SHOW_LICENCE;

    public static boolean AUTO_CREATE_ACCOUNTS;

    public static boolean LOG_LOGIN_CONTROLLER;

    public static boolean FLOOD_PROTECTION;

    public static int FAST_CONNECTION_LIMIT;

    public static int NORMAL_CONNECTION_TIME;

    public static int FAST_CONNECTION_TIME;

    public static int MAX_CONNECTION_PER_IP;

    public static int CHAMPION_FREQUENCY;

    public static int CHAMP_MIN_LVL;

    public static int CHAMP_MAX_LVL;

    public static int CHAMPION_HP;

    public static int CHAMPION_REWARDS;

    public static int CHAMPION_ADENAS_REWARDS;

    public static double CHAMPION_HP_REGEN;

    public static double CHAMPION_ATK;

    public static double CHAMPION_SPD_ATK;

    public static int CHAMPION_REWARD;

    public static int CHAMPION_REWARD_ID;

    public static int CHAMPION_REWARD_QTY;

    public static int BUFFER_MAX_SCHEMES;

    public static int BUFFER_STATIC_BUFF_COST;

    public static boolean ALLOW_CLASS_MASTERS;

    public static boolean ALTERNATE_CLASS_MASTER;

    public static ClassMasterSettings CLASS_MASTER_SETTINGS;

    public static boolean ALLOW_ENTIRE_TREE;

    public static boolean REWARD_NOBLE_IN_3RCHANGE_JOB;

    public static boolean ANNOUNCE_MAMMON_SPAWN;

    public static boolean ALT_MOB_AGRO_IN_PEACEZONE;

    public static boolean SHOW_NPC_LVL;

    public static boolean SHOW_NPC_CREST;

    public static boolean SHOW_SUMMON_CREST;

    public static boolean WYVERN_ALLOW_UPGRADER;

    public static int WYVERN_REQUIRED_LEVEL;

    public static int WYVERN_REQUIRED_CRYSTALS;

    public static double RAID_HP_REGEN_MULTIPLIER;

    public static double RAID_MP_REGEN_MULTIPLIER;

    public static double RAID_DEFENCE_MULTIPLIER;

    public static int RAID_MINION_RESPAWN_TIMER;

    public static boolean RAID_DISABLE_CURSE;

    public static int SPAWN_INTERVAL_AQ;

    public static int RANDOM_SPAWN_TIME_AQ;

    public static int SPAWN_INTERVAL_ANTHARAS;

    public static int RANDOM_SPAWN_TIME_ANTHARAS;

    public static int WAIT_TIME_ANTHARAS;

    public static int SPAWN_INTERVAL_BAIUM;

    public static int RANDOM_SPAWN_TIME_BAIUM;

    public static int SPAWN_INTERVAL_CORE;

    public static int RANDOM_SPAWN_TIME_CORE;

    public static int SPAWN_INTERVAL_FRINTEZZA;

    public static int RANDOM_SPAWN_TIME_FRINTEZZA;

    public static int WAIT_TIME_FRINTEZZA;

    public static int SPAWN_INTERVAL_ORFEN;

    public static int RANDOM_SPAWN_TIME_ORFEN;

    public static int SPAWN_INTERVAL_SAILREN;

    public static int RANDOM_SPAWN_TIME_SAILREN;

    public static int WAIT_TIME_SAILREN;

    public static int SPAWN_INTERVAL_VALAKAS;

    public static int RANDOM_SPAWN_TIME_VALAKAS;

    public static int WAIT_TIME_VALAKAS;

    public static int SPAWN_INTERVAL_ZAKEN;

    public static int RANDOM_SPAWN_TIME_ZAKEN;

    public static String FWA_FIXTIMEPATTERNOFANTHARAS;

    public static String FWA_FIXTIMEPATTERNOFBAIUM;

    public static String FWA_FIXTIMEPATTERNOFCORE;

    public static String FWA_FIXTIMEPATTERNOFORFEN;

    public static String FWA_FIXTIMEPATTERNOFVALAKAS;

    public static String FWA_FIXTIMEPATTERNOFZAKEN;

    public static String FWA_FIXTIMEPATTERNOFQA;

    public static boolean GUARD_ATTACK_AGGRO_MOB;

    public static int MAX_DRIFT_RANGE;

    public static int MIN_NPC_ANIMATION;

    public static int MAX_NPC_ANIMATION;

    public static int MIN_MONSTER_ANIMATION;

    public static int MAX_MONSTER_ANIMATION;

    public static boolean EFFECT_CANCELING;

    public static double HP_REGEN_MULTIPLIER;

    public static double MP_REGEN_MULTIPLIER;

    public static double CP_REGEN_MULTIPLIER;

    public static int PLAYER_SPAWN_PROTECTION;

    public static int PLAYER_FAKEDEATH_UP_PROTECTION;

    public static double RESPAWN_RESTORE_HP;

    public static int MAX_PVTSTORE_SLOTS_DWARF;

    public static int MAX_PVTSTORE_SLOTS_OTHER;

    public static boolean DEEPBLUE_DROP_RULES;

    public static boolean ALT_GAME_DELEVEL;

    public static int DEATH_PENALTY_CHANCE;

    public static int INVENTORY_MAXIMUM_NO_DWARF;

    public static int INVENTORY_MAXIMUM_DWARF;

    public static int INVENTORY_MAXIMUM_QUEST_ITEMS;

    public static int INVENTORY_MAXIMUM_PET;

    public static int MAX_ITEM_IN_PACKET;

    public static double ALT_WEIGHT_LIMIT;

    public static int WAREHOUSE_SLOTS_NO_DWARF;

    public static int WAREHOUSE_SLOTS_DWARF;

    public static int WAREHOUSE_SLOTS_CLAN;

    public static int FREIGHT_SLOTS;

    public static boolean ALT_GAME_FREIGHTS;

    public static int ALT_GAME_FREIGHT_PRICE;

    public static int AUGMENTATION_NG_SKILL_CHANCE;

    public static int AUGMENTATION_NG_GLOW_CHANCE;

    public static int AUGMENTATION_MID_SKILL_CHANCE;

    public static int AUGMENTATION_MID_GLOW_CHANCE;

    public static int AUGMENTATION_HIGH_SKILL_CHANCE;

    public static int AUGMENTATION_HIGH_GLOW_CHANCE;

    public static int AUGMENTATION_TOP_SKILL_CHANCE;

    public static int AUGMENTATION_TOP_GLOW_CHANCE;

    public static int AUGMENTATION_BASESTAT_CHANCE;

    public static boolean KARMA_PLAYER_CAN_BE_KILLED_IN_PZ;

    public static boolean KARMA_PLAYER_CAN_SHOP;

    public static boolean KARMA_PLAYER_CAN_USE_GK;

    public static boolean KARMA_PLAYER_CAN_TELEPORT;

    public static boolean KARMA_PLAYER_CAN_TRADE;

    public static boolean KARMA_PLAYER_CAN_USE_WH;

    public static boolean KARMA_DROP_GM;

    public static boolean KARMA_AWARD_PK_KILL;

    public static int KARMA_PK_LIMIT;

    public static String KARMA_NONDROPPABLE_PET_ITEMS;

    public static String KARMA_NONDROPPABLE_ITEMS;

    public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;

    public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;

    public static int PVP_NORMAL_TIME;

    public static int PVP_PVP_TIME;

    public static String PARTY_XP_CUTOFF_METHOD;

    public static int PARTY_XP_CUTOFF_LEVEL;

    public static double PARTY_XP_CUTOFF_PERCENT;

    public static int PARTY_RANGE;

    public static int DEFAULT_ACCESS_LEVEL;

    public static boolean GM_HERO_AURA;

    public static boolean GM_STARTUP_INVULNERABLE;

    public static boolean GM_STARTUP_INVISIBLE;

    public static boolean GM_STARTUP_SILENCE;

    public static boolean GM_STARTUP_AUTO_LIST;

    public static boolean GM_SUPER_HASTE;

    public static boolean PETITIONING_ALLOWED;

    public static int MAX_PETITIONS_PER_PLAYER;

    public static int MAX_PETITIONS_PENDING;

    public static boolean IS_CRAFTING_ENABLED;

    public static int DWARF_RECIPE_LIMIT;

    public static int COMMON_RECIPE_LIMIT;

    public static boolean ALT_BLACKSMITH_USE_RECIPES;

    public static boolean AUTO_LEARN_SKILLS;

    public static boolean MAGIC_FAILURES;

    public static int PERFECT_SHIELD_BLOCK_RATE;

    public static boolean LIFE_CRYSTAL_NEEDED;

    public static boolean SP_BOOK_NEEDED;

    public static boolean ES_SP_BOOK_NEEDED;

    public static boolean DIVINE_SP_BOOK_NEEDED;

    public static boolean SUBCLASS_WITHOUT_QUESTS;

    public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;

    public static int ALLOWED_SUBCLASS;

    public static int SUBCLASS_MAX_LEVEL;

    public static boolean STORE_SKILL_COOLTIME;

    public static int MAX_BUFFS_AMOUNT;

    public static int SIEGE_LENGTH;

    public static int MINIMUM_CLAN_LEVEL;

    public static int MAX_ATTACKERS_NUMBER;

    public static int MAX_DEFENDERS_NUMBER;

    public static int ATTACKERS_RESPAWN_DELAY;

    public static int DAY_TO_SIEGE;

    public static int SIEGE_HOUR_GLUDIO;

    public static int SIEGE_HOUR_DION;

    public static int SIEGE_HOUR_GIRAN;

    public static int SIEGE_HOUR_OREN;

    public static int SIEGE_HOUR_ADEN;

    public static int SIEGE_HOUR_INNADRIL;

    public static int SIEGE_HOUR_GODDARD;

    public static int SIEGE_HOUR_RUNE;

    public static int SIEGE_HOUR_SCHUT;

    public static boolean SIEGE_GLUDIO;

    public static boolean SIEGE_DION;

    public static boolean SIEGE_GIRAN;

    public static boolean SIEGE_OREN;

    public static boolean SIEGE_ADEN;

    public static boolean SIEGE_INNADRIL;

    public static boolean SIEGE_GODDARD;

    public static boolean SIEGE_RUNE;

    public static boolean SIEGE_SCHUT;

    public static int SIEGE_DAY_GLUDIO;

    public static int SIEGE_DAY_DION;

    public static int SIEGE_DAY_GIRAN;

    public static int SIEGE_DAY_OREN;

    public static int SIEGE_DAY_ADEN;

    public static int SIEGE_DAY_INNADRIL;

    public static int SIEGE_DAY_GODDARD;

    public static int SIEGE_DAY_RUNE;

    public static int SIEGE_DAY_SCHUT;

    public static String GAMESERVER_HOSTNAME;

    public static int PORT_GAME;

    public static String HOSTNAME;

    public static int GAME_SERVER_LOGIN_PORT;

    public static String GAME_SERVER_LOGIN_HOST;

    public static int REQUEST_ID;

    public static boolean ACCEPT_ALTERNATE_ID;

    public static String DATABASE_URL;

    public static String DATABASE_LOGIN;

    public static String DATABASE_PASSWORD;

    public static int DATABASE_MAX_CONNECTIONS;

    public static boolean SERVER_LIST_BRACKET;

    public static boolean SERVER_LIST_CLOCK;

    public static int SERVER_LIST_AGE;

    public static boolean SERVER_LIST_TESTSERVER;

    public static boolean SERVER_LIST_PVPSERVER;

    public static boolean SERVER_GMONLY;

    public static int DELETE_DAYS;

    public static int MAXIMUM_ONLINE_USERS;

    public static boolean AUTO_LOOT;

    public static boolean AUTO_LOOT_HERBS;

    public static boolean AUTO_LOOT_RAID;

    public static boolean ALLOW_DISCARDITEM;

    public static boolean MULTIPLE_ITEM_DROP;

    public static int HERB_AUTO_DESTROY_TIME;

    public static int ITEM_AUTO_DESTROY_TIME;

    public static int EQUIPABLE_ITEM_AUTO_DESTROY_TIME;

    public static Map<Integer, Integer> SPECIAL_ITEM_DESTROY_TIME;

    public static int PLAYER_DROPPED_ITEM_MULTIPLIER;

    public static double RATE_XP;

    public static double RATE_SP;

    public static double RATE_PARTY_XP;

    public static double RATE_PARTY_SP;

    public static double RATE_DROP_ADENA;

    public static double RATE_DROP_ITEMS;

    public static double RATE_DROP_ITEMS_BY_RAID;

    public static double RATE_DROP_SPOIL;

    public static int RATE_DROP_MANOR;

    public static double RATE_QUEST_DROP;

    public static double RATE_QUEST_REWARD;

    public static double RATE_QUEST_REWARD_XP;

    public static double RATE_QUEST_REWARD_SP;

    public static double RATE_QUEST_REWARD_ADENA;

    public static double RATE_KARMA_EXP_LOST;

    public static double RATE_SIEGE_GUARDS_PRICE;

    public static int PLAYER_DROP_LIMIT;

    public static int PLAYER_RATE_DROP;

    public static int PLAYER_RATE_DROP_ITEM;

    public static int PLAYER_RATE_DROP_EQUIP;

    public static int PLAYER_RATE_DROP_EQUIP_WEAPON;

    public static int KARMA_DROP_LIMIT;

    public static int KARMA_RATE_DROP;

    public static int KARMA_RATE_DROP_ITEM;

    public static int KARMA_RATE_DROP_EQUIP;

    public static int KARMA_RATE_DROP_EQUIP_WEAPON;

    public static double PET_XP_RATE;

    public static int PET_FOOD_RATE;

    public static double SINEATER_XP_RATE;

    public static double RATE_DROP_COMMON_HERBS;

    public static double RATE_DROP_HP_HERBS;

    public static double RATE_DROP_MP_HERBS;

    public static double RATE_DROP_SPECIAL_HERBS;

    public static boolean ALLOW_FREIGHT;

    public static boolean ALLOW_WAREHOUSE;

    public static boolean ALLOW_WEAR;

    public static int WEAR_DELAY;

    public static int WEAR_PRICE;

    public static boolean ALLOW_LOTTERY;

    public static boolean ALLOW_WATER;

    public static boolean ALLOW_BOAT;

    public static boolean ALLOW_CURSED_WEAPONS;

    public static boolean ALLOW_MANOR;

    public static boolean ENABLE_FALLING_DAMAGE;

    public static boolean ALT_DEV_NO_SPAWNS;

    public static boolean DEVELOPER;

    public static boolean PACKET_HANDLER_DEBUG;

    public static int DEBUG_MOVEMENT;

    public static boolean DEADLOCK_DETECTOR;

    public static int DEADLOCK_CHECK_INTERVAL;

    public static boolean RESTART_ON_DEADLOCK;

    public static boolean LOG_CHAT;

    public static boolean LOG_ITEMS;

    public static boolean GMAUDIT;

    public static boolean ENABLE_COMMUNITY_BOARD;

    public static String BBS_DEFAULT;

    public static int ROLL_DICE_TIME;

    public static int HERO_VOICE_TIME;

    public static int SUBCLASS_TIME;

    public static int DROP_ITEM_TIME;

    public static int SERVER_BYPASS_TIME;

    public static int MULTISELL_TIME;

    public static int MANUFACTURE_TIME;

    public static int MANOR_TIME;

    public static int SENDMAIL_TIME;

    public static int CHARACTER_SELECT_TIME;

    public static int GLOBAL_CHAT_TIME;

    public static int TRADE_CHAT_TIME;

    public static int SOCIAL_TIME;

    public static int SCHEDULED_THREAD_POOL_COUNT;

    public static int THREADS_PER_SCHEDULED_THREAD_POOL;

    public static int INSTANT_THREAD_POOL_COUNT;

    public static int THREADS_PER_INSTANT_THREAD_POOL;

    public static boolean L2WALKER_PROTECTION;

    public static boolean SERVER_NEWS;

    public static int ZONE_TOWN;

    public static boolean DISABLE_TUTORIAL;

    public static boolean ENABLE_EVENT_ENGINE;

    public static int TIME_BETWEEN_EVENTS;

    public static int EVENT_REGISTRATION_TIME;

    public static boolean ALLOW_DM_EVENT;

    public static String[] DM_EVENT_INTERVAL;

    public static int DM_MIN_PLAYERS;

    public static Map<Integer, Integer> DM_ON_KILL_REWARDS = new HashMap<>();

    public static Map<Integer, Integer> DM_WINNER_REWARDS = new HashMap<>();

    public static int DM_RUNNING_TIME;

    public static List<Location> DM_RESPAWN_SPOTS = new ArrayList<>();

    public static boolean ALLOW_TVT_EVENT;

    public static String[] TVT_EVENT_INTERVAL;

    public static int TVT_MIN_PLAYERS;

    public static Map<Integer, Integer> TVT_WINNER_REWARDS = new HashMap<>();

    public static Map<Integer, Integer> TVT_DRAW_REWARDS = new HashMap<>();

    public static int TVT_RUNNING_TIME;

    public static String TVT_TEAM_1_NAME;

    public static int TVT_TEAM_1_COLOR;

    public static Location TVT_TEAM_1_LOCATION;

    public static String TVT_TEAM_2_NAME;

    public static int TVT_TEAM_2_COLOR;

    public static Location TVT_TEAM_2_LOCATION;

    public static boolean ALLOW_CTF_EVENT;

    public static String[] CTF_EVENT_INTERVAL;

    public static int CTF_MIN_PLAYERS;

    public static Map<Integer, Integer> CTF_ON_SCORE_REWARDS = new HashMap<>();

    public static Map<Integer, Integer> CTF_WINNER_REWARDS = new HashMap<>();

    public static Map<Integer, Integer> CTF_DRAW_REWARDS;

    public static int CTF_RUNNING_TIME;

    public static String CTF_TEAM_1_NAME;

    public static int CTF_TEAM_1_COLOR;

    public static Location CTF_TEAM_1_LOCATION;

    public static Location CTF_TEAM_1_FLAG_LOCATION;

    public static String CTF_TEAM_2_NAME;

    public static int CTF_TEAM_2_COLOR;

    public static Location CTF_TEAM_2_LOCATION;

    public static Location CTF_TEAM_2_FLAG_LOCATION;

    public static int PCB_MIN_LEVEL;

    public static int PCB_POINT_MIN;

    public static int PCB_POINT_MAX;

    public static int PCB_CHANCE_DUAL_POINT;

    public static int PCB_INTERVAL;

    public static int PCB_COIN_ID;

    public static boolean PCB_ENABLE;

    public static String NAME_PVP;

    public static boolean PVP_EVENT_ENABLED;

    public static String[] PVP_EVENT_INTERVAL;

    public static int PVP_EVENT_RUNNING_TIME;

    public static int[][] PVP_EVENT_REWARDS;

    public static boolean ALLOW_SPECIAL_PVP_REWARD;

    public static List<int[]> PVP_SPECIAL_ITEMS_REWARD;

    public static boolean BOSS_EVENT_TIME_ON_SCREEN;

    public static int BOSS_EVENT_TIME_TO_DESPAWN_BOSS;

    public static int BOSS_EVENT_REGISTRATION_NPC_ID;

    public static Map<Integer, Integer> BOSS_EVENT_GENERAL_REWARDS = new HashMap<>();

    public static Map<Integer, Integer> BOSS_EVENT_LAST_ATTACKER_REWARDS = new HashMap<>();

    public static Map<Integer, Integer> BOSS_EVENT_MAIN_DAMAGE_DEALER_REWARDS = new HashMap<>();

    public static boolean BOSS_EVENT_REWARD_MAIN_DAMAGE_DEALER;

    public static boolean BOSS_EVENT_REWARD_LAST_ATTACKER;

    public static List<Location> BOSS_EVENT_LOCATION = new ArrayList<>();

    public static int BOSS_EVENT_REWARD_ID;

    public static int BOSS_EVENT_REWARD_COUNT;

    public static int BOSS_EVENT_MIN_DAMAGE_TO_OBTAIN_REWARD;

    public static List<Integer> BOSS_EVENT_ID = new ArrayList<>();

    public static Location BOSS_EVENT_NPC_REGISTER_LOC;

    public static int BOSS_EVENT_TIME_TO_WAIT;

    public static int BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS;

    public static int BOSS_EVENT_MIN_PLAYERS;

    public static int BOSS_EVENT_REGISTRATION_TIME;

    public static String[] BOSS_EVENT_BY_TIME_OF_DAY;

    public static boolean TOURNAMENT_EVENT_START;

    public static boolean TOURNAMENT_EVENT_TIME;

    public static boolean TOURNAMENT_EVENT_SUMMON;

    public static boolean TOURNAMENT_EVENT_ANNOUNCE;

    public static int TOURNAMENT_TIME;

    public static String[] TOURNAMENT_EVENT_INTERVAL_BY_TIME_OF_DAY;

    public static String TITLE_COLOR_TEAM1;

    public static String TITLE_COLOR_TEAM2;

    public static String MSG_TEAM1;

    public static String MSG_TEAM2;

    public static boolean Allow_Same_HWID_On_Tournament;

    public static int ARENA_NPC;

    public static int NPC_locx;

    public static int NPC_locy;

    public static int NPC_locz;

    public static int NPC_Heading;

    public static int Tournament_locx;

    public static int Tournament_locy;

    public static int Tournament_locz;

    public static boolean ALLOW_2X2_REGISTER;

    public static boolean ALLOW_4X4_REGISTER;

    public static boolean ALLOW_9X9_REGISTER;

    public static boolean ALLOW_4X4_LOSTBUFF;

    public static boolean ARENA_MESSAGE_ENABLED;

    public static String ARENA_MESSAGE_TEXT;

    public static int ARENA_MESSAGE_TIME;

    public static int ARENA_EVENT_COUNT;

    public static int[][] ARENA_EVENT_LOCS;

    public static int ARENA_EVENT_COUNT_4X4;

    public static int[][] ARENA_EVENT_LOCS_4X4;

    public static int ARENA_EVENT_COUNT_9X9;

    public static int[][] ARENA_EVENT_LOCS_9X9;

    public static int duelist_COUNT_4X4;

    public static int dreadnought_COUNT_4X4;

    public static int tanker_COUNT_4X4;

    public static int dagger_COUNT_4X4;

    public static int archer_COUNT_4X4;

    public static int bs_COUNT_4X4;

    public static int archmage_COUNT_4X4;

    public static int soultaker_COUNT_4X4;

    public static int mysticMuse_COUNT_4X4;

    public static int stormScreamer_COUNT_4X4;

    public static int titan_COUNT_4X4;

    public static int dominator_COUNT_4X4;

    public static int doomcryer_COUNT_4X4;

    public static int duelist_COUNT_9X9;

    public static int dreadnought_COUNT_9X9;

    public static int tanker_COUNT_9X9;

    public static int dagger_COUNT_9X9;

    public static int archer_COUNT_9X9;

    public static int bs_COUNT_9X9;

    public static int archmage_COUNT_9X9;

    public static int soultaker_COUNT_9X9;

    public static int mysticMuse_COUNT_9X9;

    public static int stormScreamer_COUNT_9X9;

    public static int titan_COUNT_9X9;

    public static int grandKhauatari_COUNT_9X9;

    public static int dominator_COUNT_9X9;

    public static int doomcryer_COUNT_9X9;

    public static int ARENA_PVP_AMOUNT;

    public static int ARENA_REWARD_ID;

    public static int ARENA_WIN_REWARD_COUNT;

    public static int ARENA_LOST_REWARD_COUNT;

    public static int ARENA_WIN_REWARD_COUNT_4X4;

    public static int ARENA_LOST_REWARD_COUNT_4X4;

    public static int ARENA_WIN_REWARD_COUNT_9X9;

    public static int ARENA_LOST_REWARD_COUNT_9X9;

    public static int ARENA_CHECK_INTERVAL;

    public static int ARENA_CALL_INTERVAL;

    public static int ARENA_WAIT_INTERVAL_4X4;

    public static int ARENA_WAIT_INTERVAL_9X9;

    public static int ARENA_WAIT_INTERVAL;

    public static String TOURNAMENT_ID_RESTRICT;

    public static List<Integer> TOURNAMENT_LISTID_RESTRICT;

    public static boolean ARENA_SKILL_PROTECT;

    public static List<Integer> ARENA_SKILL_LIST = new ArrayList<>();

    public static List<Integer> ARENA_DISABLE_SKILL_LIST = new ArrayList<>();

    public static List<Integer> ARENA_STOP_SKILL_LIST = new ArrayList<>();

    public static List<Integer> ARENA_DISABLE_SKILL_LIST_PERM = new ArrayList<>();

    public static String NPC_LIST;

    public static Map<Integer, Integer> PARTY_DROP_REWARDS;

    public static int[] NPC_LIST_SET;

    public static String PARTY_FARM_TITLE;

    public static boolean ENABLE_DROP_PARTYFARM;

    public static Map<Integer, List<Integer>> PARTY_DROP_LIST = new HashMap<>();

    public static String PARTY_FARMANNONCER;

    public static boolean PARTY_MESSAGE_ENABLED;

    public static long NPC_SERVER_DELAY;

    public static boolean PARTY_FARM_BY_TIME_OF_DAY;

    public static boolean START_PARTY;

    public static int EVENT_BEST_FARM_TIME;

    public static String[] EVENT_BEST_FARM_INTERVAL_BY_TIME_OF_DAY;

    public static int PARTY_FARM_MONSTER_DALAY;

    public static String PARTY_FARM_MESSAGE_TEXT;

    public static int PARTY_FARM_MESSAGE_TIME;

    public static int monsterId;

    public static int MONSTER_LOCS_COUNT;

    public static int[][] MONSTER_LOCS;

    public static boolean RESERVE_HOST_ON_LOGIN = false;

    public static int MMO_SELECTOR_SLEEP_TIME = 20;

    public static int MMO_MAX_SEND_PER_PASS = 80;

    public static int MMO_MAX_READ_PER_PASS = 80;

    public static int MMO_HELPER_BUFFER_COUNT = 20;

    public static int CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2;

    public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1;

    public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 160;

    public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5;

    public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 80;

    public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2;

    public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1;

    public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1;

    public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5;

    public static ExProperties initProperties(String filename) {
        ExProperties result = new ExProperties();
        try {
            result.load(new File(filename));
        } catch (Exception e) {
            LOGGER.error("An error occured loading '{}' config.", e, filename);
        }
        return result;
    }

    private static void loadClans() {
        ExProperties clans = initProperties("./config/clans.properties");
        ALT_CLAN_JOIN_DAYS = clans.getProperty("DaysBeforeJoinAClan", 5);
        ALT_CLAN_CREATE_DAYS = clans.getProperty("DaysBeforeCreateAClan", 10);
        ALT_MAX_NUM_OF_CLANS_IN_ALLY = clans.getProperty("AltMaxNumOfClansInAlly", 3);
        ALT_CLAN_MEMBERS_FOR_WAR = clans.getProperty("AltClanMembersForWar", 15);
        ALT_CLAN_WAR_PENALTY_WHEN_ENDED = clans.getProperty("AltClanWarPenaltyWhenEnded", 5);
        ALT_CLAN_DISSOLVE_DAYS = clans.getProperty("DaysToPassToDissolveAClan", 7);
        ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = clans.getProperty("DaysBeforeJoinAllyWhenLeaved", 1);
        ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeJoinAllyWhenDismissed", 1);
        ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeAcceptNewClanWhenDismissed", 1);
        ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = clans.getProperty("DaysBeforeCreateNewAllyWhenDissolved", 10);
        ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = clans.getProperty("AltMembersCanWithdrawFromClanWH", false);
        ALT_MANOR_REFRESH_TIME = clans.getProperty("AltManorRefreshTime", 20);
        ALT_MANOR_REFRESH_MIN = clans.getProperty("AltManorRefreshMin", 0);
        ALT_MANOR_APPROVE_TIME = clans.getProperty("AltManorApproveTime", 6);
        ALT_MANOR_APPROVE_MIN = clans.getProperty("AltManorApproveMin", 0);
        ALT_MANOR_MAINTENANCE_MIN = clans.getProperty("AltManorMaintenanceMin", 6);
        ALT_MANOR_SAVE_PERIOD_RATE = clans.getProperty("AltManorSavePeriodRate", 2) * 3600000;
        CH_TELE_FEE_RATIO = clans.getProperty("ClanHallTeleportFunctionFeeRatio", 86400000);
        CH_TELE1_FEE = clans.getProperty("ClanHallTeleportFunctionFeeLvl1", 7000);
        CH_TELE2_FEE = clans.getProperty("ClanHallTeleportFunctionFeeLvl2", 14000);
        CH_SUPPORT_FEE_RATIO = clans.getProperty("ClanHallSupportFunctionFeeRatio", 86400000);
        CH_SUPPORT1_FEE = clans.getProperty("ClanHallSupportFeeLvl1", 17500);
        CH_SUPPORT2_FEE = clans.getProperty("ClanHallSupportFeeLvl2", 35000);
        CH_SUPPORT3_FEE = clans.getProperty("ClanHallSupportFeeLvl3", 49000);
        CH_SUPPORT4_FEE = clans.getProperty("ClanHallSupportFeeLvl4", 77000);
        CH_SUPPORT5_FEE = clans.getProperty("ClanHallSupportFeeLvl5", 147000);
        CH_SUPPORT6_FEE = clans.getProperty("ClanHallSupportFeeLvl6", 252000);
        CH_SUPPORT7_FEE = clans.getProperty("ClanHallSupportFeeLvl7", 259000);
        CH_SUPPORT8_FEE = clans.getProperty("ClanHallSupportFeeLvl8", 364000);
        CH_MPREG_FEE_RATIO = clans.getProperty("ClanHallMpRegenerationFunctionFeeRatio", 86400000);
        CH_MPREG1_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl1", 14000);
        CH_MPREG2_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl2", 26250);
        CH_MPREG3_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl3", 45500);
        CH_MPREG4_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl4", 96250);
        CH_MPREG5_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl5", 140000);
        CH_HPREG_FEE_RATIO = clans.getProperty("ClanHallHpRegenerationFunctionFeeRatio", 86400000);
        CH_HPREG1_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl1", 4900);
        CH_HPREG2_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl2", 5600);
        CH_HPREG3_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl3", 7000);
        CH_HPREG4_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl4", 8166);
        CH_HPREG5_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl5", 10500);
        CH_HPREG6_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl6", 12250);
        CH_HPREG7_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl7", 14000);
        CH_HPREG8_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl8", 15750);
        CH_HPREG9_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl9", 17500);
        CH_HPREG10_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl10", 22750);
        CH_HPREG11_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl11", 26250);
        CH_HPREG12_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl12", 29750);
        CH_HPREG13_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl13", 36166);
        CH_EXPREG_FEE_RATIO = clans.getProperty("ClanHallExpRegenerationFunctionFeeRatio", 86400000);
        CH_EXPREG1_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl1", 21000);
        CH_EXPREG2_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl2", 42000);
        CH_EXPREG3_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl3", 63000);
        CH_EXPREG4_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl4", 105000);
        CH_EXPREG5_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl5", 147000);
        CH_EXPREG6_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl6", 163331);
        CH_EXPREG7_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl7", 210000);
        CH_ITEM_FEE_RATIO = clans.getProperty("ClanHallItemCreationFunctionFeeRatio", 86400000);
        CH_ITEM1_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl1", 210000);
        CH_ITEM2_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl2", 490000);
        CH_ITEM3_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl3", 980000);
        CH_CURTAIN_FEE_RATIO = clans.getProperty("ClanHallCurtainFunctionFeeRatio", 86400000);
        CH_CURTAIN1_FEE = clans.getProperty("ClanHallCurtainFunctionFeeLvl1", 2002);
        CH_CURTAIN2_FEE = clans.getProperty("ClanHallCurtainFunctionFeeLvl2", 2625);
        CH_FRONT_FEE_RATIO = clans.getProperty("ClanHallFrontPlatformFunctionFeeRatio", 86400000);
        CH_FRONT1_FEE = clans.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", 3031);
        CH_FRONT2_FEE = clans.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", 9331);
    }

    private static void loadcustomproperties() {
        ExProperties customfile = initProperties("./config/custom.properties");
        ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(customfile.getProperty("EnableModifySkillDuration", "false"));
        if (ENABLE_MODIFY_SKILL_DURATION) {
            SKILL_DURATION_LIST = new HashMap<>();
            String[] infos = customfile.getProperty("SkillDurationList", "").split(";");
            for (String skill : infos) {
                String[] skillSplit = skill.split(",");
                if (skillSplit.length != 2) {
                    LOGGER.warn("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
                } else {
                    try {
                        SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
                    } catch (NumberFormatException nfe) {
                        if (DEVELOPER)
                            nfe.printStackTrace();
                        if (!skill.isEmpty())
                            LOGGER.warn("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
                    }
                }
            }
        }
        ADENA_TO_GOLDBAR = customfile.getProperty("AdenaToGoldBar", 100000);
        ENABLE_KILLS_LIMIT_A = customfile.getProperty("EnableAGradeLimit", true);
        PVP_A_GRADE = customfile.getProperty("KillsForAGradeItem", 150);
        ENABLE_KILLS_LIMIT_S = customfile.getProperty("EnableSGradeLimit", true);
        PVP_S_GRADE = customfile.getProperty("KillsForSGradeItem", 300);
        ENABLE_EFFECT_ON_LOGIN = Boolean.parseBoolean(customfile.getProperty("EnableEffectOnLogin", "false"));
        ENABLE_EFFECT_ON_DIE = Boolean.parseBoolean(customfile.getProperty("EnableEffectOnDie", "false"));
        CANCEL_RETURN = customfile.getProperty("EnableCancelReturn", false);
        CANCEL_SECONDS = customfile.getProperty("CancelSeconds", 5);
        INFINITY_SS = customfile.getProperty("InfinitySS", false);
        INFINITY_ARROWS = customfile.getProperty("InfinityArrows", false);
        MAX_PATK_SPEED = customfile.getProperty("MaxPAtkSpeed", 1400);
        MAX_MATK_SPEED = customfile.getProperty("MaxMAtkSpeed", 1999);
        SOLOBOSS_EVENT_ENABLE = customfile.getProperty("SoloBossEventEnable", false);
        SOLOBOSS_REWARD_ID = Integer.parseInt(customfile.getProperty("SoloBossRewardID", "3470"));
        OLY_USE_CUSTOM_PERIOD_SETTINGS = customfile.getProperty("OlyUseCustomPeriodSettings", false);
        OLY_PERIOD = OlympiadPeriod.valueOf(customfile.getProperty("OlyPeriod", "MONTH"));
        OLY_PERIOD_MULTIPLIER = customfile.getProperty("OlyPeriodMultiplier", 1);
        ENABLE_RAIDBOSS_NOBLES = Boolean.parseBoolean(customfile.getProperty("RaidBossNobles", "false"));
        RAIDBOSS_NOBLES_ID = Integer.parseInt(customfile.getProperty("RaidBossNoblesId", "25325"));
        RAID_BOSS_INFO_PAGE_LIMIT = customfile.getProperty("RaidBossInfoPageLimit", 15);
        RAID_BOSS_DROP_PAGE_LIMIT = customfile.getProperty("RaidBossDropPageLimit", 15);
        RAID_BOSS_DATE_FORMAT = customfile.getProperty("RaidBossDateFormat", "MMM dd, HH:mm");
        RAID_BOSS_IDS = customfile.getProperty("RaidBossIds", "0,0");
        LIST_RAID_BOSS_IDS = new ArrayList<>();
        for (String val : RAID_BOSS_IDS.split(",")) {
            int npcId = Integer.parseInt(val);
            LIST_RAID_BOSS_IDS.add(npcId);
        }
        ALLOW_DIRECT_TP_TO_BOSS_ROOM = customfile.getProperty("AllowDirectTeleportToBossRoom", false);
        ENABLE_AUTO_PVP_ZONE = Boolean.parseBoolean(customfile.getProperty("EnableAutoPvpZone", "false"));
        UNSTUCK_TIME = customfile.getProperty("UnstuckTime", 30);
        REWARD_BOX_ID = customfile.getProperty("RewardBoxId", 9554);
        REWARD_BOX_REWARDS = parseReward(customfile, "RewardBoxReward");
        REWARD_BOX_ID_1 = customfile.getProperty("RewardBoxId_1", 9555);
        REWARD_BOX_REWARDS_1 = parseReward(customfile, "RewardBoxReward_1");
        REWARD_BOX_ID_2 = customfile.getProperty("RewardBoxId_2", 9556);
        REWARD_BOX_REWARDS_2 = parseReward(customfile, "RewardBoxReward_2");
        REWARD_BOX_ID_3 = customfile.getProperty("RewardBoxId_3", 9557);
        REWARD_BOX_REWARDS_3 = parseReward(customfile, "RewardBoxReward_3");
        ALLOW_DAILY_REWARD = customfile.getProperty("AllowDailyReward", true);
        DAILY_LOG_REWARDS = parseReward(customfile, "DailyReward");
        DAILY_REWARD_RESET_TIME = customfile.getProperty("DailyRewardResetTime", "20:00").split(",");
        ALLOW_DRESS_ME_SYSTEM = customfile.getProperty("AllowDressMeSystem", true);
        DRESS_ME_COMMAND = customfile.getProperty("DressMeCommand", "dressme");
        ALLOW_DRESS_ME_FOR_PREMIUM = customfile.getProperty("AllowDressMeForPremiumOnly", false);
        ALLOW_DRESS_ME_IN_OLY = customfile.getProperty("AllowDressMeInOly", false);
        WAR_LEGEND_AURA = Boolean.parseBoolean(customfile.getProperty("WarLegendAura", "False"));
        KILLS_TO_GET_WAR_LEGEND_AURA = Integer.parseInt(customfile.getProperty("KillsToGetWarLegendAura", "30"));
        NEWBIE_LVL = Integer.parseInt(customfile.getProperty("NewbiesLevel", "80"));
        NEWBIE_FIGHTER_SET = customfile.getProperty("FighterSet", "1204,1086");
        NEWBIE_MAGE_SET = customfile.getProperty("MageSet", "1204,1086");
        String[] NewFighterList = NEWBIE_FIGHTER_SET.split(",");
        NEWBIE_FIGHTER_BUFFS = new int[NewFighterList.length];
        for (int i = 0; i < NewFighterList.length; i++)
            NEWBIE_FIGHTER_BUFFS[i] = Integer.parseInt(NewFighterList[i]);
        String[] NewMageList = NEWBIE_MAGE_SET.split(",");
        NEWBIE_MAGE_BUFFS = new int[NewMageList.length];
        for (int j = 0; j < NewMageList.length; j++)
            NEWBIE_MAGE_BUFFS[j] = Integer.parseInt(NewMageList[j]);
        SETX = customfile.getProperty("LocX", 83278);
        SETY = customfile.getProperty("LocY", 148273);
        SETZ = customfile.getProperty("LocZ", -3407);
        ENABLE_AGATHION_SYSTEM = customfile.getProperty("EnableAgathionSystem", false);
        AGATHION_RESPAWN_DELAY = customfile.getProperty("AgathionRespawnDelay", 60);
        AGATHION_USE_DELAY = Long.parseLong(customfile.getProperty("AgathionUseDelay", "5000"));
        AGATHION_BUFF = customfile.getProperty("AgathionBuffsPlalyer", false);
        BALANCER_ALLOW = customfile.getProperty("BalancerAllow", true);
        CLAN_ITEM_ID_LEVEL_UP = customfile.getProperty("ClanLevelUPItemID", 3470);
        CLAN_COUNT_LEVEL_UP = customfile.getProperty("ClanLevelUPAmount", 100);
        CLAN_ITEM_ID_REPUTATION = customfile.getProperty("ClanReputationItemID", 3470);
        CLAN_COUNT_REPUTATION = customfile.getProperty("ClanReputationAmount", 100);
        AUTOFARM_WITHOUT_RESTRICTION = customfile.getProperty("AutoFarmWithoutRestriction", false);
        AUTOFARM_L2AutoFarmZone = customfile.getProperty("AutoFarmL2AutoFarmZone", false);
        AUTOFARM_L2PartyFarmZone = customfile.getProperty("AutoFarmL2PartyFarmZone", false);
        AUTOFARM_PeaceZone = customfile.getProperty("AutoFarmPeaceZone", false);
        String tutorial_quest_farm_loc = customfile.getProperty("TutorialQuestFarmLocation", "-121896,-22888,5920");
        String[] tutorial_quest_farm_loc_split = tutorial_quest_farm_loc.split(",");
        TUTORIAL_QUEST_FARM_LOCATION = new Location(Integer.parseInt(tutorial_quest_farm_loc_split[0]), Integer.parseInt(tutorial_quest_farm_loc_split[1]), Integer.parseInt(tutorial_quest_farm_loc_split[2]));
        String tutorial_quest_town_loc = customfile.getProperty("TutorialQuestTownLocation", "83278,148273,-3407");
        String[] tutorial_quest_town_loc_split = tutorial_quest_town_loc.split(",");
        TUTORIAL_QUEST_TOWN_LOCATION = new Location(Integer.parseInt(tutorial_quest_town_loc_split[0]), Integer.parseInt(tutorial_quest_town_loc_split[1]), Integer.parseInt(tutorial_quest_town_loc_split[2]));
        WELLCOME_MESSAGE_ACTIVE = customfile.getProperty("ActiveWellcomeMessageOnLogin", false);
        WELLCOME_SERVER_NAME = customfile.getProperty("WellcomeMessageServerName", "PvP_2025 Server");
        WELLCOME_SERVER_SECOND_MESSAGE = customfile.getProperty("WellcomeMessageSecondaryText", " Vote for us every 12hrs. Thanks for playing.");
        LEVEL_REWARDS_ENABLE = customfile.getProperty("EnableRewardPerLevel", false);
        if (LEVEL_REWARDS_ENABLE) {
            LEVEL_REWARDS = new HashMap<>();
            int size = customfile.getProperty("RewardsPerLevel", "81,9554-1;").split(";").length;
            for (int id = 0; id < size; id++) {
                String rewards = customfile.getProperty("RewardsPerLevel", "81,9554-1;").split(";")[id];
                String[] inforReward = rewards.split(",");
                if (inforReward.length != 2) {
                    LOGGER.warn("[RewardsPerLevel]: invalid config property -> RewardsPerLevel.");
                } else {
                    try {
                        String[] rewardSplit = inforReward[1].split("-");
                        if (rewardSplit.length != 2)
                            LOGGER.warn("[RewardsPerLevel]: Invalid reward format 57-100 (item-count).");
                        LEVEL_REWARDS.put(Integer.parseInt(inforReward[0]), new IntIntHolder(Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])));
                    } catch (NumberFormatException nfe) {
                        if (DEVELOPER) {
                            nfe.printStackTrace();
                            LOGGER.warn("[RewardsPerLevel]: invalid config property -> RewardsPerLevel.");
                        }
                    }
                }
            }
        }
    }

    private static void loadMultiFunctionZone() {
        ExProperties multifunctionzone = initProperties("./config/multiFunctionzone.properties");
        REVIVE = false;
        PVP_ENABLED = Boolean.parseBoolean(multifunctionzone.getProperty("EnablePvP", "false"));
        SPAWN_LOC = parseItemsList_array2d(multifunctionzone.getProperty("SpawnLoc", "150111,144740,-12248"));
        REVIVE_DELAY = Integer.parseInt(multifunctionzone.getProperty("ReviveDelay", "10"));
        if (REVIVE_DELAY != 0)
            REVIVE = true;
        GIVE_NOBLES = Boolean.parseBoolean(multifunctionzone.getProperty("GiveNoblesse", "False"));
        String[] propertySplit = multifunctionzone.getProperty("Items", "").split(",");
        Collections.addAll(ITEMS, propertySplit);
        propertySplit = multifunctionzone.getProperty("Grades", "").split(",");
        for (String i : propertySplit) {
            if (i.equals("D") || i.equals("C") || i.equals("B") || i.equals("A") || i.equals("S"))
                GRADES.add(i);
        }
        propertySplit = multifunctionzone.getProperty("Classes", "").split(",");
        Collections.addAll(CLASSES, propertySplit);
        RADIUS = Integer.parseInt(multifunctionzone.getProperty("RespawnRadius", "500"));
        ENCHANT = Integer.parseInt(multifunctionzone.getProperty("Enchant", "0"));
        REMOVE_BUFFS = Boolean.parseBoolean(multifunctionzone.getProperty("RemoveBuffs", "False"));
        REMOVE_PETS = Boolean.parseBoolean(multifunctionzone.getProperty("RemovePets", "False"));
        RESTART_ZONE = Boolean.parseBoolean(multifunctionzone.getProperty("NoRestartZone", "False"));
        STORE_ZONE = Boolean.parseBoolean(multifunctionzone.getProperty("NoStoreZone", "False"));
        LOGOUT_ZONE = Boolean.parseBoolean(multifunctionzone.getProperty("NoLogoutZone", "False"));
        REVIVE_NOBLES = Boolean.parseBoolean(multifunctionzone.getProperty("ReviveNoblesse", "False"));
        REVIVE_HEAL = Boolean.parseBoolean(multifunctionzone.getProperty("ReviveHeal", "False"));
        propertySplit = multifunctionzone.getProperty("Rewards", "57,100000").split(";");
        for (String s : propertySplit) {
            String[] ss = s.split(",");
            REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
    }

    private static void loadEvents() {
        ExProperties events = initProperties("./config/events.properties");
        ALT_OLY_START_TIME = events.getProperty("AltOlyStartTime", 18);
        ALT_OLY_MIN = events.getProperty("AltOlyMin", 0);
        ALT_OLY_CPERIOD = events.getProperty("AltOlyCPeriod", 21600000);
        ALT_OLY_BATTLE = events.getProperty("AltOlyBattle", 180000);
        ALT_OLY_WPERIOD = events.getProperty("AltOlyWPeriod", 604800000);
        ALT_OLY_VPERIOD = events.getProperty("AltOlyVPeriod", 86400000);
        ALT_OLY_WAIT_TIME = events.getProperty("AltOlyWaitTime", 30);
        ALT_OLY_WAIT_BATTLE = events.getProperty("AltOlyWaitBattle", 60);
        ALT_OLY_WAIT_END = events.getProperty("AltOlyWaitEnd", 40);
        ALT_OLY_START_POINTS = events.getProperty("AltOlyStartPoints", 18);
        ALT_OLY_WEEKLY_POINTS = events.getProperty("AltOlyWeeklyPoints", 3);
        ALT_OLY_MIN_MATCHES = events.getProperty("AltOlyMinMatchesToBeClassed", 5);
        ALT_OLY_CLASSED = events.getProperty("AltOlyClassedParticipants", 5);
        ALT_OLY_NONCLASSED = events.getProperty("AltOlyNonClassedParticipants", 9);
        ALT_OLY_CLASSED_REWARD = events.parseIntIntList("AltOlyClassedReward", "6651-50");
        ALT_OLY_NONCLASSED_REWARD = events.parseIntIntList("AltOlyNonClassedReward", "6651-30");
        ALT_OLY_GP_PER_POINT = events.getProperty("AltOlyGPPerPoint", 1000);
        ALT_OLY_HERO_POINTS = events.getProperty("AltOlyHeroPoints", 300);
        ALT_OLY_RANK1_POINTS = events.getProperty("AltOlyRank1Points", 100);
        ALT_OLY_RANK2_POINTS = events.getProperty("AltOlyRank2Points", 75);
        ALT_OLY_RANK3_POINTS = events.getProperty("AltOlyRank3Points", 55);
        ALT_OLY_RANK4_POINTS = events.getProperty("AltOlyRank4Points", 40);
        ALT_OLY_RANK5_POINTS = events.getProperty("AltOlyRank5Points", 30);
        ALT_OLY_MAX_POINTS = events.getProperty("AltOlyMaxPoints", 10);
        ALT_OLY_DIVIDER_CLASSED = events.getProperty("AltOlyDividerClassed", 3);
        ALT_OLY_DIVIDER_NON_CLASSED = events.getProperty("AltOlyDividerNonClassed", 5);
        ALT_OLY_ANNOUNCE_GAMES = events.getProperty("AltOlyAnnounceGames", true);
        ALT_GAME_CASTLE_DAWN = events.getProperty("AltCastleForDawn", true);
        ALT_GAME_CASTLE_DUSK = events.getProperty("AltCastleForDusk", true);
        ALT_FESTIVAL_MIN_PLAYER = MathUtil.limit(events.getProperty("AltFestivalMinPlayer", 5), 2, 9);
        ALT_MAXIMUM_PLAYER_CONTRIB = events.getProperty("AltMaxPlayerContrib", 1000000);
        ALT_FESTIVAL_MANAGER_START = events.getProperty("AltFestivalManagerStart", 120000);
        ALT_FESTIVAL_LENGTH = events.getProperty("AltFestivalLength", 1080000);
        ALT_FESTIVAL_CYCLE_LENGTH = events.getProperty("AltFestivalCycleLength", 2280000);
        ALT_FESTIVAL_FIRST_SPAWN = events.getProperty("AltFestivalFirstSpawn", 120000);
        ALT_FESTIVAL_FIRST_SWARM = events.getProperty("AltFestivalFirstSwarm", 300000);
        ALT_FESTIVAL_SECOND_SPAWN = events.getProperty("AltFestivalSecondSpawn", 540000);
        ALT_FESTIVAL_SECOND_SWARM = events.getProperty("AltFestivalSecondSwarm", 720000);
        ALT_FESTIVAL_CHEST_SPAWN = events.getProperty("AltFestivalChestSpawn", 900000);
        FS_TIME_ENTRY = events.getProperty("EntryTime", 55);
        FS_TIME_END = events.getProperty("EndTime", 50);
        FS_PARTY_MEMBER_COUNT = MathUtil.limit(events.getProperty("NeededPartyMembers", 4), 2, 9);
        RIFT_MIN_PARTY_SIZE = events.getProperty("RiftMinPartySize", 2);
        RIFT_MAX_JUMPS = events.getProperty("MaxRiftJumps", 4);
        RIFT_SPAWN_DELAY = events.getProperty("RiftSpawnDelay", 10000);
        RIFT_AUTO_JUMPS_TIME_MIN = events.getProperty("AutoJumpsDelayMin", 480);
        RIFT_AUTO_JUMPS_TIME_MAX = events.getProperty("AutoJumpsDelayMax", 600);
        RIFT_ENTER_COST_RECRUIT = events.getProperty("RecruitCost", 18);
        RIFT_ENTER_COST_SOLDIER = events.getProperty("SoldierCost", 21);
        RIFT_ENTER_COST_OFFICER = events.getProperty("OfficerCost", 24);
        RIFT_ENTER_COST_CAPTAIN = events.getProperty("CaptainCost", 27);
        RIFT_ENTER_COST_COMMANDER = events.getProperty("CommanderCost", 30);
        RIFT_ENTER_COST_HERO = events.getProperty("HeroCost", 33);
        RIFT_BOSS_ROOM_TIME_MUTIPLY = events.getProperty("BossRoomTimeMultiply", 1.0D);
        ALLOW_WEDDING = events.getProperty("AllowWedding", false);
        WEDDING_PRICE = events.getProperty("WeddingPrice", 1000000);
        WEDDING_SAMESEX = events.getProperty("WeddingAllowSameSex", false);
        WEDDING_FORMALWEAR = events.getProperty("WeddingFormalWear", true);
        ALT_LOTTERY_PRIZE = events.getProperty("AltLotteryPrize", 50000);
        ALT_LOTTERY_TICKET_PRICE = events.getProperty("AltLotteryTicketPrice", 2000);
        ALT_LOTTERY_5_NUMBER_RATE = events.getProperty("AltLottery5NumberRate", 0.6D);
        ALT_LOTTERY_4_NUMBER_RATE = events.getProperty("AltLottery4NumberRate", 0.2D);
        ALT_LOTTERY_3_NUMBER_RATE = events.getProperty("AltLottery3NumberRate", 0.2D);
        ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = events.getProperty("AltLottery2and1NumberPrize", 200);
        ALT_FISH_CHAMPIONSHIP_ENABLED = events.getProperty("AltFishChampionshipEnabled", true);
        ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = events.getProperty("AltFishChampionshipRewardItemId", 57);
        ALT_FISH_CHAMPIONSHIP_REWARD_1 = events.getProperty("AltFishChampionshipReward1", 800000);
        ALT_FISH_CHAMPIONSHIP_REWARD_2 = events.getProperty("AltFishChampionshipReward2", 500000);
        ALT_FISH_CHAMPIONSHIP_REWARD_3 = events.getProperty("AltFishChampionshipReward3", 300000);
        ALT_FISH_CHAMPIONSHIP_REWARD_4 = events.getProperty("AltFishChampionshipReward4", 200000);
        ALT_FISH_CHAMPIONSHIP_REWARD_5 = events.getProperty("AltFishChampionshipReward5", 100000);
    }

    private static void loadGeoengine() {
        ExProperties geoengine = initProperties("./config/geoengine.properties");
        GEODATA_PATH = geoengine.getProperty("GeoDataPath", "./data/geodata/");
        PART_OF_CHARACTER_HEIGHT = geoengine.getProperty("PartOfCharacterHeight", 75);
        MAX_OBSTACLE_HEIGHT = geoengine.getProperty("MaxObstacleHeight", 32);
        PATHFIND_BUFFERS = geoengine.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
        BASE_WEIGHT = geoengine.getProperty("BaseWeight", 10);
        DIAGONAL_WEIGHT = geoengine.getProperty("DiagonalWeight", 14);
        OBSTACLE_MULTIPLIER = geoengine.getProperty("ObstacleMultiplier", 10);
        HEURISTIC_WEIGHT = geoengine.getProperty("HeuristicWeight", 20);
        MAX_ITERATIONS = geoengine.getProperty("MaxIterations", 3500);
        DEBUG_PATH = geoengine.getProperty("DebugPath", false);
        DEBUG_GEO_NODE = geoengine.getProperty("DebugGeoNode", false);
    }

    private static void loadHexID() {
        ExProperties hexid = initProperties("./config/hexid.txt");
        SERVER_ID = Integer.parseInt(hexid.getProperty("ServerID"));
        HEX_ID = (new BigInteger(hexid.getProperty("HexID"), 16)).toByteArray();
    }

    public static void saveHexid(int serverId, String hexId) {
        saveHexid(serverId, hexId, "./config/hexid.txt");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveHexid(int serverId, String hexId, String filename) {
        try {
            File file = new File(filename);
            file.createNewFile();
            Properties hexSetting = new Properties();
            hexSetting.setProperty("ServerID", String.valueOf(serverId));
            hexSetting.setProperty("HexID", hexId);
            OutputStream out = new FileOutputStream(file);
            try {
                hexSetting.store(out, "the hexID to auth into login");
                out.close();
            } catch (Throwable throwable) {
                try {
                    out.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save hex ID to '{}' file.", e, filename);
        }
    }

    private static void loadNpcs() {
        ExProperties npcs = initProperties("./config/npcs.properties");
        CHAMPION_FREQUENCY = npcs.getProperty("ChampionFrequency", 0);
        CHAMP_MIN_LVL = npcs.getProperty("ChampionMinLevel", 20);
        CHAMP_MAX_LVL = npcs.getProperty("ChampionMaxLevel", 70);
        CHAMPION_HP = npcs.getProperty("ChampionHp", 8);
        CHAMPION_HP_REGEN = npcs.getProperty("ChampionHpRegen", 1.0D);
        CHAMPION_REWARDS = npcs.getProperty("ChampionRewards", 8);
        CHAMPION_ADENAS_REWARDS = npcs.getProperty("ChampionAdenasRewards", 1);
        CHAMPION_ATK = npcs.getProperty("ChampionAtk", 1.0D);
        CHAMPION_SPD_ATK = npcs.getProperty("ChampionSpdAtk", 1.0D);
        CHAMPION_REWARD = npcs.getProperty("ChampionRewardItem", 0);
        CHAMPION_REWARD_ID = npcs.getProperty("ChampionRewardItemID", 6393);
        CHAMPION_REWARD_QTY = npcs.getProperty("ChampionRewardItemQty", 1);
        BUFFER_MAX_SCHEMES = npcs.getProperty("BufferMaxSchemesPerChar", 4);
        BUFFER_STATIC_BUFF_COST = npcs.getProperty("BufferStaticCostPerBuff", -1);
        ALLOW_CLASS_MASTERS = npcs.getProperty("AllowClassMasters", false);
        ALTERNATE_CLASS_MASTER = npcs.getProperty("AlternateClassMaster", false);
        ALLOW_ENTIRE_TREE = npcs.getProperty("AllowEntireTree", false);
        if (ALLOW_CLASS_MASTERS)
            CLASS_MASTER_SETTINGS = new ClassMasterSettings(npcs.getProperty("ConfigClassMaster"));
        REWARD_NOBLE_IN_3RCHANGE_JOB = Boolean.parseBoolean(npcs.getProperty("EnableRewardNobleOn3rChangeJob", "True"));
        ANNOUNCE_MAMMON_SPAWN = npcs.getProperty("AnnounceMammonSpawn", true);
        ALT_MOB_AGRO_IN_PEACEZONE = npcs.getProperty("AltMobAgroInPeaceZone", true);
        SHOW_NPC_LVL = npcs.getProperty("ShowNpcLevel", false);
        SHOW_NPC_CREST = npcs.getProperty("ShowNpcCrest", false);
        SHOW_SUMMON_CREST = npcs.getProperty("ShowSummonCrest", false);
        WYVERN_ALLOW_UPGRADER = npcs.getProperty("AllowWyvernUpgrader", true);
        WYVERN_REQUIRED_LEVEL = npcs.getProperty("RequiredStriderLevel", 55);
        WYVERN_REQUIRED_CRYSTALS = npcs.getProperty("RequiredCrystalsNumber", 10);
        RAID_HP_REGEN_MULTIPLIER = npcs.getProperty("RaidHpRegenMultiplier", 1.0D);
        RAID_MP_REGEN_MULTIPLIER = npcs.getProperty("RaidMpRegenMultiplier", 1.0D);
        RAID_DEFENCE_MULTIPLIER = npcs.getProperty("RaidDefenceMultiplier", 1.0D);
        RAID_MINION_RESPAWN_TIMER = npcs.getProperty("RaidMinionRespawnTime", 300000);
        RAID_DISABLE_CURSE = npcs.getProperty("DisableRaidCurse", false);
        SPAWN_INTERVAL_AQ = npcs.getProperty("AntQueenSpawnInterval", 36);
        RANDOM_SPAWN_TIME_AQ = npcs.getProperty("AntQueenRandomSpawn", 17);
        SPAWN_INTERVAL_ANTHARAS = npcs.getProperty("AntharasSpawnInterval", 264);
        RANDOM_SPAWN_TIME_ANTHARAS = npcs.getProperty("AntharasRandomSpawn", 72);
        WAIT_TIME_ANTHARAS = npcs.getProperty("AntharasWaitTime", 30) * 60000;
        SPAWN_INTERVAL_BAIUM = npcs.getProperty("BaiumSpawnInterval", 168);
        RANDOM_SPAWN_TIME_BAIUM = npcs.getProperty("BaiumRandomSpawn", 48);
        SPAWN_INTERVAL_CORE = npcs.getProperty("CoreSpawnInterval", 60);
        RANDOM_SPAWN_TIME_CORE = npcs.getProperty("CoreRandomSpawn", 23);
        SPAWN_INTERVAL_FRINTEZZA = npcs.getProperty("FrintezzaSpawnInterval", 48);
        RANDOM_SPAWN_TIME_FRINTEZZA = npcs.getProperty("FrintezzaRandomSpawn", 8);
        WAIT_TIME_FRINTEZZA = npcs.getProperty("FrintezzaWaitTime", 1) * 60000;
        SPAWN_INTERVAL_ORFEN = npcs.getProperty("OrfenSpawnInterval", 48);
        RANDOM_SPAWN_TIME_ORFEN = npcs.getProperty("OrfenRandomSpawn", 20);
        SPAWN_INTERVAL_SAILREN = npcs.getProperty("SailrenSpawnInterval", 36);
        RANDOM_SPAWN_TIME_SAILREN = npcs.getProperty("SailrenRandomSpawn", 24);
        WAIT_TIME_SAILREN = npcs.getProperty("SailrenWaitTime", 5) * 60000;
        SPAWN_INTERVAL_VALAKAS = npcs.getProperty("ValakasSpawnInterval", 264);
        RANDOM_SPAWN_TIME_VALAKAS = npcs.getProperty("ValakasRandomSpawn", 72);
        WAIT_TIME_VALAKAS = npcs.getProperty("ValakasWaitTime", 30) * 60000;
        SPAWN_INTERVAL_ZAKEN = npcs.getProperty("ZakenSpawnInterval", 60);
        RANDOM_SPAWN_TIME_ZAKEN = npcs.getProperty("ZakenRandomSpawn", 20);
        FWA_FIXTIMEPATTERNOFANTHARAS = npcs.getProperty("AntharasRespawnTimePattern", "");
        FWA_FIXTIMEPATTERNOFBAIUM = npcs.getProperty("BaiumRespawnTimePattern", "");
        FWA_FIXTIMEPATTERNOFCORE = npcs.getProperty("CoreRespawnTimePattern", "");
        FWA_FIXTIMEPATTERNOFORFEN = npcs.getProperty("OrfenRespawnTimePattern", "");
        FWA_FIXTIMEPATTERNOFVALAKAS = npcs.getProperty("ValakasRespawnTimePattern", "");
        FWA_FIXTIMEPATTERNOFZAKEN = npcs.getProperty("ZakenRespawnTimePattern", "");
        FWA_FIXTIMEPATTERNOFQA = npcs.getProperty("QueenAntRespawnTimePattern", "");
        GUARD_ATTACK_AGGRO_MOB = npcs.getProperty("GuardAttackAggroMob", false);
        MAX_DRIFT_RANGE = npcs.getProperty("MaxDriftRange", 300);
        MIN_NPC_ANIMATION = npcs.getProperty("MinNPCAnimation", 20);
        MAX_NPC_ANIMATION = npcs.getProperty("MaxNPCAnimation", 40);
        MIN_MONSTER_ANIMATION = npcs.getProperty("MinMonsterAnimation", 10);
        MAX_MONSTER_ANIMATION = npcs.getProperty("MaxMonsterAnimation", 40);
    }

    private static void loadPlayers() {
        ExProperties players = initProperties("./config/players.properties");
        EFFECT_CANCELING = players.getProperty("CancelLesserEffect", true);
        HP_REGEN_MULTIPLIER = players.getProperty("HpRegenMultiplier", 1.0D);
        MP_REGEN_MULTIPLIER = players.getProperty("MpRegenMultiplier", 1.0D);
        CP_REGEN_MULTIPLIER = players.getProperty("CpRegenMultiplier", 1.0D);
        PLAYER_SPAWN_PROTECTION = players.getProperty("PlayerSpawnProtection", 0);
        PLAYER_FAKEDEATH_UP_PROTECTION = players.getProperty("PlayerFakeDeathUpProtection", 0);
        RESPAWN_RESTORE_HP = players.getProperty("RespawnRestoreHP", 0.7D);
        MAX_PVTSTORE_SLOTS_DWARF = players.getProperty("MaxPvtStoreSlotsDwarf", 5);
        MAX_PVTSTORE_SLOTS_OTHER = players.getProperty("MaxPvtStoreSlotsOther", 4);
        DEEPBLUE_DROP_RULES = players.getProperty("UseDeepBlueDropRules", true);
        ALT_GAME_DELEVEL = players.getProperty("Delevel", true);
        DEATH_PENALTY_CHANCE = players.getProperty("DeathPenaltyChance", 20);
        INVENTORY_MAXIMUM_NO_DWARF = players.getProperty("MaximumSlotsForNoDwarf", 80);
        INVENTORY_MAXIMUM_DWARF = players.getProperty("MaximumSlotsForDwarf", 100);
        INVENTORY_MAXIMUM_QUEST_ITEMS = players.getProperty("MaximumSlotsForQuestItems", 100);
        INVENTORY_MAXIMUM_PET = players.getProperty("MaximumSlotsForPet", 12);
        MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, INVENTORY_MAXIMUM_DWARF);
        ALT_WEIGHT_LIMIT = players.getProperty("AltWeightLimit", 1);
        WAREHOUSE_SLOTS_NO_DWARF = players.getProperty("MaximumWarehouseSlotsForNoDwarf", 100);
        WAREHOUSE_SLOTS_DWARF = players.getProperty("MaximumWarehouseSlotsForDwarf", 120);
        WAREHOUSE_SLOTS_CLAN = players.getProperty("MaximumWarehouseSlotsForClan", 150);
        FREIGHT_SLOTS = players.getProperty("MaximumFreightSlots", 20);
        ALT_GAME_FREIGHTS = players.getProperty("AltGameFreights", false);
        ALT_GAME_FREIGHT_PRICE = players.getProperty("AltGameFreightPrice", 1000);
        AUGMENTATION_NG_SKILL_CHANCE = players.getProperty("AugmentationNGSkillChance", 15);
        AUGMENTATION_NG_GLOW_CHANCE = players.getProperty("AugmentationNGGlowChance", 0);
        AUGMENTATION_MID_SKILL_CHANCE = players.getProperty("AugmentationMidSkillChance", 30);
        AUGMENTATION_MID_GLOW_CHANCE = players.getProperty("AugmentationMidGlowChance", 40);
        AUGMENTATION_HIGH_SKILL_CHANCE = players.getProperty("AugmentationHighSkillChance", 45);
        AUGMENTATION_HIGH_GLOW_CHANCE = players.getProperty("AugmentationHighGlowChance", 70);
        AUGMENTATION_TOP_SKILL_CHANCE = players.getProperty("AugmentationTopSkillChance", 60);
        AUGMENTATION_TOP_GLOW_CHANCE = players.getProperty("AugmentationTopGlowChance", 100);
        AUGMENTATION_BASESTAT_CHANCE = players.getProperty("AugmentationBaseStatChance", 1);
        KARMA_PLAYER_CAN_BE_KILLED_IN_PZ = players.getProperty("KarmaPlayerCanBeKilledInPeaceZone", false);
        KARMA_PLAYER_CAN_SHOP = players.getProperty("KarmaPlayerCanShop", false);
        KARMA_PLAYER_CAN_USE_GK = players.getProperty("KarmaPlayerCanUseGK", false);
        KARMA_PLAYER_CAN_TELEPORT = players.getProperty("KarmaPlayerCanTeleport", true);
        KARMA_PLAYER_CAN_TRADE = players.getProperty("KarmaPlayerCanTrade", true);
        KARMA_PLAYER_CAN_USE_WH = players.getProperty("KarmaPlayerCanUseWareHouse", true);
        KARMA_DROP_GM = players.getProperty("CanGMDropEquipment", false);
        KARMA_AWARD_PK_KILL = players.getProperty("AwardPKKillPVPPoint", true);
        KARMA_PK_LIMIT = players.getProperty("MinimumPKRequiredToDrop", 5);
        KARMA_NONDROPPABLE_PET_ITEMS = players.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
        KARMA_NONDROPPABLE_ITEMS = players.getProperty("ListOfNonDroppableItemsForPK", "1147,425,1146,461,10,2368,7,6,2370,2369");
        String[] array = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
        KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];
        int i;
        for (i = 0; i < array.length; i++)
            KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);
        array = KARMA_NONDROPPABLE_ITEMS.split(",");
        KARMA_LIST_NONDROPPABLE_ITEMS = new int[array.length];
        for (i = 0; i < array.length; i++)
            KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);
        Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
        Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
        PVP_NORMAL_TIME = players.getProperty("PvPVsNormalTime", 15000);
        PVP_PVP_TIME = players.getProperty("PvPVsPvPTime", 30000);
        PARTY_XP_CUTOFF_METHOD = players.getProperty("PartyXpCutoffMethod", "level");
        PARTY_XP_CUTOFF_PERCENT = players.getProperty("PartyXpCutoffPercent", 3.0D);
        PARTY_XP_CUTOFF_LEVEL = players.getProperty("PartyXpCutoffLevel", 20);
        PARTY_RANGE = players.getProperty("PartyRange", 1500);
        DEFAULT_ACCESS_LEVEL = players.getProperty("DefaultAccessLevel", 0);
        GM_HERO_AURA = players.getProperty("GMHeroAura", false);
        GM_STARTUP_INVULNERABLE = players.getProperty("GMStartupInvulnerable", true);
        GM_STARTUP_INVISIBLE = players.getProperty("GMStartupInvisible", true);
        GM_STARTUP_SILENCE = players.getProperty("GMStartupSilence", true);
        GM_STARTUP_AUTO_LIST = players.getProperty("GMStartupAutoList", true);
        GM_SUPER_HASTE = players.getProperty("GMStartupSuperHaste", true);
        PETITIONING_ALLOWED = players.getProperty("PetitioningAllowed", true);
        MAX_PETITIONS_PER_PLAYER = players.getProperty("MaxPetitionsPerPlayer", 5);
        MAX_PETITIONS_PENDING = players.getProperty("MaxPetitionsPending", 25);
        IS_CRAFTING_ENABLED = players.getProperty("CraftingEnabled", true);
        DWARF_RECIPE_LIMIT = players.getProperty("DwarfRecipeLimit", 50);
        COMMON_RECIPE_LIMIT = players.getProperty("CommonRecipeLimit", 50);
        ALT_BLACKSMITH_USE_RECIPES = players.getProperty("AltBlacksmithUseRecipes", true);
        AUTO_LEARN_SKILLS = players.getProperty("AutoLearnSkills", false);
        MAGIC_FAILURES = players.getProperty("MagicFailures", true);
        PERFECT_SHIELD_BLOCK_RATE = players.getProperty("PerfectShieldBlockRate", 5);
        LIFE_CRYSTAL_NEEDED = players.getProperty("LifeCrystalNeeded", true);
        SP_BOOK_NEEDED = players.getProperty("SpBookNeeded", true);
        ES_SP_BOOK_NEEDED = players.getProperty("EnchantSkillSpBookNeeded", true);
        DIVINE_SP_BOOK_NEEDED = players.getProperty("DivineInspirationSpBookNeeded", true);
        SUBCLASS_WITHOUT_QUESTS = players.getProperty("SubClassWithoutQuests", false);
        ALT_GAME_SUBCLASS_EVERYWHERE = players.getProperty("AltSubclassEverywhere", false);
        ALLOWED_SUBCLASS = players.getProperty("AllowedSubclass", 3);
        SUBCLASS_MAX_LEVEL = players.getProperty("SubclassMaxLevel", 80);
        MAX_BUFFS_AMOUNT = players.getProperty("MaxBuffsAmount", 20);
        STORE_SKILL_COOLTIME = players.getProperty("StoreSkillCooltime", true);
    }

    private static void loadSieges() {
        ExProperties sieges = initProperties("./config/siege.properties");
        SIEGE_LENGTH = sieges.getProperty("SiegeLength", 120);
        MINIMUM_CLAN_LEVEL = sieges.getProperty("SiegeClanMinLevel", 4);
        MAX_ATTACKERS_NUMBER = sieges.getProperty("AttackerMaxClans", 10);
        MAX_DEFENDERS_NUMBER = sieges.getProperty("DefenderMaxClans", 10);
        ATTACKERS_RESPAWN_DELAY = sieges.getProperty("AttackerRespawn", 10000);
        DAY_TO_SIEGE = Integer.parseInt(sieges.getProperty("DayToSiege", "2"));
        SIEGE_HOUR_GLUDIO = Integer.parseInt(sieges.getProperty("HourToSiege_Gludio", "16"));
        SIEGE_HOUR_DION = Integer.parseInt(sieges.getProperty("HourToSiege_Dion", "16"));
        SIEGE_HOUR_GIRAN = Integer.parseInt(sieges.getProperty("HourToSiege_Giran", "16"));
        SIEGE_HOUR_OREN = Integer.parseInt(sieges.getProperty("HourToSiege_Oren", "16"));
        SIEGE_HOUR_ADEN = Integer.parseInt(sieges.getProperty("HourToSiege_Aden", "16"));
        SIEGE_HOUR_INNADRIL = Integer.parseInt(sieges.getProperty("HourToSiege_Innadril", "16"));
        SIEGE_HOUR_GODDARD = Integer.parseInt(sieges.getProperty("HourToSiege_Goddard", "16"));
        SIEGE_HOUR_RUNE = Integer.parseInt(sieges.getProperty("HourToSiege_Rune", "16"));
        SIEGE_HOUR_SCHUT = Integer.parseInt(sieges.getProperty("HourToSiege_Schuttgart", "16"));
        SIEGE_DAY_GLUDIO = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Gludio", "1"));
        SIEGE_DAY_DION = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Dion", "7"));
        SIEGE_DAY_GIRAN = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Giran", "1"));
        SIEGE_DAY_OREN = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Oren", "7"));
        SIEGE_DAY_ADEN = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Aden", "1"));
        SIEGE_DAY_INNADRIL = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Innadril", "7"));
        SIEGE_DAY_GODDARD = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Goddard", "1"));
        SIEGE_DAY_RUNE = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Rune", "7"));
        SIEGE_DAY_SCHUT = Integer.parseInt(sieges.getProperty("DayOfTheWeek_Schuttgart", "1"));
        SIEGE_GLUDIO = sieges.getProperty("Siege_Gludio", false);
        SIEGE_DION = sieges.getProperty("Siege_Dion", false);
        SIEGE_GIRAN = sieges.getProperty("Siege_Giran", false);
        SIEGE_OREN = sieges.getProperty("Siege_Oren", false);
        SIEGE_ADEN = sieges.getProperty("Siege_Aden", false);
        SIEGE_INNADRIL = sieges.getProperty("Siege_Innadril", false);
        SIEGE_GODDARD = sieges.getProperty("Siege_Goddard", false);
        SIEGE_RUNE = sieges.getProperty("Siege_Rune", false);
        SIEGE_SCHUT = sieges.getProperty("Siege_Schuttgart", false);
    }

    private static void loadServer() {
        ExProperties server = initProperties("./config/server.properties");
        GAMESERVER_HOSTNAME = server.getProperty("GameserverHostname");
        PORT_GAME = server.getProperty("GameserverPort", 7777);
        HOSTNAME = server.getProperty("Hostname", "*");
        GAME_SERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014);
        GAME_SERVER_LOGIN_HOST = server.getProperty("LoginHost", "127.0.0.1");
        REQUEST_ID = server.getProperty("RequestServerID", 0);
        ACCEPT_ALTERNATE_ID = server.getProperty("AcceptAlternateID", true);
        DATABASE_URL = server.getProperty("URL", "jdbc:mariadb://localhost/pvp2025?serverTimezone=UTC");
        DATABASE_LOGIN = server.getProperty("Login", "root");
        DATABASE_PASSWORD = server.getProperty("Password", "");
        DATABASE_MAX_CONNECTIONS = server.getProperty("MaximumDbConnections", 10);
        SERVER_LIST_BRACKET = server.getProperty("ServerListBrackets", false);
        SERVER_LIST_CLOCK = server.getProperty("ServerListClock", false);
        SERVER_GMONLY = server.getProperty("ServerGMOnly", false);
        SERVER_LIST_AGE = server.getProperty("ServerListAgeLimit", 0);
        SERVER_LIST_TESTSERVER = server.getProperty("TestServer", false);
        SERVER_LIST_PVPSERVER = server.getProperty("PvpServer", true);
        DELETE_DAYS = server.getProperty("DeleteCharAfterDays", 7);
        MAXIMUM_ONLINE_USERS = server.getProperty("MaximumOnlineUsers", 100);
        AUTO_LOOT = server.getProperty("AutoLoot", false);
        AUTO_LOOT_HERBS = server.getProperty("AutoLootHerbs", false);
        AUTO_LOOT_RAID = server.getProperty("AutoLootRaid", false);
        ALLOW_DISCARDITEM = server.getProperty("AllowDiscardItem", true);
        MULTIPLE_ITEM_DROP = server.getProperty("MultipleItemDrop", true);
        HERB_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyHerbTime", 15) * 1000;
        ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyItemTime", 600) * 1000;
        EQUIPABLE_ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyEquipableItemTime", 0) * 1000;
        SPECIAL_ITEM_DESTROY_TIME = new HashMap<>();
        String[] data = server.getProperty("AutoDestroySpecialItemTime", (String[]) null, ",");
        if (data != null)
            for (String itemData : data) {
                String[] item = itemData.split("-");
                SPECIAL_ITEM_DESTROY_TIME.put(Integer.parseInt(item[0]), Integer.parseInt(item[1]) * 1000);
            }
        PLAYER_DROPPED_ITEM_MULTIPLIER = server.getProperty("PlayerDroppedItemMultiplier", 1);
        RATE_XP = server.getProperty("RateXp", 1.0D);
        RATE_SP = server.getProperty("RateSp", 1.0D);
        RATE_PARTY_XP = server.getProperty("RatePartyXp", 1.0D);
        RATE_PARTY_SP = server.getProperty("RatePartySp", 1.0D);
        RATE_DROP_ADENA = server.getProperty("RateDropAdena", 1.0D);
        RATE_DROP_ITEMS = server.getProperty("RateDropItems", 1.0D);
        RATE_DROP_ITEMS_BY_RAID = server.getProperty("RateRaidDropItems", 1.0D);
        RATE_DROP_SPOIL = server.getProperty("RateDropSpoil", 1.0D);
        RATE_DROP_MANOR = server.getProperty("RateDropManor", 1);
        RATE_QUEST_DROP = server.getProperty("RateQuestDrop", 1.0D);
        RATE_QUEST_REWARD = server.getProperty("RateQuestReward", 1.0D);
        RATE_QUEST_REWARD_XP = server.getProperty("RateQuestRewardXP", 1.0D);
        RATE_QUEST_REWARD_SP = server.getProperty("RateQuestRewardSP", 1.0D);
        RATE_QUEST_REWARD_ADENA = server.getProperty("RateQuestRewardAdena", 1.0D);
        RATE_KARMA_EXP_LOST = server.getProperty("RateKarmaExpLost", 1.0D);
        RATE_SIEGE_GUARDS_PRICE = server.getProperty("RateSiegeGuardsPrice", 1.0D);
        RATE_DROP_COMMON_HERBS = server.getProperty("RateCommonHerbs", 1.0D);
        RATE_DROP_HP_HERBS = server.getProperty("RateHpHerbs", 1.0D);
        RATE_DROP_MP_HERBS = server.getProperty("RateMpHerbs", 1.0D);
        RATE_DROP_SPECIAL_HERBS = server.getProperty("RateSpecialHerbs", 1.0D);
        PLAYER_DROP_LIMIT = server.getProperty("PlayerDropLimit", 3);
        PLAYER_RATE_DROP = server.getProperty("PlayerRateDrop", 5);
        PLAYER_RATE_DROP_ITEM = server.getProperty("PlayerRateDropItem", 70);
        PLAYER_RATE_DROP_EQUIP = server.getProperty("PlayerRateDropEquip", 25);
        PLAYER_RATE_DROP_EQUIP_WEAPON = server.getProperty("PlayerRateDropEquipWeapon", 5);
        PET_XP_RATE = server.getProperty("PetXpRate", 1.0D);
        PET_FOOD_RATE = server.getProperty("PetFoodRate", 1);
        SINEATER_XP_RATE = server.getProperty("SinEaterXpRate", 1.0D);
        KARMA_DROP_LIMIT = server.getProperty("KarmaDropLimit", 10);
        KARMA_RATE_DROP = server.getProperty("KarmaRateDrop", 70);
        KARMA_RATE_DROP_ITEM = server.getProperty("KarmaRateDropItem", 50);
        KARMA_RATE_DROP_EQUIP = server.getProperty("KarmaRateDropEquip", 40);
        KARMA_RATE_DROP_EQUIP_WEAPON = server.getProperty("KarmaRateDropEquipWeapon", 10);
        ALLOW_FREIGHT = server.getProperty("AllowFreight", true);
        ALLOW_WAREHOUSE = server.getProperty("AllowWarehouse", true);
        ALLOW_WEAR = server.getProperty("AllowWear", true);
        WEAR_DELAY = server.getProperty("WearDelay", 5);
        WEAR_PRICE = server.getProperty("WearPrice", 10);
        ALLOW_LOTTERY = server.getProperty("AllowLottery", true);
        ALLOW_WATER = server.getProperty("AllowWater", true);
        ALLOW_MANOR = server.getProperty("AllowManor", true);
        ALLOW_BOAT = server.getProperty("AllowBoat", true);
        ALLOW_CURSED_WEAPONS = server.getProperty("AllowCursedWeapons", true);
        ENABLE_FALLING_DAMAGE = server.getProperty("EnableFallingDamage", true);
        ALT_DEV_NO_SPAWNS = server.getProperty("NoSpawns", false);
        DEVELOPER = server.getProperty("Developer", false);
        PACKET_HANDLER_DEBUG = server.getProperty("PacketHandlerDebug", false);
        DEBUG_MOVEMENT = server.getProperty("DebugMovement", 0) * 1000;
        DEADLOCK_DETECTOR = server.getProperty("DeadLockDetector", false);
        DEADLOCK_CHECK_INTERVAL = server.getProperty("DeadLockCheckInterval", 20);
        RESTART_ON_DEADLOCK = server.getProperty("RestartOnDeadlock", false);
        LOG_CHAT = server.getProperty("LogChat", false);
        LOG_ITEMS = server.getProperty("LogItems", false);
        GMAUDIT = server.getProperty("GMAudit", false);
        ENABLE_COMMUNITY_BOARD = server.getProperty("EnableCommunityBoard", false);
        BBS_DEFAULT = server.getProperty("BBSDefault", "_bbshome");
        ROLL_DICE_TIME = server.getProperty("RollDiceTime", 4200);
        HERO_VOICE_TIME = server.getProperty("HeroVoiceTime", 10000);
        SUBCLASS_TIME = server.getProperty("SubclassTime", 2000);
        DROP_ITEM_TIME = server.getProperty("DropItemTime", 1000);
        SERVER_BYPASS_TIME = server.getProperty("ServerBypassTime", 500);
        MULTISELL_TIME = server.getProperty("MultisellTime", 100);
        MANUFACTURE_TIME = server.getProperty("ManufactureTime", 300);
        MANOR_TIME = server.getProperty("ManorTime", 3000);
        SENDMAIL_TIME = server.getProperty("SendMailTime", 10000);
        CHARACTER_SELECT_TIME = server.getProperty("CharacterSelectTime", 3000);
        GLOBAL_CHAT_TIME = server.getProperty("GlobalChatTime", 0);
        TRADE_CHAT_TIME = server.getProperty("TradeChatTime", 0);
        SOCIAL_TIME = server.getProperty("SocialTime", 2000);
        SCHEDULED_THREAD_POOL_COUNT = server.getProperty("ScheduledThreadPoolCount", -1);
        THREADS_PER_SCHEDULED_THREAD_POOL = server.getProperty("ThreadsPerScheduledThreadPool", 4);
        INSTANT_THREAD_POOL_COUNT = server.getProperty("InstantThreadPoolCount", -1);
        THREADS_PER_INSTANT_THREAD_POOL = server.getProperty("ThreadsPerInstantThreadPool", 2);
        L2WALKER_PROTECTION = server.getProperty("L2WalkerProtection", false);
        ZONE_TOWN = server.getProperty("ZoneTown", 0);
        SERVER_NEWS = server.getProperty("ShowServerNews", false);
        DISABLE_TUTORIAL = server.getProperty("DisableTutorial", false);
    }

    private static void loadEventEngine() {
        ExProperties eventengine = initProperties("./config/events/eventengine.properties");
        ENABLE_EVENT_ENGINE = eventengine.getProperty("EnableEventEngine", false);
        EVENT_REGISTRATION_TIME = eventengine.getProperty("EventRegistrationTime", 10);
        ALLOW_DM_EVENT = eventengine.getProperty("AllowDMEvent", false);
        DM_EVENT_INTERVAL = eventengine.getProperty("DMEventInterval", "8:00,14:00,20:00,2:00").split(",");
        DM_MIN_PLAYERS = eventengine.getProperty("DMMinPlayers", 2);
        String[] dm_on_kill_split = eventengine.getProperty("DMOnKillRewards", "").split(";");
        for (String s : dm_on_kill_split) {
            String[] ss = s.split(",");
            DM_ON_KILL_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        String[] dm_win_split = eventengine.getProperty("DMWinnerRewards", "57,1").split(";");
        for (String s : dm_win_split) {
            String[] ss = s.split(",");
            DM_WINNER_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        DM_RUNNING_TIME = eventengine.getProperty("DMRunningTime", 10);
        String dm_resp_spots = eventengine.getProperty("DMRespawnSpots", "0,0,0;0,0,0");
        String[] dm_resp_spots_split = dm_resp_spots.split(";");
        for (String s : dm_resp_spots_split) {
            String[] ss = s.split(",");
            DM_RESPAWN_SPOTS.add(new Location(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2])));
        }
        TVT_EVENT_INTERVAL = eventengine.getProperty("TvTEventInterval", "20:00").split(",");
        ALLOW_TVT_EVENT = eventengine.getProperty("AllowTvTEvent", false);
        TVT_MIN_PLAYERS = eventengine.getProperty("TvTMinPlayers", 2);
        String tvt_win = eventengine.getProperty("TvTWinnerRewards", "57,1");
        String[] tvt_win_split = tvt_win.split(";");
        for (String s : tvt_win_split) {
            String[] ss = s.split(",");
            TVT_WINNER_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        String tvt_draw = eventengine.getProperty("TvTDrawRewards", "57,1");
        String[] tvt_draw_split = tvt_draw.split(";");
        for (String s : tvt_draw_split) {
            String[] ss = s.split(",");
            TVT_DRAW_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        TVT_RUNNING_TIME = eventengine.getProperty("TvTRunningTime", 10);
        TVT_TEAM_1_NAME = eventengine.getProperty("TvTTeam1Name", "Orange");
        TVT_TEAM_1_COLOR = Integer.decode("0x" + eventengine.getProperty("TvTTeam1Color", "4499FF"));
        String tvt_team_1_loc = eventengine.getProperty("TvTTeam1Location", "0,0,0");
        String[] tvt_team_1_loc_split = tvt_team_1_loc.split(",");
        TVT_TEAM_1_LOCATION = new Location(Integer.parseInt(tvt_team_1_loc_split[0]), Integer.parseInt(tvt_team_1_loc_split[1]), Integer.parseInt(tvt_team_1_loc_split[2]));
        TVT_TEAM_2_NAME = eventengine.getProperty("TvTTeam2Name", "Green");
        TVT_TEAM_2_COLOR = Integer.decode("0x" + eventengine.getProperty("TvTTeam2Color", "00FF00"));
        String tvt_team_2_loc = eventengine.getProperty("TvTTeam2Location", "0,0,0");
        String[] tvt_team_2_loc_split = tvt_team_2_loc.split(",");
        TVT_TEAM_2_LOCATION = new Location(Integer.parseInt(tvt_team_2_loc_split[0]), Integer.parseInt(tvt_team_2_loc_split[1]), Integer.parseInt(tvt_team_2_loc_split[2]));
        ALLOW_CTF_EVENT = eventengine.getProperty("AllowCTFEvent", false);
        CTF_EVENT_INTERVAL = eventengine.getProperty("CTFEventInterval", "20:00").split(",");
        CTF_MIN_PLAYERS = eventengine.getProperty("CTFMinPlayers", 2);
        String ctf_on_score = eventengine.getProperty("CTFOnScoreRewards", "57,1");
        String[] ctf_on_score_split = ctf_on_score.split(";");
        for (String s : ctf_on_score_split) {
            String[] ss = s.split(",");
            CTF_ON_SCORE_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        String ctf_win = eventengine.getProperty("CTFWinnerRewards", "57,1");
        String[] ctf_win_split = ctf_win.split(";");
        for (String s : ctf_win_split) {
            String[] ss = s.split(",");
            CTF_WINNER_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        CTF_DRAW_REWARDS = new HashMap<>();
        for (String s : eventengine.getProperty("CTFDrawRewards", "57,1").split(";")) {
            String[] ss = s.split(",");
            CTF_DRAW_REWARDS.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
        }
        CTF_RUNNING_TIME = eventengine.getProperty("CTFRunningTime", 10);
        CTF_TEAM_1_NAME = eventengine.getProperty("CTFTeam1Name", "Orange");
        CTF_TEAM_1_COLOR = Integer.decode("0x" + eventengine.getProperty("CTFTeam1Color", "4499FF"));
        String ctf_team_1_loc = eventengine.getProperty("CTFTeam1Location", "0,0,0");
        String[] ctf_team_1_loc_split = ctf_team_1_loc.split(",");
        CTF_TEAM_1_LOCATION = new Location(Integer.parseInt(ctf_team_1_loc_split[0]), Integer.parseInt(ctf_team_1_loc_split[1]), Integer.parseInt(ctf_team_1_loc_split[2]));
        String ctf_team_1_flag_loc = eventengine.getProperty("CTFTeam1FlagLocation", "0,0,0");
        String[] ctf_team_1_flag_loc_split = ctf_team_1_flag_loc.split(",");
        CTF_TEAM_1_FLAG_LOCATION = new Location(Integer.parseInt(ctf_team_1_flag_loc_split[0]), Integer.parseInt(ctf_team_1_flag_loc_split[1]), Integer.parseInt(ctf_team_1_flag_loc_split[2]));
        CTF_TEAM_2_NAME = eventengine.getProperty("CTFTeam2Name", "Green");
        CTF_TEAM_2_COLOR = Integer.decode("0x" + eventengine.getProperty("CTFTeam2Color", "00FF00"));
        String ctf_team_2_loc = eventengine.getProperty("CTFTeam2Location", "0,0,0");
        String[] ctf_team_2_loc_split = ctf_team_2_loc.split(",");
        CTF_TEAM_2_LOCATION = new Location(Integer.parseInt(ctf_team_2_loc_split[0]), Integer.parseInt(ctf_team_2_loc_split[1]), Integer.parseInt(ctf_team_2_loc_split[2]));
        String ctf_team_2_flag_loc = eventengine.getProperty("CTFTeam2FlagLocation", "0,0,0");
        String[] ctf_team_2_flag_loc_split = ctf_team_2_flag_loc.split(",");
        CTF_TEAM_2_FLAG_LOCATION = new Location(Integer.parseInt(ctf_team_2_flag_loc_split[0]), Integer.parseInt(ctf_team_2_flag_loc_split[1]), Integer.parseInt(ctf_team_2_flag_loc_split[2]));
    }

    private static void loadPcBangConfig() {
        ExProperties PcBanG = initProperties("./config/events/pcBangEvent.properties");
        PCB_ENABLE = Boolean.parseBoolean(PcBanG.getProperty("PcBangPointEnable", "true"));
        PCB_MIN_LEVEL = Integer.parseInt(PcBanG.getProperty("PcBangPointMinLevel", "20"));
        PCB_POINT_MIN = Integer.parseInt(PcBanG.getProperty("PcBangPointMinCount", "20"));
        PCB_POINT_MAX = Integer.parseInt(PcBanG.getProperty("PcBangPointMaxCount", "1000000"));
        PCB_COIN_ID = Integer.parseInt(PcBanG.getProperty("PCBCoinId", "0"));
        if (PCB_POINT_MAX < 1)
            PCB_POINT_MAX = Integer.MAX_VALUE;
        PCB_CHANCE_DUAL_POINT = Integer.parseInt(PcBanG.getProperty("PcBangPointDualChance", "20"));
        PCB_INTERVAL = Integer.parseInt(PcBanG.getProperty("PcBangPointTimeStamp", "900"));
    }

    private static void loadPvpEvent() {
        ExProperties pvpevent = initProperties("./config/events/pvpEvent.properties");
        PVP_EVENT_ENABLED = pvpevent.getProperty("PvPEventEnabled", false);
        PVP_EVENT_INTERVAL = pvpevent.getProperty("PvPZEventInterval", "20:00").split(",");
        PVP_EVENT_RUNNING_TIME = pvpevent.getProperty("PvPZEventRunningTime", 120);
        PVP_EVENT_REWARDS = parseItemsList(pvpevent.getProperty("PvPEventWinnerReward", "6651,50"));
        ALLOW_SPECIAL_PVP_REWARD = pvpevent.getProperty("SpecialPvpRewardEnabled", false);
        PVP_SPECIAL_ITEMS_REWARD = new ArrayList<>();
        String[] pvpSpeReward = pvpevent.getProperty("SpecialPvpItemsReward", "57,100000").split(";");
        for (String reward : pvpSpeReward) {
            String[] rewardSplit = reward.split(",");
            if (rewardSplit.length != 2) {
                LOGGER.warn("SpecialPvpItemsReward: invalid config property -> PvpItemsReward \"" + reward + "\"");
            } else {
                try {
                    PVP_SPECIAL_ITEMS_REWARD.add(new int[]{Integer.parseInt(rewardSplit[0]),
                            Integer.parseInt(rewardSplit[1])});
                } catch (NumberFormatException nfe) {
                    if (!reward.isEmpty())
                        LOGGER.warn("SpecialPvpItemsReward: invalid config property -> PvpItemsReward \"" + reward + "\"");
                }
            }
        }
    }

    private static void loadBossEvent() {
        ExProperties bossEvent = initProperties("./config/events/killTheBossEvent.properties");
        BOSS_EVENT_BY_TIME_OF_DAY = bossEvent.getProperty("EventTime", "20:00").split(",");
        for (String bossList : bossEvent.getProperty("BossList", "29046;29029").split(";"))
            BOSS_EVENT_ID.add(Integer.parseInt(bossList));
        for (String locationsList : bossEvent.getProperty("LocationsList", "10468,-24569,-3645;174229,-88032,-5116").split(";")) {
            String[] coords = locationsList.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            BOSS_EVENT_LOCATION.add(new Location(x, y, z));
        }
        BOSS_EVENT_MIN_PLAYERS = bossEvent.getProperty("MinPlayers", 1);
        BOSS_EVENT_MIN_DAMAGE_TO_OBTAIN_REWARD = bossEvent.getProperty("MinDamage", 2000);
        BOSS_EVENT_REGISTRATION_TIME = bossEvent.getProperty("RegistrationTime", 120);
        BOSS_EVENT_REWARD_ID = bossEvent.getProperty("RewardId", 3470);
        BOSS_EVENT_REWARD_COUNT = bossEvent.getProperty("RewardCount", 10);
        BOSS_EVENT_TIME_TO_WAIT = bossEvent.getProperty("WaitTime", 30);
        BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS = bossEvent.getProperty("TeleportTime", 15);
        BOSS_EVENT_REWARD_LAST_ATTACKER = bossEvent.getProperty("RewardLastAttacker", true);
        BOSS_EVENT_REWARD_MAIN_DAMAGE_DEALER = bossEvent.getProperty("RewardMainDamageDealer", true);
        for (String rewards : bossEvent.getProperty("GeneralRewards", "57,100000;3470,10").split(";")) {
            String[] reward = rewards.split(",");
            BOSS_EVENT_GENERAL_REWARDS.put(Integer.parseInt(reward[0]), Integer.parseInt(reward[1]));
        }
        for (String rewards : bossEvent.getProperty("MainDamageDealerRewards", "57,100000;3470,10").split(";")) {
            String[] reward = rewards.split(",");
            BOSS_EVENT_MAIN_DAMAGE_DEALER_REWARDS.put(Integer.parseInt(reward[0]), Integer.parseInt(reward[1]));
        }
        for (String rewards : bossEvent.getProperty("LastAttackerRewards", "57,100000;3470,10").split(";")) {
            String[] reward = rewards.split(",");
            BOSS_EVENT_LAST_ATTACKER_REWARDS.put(Integer.parseInt(reward[0]), Integer.parseInt(reward[1]));
        }
        BOSS_EVENT_REGISTRATION_NPC_ID = bossEvent.getProperty("RegisterNpcID", 10003);
        BOSS_EVENT_TIME_TO_DESPAWN_BOSS = bossEvent.getProperty("TimeToDespawnBoss", 300);
        String[] regLoc = bossEvent.getProperty("RegisterNpcLocation", "82727,148605,-3471").split(",");
        BOSS_EVENT_NPC_REGISTER_LOC = new Location(Integer.parseInt(regLoc[0]), Integer.parseInt(regLoc[1]), Integer.parseInt(regLoc[2]));
        BOSS_EVENT_TIME_ON_SCREEN = bossEvent.getProperty("EventTimeOnScreen", true);
    }

    private static void loadTour() {
        ExProperties tournament = initProperties("./config/events/tournament.properties");
        TOURNAMENT_EVENT_START = tournament.getProperty("TournamentStartOn", false);
        TOURNAMENT_EVENT_TIME = tournament.getProperty("TournamentAutoEvent", false);
        TOURNAMENT_EVENT_SUMMON = tournament.getProperty("TournamentSummon", false);
        TOURNAMENT_EVENT_ANNOUNCE = tournament.getProperty("TournamenAnnounce", false);
        TOURNAMENT_EVENT_INTERVAL_BY_TIME_OF_DAY = tournament.getProperty("TournamentStartTime", "20:00").split(",");
        TOURNAMENT_TIME = Integer.parseInt(tournament.getProperty("TournamentEventTime", "1"));
        TITLE_COLOR_TEAM1 = tournament.getProperty("TitleColorTeam_1", "FFFFFF");
        TITLE_COLOR_TEAM2 = tournament.getProperty("TitleColorTeam_2", "FFFFFF");
        MSG_TEAM1 = tournament.getProperty("TitleTeam_1", "Team [1]");
        MSG_TEAM2 = tournament.getProperty("TitleTeam_2", "Team [2]");
        Allow_Same_HWID_On_Tournament = Boolean.parseBoolean(tournament.getProperty("Allow_Same_HWID_On_Tournament", "true"));
        ARENA_NPC = Integer.parseInt(tournament.getProperty("NPCRegister", "1"));
        NPC_locx = Integer.parseInt(tournament.getProperty("Locx", "1"));
        NPC_locy = Integer.parseInt(tournament.getProperty("Locy", "1"));
        NPC_locz = Integer.parseInt(tournament.getProperty("Locz", "1"));
        NPC_Heading = Integer.parseInt(tournament.getProperty("Heading", "1"));
        Tournament_locx = Integer.parseInt(tournament.getProperty("TournamentLocx", "1"));
        Tournament_locy = Integer.parseInt(tournament.getProperty("TournamentLocy", "1"));
        Tournament_locz = Integer.parseInt(tournament.getProperty("TournamentLocz", "1"));
        ALLOW_2X2_REGISTER = Boolean.parseBoolean(tournament.getProperty("Allow2x2Register", "true"));
        ALLOW_4X4_REGISTER = Boolean.parseBoolean(tournament.getProperty("Allow4x4Register", "true"));
        ALLOW_9X9_REGISTER = Boolean.parseBoolean(tournament.getProperty("Allow9x9Register", "true"));
        ALLOW_4X4_LOSTBUFF = Boolean.parseBoolean(tournament.getProperty("Allow4x4LostBuff", "false"));
        ARENA_MESSAGE_ENABLED = Boolean.parseBoolean(tournament.getProperty("ScreenArenaMessageEnable", "false"));
        ARENA_MESSAGE_TEXT = tournament.getProperty("ScreenArenaMessageText", "Welcome to L2J server!");
        ARENA_MESSAGE_TIME = Integer.parseInt(tournament.getProperty("ScreenArenaMessageTime", "10")) * 1000;
        String[] arenaLocs = tournament.getProperty("ArenasLoc", "").split(";");
        String[] locSplit = null;
        ARENA_EVENT_COUNT = arenaLocs.length;
        ARENA_EVENT_LOCS = new int[ARENA_EVENT_COUNT][3];
        for (int i = 0; i < ARENA_EVENT_COUNT; i++) {
            locSplit = arenaLocs[i].split(",");
            for (int m = 0; m < 3; m++)
                ARENA_EVENT_LOCS[i][m] = Integer.parseInt(locSplit[m].trim());
        }
        String[] arenaLocs4x4 = tournament.getProperty("Arenas4x4Loc", "").split(";");
        String[] locSplit4x4 = null;
        ARENA_EVENT_COUNT_4X4 = arenaLocs4x4.length;
        ARENA_EVENT_LOCS_4X4 = new int[ARENA_EVENT_COUNT_4X4][3];
        for (int j = 0; j < ARENA_EVENT_COUNT_4X4; j++) {
            locSplit4x4 = arenaLocs4x4[j].split(",");
            for (int m = 0; m < 3; m++)
                ARENA_EVENT_LOCS_4X4[j][m] = Integer.parseInt(locSplit4x4[m].trim());
        }
        String[] arenaLocs9x9 = tournament.getProperty("Arenas9x9Loc", "").split(";");
        String[] locSplit8x8 = null;
        ARENA_EVENT_COUNT_9X9 = arenaLocs9x9.length;
        ARENA_EVENT_LOCS_9X9 = new int[ARENA_EVENT_COUNT_9X9][3];
        for (int k = 0; k < ARENA_EVENT_COUNT_9X9; k++) {
            locSplit8x8 = arenaLocs9x9[k].split(",");
            for (int m = 0; m < 3; m++)
                ARENA_EVENT_LOCS_9X9[k][m] = Integer.parseInt(locSplit8x8[m].trim());
        }
        duelist_COUNT_4X4 = tournament.getProperty("duelist_amount_4x4", 1);
        dreadnought_COUNT_4X4 = tournament.getProperty("dreadnought_amount_4x4", 1);
        tanker_COUNT_4X4 = tournament.getProperty("tanker_amount_4x4", 1);
        dagger_COUNT_4X4 = tournament.getProperty("dagger_amount_4x4", 1);
        archer_COUNT_4X4 = tournament.getProperty("archer_amount_4x4", 1);
        bs_COUNT_4X4 = tournament.getProperty("bs_amount_4x4", 1);
        archmage_COUNT_4X4 = tournament.getProperty("archmage_amount_4x4", 1);
        soultaker_COUNT_4X4 = tournament.getProperty("soultaker_amount_4x4", 1);
        mysticMuse_COUNT_4X4 = tournament.getProperty("mysticMuse_amount_4x4", 1);
        stormScreamer_COUNT_4X4 = tournament.getProperty("stormScreamer_amount_4x4", 1);
        titan_COUNT_4X4 = tournament.getProperty("titan_amount_4x4", 1);
        dominator_COUNT_4X4 = tournament.getProperty("dominator_amount_4x4", 1);
        doomcryer_COUNT_4X4 = tournament.getProperty("doomcryer_amount_4x4", 1);
        duelist_COUNT_9X9 = tournament.getProperty("duelist_amount_9x9", 1);
        dreadnought_COUNT_9X9 = tournament.getProperty("dreadnought_amount_9x9", 1);
        tanker_COUNT_9X9 = tournament.getProperty("tanker_amount_9x9", 1);
        dagger_COUNT_9X9 = tournament.getProperty("dagger_amount_9x9", 1);
        archer_COUNT_9X9 = tournament.getProperty("archer_amount_9x9", 1);
        bs_COUNT_9X9 = tournament.getProperty("bs_amount_9x9", 1);
        archmage_COUNT_9X9 = tournament.getProperty("archmage_amount_9x9", 1);
        soultaker_COUNT_9X9 = tournament.getProperty("soultaker_amount_9x9", 1);
        mysticMuse_COUNT_9X9 = tournament.getProperty("mysticMuse_amount_9x9", 1);
        stormScreamer_COUNT_9X9 = tournament.getProperty("stormScreamer_amount_9x9", 1);
        titan_COUNT_9X9 = tournament.getProperty("titan_amount_9x9", 1);
        grandKhauatari_COUNT_9X9 = tournament.getProperty("grandKhauatari_amount_9x9", 1);
        dominator_COUNT_9X9 = tournament.getProperty("dominator_amount_9x9", 1);
        doomcryer_COUNT_9X9 = tournament.getProperty("doomcryer_amount_9x9", 1);
        ARENA_PVP_AMOUNT = tournament.getProperty("ArenaPvpJoin", 10);
        ARENA_REWARD_ID = tournament.getProperty("ArenaRewardId", 57);
        ARENA_WIN_REWARD_COUNT = tournament.getProperty("ArenaWinRewardCount", 1);
        ARENA_LOST_REWARD_COUNT = tournament.getProperty("ArenaLostRewardCount", 1);
        ARENA_WIN_REWARD_COUNT_4X4 = tournament.getProperty("ArenaWinRewardCount4x4", 1);
        ARENA_LOST_REWARD_COUNT_4X4 = tournament.getProperty("ArenaLostRewardCount4x4", 1);
        ARENA_WIN_REWARD_COUNT_9X9 = tournament.getProperty("ArenaWinRewardCount9x9", 1);
        ARENA_LOST_REWARD_COUNT_9X9 = tournament.getProperty("ArenaLostRewardCount9x9", 1);
        ARENA_CHECK_INTERVAL = tournament.getProperty("ArenaBattleCheckInterval", 15) * 1000;
        ARENA_CALL_INTERVAL = tournament.getProperty("ArenaBattleCallInterval", 60);
        ARENA_WAIT_INTERVAL = tournament.getProperty("ArenaBattleWaitInterval", 20);
        ARENA_WAIT_INTERVAL_4X4 = tournament.getProperty("ArenaBattleWaitInterval4x4", 45);
        ARENA_WAIT_INTERVAL_9X9 = tournament.getProperty("ArenaBattleWaitInterval9x9", 45);
        TOURNAMENT_ID_RESTRICT = tournament.getProperty("ItemsRestriction");
        TOURNAMENT_LISTID_RESTRICT = new ArrayList<>();
        for (String id : TOURNAMENT_ID_RESTRICT.split(","))
            TOURNAMENT_LISTID_RESTRICT.add(Integer.parseInt(id));
        ARENA_SKILL_PROTECT = Boolean.parseBoolean(tournament.getProperty("ArenaSkillProtect", "false"));
        for (String id : tournament.getProperty("ArenaDisableSkillList", "0").split(","))
            ARENA_SKILL_LIST.add(Integer.parseInt(id));
        for (String id : tournament.getProperty("DisableSkillList", "0").split(","))
            ARENA_DISABLE_SKILL_LIST_PERM.add(Integer.parseInt(id));
        for (String id : tournament.getProperty("ArenaDisableSkillList_noStart", "0").split(","))
            ARENA_DISABLE_SKILL_LIST.add(Integer.parseInt(id));
        for (String id : tournament.getProperty("ArenaStopSkillList", "0").split(","))
            ARENA_STOP_SKILL_LIST.add(Integer.parseInt(id));
    }

    private static void loadPartyFarm() {
        ExProperties DungeonPartyFarm = initProperties("./config/events/partyfarm.properties");
        NPC_LIST = DungeonPartyFarm.getProperty("NpcListPartyDrop", "10506,10507");
        ENABLE_DROP_PARTYFARM = DungeonPartyFarm.getProperty("EnableDrop", false);
        String[] temp = DungeonPartyFarm.getProperty("PartyDropList", "").split(";");
        for (String s : temp) {
            List<Integer> list = new ArrayList<>();
            String[] t = s.split(",");
            list.add(Integer.parseInt(t[1]));
            list.add(Integer.parseInt(t[2]));
            list.add(Integer.parseInt(t[3]));
            PARTY_DROP_LIST.put(Integer.parseInt(t[0]), list);
        }
        PARTY_FARM_MONSTER_DALAY = Integer.parseInt(DungeonPartyFarm.getProperty("MonsterDelay", "10"));
        PARTY_FARM_BY_TIME_OF_DAY = Boolean.parseBoolean(DungeonPartyFarm.getProperty("PartyFarmEventEnabled", "false"));
        START_PARTY = Boolean.parseBoolean(DungeonPartyFarm.getProperty("StartSpawnPartyFarm", "false"));
        NPC_SERVER_DELAY = DungeonPartyFarm.getProperty("npcServerDelay", 70);
        EVENT_BEST_FARM_TIME = Integer.parseInt(DungeonPartyFarm.getProperty("EventBestFarmTime", "1"));
        EVENT_BEST_FARM_INTERVAL_BY_TIME_OF_DAY = DungeonPartyFarm.getProperty("BestFarmStartTime", "20:00").split(",");
        PARTY_MESSAGE_ENABLED = Boolean.parseBoolean(DungeonPartyFarm.getProperty("ScreenPartyMessageEnable", "false"));
        PARTY_FARMANNONCER = DungeonPartyFarm.getProperty("TownAnnoncer", "Abandoned Camp");
        PARTY_FARM_MESSAGE_TEXT = DungeonPartyFarm.getProperty("ScreenPartyFarmMessageText", "Welcome to l2j server!");
        PARTY_FARM_MESSAGE_TIME = Integer.parseInt(DungeonPartyFarm.getProperty("ScreenPartyFarmMessageTime", "10")) * 1000;
        String[] monsterLocs2 = DungeonPartyFarm.getProperty("MonsterLoc", "").split(";");
        String[] locSplit3 = null;
        monsterId = Integer.parseInt(DungeonPartyFarm.getProperty("MonsterId", "1"));
        MONSTER_LOCS_COUNT = monsterLocs2.length;
        MONSTER_LOCS = new int[MONSTER_LOCS_COUNT][3];
        for (int e = 0; e < MONSTER_LOCS_COUNT; e++) {
            locSplit3 = monsterLocs2[e].split(",");
            for (int g = 0; g < 3; g++)
                MONSTER_LOCS[e][g] = Integer.parseInt(locSplit3[g].trim());
        }
    }

    private static void loadLogin() {
        ExProperties server = initProperties("./config/loginserver.properties");
        HOSTNAME = server.getProperty("Hostname", "localhost");
        LOGIN_BIND_ADDRESS = server.getProperty("LoginserverHostname", "*");
        PORT_LOGIN = server.getProperty("LoginserverPort", 2106);
        GAME_SERVER_LOGIN_HOST = server.getProperty("LoginHostname", "*");
        GAME_SERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014);
        LOGIN_TRY_BEFORE_BAN = server.getProperty("LoginTryBeforeBan", 3);
        LOGIN_BLOCK_AFTER_BAN = server.getProperty("LoginBlockAfterBan", 600);
        ACCEPT_NEW_GAMESERVER = server.getProperty("AcceptNewGameServer", false);
        SHOW_LICENCE = server.getProperty("ShowLicence", true);
        DATABASE_URL = server.getProperty("URL", "jdbc:mariadb://localhost/pvp2025?serverTimezone=UTC");
        DATABASE_LOGIN = server.getProperty("Login", "root");
        DATABASE_PASSWORD = server.getProperty("Password", "");
        DATABASE_MAX_CONNECTIONS = server.getProperty("MaximumDbConnections", 10);
        AUTO_CREATE_ACCOUNTS = server.getProperty("AutoCreateAccounts", true);
        LOG_LOGIN_CONTROLLER = server.getProperty("LogLoginController", false);
        FLOOD_PROTECTION = server.getProperty("EnableFloodProtection", true);
        FAST_CONNECTION_LIMIT = server.getProperty("FastConnectionLimit", 15);
        NORMAL_CONNECTION_TIME = server.getProperty("NormalConnectionTime", 700);
        FAST_CONNECTION_TIME = server.getProperty("FastConnectionTime", 350);
        MAX_CONNECTION_PER_IP = server.getProperty("MaxConnectionPerIP", 50);
    }

    public static void loadGameServer() {
        LOGGER.info("Loading gameserver configuration files.");
        loadClans();
        loadcustomproperties();
        loadEvents();
        loadGeoengine();
        loadHexID();
        loadMultiFunctionZone();
        loadNpcs();
        loadPcBangConfig();
        loadPlayers();
        loadPvpEvent();
        loadBossEvent();
        loadSieges();
        loadServer();
        loadEventEngine();
        loadTour();
        loadPartyFarm();
    }

    public static void loadLoginServer() {
        LOGGER.info("Loading loginserver configuration files.");
        loadLogin();
    }

    public static void loadAccountManager() {
        LOGGER.info("Loading account manager configuration files.");
        loadLogin();
    }

    public static void loadGameServerRegistration() {
        LOGGER.info("Loading gameserver registration configuration files.");
        loadLogin();
    }

    public static void loadGeodataConverter() {
        LOGGER.info("Loading geodata converter configuration files.");
        loadGeoengine();
    }

    private static int[][] parseItemsList(String line) {
        String[] propertySplit = line.split(";");
        if (propertySplit.length == 0)
            return null;
        int i = 0;
        int[][] result = new int[propertySplit.length][];
        for (String value : propertySplit) {
            String[] valueSplit = value.split(",");
            if (valueSplit.length != 2) {
                LOGGER.warn("Config: Error parsing entry -> \"" + valueSplit[0] + "\", should be itemId,itemNumber");
                return null;
            }
            result[i] = new int[2];
            try {
                result[i][0] = Integer.parseInt(valueSplit[0]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Config: Error parsing item ID -> \"" + valueSplit[0] + "\"");
                return null;
            }
            try {
                result[i][1] = Integer.parseInt(valueSplit[1]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Config: Error parsing item amount -> \"" + valueSplit[1] + "\"");
                return null;
            }
            i++;
        }
        return result;
    }

    private static int[][] parseItemsList_array2d(String line) {
        String[] propertySplit = line.split(";");
        if (propertySplit.length == 0)
            return null;
        int i = 0;
        int[][] result = new int[propertySplit.length][];
        for (String value : propertySplit) {
            String[] valueSplit = value.split(",");
            if (valueSplit.length != 3)
                return null;
            result[i] = new int[3];
            try {
                result[i][0] = Integer.parseInt(valueSplit[0]);
            } catch (NumberFormatException e) {
                return null;
            }
            try {
                result[i][1] = Integer.parseInt(valueSplit[1]);
            } catch (NumberFormatException e) {
                return null;
            }
            try {
                result[i][2] = Integer.parseInt(valueSplit[2]);
            } catch (NumberFormatException e) {
                return null;
            }
            i++;
        }
        return result;
    }

    private static List<RewardHolder> parseReward(ExProperties propertie, String configName) {
        List<RewardHolder> auxReturn = new ArrayList<>();
        String aux = propertie.getProperty(configName).trim();
        for (String randomReward : aux.split(";")) {
            String[] infos = randomReward.split(",");
            if (infos.length > 3) {
                auxReturn.add(new RewardHolder(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]), Integer.parseInt(infos[2]), Integer.parseInt(infos[3])));
            } else {
                auxReturn.add(new RewardHolder(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]), Integer.parseInt(infos[2])));
            }
        }
        return auxReturn;
    }

    public static final class ClassMasterSettings {
        private final Map<Integer, Boolean> _allowedClassChange = new HashMap<>(3);

        private final Map<Integer, List<IntIntHolder>> _claimItems = new HashMap<>(3);

        private final Map<Integer, List<IntIntHolder>> _rewardItems = new HashMap<>(3);

        public ClassMasterSettings(String configLine) {
            if (configLine != null)
                parseConfigLine(configLine.trim());
        }

        private void parseConfigLine(String configLine) {
            StringTokenizer st = new StringTokenizer(configLine, ";");
            while (st.hasMoreTokens()) {
                int job = Integer.parseInt(st.nextToken());
                this._allowedClassChange.put(job, Boolean.TRUE);
                List<IntIntHolder> items = new ArrayList<>();
                if (st.hasMoreTokens()) {
                    StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
                    while (st2.hasMoreTokens()) {
                        StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
                        items.add(new IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())));
                    }
                }
                this._claimItems.put(job, items);
                items = new ArrayList<>();
                if (st.hasMoreTokens()) {
                    StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
                    while (st2.hasMoreTokens()) {
                        StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
                        items.add(new IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())));
                    }
                }
                this._rewardItems.put(job, items);
            }
        }

        public boolean isAllowed(int job) {
            if (this._allowedClassChange.containsKey(job))
                return this._allowedClassChange.get(job);
            return false;
        }

        public List<IntIntHolder> getRewardItems(int job) {
            return this._rewardItems.get(job);
        }

        public List<IntIntHolder> getRequiredItems(int job) {
            return this._claimItems.get(job);
        }
    }
}
