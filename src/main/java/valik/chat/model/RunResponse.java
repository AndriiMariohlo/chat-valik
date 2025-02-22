package valik.chat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RunResponse {
    private String id;
    private String status;
    @JsonProperty("thread_id")
    private String threadId;
    @JsonProperty("assistant_id")
    private String assistantId;
    private Map<String, Object> metadata;
}