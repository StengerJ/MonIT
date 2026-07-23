package com.monit.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckResult {
    private String name;
    private String type;
    private CheckStatus status;
    private String message;
}
