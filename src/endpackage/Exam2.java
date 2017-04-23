package endpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 * Created by Gabriel Jadderson on 22-04-2017.
 */
public class Exam2
{


    public static final BlockingDeque<ResultObject> THE_LIST = new LinkedBlockingDeque<ResultObject>();
    public static final BlockingDeque<ResultObject> RESULT_LIST = new LinkedBlockingDeque<ResultObject>();
    public static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    /**
     * inserts into {@link null} asynchronously
     * uses the executor service to submit
     *
     * @param files    usually contains 0-4 files
     * @param executor
     */
    public static void insertAsynch(ArrayList<File> files, ExecutorService executor)
    {
        if (files.isEmpty())
            return;

        executor.submit(() ->
        {
            files.forEach((x) ->
            {
                THE_LIST.add(new ResultObject(x.toPath(), -1));

                //VALID_FILES_LIST.forEach((v) -> System.out.println(v.path() + " : " + v.number()));
                executor.submit(() -> processResult(THE_LIST, RESULT_LIST));
            });
        });
    }


    /*
    public static ArrayList<File> directoryTreeIterator(Path path, ExecutorService executor, int depth)
    {
        ArrayList<File> txtFiles = new ArrayList<>();
        try
        {
            //ArrayList<File> files = Arrays.asList(path.toFile().listFiles());
            File[] files = path.toFile().listFiles();
            if (depth == 0) //covering the initial depth of the file tree, since it's never added to the array
            {
                for (File file : files)
                {
                    if (file.getName().contains(".txt"))
                    {
                        synchronized (txtFiles) //not needed really
                        {
                            txtFiles.add(file);
                        }
                    }
                }

                insertAsynch(directoryTreeIterator(path, executor, depth + 1), executor);
            } else
            {
                for (File file : files)
                {
                    if (file.isDirectory())
                    {
                        insertAsynch(directoryTreeIterator(file.toPath(), executor, depth + 1), executor);
                    } else
                    {
                        if (file.getName().contains(".txt"))
                        {
                            synchronized (txtFiles)
                            {
                                txtFiles.add(file);
                            }
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return txtFiles;
    }
    */


    public static ArrayList<File> directoryTreeIterator(Path path, ExecutorService executor, int depth)
    {
        ArrayList<File> txtFiles = new ArrayList<>();
        try
        {
            File[] files = path.toFile().listFiles();
            if (depth == 0) //covering the initial depth of the file tree, since it's never added to the array
            {
                for (File file : files)
                    if (file.getName().contains(".txt"))
                        synchronized (txtFiles)
                        {
                            txtFiles.add(file);
                        }

                insertAsynch(directoryTreeIterator(path, executor, depth + 1), executor);
            } else
                for (File file : files)
                    if (file.isDirectory())
                    {
                        insertAsynch(directoryTreeIterator(file.toPath(), executor, depth + 1), executor);
                    } else if (file.getName().contains(".txt"))
                        synchronized (txtFiles)
                        {
                            txtFiles.add(file);
                        }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return txtFiles;
    }

    public static void incrediblyFastDirectoryTreeIterator(String path, ExecutorService executor) throws IOException
    {
        Files.walk(Paths.get(path)).parallel().filter((x) -> x.toString().endsWith(".txt")).forEach((x) ->
        {
            THE_LIST.add(new ResultObject(x, -1));
            executor.submit(() -> processResult(THE_LIST, RESULT_LIST));
        });
    }


    public static void processResult(BlockingDeque<ResultObject> iteratorList, BlockingDeque<ResultObject> resultList)
    {
        try
        {
            ResultObject resultObject = iteratorList.takeFirst();
            resultObject.setNumber(getMaxAsynch(resultObject.path()));
            resultList.add(resultObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static int getMaxAsynch(Path path) throws IOException
    {
        //long d = Files.lines(path).parallel().map(x -> x.split(",")).flatMap(x -> Arrays.stream(x)).count(); //USE THIS FOR FIND ANY!!!!!!!!!
        return Files.lines(path).parallel().map(x -> x.split(",")).flatMap(x -> Arrays.stream(x).parallel()).mapToInt(Integer::parseInt).max().getAsInt();
    }

    private static int getMax(Path file)
    {
        AtomicInteger total = new AtomicInteger(0);
        try (BufferedReader reader = Files.newBufferedReader(file))
        {
            reader.lines().forEach((line) ->
            {
                String[] numbers = line.split(",");
                for (String number : numbers)
                {
                    if (total.get() < Integer.parseInt(number))
                        total.set(Integer.parseInt(number));
                }
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return total.get();
    }


    /**
     * Finds a file that contains at most (no more than) n numbers and such that all
     * numbers in the file are equal or greater than min.
     * <p>
     * This method searches only for one (any) file in the directory
     * (parameter dir) such that the condition above is respected.
     * As soon as one such occurrence is found, the search can be
     * stopped and the method can return immediately.
     * <p>
     * As for method {@code findAll}, the search is recursive.
     */
    public static Result findAny(Path dir, int n, int min)
    {
        try
        {
            Stream stream = Files.walk(dir, 0, FileVisitOption.FOLLOW_LINKS);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public static void run()
    {
        CountDownLatch productionLatch = new CountDownLatch(1);

        new Thread(() ->
        {
            //produce(THE_LIST, executor);
            //getAndStoreTxtFiles(Paths.get("C:\\Users\\mulli\\OneDrive\\Dokumenter\\IdeaProjects\\Concurrency_Project_final\\CONCURRENCY_PROJECT\\data_example"), executor, 0);
            try
            {
                incrediblyFastDirectoryTreeIterator("C:\\Users\\mulli\\OneDrive\\Dokumenter\\IdeaProjects\\Concurrency_Project_final\\CONCURRENCY_PROJECT\\data_example", executor);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            productionLatch.countDown();
        }).start();
        System.out.println("started the production thread");


        try
        {
            productionLatch.await();
            executor.shutdown();
            executor.awaitTermination(1L, TimeUnit.DAYS);


            RESULT_LIST.forEach((x) -> System.out.println(x.path() + " : " + x.number()));
            System.out.println(THE_LIST.size());
            System.out.println("files processed: " + RESULT_LIST.size());
        } catch (InterruptedException e)
        {
        }

    }


    public static void main(String[] args)
    {
        doAndMeasure("Executors", () -> run());

    }

    public static void doAndMeasure(String caption, Runnable runnable)
    {
        long tStart = System.currentTimeMillis();
        runnable.run();
        System.out.println(caption + " took " + (System.currentTimeMillis() - tStart) + "ms");
    }


}
