package org.archive.access.nutch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ToolBase;
import org.apache.nutch.util.NutchConfiguration;

/**
 * Run multiple concurrent non-mapreduce {@link ToolBase} tasks such as
 * {@link org.apache.nutch.indexer.IndexMerger} or
 * {@link org.apache.nutch.indexer.IndexSorter}.
 * 
 * Takes input that has per line the name of the class to run and the
 * arguments to pass. Here is an example line for IndexMerger:
 * <code>org.apache.nutch.indexer.IndexMerger -workingdir /tmp index-new
 * indexes</code>. Here is one for IndexSorter:
 * <code>org.apache.nutch.indexer.IndexSorter /home/stack/tmp/crawl</code>
 * (Note that IndexSorter wants to refer to the local system; the indexes to
 * sort must be on local disk). We run as many tasks as there are input lines.
 * 
 * @author stack
 */
public class Multiple extends ToolBase implements Mapper
{
  public final Log LOG = LogFactory.getLog(this.getClass());
  private JobConf job;
    
  public void map(WritableComparable key, Writable value,
    OutputCollector output, final Reporter reporter)
    throws IOException
  {
    final String [] words = value.toString().split("\\s");
    
    if (words.length <= 0)
    {
      return;
    }
    
    final String className = words[0];
    
    // Set a timer running that will update reporter on a period.
    Timer t = new Timer(false);
    
    t.scheduleAtFixedRate(new TimerTask()
    {
      @Override
      public void run() {	 
          reporter.setStatus("Running " + className);	  
      }
    }, 0, 10000);
    
    try
    {
      int result = doMain(words);
      
      reporter.setStatus("Done running " + className + ": " + result);
      
      if (result != 0)
      {
        throw new IOException(className + " returned non-null: " +
          result + ", check logs.");
      }
    }
    finally
    {
      t.cancel();
    }
  }

 /**
  * Call {@link ToolBase#doMain(org.apache.hadoop.conf.Configuration, String[])}
  * on the passed classname.
  * @param args
  * @return Result from call to doMain.
  */
  private int doMain(final String [] args)
  {
    final String className = args[0];
    
    // Redo args so absent our 'class' command.
    String [] newArgs = Nutchwax.rewriteArgs(args, 1);
    int result = -1;
    
    try
    {
      Object obj = Class.forName(className).newInstance();
      result = ((ToolBase)obj).doMain(this.job, newArgs);
    }
    catch (Exception e)
    {
      LOG.error(className, e);
    }
    
    return result;
  }

  public void configure(final JobConf j)
  {
    this.job = j;
  }

  public void close() throws IOException
  {
    // TODO Auto-generated method stub
  }

  public static class MultipleInputFormat implements InputFormat
  {
    public RecordReader getRecordReader(final InputSplit split, 
      final JobConf job, final Reporter reporter)
      throws IOException
    {
      // Only one record/line to read.
      return new RecordReader()
      {
        private final String line = ((LineInputSplit)split).line;
        private boolean read = false;
        
        public void close() throws IOException
        {
          // TODO Auto-generated method stub
        }

        public WritableComparable createKey()
        {
          return new Text("");
        }

        public Writable createValue() {
          return new Text("");
        }

        public long getPos() throws IOException
        {
          return 0;
        }

        public float getProgress() throws IOException
        {
          return getPos();
        }

        public boolean next(Writable key, Writable value)
          throws IOException
        {
          if (read)
          {
            return false;
          }
          
          read = true;
          
          ((Text)value).set(this.line);

          return true;
        }
      };
    }

    public InputSplit[] getSplits(JobConf job, int numSplits)
      throws IOException
    {
      Path[] inputs = job.getInputPaths();

      List<String> lines = new ArrayList<String>();

      for (int i = 0; i < inputs.length; i++)
      {
        Path p = inputs[i];
        FileSystem fs = p.getFileSystem(job);
        Path [] ps = fs.listPaths(p);

        for (int j = 0; j < ps.length; j++)
        {
          if (fs.isDirectory(ps[j]))
          {
            continue;
          }
          
          addFileLines(lines, fs, ps[j]);
        }
      }
      
      List<LineInputSplit> splits =
        new ArrayList<LineInputSplit>(lines.size());
        
      for (String line: lines)
      {
        splits.add(new LineInputSplit(line));
      }
      
      job.setNumMapTasks(lines.size());
      
      return splits.toArray(new LineInputSplit [splits.size()]);
    }
    
    private void addFileLines(final List<String> lines, final FileSystem fs,
        final Path p)
      throws IOException
    {
      InputStream is = (InputStream)fs.open(p);
      LineNumberReader lnr = null;
      
      try
      {
        lnr = new LineNumberReader(new InputStreamReader(is));
        
        for (String l = null; (l = lnr.readLine()) != null;)
        {
          if (l.length() > 0 && !l.trim().startsWith("#"))
          {
            lines.add(l);
          }
        }
      }
      finally
      {
        if (lnr != null)
        {
          lnr.close();
        }
        
        is.close();
      }
    }

    public void validateInput(JobConf job) throws IOException
    {
      // Nothing to validate.
    }
  }
  
  public static class LineInputSplit implements InputSplit
  {
    private String line;
    
    protected LineInputSplit()
    {
      super();
    }
    
    public LineInputSplit(final String l)
    {
      line = l;
    }
    
    public long getLength() throws IOException
    {
      return line.length();
    }

    public String[] getLocations() throws IOException
    {
      return new String[0];
    }

    public void readFields(DataInput in) throws IOException
    {
      this.line = in.readLine();
    }

    public void write(DataOutput out) throws IOException
    {
      out.writeBytes(this.line);
    }
  }
  
  public static void usage()
  {
    System.out.println("Usage: multiple <input> <output>");
    System.out.println("Runs concurrently all commands listed in " +
      "<inputs>.");
    System.out.println("Arguments:");
    System.out.println(" <input>   Directory of input files with " +
      "each line describing task to run");
    System.out.println(" <output>  Output directory.");
    System.out.println("Example input lines:");
    System.out.println();
    System.out.println(" An input line to specify a merge would look like:");
    System.out.println();
    System.out.println(" org.apache.nutch.indexer.IndexMerger " +
      "-workingdir /3/hadoop-tmp index-monday indexes-monday");
    System.out.println();
    System.out.println(" Note that named class must implement " +
      "org.apache.hadoop.util.ToolBase");
    System.out.println();
    System.out.println(" To copy from " +
      "hdfs://HOST:PORT/user/stack/index-monday to");
    System.out.println( " file:///0/searcher.dir/index:");
    System.out.println();
    System.out.println(" org.apache.hadoop.fs.FsShell " +
      "/user/stack/index-monday /0/searcher.dir/index"); 
    System.out.println();
    System.out.println(" org.apache.nutch.indexer.IndexSorter " +
      "/home/stack/tmp/crawl"); 
    System.out.println();
    System.out.println(" Note that IndexSorter refers to local " +
      "filesystem and not to hdfs and is RAM-bound. Set");
    System.out.println(" task child RAM with the mapred.child.java.opts " +
      "property in your hadoop-site.xml.");
  }
  
  public int run(String[] args) throws Exception
  {
    if (args.length != 2 ||
        (args.length == 1 &&
          (args[0].equals("-h") || args[0].equals("--help"))))
    {
      usage();
      return -1;
    }
    
    JobConf jobcon = new JobConf(MultipleInputFormat.class);
    jobcon.setInputFormat(MultipleInputFormat.class);
    jobcon.setInputPath(new Path(args[0]));
    jobcon.setMapperClass(Multiple.class);
    jobcon.setOutputPath(new Path(args[1]));
    
    JobClient.runJob(jobcon);
    
    return 0;
  }
  
  public static void main(String[] args) throws Exception
  {
    int res = new Multiple().doMain(NutchConfiguration.create(), args);
    
    System.exit(res);
  }
}