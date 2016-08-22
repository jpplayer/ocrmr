#!/bin/bash
hdfs dfs -rm -r ocr-output
yarn jar ../target/ocrmr-1.0-SNAPSHOT-jar-with-dependencies.jar ocr.mr.OCRMR ocr-input ocr-output -Djava.library.path=/usr/lib/jni
