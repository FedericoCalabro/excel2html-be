package com.e2h.request;

import lombok.Data;

@Data
public class SortingColumn {
    private String name;
    private String type;
    private Integer order;
}
