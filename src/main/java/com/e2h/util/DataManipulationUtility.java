package com.e2h.util;

import com.e2h.request.*;
import org.apache.commons.math3.stat.StatUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManipulationUtility {

    public static boolean matchesSingleCriteria(GenerationRequest request, RowCriteria criteria, Map<String, Object> rowData){
        String columnName = criteria.getColumnName();
        String type = criteria.getType();
        String op = criteria.getOp();
        Object valueRight = criteria.getValue();
        Object valueLeft = DataManipulationUtility.extractColValue(request, rowData, columnName);

        boolean check = true;
        if (type.equals("Number")) {
            try {
                float vLeft = Float.parseFloat(valueLeft.toString());
                float vRight = Float.parseFloat(valueRight.toString());
                switch (op) {
                    case ">":
                        check = vLeft > vRight;
                        break;
                    case "<":
                        check = vLeft < vRight;
                        break;
                    case "!=":
                        check = vLeft != vRight;
                        break;
                    case "==":
                        check = vLeft == vRight;
                        break;
                    default:
                        check = false;
                }
            } catch (NumberFormatException e) {
                check = false;
            }
        } else if (type.equals("String")) {
            String vLeft = valueLeft.toString();
            String vRight = valueRight.toString();
            switch (op) {
                case ">":
                    check = vLeft.compareToIgnoreCase(vRight) < 0;
                    break;
                case "<":
                    check = vLeft.compareToIgnoreCase(vRight) > 0;
                    break;
                case "!=":
                    check = !vLeft.equalsIgnoreCase(vRight);
                    break;
                case "==":
                    check = vLeft.equalsIgnoreCase(vRight);
                    break;
                case "Regex":
                    check = vLeft.matches(vRight);
                    break;
                default:
                    check = false;
            }
        }
        return check;
    }

    public static boolean matchesAllRowsCriterias(GenerationRequest request, Map<String, Object> rowData){
        List<RowCriteria> criterias = request.getConfig().getRowCriteria();
        for (int i = 0; i < criterias.size(); i++) {
            RowCriteria criteria = criterias.get(i);
            if(!matchesSingleCriteria(request, criteria, rowData))
                return false;
        }
        return true;
    }

    public static String getColNameFromRowValue(GenerationRequest request, int row, String value){
        Map<String, Object> dataRow = request.getData().get(row);
        for(Map.Entry<String, Object> entry : dataRow.entrySet()){
            if(entry.getValue().toString().equals(value))
                return entry.getKey();
        }
        return null;
    }

    public static ArrayList<String> getColComposition(GenerationRequest request, String col) {
        ArrayList<String> composedBy = new ArrayList<>();
        ArrayList<MergedColumns> mergedColumns = request.getConfig().getMergedColumns();
        for (int i = 0; i < mergedColumns.size(); i++) {
            String name = mergedColumns.get(i).getNameDefinition();
            if (name.equals(col)) {
                return mergedColumns.get(i).getMergedFrom();
            }
        }
        composedBy.add(col);
        return composedBy;
    }

    public static String extractColValue(GenerationRequest request, Map<String, Object> rowData, String col) {
        ArrayList<String> composedBy = getColComposition(request, col);
        ArrayList<MergedColumns> mergedColumns = request.getConfig().getMergedColumns();
        if (request.getConfig().getView().equals("TABLE") || request.getConfig().getView().equals("PIVOT") || composedBy.size() == 1) {
            String finalValue = "";
            for (int i = 0; i < composedBy.size(); i++) {
                String composedName = composedBy.get(i);
                Object colValueObject = rowData.get(composedName);
                String toAppend = colValueObject != null ? colValueObject.toString() : "N/A";
                if (i < composedBy.size() - 1)
                    toAppend += " - ";
                finalValue += toAppend;
            }
            return finalValue;
        } else {
            String nameDefinition = null;
            for (int i = 0; i < mergedColumns.size(); i++) {
                String nd = mergedColumns.get(i).getNameDefinition();
                if (nd.equals(col)) {
                    nameDefinition = nd;
                    break;
                }
            }
            if (nameDefinition == null) throw new RuntimeException("No col named: " + col + " found!");

            String regex = "\\{\\{(\\d+?)}}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(nameDefinition);

            int i = 0;
            while (matcher.find()){
                String group = matcher.group();
                Object repl = rowData.get(composedBy.get(i));
                col = col.replace("{{" + (i + 1) + "}}", repl != null ? repl.toString() : "N/A");
                i++;
            }

            return col;
        }
    }

    public static double[] collectionDoubleToDoubleArr(List<Double> collection) {
        double[] ret = new double[collection.size()];
        Iterator<Double> iterator = collection.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().doubleValue();
        }
        return ret;
    }

    public static HashMap<String, ArrayList<Double>> getMapFromBlockedTarget(GenerationRequest request, String blocked, String target){
        HashMap<String, ArrayList<Double>> pivotHelper = new HashMap<>();

        for (int row = 0; row < request.getData().size(); row++) {
            Map<String, Object> rowData = request.getData().get(row);
            String blockedColValue = rowData.get(blocked).toString();
            Object targetObj = rowData.get(target);
            double targetValue;
            try {
                targetValue = Double.parseDouble(targetObj.toString());
                pivotHelper.putIfAbsent(blockedColValue, new ArrayList<>());
                pivotHelper.get(blockedColValue).add(targetValue);
            } catch (NumberFormatException e) {
//                throw new RuntimeException("Unexpected value: " + targetObj + " for column: " + target);
            }
        }

//        Runtime.getRuntime().exec()

        return pivotHelper;
    }
}
