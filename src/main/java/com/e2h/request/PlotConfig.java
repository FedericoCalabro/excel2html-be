package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class PlotConfig {
    private String type = "CACTUS";
    private String labelX;
    private String labelY;
    private String blockedCol;
    private String targetCol;
    private String labelXValue;
    private String labelYValue;
    private ArrayList<String> colors = new ArrayList<>();
    private ArrayList<String> styles = new ArrayList<>();
}
