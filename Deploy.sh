#!/bin/bash

# 1. Définition des variables et chemins
SRC_DIR="src/main/java"
LIB_DIR="lib"
OUT_DIR="output"
BIN_DIR="bin_temp"

echo "=== Début du build du Framework ==="

# 2. Nettoyage des anciens builds
rm -rf "$BIN_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$OUT_DIR"

# 3. Compilation des sources du framework uniquement
echo "Compilation des fichiers Java..."
javac -cp "$LIB_DIR/servlet-api.jar" \
      -d "$BIN_DIR" \
      "$SRC_DIR"/framework/annotations/*.java \
      "$SRC_DIR"/framework/*.java

# On vérifie si la compilation a réussi
if [ $? -eq 0 ]; then
    echo "Compilation réussie !"
    
    # 4. Création du fichier JAR à partir du dossier temporaire
    echo "Création du fichier FrameWork.jar..."
    jar -cvf "$OUT_DIR/FrameWork.jar" -C "$BIN_DIR" .
    
    echo "JAR créé avec succès dans le dossier '$OUT_DIR/'."
else
    echo "Erreur lors de la compilation."
    exit 1
fi

# 5. Nettoyage du dossier temporaire de classes
rm -rf "$BIN_DIR"

echo "=== Build terminé ==="