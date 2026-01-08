package net.sf.l2j.gameserver.scripting;

import net.sf.l2j.gameserver.enums.ScheduleType;

import java.util.Calendar;

public abstract class ScheduledQuest extends Quest {
    private ScheduleType _type;

    private Calendar _start;

    private Calendar _end;

    private boolean _started;

    public ScheduledQuest(int questId, String descr) {
        super(questId, descr);
    }

    private static int getDayOfWeek(String day) throws Exception {
        return switch (day) {
            case "MON" -> 2;
            case "TUE" -> 3;
            case "WED" -> 4;
            case "THU" -> 5;
            case "FRI" -> 6;
            case "SAT" -> 7;
            case "SUN" -> 1;
            default -> throw new Exception();
        };
    }

    public final boolean isStarted() {
        return this._started;
    }

    public final boolean setSchedule(String type, String start, String end) {
        try {
            this._type = Enum.valueOf(ScheduleType.class, type);
            this._start = parseTimeStamp(start);
            this._end = parseTimeStamp(end);
            this._started = false;
            long st = this._start.getTimeInMillis();
            long now = System.currentTimeMillis();
            if (this._end == null || this._end.getTimeInMillis() == st) {
                this._end = null;
                if (st < now)
                    this._start.add(this._type.getPeriod(), 1);
            } else {
                long en = this._end.getTimeInMillis();
                if (st < en) {
                    if (en < now) {
                        this._start.add(this._type.getPeriod(), 1);
                    } else if (st < now) {
                        this._started = true;
                    } else {
                        this._end.add(this._type.getPeriod(), -1);
                    }
                } else if (st < now) {
                    this._end.add(this._type.getPeriod(), 1);
                    this._started = true;
                } else if (now < en) {
                    this._start.add(this._type.getPeriod(), -1);
                    this._started = true;
                }
            }
            return init();
        } catch (Exception e) {
            LOGGER.error("Error loading schedule data for {}.", e, toString());
            this._type = null;
            this._start = null;
            this._end = null;
            this._started = false;
            return false;
        }
    }

    private Calendar parseTimeStamp(String value) throws Exception {
        String[] timeStamp, params, date;
        if (value == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        switch (this._type) {
            case HOURLY:
                timeStamp = value.split(":");
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            case DAILY:
                timeStamp = value.split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[2]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            case WEEKLY:
                params = value.split(" ");
                timeStamp = params[1].split(":");
                calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(params[0]));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[2]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            case MONTHLY_DAY:
                params = value.split(" ");
                timeStamp = params[1].split(":");
                calendar.set(Calendar.DATE, Integer.parseInt(params[0]));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[2]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            case MONTHLY_WEEK:
                params = value.split(" ");
                date = params[0].split("-");
                timeStamp = params[1].split(":");
                calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(date[0]));
                calendar.set(Calendar.WEEK_OF_MONTH, Integer.parseInt(date[1]));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[2]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            case YEARLY_DAY:
                params = value.split(" ");
                date = params[0].split("-");
                timeStamp = params[1].split(":");
                calendar.set(Calendar.DATE, Integer.parseInt(date[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[2]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            case YEARLY_WEEK:
                params = value.split(" ");
                date = params[0].split("-");
                timeStamp = params[1].split(":");
                calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(date[0]));
                calendar.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(date[1]));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStamp[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeStamp[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(timeStamp[2]));
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
        }
        return null;
    }

    public final long getTimeNext() {
        if (this._type == null)
            return 0L;
        return this._started ? this._end.getTimeInMillis() : this._start.getTimeInMillis();
    }

    public final void notifyAndSchedule() {
        if (this._type == null)
            return;
        if (this._end == null) {
            try {
                onStart();
            } catch (Exception e) {
                LOGGER.error("Error starting {}.", e, toString());
            }
            this._start.add(this._type.getPeriod(), 1);
            print(this._start);
            return;
        }
        if (this._started) {
            try {
                onEnd();
                this._started = false;
            } catch (Exception e) {
                LOGGER.error("Error ending {}.", e, toString());
            }
            this._start.add(this._type.getPeriod(), 1);
            print(this._start);
        } else {
            try {
                onStart();
                this._started = true;
            } catch (Exception e) {
                LOGGER.error("Error starting {}.", e, toString());
            }
            this._end.add(this._type.getPeriod(), 1);
            print(this._end);
        }
    }

    protected boolean init() {
        if (this._started)
            onStart();
        return true;
    }

    protected abstract void onStart();

    protected abstract void onEnd();

    private void print(Calendar c) {
        LOGGER.debug("{}: {} = {}.", toString(), (c == this._start) ? "Next start" : "Next end", String.format("%d.%d.%d %d:%02d:%02d", c.get(Calendar.DATE), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));
    }
}
