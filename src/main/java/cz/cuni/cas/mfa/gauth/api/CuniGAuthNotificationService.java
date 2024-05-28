package cz.cuni.cas.mfa.gauth.api;

import lombok.*;

public interface CuniGAuthNotificationService {

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class NotificationRequest {
        @NonNull
        protected final String channelId;
        @NonNull
        protected final String principalId;
        protected final String application;
        protected String name;
        protected String email;
        protected String ip_address;
        protected String browser;
        protected String requested_at;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class NotificationResponse {
        protected String title;
        protected String message;
        protected Integer code;
        protected String status;
    }

    public NotificationResponse sendNotificationRequest(NotificationRequest request);

    public NotificationResponse sendConfirmationRequest(String channelId);
}
