package endpackage.FinalPackage;

import java.nio.file.Paths;

/**
 * Created by Gabriel Jadderson on 01-05-2017.
 */
public class finalExam
{

    public static void main(String[] args)
    {
        doAndMeasure("Executors", () -> {
            Exam.findAll(Paths.get("data_example"));
        });
    }

    public static void doAndMeasure(String caption, Runnable runnable)
    {
        long tStart = System.currentTimeMillis();
        runnable.run();
        System.out.println(caption + " took " + (System.currentTimeMillis() - tStart) + "ms");
        //RESULT_LIST.forEach(x-> System.out.println(x.path() + ": " + x.number()));

    }
}
