package robustgametools.util;

/**
 * JsonFactory utility class to convert Json into
 * class object and vice versa. Also used for
 * parsing purposes
 *
 * @author kai
 */
public class JsonFactory {

    private static JsonFactory mJsonFactory = new JsonFactory();

    // To prevent instantiation
    private JsonFactory() {};

    // Singleton design pattern
    public static JsonFactory getInstance() {
        if (mJsonFactory == null) {
            mJsonFactory = new JsonFactory();
        }
        return mJsonFactory;
    }
}
