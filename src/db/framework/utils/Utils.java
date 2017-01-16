package db.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.framework.runner.MainRunner;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.parser.Parser;
import gherkin.util.FixJava;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a generic utility class for interacting with files and cucumber
 */
@SuppressWarnings("deprecation")
public class Utils {

    public static PrintStream errLog = null;
    public static PrintStream infoLog = null;
    public static Logger log = LoggerFactory.getLogger(Utils.class);
    // use these to redirect unneeded error output
    private static PrintStream originalErr = System.err;
    private static int errRedirectCalls = 0;
    private static int infoRedirectCalls = 0;

    /**
     * Executes a command on the command line (cmd for windows, else bash)
     *
     * @param cmd command to run
     * @return result of command
     */
    public static String executeCMD(String cmd) {
        long ts = System.currentTimeMillis();
        Process p = null;
        if (!isWindows()) {
            cmd = cmd.replaceAll("\"", "\\\\\"");
        } else {
            cmd = "cmd.exe /c \"" + cmd + "\"";
        }
        System.out.println(cmd);
        try {
            if (isWindows()) {
                p = Runtime.getRuntime().exec(cmd);
            } else {
                String[] cmds = new String[]{"bash", "-c", cmd};
                p = Runtime.getRuntime().exec(cmds);
            }
            return captureOutput(p);
        } catch (Throwable e1) {
            e1.printStackTrace();
        } finally {
            if (p != null)
                p.destroy();
            System.out.println("-->" + (System.currentTimeMillis() - ts) + ":" + cmd);
        }

        return null;
    }

    /**
     * Retrieves information about the selenium driver
     *
     * @param driverPath path to the driver
     * @return String with driver information
     */
    public static String getSeleniumDriverInfo(File driverPath) {
        String msg = "Cannot capture driver info.";
        String cmd;
        try {
            cmd = driverPath.getCanonicalPath();
        } catch (Exception ex) {
            ex.printStackTrace();
            return msg;
        }
        long ts = System.currentTimeMillis();
        Process p = null;
//    	cmd = "cmd.exe /c \"" + cmd + "\"";
        if (!isWindows()) {
            cmd = cmd.replaceAll("\"", "\\\\\"");
        }
        System.out.println(cmd);
        ProcessWatchDog pd = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            pd = new ProcessWatchDog(p, 3000, "getSeleniumDriverInfo()");
            return captureOutput(p).replace('\n', ' ');
        } catch (Throwable e1) {
            e1.printStackTrace();
            return msg;
        } finally {
            if (pd != null)
                pd.interrupt();
            if (p != null)
                p.destroy();
            //System.out.println("-->" + (System.currentTimeMillis() - ts) + ":" + cmd);
        }
    }

    /**
     * Reads a text file to a string
     *
     * @param f file to read
     * @return file contents
     * @throws IOException read errors
     */
    public static String readTextFile(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = Files.newInputStream(f.toPath(), StandardOpenOption.READ)) {
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader lineReader = new BufferedReader(reader);

            String line;
            while ((line = lineReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Creates a directory
     *
     * @param dir   directory name
     * @param clean true for a clean directory
     * @return resulting File
     */
    public static File createDirectory(String dir, boolean clean) {
        return createDirectory(new File(dir), clean);
    }

    /**
     * Creates a directory
     *
     * @param fDir  File to create
     * @param clean whether to delete any existing directory
     * @return directory that was created
     */
    public static File createDirectory(File fDir, boolean clean) {
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                System.err.println("Unable to make directory: " + fDir.getName());
            }
        }
        if (clean) {
            try {
                FileUtils.cleanDirectory(fDir);
            } catch (IOException e) {
                System.out.println("Error cleaning directory:" + e.getMessage());
            }
        }
        return fDir;
    }

    /**
     * Creates a directory
     *
     * @param dir directory name
     * @return resulting File
     */
    public static File createDirectory(String dir) {
        return createDirectory(dir, false);
    }

    /**
     * Returns the SHA key of a feature
     *
     * @param feature  feature file path
     * @param scenario scenario name
     * @return SHA key
     */
    public static String getScenarioShaKey(String feature, String scenario) {
        String path = (feature + scenario).replaceAll("\\s", "");
        String key = DigestUtils.sha256Hex(path);
        System.err.println("...key generation:" + path + ":" + key);
        return key;
    }

    /**
     * Converts json to "pretty" format
     *
     * @param o input json
     * @return formatted JSON as string
     */
    public static String jsonPretty(Object o) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(o);
    }

    /**
     * Writes a binary file
     *
     * @param aBytes    bytes to write
     * @param aFileName File to write to
     * @return true if write succeeded
     */
    public static boolean writeSmallBinaryFile(byte[] aBytes, File aFileName) {
        DataOutputStream os = null;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(aFileName);
            os = new DataOutputStream(fout);
            os.write(aBytes);
            return true;
        } catch (IOException ex) {
            System.out.println("Cannot create:" + aFileName.getPath());
        } finally {
            closeIoOutput(os);
            closeIoOutput(fout);
        }
        return false;
    }

    /**
     * Converts from milliseconds to days/hours/minutes/seconds
     *
     * @param millis milliseconds to convert
     * @return string result of conversion
     */
    public static String toDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) {
            sb.append(days).append(" Days ");
        }
        if (hours > 0) {
            sb.append(hours).append(" Hours ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" Minutes ");
        }
        sb.append(seconds).append(" Seconds");

        return (sb.toString());
    }

    /**
     * Writes a binary file
     *
     * @param aBytes    bytes to write
     * @param aFileName file to write to
     * @param append    whether or not to append to an existing file
     * @return true if write succeeded
     */
    public static boolean writeBinaryFile(byte[] aBytes, File aFileName, boolean append) {
        try {
            if (!append && aFileName.exists()) {
                if (!aFileName.delete()) {
                    System.err.println("Unable to delete file: " + aFileName.getName());
                }
            }
            File fDir = aFileName.getAbsoluteFile().getParentFile();
            if (!fDir.exists()) {
                if (!fDir.mkdirs()) {
                    System.err.println("Unable to create directory: " + fDir.getName());
                    return false;
                }
            }

            Path path = Paths.get(aFileName.getCanonicalPath());
            if (append && aFileName.exists()) {
                Files.write(path, aBytes, StandardOpenOption.APPEND);
            } else {
                Files.write(path, aBytes); // creates, overwrites
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Attempts to convert an object into an int
     *
     * @param number object to convert
     * @param ret    value to return if conversion fails
     * @return result of conversion
     */
    public static int parseInt(Object number, int ret) {
        try {
            if (number == null) {
                return ret;
            }
            if (number instanceof Float) {
                return ((Float) number).intValue();
            }
            if (number instanceof Double) {
                return ((Double) number).intValue();
            }
            return Integer.parseInt(number.toString().replaceAll(",", "").split("\\.")[0]);
        } catch (Exception ex) {
            return ret;
        }
    }

    /**
     * Converts gherkin feature file into json format
     *
     * @param isPretty true for "pretty" formatting
     * @param path     path to write output to
     * @return json string
     */
    @SuppressWarnings("deprecation")
    public static String gherkinToJson(boolean isPretty, String path) {
        // Define Feature file and JSON File path.
        String gherkin = null;
        try {
            gherkin = FixJava.readReader(new InputStreamReader(new FileInputStream(path.trim()), "UTF-8"));
        } catch (FileNotFoundException e) {
            Assert.fail("Feature file not found at " + path);
        } catch (UnsupportedEncodingException | RuntimeException e) {
            e.printStackTrace();
        }

        StringBuilder json = new StringBuilder();
        JSONFormatter formatter;
        // pretty or ugly selection, pretty by default
        if (!isPretty) {
            formatter = new JSONFormatter(json);// not pretty
        } else {
            formatter = new JSONPrettyFormatter(json);// pretty
        }

        Parser parser = new Parser(formatter);
        parser.parse(gherkin, path, 0);
        formatter.done();
        formatter.close();
        return json.toString();
    }

    /**
     * Sleeps for a given time
     *
     * @param sleepTime time to sleep in millis
     * @param msg       info message to display
     * @return true if sleep interrupted
     */
    public static boolean threadSleep(long sleepTime, String msg) {
        Thread cur = Thread.currentThread();
        initLogs();
        try {
            if (msg != null)
                infoLog.println("--> Thread sleep: " + msg + ":id-" + cur.getId() + ":" + sleepTime);
            Thread.sleep(sleepTime);
            if (msg != null)
                infoLog.println(new Date() + "--> Thread awake: " + msg + ":id-" + cur.getId() + ":normal");
            return false;
        } catch (InterruptedException e) {
            if (msg != null)
                errLog.println(new Date() + "--> Thread awake: " + msg + ":id-" + cur.getId() + ":" + e.getMessage());
            return true;
        }
    }

    /**
     * Checks if the machine is running OSX
     *
     * @return true if running on an OSX machine
     */
    public static boolean isOSX() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Checks if the machine is running windows
     *
     * @return true if running on a windows machine
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Checks if the machine is running windows 8
     *
     * @return true if running on a windows 8 machine
     */
    public static boolean isWindows8() {
        return System.getProperty("os.name").toLowerCase().contains("windows 8");
    }

    /**
     * Checks if the machine is running linux
     *
     * @return true if running on a linux machine
     */
    public static boolean isLinux() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }


    /**
     * Gets the method that called another
     *
     * @param from method to look for caller of
     * @return call stack which lead to the call you asked for
     */
    public static List<String> getCallFromFunction(String from) {
        return getCallFromFunction(from, 5);
    }

    /**
     * Gets the method that called another
     *
     * @param from method to look for caller of
     * @param size number of callers to list
     * @return call stack which lead to the call you asked for
     */
    public static List<String> getCallFromFunction(String from, int size) {
        StackTraceElement[] stackels = Thread.currentThread().getStackTrace();
        ArrayList<String> displayEls = new ArrayList<>();
        int count = 20;
        for (StackTraceElement stackel : stackels) {
            String trace = stackel.toString();
            if (trace.contains(".getStackTrace(") ||
                    trace.contains(".getCallFromFunction(") ||
                    trace.contains(from)) {
                continue;
            }
            if (trace.startsWith("db.")) {
                displayEls.add(trace);
            }
            if (displayEls.size() == size) {
                break;
            }
            if (--count <= 0) {
                break;
            }
        }
        return displayEls;
    }

    /**
     * Gets the ID of the current process
     *
     * @return process ID
     */
    public static int getProcessId() {
        try {
            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
            pid_method.setAccessible(true);

            return (Integer) pid_method.invoke(mgmt);
        } catch (Exception ex) {
            System.out.println("--> utils.getProcesId():" + ex.getMessage());
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Captures the desktop and writes it to an output stream
     *
     * @param out output stream to write data to
     * @throws Exception write error
     */
    public static void desktopCapture(OutputStream out) throws Exception {
        long ts = System.currentTimeMillis();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        ImageIO.write(image, "png", out);
        System.out.println("-->desktopCapture():" + (System.currentTimeMillis() - ts));
    }

    public static String listToString(List<String> list, String token, String[] cleans) {
        if (cleans != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                String s = list.get(i);
                boolean clean = false;
                for (String c : cleans) {
                    if (s.contains(c)) {
                        clean = true;
                        break;
                    }
                }
                if (clean) {
                    list.remove(i);
                }
            }
        }
        return String.join(token, list.toArray(new String[list.size()]));
    }

    private static String captureOutput(Process proc) throws Exception {
        ReadStream stdin = new ReadStream("stdin", proc.getInputStream());
        ReadStream stderr = new ReadStream("stdin", proc.getErrorStream());
        proc.waitFor();

        return stdin.getConsole().append("\n+++Error console:\n").append(stderr.getConsole()).toString();

    }

    protected static byte[] readSmallBinaryFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            Path path = Paths.get(file.getCanonicalPath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Could not read file: " + file.getName());
            return null;
        }
    }

    private static boolean closeIoOutput(OutputStream st) {
        if (st == null) {
            return true;
        }
        try {
            st.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Object toObject(byte[] bytes) {
        try (
                ObjectInputStream oo = new ObjectInputStream(new ByteArrayInputStream(bytes))
        ) {
            return oo.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] toBytes(Object object) {
        try (
                ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream)
        ) {
            objectOutStream.writeObject(object);
            return byteOutStream.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean writeObject(Object obj, File f) {
        return writeSmallBinaryFile(toBytes(obj), f);
    }

    private static Object readObject(File f) {
        return toObject(readSmallBinaryFile(f));
    }

    private static String encodeURL(String url) {
        try {
            return new java.net.URI(null, url, null).toASCIIString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    private static StringBuilder readStringFromInputStream(BufferedReader is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = "";
        while (line != null) {
            line = is.readLine();
            if (line != null) {
                sb.append(line).append("\n");
            }
        }
        return sb;
    }

    public static String httpGet(String url, StringBuilder cookies) throws Exception {
        HttpClient client = new HttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        GetMethod method = new GetMethod(encodeURL(url));
        try {
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            method.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
            if (cookies != null && !cookies.toString().isEmpty()) {
                method.addRequestHeader("Cookie", cookies.toString());
                method.addRequestHeader("Connection", "keep-alive");
                method.addRequestHeader("Cache-Control", "max-age=0");
                method.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            }

            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new Exception("Message code failed: " + method.getStatusLine());
            }
            InputStream in = method.getResponseBodyAsStream();
            StringBuilder responseBody = readStringFromInputStream(new BufferedReader(new InputStreamReader(in)));
            Object resCookie = method.getResponseHeader("Set-Cookie");
            if (cookies != null && resCookie != null) {
                String cookieValue = ((Header) resCookie).getValue();
                cookies.append(cookieValue).append(";");
            }
            return responseBody.toString();
        } finally {
            method.releaseConnection();
        }
    }

    public static ArrayList<JSONObject> jsonArrayToList(JSONArray json) {
        ArrayList<JSONObject> items = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            try {
                items.add((JSONObject) json.get(i));
            } catch (JSONException e) {
                System.err.println("Unable to convert JSONArray to List<JSONObject>: " + e);
            }
        }
        return items;
    }

    protected static class ReadStream extends Thread {
        StringBuilder console = new StringBuilder();
        String name;
        InputStream is;

        public ReadStream(String name, InputStream is) {
            this.name = name;
            this.is = is;
            this.start();
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                while (true) {
                    String s = br.readLine();
                    if (s == null)
                        break;
                    if (System.getenv("DEBUG") != null) {
                        System.out.println(s);
                    }
                    console.append(s).append("\n");
                }
                is.close();
            } catch (Exception ex) {
                System.out.println("Problem reading stream " + name + "... :" + ex);
                ex.printStackTrace();
            }
        }

        public StringBuilder getConsole() {
            return this.console;
        }
    }

    public static class ProcessWatchDog extends Thread {
        private Process m_process;
        private long m_timeout;
        private String m_name;

        /**
         * Creates a watchdog for a process to monitor it for timeouts
         *
         * @param p       process to monitor
         * @param timeout timeout in milliseconds
         * @param name    name of the process
         */
        public ProcessWatchDog(Process p, long timeout, String name) {
            this.m_process = p;
            this.m_timeout = timeout;
            this.m_name = name + System.currentTimeMillis();
            this.start();
        }

        /**
         * Kills the monitored process if it is still running
         */
        public void run() {
            Utils.threadSleep(this.m_timeout, null);
            if (this.m_process.isAlive()) {
                System.out.println("--> ProcessWatchDog.destroyForcibly():" + this.m_name + ":" + this.m_timeout);
                this.m_process.destroyForcibly();
            }
        }
    }

    /**
     * Initializes the PrintStream used to redirect any error message bloat
     */
    private static void initLogs() {
        if (errLog == null || infoLog == null) {
            try {
                File errFile = new File(MainRunner.workspace + "logs/db-error.log");
                File infoFile = new File(MainRunner.workspace + "logs/db-info.log");
                createDirectory(MainRunner.workspace + "logs");
                if (!errFile.exists()) {
                    if (!errFile.createNewFile()) {
                        System.err.println("Could not create error log file");
                    }
                }
                if (!infoFile.exists()) {
                    if (!infoFile.createNewFile()) {
                        System.err.println("Could not create info log file");
                    }
                }
                FileOutputStream errStream = new FileOutputStream(errFile);
                FileOutputStream infoStream = new FileOutputStream(infoFile);
                errLog = new PrintStream(errStream);
                infoLog = new PrintStream(infoStream);
            } catch (IOException e) {
                System.err.println("Error while creating file: " + e);
            }
        }
    }

    /**
     * Redirects System.out prints to the log files to avoid console clutter
     * <p>
     * Maintains a call count with resetSOut so redirects/resets below
     * each other don't mess each other up.
     * </p>
     */
    public static void redirectSOut() {
        if (infoLog == null) {
            initLogs();
        }
        if (infoLog != null) {
            System.setOut(infoLog);
            infoRedirectCalls++;
        }
    }

    /**
     * Redirects System.err prints to the log files to avoid console clutter
     * <p>
     * Maintains a call count with resetSErr so redirects/resets below
     * each other don't mess each other up.
     * </p>
     */
    public static void redirectSErr() {
        if (errLog == null) {
            initLogs();
        }
        if (errLog != null) {
            System.setErr(errLog);
            errRedirectCalls++;
        }
    }

    /**
     * Sets System.err back to the console
     * <p>
     * Maintains a call count with redirectSErr so redirects/resets below
     * each other don't mess each other up.
     * </p>
     */
    public static void resetSErr() {
        errRedirectCalls--;
        if (errRedirectCalls < 0) {
            errRedirectCalls = 0;
        }
        if (errRedirectCalls == 0) {
            System.setErr(originalErr);
        }
    }
}
