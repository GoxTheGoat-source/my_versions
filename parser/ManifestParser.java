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

            System.out.println("Génération du fichier index.html...");
            generateApacheHTML(versions);

            System.out.println("Terminé ! Le fichier index.html a été généré avec succès.");

        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de l'exécution :");
            e.printStackTrace();
        }
    }

    // 1. Fonction pour télécharger le JSON de Mojang
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

    // 2. Fonction pour extraire l'ID de chaque version via Regex
    private static List<String> parseVersions(String json) {
        List<String> versionsList = new ArrayList<>();
        
        // Ce pattern cherche la clé "id": "nom_de_la_version" dans le JSON
        Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String versionId = matcher.group(1);
            versionsList.add(versionId);
        }
        return versionsList;
    }

    // 3. Fonction pour écrire le fichier HTML au bon endroit
    private static void generateApacheHTML(List<String> versions) throws Exception {
        // Définition du fichier cible dans le chemin énoncé
        File targetFile = new File("myfiles/minecraft/index.html");
        
        // Sécurité : Crée les dossiers 'myfiles' et 'minecraft' s'ils n'existent pas encore
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // On passe directement le fichier cible au BufferedWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            
            // Entête HTML et Style CSS
            writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n");
            writer.write("<html>\n<head>\n");
            writer.write("    <title>Index of /myfiles/minecraft/</title>\n");
            writer.write("    <style>body, td, th { font-family: Verdana; font-size: 13px; }</style>\n");
            writer.write("</head>\n<body>\n");
            writer.write("    <h1>Index of /myfiles/minecraft/</h1>\n");
            
            // Début du tableau Apache
            writer.write("    <table>\n");
            writer.write("        <tr><th valign=\"top\"><img src=\"https://www.apache.org/icons/blank.gif\" alt=\"[ICO]\"></th>");
            writer.write("<th><a href=\"#\">Name</a></th><th><a href=\"#\">Size</a></th><th><a href=\"#\">Description</a></th></tr>\n");
            writer.write("        <tr><th colspan=\"4\"><hr></th></tr>\n");
            
            // Ligne du Parent Directory
            writer.write("        <tr><td valign=\"top\"><img src=\"https://www.apache.org/icons/back.gif\" alt=\"[PARENTDIR]\"></td>");
            writer.write("<td><a href=\"../\">Parent Directory</a></td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");

            // Boucle pour insérer chaque version trouvée
            for (String version : versions) {
                writer.write("        <tr>\n");
                writer.write("            <td valign=\"top\"><img src=\"https://www.apache.org/icons/folder.gif\" alt=\"[DIR]\"></td>\n");
                writer.write("            <td><a href=\"" + version + "\">" + version + "</a></td>\n");
                writer.write("            <td align=\"right\">131</td>\n"); 
                writer.write("            <td>&nbsp;</td>\n");
                writer.write("        </tr>\n");
            }

            // Fin du tableau et du fichier
            writer.write("        <tr><th colspan=\"4\"><hr></th></tr>\n");
            writer.write("    </table>\n");
            writer.write("</body>\n</html>\n");
        }
    }
}
