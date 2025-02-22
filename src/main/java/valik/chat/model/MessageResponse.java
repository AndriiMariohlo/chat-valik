package valik.chat.model;

import lombok.Data;
import java.util.List;

@Data
public class MessageResponse {
    private String id;
    private String role;
    private List<MessageContent> content;
}