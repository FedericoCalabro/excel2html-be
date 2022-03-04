package com.e2h.util;

import com.e2h.request.AggregationRow;
import com.e2h.request.GenerationRequest;
import com.e2h.request.MergedColumns;
import com.e2h.request.RowCriteria;
import org.apache.commons.math3.stat.StatUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManipulationUtility {

    public static boolean matchCriterias(Map<String, Object> row, GenerationRequest request) {
        ArrayList<RowCriteria> criterias = request.getConfig().getRowCriteria();
        for (int i = 0; i < criterias.size(); i++) {
            boolean check = true;
            RowCriteria criteria = criterias.get(i);
            String columnName = criteria.getColumnName();
            String type = criteria.getType();
            String op = criteria.getOp();
            Object valueRight = criteria.getValue();
            Object valueLeft = DataManipulationUtility.extractColValue(request, row, columnName);

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
            if (!check) {
                return false;
            }
        }
        return true;
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

            String regex = "\\{\\{(\\w+)}}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(nameDefinition);
            int gc = matcher.groupCount() + 1;
            for (int i = 0; i < gc; i++) {
                Object repl = rowData.get(composedBy.get(i));
                col = col.replace("{{" + (i + 1) + "}}", repl != null ? repl.toString() : "N/A");
            }
            return col;
        }
    }

    public static HashMap<String, HashMap<String, Double>> getGroupedAggrFuncMap(GenerationRequest request, AggregationRow aggrRow) {
        HashMap<String, HashMap<String, Double>> pivotFinal = new HashMap<>();
        HashMap<String, ArrayList<Double>> pivotHelper = new HashMap<>();

        String blockedCol = aggrRow.getBlockedCol();
        String target = aggrRow.getTargetCol();
        ArrayList<String> op = aggrRow.getOp();

        for (int row = 0; row < request.getData().size(); row++) {
            Map<String, Object> rowData = request.getData().get(row);
            String blockedColValue = rowData.get(blockedCol).toString();
            Object targetObj = rowData.get(target);
            double targetValue;
            try {
                targetValue = Double.parseDouble(targetObj.toString());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unexpected value: " + targetObj + " for column: " + target);
            }
            pivotHelper.putIfAbsent(blockedColValue, new ArrayList<>());
            pivotHelper.get(blockedColValue).add(targetValue);
        }

        for (Map.Entry<String, ArrayList<Double>> entry : pivotHelper.entrySet()) {
            String key = entry.getKey();
            double[] arrValue = DataManipulationUtility.collectionDoubleToDoubleArr(entry.getValue());
            HashMap<String, Double> opToRes = new HashMap<>();
            for (int i = 0; i < op.size(); i++) {
                String currOp = op.get(i);
                double res;
                switch (currOp) {
                    case "Sum":
                        res = StatUtils.sum(arrValue);
                        break;
                    case "Min":
                        res = StatUtils.min(arrValue);
                        break;
                    case "Max":
                        res = StatUtils.max(arrValue);
                        break;
                    case "Mean":
                        res = StatUtils.mean(arrValue);
                        break;
                    case "Product":
                        res = StatUtils.product(arrValue);
                        break;
                    case "Std Variance":
                        res = StatUtils.variance(arrValue);
                        break;
                    case "Std Deviation":
                        res = Math.sqrt(StatUtils.variance(arrValue));
                        break;
                    default:
                        throw new RuntimeException("Invalid op for aggregation func defined");
                }
                opToRes.put(currOp, res);
            }
            pivotFinal.put(key, opToRes);
        }
        return pivotFinal;
    }

    public static double[] collectionDoubleToDoubleArr(List<Double> collection) {
        double[] ret = new double[collection.size()];
        Iterator<Double> iterator = collection.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next().doubleValue();
        }
        return ret;
    }
}
