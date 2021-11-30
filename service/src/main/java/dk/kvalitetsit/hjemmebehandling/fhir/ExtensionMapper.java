package dk.kvalitetsit.hjemmebehandling.fhir;

import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;

import java.time.Instant;

import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExtensionMapper {
    public static Extension mapActivitySatisfiedUntil(Instant pointInTime) {
        return buildExtension(Systems.ACTIVITY_SATISFIED_UNTIL, pointInTime.toString());
    }

    public static Extension mapCarePlanSatisfiedUntil(Instant pointInTime) {
        return buildExtension(Systems.CAREPLAN_SATISFIED_UNTIL, pointInTime.toString());
    }

    public static Extension mapExaminationStatus(ExaminationStatus examinationStatus) {
        return buildExtension(Systems.EXAMINATION_STATUS, examinationStatus.toString());
    }

    public static Extension mapTriagingCategory(TriagingCategory triagingCategory) {
        return buildExtension(Systems.TRIAGING_CATEGORY, triagingCategory.toString());
    }

    public static Instant extractActivitySatisfiedUntil(List<Extension> extensions) {
        return extractInstantFromExtensions(extensions, Systems.ACTIVITY_SATISFIED_UNTIL);
    }

    public static Instant extractCarePlanSatisfiedUntil(List<Extension> extensions) {
        return extractInstantFromExtensions(extensions, Systems.CAREPLAN_SATISFIED_UNTIL);
    }

    public static ExaminationStatus extractExaminationStatus(List<Extension> extensions) {
        return extractEnumFromExtensions(extensions, Systems.EXAMINATION_STATUS, ExaminationStatus.class);
    }

    public static TriagingCategory extractTriagingCategoory(List<Extension> extensions) {
        return extractEnumFromExtensions(extensions, Systems.TRIAGING_CATEGORY, TriagingCategory.class);
    }

    private static Extension buildExtension(String url, String value) {
        return new Extension(url, new StringType(value));
    }

    private static <T extends Enum<T>> T extractEnumFromExtensions(List<Extension> extensions, String url, Class<T> type) {
        return extractFromExtensions(extensions, url, v -> Enum.valueOf(type, v.toString()));
    }

    private static Instant extractInstantFromExtensions(List<Extension> extensions, String url) {
        return extractFromExtensions(extensions, url, v -> Instant.parse(v.primitiveValue()));
    }

    private static <T> T extractFromExtensions(List<Extension> extensions, String url, Function<Type, T> extractor) {
        for(Extension extension : extensions) {
            if(extension.getUrl().equals(url)) {
                return extractor.apply(extension.getValue());
            }
        }
        throw new IllegalStateException(String.format("Could not look up url %s among the candidate extensions!", url));
    }
}
