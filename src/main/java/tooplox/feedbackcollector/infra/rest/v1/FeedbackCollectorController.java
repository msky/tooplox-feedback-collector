package tooplox.feedbackcollector.infra.rest.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tooplox.feedbackcollector.domain.FeedbackCollectorFacade;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SendMessageCommand;
import tooplox.feedbackcollector.domain.queries.ReadInboxQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxInformationQuery;
import tooplox.shared.domain.InboxId;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/inboxes")
@RequiredArgsConstructor
public class FeedbackCollectorController {
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
    public ResponseEntity<?> showInboxInformation(@RequestParam(value = "id", required = false) String inboxId) {
        return feedbackCollectorFacade.showInboxInformation(new ShowInboxInformationQuery(inboxId == null ? null : new InboxId(inboxId)))
                .fold(
                        this::getFailureResponseEntity,
                        ok()::body
                );
    }

    @PostMapping("/{inboxId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable String inboxId,
            @RequestBody SendMessageRequest request) {
        return feedbackCollectorFacade.sendMessage(
                new SendMessageCommand(
                        new InboxId(inboxId),
                        request.content()
                )).fold(
                this::getFailureResponseEntity,
                _ -> ResponseEntity.ok().build()
        );
    }

    @GetMapping("/{inboxId}/messages")
    public ResponseEntity<?> showMessage(@PathVariable(required = false) String inboxId) {
        return feedbackCollectorFacade.readInbox(new ReadInboxQuery(inboxId == null ? null : new InboxId(inboxId)))
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

    public record CreateInboxRequest(LocalDateTime expirationDate, boolean allowsAnonymousMessage, String topic) {
        public CreateInboxCommand toCommand() {
            return new CreateInboxCommand(expirationDate, allowsAnonymousMessage, topic);
        }
    }

    public record SendMessageRequest(String content) {
    }
}





