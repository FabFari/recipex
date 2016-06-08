package com.recipex.utilities;

/**
 * Created by Sara on 11/05/2016.
 */
public class Misurazione {
    public String tipo;
    public String data;
    public String dato1long;
    public String dato2long;
    public String datodouble;
    public String nota;
    public long id;
    public String idCalendar;

    public Misurazione(){}
    public Misurazione(String tipo, String data, String dato1long, String dato2long, String datodouble){
        this.tipo=tipo;
        this.data=data;
        this.dato1long=dato1long;
        this.dato2long=dato2long;
        this.datodouble=datodouble;
    }
    public void setNota(String nota){
        this.nota=nota;
    }
    public void setId(long i){id=i;}
    public void setIdCalendar(String i){idCalendar=i;}
}
