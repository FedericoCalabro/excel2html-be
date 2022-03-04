package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class AggregationRow {
    private String targetCol;
    private String blockedCol;
    private ArrayList<String> op;
}
