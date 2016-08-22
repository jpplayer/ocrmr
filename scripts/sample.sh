#!/bin/bash

hadoop fs -mkdir -p ocr-input
hadoop fs -put ../data/eurotext.png ocr-input
