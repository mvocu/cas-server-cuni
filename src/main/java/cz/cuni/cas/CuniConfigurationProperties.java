package cz.cuni.cas;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;


@ConfigurationProperties(value="cuni")
@Getter
@Setter
public class CuniConfigurationProperties {


}
