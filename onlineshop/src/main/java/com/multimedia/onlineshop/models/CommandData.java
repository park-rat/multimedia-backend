package com.multimedia.onlineshop.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandData {
    private String commandType;
    private String openAiContext;
    private String processingErrorMessage;
    private String overflowErrorMessage;
}
