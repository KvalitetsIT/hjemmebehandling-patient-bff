package dk.kvalitetsit.hjemmebehandling.api;

import dk.kvalitetsit.hjemmebehandling.constants.CarePlanStatus;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirUtils;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {
    public CarePlanModel mapCarePlanDto(CarePlanDto carePlanDto) {
        CarePlanModel carePlanModel = new CarePlanModel();

        mapBaseAttributesToModel(carePlanModel, carePlanDto, ResourceType.CarePlan);

        carePlanModel.setTitle(carePlanDto.getTitle());
        if(carePlanDto.getStatus() != null) {
            carePlanModel.setStatus(Enum.valueOf(CarePlanStatus.class, carePlanDto.getStatus()));
        }
        carePlanModel.setCreated(carePlanDto.getCreated());
        carePlanModel.setStartDate(carePlanDto.getStartDate());
        carePlanModel.setEndDate(carePlanDto.getEndDate());
        carePlanModel.setPatient(mapPatientDto(carePlanDto.getPatientDto()));
        carePlanModel.setQuestionnaires(List.of());
        if(carePlanDto.getQuestionnaires() != null) {
            carePlanModel.setQuestionnaires(carePlanDto.getQuestionnaires().stream().map(this::mapQuestionnaireWrapperDto).collect(Collectors.toList()));
        }
        carePlanModel.setPlanDefinitions(List.of());
        if(carePlanDto.getPlanDefinitions() != null) {
            carePlanModel.setPlanDefinitions(carePlanDto.getPlanDefinitions().stream().map(this::mapPlanDefinitionDto).collect(Collectors.toList()));
        }
        carePlanModel.setDepartmentName(carePlanDto.getDepartmentName());

        return carePlanModel;
    }

    public CarePlanDto mapCarePlanModel(CarePlanModel carePlan) {
        CarePlanDto carePlanDto = new CarePlanDto();

        mapBaseAttributesToDto(carePlanDto, carePlan);

        carePlanDto.setTitle(carePlan.getTitle());
        carePlanDto.setStatus(carePlan.getStatus().toString());
        carePlanDto.setCreated(carePlan.getCreated());
        carePlanDto.setStartDate(carePlan.getStartDate());
        carePlanDto.setEndDate(carePlan.getEndDate());
        carePlanDto.setPatientDto(mapPatientModel(carePlan.getPatient()));
        carePlanDto.setQuestionnaires(carePlan.getQuestionnaires().stream().map(this::mapQuestionnaireWrapperModel).collect(Collectors.toList()));
        carePlanDto.setPlanDefinitions(carePlan.getPlanDefinitions().stream().map(this::mapPlanDefinitionModel).collect(Collectors.toList()));
        carePlanDto.setDepartmentName(carePlan.getDepartmentName());

        return carePlanDto;
    }

    public FrequencyModel mapFrequencyDto(FrequencyDto frequencyDto) {
        FrequencyModel frequencyModel = new FrequencyModel();

        frequencyModel.setWeekdays(frequencyDto.getWeekdays());
        frequencyModel.setTimeOfDay(LocalTime.parse(frequencyDto.getTimeOfDay()));

        return frequencyModel;
    }

    public FrequencyDto mapFrequencyModel(FrequencyModel frequencyModel) {
        FrequencyDto frequencyDto = new FrequencyDto();

        frequencyDto.setWeekdays(frequencyModel.getWeekdays());
        if(frequencyModel.getTimeOfDay() != null)
            frequencyDto.setTimeOfDay(frequencyModel.getTimeOfDay().toString());

        return frequencyDto;
    }

    public OrganizationModel mapOrganizationDto(OrganizationDto organizationDto) {
        OrganizationModel organizationModel = new OrganizationModel();

        organizationModel.setName(organizationDto.getName());

        organizationModel.setContactDetails(mapContactDetailsDto(organizationDto.getContactDetails()));

        if(organizationDto.getPhoneHours() != null) {
            organizationModel.setPhoneHours(organizationDto.getPhoneHours()
                    .stream()
                    .map(ph -> {
                        var hours = new PhoneHourModel();
                        hours.setWeekdays(ph.getWeekdays());
                        hours.setFrom(LocalTime.parse(ph.getFrom()));
                        hours.setTo(LocalTime.parse(ph.getTo()));
                        return hours;
                    })
                    .collect(Collectors.toList()));
        }
        return organizationModel;
    }

    private PhoneModel mapPhoneDto(PhoneDto phone) {
        return new PhoneModel(phone.getPrimary(), phone.getSecondary());
    }

    public OrganizationDto mapOrganizationModel(OrganizationModel organizationModel) {
        OrganizationDto organizationDto = new OrganizationDto();

        organizationDto.setId(organizationModel.getId().toString());
        organizationDto.setName(organizationModel.getName());
        organizationDto.setBlob(organizationModel.getBlob());
        organizationDto.setContactDetails(mapContactDetailsModel(organizationModel.getContactDetails()));

        if(organizationModel.getPhoneHours() != null) {
            organizationDto.setPhoneHours(organizationModel.getPhoneHours()
                    .stream()
                    .map(ph -> {
                        var hours = new PhoneHourDto();
                        hours.setWeekdays(ph.getWeekdays());
                        hours.setFrom(ph.getFrom().toString());
                        hours.setTo(ph.getTo().toString());
                        return hours;
                    })
                    .collect(Collectors.toList()));
        }
        return organizationDto;
    }

    public PatientModel mapPatientDto(PatientDto patient) {
        PatientModel patientModel = new PatientModel();

        patientModel.setCpr(patient.getCpr());
        patientModel.setFamilyName(patient.getFamilyName());
        patientModel.setGivenName(patient.getGivenName());
        if(patient.getContactsDetails() != null) {
            patientModel.setContactDetails(mapContactDetailsDto(patient.getContactsDetails()));
        }
        patientModel.setContacts(mapPrimaryContactDtos(patient.getPrimaryContacts()));

        return patientModel;
    }

    private List<PrimaryContactModel> mapPrimaryContactDtos(List<PrimaryContactDto> contacts) {

        return contacts.stream().map(this::mapPrimaryContactDto).collect(Collectors.toList());
    }

    private PrimaryContactModel mapPrimaryContactDto(PrimaryContactDto contact) {
        PrimaryContactModel model = new PrimaryContactModel();
        model.setOrganisation(contact.getOrganization());
        model.setAffiliation(contact.getAffiliation());
        model.setName(contact.getName());
        if(contact.getContactDetails() != null) {
            model.setContactDetails(mapContactDetailsDto(contact.getContactDetails()));
        }

        return model;
    }

    public PatientDto mapPatientModel(PatientModel patient) {
        PatientDto patientDto = new PatientDto();

        patientDto.setCpr(patient.getCpr());
        patientDto.setFamilyName(patient.getFamilyName());
        patientDto.setGivenName(patient.getGivenName());
        // patientDto.setCustomUserName(patient.getCustomUserName());
        if(patient.getContactDetails() != null) {
            patientDto.setContactsDetails(mapContactDetailsModel(patient.getContactDetails()));
        }
        patientDto.setPrimaryContacts(mapPrimaryContactModels(patient.getContacts()));;
        return patientDto;
    }

    private List<PrimaryContactDto> mapPrimaryContactModels(List<PrimaryContactModel> contacts) {
        return contacts.stream().map(this::mapPrimaryContactModel).collect(Collectors.toList());
    }
    private PrimaryContactDto mapPrimaryContactModel(PrimaryContactModel contact) {
        PrimaryContactDto dto = new PrimaryContactDto();
        dto.setName(contact.getName());
        dto.setAffiliation(contact.getAffiliation());
        dto.setOrganization(contact.getOrganisation());
        if (contact.getContactDetails() != null) dto.setContactDetails(mapContactDetailsModel(contact.getContactDetails()));
        return dto;
    }

    public PlanDefinitionModel mapPlanDefinitionDto(PlanDefinitionDto planDefinitionDto) {
        PlanDefinitionModel planDefinitionModel = new PlanDefinitionModel();

        mapBaseAttributesToModel(planDefinitionModel, planDefinitionDto, ResourceType.PlanDefinition);

        planDefinitionModel.setName(planDefinitionDto.getName());
        planDefinitionModel.setTitle(planDefinitionDto.getTitle());
        // TODO - planDefinitionModel.getQuestionnaires() should never return null - but it can for now.
        if(planDefinitionDto.getQuestionnaires() != null) {
            planDefinitionModel.setQuestionnaires(planDefinitionDto.getQuestionnaires().stream().map(this::mapQuestionnaireWrapperDto).collect(Collectors.toList()));
        }

        return planDefinitionModel;
    }

    public PlanDefinitionDto mapPlanDefinitionModel(PlanDefinitionModel planDefinitionModel) {
        PlanDefinitionDto planDefinitionDto = new PlanDefinitionDto();

        mapBaseAttributesToDto(planDefinitionDto, planDefinitionModel);

        planDefinitionDto.setName(planDefinitionModel.getName());
        planDefinitionDto.setTitle(planDefinitionModel.getTitle());
        // TODO - planDefinitionModel.getQuestionnaires() should never return null - but it can for now.
        if(planDefinitionModel.getQuestionnaires() != null) {
            planDefinitionDto.setQuestionnaires(planDefinitionModel.getQuestionnaires().stream().map(this::mapQuestionnaireWrapperModel).collect(Collectors.toList()));
        }

        return planDefinitionDto;
    }

    public ThresholdModel mapThresholdDto(ThresholdDto thresholdDto) {
        ThresholdModel thresholdModel = new ThresholdModel();

        thresholdModel.setQuestionnaireItemLinkId(thresholdDto.getQuestionId());
        thresholdModel.setType(thresholdDto.getType());
        thresholdModel.setValueBoolean(thresholdDto.getValueBoolean());
        thresholdModel.setValueQuantityLow(thresholdDto.getValueQuantityLow());
        thresholdModel.setValueQuantityHigh(thresholdDto.getValueQuantityHigh());

        return thresholdModel;
    }

    public ThresholdDto mapThresholdModel(ThresholdModel thresholdModel) {
        ThresholdDto thresholdDto = new ThresholdDto();

        thresholdDto.setQuestionId(thresholdModel.getQuestionnaireItemLinkId());
        thresholdDto.setConceptCode(thresholdModel.getConceptCode());
        thresholdDto.setType(thresholdModel.getType());
        thresholdDto.setValueBoolean(thresholdModel.getValueBoolean());
        thresholdDto.setValueQuantityLow(thresholdModel.getValueQuantityLow());
        thresholdDto.setValueQuantityHigh(thresholdModel.getValueQuantityHigh());

        return thresholdDto;
    }

    public QuestionnaireModel mapQuestionnaireDto(QuestionnaireDto questionnaireDto) {
        QuestionnaireModel questionnaireModel = new QuestionnaireModel();

        mapBaseAttributesToModel(questionnaireModel, questionnaireDto, ResourceType.Questionnaire);

        questionnaireModel.setTitle(questionnaireDto.getTitle());
        questionnaireModel.setStatus(questionnaireDto.getStatus());
        if(questionnaireDto.getQuestions() != null) {
            questionnaireModel.setQuestions(questionnaireDto.getQuestions().stream().map(this::mapQuestionDto).collect(Collectors.toList()));
        }

        return questionnaireModel;
    }

    public QuestionnaireDto mapQuestionnaireModel(QuestionnaireModel questionnaireModel) {
        QuestionnaireDto questionnaireDto = new QuestionnaireDto();

        mapBaseAttributesToDto(questionnaireDto, questionnaireModel);

        questionnaireDto.setTitle(questionnaireModel.getTitle());
        questionnaireDto.setStatus(questionnaireModel.getStatus());
        if(questionnaireModel.getQuestions() != null) {
            questionnaireDto.setQuestions(questionnaireModel.getQuestions().stream().map(this::mapQuestionModel).collect(Collectors.toList()));
        }

        return questionnaireDto;
    }

    public QuestionnaireResponseModel mapQuestionnaireResponseDto(QuestionnaireResponseDto questionnaireResponseDto) {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();

        mapBaseAttributesToModel(questionnaireResponseModel, questionnaireResponseDto, ResourceType.QuestionnaireResponse);
        questionnaireResponseModel.setQuestionnaireId(toQualifiedId(questionnaireResponseDto.getQuestionnaireId(), ResourceType.Questionnaire));
        questionnaireResponseModel.setCarePlanId(toQualifiedId(questionnaireResponseDto.getCarePlanId(), ResourceType.CarePlan));
        questionnaireResponseModel.setQuestionnaireName(questionnaireResponseModel.getQuestionnaireName());
        questionnaireResponseModel.setQuestionAnswerPairs(questionnaireResponseDto.getQuestionAnswerPairs().stream().map(this::mapQuestionAnswerPairDto).collect(Collectors.toList()));

        return questionnaireResponseModel;
    }

    public QuestionnaireResponseDto mapQuestionnaireResponseModel(QuestionnaireResponseModel questionnaireResponseModel) {
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();

        mapBaseAttributesToDto(questionnaireResponseDto, questionnaireResponseModel);

        questionnaireResponseDto.setQuestionnaireId(questionnaireResponseModel.getQuestionnaireId().toString());
        questionnaireResponseDto.setCarePlanId(questionnaireResponseModel.getCarePlanId().toString());
        questionnaireResponseDto.setQuestionnaireName(questionnaireResponseModel.getQuestionnaireName());
        questionnaireResponseDto.setQuestionAnswerPairs(questionnaireResponseModel.getQuestionAnswerPairs().stream().map(this::mapQuestionAnswerPairModel).collect(Collectors.toList()));
        questionnaireResponseDto.setAnswered(questionnaireResponseModel.getAnswered());
        questionnaireResponseDto.setExaminationStatus(questionnaireResponseModel.getExaminationStatus());
        questionnaireResponseDto.setExamined(questionnaireResponseModel.getExamined());
        questionnaireResponseDto.setTriagingCategory(questionnaireResponseModel.getTriagingCategory());
        questionnaireResponseDto.setPatient(mapPatientModel(questionnaireResponseModel.getPatient()));

        return questionnaireResponseDto;
    }

    private void mapBaseAttributesToModel(BaseModel target, BaseDto source, ResourceType resourceType) {
        if(source.getId() == null) {
            // OK, in case a resource is being created.
            return;
        }

        target.setId(toQualifiedId(source.getId(), resourceType));
    }

    private QualifiedId toQualifiedId(String id, ResourceType resourceType) {
        if(FhirUtils.isPlainId(id)) {
            return new QualifiedId(id, resourceType);
        }
        else if(FhirUtils.isQualifiedId(id, resourceType)) {
            return new QualifiedId(id);
        }
        else {
            throw new IllegalArgumentException(String.format("Illegal id provided for resource of type %s: %s!", resourceType.toString(), id));
        }
    }

    private void mapBaseAttributesToDto(BaseDto target, BaseModel source) {
        target.setId(source.getId().toString());
        target.setOrganizationId(source.getOrganizationId());
    }

    private ContactDetailsModel mapContactDetailsDto(ContactDetailsDto contactDetails) {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();
        contactDetailsModel.setAddress(mapAddressDto(contactDetails.getAddress()));
        if (contactDetails.getPhone() != null) contactDetailsModel.setPhone(mapPhoneDto(contactDetails.getPhone()));
        return contactDetailsModel;
    }

    private AddressModel mapAddressDto(AddressDto address) {
        return new AddressModel(address.getStreet(), address.getPostalCode(), address.getCountry(), address.getCity());
    }

    public ContactDetailsDto mapContactDetailsModel(ContactDetailsModel contactDetails) {
        ContactDetailsDto contactDetailsDto = new ContactDetailsDto();
        contactDetailsDto.setAddress(mapAddressModel(contactDetails.getAddress()));
        if (contactDetails.getPhone() != null) contactDetailsDto.setPhone(mapPhoneModel(contactDetails.getPhone()));
        return contactDetailsDto;
    }

    private PhoneDto mapPhoneModel(PhoneModel phone) {

        return new PhoneDto(phone.getPrimary(), phone.getSecondary());
    }

    private AddressDto mapAddressModel(AddressModel address) {
        return new AddressDto(address.getStreet(), address.getPostalCode(), address.getCountry(), address.getCity());
    }


    private QuestionAnswerPairModel mapQuestionAnswerPairDto(QuestionAnswerPairDto questionAnswerPairDto) {
        QuestionAnswerPairModel questionAnswerPairModel = new QuestionAnswerPairModel();

        questionAnswerPairModel.setAnswer(mapAnswerDto(questionAnswerPairDto.getAnswer()));

        return questionAnswerPairModel;
    }

    private QuestionAnswerPairDto mapQuestionAnswerPairModel(QuestionAnswerPairModel questionAnswerPairModel) {
        QuestionAnswerPairDto questionAnswerPairDto = new QuestionAnswerPairDto();

        questionAnswerPairDto.setQuestion(mapQuestionModel(questionAnswerPairModel.getQuestion()));
        questionAnswerPairDto.setAnswer(mapAnswerModel(questionAnswerPairModel.getAnswer()));

        return questionAnswerPairDto;
    }

    private QuestionModel mapQuestionDto(QuestionDto questionDto) {
        QuestionModel questionModel = new QuestionModel();

        questionModel.setLinkId(questionDto.getLinkId());
        questionModel.setText(questionDto.getText());
        questionModel.setAbbreviation(questionDto.getAbbreviation());
        questionModel.setHelperText(questionDto.getHelperText());
        questionModel.setRequired(questionDto.getRequired());
        questionModel.setOptions(questionDto.getOptions());
        questionModel.setQuestionType(questionDto.getQuestionType());

        return questionModel;
    }

    private QuestionDto mapQuestionModel(QuestionModel questionModel) {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setDeprecated(questionModel.isDeprecated());
        questionDto.setLinkId(questionModel.getLinkId());
        questionDto.setText(questionModel.getText());
        questionDto.setAbbreviation(questionModel.getAbbreviation());
        questionDto.setHelperText(questionModel.getHelperText());
        questionDto.setRequired(questionModel.isRequired());
        questionDto.setOptions(questionModel.getOptions());
        questionDto.setQuestionType(questionModel.getQuestionType());
        questionDto.setEnableWhens(questionModel.getEnableWhens());

        if (questionModel.getMeasurementType() != null){
            questionDto.setMeasurementType(mapMeasurementTypeModel(questionModel.getMeasurementType()));
        }

        return questionDto;
    }

    public MeasurementTypeDto mapMeasurementTypeModel(MeasurementTypeModel measurementTypeModel) {
        MeasurementTypeDto measurementTypeDto = new MeasurementTypeDto();

        measurementTypeDto.setSystem(measurementTypeModel.getSystem());
        measurementTypeDto.setCode(measurementTypeModel.getCode());
        measurementTypeDto.setDisplay(measurementTypeModel.getDisplay());

        if (measurementTypeModel.getThreshold() != null) {
            measurementTypeDto.setThreshold(mapThresholdModel(measurementTypeModel.getThreshold()));
        }

        return measurementTypeDto;
    }

    private AnswerModel mapAnswerDto(AnswerDto answerDto) {
        AnswerModel answerModel = new AnswerModel();

        answerModel.setLinkId(answerDto.getLinkId());
        answerModel.setValue(answerDto.getValue());
        answerModel.setAnswerType(answerDto.getAnswerType());

        return answerModel;
    }

    private AnswerDto mapAnswerModel(AnswerModel answerModel) {
        AnswerDto answerDto = new AnswerDto();

        answerDto.setLinkId(answerModel.getLinkId());
        answerDto.setValue(answerModel.getValue());
        answerDto.setAnswerType(answerModel.getAnswerType());

        return answerDto;
    }

    private QuestionnaireWrapperModel mapQuestionnaireWrapperDto(QuestionnaireWrapperDto questionnaireWrapper) {
        QuestionnaireWrapperModel questionnaireWrapperModel = new QuestionnaireWrapperModel();

        questionnaireWrapperModel.setQuestionnaire(mapQuestionnaireDto(questionnaireWrapper.getQuestionnaire()));
        questionnaireWrapperModel.setFrequency(mapFrequencyDto(questionnaireWrapper.getFrequency()));
        questionnaireWrapperModel.setThresholds( questionnaireWrapper.getThresholds().stream().map(this::mapThresholdDto).collect(Collectors.toList()) );

        return questionnaireWrapperModel;
    }

    private QuestionnaireWrapperDto mapQuestionnaireWrapperModel(QuestionnaireWrapperModel questionnaireWrapper) {
        QuestionnaireWrapperDto questionnaireWrapperDto = new QuestionnaireWrapperDto();

        questionnaireWrapperDto.setQuestionnaire(mapQuestionnaireModel(questionnaireWrapper.getQuestionnaire()));
        questionnaireWrapperDto.setFrequency(mapFrequencyModel(questionnaireWrapper.getFrequency()));
        questionnaireWrapperDto.setThresholds( questionnaireWrapper.getThresholds().stream().map(this::mapThresholdModel).collect(Collectors.toList()) );

        return questionnaireWrapperDto;
    }
}
