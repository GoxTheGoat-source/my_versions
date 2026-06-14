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

    public static void main(String[] args) {
        try {
            System.out.println("Téléchargement du manifeste Mojang...");
            String jsonContent = fetchJSON(MANIFEST_URL);

            System.out.println("Extraction des versions...");
            List<String> versions = parseVersions(jsonContent);

            System.out.println("Génération de l'arborescence et des fichiers HTML...");
            generateStructureAndHTML(versions);

            System.out.println("Terminé ! Toute l'arborescence a été créée avec succès.");

        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de l'exécution :");
            e.printStackTrace();
        }
    }

    private static String fetchJSON(String urlString) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Minecraft-Manifest-Parser");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private static List<String> parseVersions(String json) {
        List<String> versionsList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            versionsList.add(matcher.group(1));
        }
        return versionsList;
    }

    private static void generateStructureAndHTML(List<String> versions) throws Exception {
        // 1. Génération de l'index principal : myfiles/minecraft/index.html
        File mainIndexFile = new File("myfiles/minecraft/index.html");
        ensureDirectoryExists(mainIndexFile.getParentFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mainIndexFile))) {
            writeHtmlHeader(writer, "Index of /myfiles/minecraft/", "Index of /myfiles/minecraft/");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");

            for (String version : versions) {
                writer.write("        <tr>\n");
                writer.write("            <td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td>\n");
                writer.write("            <td><a href=\"" + version + "/\">" + version + "/</a></td>\n");
                writer.write("            <td align=\"right\">131</td>\n");
                writer.write("            <td>&nbsp;</td>\n");
                writer.write("        </tr>\n");

                // 2. Génération de la sous-structure pour CHAQUE version
                generateVersionFolders(version);
            }
            writeHtmlFooter(writer);
        }
    }

    private static void generateVersionFolders(String version) throws Exception {
        String versionPath = "myfiles/minecraft/" + version + "/";

        // --- Index de la Version (contient 'downloads/' et 'libs/') ---
        File versionIndex = new File(versionPath + "index.html");
        ensureDirectoryExists(versionIndex.getParentFile());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(versionIndex))) {
            writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + version + "/", "Index of /myfiles/minecraft/" + version + "/");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"downloads/\">downloads/</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"libs/\">libs/</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writeHtmlFooter(writer);
        }

        // --- Index du dossier 'downloads' (pour client, server, windows_server, client.txt) ---
        File downloadsIndex = new File(versionPath + "downloads/index.html");
        ensureDirectoryExists(downloadsIndex.getParentFile());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(downloadsIndex))) {
            writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + version + "/downloads/", "Index of /myfiles/minecraft/" + version + "/downloads/");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            
            // Fichiers prêts à recevoir tes futurs liens directs
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[BIN]\"></td><td><a href=\"client.jar\">client.jar</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[BIN]\"></td><td><a href=\"server.jar\">server.jar</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[BIN]\"></td><td><a href=\"windows_server.exe\">windows_server.exe</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/text.gif\" alt=\"[TXT]\"></td><td><a href=\"client.txt\">client.txt</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writeHtmlFooter(writer);
        }

        // --- Index du dossier 'libs' (vide pour le moment) ---
        File libsIndex = new File(versionPath + "libs/index.html");
        ensureDirectoryExists(libsIndex.getParentFile());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(libsIndex))) {
            writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + version + "/libs/", "Index of /myfiles/minecraft/" + version + "/libs/");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            writeHtmlFooter(writer);
        }
    }

    private static void ensureDirectoryExists(File dir) {
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void writeHtmlHeader(BufferedWriter writer, String title, String heading) throws Exception {
        writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n");
        writer.write("<html>\n<head>\n");
        writer.write("    <title>" + title + "</title>\n");
        writer.write("    <style>body, td, th { font-family: Verdana; font-size: 13px; }</style>\n");
        writer.write("</head>\n<body>\n");
        writer.write("    <h1>" + heading + "</h1>\n");
        writer.write("    <table>\n");
        writer.write("        <tr><th valign=\"top\"><img src=\"https://www.apache.org/icons/blank.gif\" alt=\"[ICO]\"></th>");
        writer.write("<th><a href=\"#\">Name</a></th><th><a href=\"#\">Size</a></th><th><a href=\"#\">Description</a></th></tr>\n");
        writer.write("        <tr><th colspan=\"4\"><hr></th></tr>\n");
    }

    private static void writeHtmlFooter(BufferedWriter writer) throws Exception {
        writer.write("        <tr><th colspan=\"4\"><hr></th></tr>\n");
        writer.write("    </table>\n");
        writer.write("</body>\n</html>\n");
    }
}
