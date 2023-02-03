package dk.kvalitetsit.hjemmebehandling.service.frequency;

import dk.kvalitetsit.hjemmebehandling.model.FrequencyModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Definitioner:
 *  - SatisfiedUntil: hvornår vises den næste blå alarm (deadline for besvarelse)
 *  - Genberegning: Udregner 'SatisfiedUntil' udfra frekvens, deadline og tidspunkt for genberegningen.
 *                  'SatisfiedUntil' kan både repræsentere "næste deadline" og "seneste deadline". Se regler nedenfor.
 *
 * Hvis "satisfiedUntil" er før dags dato vises en blå alarm.
 * Blå alarmer vises efter klokken 11.
 *
 * Regler til genberegning for kliniker:
 *  - Klikker man "fjern alarm" så laves der en genberegning til næste skemalagte dag.
 *  - Ændres frekvensen så laves der en genberegning til næste skemalagte dag.
 *
 *  Regler til genberegning for patient:
 *  - Indsendes der et spørgeskema på en skemalagt dag inden klokken 11, så laves en genberegning til næste skemalagte dag.
 *  - Indsendes der et spørgeskema på en skemalagt dag efter klokken 11, så laves der ikke en genberegning. Dvs. seneste skemalagte dag er stadig deadline.
 *  - Indsendes der et spørgeskema på en ikke skemalagt dag, så laves der ikke en genberegning. Dvs. seneste skemalagte dag er stadig deadline.
 */
public class FrequencyEnumerator {
    private List<DayOfWeek> weekDays;
    private LocalTime deadlineTime; //fx if you wanna say "Før kl 11", deadlineTime should be 11:00
    private Instant currentSatisfiedUntil;

    public FrequencyEnumerator(FrequencyModel frequency, Instant currensSatisfiedUntil) {
        this.deadlineTime = frequency.getTimeOfDay();
        this.weekDays = frequency.getWeekdays().stream()
            .map(d -> toDayOfWeek(d))
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
        this.currentSatisfiedUntil = currensSatisfiedUntil;
    }

    /**
     * Beregner SatisfiedUntil ud fra et givent tidspunkt
     *
     * Bemærk: dette er beregningen til patient-interaktion, dvs klokkeslæt og dag er afgørende for beregningen.
     * @param pointInTime tidspunkt næste deadlines skal beregnes ud fra
     * @return
     */
    public Instant getSatisfiedUntil(Instant pointInTime) {
        if (weekDays.isEmpty())  {
            return Instant.MAX; // no deadline
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(pointInTime, ZoneId.of("Europe/Copenhagen"));

        if (zonedDateTime.toLocalTime().isBefore(deadlineTime) && weekDays.contains(zonedDateTime.getDayOfWeek())) {
            // adjust to the successive weekday from the frequency model
            var successiveDayOfWeek = getSuccessiveDayOfWeek(zonedDateTime.getDayOfWeek());
            currentSatisfiedUntil = zonedDateTime
                    .with(TemporalAdjusters.next(successiveDayOfWeek))
                    .with(deadlineTime)
                    .toInstant();
        }
        // else: ingen genberegning

        return currentSatisfiedUntil;
    }

    private DayOfWeek getSuccessiveDayOfWeek(DayOfWeek dayOfWeek) {
        return weekDays.stream()
            .filter(weekDay -> weekDay.compareTo(dayOfWeek) > 0)
            .findFirst()
            .orElseGet(() -> weekDays.get(0));
    }

   private DayOfWeek toDayOfWeek(Weekday weekday) {
        switch(weekday) {
            case MON:
                return DayOfWeek.MONDAY;
            case TUE:
                return DayOfWeek.TUESDAY;
            case WED:
                return DayOfWeek.WEDNESDAY;
            case THU:
                return DayOfWeek.THURSDAY;
            case FRI:
                return DayOfWeek.FRIDAY;
            case SAT:
                return DayOfWeek.SATURDAY;
            case SUN:
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException(String.format("Can't map Weekday: %s", weekday));
        }
    }
}
