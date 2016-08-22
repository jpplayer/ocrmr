# ocrmr
OCR via Map Reduce

# Usage
1. Package Tesseract, Leptonica native binaries and Tessdata for deployment on Hadoop. See [scripts/tesspackage.sh](scripts/tesspackage.sh).
2. Place images in HDFS
3. Build project with `mvn package`
4. Run
```bash
hadoop jar target/ocrmr-*-with-dependencies.jar ocr.mr.OCRMR ocr-input ocr-output -Djava.library.path=/usr/lib/jni
```
