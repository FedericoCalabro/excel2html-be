package com.e2h.util;

import com.e2h.request.AggregationRow;
import com.e2h.request.GenerationRequest;
import com.e2h.request.MergedColumns;
import com.e2h.request.RowCriteria;
import com.webfirmframework.wffweb.tag.html.links.A;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.math.BigDecimal;
import java.util.*;

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
                    check = false;
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

    public static HashMap<String, Double> getGroupedAggrFuncMap(GenerationRequest request, AggregationRow aggrRow){
        HashMap<String, Double> pivotFinal = new HashMap<>();
        HashMap<String, ArrayList<Double>> pivotHelper = new HashMap<>();

        String blockedCol = aggrRow.getBlockedCol();
        String target = aggrRow.getTargetCol();
        String op = aggrRow.getOp();

        for (int row = 0; row < request.getData().size(); row++) {
            Map<String, Object> rowData = request.getData().get(row);
            String blockedColValue = rowData.get(blockedCol).toString();
            Object targetObj = rowData.get(target);
            double targetValue;
            try {
                targetValue = Double.parseDouble(targetObj.toString());
            } catch (NumberFormatException e){
                throw new RuntimeException("Unexpected value: " + targetObj + " for column: " + target);
            }
            pivotHelper.putIfAbsent(blockedColValue, new ArrayList<>());
            pivotHelper.get(blockedColValue).add(targetValue);
        }

        for (Map.Entry<String, ArrayList<Double>> entry : pivotHelper.entrySet()){
            String key = entry.getKey();
            double[] arrValue = DataManipulationUtility.collectionDoubleToDoubleArr(entry.getValue());
            switch (op){
                case "Sum":
                    pivotFinal.put(key, StatUtils.sum(arrValue));
                    break;
                case "Min":
                    pivotFinal.put(key, StatUtils.min(arrValue));
                    break;
                case "Max":
                    pivotFinal.put(key, StatUtils.max(arrValue));
                    break;
                case "Mean":
                    pivotFinal.put(key, StatUtils.mean(arrValue));
                    break;
                case "Product":
                    pivotFinal.put(key, StatUtils.product(arrValue));
                    break;
                case "Std Deviation":
                    double variance = StatUtils.variance(arrValue);
                    pivotFinal.put(key, Math.sqrt(variance));
                    break;
                case "Std Variance":
                    pivotFinal.put(key, StatUtils.variance(arrValue));
                    break;
            }
        }
        return pivotFinal;
    }

    public static double[] collectionDoubleToDoubleArr(List<Double> collection){
        double[] ret = new double[collection.size()];
        Iterator<Double> iterator = collection.iterator();
        for (int i = 0; i < ret.length; i++)
            ret[i] = iterator.next().intValue();
        return ret;
    }

}
