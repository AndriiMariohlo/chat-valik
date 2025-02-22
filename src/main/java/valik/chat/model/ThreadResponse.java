package valik.chat.model;

import lombok.Data;
import java.util.Map;

@Data
public class ThreadResponse {
    private String id;
    private String object;
    private long created_at;
    private Map<String, Object> metadata;
}