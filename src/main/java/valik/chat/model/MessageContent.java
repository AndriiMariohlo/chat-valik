package valik.chat.model;

import lombok.Data;

@Data
public class MessageContent {
    private String type;
    private TextContent text;
}