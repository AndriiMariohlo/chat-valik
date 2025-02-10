package valik.chat.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telegram")
@Data
public class TelegramSecretsConfig {
    private String token;
    private String username;
}
