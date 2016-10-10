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
 * @author Carlos Arancón del Valle
 */
public class TermWeighting {

    private Connection conn;
    private String url;
    private String dbName;
    private String driver;
    private String userName;
    private String password;

    public TermWeighting(String url, String dbName, String user, String password) {
        this.url = url;
        this.dbName = dbName;
        this.userName = userName;
        this.password = password;
        driver = "com.mysql.jdbc.Driver";
    }

    public void KLD(String fichero) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String cuentaReg = "", cuentaRegFic = "", cuentaLemFic = "", cuentaLem = "", ordenUpdate = "";
        Statement instCuentaReg = null, instCuentaRegFic = null, instCuentaLemFic = null, instCuentaLem = null, instrUpdt = null;
        ResultSet rsCuentaReg = null, rsCuentaRegFic = null, rsCuentaLemFic = null, rsCuentaLem = null;
        String nC = "", ntC = "", ntD = "";
        String tag = "", lemma = "", form = "", valor = "", indice = "";
        int nD = 0;
        Double kld = 0.0, pdt = 0.0, pct = 0.0;

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url + dbName, userName, password);

        cuentaReg = "SELECT SUM(TABLE_ROWS) TOTAL_REGS FROM information_schema.`TABLES` T WHERE TABLE_SCHEMA = \'" + dbName + "\';";
        instCuentaReg = (Statement) conn.createStatement();
        rsCuentaReg = instCuentaReg.executeQuery(cuentaReg);
        instCuentaReg = null;
        while (rsCuentaReg.next()) {
            nC = rsCuentaReg.getString(1);
        }

        cuentaRegFic = "SELECT * FROM " + fichero + ";";
        instCuentaRegFic = (Statement) conn.createStatement();
        rsCuentaRegFic = instCuentaRegFic.executeQuery(cuentaRegFic);
        instCuentaRegFic = null;
        nD = 0;
        while (rsCuentaRegFic.next()) {
            tag = rsCuentaRegFic.getString(1);
            lemma = rsCuentaRegFic.getString(2);
            form = rsCuentaRegFic.getString(3);
            valor = rsCuentaRegFic.getString(4);
            indice = rsCuentaRegFic.getString(5);
            nD = nD + 1;

            cuentaLemFic = "SELECT COUNT(*) FROM " + fichero + " WHERE lemma like \"" + lemma + "\";";
            instCuentaLemFic = (Statement) conn.createStatement();
            rsCuentaLemFic = instCuentaLemFic.executeQuery(cuentaLemFic);
            instCuentaLemFic = null;
            while (rsCuentaLemFic.next()) {
                ntD = rsCuentaLemFic.getString(1);
            }

            cuentaLem = "SELECT COUNT(*) FROM Recopila WHERE lemma like \"" + lemma + "\";";
            instCuentaLem = (Statement) conn.createStatement();
            rsCuentaLem = instCuentaLem.executeQuery(cuentaLem);
            instCuentaLem = null;
            while (rsCuentaLem.next()) {
                ntC = rsCuentaLem.getString(1);
            }

            pdt = Double.parseDouble(ntD) / nD;
            pct = Double.parseDouble(ntC) / Double.parseDouble(nC);

            kld = pdt * (Math.log10(pdt / pct));

            ordenUpdate = "UPDATE "+fichero + " SET valor=\"" + kld + "\" WHERE indice=\"" + indice + "\";";
            instrUpdt = (Statement) conn.createStatement();
            instrUpdt.executeUpdate(ordenUpdate);

            instrUpdt = null;

        }

            conn.close();
    }

}
