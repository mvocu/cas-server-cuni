package cz.cuni.cas;

import com.fasterxml.jackson.annotation.JsonFilter;
import cz.cuni.cas.mfa.gauth.config.CuniGAuthConfigurationProperties;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;


@ConfigurationProperties(value="cuni")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CuniConfigurationProperties")
public class CuniConfigurationProperties implements Serializable {

    public static final String PREFIX = "cuni";

    @Serial
    private static final long serialVersionUID = -1571593685424325416L;

    @NestedConfigurationProperty
    private CuniGAuthConfigurationProperties gauth = new CuniGAuthConfigurationProperties();

}
