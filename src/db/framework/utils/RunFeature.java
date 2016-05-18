package db.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.framework.runner.MainRunner;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xerces.impl.dv.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class RunFeature {
    private static final String m_version = "1a.0001";
    private static int m_pid;
    private File m_repo_jar;
    private String m_workspace;
    private String m_eeURL;

    public RunFeature() throws Throwable {
        System.out.println("RunFeature version: " + m_version);
        m_pid = Utils.getProcessId();
        this.m_eeURL = "http://" + System.getenv("EE") + "/json";
        this.m_workspace = System.getenv("WORKSPACE");
        this.m_repo_jar = new File(this.m_workspace + "/" + System.getenv("repo_jar"));
        this.cleanWorkSpace();
        this.dumpEnvironmentVariables();

        System.out.println("\n\nPreparing workspace...");
        System.out.println("db/framework/resources");
        Utils.outputJarFile(this.m_repo_jar, "db/framework/resources", this.m_workspace + "/db/framework/resources");
        System.out.println("/db/shared/resources");
        Utils.outputJarFile(this.m_repo_jar, "db/shared/resources", this.m_workspace + "/db/shared/resources");
        System.out.println("/db/projects");
        Utils.outputJarFile(this.m_repo_jar, "db/projects", this.m_workspace + "/db/projects", ".feature");

        if (MainRunner.scenarios != null)
            MainRunner.scenarios = MainRunner.scenarios.replaceAll("features/", "db/projects/" + System.getenv("db_project").trim().replace(".", "/") + "/features/");

        try {
            System.out.println("\n\nInitializing MainRunner()...");
            MainRunner.main(null);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        this.archive();
        System.exit(MainRunner.runStatus);
    }

    public static void main(String[] args) {
        try {
//			utils.get_tar_file_list(new File("C:\\Users\\m526092\\eclipse_workspace\\JenkinsSlave\\builds\\processed\\11.120.180.247.DSV_test_Windows_7@2.69.1441286019238.tar"), "testreport/");
//			utils.outputJarFile(new File("C:\\Users\\m526092\\eclipse_workspace\\JenkinsSlave\\repo\\SDT\\master.sdt.jar"), "sdt/features/", "features");

            if (args.length == 0) {
                new RunFeature();
            } else if (args[0].equals("-self_clean")) {
                new ProcessWatchDog();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean checkAborted() {
        if (System.getenv("BUILD_URL") == null)
            return false;
        try {
            String bstatus = Utils.httpGet(System.getenv("BUILD_URL") + "api/json", null);
            String result = (String) new Gson().fromJson(bstatus, Map.class).get("result");
            if (result == null)
                result = "";
            if (result.equals("ABORTED"))
                return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getBuildConsole(String jobBuildLink) {
        System.out.println("-->Archiver.getBuildConsole():" + jobBuildLink);
        String console = "console is not available...";
        try {
            console = "<pre>" + Utils.httpGet(jobBuildLink + "/logText/progressiveHtml", null) + "</pre>";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return console;
    }

    public void cleanWorkSpace() {
        System.err.println("-->cleanWorkSpace()...");
        try {
            File[] files = new File(m_workspace).listFiles();
            for (File f : files) {
                if (f.getName().equals(this.m_repo_jar.getName()))
                    continue;
                System.out.println("--> removing " + f.getCanonicalPath());
                if (f.isDirectory())
                    FileUtils.cleanDirectory(f);
                f.delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void dumpEnvironmentVariables() {
        String logsPath = this.m_workspace + "/logs";
        File f = Utils.createDirectory(new File(logsPath), true);
        f = new File(logsPath + "/env_variables.json");
        System.out.println("\n\nDumping Environment variables...:" + logsPath + "/env_variables.json");
        Hashtable<String, String> h = new Hashtable<>();
        h.putAll(System.getenv());
        h.put("pid", Utils.getProcessId() + "");
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(h);
        System.out.println(json);
        System.out.println();
        System.out.println();
        Utils.writeSmallBinaryFile(json.getBytes(), f);
    }

    public void archive() throws Exception {
        System.out.println("Archiving logs directory...");
        File ftempfiles = Utils.createDirectory(new File("tempfiles"), true);

        File ws = new File(this.m_workspace);
        File flog = new File(ws.getCanonicalPath() + File.separator + "logs");
        if (!flog.exists() || flog.listFiles().length == 0) {
            System.out.println("Logs dir is empty:" + flog.getCanonicalPath());
            return;
        }

        if (!new File(flog.getCanonicalPath() + File.separator + "cucumber.json").exists()) {
            File fenv = new File(flog.getCanonicalPath() + File.separator + "env_variables.json");
            try {
                if (fenv.exists()) {
                    Map env = new Gson().fromJson(Utils.readTextFile(fenv), Map.class);
                    String jenkinsURL = env.get("JENKINS_URL").toString();
                    String jobName = env.get("JOB_NAME").toString();
                    String build = env.get("BUILD_NUMBER").toString();
                    String link = jenkinsURL + "job/" + jobName + "/" + build;
                    String console = getBuildConsole(link);
                    System.out.println("Notifying admins: " + this.m_eeURL);
                    StringBuilder res = new StringBuilder();
                    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                        Hashtable<String, String> hparams = new Hashtable<>();
                        hparams.put("action", "sendMail");
                        hparams.put("_to", "_admin");
                        hparams.put("_msg", "Reported by Archiver : job " + jobName + ":" + build + "\n\n" + link +
                                "\n\n===== Log =====\n\n" + console);
                        hparams.put("_subject", "Abornormal Completion:" + jobName + ": " + env.get("NODE_NAME") + " " + new Date());
                        Utils.post(client, this.m_eeURL, hparams, new StringBuilder(), res);
                    } finally {
                        System.out.println("==> " + res);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return;
        }


        File fpushed = new File(ws.getCanonicalPath() + File.separator + "jenkins_admin.pushed");
        if (fpushed.exists())
            return;

        System.out.println(Utils.executeCMD("cd \"" + flog.getCanonicalPath() + "\" && tar -cvf log.tar *"));
        File flogtar = new File(flog.getCanonicalPath() + File.separator + "log.tar");
        System.out.println("Locating log.tar:" + flogtar.exists() + ":" + flogtar.getCanonicalPath() + ":" + flogtar.length());
        File ftempPushtar = new File(ftempfiles.getCanonicalFile() + File.separator +
                ws.getName().replaceAll(" ", "_") + "." +
                System.getenv("BUILD_NUMBER") + "." +
                System.currentTimeMillis() + ".tar");
        flogtar.renameTo(ftempPushtar);
        sendToServer(ftempPushtar, fpushed);
    }

    public void sendToServer(File ftempPushtar, File fpushed) throws Exception {
        System.out.println("pushing log.tar:" + ftempPushtar.getCanonicalPath());
        HashMap<String, String> hparams = new HashMap<>();
        hparams.put("file_name", "builds/" + InetAddress.getLocalHost().getHostAddress() + "." + ftempPushtar.getName());
        hparams.put("last_modified", ftempPushtar.lastModified() + "");
        Thread th = new PushLog(this, ftempPushtar, hparams, fpushed);
        th.run();
        th.join();
    }

    public void postToServer(Map hparams, StringBuilder cookies, StringBuilder result) throws Exception {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            Utils.post(client, this.m_eeURL, hparams, cookies, result);
        }
    }

    public static class ProcessWatchDog extends Thread {
        public ProcessWatchDog() {
            this.start();
        }

        public void run() {
            long ts = System.currentTimeMillis();
            long dur = 3 * 60 * 60 * 1000;
            while (System.currentTimeMillis() - ts < dur) {
                if (checkAborted()) {
                    System.out.println(Utils.executeCMD("taskkill /f /t /PID " + m_pid));
                    break;
                }
                Utils.threadSleep(60 * 1000, "RunFeature.ProcessWatchDog.run()");
            }
        }
    }

    public static class PushLog extends Thread {
        private File m_fpushed, m_pushObj;
        private RunFeature m_runFeature;

        public PushLog(RunFeature runFeature, File flogtar, HashMap hparams, File fpushed) throws Exception {
            this.m_runFeature = runFeature;
            this.m_fpushed = fpushed;
            Hashtable hobj = new Hashtable();
            hobj.put("flogtar", flogtar.getCanonicalPath());
            hobj.put("hparam", hparams);
            String parent = flogtar.getAbsoluteFile().getParent();
            this.m_pushObj = new File(parent + File.separator + "push.obj." + System.currentTimeMillis());
            Utils.writeBinaryFile(new Gson().toJson(hobj).getBytes(), this.m_pushObj, false);
        }

        public void run() {
            try {
                pushLogObj(this.m_pushObj);
            } catch (Exception ex) {
                if (m_fpushed != null)
                    m_fpushed.delete();
                ex.printStackTrace();
            }
        }

        public void pushLogObj(File fpushObj) throws Exception {
            FileInputStream fis = null;
            try {
                Map hpushObj = new Gson().fromJson(Utils.readTextFile(fpushObj).toString(), Map.class);
                File flogTar = new File(hpushObj.get("flogtar").toString());
                System.out.println("-->Pushing " + flogTar.getCanonicalPath());
                fis = new FileInputStream(flogTar);
                StringBuilder cookies = new StringBuilder();
                StringBuilder result = new StringBuilder();
                Map hparams = (Map) hpushObj.get("hparam");
                hparams.put("action", "uploadFile1");
                hparams.put("append", "false");
                byte buffer[] = new byte[500 * 1024];
                int read = 0, total = 0;
                while ((read = fis.read(buffer)) != -1) {
                    byte[] wbuffer = new byte[read];
                    System.arraycopy(buffer, 0, wbuffer, 0, read);
                    hparams.put("file_data", Base64.encode(wbuffer));
                    this.m_runFeature.postToServer(hparams, cookies, result);
                    hparams.put("append", "true");
//					System.out.println("-->result: " + result);
                    if (result.toString().toLowerCase().contains("error"))
                        throw new Exception(result.toString());
                    total += read;
                    System.out.print(".");
                    Utils.threadSleep(200, null);
                }

                hparams.put("append", "close");
                hparams.remove("file_data");
                this.m_runFeature.postToServer(hparams, cookies, result);
                System.out.println("File uploaded:" + flogTar.getCanonicalPath() + ":" + total + ":" + hparams.get("file_name") + "\n" + hparams);
                flogTar.delete();
                fpushObj.delete();
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
