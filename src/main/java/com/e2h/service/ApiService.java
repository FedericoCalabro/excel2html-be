package com.e2h.service;

import com.e2h.entity.GenerationEntity;
import com.e2h.generator.*;
import com.e2h.repository.GenerationRepository;
import com.e2h.request.GenerationRequest;
import com.e2h.request.SendToServer;
import com.e2h.request.SortingColumn;
import com.e2h.util.DataManipulationUtility;
import com.e2h.util.DataRowComparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webfirmframework.wffweb.tag.html.Html;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
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

    public boolean sendToServer(String id, SendToServer config){
        GenerationEntity entity = this.get(id);

        String cwd = System.getProperty("user.dir");
        String filename = "temp" + new Date().getTime() + ".html";
        String localAbsPathToFile = cwd + "\\" + filename;

        File file = new File(localAbsPathToFile);

        try{
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(localAbsPathToFile, false);
            fos.write(entity.getHtml().getBytes());
            fos.close();

            String command = String.format("scp -P %d %s %s@%s:%s", config.getPort(), localAbsPathToFile, config.getUsername(), config.getUrl(), config.getAbsPath());
            Process process = Runtime.getRuntime().exec(command);
            PrintWriter pw = new PrintWriter(process.getOutputStream(), true);
            pw.println(config.getPassword());
            pw.close();
            InputStream ip = process.getErrorStream();
            String s = new String(ip.readAllBytes());
            if(s.length() > 0)
                throw new RuntimeException("Errore: " + s);

        }catch (IOException e){
            throw new RuntimeException("Errore durante l'invio del file");
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }finally {
            file.delete();
        }

        return true;
    }
}
