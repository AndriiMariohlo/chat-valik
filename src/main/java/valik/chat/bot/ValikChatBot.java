package valik.chat.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import valik.chat.service.CitationParserService;

import java.io.Serializable;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.containsAnyIgnoreCase;

@RequiredArgsConstructor
@Slf4j
@Component
public class ValikChatBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final CitationParserService citationParserService;
    private boolean justSent;
    private Integer justSentMessageId;

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
        if (method instanceof SendMessage sm) {
            Message execute = telegramClient.execute(sm);
            justSentMessageId = execute.getMessageId();
        } else if (method instanceof BotApiMethod<? extends Serializable> botApiMethod) {
            telegramClient.execute(botApiMethod);
        }
    }

    @Override
    public void consume(Update update) {
        checkSosal(update);

        int i = RandomUtils.secure().randomInt(0, 7);
        if (i == 6) {
            sendLikeEmoji(update);
        }

        log.trace("Received update: {}", update);
        if (justSent) {
            sendLikeEmoji(update);
            justSent = false;
        }

        if (update.hasMessage() && update.getMessage().hasText()
            && contains(update, "ебани", "ебанешь", "ебануть", "выкати", "выкатишь", "выкатить", "захуярь", "захуяришь", "захуярить", "выдави", "выдавишь", "выдавить", "скажи", "пиздани")
            && contains(update, "цитату", "цитатку", "цитаточку")
        ) {
            sendMessages(update.getMessage().getChatId());
        }
    }

    private void checkSosal(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            checkSosalReply(update);
            checkSosalNext(update);
        }
    }

    private void checkSosalReply(Update update) {
        Message replyToMessage = update.getMessage().getReplyToMessage();
        if (replyToMessage != null) {
            Boolean isBotReply = replyToMessage.getFrom().getIsBot();
            Integer replyMessageId = replyToMessage.getMessageId();
            boolean isYes = update.getMessage().getText().equalsIgnoreCase("да");

            if (isBotReply && isYes) {
                EditMessageText editMessage = EditMessageText.builder()
                        .chatId(update.getMessage().getChatId())
                        .messageId(replyMessageId)
                        .text("Сосал?")
                        .build();
                SendMessage sm = SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text("понял понял")
                        .build();
                sendResponse(List.of(editMessage, sm));
            }
        }
    }

    private void checkSosalNext(Update update) {
        boolean isYes = update.getMessage().getText().equalsIgnoreCase("да");
        if (justSent && isYes) {
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(update.getMessage().getChatId())
                    .messageId(justSentMessageId)
                    .text("Сосал?")
                    .build();
            SendMessage sm = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("понял понял")
                    .build();
            sendResponse(List.of(editMessage, sm));
        }
    }

    private void sendLikeEmoji(Update update) {
        SetMessageReaction emoji = SetMessageReaction.builder().chatId(update.getMessage().getChatId()).messageId(update.getMessage().getMessageId()).reactionTypes(List.of(new ReactionTypeEmoji("emoji", "\uD83D\uDC4D"))).build();
        sendResponse(List.of(emoji));
    }

    private static boolean contains(Update update, String... str) {
        return containsAnyIgnoreCase(update.getMessage().getText(), str);
    }

    public void sendMessages(Long chatId) {
        SendMessage response = SendMessage.builder().text(citationParserService.getRandomCitation()).chatId(chatId).build();
        sendResponse(List.of(response));
        log.debug("Message sent: {}", response.getText());
        justSent = true;
    }

}

