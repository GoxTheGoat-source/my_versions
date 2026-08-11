# Minecraft Error 422 - Cross-Platform & Deobfuscation Project

This project aims to fix and port the famous Minecraft creepy/ARG mod **Error 422** (originally based on Minecraft 1.5.2) to make it fully cross-platform (Windows, Linux, macOS) and to map its heavily obfuscated structure.

---

## 🚀 Current Progress

* **Cross-Platform Support:** Added proper native files (`.dll`, `.so`, `.dylib`) to allow the JAR to run on modern multi-OS environments.
* **Mappings Extracted:** Extracted base mappings using `client.srg` from MCP 1.5.2.
* **Structure File Generated:** Formatted a `.jobf` structure file using **Enigma 0.10.4-beta** to prepare for full deobfuscation.

---

## 📂 Project Downloads

All required files, binaries, and current mappings can be found in the [Releases](https://github.com/GoxtheGoat-source/my_versions/releases) tab:
* 📦 `ERROR422-crossplatform.jar` *(Experimental / Not fully tested yet)* [link](https://github.com/GoxtheGoat-source/my_versions/releases/download/versions/ERROR422-crossplatform.jar)
* 🛠️ `ERROR422.7z` *(Contains the necessary equipment for deobfuscation)* [link](https://github.com/GoxtheGoat-source/my_versions/releases/download/versions/ERROR422.7z)

---

## ❌ The Challenge: The `none/` Package

The original mod uses a custom obfuscation layer that pushes critical classes (like menus, event handlers, or game loops) into a root package named `none/`. 

Right now, these classes are a mess of raw bytecode and unreadable names, making the game unstable and hard to patch.

---

## 🤝 How to Help (Contribute!)

If you are skilled in **Java Bytecode, Reverse Engineering, Recaf, or Enigma**, your help is more than welcome! 

### How to get started:
1. Download the 7z archive and test the cross-platform JAR from the Releases tab.
2. Open the project in **Enigma(Included in the 7z archive)** or **Recaf**.
3. Help us reverse-engineer, clean up, and rename the classes inside the `none/` folder.
4. Open an **Issue** or submit a **Pull Request** right here on GitHub to share your findings!
## 🥱 Laziness

**It's best to read the README.txt file in 7z**

Let's crack the code of Error 422 together.
