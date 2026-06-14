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

    // Classe interne pour stocker les infos de chaque version
    static class VersionInfo {
        String id;
        String url;

        VersionInfo(String id, String url) {
            this.id = id;
            this.url = url;
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

    // Extrait à la fois l'ID ET l'URL du manifeste principal
    private static List<VersionInfo> parseMainManifest(String json) {
        List<VersionInfo> list = new ArrayList<>();
        // Regex pour capturer l'id et l'url dans le tableau "versions"
        Pattern pattern = Pattern.compile("\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"type\"\\s*:\\s*\"[^\"]+\"\\s*,\\s*\"url\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            list.add(new VersionInfo(matcher.group(1), matcher.group(2)));
        }
        return list;
    }

    private static void generateStructureAndHTML(List<VersionInfo> versions) throws Exception {
        File mainIndexFile = new File("myfiles/minecraft/index.html");
        ensureDirectoryExists(mainIndexFile.getParentFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mainIndexFile))) {
            writeHtmlHeader(writer, "Index of /myfiles/minecraft/", "Index of /myfiles/minecraft/");
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");

            // Pour limiter le temps d'exécution sur GitHub, on peut traiter en priorité les 50 dernières versions
            // Ou tout traiter si le réseau suit. Ici on traite tout.
            for (VersionInfo v : versions) {
                writer.write("        <tr>\n");
                writer.write("            <td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td>\n");
                writer.write("            <td><a href=\"" + v.id + "/\">" + v.id + "/</a></td>\n");
                writer.write("            <td align=\"right\">-</td>\n");
                writer.write("            <td>&nbsp;</td>\n");
                writer.write("        </tr>\n");

                // Génération des sous-dossiers avec lecture de l'URL spécifique
                generateVersionFolders(v);
            }
            writeHtmlFooter(writer);
        }
    }

    private static void generateVersionFolders(VersionInfo v) {
        try {
            String versionPath = "myfiles/minecraft/" + v.id + "/";
            
            // 1. On télécharge le JSON spécifique de cette version pour lire l'objet "downloads"
            String versionJson = fetchJSON(v.url);
            
            // Extraction des URLs des composants via Regex
            String clientUrl = extractDownloadUrl(versionJson, "client");
            String serverUrl = extractDownloadUrl(versionJson, "server");
            String windowsServerUrl = extractDownloadUrl(versionJson, "windows_server");

            // --- Index de la Version ---
            File versionIndex = new File(versionPath + "index.html");
            ensureDirectoryExists(versionIndex.getParentFile());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(versionIndex))) {
                writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + v.id + "/", "Index of /myfiles/minecraft/" + v.id + "/");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"downloads/\">downloads/</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td><td><a href=\"libs/\">libs/</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
                writeHtmlFooter(writer);
            }

            // --- Index du dossier 'downloads' avec les vrais liens officiels ---
            File downloadsIndex = new File(versionPath + "downloads/index.html");
            ensureDirectoryExists(downloadsIndex.getParentFile());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(downloadsIndex))) {
                writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + v.id + "/downloads/", "Index of /myfiles/minecraft/" + v.id + "/downloads/");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
                
                if (!clientUrl.isEmpty()) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[BIN]\"></td><td><a href=\"" + clientUrl + "\">client.jar</a></td><td align=\"right\">  - </td><td>Lien Officiel Mojang</td></tr>\n");
                }
                if (!serverUrl.isEmpty()) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[BIN]\"></td><td><a href=\"" + serverUrl + "\">server.jar</a></td><td align=\"right\">  - </td><td>Lien Officiel Mojang</td></tr>\n");
                }
                if (!windowsServerUrl.isEmpty()) {
                    writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/binary.gif\" alt=\"[BIN]\"></td><td><a href=\"" + windowsServerUrl + "\">windows_server.exe</a></td><td align=\"right\">  - </td><td>Lien Officiel Mojang</td></tr>\n");
                }
                
                // client.txt reste pointé localement au cas où tu le rajoutes à la main
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/text.gif\" alt=\"[TXT]\"></td><td><a href=\"client.txt\">client.txt</a></td><td align=\"right\">  - </td><td>Configuration locale</td></tr>\n");
                writeHtmlFooter(writer);
            }

            // --- Index du dossier 'libs' ---
            File libsIndex = new File(versionPath + "libs/index.html");
            ensureDirectoryExists(libsIndex.getParentFile());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(libsIndex))) {
                writeHtmlHeader(writer, "Index of /myfiles/minecraft/" + v.id + "/libs/", "Index of /myfiles/minecraft/" + v.id + "/libs/");
                writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
                writeHtmlFooter(writer);
            }

        } catch (Exception e) {
            // Si une vieille version échoue (par exemple URL 404 chez Mojang), on passe à la suivante sans crasher
            System.err.println("Passage de la version " + v.id + " suite à une erreur de lecture.");
        }
    }

    // Utilitaire Regex pour cibler un type de download précis (client, server...)
    private static String extractDownloadUrl(String json, String type) {
        Pattern p = Pattern.compile("\"" + type + "\"\\s*:\\s*\\{[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.pattern().matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static void ensureDirectoryExists(File dir) {
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void writeHtmlHeader(BufferedWriter writer, String title, String heading) throws Exception {
        writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n<html>\n<head>\n");
        writer.write("    <title>" + title + "</title>\n");
        writer.write("    <style>body, td, th { font-family: Verdana; font-size: 13px; }</style>\n</head>\n<body>\n");
        writer.write("    <h1>" + heading + "</h1>\n    <table>\n");
        writer.write("        <tr><th valign=\"top\"><img src=\"https://www.apache.org/icons/blank.gif\" alt=\"[ICO]\"></th><th>Name</th><th>Size</th><th>Description</th></tr>\n");
        writer.write("        <tr><th colspan=\"4\"><hr></th></tr>\n");
    }

    private static void writeHtmlFooter(BufferedWriter writer) throws Exception {
        writer.write("        <tr><th colspan=\"4\"><hr></th></tr>\n    </table>\n</body>\n</html>\n");
    }
}
