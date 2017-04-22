/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package endpackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 *
 * @author joel
 */
public class FileSystemGenerator 
{


    public static void main(String[] args)
    {
        
        Random random = new Random();
        int upperBound = 10000;
        
        Path name = Paths.get("file_system");
        
        writeLayer(random, upperBound, name, 7);
        
    }
    
    public static void writeLayer(Random random, int upperBound, Path path, int recursionsLeft)
    {
        if (recursionsLeft <= 0)
        {
            return;
        }
        
        System.out.println("New dir: " + path.toFile().mkdir());
        
        for(int i = 0; i < 4; i++)
        {
            StringBuilder sb = new StringBuilder();
            int amount = 10000 + random.nextInt(100);
        
            for(int j = 0; j < amount; j++)
            {
                sb.append(random.nextInt(upperBound));
                sb.append(",");
            }
            sb.append(random.nextInt(upperBound));
            String subPath = path.toString() + "/file" + i + ".txt";
            writeTextFile(sb.toString(), subPath);
        }
        
        
        int directories = random.nextInt(4) + 1;
        for(int i = 0; i < directories; i++)
        {
            writeLayer(random, upperBound, Paths.get(path.toString() + "/d" + i), recursionsLeft - 1);
        }
        
        
        
    }
    
    public static void writeTextFile(String s, String file)
    {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(new File(file));
            bw = new BufferedWriter(fw);
            bw.write(s);
            //System.out.println("Done");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(bw != null)
                {
                    bw.close();
                }
                if(fw != null)
                {
                    fw.close();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
