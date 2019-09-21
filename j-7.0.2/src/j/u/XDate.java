package j.u;

import java.util.Date;

/**
 * Extend Date
 *
 * @author Fred
 */
//<editor-fold>
public class XDate extends Date {

    public XDate() {
        super();
    }

    public XDate(long mills) {
        super(mills);
    }

    public static XDate getNow() {
        return new XDate();
    }

    public boolean isBetween(Date start, Date end) {
        return this.before(end) && this.after(start);
    }

    public XDate add(TimeS span) {
        return new XDate(this.getTime() + span.getTime());
    }

    public XDate addSeconds(int seconds) {
        return this.add(TimeS.valueOfSeconds(seconds));
    }

    public XDate addMinutes(int minutes) {
        return this.add(TimeS.valueOfMinutes(minutes));
    }

    public XDate addHours(int hours) {
        return this.add(TimeS.valueOfHours(hours));
    }

    public XDate addDays(int days) {
        return this.add(TimeS.valueOfDays(days));
    }

    public TimeS diff(XDate other) {
        return new TimeS(this.getTime() - other.getTime());
    }

    public String toString(String format) {
        return StrU.format(String.format("{0,date,%s}", format), this);
    }

    @Override
    public String toString() {
        return this.toString("yyyy-MM-dd HH:mm:ss");
    }
    
    public static long elapse(Runnable fun){
        long s = System.currentTimeMillis();
        fun.run();
        return System.currentTimeMillis() - s;
    }
    

    public static class TimeS {

        private long m;

        public TimeS(long millis) {
            this.m = millis;
        }

        public TimeS() {
            this(0L);
        }

        public TimeS(Date start, Date end) {
            this(end.getTime() - start.getTime());
        }

        public static TimeS valueOfSeconds(int seconds) {
            return new TimeS(seconds * 1000);
        }

        public static TimeS valueOfMinutes(int minutes) {
            return TimeS.valueOfSeconds(minutes * 60);
        }

        public static TimeS valueOfHours(int hours) {
            return TimeS.valueOfSeconds(hours * 3600);
        }

        public static TimeS valueOfDays(int days) {
            return TimeS.valueOfSeconds(days * 86400);
        }

        public TimeS addSeconds(int seconds) {
            this.m += seconds * 1000;
            return this;
        }

        public TimeS addMinutes(int minutes) {
            return this.addSeconds(minutes * 60);
        }

        public TimeS addHours(int hours) {
            return this.addSeconds(hours * 3600);
        }

        public TimeS addDays(int days) {
            return this.addSeconds(days * 86400);
        }

        public long getTime() {
            return this.m;
        }

        public long getSeconds() {
            return this.m / 1000;
        }

        public long getMinutes() {
            return this.getSeconds() / 60;
        }

        public long getHours() {
            return this.getSeconds() / 3600;
        }

        public long getDays() {
            return this.getSeconds() / 86400;
        }

    }
}
//</editor-fold>
