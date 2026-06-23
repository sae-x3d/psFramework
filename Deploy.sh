#!/bin/bash

echo "=== Début du build du Framework (Maven) ==="

mvn clean install -f "$(dirname "$0")/pom.xml"

if [ $? -eq 0 ]; then
    mkdir -p "$(dirname "$0")/output"
    cp "$(dirname "$0")/target/atframework-1.0-SNAPSHOT.jar" "$(dirname "$0")/output/FrameWork.jar"
    echo "JAR créé avec succès dans le dossier 'output/'."
    echo "=== Build terminé ==="
else
    echo "Erreur lors de la compilation."
    exit 1
fi
