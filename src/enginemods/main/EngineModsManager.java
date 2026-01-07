package enginemods.main;

import enginemods.main.data.*;
import enginemods.main.engine.AbstractMods;
import enginemods.main.engine.community.ClanCommunityBoard;
import enginemods.main.engine.community.FavoriteCommunityBoard;
import enginemods.main.engine.community.MemoCommunityBoard;
import enginemods.main.engine.community.RegionComunityBoard;
import enginemods.main.engine.events.BonusWeekend;
import enginemods.main.engine.events.Champions;
import enginemods.main.engine.events.CityElpys;
import enginemods.main.engine.events.RandomBossSpawn;
import enginemods.main.engine.mods.*;
import enginemods.main.engine.npc.NpcBufferScheme;
import enginemods.main.engine.npc.NpcClassMaster;
import enginemods.main.engine.npc.NpcRanking;
import enginemods.main.engine.npc.NpcTeleporter;
import enginemods.main.instances.NpcDropsInstance;
import enginemods.main.instances.NpcExpInstance;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.zone.ZoneType;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EngineModsManager {
    private static final Logger LOG = Logger.getLogger(AbstractMods.class.getName());
    private static final Map<Integer, AbstractMods> ENGINES_MODS = new LinkedHashMap();

    public static void init() {
        try {
            ModsData.load();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        try {
            PlayerData.load();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        try {
            IconData.load();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        try {
            SkillData.load();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        try {
            ConfigData.load();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        try {
            FakePlayerData.load();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        try {
            SchemeBuffData.load();
        } catch (Exception var1) {
            var1.printStackTrace();
        }

        LOG.config("-----------------------------------------------------------");
        loadModsAndEvents();
    }

    private static void loadModsAndEvents() {
        ColorAccordingAmountPvPorPk.getInstance();
        EnchantAbnormalEffectArmor.getInstance();
        SpreeKills.getInstance();
        SubClassAcumulatives.getInstance();
        PvpReward.getInstance();
        AnnounceKillBoss.getInstance();
        SellBuffs.getInstance();
        AntiBot.getInstance();
        NewCharacterCreated.getInstance();
        SystemAio.getInstance();
        SystemVip.getInstance();
        OfflineShop.getInstance();
        BonusWeekend.getInstance();
        Champions.getInstance();
        RandomBossSpawn.getInstance();
        CityElpys.getInstance();
        FakePlayer.getInstance();
        NpcRanking.getInstance();
        NpcClassMaster.getInstance();
        NpcTeleporter.getInstance();
        NpcBufferScheme.getInstance();
        RegionComunityBoard.getInstance();
        FavoriteCommunityBoard.getInstance();
        ClanCommunityBoard.getInstance();
        MemoCommunityBoard.getInstance();
    }

    public static void registerMod(AbstractMods type) {
        ENGINES_MODS.put(type.hashCode(), type);
    }

    public static Collection<AbstractMods> getAllMods() {
        return ENGINES_MODS.values();
    }

    public static AbstractMods getMod(int type) {
        return ENGINES_MODS.get(type);
    }

    public static synchronized boolean onCommunityBoard(Player player, String command) {
        Iterator var2 = ENGINES_MODS.values().iterator();

        while (var2.hasNext()) {
            AbstractMods mod = (AbstractMods) var2.next();

            try {
                if (mod.isStarting() && mod.onCommunityBoard(player, command)) {
                    return true;
                }
            } catch (Exception var5) {
                LOG.log(Level.SEVERE, var5.getMessage());
                var5.printStackTrace();
            }
        }

        return false;
    }

    public static synchronized void onShutDown() {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onShutDown();
                mod.endMod();
                mod.cancelScheduledState();
            } catch (Exception var2) {
                LOG.log(Level.SEVERE, var2.getMessage());
                var2.printStackTrace();
            }

        });
    }

    public static synchronized boolean onExitWorld(Player player) {
        boolean exitPlayer = false;
        Iterator var2 = ENGINES_MODS.values().iterator();

        while (var2.hasNext()) {
            AbstractMods mod = (AbstractMods) var2.next();

            try {
                if (mod.isStarting() && mod.onExitWorld(player)) {
                    exitPlayer = true;
                }
            } catch (Exception var5) {
                LOG.log(Level.SEVERE, var5.getMessage());
                var5.printStackTrace();
            }
        }

        return exitPlayer;
    }

    public static synchronized boolean onNpcExpSp(Monster npc, Creature character) {
        if (character == null) {
            return false;
        } else {
            Player killer = character.getActingPlayer();
            if (killer == null) {
                return false;
            } else {
                NpcExpInstance instance = new NpcExpInstance();
                Iterator var4 = ENGINES_MODS.values().iterator();

                while (var4.hasNext()) {
                    AbstractMods mod = (AbstractMods) var4.next();

                    try {
                        if (mod.isStarting()) {
                            mod.onNpcExpSp(killer, npc, instance);
                        }
                    } catch (Exception var7) {
                        LOG.log(Level.SEVERE, var7.getMessage());
                        var7.printStackTrace();
                    }
                }

                if (instance.hasSettings()) {
                    instance.init(npc, character);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static synchronized boolean onNpcDrop(Monster npc, Creature character) {
        if (character == null) {
            return false;
        } else {
            Player killer = character.getActingPlayer();
            if (killer == null) {
                return false;
            } else {
                NpcDropsInstance instance = new NpcDropsInstance();
                Iterator var4 = ENGINES_MODS.values().iterator();

                while (var4.hasNext()) {
                    AbstractMods mod = (AbstractMods) var4.next();

                    try {
                        if (mod.isStarting()) {
                            mod.onNpcDrop(killer, npc, instance);
                        }
                    } catch (Exception var7) {
                        LOG.log(Level.SEVERE, var7.getMessage());
                        var7.printStackTrace();
                    }
                }

                if (instance.hasSettings()) {
                    instance.init(npc, character);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static synchronized void onEnterZone(Creature player, ZoneType zone) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onEnterZone(player, zone);
            } catch (Exception var4) {
                LOG.log(Level.SEVERE, var4.getMessage());
                var4.printStackTrace();
            }

        });
    }

    public static synchronized void onExitZone(Creature player, ZoneType zone) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onExitZone(player, zone);
            } catch (Exception var4) {
                LOG.log(Level.SEVERE, var4.getMessage());
                var4.printStackTrace();
            }

        });
    }

    public static synchronized void onCreateCharacter(Player player) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onCreateCharacter(player);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
    }

    public static synchronized boolean onVoiced(Player player, String chat) {
        Iterator var2 = ENGINES_MODS.values().iterator();

        while (var2.hasNext()) {
            AbstractMods mod = (AbstractMods) var2.next();
            if (mod.isStarting()) {
                try {
                    if (chat.startsWith("admin_")) {
                        if (player.getAccessLevel().getLevel() < 1) {
                            return false;
                        }

                        if (mod.onAdminCommand(player, chat.replace("admin_", ""))) {
                            return true;
                        }
                    } else if (chat.startsWith(".")) {
                        if (mod.onVoicedCommand(player, chat.replace(".", ""))) {
                            return true;
                        }
                    } else if (mod.onChat(player, chat)) {
                        return true;
                    }
                } catch (Exception var5) {
                    LOG.log(Level.SEVERE, var5.getMessage());
                    var5.printStackTrace();
                }
            }
        }

        return false;
    }

    public static synchronized boolean onInteract(Player player, Creature character) {
        Iterator var2 = ENGINES_MODS.values().iterator();

        while (var2.hasNext()) {
            AbstractMods mod = (AbstractMods) var2.next();
            if (mod.isStarting()) {
                try {
                    if (mod.onInteract(player, character)) {
                        return true;
                    }
                } catch (Exception var5) {
                    LOG.log(Level.SEVERE, var5.getMessage());
                    var5.printStackTrace();
                }
            }
        }

        return false;
    }

    public static synchronized void onEvent(Player player, String command) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return command.startsWith(mod.getClass().getSimpleName()) && mod.isStarting();
        }).forEach((mod) -> {
            WorldObject obj = player.getTarget();
            if (obj instanceof Creature) {
                if (obj == null || player.isInsideRadius(obj, 150, false, false)) {
                    try {
                        mod.onEvent(player, (Creature) obj, command.replace(mod.getClass().getSimpleName() + " ", ""));
                    } catch (Exception var5) {
                        LOG.log(Level.SEVERE, var5.getMessage());
                        var5.printStackTrace();
                    }

                }
            }
        });
    }

    public static synchronized String onSeeNpcTitle(int objectId) {
        String title = null;
        Iterator var2 = ENGINES_MODS.values().iterator();

        while (var2.hasNext()) {
            AbstractMods mod = (AbstractMods) var2.next();
            if (mod.isStarting()) {
                try {
                    String aux = mod.onSeeNpcTitle(objectId);
                    if (aux != null) {
                        title = aux;
                    }
                } catch (Exception var5) {
                    LOG.log(Level.SEVERE, var5.getMessage());
                    var5.printStackTrace();
                }
            }
        }

        return title;
    }

    public static synchronized void onSpawn(Npc obj) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onSpawn(obj);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
    }

    public static synchronized void onEnterWorld(Player player) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onEnterWorld(player);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
    }

    public static synchronized void onKill(Creature killer, Creature victim, boolean isPet) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onKill(killer, victim, isPet);
            } catch (Exception var5) {
                LOG.log(Level.SEVERE, var5.getMessage());
                var5.printStackTrace();
            }

        });
    }

    public static synchronized void onDeath(Creature player) {
        try {
            ENGINES_MODS.values().stream().filter((mod) -> {
                return mod.isStarting();
            }).forEach((mod) -> {
                mod.onDeath(player);
            });
        } catch (Exception var2) {
            LOG.log(Level.SEVERE, var2.getMessage());
            var2.printStackTrace();
        }

    }

    public static synchronized void onEnchant(Creature player) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onEnchant(player);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
    }

    public static synchronized void onEquip(Creature player) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onEquip(player);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
    }

    public static synchronized void onUnequip(Creature player) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onUnequip(player);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
    }

    public static synchronized boolean onRestoreSkills(Player player) {
        ENGINES_MODS.values().stream().filter((mod) -> {
            return mod.isStarting();
        }).forEach((mod) -> {
            try {
                mod.onRestoreSkills(player);
            } catch (Exception var3) {
                LOG.log(Level.SEVERE, var3.getMessage());
                var3.printStackTrace();
            }

        });
        return false;
    }

    public static synchronized double onStats(Stats stat, Creature character, double value) {
        Iterator var4 = ENGINES_MODS.values().iterator();

        while (var4.hasNext()) {
            AbstractMods mod = (AbstractMods) var4.next();
            if (mod.isStarting() && character != null) {
                try {
                    value += mod.onStats(stat, character, value) - value;
                } catch (Exception var7) {
                    LOG.log(Level.SEVERE, var7.getMessage());
                    var7.printStackTrace();
                }
            }
        }

        return value;
    }
}