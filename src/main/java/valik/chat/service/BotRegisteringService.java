package valik.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import valik.chat.bot.ValikChatBot;
import valik.chat.configuration.TelegramSecretsConfig;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.register-bot", havingValue = "true")
public class BotRegisteringService implements SpringLongPollingBot {
    private final TelegramSecretsConfig telegramSecretsConfig;
    private final ValikChatBot bot;

    @Override
    public String getBotToken() {
        return telegramSecretsConfig.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return bot;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }
}
