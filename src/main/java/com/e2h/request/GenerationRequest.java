package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

@Data
public class GenerationRequest {
    private ArrayList<Map<String, Object>> data;
    private Config config;
}
