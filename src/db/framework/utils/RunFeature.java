package db.framework.utils;

import com.google.gson.Gson;
import db.framework.runner.MainRunner;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RunFeature {
    private static final String m_version = "1a.0001";
    private static int m_pid;
    private File m_repo_jar;
    private String m_workspace;

    public RunFeature() throws Throwable {
        int remoteDebugDelay = Utils.parseInt(System.getenv("REMOTE_DEBUG_DELAY"), 0);
        if (remoteDebugDelay > 0) {
            Utils.threadSleep(remoteDebugDelay * 1000, "Remote debug delay:" + remoteDebugDelay);
        }

        System.out.println("RunFeature version: " + m_version);
        m_pid = Utils.getProcessId();
        this.m_workspace = System.getenv("WORKSPACE");
        this.m_repo_jar = new File(this.m_workspace + "/" + System.getenv("repo_jar"));
        this.cleanWorkSpace();

        if (MainRunner.scenarios != null) {
            MainRunner.scenarios = MainRunner.scenarios.replaceAll("features/", System.getenv("db_project").trim().replace(".", "/") + "/features/");
        }

        try {
            System.out.println("\n\nInitializing MainRunner()...");
            MainRunner.main(null);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        System.exit(MainRunner.runStatus);
    }

    public static void main(String[] args) {
        try {
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
        if (System.getenv("BUILD_URL") == null) {
            return false;
        }
        try {
            String bstatus = Utils.httpGet(System.getenv("BUILD_URL") + "api/json", null);
            String result = (String) new Gson().fromJson(bstatus, Map.class).get("result");
            if (result == null) {
                result = "";
            }
            if (result.equals("ABORTED")) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void cleanWorkSpace() {
        System.err.println("-->cleanWorkSpace()...");
        try {
            File[] files = new File(m_workspace).listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.getName().equals(this.m_repo_jar.getName())) {
                    continue;
                }
                System.out.println("--> removing " + f.getPath());
                if (f.isDirectory()) {
                    try {
                        FileUtils.cleanDirectory(f);
                    } catch (IOException iex) {
                        System.err.println("-->Cannot clean " + f.getPath() + ":" + iex.getMessage());
                        continue;
                    }
                }
                if (!f.delete()) {
                    System.err.println("Failed to delete file: " + f.getPath());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
}
