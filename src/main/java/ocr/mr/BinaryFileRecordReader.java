package ocr.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

class BinaryFileRecordReader extends RecordReader<Text, BytesWritable> {
	private CombineFileSplit split;
	//private Configuration conf;
	//private BytesWritable value = new BytesWritable();
	private FileSystem fs;
	private Text key;
	private BytesWritable value;
	private Path[] paths;
	private FSDataInputStream currentStream;
	private int count = 0;
	private boolean processed = false;

	public BinaryFileRecordReader(CombineFileSplit split, TaskAttemptContext context)
			throws IOException
	{
		this.paths = split.getPaths();
		this.fs = FileSystem.get(context.getConfiguration());
		this.split = split;
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (count >= split.getNumPaths())
		{
			processed = true;
			return false; // we have no more data to parse
		}

		Path path = null;
		key = new Text();
		value = new BytesWritable();

		try
		{
			path = this.paths[count];
		} catch (Exception e)
		{
			return false;
		}

		currentStream = null;
		try {
		currentStream = fs.open(path);
		key.set(path.getName());
		int length = (int) fs.getFileStatus(path).getLen();
		byte[] contents = new byte[ length ];
		        //Path file = fileSplit.getPath();
                        //FileSystem fs = file.getFileSystem(conf);
                        //FSDataInputStream in = null;
                        
                                //in = fs.open(file);
                                IOUtils.readFully(currentStream, contents, 0, length);
                                value.set(contents, 0, length);
                        } finally {
//                                IOUtils.closeStream(in);
 				currentStream.close();
                       }
//
		//value.set(tikaHelper.readPath(currentStream));
//
		//currentStream.close();
		count++;

		return true; // we have more data to parse



/*

		if (!processed) {
			byte[] contents = new byte[(int) fileSplit.getLength()];
			Path file = fileSplit.getPath();
			FileSystem fs = file.getFileSystem(conf);
			FSDataInputStream in = null;
			try {
				in = fs.open(file);
				IOUtils.readFully(in, contents, 0, contents.length);
				value.set(contents, 0, contents.length);
			} finally {
				IOUtils.closeStream(in);
			}
			processed = true;
			return true;
		}
		return false;
*/
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public BytesWritable getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException {
		return processed ? 1.0f : (float) (count / paths.length);
	}

	@Override
	public void close() throws IOException {
		processed = true;
	}
}
