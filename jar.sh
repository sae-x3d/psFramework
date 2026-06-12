#!/bin/bash

# Nettoyage
rm -rf out
mkdir -p out

# Compilation
javac -cp "lib/servlet-api.jar" -d out $(find . -name "*.java")

# Vérification
if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

# Création du JAR
jar cf psfw.jar -C out .
mkdir -p output 
mv psfw.jar output

rm ../psfw/lib/psfw.jar
cp -d output/psfw.jar ../psfw/lib/

echo "JAR créé : psfw.jar"
rm -rf out