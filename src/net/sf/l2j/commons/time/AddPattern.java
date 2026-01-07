package net.sf.l2j.commons.time;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AddPattern implements NextTime {
    private int monthInc = -1;

    private int monthSet = -1;

    private int dayOfMonthInc = -1;

    private int dayOfMonthSet = -1;

    private int hourOfDayInc = -1;

    private int hourOfDaySet = -1;

    private int minuteInc = -1;

    private int minuteSet = -1;

    public AddPattern(String pattern) {
        String[] parts = pattern.split("\\s+");
        if (parts.length == 2) {
            String datepartsstr = parts[0];
            String[] dateparts = datepartsstr.split(":");
            if (dateparts.length == 2)
                if (dateparts[0].startsWith("+")) {
                    this.monthInc = Integer.parseInt(dateparts[0].substring(1));
                } else {
                    this.monthSet = Integer.parseInt(dateparts[0]) - 1;
                }
            String datemodstr = dateparts[dateparts.length - 1];
            if (datemodstr.startsWith("+")) {
                this.dayOfMonthInc = Integer.parseInt(datemodstr.substring(1));
            } else {
                this.dayOfMonthSet = Integer.parseInt(datemodstr);
            }
        }
        String[] timeparts = parts[parts.length - 1].split(":");
        if (timeparts[0].startsWith("+")) {
            this.hourOfDayInc = Integer.parseInt(timeparts[0].substring(1));
        } else {
            this.hourOfDaySet = Integer.parseInt(timeparts[0]);
        }
        if (timeparts[1].startsWith("+")) {
            this.minuteInc = Integer.parseInt(timeparts[1].substring(1));
        } else {
            this.minuteSet = Integer.parseInt(timeparts[1]);
        }
    }

    public long next(long millis) {
        GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault());
        gc.setTimeInMillis(millis);
        if (this.monthInc >= 0)
            gc.add(Calendar.MONTH, this.monthInc);
        if (this.monthSet >= 0)
            gc.set(Calendar.MONTH, this.monthSet);
        if (this.dayOfMonthInc >= 0)
            gc.add(Calendar.DATE, this.dayOfMonthInc);
        if (this.dayOfMonthSet >= 0)
            gc.set(Calendar.DATE, this.dayOfMonthSet);
        if (this.hourOfDayInc >= 0)
            gc.add(Calendar.HOUR_OF_DAY, this.hourOfDayInc);
        if (this.hourOfDaySet >= 0)
            gc.set(Calendar.HOUR_OF_DAY, this.hourOfDaySet);
        if (this.minuteInc >= 0)
            gc.add(Calendar.MINUTE, this.minuteInc);
        if (this.minuteSet >= 0)
            gc.set(Calendar.MINUTE, this.minuteSet);
        return gc.getTimeInMillis();
    }
}
