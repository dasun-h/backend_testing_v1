package db.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.framework.runner.MainRunner;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.parser.Parser;
import gherkin.util.FixJava;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a generic utility class for interacting with files and cucumber
 */
@SuppressWarnings("deprecation")
public class Utils {

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
     * @param fDir  File to create
     * @param clean whether to delete any existing directory
     * @return directory that was created
     */
    public static File createDirectory(File fDir, boolean clean) {
        if (!fDir.exists()) {
            fDir.mkdirs();
        }
        if (clean)
            try {
                FileUtils.cleanDirectory(fDir);
            } catch (IOException e) {
                System.out.println("Error cleaning directory:" + e.getMessage());
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
     * Creates a directory
     *
     * @param dir   directory name
     * @param clean true for a clean directory
     * @return resulting File
     */
    public static File createDirectory(String dir, boolean clean) {
        File fdir = new File(dir);
        if (!fdir.exists()) {
            fdir.mkdirs();
        }
        try {
            if (clean)
                FileUtils.cleanDirectory(fdir);
        } catch (IOException e) {
            System.out.println("Error cleaning directory:" + e.getMessage() + ":" + dir);
        }
        return fdir;
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
        return writeBinaryFile(aBytes, aFileName, false);
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
        if (days > 0)
            sb.append(days).append(" Days ");
        if (hours > 0)
            sb.append(hours).append(" Hours ");
        if (minutes > 0)
            sb.append(minutes).append(" Minutes ");
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
                aFileName.delete();
            }
            File fdir = aFileName.getAbsoluteFile().getParentFile();
            if (!fdir.exists())
                fdir.mkdirs();

            Path path = Paths.get(aFileName.getCanonicalPath());
            if (append && aFileName.exists())
                Files.write(path, aBytes, StandardOpenOption.APPEND);
            else
                Files.write(path, aBytes); // creates, overwrites
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
            if (number == null)
                return ret;
            if (number instanceof Float)
                return ((Float) number).intValue();
            if (number instanceof Double)
                return ((Double) number).intValue();
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
    public static String gherkinTojson(boolean isPretty, String path) {
        // Define Feature file and JSON File path.
        String gherkin = null;
        try {
            gherkin = FixJava.readReader(new InputStreamReader(new FileInputStream(path.trim()), "UTF-8"));
        } catch (FileNotFoundException e) {
            Assert.fail("Feature file not found at " + path);
            // e.printStackTrace();
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
//		System.out.println("json output: n" + json + "'");
        return json.toString();
    }

    /**
     * Sleeps for a given time
     *
     * @param sleeptime time to sleep in millis
     * @param msg       info message to display
     * @return true if sleep interrupted
     */
    public static boolean threadSleep(long sleeptime, String msg) {
        Thread cur = Thread.currentThread();
        try {
            //if (msg != null)
            //    System.out.println("--> Thread sleep: " + msg + ":id-" + cur.getId() + ":" + sleeptime);
            Thread.sleep(sleeptime);
            //if (msg != null)
            //    System.out.println(new Date() + "--> Thread awake: " + msg + ":id-" + cur.getId() + ":normal");
            return false;
        } catch (InterruptedException e) {
            //if (msg != null)
            //    System.out.println(new Date() + "--> Thread awake: " + msg + ":id-" + cur.getId() + ":" + e.getMessage());
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
                    trace.contains(from))
                continue;
            if (displayEls.size() == size) {
                break;
            }
            if (trace.startsWith("sdt."))
                displayEls.add(trace);
            if (--count <= 0)
                break;
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

    /**
     * Gets a resource file with a given name
     *
     * @param fname file name
     * @return resulting File
     */
    public static File getResourceFile(String fname) {
        String resPath = "src/db/";
        if (!new File("src").exists())
            resPath = "db/";

        // project data
        String full_path = getResourcePath(fname);
        String path = resPath + MainRunner.project.replace(".", "/") + "/resources/data/" + full_path;
        File resource = new File(path);
        if (resource.exists() && !resource.isDirectory()) {
            return resource;
        }

        // shared data
        path = resPath + "shared/resources/data/" + full_path;
        resource = new File(path);
        if (resource.exists() && !resource.isDirectory()) {
            return resource;
        }
        return resource;
    }

    /**
     * Gets the path to a resource file
     *
     * @param fName file to look for
     * @return file path
     */
    private static String getResourcePath(String fName) {
        return "other/" + fName;
    }

    protected static String listToString(List<String> list, String token, String[] cleans) {
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
                if (clean)
                    list.remove(i);
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

    protected static byte[] readSmallBinaryFile(File aFileName) {
        if (aFileName == null || !aFileName.exists())
            return null;
        try {
            Path path = Paths.get(aFileName.getCanonicalPath());
            return Files.readAllBytes(path);
        } catch (IOException e) {

        }
        return null;
    }

    private static boolean closeIoInput(InputStream st) {
        if (st == null)
            return true;
        try {
            st.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static ArrayList getTarFileList(File tar, String filepath) throws IOException {
        ArrayList list = new ArrayList();
        FileInputStream fin = null;
        TarArchiveInputStream intar = null;
        try {
            fin = new FileInputStream(tar);
            intar = new TarArchiveInputStream(fin);
            getCompressFileList(list, intar, filepath);
        } finally {
            if (intar != null)
                intar.close();
            if (fin != null)
                fin.close();
        }
        return list;
    }

    private static void getCompressFileList(ArrayList list, ArchiveInputStream intar, String filepath) throws IOException {
        ArchiveEntry entry = null;
        while ((entry = intar.getNextEntry()) != null) {
            if (!entry.getName().contains(filepath))
                continue;
            HashMap hsf = new HashMap();
            hsf.put("name", entry.getName());
            hsf.put("length", entry.getSize());
            if (entry.isDirectory())
                hsf.put("directory", entry.isDirectory());
            list.add(hsf);
        }
    }

    private static byte[] getTarFile(File tar, String filepath) throws IOException {
        FileInputStream fin = null;
        TarArchiveInputStream intar = null;
        try {
            fin = new FileInputStream(tar);
            intar = new TarArchiveInputStream(fin);
            TarArchiveEntry entry = null;
            while ((entry = intar.getNextTarEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().startsWith(filepath))
                    continue;
                byte[] ret = new byte[(int) entry.getSize()];
                intar.read(ret, 0, ret.length);
                return ret;
            }
        } finally {
            if (intar != null)
                intar.close();
            if (fin != null)
                fin.close();
        }
        return null;
    }

    private static boolean outputTarFile(File tar, String arfilepath, String outputpath) throws IOException {
        FileInputStream fin = null;
        TarArchiveInputStream intar = null;
        try {
            fin = new FileInputStream(tar);
            intar = new TarArchiveInputStream(fin);
            outputCompressFile(arfilepath, intar, outputpath);
        } finally {
            closeIoInput(intar);
            closeIoInput(fin);
        }
        return false;
    }

    protected static boolean outputJarFile(File ar, String arfilepath, String outputpath, String... fileFilters) throws IOException {
        FileInputStream fin = null;
        JarArchiveInputStream inar = null;
        try {
            fin = new FileInputStream(ar);
            inar = new JarArchiveInputStream(fin);
            outputCompressFile(arfilepath, inar, outputpath, fileFilters);
        } finally {
            closeIoInput(inar);
            closeIoInput(fin);
        }
        return false;
    }

    private static String getOutputPath(String tarpath, String outputpath, String path) {
        if (outputpath.isEmpty())
            return path;
        return outputpath + "/" + path.replaceAll(tarpath, "");
    }

    private static boolean isFileFilter(String[] filters, String path) {
        if (filters.length == 0)
            return true;
        for (String filter : filters) {
            if (path.contains(filter))
                return true;
        }
        return false;
    }

    private static void outputCompressFile(String tarfilepath, ArchiveInputStream intar, String outputpath, String... fileFilters) throws IOException {
        File foutput = new File(outputpath);
        createDirectory(foutput.getAbsoluteFile().getParentFile(), false);

        ArchiveEntry entry;
        while ((entry = intar.getNextEntry()) != null) {
            String path = entry.getName();
            if (!path.startsWith(tarfilepath))
                continue;
            if (!entry.isDirectory() && !isFileFilter(fileFilters, path))
                continue;

            if (entry.isDirectory()) {
                createDirectory(new File(getOutputPath(tarfilepath, outputpath, path)), false);
            } else {
                File fout = new File(getOutputPath(tarfilepath, outputpath, path));
                long ts = System.currentTimeMillis();
                System.out.print("writing " + fout.getCanonicalPath() + "...");
                fout.delete();

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int length = -1;
                while ((length = intar.read(buff)) > -1) {
                    bout.write(buff, 0, length);
                }
                System.out.println(System.currentTimeMillis() - ts);
                writeBinaryFile(bout.toByteArray(), fout, true);
            }
        }
    }

    private static boolean closeIoOutput(OutputStream st) {
        if (st == null)
            return true;
        try {
            st.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Object toObject(byte[] bytes) {
        try {
            ByteArrayInputStream bi = null;
            ObjectInputStream oo = null;
            try {
                bi = new ByteArrayInputStream(bytes);
                oo = new ObjectInputStream(bi);
            } finally {
                if (oo != null)
                    oo.close();
                if (bi != null)
                    bi.close();
            }

            return oo.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] toBytes(Object object) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (Exception ex) {
            return null;
        } finally {
            closeIoOutput(oos);
            closeIoOutput(baos);
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
            if (line != null)
                sb.append(line).append("\n");
        }
        return sb;
    }

    protected static String httpGet(String url, StringBuilder cookies) throws Exception {
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

    private static void appendCookies(StringBuilder cookies, CloseableHttpResponse response) {
        Object resCookie = response.getHeaders("Set-Cookie");
        if (resCookie != null) {
            try {
                if (resCookie.getClass().toString().contains("[Lorg.apache.http.Header")) {
                    org.apache.http.Header[] headers = (org.apache.http.Header[]) resCookie;
                    if (headers.length > 0) {
                        String cookieValue = headers[0].toString().replaceAll("Set-Cookie: ", "");
                        cookies.append(cookieValue).append(";");
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    protected static int post(CloseableHttpClient client, String url, Map hparams, StringBuilder cookies, StringBuilder result) throws Exception {
        HttpPost post = new HttpPost(encodeURL(url));
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Connection", "keep-alive");
        if (!cookies.toString().isEmpty())
            post.setHeader("Cookie", cookies.toString());

        Iterator en = hparams.keySet().iterator();
        List<NameValuePair> urlParameters = new ArrayList<>();
        while (en.hasNext()) {
            String key = en.next().toString();
            urlParameters.add(new BasicNameValuePair(key, hparams.get(key).toString()));
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        if (!url.endsWith("/j_acegi_security_check"))
            System.out.println("post():" + url + "\n-->Params: " + ((url.endsWith("/json") ? "json_data..." : urlParameters)));
        else
            System.out.println("post():" + url);
        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        if (200 > statusCode || statusCode >= 400) {
            System.out.println("-->post().reponse: " + response);
            throw new Exception("Message code failed: " + response.getStatusLine());
        }

        appendCookies(cookies, (CloseableHttpResponse) response);

        result.append(readStringFromInputStream(new BufferedReader(new InputStreamReader(response.getEntity().getContent()))).toString());
        return statusCode;
    }

    /**
     * Method to return SQL Queries
     *
     * @return SQL queries as json object
     */
    public static JSONObject getSqlQueries() {

        File queries = getResourceFile("queries.json");
        JSONObject jsonObject = null;

        try {
            String jsonTxt = Utils.readTextFile(queries);
            jsonObject = new JSONObject(jsonTxt);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    public static String decryptPassword(String password) {
        String pWord = null;
        try {
            File passwordTxt = getResourceFile("password.json");
            String javaText = Utils.readTextFile(passwordTxt);
            JSONObject json = new JSONObject(javaText);
            pWord = json.get(password).toString();
        } catch (Exception e) {
            Assert.fail("Unable to find data in file" + e);
        }
        return pWord;
    }

    /**
     * Method to return all contextual media information
     *
     * @return Contextual Media information
     */
    public static JSONObject getContextualizeMedia() {

        File queries = getResourceFile("contextualize_media.json");
        JSONObject jsonObject = null;

        try {
            String jsonTxt = Utils.readTextFile(queries);
            jsonObject = new JSONObject(jsonTxt);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    public static class PageHangWatchDog extends Thread {
        private final static long TIMEOUT = 60 * 1000L;
        private static PageHangWatchDog m_PageHangWatchDog;
        private static boolean m_pause;
        private String m_url;
        private long m_ts;

        private PageHangWatchDog() {
            System.err.println("--> Start:PageHangWatchDog:" + new Date());
            this.reset(MainRunner.getWebDriver().getCurrentUrl());
            this.start();
        }

        public static void init() {
            if (m_PageHangWatchDog == null) {
                m_PageHangWatchDog = new PageHangWatchDog();
            }
        }

        public static void pause(boolean ispause) {
            m_pause = ispause;
        }

        private void reset(String url) {
            this.m_ts = System.currentTimeMillis();
            if (url != null)
                this.m_url = url;
        }

        public void run() {
            Thread.currentThread().setPriority(MAX_PRIORITY);
            while (true) {
                //System.err.print("^");
                try {
                    if (m_pause)
                        continue;
                    //System.err.print("^");
                    if (!MainRunner.driverInitialized())
                        continue;
                    String url = MainRunner.currentURL;
                    if (url.contains("about:blank"))
                        continue;
                    if (url.equals(this.m_url)) {
                        if (System.currentTimeMillis() - this.m_ts > TIMEOUT) {
                            System.err.println("--> PageHangWatchDog: timeout at " + this.m_url);
                            new Thread(StepUtils::stopPageLoad).start();
                            this.reset(null);
                        }
                    } else {
                        this.reset(url);
                    }
                } catch (Throwable ex) {
                    System.err.println("--> Error:PageHangWatchDog:" + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    //System.err.print(m_pause ? "|" : "~");
                    Utils.threadSleep(5000, this.getClass().getSimpleName());
                }
            }
        }
    }

    public static class ThreadWatchDog extends Thread {
        private Thread m_thread;
        private long m_timeout;
        private String m_name;
        private Runnable m_callback;

        public ThreadWatchDog(Thread th, long timeout, String name, Runnable callback) {
            this.m_thread = th;
            this.m_timeout = timeout;
            this.m_name = name + System.currentTimeMillis();
            this.m_callback = callback;
            this.start();
        }

        public void run() {
            if (Utils.threadSleep(this.m_timeout, "--> ThreadWatchDog.start():" + this.m_name + ":" + this.m_timeout)) {
                System.err.println("--> ThreadWatchDog.start():" + this.m_name + ":" + this.m_timeout + ": exit normally.");
                return;
            }
            if (this.m_thread != null && this.m_thread.isAlive()) {
                System.err.println("--> ThreadWatchDog.destroy():" + this.m_name + ":" + this.m_timeout);
                this.m_thread.interrupt();
            }
            if (this.m_callback != null)
                m_callback.run();
        }
    }

    protected static class ReadStream extends Thread {
        StringBuilder console = new StringBuilder();
        String name;
        InputStream is;
        Thread thread;

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
                    if (System.getenv("DEBUG") != null)
                        System.out.println(s);
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

    protected abstract static class UtilsComparator implements Comparator {
        private Object[] m_params;

        public UtilsComparator(Object[] params) {
            this.m_params = params;
        }

        @Override
        public abstract int compare(Object o1, Object o2);

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
}
