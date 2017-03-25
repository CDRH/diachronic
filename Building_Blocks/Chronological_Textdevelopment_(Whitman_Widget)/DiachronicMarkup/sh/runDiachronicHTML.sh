#!/bin/sh
export CLASSPATH=../lib/saxon9he.jar

clear

cd ../input

rm  ../htmlOutput/*.*

# tex.00030-text.xml
# uva.00256-v2-text

for i in `ls *.xml`; do

java -Xmx2100m net.sf.saxon.Transform ../input/${i} ../xslt/diachronic.xsl >../htmlOutput/${i}.html

open ../htmlOutput/${i}.html

done;

echo "Done. " 
echo " "