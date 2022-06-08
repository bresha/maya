package com.mindsmiths.GoogleTranslateAdapter;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputDataPayload implements Serializable {
    private String userId;
    private String text;
    private String source;
    private String target;
}