#!/bin/bash

cd "$(dirname "$0")"

# Nettoyage
rm -rf out
mkdir -p out

# Compilation
javac -Xlint -cp "lib/*" -d out $(find . -name "*.java")

# Vérification
if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

# Création du JAR
jar cf lib/FrameWork.jar -C out .

echo "JAR créé : lib/FrameWork.jar"
rm -rf out