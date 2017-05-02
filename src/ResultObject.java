import java.nio.file.Path;

/**
 * Created by Gabriel Jadderson on 20/04/2017.
 */
public class ResultObject implements Result
{

    private Path path;
    private int number;

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

    public void setNumber(int number)
    {
        this.number = number;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }
}
