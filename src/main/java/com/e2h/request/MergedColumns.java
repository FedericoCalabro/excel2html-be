package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MergedColumns {
    private String nameDefinition;
    private ArrayList<String> mergedFrom = new ArrayList<>();
}
