package pl.edu.agh.muvto;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pl.edu.agh.muvto.Main;

/**
 * Unit test for simple app.
 */
public class MainTest extends TestCase
{
    public MainTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite(MainTest.class);
    }

    public void testMain()
    {
        Main.main(new String[]{});
    }
}
