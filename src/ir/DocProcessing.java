/*
 * Copyright 2016 carlos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author carlos
 */
public class DocProcessing {

    public static int numPal;
    private Connection conn;
    private String url;
    private String dbName;
    private String driver;
    private String userName;
    private String password;
   
    public void lematizaFicheros(String carpeta, boolean multiterm, String url, String dbName, String user, String password) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        File directorio = new File(carpeta);
        String nomfic = "";
        FileReader fr;
        BufferedReader br;
        String contenido, linea, insertar, orden, ord;
        Lematizacion lm = null;
        Statement instruccion =null, stmt= null;
        this.url = url;
        this.dbName = dbName;
        this.userName = userName;
        this.password = password;
        driver = "com.mysql.jdbc.Driver";

        File[] ficheros = directorio.listFiles();
        Class.forName(driver).newInstance();
        
        conn = DriverManager.getConnection(url + dbName, userName, password);
        for (int i = 0; i < ficheros.length; i++) {
            lm = new Lematizacion(multiterm);
            numPal = 0;
            nomfic = ficheros[i].getName();
            linea = "";
            fr = new FileReader(carpeta + "/" + nomfic);
            br = new BufferedReader(fr);

            contenido = "";
            linea = br.readLine();

            while (linea != null) {
                contenido = contenido + linea + " ";
                linea = br.readLine();
            }

            br.close();
            fr.close();

            System.out.println(i + "- Extrayendo conceptos para " + nomfic);

            insertar = lm.morfologico(contenido);
            lm = null;
            System.gc();
            
            ord = "CREATE TABLE IF NOT EXIST `IRDDBB2016`.`"+nomfic+"` ( `tag` VARCHAR(50) NULL , `lemma` VARCHAR(50) NOT NULL , `form` VARCHAR(50) NULL , `valor` DOUBLE NOT NULL DEFAULT '0' , `indice` INT NOT NULL , INDEX `indlemma` (`lemma`)) ENGINE = InnoDB";
            stmt = conn.createStatement();
            stmt.executeUpdate(ord);
            
            orden = "INSERT INTO " + nomfic + " (tag,lemma,form,valor,indice) VALUES (" + insertar + ");";
            instruccion = (Statement) conn.createStatement();
            instruccion.executeUpdate(orden);
        }

        conn.close();

    }

}
