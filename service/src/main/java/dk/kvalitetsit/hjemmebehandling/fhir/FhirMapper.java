package dk.kvalitetsit.hjemmebehandling.fhir;

import dk.kvalitetsit.hjemmebehandling.api.Option;
import dk.kvalitetsit.hjemmebehandling.constants.*;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumeration;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    .map(this::buildCarePlanActivity)
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

        carePlanModel.setPlanDefinitions(new ArrayList<>());
        for(var ic : carePlan.getInstantiatesCanonical()) {
            var planDefinition = lookupResult
                .getPlanDefinition(ic.getValue())
                .orElseThrow(() -> new IllegalStateException(String.format("Could not look up PlanDefinition for CarePlan %s!", carePlanModel.getId())));
            carePlanModel.getPlanDefinitions().add(mapPlanDefinition(planDefinition, lookupResult));
        }

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
            wrapper.setSatisfiedUntil(ExtensionMapper.extractActivitySatisfiedUntil(activity.getDetail().getExtension()));

            // find thresholds from plandefinition
            Optional<List<ThresholdModel>> thresholds = carePlanModel.getPlanDefinitions().stream()
                .flatMap(p -> p.getQuestionnaires().stream())
                .filter(q -> q.getQuestionnaire().getId().equals(questionnaireModel.getId()))
                .findFirst()
                .map(QuestionnaireWrapperModel::getThresholds);
            thresholds.ifPresent(wrapper::setThresholds);

            carePlanModel.getQuestionnaires().add(wrapper);
        }

        carePlanModel.setSatisfiedUntil(ExtensionMapper.extractCarePlanSatisfiedUntil(carePlan.getExtension()));

        String organizationId = ExtensionMapper.extractOrganizationId(carePlan.getExtension());
        Organization organization = lookupResult.getOrganization(organizationId)
                .orElseThrow(() -> new IllegalStateException(String.format("Organization with id %s was not present when trying to map careplan %s!", organizationId, carePlan.getId())));
        carePlanModel.setDepartmentName(organization.getName());

        if (organization.getExtensionByUrl(Systems.QUESTIONNAIRE_SUMMARY_BLOB) != null) {
            final String staticSummaryHtml = organization.getExtensionByUrl(Systems.QUESTIONNAIRE_SUMMARY_BLOB).getValue().primitiveValue();
            carePlanModel.getQuestionnaires().stream()
                    .map(w -> w.getQuestionnaire())
                    .forEach(q -> q.setBlob(staticSummaryHtml));
        }

        return carePlanModel;
    }

    public OrganizationModel mapOrganization(Organization organization) {
        OrganizationModel organizationModel = new OrganizationModel();

        organizationModel.setId(extractId(organization));
        organizationModel.setName(organization.getName());
        if (organization.getExtensionByUrl(Systems.ORGANISATION_BLOB) != null) organizationModel.setBlob(organization.getExtensionByUrl(Systems.ORGANISATION_BLOB).getValue().primitiveValue());
        organizationModel.setContactDetails(new ContactDetailsModel());

        var address = organization.getAddressFirstRep();
        if(address != null) {
            organizationModel.getContactDetails().setAddress(new AddressModel());
            organizationModel.getContactDetails().getAddress().setStreet(String.join("\n", address.getLine().stream().map(PrimitiveType::getValue).collect(Collectors.toList())));
            organizationModel.getContactDetails().getAddress().setPostalCode(address.getPostalCode());
            organizationModel.getContactDetails().getAddress().setCity(address.getCity());
            organizationModel.getContactDetails().getAddress().setCountry(address.getCountry());
        }

        var telecom = organization.getTelecomFirstRep();
        if(telecom != null) {
            organizationModel.getContactDetails().setPhone(new PhoneModel());
            organizationModel.getContactDetails().getPhone().setPrimary(telecom.getValue());
            organizationModel.setPhoneHours(ExtensionMapper.extractPhoneHours(telecom.getExtensionsByUrl(Systems.PHONE_HOURS)));
        }

        return organizationModel;
    }

    public PatientModel mapPatient(Patient patient) {
        PatientModel patientModel = new PatientModel();

        patientModel.setId(extractId(patient));
        //patientModel.setCustomUserId(ExtensionMapper.extractCustomUserId(patient.getExtension()));
        //patientModel.setCustomUserName(ExtensionMapper.extractCustomUserName(patient.getExtension()));
        patientModel.setGivenName(extractGivenNames(patient));
        patientModel.setFamilyName(extractFamilyName(patient));
        patientModel.setCpr(extractCpr(patient));
        patientModel.setContactDetails(extractPatientContactDetails(patient));


        patientModel.setContacts(mapContacts(patient));

        return patientModel;
    }

    public List<PrimaryContactModel> mapContacts(Patient patient) {
        return patient.getContact().stream().map(this::mapContact).collect(Collectors.toList());
    }

    public PrimaryContactModel mapContact(Patient.ContactComponent contact) {
        PrimaryContactModel model = new PrimaryContactModel();
        model.setName(contact.getName().getText());
        model.setAffiliation(contact.getRelationshipFirstRep().getText());
        model.setOrganisation(contact.getOrganization().getReference());

        var primaryRelativeContactDetails = new ContactDetailsModel();
        primaryRelativeContactDetails.setAddress(new AddressModel());

        // Extract phone numbers
        if(contact.getTelecom() != null && !contact.getTelecom().isEmpty()) {
            primaryRelativeContactDetails.setPhone(new PhoneModel());
            for(var telecom : contact.getTelecom()) {
                if(telecom.getRank() == 1) {
                    primaryRelativeContactDetails.getPhone().setPrimary(telecom.getValue());
                }
                if(telecom.getRank() == 2) {
                    primaryRelativeContactDetails.getPhone().setSecondary(telecom.getValue());
                }
            }
            model.setContactDetails(primaryRelativeContactDetails);
        }
        return model;
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
        questionnaireModel.setQuestions(questionnaire.getItem().stream()
                .filter(q -> !q.getLinkId().equals(Systems.CALL_TO_ACTION_LINK_ID)) // filter out call-to-action's
                .map(this::mapQuestionnaireItem).collect(Collectors.toList()));
        questionnaireModel.setCallToAction(questionnaire.getItem().stream()
                .filter(q -> q.getLinkId().equals(Systems.CALL_TO_ACTION_LINK_ID)) // process call-to-action's
                .findFirst()
                .map(this::mapQuestionnaireItem)
                .orElse(null));

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

    public QuestionnaireResponseModel mapQuestionnaireResponse(QuestionnaireResponse questionnaireResponse, FhirLookupResult lookupResult, List<Questionnaire> historicalQuestionnaires) {

        if( historicalQuestionnaires == null ) mapQuestionnaireResponse(questionnaireResponse, lookupResult);

        QuestionnaireResponseModel questionnaireResponseModel = constructQuestionnaireResponse(questionnaireResponse, lookupResult);

        // Populate questionAnswerMap
        List<QuestionAnswerPairModel> answers = new ArrayList<>();

        //Look through all the given questionnaires
        for(var item : questionnaireResponse.getItem()) {
            QuestionModel question = null;
            boolean deprecated = false;
            int i = 0;
            assert historicalQuestionnaires != null;
            for (Questionnaire q : historicalQuestionnaires) {
                if (i > 0) deprecated = true;
                boolean hasNext = i < historicalQuestionnaires.size()-1;
                try {
                    question = getQuestion(q, item.getLinkId());
                    question.setDeprecated(deprecated);
                    break;
                }catch (IllegalStateException e) {
                    if (!hasNext) throw new IllegalStateException("Corresponding question could not be found in the given questionnaires");
                }
                i++;
            }
            AnswerModel answer = getAnswer(item);
            answers.add(new QuestionAnswerPairModel(question, answer));
        }
        questionnaireResponseModel.setQuestionAnswerPairs(answers);
        return questionnaireResponseModel;
    }

    public QuestionnaireResponseModel mapQuestionnaireResponse(QuestionnaireResponse questionnaireResponse, FhirLookupResult lookupResult) {
        QuestionnaireResponseModel questionnaireResponseModel = constructQuestionnaireResponse(questionnaireResponse, lookupResult);

        String questionnaireId = questionnaireResponse.getQuestionnaire();
        Questionnaire questionnaire = lookupResult.getQuestionnaire(questionnaireId)
                .orElseThrow(() -> new IllegalStateException(String.format("No Questionnaire found with id %s!", questionnaireId)));

        // Populate questionAnswerMap
        List<QuestionAnswerPairModel> answers = new ArrayList<>();

        for(var item : questionnaireResponse.getItem()) {
            QuestionModel question;
            try {
                question = getQuestion(questionnaire, item.getLinkId());
            } catch (IllegalStateException e) {
                // Corresponding question could not be found in the current/newest questionnaire
                // ignore
                // Or use the overloaded version which runs thought historical versions as well
                // and returns deprecated questions
                question = null;
            }
            AnswerModel answer = getAnswer(item);
            answers.add(new QuestionAnswerPairModel(question, answer));
        }
        questionnaireResponseModel.setQuestionAnswerPairs(answers);


        return questionnaireResponseModel;
    }



    private QuestionnaireResponseModel constructQuestionnaireResponse(QuestionnaireResponse questionnaireResponse, FhirLookupResult lookupResult) {
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


        questionnaireResponseModel.setAnswered(questionnaireResponse.getAuthored().toInstant());
        questionnaireResponseModel.setExaminationStatus(ExtensionMapper.extractExaminationStatus(questionnaireResponse.getExtension()));
        if (questionnaireResponseModel.getExaminationStatus().equals(ExaminationStatus.EXAMINED)) {
            questionnaireResponseModel.setExamined(questionnaireResponse.getMeta().getLastUpdated().toInstant());
        }
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

    public FrequencyModel mapTiming(Timing timing) {
        FrequencyModel frequencyModel = new FrequencyModel();

        if(timing.getRepeat() != null) {
            Timing.TimingRepeatComponent repeat = timing.getRepeat();
            frequencyModel.setWeekdays(repeat.getDayOfWeek().stream().map(d -> Enum.valueOf(Weekday.class, d.getValue().toString())).collect(Collectors.toList()));
            if(!repeat.getTimeOfDay().isEmpty())
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

        contactDetails.setAddress(new AddressModel());
        var lines = patient.getAddressFirstRep().getLine();
        if(lines != null && !lines.isEmpty()) {
            contactDetails.getAddress().setStreet(String.join(", ", lines.stream().map(PrimitiveType::getValue).collect(Collectors.toList())));
        }
        contactDetails.getAddress().setCity(patient.getAddressFirstRep().getCity());
        contactDetails.getAddress().setPostalCode(patient.getAddressFirstRep().getPostalCode());
        contactDetails.getAddress().setCountry(extractCountry(patient));

        contactDetails.setPhone(new PhoneModel());
        contactDetails.getPhone().setPrimary(extractPrimaryPhone(patient.getTelecom()));
        contactDetails.getPhone().setSecondary(extractSecondaryPhone(patient.getTelecom()));

        return contactDetails;
    }
    private String extractCountry(Patient patient) {
        var country = patient.getAddressFirstRep().getCountry();
        if(country == null || country.isEmpty()) {
            return null;
        }
        return country;
    }
    private String extractPrimaryPhone(List<ContactPoint> contactPoints) {
        return extractPhone(contactPoints, 1);
    }

    private String extractSecondaryPhone(List<ContactPoint> contactPoints) {
        return extractPhone(contactPoints, 2);
    }

    private String extractPhone(List<ContactPoint> contactPoints, int rank) {
        if(contactPoints == null || contactPoints.isEmpty()) {
            return null;
        }
        for(ContactPoint cp : contactPoints) {
            if(cp.getSystem().equals(ContactPoint.ContactPointSystem.PHONE) && cp.getRank() == rank) {
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
            if(item != null && item.getLinkId() != null && item.getLinkId().equals(linkId)) {
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
        question.setAbbreviation(ExtensionMapper.extractQuestionAbbreviation(item.getExtension()));
        question.setHelperText( mapQuestionnaireItemHelperText(item.getItem()));
        question.setRequired(item.getRequired());
        if(item.getAnswerOption() != null) {
            question.setOptions(mapOptions(item.getAnswerOption()));
        }
        question.setQuestionType(mapQuestionType(item.getType()));
        if (item.getType().equals(Questionnaire.QuestionnaireItemType.QUANTITY) && item.hasCode()) {
            question.setMeasurementType(mapCodingConcept(item.getCodeFirstRep().getSystem(), item.getCodeFirstRep().getCode(), item.getCodeFirstRep().getDisplay()));
        }
        if (item.hasEnableWhen()) {
            question.setEnableWhens(mapEnableWhens(item.getEnableWhen()));
        }
        if (item.getType() == Questionnaire.QuestionnaireItemType.GROUP) {
            question.setSubQuestions( mapQuestionnaireItemGroupQuestions(item.getItem()) );
        }

        return question;
    }

    private List<QuestionModel> mapQuestionnaireItemGroupQuestions(List<Questionnaire.QuestionnaireItemComponent> item) {
        return item.stream()
                .filter(i -> i.getType() != Questionnaire.QuestionnaireItemType.DISPLAY)
                .map(this::mapQuestionnaireItem)
                .collect(Collectors.toList());
    }

    public List<MeasurementTypeModel> extractMeasurementTypes(ValueSet valueSet) {
        List<MeasurementTypeModel> result = new ArrayList<>();

        valueSet.getCompose().getInclude()
                .forEach(csc -> {
                    var measurementTypes = csc.getConcept().stream()
                            .map(crc -> mapConceptReferenceComponent(csc.getSystem(), crc))
                            .collect(Collectors.toList());

                    result.addAll(measurementTypes);
                });

        return result;
    }

    private MeasurementTypeModel mapConceptReferenceComponent(String system, ValueSet.ConceptReferenceComponent concept) {
        MeasurementTypeModel measurementTypeModel = mapCodingConcept(system, concept.getCode(), concept.getDisplay());
        measurementTypeModel.setThreshold(ExtensionMapper.extractThreshold(concept.getExtensionFirstRep()));

        return measurementTypeModel;
    }

    private MeasurementTypeModel mapCodingConcept(String system, String code, String display) {
        MeasurementTypeModel measurementTypeModel = new MeasurementTypeModel();

        measurementTypeModel.setSystem(system);
        measurementTypeModel.setCode(code);
        measurementTypeModel.setDisplay(display);

        return measurementTypeModel;
    }

    private String mapQuestionnaireItemHelperText(List<Questionnaire.QuestionnaireItemComponent> item) {
        return item.stream()
            .filter(i -> i.getType().equals(Questionnaire.QuestionnaireItemType.DISPLAY))
            .map(Questionnaire.QuestionnaireItemComponent::getText)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null)
            ;

    }

    private List<QuestionModel.EnableWhen> mapEnableWhens(List<Questionnaire.QuestionnaireItemEnableWhenComponent> enableWhen) {
        return enableWhen
            .stream()
            .map(this::mapEnableWhen)
            .collect(Collectors.toList());
    }

    private QuestionModel.EnableWhen mapEnableWhen(Questionnaire.QuestionnaireItemEnableWhenComponent enableWhen) {
        QuestionModel.EnableWhen newEnableWhen = new QuestionModel.EnableWhen();

        newEnableWhen.setOperator( mapEnableWhenOperator(enableWhen.getOperator()) );
        newEnableWhen.setAnswer( mapAnswer(enableWhen.getQuestion(), enableWhen.getAnswer()) );

        return newEnableWhen;
    }

    private AnswerModel mapAnswer(String question, Type answer) {
        AnswerModel answerModel = new AnswerModel();
        answerModel.setLinkId(question);

        if (answer instanceof StringType) {
            answerModel.setAnswerType(AnswerType.STRING);
            answerModel.setValue( ((StringType)answer).asStringValue() );
        }
        else if (answer instanceof BooleanType) {
            answerModel.setAnswerType(AnswerType.BOOLEAN);
            answerModel.setValue(((BooleanType) answer).asStringValue() );
        }
        else if (answer instanceof Quantity) {
            answerModel.setAnswerType(AnswerType.QUANTITY);
            answerModel.setValue(((Quantity) answer).getValueElement().asStringValue() );
        }
        else if (answer instanceof IntegerType) {
            answerModel.setAnswerType(AnswerType.INTEGER);
            answerModel.setValue(((IntegerType) answer).asStringValue() );
        }
        else {
            throw new IllegalArgumentException(String.format("Unsupported AnswerItem of type: %s", answer));
        }

        return answerModel;
    }

    private EnableWhenOperator mapEnableWhenOperator(Questionnaire.QuestionnaireItemOperator operator) {
        switch (operator) {
            case EQUAL:
                return EnableWhenOperator.EQUAL;
            case LESS_THAN:
                return EnableWhenOperator.LESS_THAN;
            case LESS_OR_EQUAL:
                return EnableWhenOperator.LESS_OR_EQUAL;
            case GREATER_THAN:
                return EnableWhenOperator.GREATER_THAN;
            case GREATER_OR_EQUAL:
                return EnableWhenOperator.GREATER_OR_EQUAL;
            default:
                throw new IllegalArgumentException(String.format("Don't know how to map QuestionnaireItemOperator %s", operator.toString()));
        }
    }

    private List<Option> mapOptions(List<Questionnaire.QuestionnaireItemAnswerOptionComponent> optionComponents) {
        return optionComponents
                .stream()
                .map(oc -> new Option(oc.getValue().primitiveValue(), ExtensionMapper.extractAnswerOptionComment(oc.getExtension())))
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
            case DISPLAY:
                return QuestionType.DISPLAY;
            case GROUP:
                return QuestionType.GROUP;
            default:
                throw new IllegalArgumentException(String.format("Don't know how to map QuestionnaireItemType %s", type.toString()));
        }
    }

    private AnswerModel getAnswer(QuestionnaireResponse.QuestionnaireResponseItemComponent item) {
        AnswerModel answer = new AnswerModel();
        answer.setLinkId(item.getLinkId());

        boolean emptyAnswer = item.getAnswer().isEmpty();
        boolean hasSubAnswers = !item.getItem().isEmpty();
        if (emptyAnswer && hasSubAnswers) {
            // group answer with sub-answers
            answer.setAnswerType(AnswerType.GROUP);
            answer.setSubAnswers(item.getItem().stream().map(this::getAnswer).collect(Collectors.toList()));
        }
        else {

            var answerItem = extractAnswerItem(item);

            if (answerItem.hasValueStringType()) {
                answer.setValue(answerItem.getValue().primitiveValue());
            } else if (answerItem.hasValueIntegerType()) {
                answer.setValue(answerItem.getValueIntegerType().primitiveValue());
            } else if (answerItem.hasValueQuantity()) {
                answer.setValue(answerItem.getValueQuantity().getValueElement().primitiveValue());
            } else if (answerItem.hasValueBooleanType()) {
                answer.setValue(answerItem.getValueBooleanType().primitiveValue());
            }
            answer.setAnswerType(getAnswerType(answerItem));
        }

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

        return buildActivity(instantiatesCanonical, timing, activitySatisfiedUntil);
    }

    private CarePlan.CarePlanActivityComponent buildActivity(CanonicalType instantiatesCanonical, Type timing, Extension activitySatisfiedUntil) {
        CarePlan.CarePlanActivityComponent activity = new CarePlan.CarePlanActivityComponent();

        activity.setDetail(buildDetail(instantiatesCanonical, timing, activitySatisfiedUntil));

        return activity;
    }

    private CarePlan.CarePlanActivityDetailComponent buildDetail(CanonicalType instantiatesCanonical, Type timing, Extension activitySatisfiedUntil) {
        CarePlan.CarePlanActivityDetailComponent detail = new CarePlan.CarePlanActivityDetailComponent();

        detail.setInstantiatesCanonical(List.of(instantiatesCanonical));
        detail.setStatus(CarePlan.CarePlanActivityStatus.NOTSTARTED);
        detail.addExtension(activitySatisfiedUntil);
        detail.setScheduled(timing);

        return detail;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent getQuestionnaireResponseItem(AnswerModel answer) {
        var item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();

        item.setLinkId(answer.getLinkId());
        item.getAnswer().add(getAnswerItem(answer));

        if (answer.getAnswerType() == AnswerType.GROUP && answer.getSubAnswers() != null) {
            item.setItem(answer.getSubAnswers().stream().map(this::getQuestionnaireResponseItem).collect(Collectors.toList()));
        }

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
            case GROUP:
                // return default = null
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

        List<ThresholdModel> questionnaireThresholds = ExtensionMapper.extractThresholds(
            questionnaire.getItem().stream()
                .flatMap(q -> q.getExtensionsByUrl(Systems.THRESHOLD).stream())
                .collect(Collectors.toList())
        );
        List<ThresholdModel> planDefinitionThresholds = ExtensionMapper.extractThresholds(action.getExtensionsByUrl(Systems.THRESHOLD));

        List<ThresholdModel> combinedThresholds = Stream.of(questionnaireThresholds, planDefinitionThresholds)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        wrapper.setThresholds(combinedThresholds);

        return wrapper;
    }
}
