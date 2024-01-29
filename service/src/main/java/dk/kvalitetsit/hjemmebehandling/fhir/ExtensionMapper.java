package dk.kvalitetsit.hjemmebehandling.fhir;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.model.PhoneHourModel;
import dk.kvalitetsit.hjemmebehandling.model.ThresholdModel;
import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.hl7.fhir.r4.model.*;

import java.sql.Date;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtensionMapper {
    protected static final LocalDateTime MAX_SATISFIED_UNTIL_DATE = LocalDateTime.of(9999, Month.DECEMBER, 31, 11, 00);

    public static Extension mapActivitySatisfiedUntil(Instant pointInTime) {
        return buildDateTimeExtension(Systems.ACTIVITY_SATISFIED_UNTIL, pointInTime);
    }

    public static Extension mapCarePlanSatisfiedUntil(Instant pointInTime) {
        return buildDateTimeExtension(Systems.CAREPLAN_SATISFIED_UNTIL, pointInTime);
    }

    public static Extension mapExaminationStatus(ExaminationStatus examinationStatus) {
        return buildStringExtension(Systems.EXAMINATION_STATUS, examinationStatus.toString());
    }

    public static Extension mapOrganizationId(String organizationId) {
        return buildReferenceExtension(Systems.ORGANIZATION, organizationId);
    }

    public static Extension mapQuestionAbbreviation(String abbreviation) {
        return buildStringExtension(Systems.QUESTION_ABBREVIATION, abbreviation);
    }

    public static Extension mapBlob(String blob) {
        return buildStringExtension(Systems.ORGANISATION_BLOB, blob);
    }

    public static Extension mapQuestionnaireSummaryBlob(String blob) {
        return buildStringExtension(Systems.QUESTIONNAIRE_SUMMARY_BLOB, blob);
    }


    public static Extension mapPhoneHours(PhoneHourModel phoneHours) {
        List<Extension> extensions = new ArrayList<>();

        for(var day : phoneHours.getWeekdays()) {
            extensions.add(buildStringExtension(Systems.PHONE_HOURS_WEEKDAY, day.toString()));
        }
        extensions.add(buildTimeExtension(Systems.PHONE_HOURS_FROM, phoneHours.getFrom()));
        extensions.add(buildTimeExtension(Systems.PHONE_HOURS_TO, phoneHours.getTo()));

        return buildCompositeExtension(Systems.PHONE_HOURS, extensions);
    }

    public static List<Extension> mapThresholds(List<ThresholdModel> thresholds) {
        return thresholds.stream().map(t -> mapThreshold(t)).collect(Collectors.toList());
    }

    public static Extension mapThreshold(ThresholdModel threshold) {
        Extension linkIdExtension = buildStringExtension(Systems.THRESHOLD_QUESTIONNAIRE_ITEM_LINKID, threshold.getQuestionnaireItemLinkId());
        Extension thresholdTypeExtension = buildStringExtension(Systems.THRESHOLD_TYPE, threshold.getType().toString());
        Extension thresholdValueExtension = null;
        if(threshold.getValueBoolean() != null) {
            thresholdValueExtension = buildBooleanExtension(Systems.THRESHOLD_VALUE_BOOLEAN, threshold.getValueBoolean());
        }
        else if(threshold.getValueQuantityLow() != null || threshold.getValueQuantityHigh() != null) {
            thresholdValueExtension = buildRangeExtension(Systems.THRESHOLD_VALUE_RANGE, threshold.getValueQuantityLow(), threshold.getValueQuantityHigh());
        }

        return buildCompositeExtension(Systems.THRESHOLD, List.of(linkIdExtension, thresholdTypeExtension, thresholdValueExtension));
    }

    public static Extension mapTriagingCategory(TriagingCategory triagingCategory) {
        return buildStringExtension(Systems.TRIAGING_CATEGORY, triagingCategory.toString());
    }

    private static String extractStringFromExtensions(List<Extension> extensions, String url) {
        return extractFromOptionalExtensions(extensions, url, v -> ((StringType) v).getValue());
    }

    public static Instant extractActivitySatisfiedUntil(List<Extension> extensions) {
        return extractInstantFromExtensions(extensions, Systems.ACTIVITY_SATISFIED_UNTIL);
    }

    public static String extractQuestionAbbreviation(List<Extension> extensions) {
        return extractStringFromExtensions(extensions, Systems.QUESTION_ABBREVIATION);
    }

    public static Instant extractCarePlanSatisfiedUntil(List<Extension> extensions) {
        return extractInstantFromExtensions(extensions, Systems.CAREPLAN_SATISFIED_UNTIL);
    }

    public static ExaminationStatus extractExaminationStatus(List<Extension> extensions) {
        return extractEnumFromExtensions(extensions, Systems.EXAMINATION_STATUS, ExaminationStatus.class);
    }

    public static String extractOrganizationId(List<Extension> extensions) {
        return extractReferenceFromExtensions(extensions, Systems.ORGANIZATION);
    }

    public static Optional<String> tryExtractOrganizationId(List<Extension> extensions) {
        return tryExtractReferenceFromExtensions(extensions, Systems.ORGANIZATION);
    }

    public static List<PhoneHourModel> extractPhoneHours(List<Extension> extensions) {
        return extensions.stream().map(e -> extractPhoneHours(e)).collect(Collectors.toList());
    }

    private static <T> T extractFromOptionalExtensions(List<Extension> extensions, String url, Function<Type, T> extractor) {
        return tryExtractFromExtensions(extensions, url, extractor).orElse(null);
    }


    public static PhoneHourModel extractPhoneHours(Extension extension) {
        PhoneHourModel phoneHourModel = new PhoneHourModel();

        phoneHourModel.setWeekdays(extension.getExtensionsByUrl(Systems.PHONE_HOURS_WEEKDAY)
                .stream()
                .map(e -> Enum.valueOf(Weekday.class, e.getValue().toString()))
                .collect(Collectors.toList()));

        phoneHourModel.setFrom(LocalTime.parse(extension.getExtensionByUrl(Systems.PHONE_HOURS_FROM).getValue().primitiveValue()));
        phoneHourModel.setTo(LocalTime.parse(extension.getExtensionByUrl(Systems.PHONE_HOURS_TO).getValue().primitiveValue()));

        return phoneHourModel;
    }

    public static List<ThresholdModel> extractThresholds(List<Extension> extensions) {
        return extensions.stream().map(e -> extractThreshold(e)).collect(Collectors.toList());
    }

    public static ThresholdModel extractThreshold(Extension extension) {
        ThresholdModel thresholdModel = new ThresholdModel();

        thresholdModel.setQuestionnaireItemLinkId( extension.getExtensionString(Systems.THRESHOLD_QUESTIONNAIRE_ITEM_LINKID) );
        if ( extension.hasExtension(Systems.THRESHOLD_CONCEPT_CODE) && extension.getExtensionByUrl(Systems.THRESHOLD_CONCEPT_CODE).hasValue()) {
            thresholdModel.setConceptCode(extension.getExtensionString(Systems.THRESHOLD_CONCEPT_CODE));
        }
        thresholdModel.setType( Enum.valueOf(ThresholdType.class, extension.getExtensionString(Systems.THRESHOLD_TYPE)) );
        if ( extension.hasExtension(Systems.THRESHOLD_VALUE_BOOLEAN) ) {
            BooleanType valueBoolean = (BooleanType) extension.getExtensionByUrl(Systems.THRESHOLD_VALUE_BOOLEAN).getValue();
            thresholdModel.setValueBoolean( valueBoolean.booleanValue() );
        }
        if ( extension.hasExtension(Systems.THRESHOLD_VALUE_RANGE) ) {
            Range valueRange = (Range) extension.getExtensionByUrl(Systems.THRESHOLD_VALUE_RANGE).getValue();
            if (valueRange.hasLow()) {
                thresholdModel.setValueQuantityLow( valueRange.getLow().getValue().doubleValue() );
            }
            if (valueRange.hasHigh()) {
                thresholdModel.setValueQuantityHigh( valueRange.getHigh().getValue().doubleValue() );
            }
        }
        if ( extension.hasExtension(Systems.THRESHOLD_VALUE_OPTION) ) {
            StringType valueOption = (StringType) extension.getExtensionByUrl(Systems.THRESHOLD_VALUE_OPTION).getValue();
            thresholdModel.setValueOption( valueOption.getValue() );
        }

        return thresholdModel;
    }

    public static TriagingCategory extractTriagingCategoory(List<Extension> extensions) {
        return extractEnumFromExtensions(extensions, Systems.TRIAGING_CATEGORY, TriagingCategory.class);
    }

    private static Extension buildBooleanExtension(String url, boolean value) {
        return new Extension(url, new BooleanType(value));
    }

    private static Extension buildCompositeExtension(String url, List<Extension> extensions) {
        Extension extension = new Extension(url);

        for(Extension e : extensions) {
            extension.addExtension(e);
        }

        return extension;
    }

    private static Extension buildDateTimeExtension(String url, Instant value) {
        // the need for this is so stupid..
        if (value.equals(Instant.MAX)) {
            // men hapi-fhir clientens json parser af dato har fixed dato-format med 4 digits år, så den knækker hvis man sender år 'en million millard'
            value = MAX_SATISFIED_UNTIL_DATE.toInstant(ZoneId.of("Europe/Copenhagen").getRules().getOffset(Instant.now()));
        }
        return new Extension(url, new DateTimeType(Date.from(value), TemporalPrecisionEnum.MILLI, TimeZone.getTimeZone("UTC")));
    }

    private static Extension buildRangeExtension(String url, Double low, Double high) {
        Range range = new Range();

        if(low != null) {
            range.setLow(new Quantity(low));
        }
        if(high != null) {
            range.setHigh(new Quantity(high));
        }

        return new Extension(url, range);
    }

    private static Extension buildReferenceExtension(String url, String value) {
        return new Extension(url, new Reference(value));
    }

    private static Extension buildStringExtension(String url, String value) {
        return new Extension(url, new StringType(value));
    }

    private static Extension buildTimeExtension(String url, LocalTime value) {
        return new Extension(url, new TimeType(value.toString()));
    }

    private static <T extends Enum<T>> T extractEnumFromExtensions(List<Extension> extensions, String url, Class<T> type) {
        return extractFromExtensions(extensions, url, v -> Enum.valueOf(type, v.toString()));
    }

    private static Instant extractInstantFromExtensions(List<Extension> extensions, String url) {
        return extractFromExtensions(extensions, url, v -> {
            Instant result = ((DateTimeType) v).getValue().toInstant();

            if (result.equals(MAX_SATISFIED_UNTIL_DATE.toInstant(ZoneId.of("Europe/Copenhagen").getRules().getOffset(Instant.now())))) {
                return Instant.MAX;
            }
            return result;
        });
    }

    private static String extractReferenceFromExtensions(List<Extension> extensions, String url) {
        return extractFromExtensions(extensions, url, v -> ((Reference) v).getReference());
    }

    private static Optional<String> tryExtractReferenceFromExtensions(List<Extension> extensions, String url) {
        return tryExtractFromExtensions(extensions, url, v -> ((Reference) v).getReference());
    }

    private static <T> T extractFromExtensions(List<Extension> extensions, String url, Function<Type, T> extractor) {
        return tryExtractFromExtensions(extensions, url, extractor)
                .orElseThrow(() -> new IllegalStateException(String.format("Could not look up url %s among the candidate extensions!", url)));
    }

    private static <T> Optional<T> tryExtractFromExtensions(List<Extension> extensions, String url, Function<Type, T> extractor) {
        for(Extension extension : extensions) {
            if(extension.getUrl().equals(url)) {
                return Optional.of(extractor.apply(extension.getValue()));
            }
        }
        return Optional.empty();
    }
}