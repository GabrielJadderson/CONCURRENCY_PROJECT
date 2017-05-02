import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by Gabriel Jadderson on 01-05-2017.
 */
public class ProcessAll
{

    public final BlockingDeque<ResultObject> THE_LIST = new LinkedBlockingDeque<>();
    public final BlockingDeque<ResultObject> RESULT_LIST = new LinkedBlockingDeque<>();
    public final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    public CountDownLatch productionLatch = new CountDownLatch(1);

    public List<Result> run(Path dir)
    {

        new Thread(() ->
        {
            try
            {
                increadiblyFastDirectoryTreeIterator(dir, executor);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            productionLatch.countDown();
        }).start();


        try
        {
            productionLatch.await();
            executor.shutdown();
            executor.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        List<Result> finalList = new ArrayList<>();
        RESULT_LIST.iterator().forEachRemaining(x -> finalList.add(x));
        return finalList;
    }


    private static void processResult(BlockingDeque<ResultObject> iteratorList, BlockingDeque<ResultObject> resultList)
    {
        try
        {
            ResultObject resultObject = iteratorList.takeFirst();
            resultObject.setNumber(getMax(resultObject.path()));
            resultList.add(resultObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static int getMax(Path path)
    {
        try (FileReader fileReader = new FileReader(path.toFile()); BufferedReader bufferedReader = new BufferedReader(fileReader))
        {
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] numbers = line.split(",");
                return Stream.of(numbers).mapToInt(Integer::parseInt).max().getAsInt();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }


    private void increadiblyFastDirectoryTreeIterator(Path path, ExecutorService executor) throws IOException
    {
        Files.walk(path).parallel().filter((x) -> x.toString().endsWith(".txt")).forEach((x) ->
        {
            THE_LIST.add(new ResultObject(x, -1));
            executor.submit(() -> processResult(THE_LIST, RESULT_LIST)); //process the result concurrently
        });
    }

}
