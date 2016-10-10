/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import edu.upc.freeling.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author carlos
 */
class Lematizacion {

    // Modify this line to be your FreeLing installation directory
    private String FREELINGDIR;
    private String DATA;
    private String LANG;
    private String nomfic;
    private String lineaPal = "", lineaPal2="";

    private boolean reconocimientoMultiples;

    double valor;

    public Lematizacion(boolean multiple) {
        FREELINGDIR = "/usr/local";
        DATA = FREELINGDIR + "/share/freeling/";
        LANG = "es";
        this.reconocimientoMultiples = multiple;

    }

    public String[] morfologico(String line, String nomfic) throws IOException {
        //System.loadLibrary("freeling_javaAPI");

        this.nomfic = nomfic;

        System.load("/home/carlos/freeling/APIs/java/" + "libfreeling_javaAPI.so");

        Util.initLocale("default");

        // Create options set for maco analyzer.
        // Default values are Ok, except for data files.
        MacoOptions op = new MacoOptions(LANG);

        if (reconocimientoMultiples) {
            op.setActiveModules(false, true, true, true, true, true, true, true, true, true);
        } else {
            op.setActiveModules(false, true, true, true, true, true, false, true, true, false);
        }

        op.setDataFiles(
                "",
                DATA + LANG + "/locucions.dat",
                DATA + LANG + "/quantities.dat",
                DATA + LANG + "/afixos.dat",
                DATA + LANG + "/probabilitats.dat",
                DATA + LANG + "/dicc.src",
                DATA + LANG + "/np.dat",
                DATA + "common/punct.dat",
                DATA + LANG + "/corrector/corrector.dat");

        // Create analyzers.
        Tokenizer tk = new Tokenizer(DATA + LANG + "/tokenizer.dat");
        Splitter sp = new Splitter(DATA + LANG + "/splitter.dat");
        Maco mf = new Maco(op);

        HmmTagger tg = new HmmTagger(DATA + LANG + "/tagger.dat", true, 2);
        Nec neclass = new Nec(DATA + LANG + "/nerc/nec/nec-ab-poor1.dat");

        Senses sen = new Senses(DATA + LANG + "/senses.dat"); // sense dictionary
        Ukb dis = new Ukb(DATA + LANG + "/ukb.dat"); // sense disambiguator

        // Identify language of the text.  
        // Note that this will identify the language, but will NOT adapt
        // the analyzers to the detected language.  All the processing 
        // in the loop below is done by modules for LANG (set to "es" at
        // the beggining of this class) created above.
        if (line != null) {
            // Extract the tokens from the line 

            ListWord l = tk.tokenize(line);

            // Split the tokens into distinct sentences.
            ListSentence ls = sp.split(l, true);

            // Perform morphological analysis
            mf.analyze(ls);

            // Perform part-of-speech tagging.
            tg.analyze(ls);

            // Perform named entity (NE) classificiation.
            neclass.analyze(ls);

            sen.analyze(ls);
            dis.analyze(ls);
            makeResults(ls);

        }

        String lp[]={lineaPal.substring(0,lineaPal.length()-1),lineaPal2.substring(0,lineaPal.length()-1)};
        return lp;
    }

    FileWriter fw;
    BufferedWriter bw;

    private void makeResults(ListSentence ls) throws IOException {

        ListSentenceIterator sIt = new ListSentenceIterator(ls);

        fw = new FileWriter("palabras.csv");
        bw = new BufferedWriter(fw);

        int numPal = 0;
        ListWordIterator wIt = null;
        Sentence s = null;
        Word w;
        while (sIt.hasNext()) {
            s = sIt.next();
            wIt = new ListWordIterator(s);
            while (wIt.hasNext()) {
                w = wIt.next();

                añadirPalabra(w);

                //System.out.println(w.getForm() + " " + w.getLemma() + " " + w.getTag());
                numPal++;
            }

            //System.out.println();            
        }
        bw.close();
        fw.close();
    }

    public void añadirPalabra(Word w) throws IOException {
        String tag, lemma, form, indice;
        tag = w.getTag();
        lemma = w.getLemma();
        form = w.getForm();
        valor = 0;

        DocProcessing.numPal = DocProcessing.numPal + 1;

        indice = DocProcessing.numPal + "";

        String orden = "";

        if (form.equals("\"")) {
            lemma = "\\\"";
            form = "\\\"";
        }
        if (form.equals("\\")) {
            lemma = "\\\\";
            form = "\\\\";
        }

        lineaPal = lineaPal + "(\"" + tag + "\",\"" + lemma + "\",\"" + form + "\"," + valor + "," + indice + "),";
        lineaPal2 = lineaPal2 + "(\"" + tag + "\",\"" + lemma + "\",\"" + form + "\"," + valor + ",\"" +nomfic+ "\"," + indice + "),";
    }

}
