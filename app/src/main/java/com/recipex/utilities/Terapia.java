package com.recipex.utilities;

/**
 * Created by Sara on 07/05/2016.
 */
public class Terapia {
    public String nome;
    public long dose;
    public String tipo;
    public boolean ricetta;

    public Terapia(String nome, long dose, String tipo, boolean ricetta){
        this.nome=nome;
        this.dose=dose;
        this.tipo=tipo;
        this.ricetta=ricetta;
    }

}
