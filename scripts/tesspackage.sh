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

cd /tmp
mkdir -p tesslib
cp $PREFIX/libtesseract.so.3 tesslib/libtesseract.so
cp $PREFIX/liblept.so.4 tesslib/
cp $PREFIX/libwebp.so.5 tesslib/
cp $PREFIX/libgif.so.4 tesslib/
cd tesslib
tar cfz ../tesslib.tar.gz *
hdfs dfs -put -f ../tesslib.tar.gz /tmp

#yum -y erase leptonica tesseract
cd "$BAK"
