package valik.chat.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class CitationParserService {

    private final List<String> citations = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        ClassPathResource resource = new ClassPathResource("citations/valik.txt");
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    citations.add(line);
                }
            }
            log.info("Загружено {} цитат", citations.size());
        } catch (IOException e) {
            log.error("Ошибка при чтении файла цитат", e);
        }
    }

    public String getRandomCitation() {
        if (citations.isEmpty()) {
            return "Нет цитат для отображения";
        }
        int index = random.nextInt(citations.size());
        return citations.get(index);
    }
}
