package space.greenraven.android.nabalny;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyHelper {
    public static final String PROP_FILE="app.properties";
    public static final String RESULTS_URL="results.url";
    public static final String VOTE_URL="vote.url";

    private static Properties props = new Properties();

    public static void init(Context context){
        try {
            InputStream is = context.getAssets().open(PROP_FILE);
            props.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String propName){
        return props.getProperty(propName);
    }
}
