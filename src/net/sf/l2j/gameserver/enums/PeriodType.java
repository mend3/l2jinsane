/**/
package net.sf.l2j.gameserver.enums;

import net.sf.l2j.gameserver.network.SystemMessageId;

public enum PeriodType {
    RECRUITING("Quest Event Initialization", SystemMessageId.PREPARATIONS_PERIOD_BEGUN),
    COMPETITION("Competition (Quest Event)", SystemMessageId.COMPETITION_PERIOD_BEGUN),
    RESULTS("Quest Event Results", SystemMessageId.RESULTS_PERIOD_BEGUN),
    SEAL_VALIDATION("Seal Validation", SystemMessageId.VALIDATION_PERIOD_BEGUN);

    public static final PeriodType[] VALUES = values();
    private final String _name;
    private final SystemMessageId _smId;

    PeriodType(String name, SystemMessageId smId) {
        this._name = name;
        this._smId = smId;
    }

    public String getName() {
        return this._name;
    }

    public SystemMessageId getMessageId() {
        return this._smId;
    }
}
