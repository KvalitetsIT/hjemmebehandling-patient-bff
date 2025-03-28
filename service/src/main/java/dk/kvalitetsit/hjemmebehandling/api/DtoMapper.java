package dk.kvalitetsit.hjemmebehandling.api;

import dk.kvalitetsit.hjemmebehandling.constants.*;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirUtils;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.Option;
import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.hl7.fhir.r4.model.ResourceType;
import org.openapitools.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DtoMapper {
    public CarePlanModel mapCarePlanDto(CarePlanDto carePlanDto) {
        CarePlanModel carePlanModel = new CarePlanModel();
        mapBaseAttributesToModel(carePlanModel, carePlanDto, ResourceType.CarePlan);

        carePlanDto.getStatus().map(x -> Enum.valueOf(CarePlanStatus.class, x)).ifPresent(carePlanModel::setStatus);

        Optional.ofNullable(carePlanDto.getQuestionnaires()).map(x -> x.stream().map(this::mapQuestionnaireWrapperDto).toList()).ifPresent(carePlanModel::setQuestionnaires);
        Optional.ofNullable(carePlanDto.getPlanDefinitions()).map(x -> x.stream().map(this::mapPlanDefinitionDto).toList()).ifPresent(carePlanModel::setPlanDefinitions);

        carePlanDto.getTitle().ifPresent(carePlanModel::setTitle);
        carePlanDto.getDepartmentName().ifPresent(carePlanModel::setDepartmentName);

        carePlanDto.getCreated().map(OffsetDateTime::toInstant).ifPresent(carePlanModel::setCreated);
        carePlanDto.getStartDate().map(OffsetDateTime::toInstant).ifPresent(carePlanModel::setStartDate);
        carePlanDto.getEndDate().map(OffsetDateTime::toInstant).ifPresent(carePlanModel::setEndDate);

        carePlanDto.getPatientDto().map(this::mapPatientDto).ifPresent(carePlanModel::setPatient);

        carePlanModel.setQuestionnaires(List.of());
        carePlanModel.setPlanDefinitions(List.of());

        return carePlanModel;
    }

    public CarePlanDto mapCarePlanModel(CarePlanModel carePlan) {
        CarePlanDto carePlanDto = new CarePlanDto();

        mapBaseAttributesToDto(carePlanDto, carePlan);
        carePlanDto
                .title(carePlan.getTitle())
                .status(carePlan.getStatus().toString())
                .departmentName(carePlan.getDepartmentName());

        Optional.ofNullable(carePlan.getCreated()).map(this::mapInstant).ifPresent(carePlanDto::created);
        Optional.ofNullable(carePlan.getStartDate()).map(this::mapInstant).ifPresent(carePlanDto::startDate);
        Optional.ofNullable(carePlan.getEndDate()).map(this::mapInstant).ifPresent(carePlanDto::endDate);
        Optional.ofNullable(carePlan.getPatient()).map(this::mapPatientModel).ifPresent(carePlanDto::patientDto);

        Optional.ofNullable(carePlan.getQuestionnaires())
                .map(x -> x.stream().map(this::mapQuestionnaireWrapperModel).collect(Collectors.toList()))
                .ifPresent(carePlanDto::setQuestionnaires);

        Optional.ofNullable(carePlan.getPlanDefinitions())
                .map(x -> x.stream().map(this::mapPlanDefinitionModel).collect(Collectors.toList()))
                .ifPresent(carePlanDto::setPlanDefinitions);

        return carePlanDto;
    }

    private OffsetDateTime mapInstant(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public FrequencyModel mapFrequencyDto(FrequencyDto frequencyDto) {
        FrequencyModel frequencyModel = new FrequencyModel();
        frequencyModel.setWeekdays(frequencyDto.getWeekdays().stream().map(this::mapWeekday).toList());
        frequencyDto.getTimeOfDay().map(LocalTime::parse).ifPresent(frequencyModel::setTimeOfDay);
        return frequencyModel;
    }

    public FrequencyDto mapFrequencyModel(FrequencyModel frequencyModel) {
        FrequencyDto frequencyDto = new FrequencyDto();
        Optional.ofNullable(frequencyModel.getWeekdays()).map(x -> x.stream().map(this::mapWeekday).toList()).ifPresent(frequencyDto::setWeekdays);
        frequencyDto.setTimeOfDay(Optional.ofNullable(frequencyModel.getTimeOfDay()).map(Object::toString));
        return frequencyDto;
    }

    private WeekDayDto mapWeekday(Weekday weekday) {
        return switch (weekday) {
            case MON -> WeekDayDto.MON;
            case TUE -> WeekDayDto.TUE;
            case WED -> WeekDayDto.WED;
            case THU -> WeekDayDto.THU;
            case FRI -> WeekDayDto.FRI;
            case SAT -> WeekDayDto.SAT;
            case SUN -> WeekDayDto.SUN;
        };
    }

    private Weekday mapWeekday(WeekDayDto weekday) {
        return switch (weekday) {
            case MON -> Weekday.MON;
            case TUE -> Weekday.TUE;
            case WED -> Weekday.WED;
            case THU -> Weekday.THU;
            case FRI -> Weekday.FRI;
            case SAT -> Weekday.SAT;
            case SUN -> Weekday.SUN;
        };
    }

    public OrganizationModel mapOrganizationDto(OrganizationDto organizationDto) {
        OrganizationModel organizationModel = new OrganizationModel();

        organizationDto.getName().ifPresent(organizationModel::setName);
        organizationDto.getContactDetails().map(this::mapContactDetailsDto).ifPresent(organizationModel::setContactDetails);

        if (organizationDto.getPhoneHours() != null) {
            organizationModel.setPhoneHours(organizationDto.getPhoneHours()
                    .stream()
                    .map(ph -> {
                        var hours = new PhoneHourModel();
                        hours.setWeekdays(ph.getWeekdays().stream().map(this::mapWeekday).toList());
                        ph.getFrom().map(LocalTime::parse).ifPresent(hours::setFrom);
                        ph.getTo().map(LocalTime::parse).ifPresent(hours::setTo);
                        return hours;
                    })
                    .collect(Collectors.toList()));
        }
        return organizationModel;
    }

    private PhoneModel mapPhoneDto(PhoneDto phone) {
        var model = new PhoneModel();
        phone.getPrimary().ifPresent(model::setPrimary);
        phone.getSecondary().ifPresent(model::setSecondary);
        return model;
    }

    public OrganizationDto mapOrganizationModel(OrganizationModel organizationModel) {
        OrganizationDto organizationDto = new OrganizationDto()
                .id(organizationModel.getId().toString())
                .name(organizationModel.getName())
                .blob(organizationModel.getBlob())
                .contactDetails(mapContactDetailsModel(organizationModel.getContactDetails()));

        Optional.ofNullable(organizationModel.getPhoneHours()).ifPresent(x -> organizationDto.setPhoneHours(x.stream()
                .map(ph -> new PhoneHourDto()
                        .weekdays(ph.getWeekdays().stream().map(this::mapWeekday).toList())
                        .from(ph.getFrom().toString())
                        .to(ph.getTo().toString()))
                .collect(Collectors.toList()))
        );
        return organizationDto;
    }

    public PatientModel mapPatientDto(PatientDto patient) {
        PatientModel patientModel = new PatientModel();
        patient.getCpr().ifPresent(patientModel::setCpr);
        patient.getFamilyName().ifPresent(patientModel::setFamilyName);
        patient.getGivenName().ifPresent(patientModel::setGivenName);
        patient.getContactsDetails().map(this::mapContactDetailsDto).ifPresent(patientModel::setContactDetails);
        patientModel.setContacts(mapPrimaryContactDtos(patient.getPrimaryContacts()));
        return patientModel;
    }

    private List<PrimaryContactModel> mapPrimaryContactDtos(List<PrimaryContactDto> contacts) {
        return contacts.stream().map(this::mapPrimaryContactDto).collect(Collectors.toList());
    }

    private PrimaryContactModel mapPrimaryContactDto(PrimaryContactDto contact) {
        PrimaryContactModel model = new PrimaryContactModel();
        contact.getOrganization().ifPresent(model::setOrganisation);
        contact.getAffiliation().ifPresent(model::setAffiliation);
        contact.getName().ifPresent(model::setName);
        contact.getContactDetails().map(this::mapContactDetailsDto).ifPresent(model::setContactDetails);
        return model;
    }

    public PatientDto mapPatientModel(PatientModel patient) {
        PatientDto patientDto = new PatientDto().cpr(patient.getCpr())
                .familyName(patient.getFamilyName())
                .givenName(patient.getGivenName());
        patientDto.setContactsDetails(Optional.ofNullable(patient.getContactDetails()).map(this::mapContactDetailsModel));
        patientDto.setPrimaryContacts(mapPrimaryContactModels(patient.getContacts()));
        return patientDto;
    }

    private List<PrimaryContactDto> mapPrimaryContactModels(List<PrimaryContactModel> contacts) {
        return contacts.stream().map(this::mapPrimaryContactModel).collect(Collectors.toList());
    }

    private PrimaryContactDto mapPrimaryContactModel(PrimaryContactModel contact) {
        PrimaryContactDto dto = new PrimaryContactDto()
                .name(contact.getName())
                .affiliation(contact.getAffiliation())
                .organization(contact.getOrganisation());
        dto.setContactDetails(Optional.ofNullable(contact.getContactDetails()).map(this::mapContactDetailsModel));
        return dto;
    }

    public PlanDefinitionModel mapPlanDefinitionDto(PlanDefinitionDto planDefinitionDto) {
        PlanDefinitionModel planDefinitionModel = new PlanDefinitionModel();
        mapBaseAttributesToModel(planDefinitionModel, planDefinitionDto, ResourceType.PlanDefinition);

        planDefinitionDto.getName().ifPresent(planDefinitionModel::setName);
        planDefinitionDto.getTitle().ifPresent(planDefinitionModel::setTitle);

        Optional.ofNullable(planDefinitionDto.getQuestionnaires())
                .map(x -> x.stream().map(this::mapQuestionnaireWrapperDto).toList())
                .ifPresent(planDefinitionModel::setQuestionnaires);
        return planDefinitionModel;
    }

    public PlanDefinitionDto mapPlanDefinitionModel(PlanDefinitionModel planDefinitionModel) {
        PlanDefinitionDto planDefinitionDto = new PlanDefinitionDto();
        mapBaseAttributesToDto(planDefinitionDto, planDefinitionModel);
        planDefinitionDto
                .name(planDefinitionModel.getName())
                .title(planDefinitionModel.getTitle());
        Optional.ofNullable(planDefinitionModel.getQuestionnaires())
                .map(x -> x.stream().map(this::mapQuestionnaireWrapperModel).toList())
                .ifPresent(planDefinitionDto::setQuestionnaires);
        return planDefinitionDto;
    }

    public ThresholdModel mapThresholdDto(ThresholdDto thresholdDto) {
        ThresholdModel thresholdModel = new ThresholdModel();
        thresholdDto.getQuestionId().ifPresent(thresholdModel::setQuestionnaireItemLinkId);
        thresholdDto.getType().map(this::mapThresholdTypeDto).ifPresent(thresholdModel::setType);
        thresholdDto.getValueBoolean().ifPresent(thresholdModel::setValueBoolean);
        thresholdDto.getValueQuantityLow().ifPresent(thresholdModel::setValueQuantityLow);
        thresholdDto.getValueQuantityHigh().ifPresent(thresholdModel::setValueQuantityHigh);
        return thresholdModel;
    }

    private ThresholdType mapThresholdTypeDto(ThresholdDto.TypeEnum x) {
        return switch (x) {
            case NORMAL -> ThresholdType.NORMAL;
            case ABNORMAL -> ThresholdType.ABNORMAL;
            case CRITICAL -> ThresholdType.CRITICAL;
        };
    }

    public ThresholdDto mapThresholdModel(ThresholdModel thresholdModel) {
        return new ThresholdDto()
                .questionId(thresholdModel.getQuestionnaireItemLinkId())
                .conceptCode(thresholdModel.getConceptCode())
                .type(mapThresholdTypeModel(thresholdModel.getType()))
                .valueBoolean(thresholdModel.getValueBoolean())
                .valueQuantityLow(thresholdModel.getValueQuantityLow())
                .valueQuantityHigh(thresholdModel.getValueQuantityHigh());
    }

    private ThresholdDto.TypeEnum mapThresholdTypeModel(ThresholdType type) {
        return switch (type) {
            case NORMAL -> ThresholdDto.TypeEnum.NORMAL;
            case ABNORMAL -> ThresholdDto.TypeEnum.ABNORMAL;
            case CRITICAL -> ThresholdDto.TypeEnum.CRITICAL;
        };
    }

    public QuestionnaireModel mapQuestionnaireDto(QuestionnaireDto questionnaireDto) {
        QuestionnaireModel questionnaireModel = new QuestionnaireModel();
        mapBaseAttributesToModel(questionnaireModel, questionnaireDto, ResourceType.Questionnaire);
        questionnaireDto.getTitle().ifPresent(questionnaireModel::setTitle);
        questionnaireDto.getStatus().ifPresent(questionnaireModel::setStatus);
        Optional.ofNullable(questionnaireDto.getQuestions()).map(x -> x.stream().map(this::mapQuestionDto).toList()).ifPresent(questionnaireModel::setQuestions);
        return questionnaireModel;
    }

    public QuestionnaireDto mapQuestionnaireModel(QuestionnaireModel questionnaireModel) {
        QuestionnaireDto questionnaireDto = new QuestionnaireDto();
        mapBaseAttributesToDto(questionnaireDto, questionnaireModel);

        questionnaireDto
                .title(questionnaireModel.getTitle())
                .status(questionnaireModel.getStatus())
                .blob(questionnaireModel.getBlob());

        Optional.ofNullable(questionnaireModel.getQuestions())
                .map(x -> x.stream().map(this::mapQuestionModel).toList())
                .ifPresent(questionnaireDto::setQuestions);

        return questionnaireDto;
    }

    public QuestionnaireResponseModel mapQuestionnaireResponseDto(QuestionnaireResponseDto questionnaireResponseDto) {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();
        mapBaseAttributesToModel(questionnaireResponseModel, questionnaireResponseDto, ResourceType.QuestionnaireResponse);
        questionnaireResponseDto.getQuestionnaireId().map(x -> this.toQualifiedId(x, ResourceType.Questionnaire)).ifPresent(questionnaireResponseModel::setQuestionnaireId);
        questionnaireResponseDto.getCarePlanId().map(x -> this.toQualifiedId(x, ResourceType.CarePlan)).ifPresent(questionnaireResponseModel::setCarePlanId);
        questionnaireResponseModel.setQuestionnaireName(questionnaireResponseModel.getQuestionnaireName());
        questionnaireResponseModel.setQuestionAnswerPairs(questionnaireResponseDto.getQuestionAnswerPairs().stream().map(this::mapQuestionAnswerPairDto).collect(Collectors.toList()));
        return questionnaireResponseModel;
    }

    public QuestionnaireResponseDto mapQuestionnaireResponseModel(QuestionnaireResponseModel questionnaireResponseModel) {
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();
        mapBaseAttributesToDto(questionnaireResponseDto, questionnaireResponseModel);
        questionnaireResponseDto
                .questionnaireId(questionnaireResponseModel.getQuestionnaireId().toString())
                .carePlanId(questionnaireResponseModel.getCarePlanId().toString())
                .questionnaireName(questionnaireResponseModel.getQuestionnaireName());

        Optional.ofNullable(questionnaireResponseModel.getExaminationStatus()).map(this::mapExaminationStatusModel).ifPresent(questionnaireResponseDto::examinationStatus);
        Optional.ofNullable(questionnaireResponseModel.getTriagingCategory()).map(this::mapTriagingModel).ifPresent(questionnaireResponseDto::triagingCategory);
        Optional.ofNullable((questionnaireResponseModel.getAnswered())).map(x -> x.atOffset(ZoneOffset.UTC)).ifPresent(questionnaireResponseDto::answered);
        Optional.ofNullable((questionnaireResponseModel.getExamined())).map(x -> x.atOffset(ZoneOffset.UTC)).ifPresent(questionnaireResponseDto::examined);

        questionnaireResponseDto.setQuestionAnswerPairs(questionnaireResponseModel.getQuestionAnswerPairs().stream().map(this::mapQuestionAnswerPairModel).collect(Collectors.toList()));
        questionnaireResponseDto.setPatient(Optional.ofNullable(questionnaireResponseModel.getPatient()).map(this::mapPatientModel));
        return questionnaireResponseDto;
    }

    private QuestionnaireResponseDto.TriagingCategoryEnum mapTriagingModel(TriagingCategory triagingCategory) {
        return switch (triagingCategory) {
            case RED -> QuestionnaireResponseDto.TriagingCategoryEnum.RED;
            case YELLOW -> QuestionnaireResponseDto.TriagingCategoryEnum.YELLOW;
            case GREEN -> QuestionnaireResponseDto.TriagingCategoryEnum.GREEN;
        };
    }

    private QuestionnaireResponseDto.ExaminationStatusEnum mapExaminationStatusModel(ExaminationStatus examinationStatus) {
        return switch (examinationStatus) {
            case NOT_EXAMINED -> QuestionnaireResponseDto.ExaminationStatusEnum.NOT_EXAMINED;
            case UNDER_EXAMINATION -> QuestionnaireResponseDto.ExaminationStatusEnum.UNDER_EXAMINATION;
            case EXAMINED -> QuestionnaireResponseDto.ExaminationStatusEnum.EXAMINED;
        };
    }

    private void mapBaseAttributesToModel(BaseModel target, BaseDto source, ResourceType resourceType) {
        if (source.getId().isEmpty()) {
            // OK, in case a resource is being created.
            return;
        }

        target.setId(toQualifiedId(source.getId().get(), resourceType));
    }

    private QualifiedId toQualifiedId(String id, ResourceType resourceType) {
        if (FhirUtils.isPlainId(id)) {
            return new QualifiedId(id, resourceType);
        } else if (FhirUtils.isQualifiedId(id, resourceType)) {
            return new QualifiedId(id);
        } else {
            throw new IllegalArgumentException(String.format("Illegal id provided for resource of type %s: %s!", resourceType.toString(), id));
        }
    }

    private void mapBaseAttributesToDto(BaseDto target, BaseModel source) {
        target.setId(Optional.ofNullable(source.getId()).map(Object::toString));
        target.setOrganizationId(Optional.ofNullable(source.getOrganizationId()));
    }

    private ContactDetailsModel mapContactDetailsDto(ContactDetailsDto contactDetails) {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();
        contactDetails.getAddress().map(this::mapAddressDto).ifPresent(contactDetailsModel::setAddress);
        contactDetails.getPhone().map(this::mapPhoneDto).ifPresent(contactDetailsModel::setPhone);
        return contactDetailsModel;
    }

    private AddressModel mapAddressDto(AddressDto address) {
        var model = new AddressModel();
        address.getStreet().ifPresent(model::setStreet);
        address.getPostalCode().ifPresent(model::setPostalCode);
        address.getCountry().ifPresent(model::setCountry);
        address.getCity().ifPresent(model::setCity);
        return model;
    }

    public ContactDetailsDto mapContactDetailsModel(ContactDetailsModel contactDetails) {
        ContactDetailsDto contactDetailsDto = new ContactDetailsDto()
                .address(mapAddressModel(contactDetails.getAddress()));

        contactDetailsDto.setPhone(Optional.ofNullable(contactDetails.getPhone()).map(this::mapPhoneModel));
        return contactDetailsDto;
    }

    private PhoneDto mapPhoneModel(PhoneModel phone) {
        return new PhoneDto()
                .primary(phone.getPrimary())
                .secondary(phone.getSecondary());
    }

    private AddressDto mapAddressModel(AddressModel address) {
        return new AddressDto()
                .street(address.getStreet())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .city(address.getCity());
    }


    private QuestionAnswerPairModel mapQuestionAnswerPairDto(QuestionAnswerPairDto questionAnswerPairDto) {
        QuestionAnswerPairModel questionAnswerPairModel = new QuestionAnswerPairModel();
        questionAnswerPairDto.getAnswer().map(this::mapAnswerDto).ifPresent(questionAnswerPairModel::setAnswer);
        ;
        return questionAnswerPairModel;
    }

    private QuestionAnswerPairDto mapQuestionAnswerPairModel(QuestionAnswerPairModel questionAnswerPairModel) {
        return new QuestionAnswerPairDto()
                .question(mapQuestionModel(questionAnswerPairModel.getQuestion()))
                .answer(mapAnswerModel(questionAnswerPairModel.getAnswer()));
    }

    private QuestionModel mapQuestionDto(QuestionDto questionDto) {
        QuestionModel questionModel = new QuestionModel();
        questionDto.getLinkId().ifPresent(questionModel::setLinkId);
        questionDto.getText().ifPresent(questionModel::setText);
        questionDto.getAbbreviation().ifPresent(questionModel::setAbbreviation);
        questionDto.getHelperText().ifPresent(questionModel::setHelperText);
        questionDto.getRequired().ifPresent(questionModel::setRequired);
        questionModel.setOptions(questionDto.getOptions().stream().map(this::mapOptionDto).toList());
        questionDto.getQuestionType().map(this::mapQuestionTypeDto).ifPresent(questionModel::setQuestionType);
        return questionModel;
    }

    private Option mapOptionDto(org.openapitools.model.Option option) {
        var model = new Option();
        option.getOption().ifPresent(model::setOption);
        option.getComment().ifPresent(model::setComment);
        return model;
    }

    private QuestionType mapQuestionTypeDto(QuestionDto.QuestionTypeEnum questionTypeEnum) {
        return switch (questionTypeEnum) {
            case CHOICE -> QuestionType.CHOICE;
            case INTEGER -> QuestionType.INTEGER;
            case QUANTITY -> QuestionType.QUANTITY;
            case STRING -> QuestionType.STRING;
            case BOOLEAN -> QuestionType.BOOLEAN;
            case DISPLAY -> QuestionType.DISPLAY;
            case GROUP -> QuestionType.GROUP;
        };
    }

    private QuestionDto mapQuestionModel(QuestionModel questionModel) {
        QuestionDto questionDto = new QuestionDto().deprecated(questionModel.isDeprecated())
                .linkId(questionModel.getLinkId())
                .text(questionModel.getText())
                .abbreviation(questionModel.getAbbreviation())
                .helperText(questionModel.getHelperText())
                .required(questionModel.isRequired());

        Optional.ofNullable(questionModel.getEnableWhens()).map(x -> x.stream().map(this::mapEnableWhenModel).toList()).ifPresent(questionDto::enableWhens);

        Optional.ofNullable(questionModel.getQuestionType())
                .map(this::mapQuestionTypeModel)
                .ifPresent(questionDto::questionType);

        Optional.ofNullable(questionModel.getOptions())
                .map(x -> x.stream().map(this::mapOptionModel).toList())
                .ifPresent(questionDto::options);

        questionDto.setMeasurementType(Optional.ofNullable(questionModel.getMeasurementType()).map(this::mapMeasurementTypeModel));

        if (questionModel.getQuestionType() == QuestionType.GROUP && questionModel.getSubQuestions() != null) {
            questionDto.setSubQuestions(questionModel.getSubQuestions().stream().map(this::mapQuestionModel).collect(Collectors.toList()));
        }

        return questionDto;
    }

    private EnableWhen mapEnableWhenModel(QuestionModel.EnableWhen x) {
        return new EnableWhen()
                .operator(this.mapEnableWhenOperatorModel(x.getOperator()))
                .answer(this.mapAnswerModel(x.getAnswer()));
    }

    private EnableWhen.OperatorEnum mapEnableWhenOperatorModel(EnableWhenOperator operator) {
        return switch (operator) {
            case EQUAL -> EnableWhen.OperatorEnum.EQUAL;
            case GREATER_THAN -> EnableWhen.OperatorEnum.GREATER_THAN;
            case LESS_THAN -> EnableWhen.OperatorEnum.LESS_THAN;
            case GREATER_OR_EQUAL -> EnableWhen.OperatorEnum.GREATER_OR_EQUAL;
            case LESS_OR_EQUAL -> EnableWhen.OperatorEnum.LESS_OR_EQUAL;
        };
    }

    private QuestionDto.QuestionTypeEnum mapQuestionTypeModel(QuestionType questionType) {
        return switch (questionType) {
            case CHOICE -> QuestionDto.QuestionTypeEnum.CHOICE;
            case INTEGER -> QuestionDto.QuestionTypeEnum.INTEGER;
            case QUANTITY -> QuestionDto.QuestionTypeEnum.QUANTITY;
            case STRING -> QuestionDto.QuestionTypeEnum.STRING;
            case BOOLEAN -> QuestionDto.QuestionTypeEnum.BOOLEAN;
            case DISPLAY -> QuestionDto.QuestionTypeEnum.DISPLAY;
            case GROUP -> QuestionDto.QuestionTypeEnum.GROUP;
        };
    }

    private org.openapitools.model.Option mapOptionModel(Option x) {
        return new org.openapitools.model.Option()
                .option(x.getOption())
                .comment(x.getComment());
    }

    public MeasurementTypeDto mapMeasurementTypeModel(MeasurementTypeModel measurementTypeModel) {
        MeasurementTypeDto measurementTypeDto = new MeasurementTypeDto()
                .system(measurementTypeModel.getSystem())
                .code(measurementTypeModel.getCode())
                .display(measurementTypeModel.getDisplay());

        measurementTypeDto.setThreshold(Optional.ofNullable(measurementTypeModel.getThreshold()).map(this::mapThresholdModel));

        return measurementTypeDto;
    }

    private AnswerModel mapAnswerDto(AnswerDto answerDto) {
        AnswerModel answerModel = new AnswerModel();

        answerDto.getLinkId().ifPresent(answerModel::setLinkId);
        answerDto.getValue().ifPresent(answerModel::setValue);
        answerDto.getAnswerType().map(this::mapAnswerType).ifPresent(answerModel::setAnswerType);

        answerDto.getAnswerType().ifPresent(answerType -> {
            if (answerType == AnswerDto.AnswerTypeEnum.GROUP && answerDto.getSubAnswers() != null) {
                answerModel.setSubAnswers(answerDto.getSubAnswers().stream().map(this::mapAnswerDto).collect(Collectors.toList()));
            }
        });

        return answerModel;
    }

    private AnswerType mapAnswerType(AnswerDto.AnswerTypeEnum answerTypeEnum) {
        return switch (answerTypeEnum) {
            case INTEGER -> AnswerType.INTEGER;
            case STRING -> AnswerType.STRING;
            case BOOLEAN -> AnswerType.BOOLEAN;
            case QUANTITY -> AnswerType.QUANTITY;
            case GROUP -> AnswerType.GROUP;
        };
    }

    private AnswerDto mapAnswerModel(AnswerModel answerModel) {
        AnswerDto answerDto = new AnswerDto()
                .linkId(answerModel.getLinkId())
                .value(answerModel.getValue())
                .answerType(this.mapAnswerTypeModel(answerModel.getAnswerType()));

        if (answerModel.getAnswerType() == AnswerType.GROUP && answerModel.getSubAnswers() != null) {
            answerDto.setSubAnswers(answerModel.getSubAnswers().stream().map(this::mapAnswerModel).collect(Collectors.toList()));
        }
        return answerDto;
    }

    private AnswerDto.AnswerTypeEnum mapAnswerTypeModel(AnswerType answerType) {
        return switch (answerType) {
            case INTEGER -> AnswerDto.AnswerTypeEnum.INTEGER;
            case STRING -> AnswerDto.AnswerTypeEnum.STRING;
            case BOOLEAN -> AnswerDto.AnswerTypeEnum.BOOLEAN;
            case QUANTITY -> AnswerDto.AnswerTypeEnum.QUANTITY;
            case GROUP -> AnswerDto.AnswerTypeEnum.GROUP;
        };
    }

    private QuestionnaireWrapperModel mapQuestionnaireWrapperDto(QuestionnaireWrapperDto questionnaireWrapper) {
        QuestionnaireWrapperModel questionnaireWrapperModel = new QuestionnaireWrapperModel();
        questionnaireWrapper.getQuestionnaire().map(this::mapQuestionnaireDto).ifPresent(questionnaireWrapperModel::setQuestionnaire);
        questionnaireWrapper.getFrequency().map(this::mapFrequencyDto).ifPresent(questionnaireWrapperModel::setFrequency);
        ;
        questionnaireWrapperModel.setThresholds(questionnaireWrapper.getThresholds().stream().map(this::mapThresholdDto).collect(Collectors.toList()));
        return questionnaireWrapperModel;
    }

    private QuestionnaireWrapperDto mapQuestionnaireWrapperModel(QuestionnaireWrapperModel questionnaireWrapper) {
        QuestionnaireWrapperDto questionnaireWrapperDto = new QuestionnaireWrapperDto();
        Optional.ofNullable(questionnaireWrapper.getQuestionnaire()).map(this::mapQuestionnaireModel).ifPresent(questionnaireWrapperDto::questionnaire);
        Optional.ofNullable(questionnaireWrapper.getFrequency()).map(this::mapFrequencyModel).ifPresent(questionnaireWrapperDto::frequency);
        questionnaireWrapperDto.setThresholds(questionnaireWrapper.getThresholds().stream().map(this::mapThresholdModel).toList());
        return questionnaireWrapperDto;
    }
}
