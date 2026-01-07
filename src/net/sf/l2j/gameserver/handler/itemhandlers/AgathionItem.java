package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.AgathionData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Agathion;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.Optional;

public class AgathionItem implements IItemHandler {
    private static final String MESSAGE_WAIT_TO_USE = "You must wait before using this item again.";

    private static final String MESSAGE_SYSTEM_DISABLED = "Agathion system is disabled.";

    private static final String MESSAGE_INVALID_AGATHION = "This item is not linked to a valid Agathion.";

    private static final String MESSAGE_PREVIOUS_REMOVED = "Previous Agathion removed.";

    private static final String MESSAGE_NPC_ERROR = "Error finding the Agathion NPC.";

    private static final String MESSAGE_SUCCESS = "New Agathion summoned successfully.";

    private static final String MESSAGE_SPAWN_ERROR = "Error summoning Agathion.";

    private static final long AGATHION_USE_DELAY_MS = 5000L;

    private static final int SPAWN_OFFSET_X = 100;

    private static final int SPAWN_OFFSET_Y = 100;

    private static boolean canUseAgathion(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastUse = player.getLastAgathionUse();
        long cooldownTime = Config.AGATHION_USE_DELAY;
        if (currentTime < lastUse + cooldownTime) {
            player.sendMessage("You must wait before using this item again.");
            return false;
        }
        player.setLastAgathionUse(currentTime);
        return true;
    }

    private static boolean isAgathionSystemEnabled(Player player) {
        if (!Config.ENABLE_AGATHION_SYSTEM) {
            player.sendMessage("Agathion system is disabled.");
            return false;
        }
        return true;
    }

    private static boolean isPlayerReadyToUseAgathion(Player player) {
        if (!player.canUseAgathion()) {
            player.sendMessage("You must wait before using this item again.");
            return false;
        }
        player.setNextAgathionUseDelay(5000L);
        return true;
    }

    private static void processAgathionInvocation(Player player, ItemInstance item) {
        int npcId = AgathionData.getNpcId(item.getItemId());
        if (npcId == -1) {
            player.sendMessage("This item is not linked to a valid Agathion.");
            return;
        }
        removeExistingAgathion(player);
        Optional<NpcTemplate> templateOpt = getNpcTemplate(npcId);
        if (templateOpt.isEmpty()) {
            player.sendMessage("Error finding the Agathion NPC.");
            return;
        }
        spawnNewAgathion(player, item, templateOpt.get());
    }

    private static void removeExistingAgathion(Player player) {
        Agathion currentAgathion = (Agathion) player.getAgathion();
        if (currentAgathion != null) {
            player.despawnAgathion();
            player.sendMessage("Previous Agathion removed.");
        }
    }

    private static Optional<NpcTemplate> getNpcTemplate(int npcId) {
        try {
            NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
            return Optional.ofNullable(template);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static void spawnNewAgathion(Player player, ItemInstance item, NpcTemplate template) {
        try {
            Agathion agathion = createAgathionInstance(player, template);
            configureAgathion(agathion, player);
            spawnAgathion(agathion, player);
            finalizeAgathionSetup(player, item, agathion);
            player.sendMessage("New Agathion summoned successfully.");
        } catch (Exception e) {
            handleSpawnError(player, e);
        }
    }

    private static Agathion createAgathionInstance(Player player, NpcTemplate template) {
        int objectId = IdFactory.getInstance().getNextId();
        return new Agathion(objectId, template, player);
    }

    private static void configureAgathion(Agathion agathion, Player player) {
        agathion.setIsRunning(true);
    }

    private static void spawnAgathion(Agathion agathion, Player player) {
        int spawnX = player.getX() + 20 + 100;
        int spawnY = player.getY() + 100;
        int spawnZ = player.getZ();
        agathion.spawnMe(spawnX, spawnY, spawnZ);
        agathion.getAI().setIntention(IntentionType.FOLLOW, player);
    }

    private static void finalizeAgathionSetup(Player player, ItemInstance item, Agathion agathion) {
        player.setAgathion(agathion);
        if (Config.AGATHION_BUFF)
            agathion.applyBuffToOwner();
        item.setAgathionItem(true);
        player.setNextAgathionUseDelay(Config.AGATHION_USE_DELAY);
    }

    private static void handleSpawnError(Player player, Exception e) {
        e.printStackTrace();
        player.sendMessage("Error summoning Agathion.");
    }

    public void useItem(Playable playable, ItemInstance item, boolean ctrl) {
        Player player = null;
        if (playable instanceof Player) {
            player = (Player) playable;
        } else {
            return;
        }
        if (!canUseAgathion(player))
            return;
        if (!isAgathionSystemEnabled(player))
            return;
        if (!isPlayerReadyToUseAgathion(player))
            return;
        processAgathionInvocation(player, item);
    }
}
