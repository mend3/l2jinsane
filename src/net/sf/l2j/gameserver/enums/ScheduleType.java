/**/
package net.sf.l2j.gameserver.enums;

public enum ScheduleType {
    HOURLY(10),
    DAILY(6),
    WEEKLY(3),
    MONTHLY_DAY(2),
    MONTHLY_WEEK(2),
    YEARLY_DAY(1),
    YEARLY_WEEK(1);

    private final int _period;

    ScheduleType(int period) {
        this._period = period;
    }

    // $FF: synthetic method
    private static ScheduleType[] $values() {
        return new ScheduleType[]{HOURLY, DAILY, WEEKLY, MONTHLY_DAY, MONTHLY_WEEK, YEARLY_DAY, YEARLY_WEEK};
    }

    public final int getPeriod() {
        return this._period;
    }
}
