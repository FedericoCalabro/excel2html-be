package com.e2h.service;

import com.e2h.entity.GenerationEntity;
import com.e2h.generator.*;
import com.e2h.repository.GenerationRepository;
import com.e2h.request.GenerationRequest;
import com.e2h.request.SortingColumn;
import com.e2h.util.DataManipulationUtility;
import com.webfirmframework.wffweb.tag.html.Html;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ApiService {

    @Autowired
    GenerationRepository repository;

    GeneratorStrategy generator;

    public GenerationEntity preview (GenerationRequest request){

        try {
            String view = request.getConfig().getView();
            var data= request.getData();
            switch (view) {
                case "TABLE": generator = new TableGenerator(); break;
                case "CARD":  generator = new CardGenerator(); break;
                case "PIVOT":  generator = new PivotTableGenerator(); break;
                default: throw new RuntimeException("Bad view-mode configured!");
            }

            ArrayList<Map<String, Object>> rows = request.getData();
            rows.removeIf(row -> !DataManipulationUtility.matchCriterias(row, request));
            Collections.sort(rows, mapComparator(request));

            Html document = generator.generate(request);
            HtmlGeneratorUtility.finalizeDocument(document);

            String plainHTML = document.toHtmlString();
            GenerationEntity entity = new GenerationEntity();
            entity.setHtml(plainHTML);
            return repository.save(entity);

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public GenerationEntity get(String id){
        return repository.getById(id);
    }

    public Resource download(String id) {
        GenerationEntity entity = repository.getById(id);
        return new ByteArrayResource(entity.getHtml().getBytes(StandardCharsets.UTF_8));
    }

    private Comparator<Map<String, Object>> mapComparator(GenerationRequest request) {
        ArrayList<SortingColumn> sortingBy = request.getConfig().getSortByColumns();
        return new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                for (int i = 0; i < sortingBy.size(); i++) {
                    int sort = 0;
                    String colSorting = sortingBy.get(i).getName();
                    String type = sortingBy.get(i).getType();
                    int order = sortingBy.get(i).getOrder();

                    Object m1Value = m1.get(colSorting);
                    Object m2Value = m2.get(colSorting);

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
