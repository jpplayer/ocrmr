#!/bin/bash
BAK="$PWD"
yum -y install tesseract-devel
cd /usr/share/tesseract
tar cfz /tmp/tessdata.tar.gz tessdata
hdfs dfs -put -f /tmp/tessdata.tar.gz /tmp
if [[ -f /lib64/libtesseract.so.3 ]]; then
  PREFIX=/lib64
else
  PREFIX=/usr/lib64
fi

hdfs dfs -put -f $PREFIX/libtesseract.so.3 /tmp/libtesseract.so
hdfs dfs -put -f $PREFIX/liblept.so.4 /tmp/
hdfs dfs -put -f $PREFIX/libwebp.so.5 /tmp/
hdfs dfs -put -f $PREFIX/libgif.so.4 /tmp/

#yum -y erase leptonica tesseract
cd "$BAK"
