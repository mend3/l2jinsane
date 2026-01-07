package net.sf.l2j.gameserver.data;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Agathion;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AgathionData {
    private static final Logger LOGGER = Logger.getLogger(AgathionData.class.getName());

    private static final Path CONFIG_FILE = Paths.get("config/agathions.properties");

    private static final long DEFAULT_DURATION = 300000L;

    private static final int SPAWN_OFFSET = 100;

    private static final Map<Integer, Integer> AGATHION_MAP = new ConcurrentHashMap<>();

    private static final Map<Integer, int[]> AGATHION_BUFFS = new ConcurrentHashMap<>();

    private static final Map<Integer, Long> AGATHION_TIMERS = new ConcurrentHashMap<>();

    private AgathionData() {
    }

    public static void spawnAgathion(Player player, int npcId) {
        if (player == null || npcId <= 0) {
            LOGGER.warning("Invalid parameters for spawnAgathion: player=" + player + ", npcId=" + npcId);
            return;
        }
        if (player.getAgathion() != null)
            player.despawnAgathion();
        NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
        if (template == null) {
            LOGGER.warning("Template not found for NPC ID: " + npcId);
            return;
        }
        try {
            int objectId = IdFactory.getInstance().getNextId();
            Agathion agathion = new Agathion(objectId, template, player);
            agathion.setTitle(player.getName());
            agathion.setIsRunning(true);
            agathion.spawnMe(player
                    .getX() + 20 + 100, player
                    .getY() + 100, player
                    .getZ());
            agathion.getAI().setIntention(IntentionType.FOLLOW, player);
            if (Config.AGATHION_BUFF)
                agathion.applyBuffToOwner();
            player.setAgathion(agathion);
            LOGGER.info("Agathion spawned successfully for player: " + player.getName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error spawning Agathion for player: " + player.getName(), e);
            player.sendMessage("Error summoning Agathion.");
        }
    }

    private static void processConfigEntry(String key, String value) {
        String[] valueParts = value.split(";");
        int itemId = Integer.parseInt(key);
        int npcId = Integer.parseInt(valueParts[0]);
        AGATHION_MAP.put(Integer.valueOf(itemId), Integer.valueOf(npcId));
        if (valueParts.length > 1 && !valueParts[1].isEmpty()) {
            String[] buffData = valueParts[1].split(",");
            if (buffData.length == 2) {
                int skillId = Integer.parseInt(buffData[0]);
                int skillLevel = Integer.parseInt(buffData[1]);
                AGATHION_BUFFS.put(Integer.valueOf(npcId), new int[]{skillId, skillLevel});
            }
        }
        if (valueParts.length > 2 && !valueParts[2].isEmpty()) {
            long duration = Long.parseLong(valueParts[2]);
            AGATHION_TIMERS.put(Integer.valueOf(npcId), Long.valueOf(duration));
        }
    }

    public static int getNpcId(int itemId) {
        return AGATHION_MAP.getOrDefault(Integer.valueOf(itemId), Integer.valueOf(-1));
    }

    public static Map<Integer, Integer> getAgathions() {
        return Map.copyOf(AGATHION_MAP);
    }

    public static int[] getBuffForNpc(int npcId) {
        int[] buff = AGATHION_BUFFS.get(Integer.valueOf(npcId));
        return (buff != null) ? buff.clone() : null;
    }

    public static long getAgathionDuration(int npcId) {
        return AGATHION_TIMERS.getOrDefault(Integer.valueOf(npcId), Long.valueOf(300000L));
    }

    public static AgathionData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        AGATHION_MAP.clear();
        AGATHION_BUFFS.clear();
        AGATHION_TIMERS.clear();
        if (!Files.exists(CONFIG_FILE)) {
            LOGGER.warning("Agathions configuration file not found: " + CONFIG_FILE);
            return;
        }
        try {
            InputStream inputStream = Files.newInputStream(CONFIG_FILE);
            try {
                Properties properties = new Properties();
                properties.load(inputStream);
                properties.stringPropertyNames().forEach(key -> {
                    try {
                        processConfigEntry(key, properties.getProperty(key));
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid number format in config for key: " + key);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error processing config entry: " + key, e);
                    }
                });
                LOGGER.info("Loaded " + AGATHION_MAP.size() + " Agathion configurations");
                if (inputStream != null)
                    inputStream.close();
            } catch (Throwable throwable) {
                if (inputStream != null)
                    try {
                        inputStream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading Agathions configuration", e);
        }
    }

    private static class SingletonHolder {
        private static final AgathionData INSTANCE = new AgathionData();
    }
}
