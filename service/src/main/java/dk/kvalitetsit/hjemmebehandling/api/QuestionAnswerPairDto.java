package dk.kvalitetsit.hjemmebehandling.api;

public class QuestionAnswerPairDto {
    private QuestionDto question;
    private AnswerDto answer;

    public QuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(QuestionDto question) {
        this.question = question;
    }

    public AnswerDto getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerDto answer) {
        this.answer = answer;
    }
}
