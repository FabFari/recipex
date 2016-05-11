package com.recipex.utilities;

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


    public Terapia(String nome, long dose, String tipo, boolean ricetta, String ingrediente, String unità,
                   long quantità, String foglio){
        this.nome=nome;
        this.dose=dose;
        this.tipo=tipo;
        this.ricetta=ricetta;
        this.ingrediente=ingrediente;
        this.quantità=quantità;
        this.unità=unità;
        this.foglio=foglio;
    }

}
