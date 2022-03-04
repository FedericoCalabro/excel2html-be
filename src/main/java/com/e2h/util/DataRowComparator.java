package com.e2h.util;

import com.e2h.request.GenerationRequest;
import com.e2h.request.SortingColumn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class DataRowComparator {
    public static Comparator<Map<String, Object>> get(GenerationRequest request) {
        ArrayList<SortingColumn> sortingBy = request.getConfig().getSortByColumns();
        return new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                for (int i = 0; i < sortingBy.size(); i++) {
                    int sort = 0;
                    String colSorting = sortingBy.get(i).getName();
                    String type = sortingBy.get(i).getType();
                    int order = sortingBy.get(i).getOrder();

                    Object m1Value = DataManipulationUtility.extractColValue(request, m1, colSorting);
                    Object m2Value = DataManipulationUtility.extractColValue(request, m2, colSorting);

                    if(type.equals("Number")) {
                        try {
                            float vLeft = Float.parseFloat(m1Value.toString());
                            float vRight = Float.parseFloat(m2Value.toString());
                            sort = Float.compare(vLeft, vRight);
                        } catch (NumberFormatException e){
                            sort = 0;
                        }
                    } else if(type.equals("String")){
                        String vLeft = m1Value.toString();
                        String vRight = m2Value.toString();
                        sort = vLeft.compareTo(vRight);
                    }

                    if(sort != 0)
                        return sort*order;
                }
                return -1;
            }
        };
    }
}
