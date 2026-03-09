package tooplox.feedbackcollector.infra.rest.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tooplox.feedbackcollector.domain.FeedbackCollectorFacade;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.queries.ShowFeedbackQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.shared.domain.InboxId;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/inboxes")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackCollectorFacade feedbackCollectorFacade;

    @PostMapping()
    public ResponseEntity<?> createInbox(@RequestBody CreateInboxRequest request) {
        return feedbackCollectorFacade.createInbox(request.toCommand())
                .fold(
                        this::getFailureResponseEntity,
                        ok()::body
                );
    }

    @GetMapping()
    public ResponseEntity<?> showInbox(@RequestParam(value = "id", required = false) String inboxId) {
        return feedbackCollectorFacade.showInbox(new ShowInboxQuery(inboxId == null ? null : new InboxId(inboxId)))
                .fold(
                        this::getFailureResponseEntity,
                        ok()::body
                );
    }

    @PostMapping("/{inboxId}/messages")
    public ResponseEntity<?> submitFeedback(
            @PathVariable String inboxId,
            @RequestBody SubmitFeedbackRequest request) {
        return feedbackCollectorFacade.submitFeedback(
                new SubmitFeedbackCommand(
                        new InboxId(inboxId),
                        request.content()
                )).fold(
                this::getFailureResponseEntity,
                _ -> ResponseEntity.ok().build()
        );
    }

    @GetMapping("/{inboxId}/messages")
    public ResponseEntity<?> showFeedback(@PathVariable(required = false) String inboxId) {
        return feedbackCollectorFacade.showFeedback(new ShowFeedbackQuery(inboxId == null ? null : new InboxId(inboxId)))
                .fold(
                        this::getFailureResponseEntity,
                        ok()::body
                );
    }

    private ResponseEntity<FailureResponse> getFailureResponseEntity(Object failure) {
        return ResponseEntity.status(statusFor(failure))
                .body(FailureResponse.from(failure.getClass()));
    }

    private HttpStatus statusFor(Object failure) {
        if (failure.getClass().getSimpleName().contains("NotFound")) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }

    public record CreateInboxRequest(LocalDateTime expirationDate, boolean allowsAnonymousFeedback) {
        public CreateInboxCommand toCommand() {
            return new CreateInboxCommand(expirationDate, allowsAnonymousFeedback);
        }
    }

    public record SubmitFeedbackRequest(String content) {
    }
}





