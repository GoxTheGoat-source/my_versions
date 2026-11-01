# Minecraft ERROR422 — Cross-Platform & Deobfuscation Project

**Reverse engineering, Java bytecode analysis, cross-platform compatibility and software preservation.**

This is an independent research project focused on understanding, deobfuscating and improving the platform compatibility of the existing Minecraft ERROR422 game, originally based on Minecraft 1.5.2.

The project aims to investigate its heavily obfuscated Java structure, identify custom classes and methods, reconstruct useful mappings, and make the game runnable on different operating systems.

**This is not the original game, and this project does not claim authorship of it. It is an independent research and software preservation effort.**

---

## 🚀 Project Goals

- **Cross-Platform Compatibility:** Provide and investigate the native libraries required by Windows, Linux and macOS.
- **Java Bytecode Analysis:** Understand the original compiled classes without relying on unavailable source code.
- **Deobfuscation:** Identify custom classes, methods and fields and document their actual purposes.
- **Mapping Reconstruction:** Build and maintain mappings for the original Minecraft 1.5.2 code and ERROR422-specific modifications.
- **Software Preservation:** Keep this legacy Java game accessible and reproducible on different operating systems.

---

## 📊 Current Progress

### Cross-Platform Testing

The experimental JAR has been tested on the following configurations:

| Operating System | Architecture | Java Version | Status |
|---|---|---|---|
| Windows 10 (Build 10240) | x86 (32-bit) | Oracle Java 8u191 | Tested |
| Linux | x86_64 | OpenJDK 21 | Tested |
| macOS | Architecture not recorded | Java 21 | Reported working; further details required |
| macOS Apple Silicon | ARM64 | — | Native compatibility not confirmed |

### Windows Testing

Tested on Windows 10 Build 10240, x86, with Oracle Java 8u191.

- Minecraft successfully initialized LWJGL 2.4.2.
- The original bundled LWJGL OpenAL implementation initialized successfully.
- The integrated Minecraft server started and generated a new world.
- The player successfully connected to the integrated server.
- An initial run without a specified heap limit encountered `OutOfMemoryError` in `ChannelLWJGLOpenAL.queueBuffer()`, followed by an OpenGL out-of-memory error and an Intel graphics-driver crash (`igdusc32.dll`).
- A subsequent test using `-Xmx512M` successfully reached the world.
- A longer test using `-Xmx1G` remained running and eventually shut down cleanly.
- The server saved the Overworld, Nether and End.
- The SoundSystem shut down cleanly.

Performance warnings were still observed, including `Can't keep up!` and `Memory connection overburdened`.

**Important:** Successful OpenAL initialization does not, by itself, prove that all sounds were audible. Actual audio playback still requires further verification.

### Linux Testing

Tested on Linux x86_64 with OpenJDK 21.

- The game successfully launched with a graphical interface using a virtual X display.
- LWJGL 2.4.2 loaded its Linux native components.
- A new Survival world was created.
- The world was reopened and explored.
- The game successfully saved the world and shut down its integrated server.
- The `wlK` class received a platform-independent path correction using `File.separator`.
- The corrected class was tested for directory creation and extraction of the four OGG files.

Audio compatibility with the original bundled native libraries remains under investigation. An experimental OpenAL Soft replacement was also tested, but this does not constitute validation of the original audio setup.

### macOS Testing

A successful Java 21 test on macOS has been reported after adding a compatibility JAR containing Apple's Java extensions (`AppleJavaExtensions.jar`).

The exact macOS version, processor architecture and detailed test results have not yet been documented.

**Apple Silicon native ARM64 compatibility is not confirmed.** The presence of macOS-related Java classes and native libraries does not guarantee native ARM64 support.

Further testing is required to establish compatibility with Intel Macs and Apple Silicon, including the possible use of Rosetta.

---

## 🔬 Deobfuscation Progress

The project uses Enigma Legacy 0.10.4-beta to reconstruct mappings and investigate the original Java bytecode.

Current achievements:

- [x] Extracted base mappings from MCP 1.5.2 (`client.srg`).
- [x] Generated an Enigma `.jobf` structure file.
- [x] Identified multiple ERROR422-specific classes.
- [x] Investigated the original `ERR422Loader` and JAR loading mechanism.
- [x] Identified several important custom methods and fields.
- [x] Corrected the platform-dependent path handling in `wlK`.
- [ ] Complete the identification of unknown classes in `none/`.
- [ ] Reconstruct the remaining methods and fields.
- [ ] Validate all mappings against actual game behavior.

---

## 🧬 Known ERROR422-Specific Classes

The following mappings have been identified in the current Enigma `.jobf` structure.

| Obfuscated Name | Identified Class |
|---|---|
| `BuD` | `net/minecraft/err422/enums/EnumWorldEventHelper` |
| `Eya` | `net/minecraft/err422/enums/EnumWorldEvent` |
| `FqX` | `net/minecraft/err422/eventbus/EventBus` |
| `Jcz` | `net/minecraft/err422/util/TickTimeHelper` |
| `Lbz` | `net/minecraft/err422/util/KeyboardMonitor` |
| `OEk` | `net/minecraft/err422/gui/GuiTextBackground` |
| `QXI` | `net/minecraft/err422/ERR422Manager` |
| `UYQ` | `net/minecraft/err422/enums/EnumGlitchActionHelper` |
| `YRn` | `net/minecraft/err422/util/AntiCheat` |
| `eCD` | `net/minecraft/err422/util/RNGHelper` |
| `fRU` | `net/minecraft/err422/util/TickHandler` |
| `fxJ` | `net/minecraft/err422/entity422/Entity422` |
| `hdK` | `net/minecraft/err422/enums/EnumGlitchAction` |
| `nVa` | `net/minecraft/err422/util/TextFormatter` |
| `sRv` | `net/minecraft/err422/command/CommandManager` |
| `wlK` | `net/minecraft/err422/sound/CustomSoundManager` |
| `ymm` | `net/minecraft/err422/gui/GuiOverlay` |

These are selected findings from the current mapping file, not a complete list of all game classes.

### Selected Method Discoveries

Some important methods have also been identified.

**EventBus (`FqX`)**

- `getInstance()`
- `tick()`
- `isTimerExpired()`
- `scheduleTimer()`
- `processMovementGlitch()`
- `processWorldEvent()`
- `initializeTimer()`
- `modifyNearbyTerrain()`
- `processEntityEvent()`
- `processPotionEvent()`
- `processEventGlitch()`
- `processTerrainEvent()`
- `processErrorMessage()`

**ERR422Manager (`QXI`)**

- `changeItemStack()`
- `setPlacedBlock()`
- `findPlayerEntity()`
- `displayFakeCrashReport()`

**TickTimeHelper (`Jcz`)**

- `minutesToTicks()`

**CustomSoundManager (`wlK`)**

- `loadSounds()`
- Maintains the sound directory and sound-file mappings.
- Its platform-dependent path construction was modified to use `File.separator`.

**RNGHelper (`eCD`)**

- `getRandomNumber()`

**TickHandler (`fRU`)**

- `getInstance()`
- `tick()`

**TextFormatter (`nVa`)**

- `getEncodingError()`
- `toHexString()`

These names come from the current Enigma mapping structure. Their complete behavior still needs to be established through bytecode analysis and in-game testing.

---

## ❌ The Challenge: The `none/` Package

A significant part of the remaining research concerns classes inside `none/`.

These classes contain heavily obfuscated structures and currently lack reliable documentation.

Understanding their behavior requires more than simply renaming them. Their methods, fields, references and interactions with other game classes must also be investigated.

### Current Tasks

- [ ] Identify unknown classes in `none/`.
- [ ] Determine the purpose of important methods.
- [ ] Identify fields and their relationships.
- [ ] Trace references between custom and Minecraft classes.
- [ ] Rename classes and methods only when their purpose is sufficiently confirmed.
- [ ] Document discoveries in GitHub Issues or Discussions.
- [ ] Test proposed mappings and modifications in-game.
- [ ] Validate, correct or reject previous findings.
- [ ] Keep mapping files synchronized with new discoveries.

---

## 📦 Project Downloads

All required files, experimental builds and current mappings are available in the [GitHub Releases](https://github.com/GoxTheGoat-source/my_versions/releases) section.

### Available Files

- [ERROR422-crossplatform-modified.jar](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422-crossplatform-modified.jar) — Modified experimental cross-platform build.
- [ERROR422-crossplatform.jar](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422-crossplatform.jar) — Earlier experimental cross-platform build.
- [ERROR422.7z](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422.7z) — Contains the necessary equipment and files for deobfuscation.
- [ERROR422.jobf](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422.jobf) — Updated Enigma structure file.

**Important:** The `ERROR422.jobf` file included in the 7z archive may be outdated. Use the separately published version above when working with the latest mappings.

The modified `wlK` class is also available as a separate class file in the project distribution when provided.

---

## 🧰 Tools & Technologies

The project uses or investigates the following tools and technologies:

- Java and JVM bytecode.
- Minecraft Coder Pack (MCP) 1.5.2.
- Enigma Legacy 0.10.4-beta.
- Recaf.
- Java Archive (JAR) analysis.
- LWJGL 2.4.2.
- JInput 2.0.0-b01.
- Paulscode SoundSystem.
- OpenAL.
- AppleJavaExtensions.
- Windows, Linux and macOS native libraries.

---

## 🤝 How to Contribute

Contributions are welcome, especially from people familiar with:

- Java bytecode.
- Reverse engineering.
- Enigma and Recaf.
- Minecraft 1.5.2 internals.
- LWJGL and JInput.
- Legacy Java compatibility.
- Native library loading.
- Software preservation.

### Getting Started

1. Download the project archive and experimental JAR from the Releases section.
2. Open the relevant files using Enigma or Recaf.
3. Investigate unknown classes inside `none/`.
4. Compare your findings with existing mappings.
5. Open an Issue or submit a Pull Request to share your results.

### What to Include in a Finding

To make discoveries easier to verify, please provide:

- The original obfuscated class or method name.
- The proposed mapping.
- The bytecode or behavior supporting the identification.
- Relevant screenshots, logs or test results.
- The tool and version used.

Unconfirmed guesses should be clearly marked as such.

---

## 🖥️ Cross-Platform Testing Contributions

Testing on additional systems is particularly useful.

If you manage to launch the experimental JAR, please report:

- Operating system and version.
- CPU architecture (x86, x86_64, ARM64, etc.).
- Java version and distribution.
- Whether the graphical interface opens.
- Whether world creation and saving work.
- Whether the game can be reopened successfully.
- Whether audio is actually audible.
- Whether native libraries load correctly.
- Any crashes or relevant error messages.

A successful menu launch alone does not constitute complete compatibility.

---

## ⚠️ Project Status & Safety Notes

This project is experimental.

Not all operating systems, CPU architectures, Java versions or native library combinations have been validated.

The bundled native dependencies are legacy components, and audio, graphics and memory compatibility still require investigation.

Old or unsigned native libraries can trigger antivirus false positives, but their age or lack of a digital signature does not prove that a file is harmless.

Always scan downloaded binaries independently and preserve your world saves before testing modified builds.

---

## 🇫🇷 Version française

# Minecraft ERROR422 — Projet multiplateforme et de désobfuscation

**Rétro-ingénierie, analyse du bytecode Java, compatibilité multiplateforme et préservation logicielle.**

Ce projet de recherche indépendant vise à comprendre, désobfusquer et améliorer la compatibilité multiplateforme du jeu existant Minecraft ERROR422, basé à l'origine sur Minecraft 1.5.2.

L'objectif est d'étudier sa structure Java fortement obfusquée, d'identifier ses classes et méthodes personnalisées, de reconstruire des mappings exploitables et de permettre l'exécution du jeu sur différents systèmes d'exploitation.

**Ce projet ne revendique pas la création du jeu original. Il s'agit d'un projet indépendant de recherche et de préservation logicielle.**

---

## 🚀 Objectifs du projet

- **Compatibilité multiplateforme :** fournir et étudier les bibliothèques natives nécessaires à Windows, Linux et macOS.
- **Analyse du bytecode Java :** comprendre les classes compilées originales sans dépendre d'un code source indisponible.
- **Désobfuscation :** identifier les classes, méthodes et champs personnalisés et documenter leur véritable fonction.
- **Reconstruction des mappings :** construire et maintenir des mappings pour le code original de Minecraft 1.5.2 et les modifications spécifiques à ERROR422.
- **Préservation logicielle :** conserver l'accessibilité et la possibilité d'exécuter ce jeu Java historique sur différents systèmes.

---

## 📊 Avancement actuel

### Tests multiplateformes

Le JAR expérimental a été testé sur les configurations suivantes :

| Système d'exploitation | Architecture | Version Java | Statut |
|---|---|---|---|
| Windows 10 (Build 10240) | x86 (32 bits) | Oracle Java 8u191 | Testé |
| Linux | x86_64 | OpenJDK 21 | Testé |
| macOS | Architecture non précisée | Java 21 | Fonctionnement rapporté ; détails à compléter |
| macOS Apple Silicon | ARM64 | — | Compatibilité native non confirmée |

### Tests Windows

Tests réalisés sous Windows 10 Build 10240, x86, avec Oracle Java 8u191.

- Minecraft initialise correctement LWJGL 2.4.2.
- L'implémentation LWJGL OpenAL originale fournie dans le JAR s'initialise.
- Le serveur intégré de Minecraft démarre et génère un nouveau monde.
- Le joueur parvient à rejoindre le serveur intégré.
- Un premier lancement sans limite de mémoire configurée provoque un `OutOfMemoryError` dans `ChannelLWJGLOpenAL.queueBuffer()`, suivi d'une erreur de mémoire OpenGL et d'un crash du pilote graphique Intel (`igdusc32.dll`).
- Un deuxième test utilisant `-Xmx512M` parvient à charger le monde.
- Un test prolongé avec `-Xmx1G` reste fonctionnel et se termine par un arrêt propre.
- Le serveur sauvegarde l'Overworld, le Nether et l'End.
- Le SoundSystem s'arrête proprement.

Des avertissements de performances ont néanmoins été observés, notamment `Can't keep up!` et `Memory connection overburdened`.

**Important :** l'initialisation réussie d'OpenAL ne prouve pas à elle seule que tous les sons sont effectivement audibles. La lecture audio réelle nécessite encore des vérifications.

### Tests Linux

Tests réalisés sous Linux x86_64 avec OpenJDK 21.

- Le jeu démarre avec une interface graphique utilisant un affichage X virtuel.
- LWJGL 2.4.2 charge ses composants natifs Linux.
- Un nouveau monde Survival est créé.
- Le monde est rouvert et exploré.
- Le jeu sauvegarde correctement le monde et arrête son serveur intégré.
- La classe `wlK` reçoit une correction multiplateforme de ses chemins avec `File.separator`.
- La classe corrigée est testée pour la création du dossier et l'extraction des quatre fichiers OGG.

La compatibilité audio avec les bibliothèques natives originales reste à étudier. Un remplacement expérimental par OpenAL Soft a également été testé, mais cela ne valide pas le fonctionnement de la configuration audio originale.

### Tests macOS

Un test réussi avec Java 21 sous macOS a été rapporté après l'ajout d'un JAR de compatibilité contenant les extensions Java d'Apple (`AppleJavaExtensions.jar`).

La version exacte de macOS, l'architecture du processeur et les journaux détaillés du test restent à documenter.

**La compatibilité native avec Apple Silicon ARM64 n'est pas confirmée.** La présence de classes Java et de bibliothèques natives liées à macOS ne garantit pas leur compatibilité native avec ARM64.

Des tests supplémentaires sont nécessaires pour établir la compatibilité avec les Mac Intel et Apple Silicon, notamment en examinant l'éventuelle utilisation de Rosetta.

---

## 🔬 Avancement de la désobfuscation

Le projet utilise Enigma Legacy 0.10.4-beta pour reconstruire les mappings et étudier le bytecode Java original.

Réalisations actuelles :

- [x] Extraction des mappings de base depuis MCP 1.5.2 (`client.srg`).
- [x] Génération d'une structure `.jobf` avec Enigma.
- [x] Identification de plusieurs classes personnalisées d'ERROR422.
- [x] Étude du `ERR422Loader` original et du mécanisme de chargement des JAR.
- [x] Identification de plusieurs méthodes et champs importants.
- [x] Correction de la gestion des chemins dépendants du système dans `wlK`.
- [ ] Terminer l'identification des classes inconnues dans `none/`.
- [ ] Reconstruire les méthodes et champs restants.
- [ ] Valider les mappings avec le comportement réel du jeu.

---

## 🧬 Classes spécifiques à ERROR422 identifiées

Les mappings suivants ont été identifiés dans la structure Enigma `.jobf` actuelle.

| Nom obfusqué | Classe identifiée |
|---|---|
| `BuD` | `net/minecraft/err422/enums/EnumWorldEventHelper` |
| `Eya` | `net/minecraft/err422/enums/EnumWorldEvent` |
| `FqX` | `net/minecraft/err422/eventbus/EventBus` |
| `Jcz` | `net/minecraft/err422/util/TickTimeHelper` |
| `Lbz` | `net/minecraft/err422/util/KeyboardMonitor` |
| `OEk` | `net/minecraft/err422/gui/GuiTextBackground` |
| `QXI` | `net/minecraft/err422/ERR422Manager` |
| `UYQ` | `net/minecraft/err422/enums/EnumGlitchActionHelper` |
| `YRn` | `net/minecraft/err422/util/AntiCheat` |
| `eCD` | `net/minecraft/err422/util/RNGHelper` |
| `fRU` | `net/minecraft/err422/util/TickHandler` |
| `fxJ` | `net/minecraft/err422/entity422/Entity422` |
| `hdK` | `net/minecraft/err422/enums/EnumGlitchAction` |
| `nVa` | `net/minecraft/err422/util/TextFormatter` |
| `sRv` | `net/minecraft/err422/command/CommandManager` |
| `wlK` | `net/minecraft/err422/sound/CustomSoundManager` |
| `ymm` | `net/minecraft/err422/gui/GuiOverlay` |

Il s'agit d'une sélection de découvertes issues du fichier de mappings actuel, et non de la liste complète des classes du jeu.

### Découvertes sur certaines méthodes

Plusieurs méthodes importantes ont également été identifiées.

**EventBus (`FqX`)**

- `getInstance()`
- `tick()`
- `isTimerExpired()`
- `scheduleTimer()`
- `processMovementGlitch()`
- `processWorldEvent()`
- `initializeTimer()`
- `modifyNearbyTerrain()`
- `processEntityEvent()`
- `processPotionEvent()`
- `processEventGlitch()`
- `processTerrainEvent()`
- `processErrorMessage()`

**ERR422Manager (`QXI`)**

- `changeItemStack()`
- `setPlacedBlock()`
- `findPlayerEntity()`
- `displayFakeCrashReport()`

**TickTimeHelper (`Jcz`)**

- `minutesToTicks()`

**CustomSoundManager (`wlK`)**

- `loadSounds()`
- Gère le dossier des sons et les mappings des fichiers audio.
- Sa construction de chemins dépendante du système a été modifiée pour utiliser `File.separator`.

**RNGHelper (`eCD`)**

- `getRandomNumber()`

**TickHandler (`fRU`)**

- `getInstance()`
- `tick()`

**TextFormatter (`nVa`)**

- `getEncodingError()`
- `toHexString()`

Ces noms proviennent de la structure actuelle des mappings Enigma. Leur comportement complet doit encore être établi par l'analyse du bytecode et les tests en jeu.

---

## ❌ Le défi : le package `none/`

Une partie importante du travail restant concerne les classes situées dans `none/`.

Ces classes possèdent des structures fortement obfusquées et ne disposent pas encore d'une documentation fiable.

Comprendre leur comportement nécessite davantage qu'un simple renommage. Il faut également étudier leurs méthodes, champs, références et interactions avec les autres classes du jeu.

### Tâches actuelles

- [ ] Identifier les classes inconnues de `none/`.
- [ ] Déterminer le rôle des méthodes importantes.
- [ ] Identifier les champs et leurs relations.
- [ ] Étudier les références entre les classes personnalisées et celles de Minecraft.
- [ ] Renommer les classes et méthodes uniquement lorsque leur fonction est suffisamment confirmée.
- [ ] Documenter les découvertes dans les GitHub Issues ou Discussions.
- [ ] Tester les mappings et modifications proposés en jeu.
- [ ] Valider, corriger ou rejeter les découvertes précédentes.
- [ ] Maintenir les fichiers de mappings à jour avec les nouvelles découvertes.

---

## 📦 Téléchargements du projet

Tous les fichiers nécessaires, builds expérimentaux et mappings actuels sont disponibles dans la section [GitHub Releases](https://github.com/GoxTheGoat-source/my_versions/releases).

### Fichiers disponibles

- [ERROR422-crossplatform-modified.jar](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422-crossplatform-modified.jar) — Version multiplateforme expérimentale modifiée.
- [ERROR422-crossplatform.jar](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422-crossplatform.jar) — Première version expérimentale multiplateforme.
- [ERROR422.7z](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422.7z) — Contient les outils et fichiers nécessaires à la désobfuscation.
- [ERROR422.jobf](https://github.com/GoxTheGoat-source/my_versions/releases/download/versions/ERROR422.jobf) — Fichier de structure Enigma actualisé.

**Important :** le fichier `ERROR422.jobf` inclus dans l'archive 7z peut être obsolète. Utilisez la version publiée séparément ci-dessus pour travailler avec les mappings les plus récents.

La classe `wlK` modifiée est également disponible séparément dans la distribution du projet lorsqu'elle est publiée.

---

## 🧰 Outils et technologies

Le projet utilise ou étudie les outils et technologies suivants :

- Java et bytecode JVM.
- Minecraft Coder Pack (MCP) 1.5.2.
- Enigma Legacy 0.10.4-beta.
- Recaf.
- Analyse d'archives Java (JAR).
- LWJGL 2.4.2.
- JInput 2.0.0-b01.
- Paulscode SoundSystem.
- OpenAL.
- AppleJavaExtensions.
- Bibliothèques natives Windows, Linux et macOS.

---

## 🤝 Comment contribuer ?

Les contributions sont les bienvenues, en particulier de la part des personnes connaissant :

- Le bytecode Java.
- La rétro-ingénierie.
- Enigma et Recaf.
- Les mécanismes internes de Minecraft 1.5.2.
- LWJGL et JInput.
- La compatibilité avec les anciennes versions de Java.
- Le chargement des bibliothèques natives.
- La préservation logicielle.

### Premiers pas

1. Téléchargez l'archive du projet et le JAR expérimental depuis la section Releases.
2. Ouvrez les fichiers concernés avec Enigma ou Recaf.
3. Étudiez les classes inconnues de `none/`.
4. Comparez vos découvertes aux mappings existants.
5. Ouvrez une Issue ou soumettez une Pull Request pour partager vos résultats.

### Informations à fournir pour une découverte

Pour faciliter la vérification des résultats, veuillez fournir :

- Le nom original de la classe ou méthode obfusquée.
- Le mapping proposé.
- Le bytecode ou le comportement justifiant l'identification.
- Les captures d'écran, journaux ou résultats de tests pertinents.
- L'outil et sa version.

Les hypothèses non confirmées doivent être clairement signalées comme telles.

---

## 🖥️ Contributions aux tests multiplateformes

Les tests sur d'autres systèmes sont particulièrement utiles.

Si vous réussissez à lancer le JAR expérimental, veuillez indiquer :

- Le système d'exploitation et sa version.
- L'architecture du processeur (x86, x86_64, ARM64, etc.).
- La version et la distribution de Java.
- Si l'interface graphique s'ouvre.
- Si la création et la sauvegarde d'un monde fonctionnent.
- Si le monde peut être rouvert correctement.
- Si les sons sont réellement audibles.
- Si les bibliothèques natives se chargent correctement.
- Les éventuels crashs ou messages d'erreur pertinents.

Un simple affichage du menu ne constitue pas une preuve de compatibilité complète.

---

## ⚠️ Statut du projet et sécurité

Ce projet reste expérimental.

Tous les systèmes d'exploitation, architectures de processeur, versions de Java et combinaisons de bibliothèques natives n'ont pas été validés.

Les dépendances natives embarquées sont anciennes, et la compatibilité audio, graphique et mémoire nécessite encore des investigations.

Les anciennes bibliothèques natives non signées peuvent provoquer des faux positifs antivirus, mais leur ancienneté ou l'absence de signature numérique ne prouve pas qu'un fichier est inoffensif.

Analysez toujours les fichiers binaires téléchargés séparément et conservez une copie de vos sauvegardes avant de tester des versions modifiées.

---

## 😴 Notes complémentaires / Extra Notes

### English

Yes, I know. There is also a `README.txt` inside the 7z archive. Please read it if you need additional information about the deobfuscation setup.

The `.jobf` file included in that archive may be outdated. Use the regularly updated version published separately in the GitHub Releases section.

**Let's crack the code of ERROR422 together!**

### Français

Oui, je sais. Il existe également un fichier `README.txt` dans l'archive 7z. Consultez-le si vous avez besoin d'informations supplémentaires sur la configuration de l'environnement de désobfuscation.

Le fichier `.jobf` inclus dans cette archive peut être obsolète. Utilisez la version régulièrement actualisée et publiée séparément dans la section GitHub Releases.

**Ensemble, déchiffrons le code d'ERROR422 !**
