package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRefinePacket extends L2GameClientPacket {
    public static final int GRADE_NONE = 0;

    public static final int GRADE_MID = 1;

    public static final int GRADE_HIGH = 2;

    public static final int GRADE_TOP = 3;

    protected static final int GEMSTONE_D = 2130;

    protected static final int GEMSTONE_C = 2131;

    protected static final int GEMSTONE_B = 2132;

    private static final Map<Integer, LifeStone> _lifeStones = new HashMap<>();

    static {
        _lifeStones.put(8723, new LifeStone(0, 0));
        _lifeStones.put(8724, new LifeStone(0, 1));
        _lifeStones.put(8725, new LifeStone(0, 2));
        _lifeStones.put(8726, new LifeStone(0, 3));
        _lifeStones.put(8727, new LifeStone(0, 4));
        _lifeStones.put(8728, new LifeStone(0, 5));
        _lifeStones.put(8729, new LifeStone(0, 6));
        _lifeStones.put(8730, new LifeStone(0, 7));
        _lifeStones.put(8731, new LifeStone(0, 8));
        _lifeStones.put(8732, new LifeStone(0, 9));
        _lifeStones.put(8733, new LifeStone(1, 0));
        _lifeStones.put(8734, new LifeStone(1, 1));
        _lifeStones.put(8735, new LifeStone(1, 2));
        _lifeStones.put(8736, new LifeStone(1, 3));
        _lifeStones.put(8737, new LifeStone(1, 4));
        _lifeStones.put(8738, new LifeStone(1, 5));
        _lifeStones.put(8739, new LifeStone(1, 6));
        _lifeStones.put(8740, new LifeStone(1, 7));
        _lifeStones.put(8741, new LifeStone(1, 8));
        _lifeStones.put(8742, new LifeStone(1, 9));
        _lifeStones.put(8743, new LifeStone(2, 0));
        _lifeStones.put(8744, new LifeStone(2, 1));
        _lifeStones.put(8745, new LifeStone(2, 2));
        _lifeStones.put(8746, new LifeStone(2, 3));
        _lifeStones.put(8747, new LifeStone(2, 4));
        _lifeStones.put(8748, new LifeStone(2, 5));
        _lifeStones.put(8749, new LifeStone(2, 6));
        _lifeStones.put(8750, new LifeStone(2, 7));
        _lifeStones.put(8751, new LifeStone(2, 8));
        _lifeStones.put(8752, new LifeStone(2, 9));
        _lifeStones.put(8753, new LifeStone(3, 0));
        _lifeStones.put(8754, new LifeStone(3, 1));
        _lifeStones.put(8755, new LifeStone(3, 2));
        _lifeStones.put(8756, new LifeStone(3, 3));
        _lifeStones.put(8757, new LifeStone(3, 4));
        _lifeStones.put(8758, new LifeStone(3, 5));
        _lifeStones.put(8759, new LifeStone(3, 6));
        _lifeStones.put(8760, new LifeStone(3, 7));
        _lifeStones.put(8761, new LifeStone(3, 8));
        _lifeStones.put(8762, new LifeStone(3, 9));
    }

    protected static LifeStone getLifeStone(int itemId) {
        return _lifeStones.get(itemId);
    }

    protected static boolean isValid(Player player, ItemInstance item, ItemInstance refinerItem, ItemInstance gemStones) {
        if (!isValid(player, item, refinerItem))
            return false;
        if (gemStones.getOwnerId() != player.getObjectId())
            return false;
        if (gemStones.getLocation() != ItemInstance.ItemLocation.INVENTORY)
            return false;
        CrystalType grade = item.getItem().getCrystalType();
        if (getGemStoneId(grade) != gemStones.getItemId())
            return false;
        return getGemStoneCount(grade) <= gemStones.getCount();
    }

    protected static boolean isValid(Player player, ItemInstance item, ItemInstance refinerItem) {
        if (!isValid(player, item))
            return false;
        if (refinerItem.getOwnerId() != player.getObjectId())
            return false;
        if (refinerItem.getLocation() != ItemInstance.ItemLocation.INVENTORY)
            return false;
        LifeStone ls = _lifeStones.get(refinerItem.getItemId());
        if (ls == null)
            return false;
        return player.getLevel() >= ls.getPlayerLevel();
    }

    protected static boolean isValid(Player player, ItemInstance item) {
        if (!isValid(player))
            return false;
        if (item.getOwnerId() != player.getObjectId())
            return false;
        if (item.isAugmented())
            return false;
        if (item.isHeroItem())
            return false;
        if (item.isShadowItem())
            return false;
        return !item.getItem().getCrystalType().isLesser(CrystalType.C);
    }

    protected static boolean isValid(Player player) {
        if (player.isInStoreMode()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
            return false;
        }
        if (player.getActiveTradeList() != null) {
            player.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
            return false;
        }
        if (player.isDead()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
            return false;
        }
        if (player.isParalyzed()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
            return false;
        }
        if (player.isFishing()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
            return false;
        }
        if (player.isSitting()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
            return false;
        }
        if (player.isCursedWeaponEquipped())
            return false;
        return !player.isProcessingTransaction();
    }

    protected static int getGemStoneId(CrystalType itemGrade) {
        switch (itemGrade) {
            case C:
            case B:
                return 2130;
            case A:
            case S:
                return 2131;
        }
        return 0;
    }

    protected static int getGemStoneCount(CrystalType itemGrade) {
        switch (itemGrade) {
            case C:
                return 20;
            case B:
                return 30;
            case A:
                return 20;
            case S:
                return 25;
        }
        return 0;
    }

    protected static final class LifeStone {
        private static final int[] LEVELS = new int[]{46, 49, 52, 55, 58, 61, 64, 67, 70, 76};

        private final int _grade;

        private final int _level;

        public LifeStone(int grade, int level) {
            this._grade = grade;
            this._level = level;
        }

        public int getLevel() {
            return this._level;
        }

        public int getGrade() {
            return this._grade;
        }

        public int getPlayerLevel() {
            return LEVELS[this._level];
        }
    }
}
