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

    public final int getPeriod() {
        return this._period;
    }
}
