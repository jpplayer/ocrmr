package ocr.mr;

import com.google.common.io.Files;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.IOUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.apache.commons.io.IOUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;

public class OCRMR extends Configured implements Tool {
	public static class Map extends Mapper<Text, BytesWritable, Text, Text> {

		private Text word = new Text();
		private Text filepath;
		private Text filename;

		Tesseract instance = null;

		@Override
			protected void setup(Context context) throws IOException, InterruptedException {

				File tessdata = new File("tessdata.tar.gz/tessdata");
				File nativelibs = new File("tesslib.tar.gz");
				try {
					for( File lib : nativelibs.listFiles() ) {
						System.load(lib.getCanonicalPath());
					}

					instance = Tesseract.getInstance();
					instance.setDatapath(tessdata.getCanonicalPath());
					instance.setTessVariable("LC_NUMERIC", "C");
				} catch (IOException e) {
					System.out.println("Failed to obtain Tessdata library or folder");
				}

			}

		public void map(Text key, BytesWritable data, Context context)
			throws IOException, InterruptedException {

				InputStream is = new ByteArrayInputStream(data.getBytes());
				BufferedImage image = ImageIO.read(IOUtils.toBufferedInputStream(is));

				String result = null;
				try {
					result = instance.doOCR(image);
				} catch (TesseractException e) {
					e.printStackTrace();
				}
				if (!result.isEmpty()) {
					word.set(result);
					context.write(key, word);
				}
			}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text filename, Text ocrText, Context context)
			throws IOException, InterruptedException {
				context.write(filename, new Text(ocrText));
			}
	}

	@Override
		public int run(String[] args) throws Exception {
			Job job = Job.getInstance(getConf());
			job.setJarByClass(OCRMR.class);
			job.setJobName("OCR");

			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);

			job.setMapperClass(Map.class);
			job.setCombinerClass(Reduce.class);
			job.setReducerClass(Reduce.class);

			job.setInputFormatClass(BinaryFileInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);

			FileInputFormat.setInputPaths(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));

			job.addCacheArchive(new URI("/tmp/tessdata.tar.gz"));
			job.addArchiveToClassPath(new org.apache.hadoop.fs.Path(new URI("/tmp/tesslib.tar.gz")));

			return job.waitForCompletion(true) ? 0 : 1;
		}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new OCRMR(), args);
		System.exit(exitCode);
	}

}
