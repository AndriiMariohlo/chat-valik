package valik.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import valik.chat.model.*;

import java.time.Duration;
import java.util.Map;

@Service
public class OpenAiService {

    private final WebClient webClient;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.assistant-id}")
    private String assistantId;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/").build();
    }

    public String getResponse(String userMessage) {
        try {
            ThreadResponse threadResponse = webClient.post()
                    .uri("threads")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .bodyValue(Map.of())
                    .retrieve()
                    .bodyToMono(ThreadResponse.class)
                    .block();

            if (threadResponse == null || threadResponse.getId() == null) {
                return "Ошибка: не удалось создать тред.";
            }

            String threadId = threadResponse.getId();

            webClient.post()
                    .uri("threads/" + threadId + "/messages")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .bodyValue(Map.of(
                            "role", "user",
                            "content", userMessage
                    ))
                    .retrieve()
                    .bodyToMono(MessageResponse.class)
                    .block();

            RunResponse runResponse = webClient.post()
                    .uri("threads/" + threadId + "/runs")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .bodyValue(Map.of(
                            "assistant_id", assistantId
                    ))
                    .retrieve()
                    .bodyToMono(RunResponse.class)
                    .block();

            if (runResponse == null || runResponse.getId() == null) {
                return "Ошибка: не удалось запустить ассистента.";
            }

            String runId = runResponse.getId();

            webClient.get()
                    .uri("threads/" + threadId + "/runs/" + runId)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .retrieve()
                    .bodyToMono(RunResponse.class)
                    .repeat()
                    .takeUntil(status -> "completed".equals(status.getStatus()))
                    .timeout(Duration.ofSeconds(30))
                    .last()
                    .block();

            ResponseWrapper messagesResponse = webClient.get()
                    .uri("threads/" + threadId + "/messages")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();

            if (messagesResponse == null || messagesResponse.getData() == null) {
                return "Ошибка: не удалось получить ответ ассистента.";
            }

            return messagesResponse.getData().stream()
                    .filter(message -> "assistant".equals(message.getRole()))
                    .findFirst()
                    .flatMap(message -> message.getContent().stream()
                            .filter(content -> "text".equals(content.getType()))
                            .map(content -> content.getText().getValue())
                            .findFirst())
                    .orElse("Ошибка: ассистент не ответил.");

        } catch (WebClientResponseException e) {
            System.err.println("Ошибка при вызове OpenAI API: " + e.getResponseBodyAsString());
            return "Ошибка OpenAI: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при обращении к OpenAI Assistant API.";
        }
    }
}