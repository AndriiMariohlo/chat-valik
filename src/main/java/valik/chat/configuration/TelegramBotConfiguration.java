package valik.chat.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TelegramBotConfiguration {
    private final TelegramSecretsConfig telegramSecretsConfig;

    @Bean
    public TelegramClient telegramClient() {
        String token = telegramSecretsConfig.getToken();
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Telegram token not set, please set the TELEGRAM_BOT_TOKEN variable");
        }
        return new OkHttpTelegramClient(token);
    }
}
