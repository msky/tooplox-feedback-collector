package tooplox.feedbackcollector.infra.rest.v1;

public record FailureResponse(String type) {
    public static FailureResponse from(Class failureClass) {
        return new FailureResponse(failureClass.getSimpleName());
    }
}

