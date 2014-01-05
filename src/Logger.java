import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Very simple logger implementation. It adds the current time and thread info
 * to the string and outputs that to the System.out.
 *
 * Created by u on 2014-01-05.
 */
public class Logger {
    static void log(String format, Object... args) {
        log(String.format(format, args));
    }

    static void log(String s) {
        String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        final Thread currentThread = Thread.currentThread();

        String l = String.format("%s: [T#%d, %s]: %s", time,
                currentThread.getId(), currentThread.getName(), s);
        System.out.println(l);
        System.out.flush();
    }
}
