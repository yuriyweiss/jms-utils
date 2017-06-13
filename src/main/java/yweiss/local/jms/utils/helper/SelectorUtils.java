package yweiss.local.jms.utils.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectorUtils {

    private static final Pattern PATTERN_ALL = Pattern.compile("ALL");
    private static final Pattern PATTERN_BY_IDS = Pattern.compile("BY_IDS");
    private static final Pattern PATTERN_BY_DATE_SHIFT = Pattern.compile("(\\d+)D(\\d\\d)H(\\d\\d)M");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static boolean isSelectAll(String selectorPattern) {
        return PATTERN_ALL.matcher(selectorPattern).matches();
    }

    public static boolean isSelectByIds(String selectorPattern) {
        return PATTERN_BY_IDS.matcher(selectorPattern).matches();
    }

    public static String buildMessagesSelector(String selectorPattern) {
        if (SelectorUtils.isSelectAll(selectorPattern)) {
            return null;
        } else {
            // last selection variant - by date
            Date filterDate = calcFilterDate(selectorPattern);
            String selector = SelectorUtils.buildSelectorByDate(filterDate);
            System.out.println("messages selector: " + selector);
            return selector;
        }
    }

    public static Date buildFilterDate(String selectorPattern) {
        if (SelectorUtils.isSelectAll(selectorPattern)) {
            return null;
        } else {
            return calcFilterDate(selectorPattern);
        }
    }

    private static Date calcFilterDate(String selectorPattern) {
        Matcher matcher = PATTERN_BY_DATE_SHIFT.matcher(selectorPattern);
        if (matcher.matches()) {
            return calcFilterDateByDateShift(matcher.group(1), matcher.group(2), matcher.group(3));
        } else {
            try {
                return sdf.parse(selectorPattern);
            } catch (ParseException e) {
                System.out.println("date pattern parsing failed for: " + selectorPattern);
            }
        }
        throw new RuntimeException("invalid selector pattern: " + selectorPattern);
    }

    private static Date calcFilterDateByDateShift(String daysStr, String hoursStr, String minutesStr) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, -1 * Integer.parseInt(daysStr));
        calendar.add(Calendar.HOUR_OF_DAY, -1 * Integer.parseInt(hoursStr));
        calendar.add(Calendar.MINUTE, -1 * Integer.parseInt(minutesStr));
        return calendar.getTime();
    }

    private static String buildSelectorByDate(Date filterDate) {
        return "JMSTimestamp < " + filterDate.getTime();
    }
}
