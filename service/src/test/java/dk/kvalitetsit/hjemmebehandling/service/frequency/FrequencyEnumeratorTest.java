package dk.kvalitetsit.hjemmebehandling.service.frequency;

import dk.kvalitetsit.hjemmebehandling.model.FrequencyModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FrequencyEnumeratorTest {
    private static final Instant FRIDAY_AFTERNOON = Instant.parse("2021-11-26T13:00:00.000Z");
    private static final LocalTime ellevenOClock = LocalTime.parse("11:00");
    private static final LocalTime fourteenOClock = LocalTime.parse("14:00");

    private static final FrequencyModel allWeekAt11 = buildFrequency(List.of(Weekday.MON,Weekday.TUE,Weekday.WED, Weekday.THU, Weekday.FRI,Weekday.SAT,Weekday.SUN),ellevenOClock);
    private static final FrequencyModel tuesdayAndFridayAt14 = buildFrequency(List.of(Weekday.TUE, Weekday.FRI),fourteenOClock);
    private static final FrequencyModel FridayAt14 = buildFrequency(List.of(Weekday.FRI),fourteenOClock);

    @Test
    public void check_winter_and_daylight_saving_time_returns_same_next_hour() {
        ZoneId zoneId = ZoneId.of("Europe/Copenhagen");
        ZoneRules zoneRules = zoneId.getRules();

        ZonedDateTime todayAtEight = ZonedDateTime.ofInstant(Instant.now(), zoneId).with(LocalTime.parse("08:00"));
        ZoneOffsetTransition zoneOffsetTransition = zoneRules.nextTransition(todayAtEight.toInstant());

        Instant winterTime, daylightSavingTime;
        if (zoneRules.isDaylightSavings(todayAtEight.toInstant())) {
            daylightSavingTime = todayAtEight.toInstant();
            winterTime = zoneOffsetTransition.getInstant();
        }
        else {
            winterTime = todayAtEight.toInstant();
            daylightSavingTime = zoneOffsetTransition.getInstant();
        }

        FrequencyModel fm = allWeekAt11;
        //FrequencyEnumerator frequencyEnumerator = new FrequencyEnumerator(fm);

        // Act
        Instant winterTimeNext = new FrequencyEnumerator(fm, daylightSavingTime).getSatisfiedUntil(winterTime);
        Instant daylightSavingTimeNext = new FrequencyEnumerator(fm, winterTime).getSatisfiedUntil(daylightSavingTime);

        // Assert
        assertTrue(zoneRules.isDaylightSavings(daylightSavingTime));
        assertFalse(zoneRules.isDaylightSavings(winterTime));

        assertNotEquals(winterTime.atZone(ZoneOffset.UTC).getHour(), daylightSavingTime.atZone(ZoneOffset.UTC).getHour());
        assertNotEquals(winterTimeNext.atZone(ZoneOffset.UTC).getHour(), daylightSavingTimeNext.atZone(ZoneOffset.UTC).getHour());
        assertEquals(winterTimeNext.atZone(zoneId).getHour(), daylightSavingTimeNext.atZone(zoneId).getHour());
        assertEquals(fm.getTimeOfDay().getHour(), winterTimeNext.atZone(zoneId).getHour());
    }

    private static Stream<Arguments> givenFrequencyTimeToCalculateAndExpectedResult_NextShouldResultInExpectedTime() {
        return Stream.of(
            // Instant is in UTC
            // Recalculating before deadline on a scheduled weekday should advance SatisfiedUntil to the following scheduled weekday, otherwise most recent deadline is expected
            Arguments.of(FridayAt14, Instant.parse("2021-11-19T13:00:00.00Z"), Instant.parse("2021-11-23T10:11:12.124Z"), Instant.parse("2021-11-19T13:00:00.00Z")), // tuesday
            Arguments.of(FridayAt14, Instant.parse("2021-11-19T13:00:00.00Z"), Instant.parse("2021-11-24T10:11:12.124Z"), Instant.parse("2021-11-19T13:00:00.00Z")), // wednesday
            Arguments.of(FridayAt14, Instant.parse("2021-11-19T13:00:00.00Z"), Instant.parse("2021-11-25T10:11:12.124Z"), Instant.parse("2021-11-19T13:00:00.00Z")), // thursday
            Arguments.of(FridayAt14, Instant.parse("2021-11-19T13:00:00.00Z"), Instant.parse("2021-11-26T10:11:12.124Z"), Instant.parse("2021-12-03T13:00:00.00Z")), // friday
            Arguments.of(FridayAt14, Instant.parse("2021-12-03T13:00:00.00Z"), Instant.parse("2021-11-27T10:11:12.124Z"), Instant.parse("2021-12-03T13:00:00.00Z")), // saturday
            Arguments.of(FridayAt14, Instant.parse("2021-12-03T13:00:00.00Z"), Instant.parse("2021-11-28T10:11:12.124Z"), Instant.parse("2021-12-03T13:00:00.00Z")), // sunday
            Arguments.of(FridayAt14, Instant.parse("2021-12-03T13:00:00.00Z"), Instant.parse("2021-11-29T10:11:12.124Z"), Instant.parse("2021-12-03T13:00:00.00Z")), // monday

            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-19T13:00:00.00Z"), Instant.parse("2021-11-23T10:11:12.124Z"), Instant.parse("2021-11-26T13:00:00.00Z")), //tuesday
            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-26T13:00:00.00Z"), Instant.parse("2021-11-24T10:11:12.124Z"), Instant.parse("2021-11-26T13:00:00.00Z")),
            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-26T13:00:00.00Z"), Instant.parse("2021-11-25T10:11:12.124Z"), Instant.parse("2021-11-26T13:00:00.00Z")),
            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-26T13:00:00.00Z"), Instant.parse("2021-11-26T10:11:12.124Z"), Instant.parse("2021-11-30T13:00:00.00Z")),
            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-30T13:00:00.00Z"), Instant.parse("2021-11-27T10:11:12.124Z"), Instant.parse("2021-11-30T13:00:00.00Z")),
            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-30T13:00:00.00Z"), Instant.parse("2021-11-28T10:11:12.124Z"), Instant.parse("2021-11-30T13:00:00.00Z")),
            Arguments.of(tuesdayAndFridayAt14,Instant.parse("2021-11-30T13:00:00.00Z"), Instant.parse("2021-11-29T10:11:12.124Z"), Instant.parse("2021-11-30T13:00:00.00Z")),

            // recalculating on a scheduled weekday before deadline should advance SatisfiedUntil to the following day (Instant is UTC)
            Arguments.of(allWeekAt11, Instant.parse("2021-11-23T10:00:00.00Z"), Instant.parse("2021-11-23T09:11:12.124Z"), Instant.parse("2021-11-24T10:00:00.00Z")),
            Arguments.of(allWeekAt11, Instant.parse("2021-11-24T10:00:00.00Z"), Instant.parse("2021-11-24T09:11:12.124Z"), Instant.parse("2021-11-25T10:00:00.00Z")),
            Arguments.of(allWeekAt11, Instant.parse("2021-11-25T10:00:00.00Z"), Instant.parse("2021-11-25T09:11:12.124Z"), Instant.parse("2021-11-26T10:00:00.00Z")),
            Arguments.of(allWeekAt11, Instant.parse("2021-11-26T10:00:00.00Z"), Instant.parse("2021-11-26T09:11:12.124Z"), Instant.parse("2021-11-27T10:00:00.00Z")),
            Arguments.of(allWeekAt11, Instant.parse("2021-11-27T10:00:00.00Z"), Instant.parse("2021-11-27T09:11:12.124Z"), Instant.parse("2021-11-28T10:00:00.00Z")),
            Arguments.of(allWeekAt11, Instant.parse("2021-11-28T10:00:00.00Z"), Instant.parse("2021-11-28T09:11:12.124Z"), Instant.parse("2021-11-29T10:00:00.00Z")),
            Arguments.of(allWeekAt11, Instant.parse("2021-11-29T10:00:00.00Z"), Instant.parse("2021-11-29T09:11:12.124Z"), Instant.parse("2021-11-30T10:00:00.00Z")),

            //Recalculating at exactly deadline 11 o'clock should trigger blue alarm the next day (eg. not advance SatisfiedUntil)
            Arguments.of(allWeekAt11, Instant.parse("2021-11-23T10:00:00.00Z"), Instant.parse("2021-11-23T10:00:00.00Z"), Instant.parse("2021-11-23T10:00:00.00Z")),

            //Recalculating after 11 should also trigger blue alarm the next day
            Arguments.of(allWeekAt11, Instant.parse("2021-11-23T10:00:00.00Z"), Instant.parse("2021-11-23T16:12:12.124Z"), Instant.parse("2021-11-23T10:00:00.00Z"))
        );
    }
    @ParameterizedTest
    @MethodSource // arguments comes from a method that is name the same as the test
    public void givenFrequencyTimeToCalculateAndExpectedResult_NextShouldResultInExpectedTime(FrequencyModel frequencyModel, Instant currentSatisfiedUntil, Instant timeOfRecalculate, Instant expectedResult){
        // Arrange
        FrequencyEnumerator subject = new FrequencyEnumerator(frequencyModel, currentSatisfiedUntil);

        // Act
        Instant result = subject.getSatisfiedUntil(timeOfRecalculate);
        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    public void next_weekdaysOmitted_interpretedAsNoDeadline() {
        // Arrange
        FrequencyModel frequencyModel = buildFrequency(List.of(), LocalTime.parse("14:00"));

        FrequencyEnumerator subject = new FrequencyEnumerator(frequencyModel, FRIDAY_AFTERNOON);

        // Act
        Instant result = subject.getSatisfiedUntil(FRIDAY_AFTERNOON);

        // Assert
        assertEquals(Instant.MAX, result);
    }


    private static FrequencyModel buildFrequency(List<Weekday> weekdays, LocalTime timeOfDay) {
        FrequencyModel frequencyModel = new FrequencyModel();

        frequencyModel.setWeekdays(weekdays);
        frequencyModel.setTimeOfDay(timeOfDay);

        return frequencyModel;
    }
}