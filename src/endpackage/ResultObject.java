package endpackage;

import java.nio.file.Path;

/**
 * Created by Gabriel Jadderson on 20/04/2017.
 */
public class ResultObject implements Result
{

    private final Path path;
    private final int number;

    public ResultObject(Path path, int number)
    {
        this.path = path;
        this.number = number;
    }

    @Override
    public Path path()
    {
        return path;
    }

    @Override
    public int number()
    {
        return number;
    }

}
