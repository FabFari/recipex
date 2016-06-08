package com.recipex.utilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sara on 07/05/2016.
 */
public class Terapia {
    public String nome;
    public long dose;
    public String tipo;
    public boolean ricetta;
    public String ingrediente;
    public String unità;
    public long quantità;
    public String foglio;
    public String caregiver;
    public long id;
    public ArrayList<String> idsCalendar;


    public Terapia(String nome, long dose, String tipo, boolean ricetta, String ingrediente, String unità,
                   long quantità, String foglio,String caregiver, long id){
        this.nome=nome;
        this.dose=dose;
        this.tipo=tipo;
        this.ricetta=ricetta;
        this.ingrediente=ingrediente;
        this.quantità=quantità;
        this.unità=unità;
        this.foglio=foglio;
        this.caregiver=caregiver;
        this.id = id;
        //se non ho eventi associati sta lista rimane vuota, e non ho problemi quando chiamo l'async task rimuovi nell'adapter
        this.idsCalendar=new ArrayList<>();
    }

    public void setIdCalendar(List<String> l){idsCalendar=(ArrayList)l;}

}
