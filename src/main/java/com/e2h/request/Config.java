package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Config {
    private String view = "TABLE";
    private String name = "DATA-VIEW";

    private boolean addRowCounter = false;

    private ArrayList<String> columns = new ArrayList<>();
    private ArrayList<String> themeColor = new ArrayList<>();
    private ArrayList<String> textColor = new ArrayList<>();
    private ArrayList<SortingColumn> sortByColumns = new ArrayList<>();
    private ArrayList<MergedColumns> mergedColumns = new ArrayList<>();
    private ArrayList<AggregationRow> aggregationRows = new ArrayList<>();
    private ArrayList<RowCriteria> rowCriteria = new ArrayList<>();
    private CardHeaderConfig cardHeaderConfig = new CardHeaderConfig();
}
