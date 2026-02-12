package com.marv.arionwallet.modules.auth.infrastructure;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "arionwallet.bootstrap.admin")
@Setter
@Getter
public class AdminBootstrapProperties {

    private String email;
}
