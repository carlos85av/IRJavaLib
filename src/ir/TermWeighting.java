/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author carlos
 */
public class TermWeighting {

    public Connection conn = null;
    public String url = "jdbc:mysql://localhost:3306/";
    public String dbName = "KLDDBB";
    public String driver = "com.mysql.jdbc.Driver";
    public String userName = "root";
    public String password = "root";

    public void TFIDF() throws SQLException {

        try {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url + dbName, userName, password);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }

        String ordenSelect = "", ordenUpdate = "", ordencount1 = "";
        Statement instruccion = null, instruccion1 = null, instruccion2 = null;
        ResultSet bdatos = null, rc1 = null;

        try {
            ordenSelect = "select * from WORDS_kld;";
            instruccion = (Statement) conn.createStatement();
            bdatos = instruccion.executeQuery(ordenSelect);
            instruccion = null;

            String idf = "", kld = "", indice = "", ntD = "", lemma = "", nomfic = "";
            double kldidf = 0;
            int contador = 0;

            while (bdatos.next()) {
                indice = bdatos.getString(8);
                idf = bdatos.getString(6);
                kld = bdatos.getString(5);
                lemma = bdatos.getString(2);
                nomfic = bdatos.getString(4);

                ordencount1 = "select count(*) from WORDS_kld where lemma=\"" + lemma + "\" and nomfic like \"" + nomfic + "\";";
                instruccion1 = (Statement) conn.createStatement();
                rc1 = instruccion1.executeQuery(ordencount1);
                instruccion1 = null;
                while (rc1.next()) {
                    ntD = rc1.getString(1);
                }

                kldidf = Double.parseDouble(idf) * Double.parseDouble(ntD);

                try {
                    ordenUpdate = "UPDATE WORDS_kld SET valorKLDIDF=\"" + kldidf + "\" WHERE indice like \"" + indice + "\";";
                    instruccion2 = (Statement) conn.createStatement();
                    instruccion2.executeUpdate(ordenUpdate);

                    instruccion2 = null;
                    
                    contador = contador + 1;

                } catch (SQLException er) {
                    // TODO Auto-generated catch block
                    er.printStackTrace();
                }

            }

        } catch (SQLException er) {
            // TODO Auto-generated catch block
            er.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
