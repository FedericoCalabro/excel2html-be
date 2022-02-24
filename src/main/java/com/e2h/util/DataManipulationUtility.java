package com.e2h.util;

import com.e2h.request.GenerationRequest;
import com.e2h.request.MergedColumns;
import com.e2h.request.RowCriteria;
import com.webfirmframework.wffweb.tag.html.links.A;

import java.util.ArrayList;
import java.util.Map;

public class DataManipulationUtility {

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
            Object valueLeft = DataManipulationUtility.extractColValue(request, row, columnName);

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
    
    public static ArrayList<String> getColComposition(GenerationRequest request, String col){
        ArrayList<String> composedBy = new ArrayList<>();
        ArrayList<MergedColumns> mergedColumns = request.getConfig().getMergedColumns();
        for (int i = 0; i < mergedColumns.size(); i++) {
            String name = mergedColumns.get(i).getName();
            if(name.equals(col)){
                return mergedColumns.get(i).getMergedFrom();
            }
        }
        composedBy.add(col);
        return composedBy;
    }
    
    public static String extractColValue(GenerationRequest request, Map<String, Object> rowData, String col){
        ArrayList<String> composedBy = getColComposition(request, col);
        String finalValue = "";
        for (int i = 0; i < composedBy.size(); i++) {
            String composedName = composedBy.get(i);
            Object colValueObject = rowData.get(composedName);
            String toAppend = colValueObject != null ? colValueObject.toString() : "#";
            if(i < composedBy.size() - 1)
                toAppend += " - ";
            finalValue += toAppend;
        }
        return finalValue;
    }

}
