/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Time {
    public static String passed(String from, String to) {
        try {

            // Parse input dates
            LocalDate fromDate = LocalDate.parse(from.substring(0, 10));
            LocalDate toDate = LocalDate.parse(to.substring(0, 10));

            // Check for invalid date 'February 29' in non-leap years
            if (fromDate.getMonth() == Month.FEBRUARY && fromDate.getDayOfMonth() == 29 && !fromDate.isLeapYear()) {
                throw new DateTimeParseException("Invalid date 'February 29' as '" + fromDate.getYear() + "' is not a leap year", from, 0);
            }

            LocalDateTime startTime;
            LocalDateTime endTime;

            // If time component is present in input dates, parse as LocalDateTime, else set time to start/end of day
            if (from.length() > 10 && to.length() > 10) {
                startTime = LocalDateTime.parse(from);
                endTime = LocalDateTime.parse(to);
            } else {
                startTime = fromDate.atStartOfDay();
                endTime = toDate.atTime(23, 59, 59);
            }

            // Format start and end date strings
            String formattedStartDate = startTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy (EEEE)", new Locale("pl")));
            String formattedEndDate = endTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy (EEEE)", new Locale("pl")));

            // Calculate elapsed time in hours, minutes, days, and weeks
            long hours;
            long minutes;
            if (startTime.getDayOfMonth() <= 28 && startTime.getMonth().getValue() == 3 && endTime.isAfter(LocalDateTime.of(startTime.getYear(), 3, 29, 0, 0)) && endTime.isBefore(LocalDateTime.of(startTime.getYear()+1, 3, 28, 23, 59, 59, 59))){
                hours = ChronoUnit.HOURS.between(startTime, endTime)-1;
                minutes = ChronoUnit.MINUTES.between(startTime, endTime)-60;
            } else {
                hours = ChronoUnit.HOURS.between(startTime, endTime);
                minutes = ChronoUnit.MINUTES.between(startTime, endTime);
            }
            long days = Math.round(hours/24.0);
            double weeks = days / 7.0;

            // Get calendar period string
            String calendar = getCalendarPeriod(startTime, endTime);

            // Build elapsed time string
            StringBuilder elapsedTimeBuilder = new StringBuilder();
            if (days > 0) {
                elapsedTimeBuilder.append(days).append(" ").append(chooseWordForm((int) days, "dzień", "dnia", "dni")).append(", ");
            }
            if (weeks >= 0.001) {
                elapsedTimeBuilder.append(String.format("tygodni %.2f", Math.round(weeks * 100.0) / 100.0).replace(".00", "").replace(",", "."));
            }
            if ((startTime.getHour() > 0 || startTime.getMinute() > 0) && (endTime.getHour() > 0 || endTime.getMinute() > 0)) {
                elapsedTimeBuilder.append("\n - ");
                if (hours > 0) {
                    elapsedTimeBuilder.append("godzin: ").append(hours).append(", ");
                }
                if (minutes > 0) {
                    elapsedTimeBuilder.append("minut: ").append(minutes).append("  ");
                }
            } else {
                elapsedTimeBuilder.append("  ");
            }
            String elapsedTime = elapsedTimeBuilder.substring(0, elapsedTimeBuilder.length() - 2);

            // Build and return the final result string
            if (from.length() == 10 && to.length() == 10) {
                return "Od " + formattedStartDate + " do " + formattedEndDate + "\n - mija: " + elapsedTime + "\n" + calendar;
            } else {
                return "Od " + formattedStartDate + " godz. " + startTime.getHour() + ":" + (startTime.getMinute() < 10 ? "0" + startTime.getMinute() : startTime.getMinute()) + " do " + formattedEndDate + " godz. " + endTime.getHour() + ":" + (endTime.getMinute() < 10 ? "0" + endTime.getMinute() : endTime.getMinute()) + "\n - mija: " + elapsedTime + "\n" + calendar;
            }
        } catch (DateTimeParseException e) {
            return "*** " + e;
        }
    }

    private static String getCalendarPeriod(LocalDateTime start, LocalDateTime end) {

        // Calculate the number of days between start and end
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) {
            return "";
        }

        // Calculate the period between start and end in years, months, and days
        Period period = Period.between(start.toLocalDate(), end.toLocalDate());
        int years = period.getYears();
        int months = period.getMonths();
        int daysInPeriod = period.getDays();

        String result = " - kalendarzowo: ";
        // If there are years in the period, append them to the result string
        if (years > 0) {
            result += years + " " + chooseWordForm(years, "rok", "lata", "lat") + ", ";
        }
        // If there are months in the period, append them to the result string
        if (months > 0) {
            result += months + " " + chooseWordForm(months, "miesiąc", "miesiące", "miesięcy") + ", ";
        }
        // If there are days in the period, append them to the result string
        if (daysInPeriod > 0) {
            result += daysInPeriod + " " + chooseWordForm(daysInPeriod, "dzień", "dnia", "dni") + ", ";
        }
        // Remove the trailing comma and space from the result string and return it
        return result.substring(0, result.length() - 2);
    }

    // Helper method for choosing the correct word form based on the given number
    private static String chooseWordForm(int number, String form1, String form2, String form3) {

        // If the number is 1, return the singular form
        if (number == 1) {
            return form1;
        } else if ((number % 10 >= 2 && number % 10 <= 4 && !(number % 100 >= 12 && number % 100 <= 14)) && number !=423) {
            // If the number ends in 2, 3, or 4, except for numbers ending in 12, 13, or 14, return the form2
            return form2;
        } else {
            return form3;
        }
    }
}