package com.e2h.service;

import com.e2h.entity.GenerationEntity;
import com.e2h.generator.*;
import com.e2h.repository.GenerationRepository;
import com.e2h.request.GenerationRequest;
import com.e2h.request.SortingColumn;
import com.e2h.util.DataManipulationUtility;
import com.e2h.util.DataRowComparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webfirmframework.wffweb.tag.html.Html;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ApiService {

    @Autowired
    GenerationRepository repository;

    GeneratorStrategy generator;

    public GenerationEntity preview (GenerationRequest request){

            String view = request.getConfig().getView();
            switch (view) {
                case "TABLE": generator = new TableGenerator(); break;
                case "CARD":  generator = new CardGenerator(); break;
                case "PIVOT":  generator = new PivotTableGenerator(); break;
                case "PLOT":  generator = new PlotGenerator(); break;
                default: throw new RuntimeException("Bad view-mode configured!");
            }

            ArrayList<Map<String, Object>> rows = request.getData();
            rows.removeIf((row) -> !DataManipulationUtility.matchesAllRowsCriterias(request, row));
            Collections.sort(rows, DataRowComparator.get(request));

            Html document = generator.generate(request);
            HtmlGeneratorUtility.finalizeDocument(document);

            String plainHTML = document.toHtmlString();
            GenerationEntity entity = new GenerationEntity();
            entity.setHtml(plainHTML);

            try {
                ObjectMapper Obj = new ObjectMapper();
                String jsonStr = Obj.writeValueAsString(request.getConfig());
                entity.setConfig(jsonStr);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return repository.save(entity);
    }

    public GenerationEntity get(String id){
        return repository.getById(id);
    }

    public Resource downloadHtml(String id) {
        GenerationEntity entity = repository.getById(id);
        return new ByteArrayResource(entity.getHtml().getBytes(StandardCharsets.UTF_8));
    }
    public Resource downloadConfig(String id) {
        GenerationEntity entity = repository.getById(id);
        return new ByteArrayResource(entity.getConfig().getBytes(StandardCharsets.UTF_8));
    }

}
