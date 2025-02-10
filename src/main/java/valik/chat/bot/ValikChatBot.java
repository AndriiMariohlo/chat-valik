package valik.chat.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import valik.chat.service.CitationParserService;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class ValikChatBot implements LongPollingSingleThreadUpdateConsumer {

    public static final String TEST_GROUP_ID = "-4620833741";
    private final TelegramClient telegramClient;
    private final CitationParserService citationParserService;
    private boolean justSent;

    private void sendResponse(List<? extends PartialBotApiMethod<? extends Serializable>> botApiMethods) {
        log.debug("Total response messages size: {}", botApiMethods.size());
        botApiMethods.forEach(method -> {
            try {
                log.trace("Sending response: {}", method);
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

        log.trace("Received update: {}", update);
        if (justSent) {
            SetMessageReaction emoji = SetMessageReaction.builder().chatId(update.getMessage().getChatId()).messageId(update.getMessage().getMessageId()).reactionTypes(List.of(new ReactionTypeEmoji("emoji", "\uD83D\uDC4D"))).build();
            sendResponse(List.of(emoji));
            justSent = false;
        }

        if (update.getMessage().hasText() && StringUtils.containsIgnoreCase(update.getMessage().getText(), "ебани")
            && (StringUtils.containsIgnoreCase(update.getMessage().getText(), "цитату") || StringUtils.containsIgnoreCase(update.getMessage().getText(), "цитатку"))) {
            sendMessages();
        }
    }

    public void sendMessages() {
        SendMessage response = SendMessage.builder().text(citationParserService.getRandomCitation()).chatId(TEST_GROUP_ID).build();
        sendResponse(List.of(response));
        log.debug("Message sent: {}", response.getText());
        justSent = true;
    }

}

