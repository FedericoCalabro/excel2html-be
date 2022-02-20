package com.e2h.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Config {
    private String view = "TABLE";
    private String name = "DATA-VIEW";
    private boolean addRowCounter = false; //se sono in cardvire questo valore DEVE essere false
    private ArrayList<String> columns = new ArrayList<>();

    //TODO: devono avere uguale lunghezza
    //quali tra le colonne di orderColumns sono effettivamente dei link
    private ArrayList<String> linkColumns = new ArrayList<>();
    //che scritta visualizzare per il link i-esimo?
    private ArrayList<String> linkNames = new ArrayList<>();

    private ArrayList<RowCriteria> rowCriteria = new ArrayList<>();
}
