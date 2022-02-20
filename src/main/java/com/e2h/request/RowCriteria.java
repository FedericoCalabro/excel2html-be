package com.e2h.request;

import lombok.Data;

@Data
public class RowCriteria {
    private String columnName;
    private String type;
    private String op;
    private Object value;
}
