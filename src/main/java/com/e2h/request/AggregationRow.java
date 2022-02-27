package com.e2h.request;

import lombok.Data;

@Data
public class AggregationRow {
    private String targetCol;
    private String blockedCol;
    private String op;
}
