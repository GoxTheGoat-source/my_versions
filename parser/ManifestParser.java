package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManifestParser {

    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    // Optimisation : Compilation unique des patterns pour éviter la surcharge CPU dans les boucles
    private static final Pattern MAIN_MANIFEST_PATTERN = Pattern.compile("\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"type\"\\s*:\\s*\"[^\"]+\"\\s*,\\s*\"url\"\\s*:\\s*\"([^\"]+)\"");

    static class VersionInfo {
        String id;
        String url;

        VersionInfo(String id, String url) {
            this.id = id;
            this.url = url;
        }
    }

    static class FileInfo {
        String url;
        String size;
        String sha1;

        FileInfo(String url, String rawSize, String sha1) {
            this.url = url;
            this.size = formatSize(rawSize);
            this.sha1 = (sha1 != null) ? sha1 : "-";
        }

        private String formatSize(String rawSize) {
            if (rawSize == null || rawSize.isEmpty()) return "-";
            try {
                long bytes = Long.parseLong(rawSize);
                if (bytes < 1024) return bytes + " B";
                if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            } catch (NumberFormatException e) {
                return "-";
            }
        }
    }

    static class LibData {
        String nameId;
        String jarName;
        FileInfo fileInfo;

        LibData(String nameId, String jarName, FileInfo fileInfo) {
            this.nameId = nameId;
            this.jarName = jarName;
            this.fileInfo = fileInfo;
        }
    }

    static class AssetIndexData {
        String id;
        FileInfo fileInfo;

        AssetIndexData(String id, FileInfo fileInfo) {
            this.id = id;
            this.fileInfo = fileInfo;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Téléchargement du manifeste principal Mojang...");
            String mainJson = fetchJSON(MANIFEST_URL);

            System.out.println("Extraction des URLs des versions...");
            List<VersionInfo> versions = parseMainManifest(mainJson);

            System.out.println("Génération de l'arborescence globale...");
            generateStructureAndHTML(versions);

            System.out.println("Terminé ! Le site a été mis à jour.");

        } catch (Exception e) {
            System.err.println("Erreur critique :");
            e.printStackTrace();
        }
    }

    private static String fetchJSON(String urlString) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Minecraft-Manifest-Parser");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private static List<VersionInfo> parseMainManifest(String json) {
        List<VersionInfo> list = new ArrayList<>();
        Matcher matcher = MAIN_MANIFEST_PATTERN.matcher(json);

        while (matcher.find()) {
            list.add(new VersionInfo(matcher.group(1), matcher.group(2)));
        }
        return list;
    }

    private static void generateStructureAndHTML(List<VersionInfo> versions) throws Exception {
        // Étape 1 : On génère d'abord tous les dossiers enfants
        for (VersionInfo v : versions) {
            generateVersionFolders(v);
        }

        // Étape 2 : Écriture de l'index principal racine
        File mainIndexFile = new File("myfiles/minecraft/index.html");
        ensureDirectoryExists(mainIndexFile.getParentFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mainIndexFile))) {
            writeHtmlHeader(writer, "Index of /myfiles/minecraft/", "Index of /myfiles/minecraft/");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");

            for (VersionInfo v : versions) {
                File versionDir = new File("myfiles/minecraft/" + v.id);
                String folderItemsCount = countItemsInDirectory(versionDir);

                writer.write("        <tr>\n");
                writer.write("            <td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td>\n");
                writer.write("            <td><a href=\"" + v.id + "/\">" + v.id + "</a></td>\n");
                writer.write("            <td align=\"right\">" + folderItemsCount + "</td>\n");
                writer.write("            <td>&nbsp;</td>\n");
                writer.write("            <td>&nbsp;</td>\n");
                writer.write("        </tr>\n");
            }
            writeHtmlFooter(writer);
        }
    }

    private static void generateVersionFolders(VersionInfo v) {
        try {
            String versionPath = "myfiles/minecraft/" + v.id + "/";
            String versionJson = fetchJSON(v.url);
            
            FileInfo client = extractDownloadDetails(versionJson, "client");
            FileInfo server = extractDownloadDetails(versionJson, "server");
            FileInfo windowsServer = extractDownloadDetails(versionJson, "windows_server");
            FileInfo clientTxt = extractDownloadDetails(versionJson, "client_mappings");
            FileInfo serverTxt = extractDownloadDetails(versionJson, "server_mappings");

            List<LibData> libraries = extractLibrariesDetails(versionJson);
            AssetIndexData assetIndex = extractAssetIndexDetails(versionJson);

            File downloadsDir = new File(versionPath + "downloads");
            File libsDir = new File(versionPath + "libs");
            File assetsLinkDir = new File(versionPath + "assets");
            ensureDirectoryExists(downloadsDir);
            ensureDirectoryExists(libsDir);
            ensureDirectoryExists(assetsLinkDir);

            int downloadsCount = 0;

            // --- Index du dossier 'downloads' ---
            File downloadsIndex = new File(downloadsDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(downloadsIndex))) {
                writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + v.id + "/downloads/", "Index of /myfiles/minecraft/" + v.id + "/downloads/");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                
                if (client != null) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[JAR]\"></td><td><a href=\"" + client.url + "\">client.jar</a></td><td align=\"right\">" + client.size + "</td><td>client</td><td>" + client.sha1 + "</td></tr>\n");
                    downloadsCount++;
                }
                if (server != null) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[JAR]\"></td><td><a href=\"" + server.url + "\">server.jar</a></td><td align=\"right\">" + server.size + "</td><td>server</td><td>" + server.sha1 + "</td></tr>\n");
                    downloadsCount++;
                }
                if (windowsServer != null) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[EXE]\"></td><td><a href=\"" + windowsServer.url + "\">windows_server.exe</a></td><td align=\"right\">" + windowsServer.size + "</td><td>windows_server</td><td>" + windowsServer.sha1 + "</td></tr>\n");
                    downloadsCount++;
                }
                if (clientTxt != null) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/text.gif\" alt=\"[TXT]\"></td><td><a href=\"" + clientTxt.url + "\">client.txt</a></td><td align=\"right\">" + clientTxt.size + "</td><td>mapping</td><td>" + clientTxt.sha1 + "</td></tr>\n");
                    downloadsCount++;
                }
                if(serverTxt != null) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/text.gif\" alt=\"[TXT]\"></td><td><a href=\"" + serverTxt.url + "\">client.txt</a></td><td align=\"right\">" + serverTxt.size + "</td><td>mapping</td><td>" + serverTxt.sha1 + "</td></tr>\n");
                    downloadsCount++;
                }
                writeHtmlFooter(writer);
            }

            // --- Index du dossier 'libs' ---
            int libsCount = libraries.size();
            File libsIndex = new File(libsDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(libsIndex))) {
                writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + v.id + "/libs/", "Index of /myfiles/minecraft/" + v.id + "/libs/");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                
                for (LibData lib : libraries) {
                    String safeFolderName = lib.nameId.replace(":", "-");
                    File individualLibDir = new File(libsDir, safeFolderName);
                    ensureDirectoryExists(individualLibDir);

                    File individualLibIndex = new File(individualLibDir, "index.html");
                    try (BufferedWriter libWriter = new BufferedWriter(new FileWriter(individualLibIndex))) {
                        writeHtmlHeader(libWriter, "Index of /myfiles/minecraft/" + v.id + "/libs/" + safeFolderName + "/", "Index of /myfiles/minecraft/" + v.id + "/libs/" + safeFolderName + "/");
                        libWriter.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                        libWriter.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[JAR]\"></td><td><a href=\"" + lib.fileInfo.url + "\">" + lib.jarName + "</a></td><td align=\"right\">" + lib.fileInfo.size + "</td><td>artifact</td><td>" + lib.fileInfo.sha1 + "</td></tr>\n");
                        writeHtmlFooter(libWriter);
                    }

                    // Le texte affiché devient l'ID d'origine (avec les ':') et le href pointe vers le dossier sécurisé
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"" + safeFolderName + "/\">" + safeFolderName + "</a></td><td align=\"right\">1 item</td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                }
                writeHtmlFooter(writer);
            }

            // --- Index du dossier 'assets' orienté mcasset.cloud (GitHub) ---
            int assetsCount = 0;
            if (assetIndex != null) {
                File localAssetsIndex = new File(assetsLinkDir, "index.html");
                try (BufferedWriter assetLocalWriter = new BufferedWriter(new FileWriter(localAssetsIndex))) {
                    writeHtmlHeader(assetLocalWriter, "Index of /myfiles/minecraft/" + v.id + "/assets/", "Index of /myfiles/minecraft/" + v.id + "/assets/");
                    assetLocalWriter.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                    
                    // Redirection externe cliquable vers les ressources déshachées et structurées sur GitHub
                    String mcAssetsGithubUrl = "https://github.com/InventiveTalentDev/minecraft-assets/tree/"+v.id+"/assets/";
                    
                    assetLocalWriter.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/link.gif\" alt=\"[LNK]\"></td><td><a href=\"" + mcAssetsGithubUrl + "\">Redirect Link</a></td><td align=\"right\">  - </td><td>minecraft-assets/assets at "+v.id+" · InventivetalentDev/minecraft-assets</td><td>&nbsp;</td></tr>\n");
                    writeHtmlFooter(assetLocalWriter);
                }
                assetsCount = 1;
            }

            // --- Index de la Version ---
            File versionIndex = new File(versionPath + "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(versionIndex))) {
                writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + v.id + "/", "Index of /myfiles/minecraft/" + v.id + "/");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                
                String downloadsLabel = downloadsCount + (downloadsCount > 1 ? " items" : " item");
                String libsLabel = libsCount + (libsCount > 1 ? " items" : " item");
                String assetsLabel = assetsCount + (assetsCount > 1 ? " items" : " item");
                
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"downloads/\">downloads</a></td><td align=\"right\">" + downloadsLabel + "</td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"libs/\">libs</a></td><td align=\"right\">" + libsLabel + "</td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"assets/\">assets</a></td><td align=\"right\">" + assetsLabel + "</td><td>&nbsp;</td><td>&nbsp;</td></tr>\n");
                writeHtmlFooter(writer);
            }

        } catch (Exception e) {
            System.err.println("Passage de la version " + v.id + " suite à une erreur de lecture.");
        }
    }

    private static FileInfo extractDownloadDetails(String json, String type) {
        Pattern pBlock = Pattern.compile("\"" + type + "\"\\s*:\\s*\\{([^}]+)\\}");
        Matcher mBlock = pBlock.matcher(json);
        
        if (mBlock.find()) {
            String blockContent = mBlock.group(1);
            String sha1 = extractField(blockContent, "sha1");
            String size = extractField(blockContent, "size");
            String url = extractField(blockContent, "url");
            
            if (!url.isEmpty()) {
                return new FileInfo(url, size, sha1);
            }
        }
        return null;
    }

    private static AssetIndexData extractAssetIndexDetails(String json) {
        Pattern pBlock = Pattern.compile("\"assetIndex\"\\s*:\\s*\\{([^}]+)\\}");
        Matcher mBlock = pBlock.matcher(json);

        if (mBlock.find()) {
            String blockContent = mBlock.group(1);
            String id = extractField(blockContent, "id");
            String sha1 = extractField(blockContent, "sha1");
            String size = extractField(blockContent, "size");
            String url = extractField(blockContent, "url");

            if (!url.isEmpty() && !id.isEmpty()) {
                return new AssetIndexData(id, new FileInfo(url, size, sha1));
            }
        }
        return null;
    }

    private static List<LibData> extractLibrariesDetails(String json) {
        List<LibData> list = new ArrayList<>();
        
        Pattern pLibsBlock = Pattern.compile("\"libraries\"\\s*:\\s*\\[(.*)\\]\\s*,\\s*\"logging\"");
        Matcher mLibsBlock = pLibsBlock.matcher(json);
        
        String libsContent = "";
        if (mLibsBlock.find()) {
            libsContent = mLibsBlock.group(1);
        } else {
            Pattern pAlternative = Pattern.compile("\"libraries\"\\s*:\\s*\\[(.*)\\]");
            Matcher mAlternative = pAlternative.matcher(json);
            if (mAlternative.find()) {
                libsContent = mAlternative.group(1);
            }
        }

        if (!libsContent.isEmpty()) {
            Pattern pSingleLib = Pattern.compile("\\{\\s*\"downloads\"\\s*:\\s*\\{[^}]*\"artifact\"\\s*:\\s*\\{([^}]+)\\}[^}]*\\}\\s*,\\s*\"name\"\\s*:\\s*\"([^\"]+)\"");
            Matcher mSingleLib = pSingleLib.matcher(libsContent);
            
            while (mSingleLib.find()) {
                String artifactBlock = mSingleLib.group(1);
                String nameId = mSingleLib.group(2);
                
                String url = extractField(artifactBlock, "url");
                String size = extractField(artifactBlock, "size");
                String sha1 = extractField(artifactBlock, "sha1");
                String path = extractField(artifactBlock, "path");
                
                if (!url.isEmpty()) {
                    String filename = path.substring(path.lastIndexOf('/') + 1);
                    if (filename.isEmpty()) {
                        filename = "library.jar";
                    }
                    list.add(new LibData(nameId, filename, new FileInfo(url, size, sha1)));
                }
            }
        }
        return list;
    }

    private static String extractField(String block, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher m = p.matcher(block);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    private static String countItemsInDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                int count = 0;
                for (String name : list) {
                    if (!name.equals("index.html")) {
                        count++;
                    }
                }
                return count + (count > 1 ? " items" : " item");
            }
        }
        return "0 items";
    }

    private static void ensureDirectoryExists(File dir) {
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void writeHtmlHeader(BufferedWriter writer, String title, String heading) throws Exception {
        writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n<html>\n<head>\n");
        writer.write("    <title>" + title + "</title>\n");
        writer.write("    <style>body, td, th { font-family: Verdana; font-size: 13px; text-align: left; } th { font-weight: bold; }</style>\n</head>\n<body>\n");
        writer.write("    <h1>" + heading + "</h1>\n    <table>\n");
        writer.write("        <tr><th valign=\"top\"><img src=\"https://www.apache.org/icons/blank.gif\" alt=\"[ICO]\"></th><th><a href=\"#\">Name</a></th><th><a href=\"#\">Size</a></th><th><a href=\"#\">Description</a></th><th><a href=\"#\">SHA-1</a></th></tr>\n");
        writer.write("        <tr><th colspan=\"5\"><hr></th></tr>\n");
    }

    private static void writeHtmlFooter(BufferedWriter writer) throws Exception {
        writer.write("        <tr><th colspan=\"5\"><hr></th></tr>\n    </table>\n</body>\n</html>\n");
    }
}
