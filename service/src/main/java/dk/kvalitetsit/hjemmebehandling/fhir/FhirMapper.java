package dk.kvalitetsit.hjemmebehandling.fhir;

import dk.kvalitetsit.hjemmebehandling.constants.*;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import dk.kvalitetsit.hjemmebehandling.util.DateProvider;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FhirMapper {
    public CarePlan mapCarePlanModel(CarePlanModel carePlanModel) {
        CarePlan carePlan = new CarePlan();

        mapBaseAttributesToFhir(carePlan, carePlanModel);

        carePlan.setTitle(carePlanModel.getTitle());
        carePlan.setStatus(Enum.valueOf(CarePlan.CarePlanStatus.class, carePlanModel.getStatus().toString()));
        carePlan.setCreated(Date.from(carePlanModel.getCreated()));
        if(carePlanModel.getStartDate() != null) {
            carePlan.setPeriod(new Period());
            carePlan.getPeriod().setStart(Date.from(carePlanModel.getStartDate()));
        }
        carePlan.addExtension(ExtensionMapper.mapCarePlanSatisfiedUntil(carePlanModel.getSatisfiedUntil()));

        // Set the subject
        if(carePlanModel.getPatient().getId() != null) {
            carePlan.setSubject(new Reference(carePlanModel.getPatient().getId().toString()));
        }

        // Map questionnaires to activities
        if(carePlanModel.getQuestionnaires() != null) {
            carePlan.setActivity(carePlanModel.getQuestionnaires()
                    .stream()
                    .map(q -> buildCarePlanActivity(q))
                    .collect(Collectors.toList()));
        }

        if(carePlanModel.getPlanDefinitions() != null) {
            // Add references to planDefinitions
            carePlan.setInstantiatesCanonical(carePlanModel.getPlanDefinitions()
                    .stream()
                    .map(pd -> new CanonicalType(pd.getId().toString()))
                    .collect(Collectors.toList()));
        }

        return carePlan;
    }

    public CarePlanModel mapCarePlan(CarePlan carePlan, FhirLookupResult lookupResult) {
        CarePlanModel carePlanModel = new CarePlanModel();

        mapBaseAttributesToModel(carePlanModel, carePlan);

        carePlanModel.setTitle(carePlan.getTitle());
        carePlanModel.setStatus(Enum.valueOf(CarePlanStatus.class, carePlan.getStatus().toString()));
        carePlanModel.setCreated(carePlan.getCreated().toInstant());
        carePlanModel.setStartDate(carePlan.getPeriod().getStart().toInstant());
        if(carePlan.getPeriod().getEnd() != null) {
            carePlanModel.setEndDate(carePlan.getPeriod().getEnd().toInstant());
        }

        String patientId = carePlan.getSubject().getReference();
        Patient patient = lookupResult.getPatient(patientId).orElseThrow(() -> new IllegalStateException(String.format("Could not look up Patient for CarePlan %s!", carePlanModel.getId())));
        carePlanModel.setPatient(mapPatient(patient));

        carePlanModel.setQuestionnaires(new ArrayList<>());
        for(var activity : carePlan.getActivity()) {
            String questionnaireId = activity.getDetail().getInstantiatesCanonical().get(0).getValue();
            var questionnaire = lookupResult
                    .getQuestionnaire(questionnaireId)
                    .orElseThrow(() -> new IllegalStateException(String.format("Could not look up Questionnaire for CarePlan %s!", carePlanModel.getId())));

            var questionnaireModel = mapQuestionnaire(questionnaire);
            var frequencyModel = mapTiming(activity.getDetail().getScheduledTiming());

            var wrapper = new QuestionnaireWrapperModel();
            wrapper.setQuestionnaire(questionnaireModel);
            wrapper.setFrequency(frequencyModel);
            List<ThresholdModel> thresholds = getThresholds(activity.getDetail());
            wrapper.setThresholds(thresholds);
            wrapper.setSatisfiedUntil(ExtensionMapper.extractActivitySatisfiedUntil(activity.getDetail().getExtension()));

            carePlanModel.getQuestionnaires().add(wrapper);
        }

        carePlanModel.setPlanDefinitions(new ArrayList<>());
        for(var ic : carePlan.getInstantiatesCanonical()) {
            var planDefinition = lookupResult
                    .getPlanDefinition(ic.getValue())
                    .orElseThrow(() -> new IllegalStateException(String.format("Could not look up PlanDefinition for CarePlan %s!", carePlanModel.getId())));
            carePlanModel.getPlanDefinitions().add(mapPlanDefinition(planDefinition, lookupResult));
        }

        carePlanModel.setSatisfiedUntil(ExtensionMapper.extractCarePlanSatisfiedUntil(carePlan.getExtension()));

        String organizationId = ExtensionMapper.extractOrganizationId(carePlan.getExtension());
        Organization organization = lookupResult.getOrganization(organizationId)
                .orElseThrow(() -> new IllegalStateException(String.format("Organization with id %s was not present when trying to map careplan %s!", organizationId, carePlan.getId())));
        carePlanModel.setDepartmentName(organization.getName());

        return carePlanModel;
    }

    public PatientModel mapPatient(Patient patient) {
        PatientModel patientModel = new PatientModel();

        patientModel.setId(extractId(patient));
        patientModel.setCpr(extractCpr(patient));
        patientModel.setFamilyName(extractFamilyName(patient));
        patientModel.setGivenName(extractGivenNames(patient));
        patientModel.setPatientContactDetails(extractPatientContactDetails(patient));

        return patientModel;
    }

    public PlanDefinitionModel mapPlanDefinition(PlanDefinition planDefinition, FhirLookupResult lookupResult) {
        PlanDefinitionModel planDefinitionModel = new PlanDefinitionModel();

        mapBaseAttributesToModel(planDefinitionModel, planDefinition);

        planDefinitionModel.setName(planDefinition.getName());
        planDefinitionModel.setTitle(planDefinition.getTitle());

        // Map actions to questionnaires, along with their frequencies and thresholds
        planDefinitionModel.setQuestionnaires(planDefinition.getAction().stream().map(a -> mapPlanDefinitionAction(a, lookupResult)).collect(Collectors.toList()));

        return planDefinitionModel;
    }

    public QuestionnaireModel mapQuestionnaire(Questionnaire questionnaire) {
        QuestionnaireModel questionnaireModel = new QuestionnaireModel();

        mapBaseAttributesToModel(questionnaireModel, questionnaire);

        questionnaireModel.setTitle(questionnaire.getTitle());
        questionnaireModel.setStatus(questionnaire.getStatus().getDisplay());
        questionnaireModel.setQuestions(questionnaire.getItem().stream().map(item -> mapQuestionnaireItem(item)).collect(Collectors.toList()));

        return questionnaireModel;
    }

    public QuestionnaireResponse mapQuestionnaireResponseModel(QuestionnaireResponseModel questionnaireResponseModel) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

        mapBaseAttributesToFhir(questionnaireResponse, questionnaireResponseModel);

        questionnaireResponse.setQuestionnaire(questionnaireResponseModel.getQuestionnaireId().toString());
        for(var questionAnswerPair : questionnaireResponseModel.getQuestionAnswerPairs()) {
            questionnaireResponse.getItem().add(getQuestionnaireResponseItem(questionAnswerPair.getAnswer()));
        }
        questionnaireResponse.setBasedOn(List.of(new Reference(questionnaireResponseModel.getCarePlanId().toString())));
        questionnaireResponse.setAuthor(new Reference(questionnaireResponseModel.getAuthorId().toString()));
        questionnaireResponse.setSource(new Reference(questionnaireResponseModel.getSourceId().toString()));
        questionnaireResponse.setAuthored(Date.from(questionnaireResponseModel.getAnswered()));
        questionnaireResponse.getExtension().add(ExtensionMapper.mapExaminationStatus(questionnaireResponseModel.getExaminationStatus()));
        questionnaireResponse.getExtension().add(ExtensionMapper.mapTriagingCategory(questionnaireResponseModel.getTriagingCategory()));
        questionnaireResponse.setSubject(new Reference(questionnaireResponseModel.getPatient().getId().toString()));

        return questionnaireResponse;
    }

    public QuestionnaireResponseModel mapQuestionnaireResponse(QuestionnaireResponse questionnaireResponse, FhirLookupResult lookupResult) {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();

        mapBaseAttributesToModel(questionnaireResponseModel, questionnaireResponse);

        String questionnaireId = questionnaireResponse.getQuestionnaire();
        Questionnaire questionnaire = lookupResult.getQuestionnaire(questionnaireId)
                .orElseThrow(() -> new IllegalStateException(String.format("No Questionnaire found with id %s!", questionnaireId)));
        questionnaireResponseModel.setQuestionnaireName(questionnaire.getTitle());
        questionnaireResponseModel.setQuestionnaireId(extractId(questionnaire));

        if(questionnaireResponse.getBasedOn() == null || questionnaireResponse.getBasedOn().size() != 1) {
            throw new IllegalStateException(String.format("Error mapping QuestionnaireResponse %s: Expected exactly one BasedOn-attribute!", questionnaireResponseModel.getId().toString()));
        }
        questionnaireResponseModel.setCarePlanId(new QualifiedId(questionnaireResponse.getBasedOn().get(0).getReference()));

        if(questionnaireResponse.getAuthor() == null) {
            throw new IllegalStateException(String.format("Error mapping QuestionnaireResponse %s: No Author-attribute present!!", questionnaireResponseModel.getId().toString()));
        }
        questionnaireResponseModel.setAuthorId(new QualifiedId(questionnaireResponse.getAuthor().getReference()));

        if(questionnaireResponse.getSource() == null) {
            throw new IllegalStateException(String.format("Error mapping QuestionnaireResponse %s: No Source-attribute present!!", questionnaireResponseModel.getId().toString()));
        }
        questionnaireResponseModel.setSourceId(new QualifiedId(questionnaireResponse.getSource().getReference()));

        // Populate questionAnswerMap
        List<QuestionAnswerPairModel> answers = new ArrayList<>();

        for(var item : questionnaireResponse.getItem()) {
            QuestionModel question = getQuestion(questionnaire, item.getLinkId());
            AnswerModel answer = getAnswer(item);
            answers.add(new QuestionAnswerPairModel(question, answer));
        }

        questionnaireResponseModel.setQuestionAnswerPairs(answers);
        questionnaireResponseModel.setAnswered(questionnaireResponse.getAuthored().toInstant());
        questionnaireResponseModel.setExaminationStatus(ExtensionMapper.extractExaminationStatus(questionnaireResponse.getExtension()));
        questionnaireResponseModel.setTriagingCategory(ExtensionMapper.extractTriagingCategoory(questionnaireResponse.getExtension()));

        String patientId = questionnaireResponse.getSubject().getReference();
        Patient patient = lookupResult.getPatient(patientId)
                .orElseThrow(() -> new IllegalStateException(String.format("No Patient found with id %s!", patientId)));
        questionnaireResponseModel.setPatient(mapPatient(patient));

        String carePlanId = questionnaireResponse.getBasedOnFirstRep().getReference();
        CarePlan carePlan = lookupResult.getCarePlan(carePlanId)
                .orElseThrow(() -> new IllegalStateException(String.format("No CarePlan found with id %s!", carePlanId)));
        String planDefinitionId = carePlan.getInstantiatesCanonical().get(0).getValue();
        PlanDefinition planDefinition = lookupResult.getPlanDefinition(planDefinitionId)
                .orElseThrow(() -> new IllegalStateException(String.format("No PlanDefinition found with id %s!", planDefinitionId)));
        questionnaireResponseModel.setPlanDefinitionTitle(planDefinition.getTitle());

        return questionnaireResponseModel;
    }

    private FrequencyModel mapTiming(Timing timing) {
        FrequencyModel frequencyModel = new FrequencyModel();

        if(timing.getRepeat() != null) {
            Timing.TimingRepeatComponent repeat = timing.getRepeat();
            frequencyModel.setWeekdays(repeat.getDayOfWeek().stream().map(d -> Enum.valueOf(Weekday.class, d.getValue().toString())).collect(Collectors.toList()));
            frequencyModel.setTimeOfDay(LocalTime.parse(repeat.getTimeOfDay().get(0).getValue()));
        }

        return frequencyModel;
    }

    private void mapBaseAttributesToModel(BaseModel target, DomainResource source) {
        target.setId(extractId(source));
        target.setOrganizationId(ExtensionMapper.extractOrganizationId(source.getExtension()));
    }

    private void mapBaseAttributesToFhir(DomainResource target, BaseModel source) {
        // We may be creating the resource, and in that case, it is perfectly ok for it not to have id and organization id.
        if(source.getId() != null) {
            target.setId(source.getId().toString());
        }
        if(source.getOrganizationId() != null) {
            target.addExtension(ExtensionMapper.mapOrganizationId(source.getOrganizationId()));
        }
    }

    private List<ThresholdModel> getThresholds(CarePlan.CarePlanActivityDetailComponent detail) {
        return ExtensionMapper.extractThresholds(detail.getExtensionsByUrl(Systems.THRESHOLD));
    }

    private QualifiedId extractId(DomainResource resource) {
        String unqualifiedVersionless = resource.getIdElement().toUnqualifiedVersionless().getValue();
        if(FhirUtils.isPlainId(unqualifiedVersionless)) {
            return new QualifiedId(unqualifiedVersionless, resource.getResourceType());
        }
        else if (FhirUtils.isQualifiedId(unqualifiedVersionless, resource.getResourceType())) {
            return new QualifiedId(unqualifiedVersionless);
        }
        else {
            throw new IllegalArgumentException(String.format("Illegal id for resource of type %s: %s!", resource.getResourceType(), unqualifiedVersionless));
        }
    }

    private String extractCpr(Patient patient) {
        return patient.getIdentifier().get(0).getValue();
    }

    private String extractFamilyName(Patient patient) {
        if(patient.getName() == null || patient.getName().isEmpty()) {
            return null;
        }
        return patient.getName().get(0).getFamily();
    }

    private String extractGivenNames(Patient patient) {
        if(patient.getName() == null || patient.getName().isEmpty()) {
            return null;
        }
        return patient.getName().get(0).getGivenAsSingleString();
    }

    private ContactDetailsModel extractPatientContactDetails(Patient patient) {
        ContactDetailsModel contactDetails = new ContactDetailsModel();

        contactDetails.setPrimaryPhone(extractPrimaryPhone(patient));

        return contactDetails;
    }

    private String extractPrimaryPhone(Patient patient) {
        if(patient.getTelecom() == null || patient.getTelecom().isEmpty()) {
            return null;
        }
        for(ContactPoint cp : patient.getTelecom()) {
            if(!cp.getValue().contains("@")) {
                return cp.getValue();
            }
        }
        return null;
    }

    private QuestionModel getQuestion(Questionnaire questionnaire, String linkId) {
        var item = getQuestionnaireItem(questionnaire, linkId);
        if(item == null) {
            throw new IllegalStateException(String.format("Malformed QuestionnaireResponse: Question for linkId %s not found in Questionnaire %s!", linkId, questionnaire.getId()));
        }

        return mapQuestionnaireItem(item);
    }

    private Questionnaire.QuestionnaireItemComponent getQuestionnaireItem(Questionnaire questionnaire, String linkId) {
        for(var item : questionnaire.getItem()) {
            if(item.getLinkId().equals(linkId)) {
                return item;
            }
        }
        return null;
    }

    private Timing mapFrequencyModel(FrequencyModel frequencyModel) {
        Timing timing = new Timing();

        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();

        EnumFactory<Timing.DayOfWeek> factory = new Timing.DayOfWeekEnumFactory();
        repeat.setDayOfWeek(frequencyModel.getWeekdays().stream().map(w -> new Enumeration<>(factory, w.toString().toLowerCase())).collect(Collectors.toList()));
        repeat.setTimeOfDay(List.of(new TimeType(frequencyModel.getTimeOfDay().toString())));
        timing.setRepeat(repeat);

        return timing;
    }

    private QuestionModel mapQuestionnaireItem(Questionnaire.QuestionnaireItemComponent item) {
        QuestionModel question = new QuestionModel();

        question.setLinkId(item.getLinkId());
        question.setText(item.getText());
        question.setRequired(item.getRequired());
        if(item.getAnswerOption() != null) {
            question.setOptions(mapOptions(item.getAnswerOption()));
        }
        question.setQuestionType(mapQuestionType(item.getType()));

        return question;
    }

    private List<String> mapOptions(List<Questionnaire.QuestionnaireItemAnswerOptionComponent> optionComponents) {
        return optionComponents
                .stream()
                .map(oc -> oc.getValue().primitiveValue())
                .collect(Collectors.toList());
    }

    private QuestionType mapQuestionType(Questionnaire.QuestionnaireItemType type) {
        switch(type) {
            case CHOICE:
                return QuestionType.CHOICE;
            case INTEGER:
                return QuestionType.INTEGER;
            case QUANTITY:
                return QuestionType.QUANTITY;
            case STRING:
                return QuestionType.STRING;
            case BOOLEAN:
                return QuestionType.BOOLEAN;
            default:
                throw new IllegalArgumentException(String.format("Don't know how to map QuestionnaireItemType %s", type.toString()));
        }
    }

    private AnswerModel getAnswer(QuestionnaireResponse.QuestionnaireResponseItemComponent item) {
        AnswerModel answer = new AnswerModel();

        var answerItem = extractAnswerItem(item);
        answer.setLinkId(item.getLinkId());

        if(answerItem.hasValueStringType()) {
            answer.setValue(answerItem.getValue().primitiveValue());
        }
        else if(answerItem.hasValueIntegerType()) {
            answer.setValue(answerItem.getValueIntegerType().primitiveValue());
        }
        else if(answerItem.hasValueQuantity()) {
            answer.setValue(answerItem.getValueQuantity().getValueElement().primitiveValue());
        }
        else if(answerItem.hasValueBooleanType()) {
            answer.setValue(answerItem.getValueBooleanType().primitiveValue());
        }
        answer.setAnswerType(getAnswerType(answerItem));

        return answer;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent extractAnswerItem(QuestionnaireResponse.QuestionnaireResponseItemComponent item) {
        if(item.getAnswer() == null || item.getAnswer().size() != 1) {
            throw new IllegalStateException("Expected exactly one answer!");
        }
        return item.getAnswer().get(0);
    }

    private AnswerType getAnswerType(QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answerItem) {
        Type answerType = answerItem.getValue();
        if (answerType instanceof StringType) {
            return AnswerType.STRING;
        }
        if (answerType instanceof BooleanType) {
            return AnswerType.BOOLEAN;
        }
        else if (answerType instanceof Quantity) {
            return AnswerType.QUANTITY;
        }
        else if (answerType instanceof IntegerType) {
            return AnswerType.INTEGER;
        }
        else {
            throw new IllegalArgumentException(String.format("Unsupported AnswerItem of type: %s", answerType));
        }
    }

    private CarePlan.CarePlanActivityComponent buildCarePlanActivity(QuestionnaireWrapperModel questionnaireWrapperModel) {
        CanonicalType instantiatesCanonical = new CanonicalType(questionnaireWrapperModel.getQuestionnaire().getId().toString());
        Type timing = mapFrequencyModel(questionnaireWrapperModel.getFrequency());
        Extension activitySatisfiedUntil = ExtensionMapper.mapActivitySatisfiedUntil(questionnaireWrapperModel.getSatisfiedUntil());
        List<Extension> thresholds = ExtensionMapper.mapThresholds(questionnaireWrapperModel.getThresholds());

        return buildActivity(instantiatesCanonical, timing, activitySatisfiedUntil, thresholds);
    }

    private CarePlan.CarePlanActivityComponent buildActivity(CanonicalType instantiatesCanonical, Type timing, Extension activitySatisfiedUntil, List<Extension> thresholds) {
        CarePlan.CarePlanActivityComponent activity = new CarePlan.CarePlanActivityComponent();

        activity.setDetail(buildDetail(instantiatesCanonical, timing, activitySatisfiedUntil, thresholds));

        return activity;
    }

    private CarePlan.CarePlanActivityDetailComponent buildDetail(CanonicalType instantiatesCanonical, Type timing, Extension activitySatisfiedUntil, List<Extension> thresholds) {
        CarePlan.CarePlanActivityDetailComponent detail = new CarePlan.CarePlanActivityDetailComponent();

        detail.setInstantiatesCanonical(List.of(instantiatesCanonical));
        detail.setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED);
        detail.addExtension(activitySatisfiedUntil);
        for(Extension threshold : thresholds) {
            detail.addExtension(threshold);
        }

        detail.setScheduled(timing);

        return detail;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent getQuestionnaireResponseItem(AnswerModel answer) {
        var item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();

        item.setLinkId(answer.getLinkId());
        item.getAnswer().add(getAnswerItem(answer));

        return item;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent getAnswerItem(AnswerModel answer) {
        var answerItem = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();

        answerItem.setValue(getValue(answer));

        return answerItem;
    }

    private Type getValue(AnswerModel answer) {
        Type value = null;
        switch(answer.getAnswerType()) {
            case INTEGER:
                value = new IntegerType(answer.getValue());
                break;
            case STRING:
                value = new StringType(answer.getValue());
                break;
            case QUANTITY:
                value = new Quantity(Double.parseDouble(answer.getValue()));
                break;
            case BOOLEAN:
                value = new BooleanType(answer.getValue());
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown AnswerType: %s", answer.getAnswerType()));
        }
        return value;
    }

    private QuestionnaireWrapperModel mapPlanDefinitionAction(PlanDefinition.PlanDefinitionActionComponent action, FhirLookupResult lookupResult) {
        var wrapper = new QuestionnaireWrapperModel();

        wrapper.setFrequency(mapTiming(action.getTimingTiming()));

        String questionnaireId = action.getDefinitionCanonicalType().getValue();
        Questionnaire questionnaire = lookupResult
                .getQuestionnaire(questionnaireId)
                .orElseThrow(() -> new IllegalStateException(String.format("Could not look up Questionnaire with id %s!", questionnaireId)));
        wrapper.setQuestionnaire(mapQuestionnaire(questionnaire));

        List<ThresholdModel> thresholds = ExtensionMapper.extractThresholds(action.getExtensionsByUrl(Systems.THRESHOLD));
        wrapper.setThresholds(thresholds);

        return wrapper;
    }
}
