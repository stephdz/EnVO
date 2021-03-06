#!/bin/bash
##############################################################################################
# envo.sh : Téléchargement des sous-titres sur Internet
# Paramètres :
#  - $1 : options (-v pour plus de logs, 
#                  -w pour avoir la progression dans un popup,
#                  -t pour transcoder un fichier de sous-titres)
#  - $2 : langue (fre pour français)
#  - $3 : chemin vers la vidéo ou nom de fichier (existance non obligatoire)
##############################################################################################

# Constantes
ENVO_JAR=$(dirname $0)/../EnVO.jar

# Recherche des paramètres
WINDOWED="false"
OPTIONS=""
LANG="fre"
VIDEO=""

# On commence par les options
for param in $@; do
        if [ "$param" = "-${param:1}" ]; then
                if [ "$param" = "-w" ]; then
                        WINDOWED="true"
                else
                        OPTIONS="$OPTIONS $param"
                fi
                shift
        else
                break
        fi
done

# La langue et le fichier à traiter
LANG=$1
VIDEO=$2

# Exécution du Jar avec les paramètres fournis
if [ "$WINDOWED" = "true" ]; then
        java -jar "$ENVO_JAR" $OPTIONS "$LANG" "$VIDEO" | sed 's/#/-/g'  | zenity --progress \
                --title="Recherche de sous-titres" \
                --text="Recherche en cours..." \
                --percentage=0 \
                --pulsate \
                --auto-close \
                --no-cancel
else
        java -jar "$ENVO_JAR" $OPTIONS "$LANG" "$VIDEO"
fi

