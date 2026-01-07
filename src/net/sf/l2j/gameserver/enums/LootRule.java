/**/
package net.sf.l2j.gameserver.enums;

import net.sf.l2j.gameserver.network.SystemMessageId;

public enum LootRule {
    ITEM_LOOTER(SystemMessageId.LOOTING_FINDERS_KEEPERS),
    ITEM_RANDOM(SystemMessageId.LOOTING_RANDOM),
    ITEM_RANDOM_SPOIL(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL),
    ITEM_ORDER(SystemMessageId.LOOTING_BY_TURN),
    ITEM_ORDER_SPOIL(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL);

    public static final LootRule[] VALUES = values();
    private final SystemMessageId _smId;

    LootRule(SystemMessageId smId) {
        this._smId = smId;
    }

    // $FF: synthetic method
    private static LootRule[] $values() {
        return new LootRule[]{ITEM_LOOTER, ITEM_RANDOM, ITEM_RANDOM_SPOIL, ITEM_ORDER, ITEM_ORDER_SPOIL};
    }

    public SystemMessageId getMessageId() {
        return this._smId;
    }
}
