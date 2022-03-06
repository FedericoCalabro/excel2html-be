package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CardHeaderConfig {
    private boolean isLink;
    private String linkDisplayColumn;
    private String linkValueColumn;
    private ArrayList<CardHeaderColorCriteria> colorsCriteria = new ArrayList();
}
