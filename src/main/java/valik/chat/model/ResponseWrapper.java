package valik.chat.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseWrapper {
    private List<MessageResponse> data;
}