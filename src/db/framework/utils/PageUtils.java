package db.framework.utils;

import db.framework.runner.MainRunner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class pulls & manages data from page & panel JSON files
 */
public class PageUtils {
    protected static HashMap<String, JSONObject> cachePagesProject = new HashMap<>();
    protected static HashMap<String, JSONObject> cachePagesShared = new HashMap<>();

    /**
     * Prints out the values of all saved pages/panels
     */
    public static void displayPageJSONHash() {
        for (Map.Entry mapEntry : cachePagesProject.entrySet()) {
            System.out.println("project page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
        for (Map.Entry mapEntry : cachePagesProject.entrySet()) {
            System.out.println("shared page cache: key: '" + mapEntry.getKey() + "' Value: '" + mapEntry.getValue() + "'");
        }
    }

    /**
     * Loads a JSON object from file into memory
     * <p>
     * This method first looks in your project's directory (passed as environment variable "project"), then in the
     * </p>
     *
     * @param pagePath name of page to load
     */
    public static void loadPageJSON(String pagePath) {
        if (cachePagesProject.get(pagePath) != null || cachePagesShared.get(pagePath) != null)
            return;

        String resRepoPath = "src/";
        if (!new File(resRepoPath).exists())
            resRepoPath = "./";
        String path = pagePath.replace(".page.", ".pages.").replace(".panel.", ".panels.").replace(".", "/");
        String resPath = "/resources/elements/" + path + ".json";

        // project elements first
        if (MainRunner.projectDir != null) {
            path = resRepoPath + MainRunner.projectDir.replace(".", "/") + resPath;
            loadOnePageJSONFile(pagePath, path, "project");

            // also load panel elements
            if (pagePath.contains(".page.")) {
                path = path.replace("/pages/", "/panels/");
                loadOnePageJSONFile(pagePath, path, "project");
            }
        }

        // shared elements next
        path = resRepoPath + "db/shared" + resPath;
        loadOnePageJSONFile(pagePath, path, "shared");

        // also load panel elements
        if (pagePath.contains(".page.")) {
            path = path.replace("/pages/", "/panels/");
            loadOnePageJSONFile(pagePath, path, "shared");
        }

        // project elements first
        if (MainRunner.projectDir != null) {
            path = resRepoPath + MainRunner.projectDir.replace(".", "/") + resPath;
            loadOnePageJSONFile(pagePath, path, "project");

            // also load panel elements
            if (pagePath.contains(".page.")) {
                path = path.replace("/pages/", "/panels/");
                loadOnePageJSONFile(pagePath, path, "project");
            }
        }

        // shared elements next
        path = resRepoPath + "db/shared" + resPath;
        loadOnePageJSONFile(pagePath, path, "shared");

        // also load panel elements
        if (pagePath.contains(".page.")) {
            path = path.replace("/pages/", "/panels/");
            loadOnePageJSONFile(pagePath, path, "shared");
        }
    }

    private static boolean loadOnePageJSONFile(String pagePath, String filePath, String cache) {
        File f = new File(filePath);
        if (f.exists() && !f.isDirectory()) {
            loadPageJsonFiles(pagePath, f, cache);
            return true;
        }

        // find file recursively under the directory
        String fName = f.getName();
        File dir = f.getParentFile();
        f = findPage(dir, fName);

        if (f != null && f.exists() && !f.isDirectory()) {
            int fileCount = countFoundPage(dir, fName);
            if (fileCount < 1)
                return false;
            if (fileCount == 1) {
                loadPageJsonFiles(pagePath, f, cache);
                return true;
            } else {
                Assert.fail("Resource Error: Multiple '" + fName + "'(total: " + fileCount + ") " +
                        " files found under '" + dir.getAbsolutePath() + "'");
            }
        }
        return false;
    }

    // recursively checks all subdirectories for a file matching pageName
    private static File findPage(File dir, String pageName) {
        File[] subDirs = dir.listFiles(File::isDirectory);
        File[] resources = dir.listFiles(File::isFile);
        if (resources != null) {
            for (File resource : resources) {
                if (resource.getName().equals(pageName))
                    return resource;
            }
        }
        if (subDirs == null)
            return null;
        File resource = null;
        for (File subDir : subDirs) {
            resource = findPage(subDir, pageName);
            if (resource != null && resource.getName().equals(pageName))
                break;
        }
        return resource;
    }

    // recursively checks all subdirectories for a file matching pageName
    private static int countFoundPage(File dir, String pageName) {
        int count = 0;
        File[] subDirs = dir.listFiles(File::isDirectory);
        File[] resources = dir.listFiles(File::isFile);
        if (resources != null) {
            for (File resource : resources) {
                if (resource.getName().equals(pageName))
                    count++;
            }
        }
        if (subDirs == null)
            return count;

        for (File subDir : subDirs) {
            int subCount = countFoundPage(subDir, pageName);
            count += subCount;
        }
        return count;
    }

    private static void loadPageJsonFiles(String pagePath, File file, String cache) {
        if (cache.equals("project")) {
            if (cachePagesProject.get(pagePath) != null)
                return;
        } else {
            if (cachePagesShared.get(pagePath) != null)
                return;
        }

        JSONObject pageJson;
        try {
            pageJson = new JSONObject(Utils.readTextFile(file));
        } catch (IOException | JSONException e) {
            System.err.println("-->Error parsing json at PageUtils.loadPageJSON() for page: " + file.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        // put new DataFile entry
        if (cache.equals("project"))
            cachePagesProject.put(pagePath, pageJson);
        else
            cachePagesShared.put(pagePath, pageJson);

        // process included Panel files
        JSONArray includedDataFiles = null;
        try {
            includedDataFiles = pageJson.getJSONArray("include");
        } catch (JSONException e) {
            // no 'include'
        }

        if (includedDataFiles == null)
            return;

        for (int i = 0; i < includedDataFiles.length(); i++) {
            try {
                String panelName = includedDataFiles.getString(i);
                if (panelName.contains("panel.")) {
                    String panel_name = panelName.replace("panel.", "");
                    String[] parts = pagePath.split(Pattern.quote("."));
                    String panel_path = parts[0] + "." + parts[1] + ".panel." + panel_name;
                    loadPageJSON(panel_path);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the value of an element
     *
     * @param elementKey String selector in format "page_name.element_name"
     * @return Value of the element
     */
    public static String findPageData(String elementKey) {
        PageElement element = new PageElement(elementKey);
        return findPageElement(element);
    }

    /**
     * Finds JSON entry value from JSON object
     *
     * @param element PageElement containing data to find
     * @return Value of the element
     */
    //
    public static String findPageElementValue(PageElement element) {
        // load the page element data
        String pageName = element.getPageName();
        String elementName = element.getElementName();
        if (pageName == null || elementName == null)
            return null;

        String page_path = getPageFullPath(pageName);
        loadPageJSON(page_path);

        // search element value
        return findPageJSONValue(page_path, elementName);
    }

    /**
     * Finds JSON entry value from JSON object and strips selector info
     *
     * @param element PageElement containing data to find
     * @return Value of the element
     */
    public static String findPageElement(PageElement element) {
        // search element value
        String elementString = findPageElementValue(element);
        if (elementString == null)
            return null;

        return String.valueOf(element.parseValue(elementString));
    }

    // get element value from JSON object in memory
    private static String findPageJSONValue(String pagePath, String elementName) {
        String result;
        result = findCachePageJSONValue(pagePath, elementName);

        // try panel
        if (result == null && pagePath.contains(".page."))
            result = findCachePageJSONValue(pagePath.replace(".page.", ".panel."), elementName);

        return result;
    }

    private static String findCachePageJSONValue(String pagePath, String elementName) {
        String result = findPageJSONValueInternal(pagePath, elementName, "project");
        return result != null ? result : findPageJSONValueInternal(pagePath, elementName, "shared");
    }

    private static String findPageJSONValueInternal(String pagePath, String elementName, String cache) {
        String result = null;
        JSONObject pageData;
        if (cache.equals("project"))
            pageData = cachePagesProject.get(pagePath);
        else
            pageData = cachePagesShared.get(pagePath);

        try {
            result = (String) pageData.get(elementName);
        } catch (Exception e) {
            // skip any error
        }

        if (result != null)
            return result;

        // search in panels
        JSONArray includedDataFiles = null;
        try {
            includedDataFiles = pageData.getJSONArray("include");
        } catch (Exception e) {
            // no 'include'
        }

        if (includedDataFiles == null) {
            // System.out.println("No value found for " + pageName + "." + elementName);
            return result;
        }

        int count = includedDataFiles.length();
        for (int i = 0; i < count; i++) {
            try {
                String panelName = includedDataFiles.getString(i);
                if (panelName.contains("panel.")) {
                    String panel_name = panelName.replace("panel.", "");
                    String[] parts = pagePath.split(Pattern.quote("."));
                    String panel_path = parts[0] + "." + parts[1] + ".panel." + panel_name;
                    result = findPageJSONValueInternal(panel_path, elementName, cache);
                    if (result != null)
                        return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.out.println("No value found for " + pagePath + "." + elementName);
        return result;
    }

    // make 'home' or 'panel.home' to 'website.mcom.page.home' or 'website.mcom.panel.home'
    public static String getPageFullPath(String pageName) {
        String pagePath = "";
        if (pageName.contains("panels."))
            pagePath = pagePath + pageName;
        else
            pagePath = pagePath + "pages." + pageName;
        return pagePath;
    }
}
