package cz.cuni.cas.mfa.gauth.config;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CuniGauthConfigurationProperties")
public class CuniGAuthConfigurationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1356649641265489870L;

    /**
     *
     */
    private String notification_url = "https://localhost/notification_request";

    /**
     *
     */
    private String confirmation_url = "https://localhost/confirmation_request";

    /**
     *
     */
    private String token = "";

    /**
     *
     */
    private String name_attribute = "";

    /**
     *
     */
    private String email_attribute = "";
}
