package com.e2h.util;

import com.e2h.request.GenerationRequest;
import com.e2h.request.RowCriteria;

import java.util.ArrayList;
import java.util.Map;

public class DataManipulationUtility {

    public static String extractValue(GenerationRequest request, int row, String col){
        ArrayList<Map<String, Object>> data = request.getData();
        Map<String, Object> rowData = data.get(row);
        return rowData.get(col).toString();
    }

    public static boolean isLink(GenerationRequest request, String col) {
        ArrayList<String> linkColumns = request.getConfig().getLinkColumns();
        return linkColumns.contains(col);
    }

    public static String getLinkName(GenerationRequest request, String col){
        int index = request.getConfig().getLinkColumns().indexOf(col);
        return request.getConfig().getLinkNames().get(index);
    }
    
    public static boolean matchCriterias(Map<String, Object> row, GenerationRequest request){
        ArrayList<RowCriteria> criterias = request.getConfig().getRowCriteria();
        for (int i = 0; i < criterias.size(); i++) {
            boolean check = true;
            RowCriteria criteria = criterias.get(i);
            String columnName = criteria.getColumnName() ;
            String type = criteria.getType();
            String op = criteria.getOp();
            Object valueRight = criteria.getValue();
            Object valueLeft = row.get(columnName);

            if(type.equals("Number")) {
                try {
                    float vLeft = Float.parseFloat(valueLeft.toString());
                    float vRight = Float.parseFloat(valueRight.toString());
                    switch (op){
                        case ">": check = vLeft > vRight; break;
                        case "<": check = vLeft < vRight; break;
                        case "!=": check = vLeft != vRight; break;
                        case "==": check = vLeft == vRight; break;
                        default: check = false;
                    }
                } catch (NumberFormatException e){
                    throw new RuntimeException("Unexpected type for column: " + columnName);
                }
            } else if(type.equals("String")){
                String vLeft = valueLeft.toString();
                String vRight = valueRight.toString();
                switch (op){
                    case ">": check = vLeft.compareToIgnoreCase(vRight) < 0; break;
                    case "<": check = vLeft.compareToIgnoreCase(vRight) > 0; break;
                    case "!=": check = !vLeft.equalsIgnoreCase(vRight); break;
                    case "==": check = vLeft.equalsIgnoreCase(vRight); break;
                    case "Regex": check = vLeft.matches(vRight); break;
                    default: check = false;
                }
            }
            if(!check) {
                return false;
            }
        }
        return true;
    }

}
