package valik.chat.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class ValikChatBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    private void sendResponse(List<? extends PartialBotApiMethod<? extends Serializable>> botApiMethods) {
        log.debug("Total response messages size: {}", botApiMethods.size());
        botApiMethods.forEach(method -> {
            try {
                log.debug("Sending response: {}", method);
                execute(method);
            } catch (TelegramApiException e) {
                log.error("Error sending message: {}, reason: {}", method, e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private void execute(PartialBotApiMethod<? extends Serializable> method) throws TelegramApiException {
        if (method instanceof BotApiMethod<? extends Serializable> botApiMethod) {
            telegramClient.execute(botApiMethod);
        }
    }

    @Override
    public void consume(Update update) {
        //TODO for now just send a response with user input
        SendMessage response = SendMessage.builder().text(update.getMessage().getText()).chatId(update.getMessage().getChatId()).build();
        sendResponse(List.of(response));
    }

}

