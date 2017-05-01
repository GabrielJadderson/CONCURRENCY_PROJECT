package endpackage;

import endpackage.FinalPackage.Result;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gabriel Jadderson on 09-04-2017.
 */
public class test
{


    public static List<File> textFiles = new ArrayList<>();


    public test()
    {
        Path startDirectory = Paths.get("data_example/");
        try
        {
            findALLRecursive(startDirectory);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void countNumbers(Path file) throws IOException
    {

        BufferedReader reader = Files.newBufferedReader(file);
        reader.lines().forEach((line) ->
        {
            String[] numbers = line.split(",");
            int total = 0;
            for (String number : numbers)
            {
                total += Integer.parseInt(number);
            }
            System.out.println(file.toString() + " : " + total);
        });

    }

    public static List<Result> findALLRecursive(Path dir) throws IOException
    {
        List<Result> resultList = new ArrayList<>();
        File[] directories = dir.toFile().listFiles();
        for (File f : directories)
        {
            if (!f.isDirectory() && f.getName().contains(".txt"))
            {
                countNumbers(f.toPath());
                textFiles.add(f);
            } else
            {
                findALLRecursive(Paths.get(dir.toAbsolutePath().toString() + "/" + f.getName()));
            }
        }
        return resultList;
    }

    public static List<Result> addToResult(List<Result> list, Path path, int number)
    {

        return list;
    }
}
