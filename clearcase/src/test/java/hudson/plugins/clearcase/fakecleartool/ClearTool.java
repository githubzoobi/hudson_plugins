/**
 * The MIT License
 *
 * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
 *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.clearcase.fakecleartool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henrik
 */
public class ClearTool {

    static List<String> lines = new ArrayList<String>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: too few aagruments");
            System.exit(0);
        }
        readLogfile(args[0]);

        //System.out.println("Command:" +args[1]);
        //System.out.println("linex:" +lines.size());
        boolean hasPrinted=false;
        boolean started = false;
        for (String line : lines) {

            if (!started && line.contains("cleartool") && line.contains(args[1])) {
                //System.out.println("found possible start:"+line);
                if (args[1].equals("lsactivity") && line.contains(args[args.length-1])) {
                    started = true;
                    continue;
                }
                if (args[1].equals("lshistory")) {
                    //System.out.println("statred:");
                    started = true;
                    continue;
                }
            }

            if (started && line.contains("cleartool")) {
                //System.out.println("found end:"+line);
                break;
            }

            if (started) {
                hasPrinted=true;
                System.out.println(line);
            }
        }
        if (!hasPrinted) {
            System.out.println("Cleartool Error: command not understood");
        }



    }

    private static void readLogfile(String filename) {
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            boolean done = false;
            while (!done) {
                String line = br.readLine();
                if (line != null) {
                    lines.add(line);
                } else {
                    done = true;
                }
            }
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(ClearTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ClearTool.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
