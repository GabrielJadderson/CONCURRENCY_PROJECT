package endpackage;


/**
 * This class is present only for helping you in testing your software.
 * It will be completely ignored in the evaluation.
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Main
{
    public static void main(String[] args)
    {
        long startTime = System.nanoTime();

        new test();
        //new FileFinder();

        long endTime = System.nanoTime();

        double difference = (endTime - startTime) / 1e6;
        System.out.println("took: " + difference + " ms.");
    }
}