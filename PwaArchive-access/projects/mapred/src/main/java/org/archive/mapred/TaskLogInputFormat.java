package org.archive.mapred;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.net.DNS;

/**
 * An InputFormat for {@link TaskLog} <code>userlogs</code>.
 * 
 * The number of splits is equal to the <code>numTasks</code> count passed
 * to {@link #getSplits(JobConf, int)}, usually the count of map tasks.  Per
 * split, the local logs associated with the configured jobid are streamed.
 * Records are a single log line with a key of form 'HOST:TASK_ID:LINE_NO'.
 * Note, if a split (host) does not have logs corresponding to the passed
 * jobid -- say, the host is new to the cluster -- then the task fails and is
 * scheduled elsewhere.  This can make for double-counting of logs if the new
 * host has already had its logs processed by a previous task.  This makes for
 * fuzzy analysis: good for figuring if errors or general rate of problems
 * but bad for precision reporting.
 * 
 * <p>Before use, client must set jobid and optionally the userlogs subdir
 * -- whether stdout stderr, or syslog -- and whether to look at map or
 * reduce task logs or at both. See {@link #setJobid(JobConf, int)},
 * {@link #setLogfilter(JobConf, org.apache.hadoop.mapred.TaskLog.LogFilter)},
 * and {@link #setWhichTaskLogs(JobConf,
 * org.apache.hadoop.mapred.TaskLogInputFormat.TaskLogs)}.
 *
 * <p>This is an amended version of the TaskLogInputFormat that is part of
 * hadoop-1199.
 * 
 * @author stack
 */
public class TaskLogInputFormat implements InputFormat {
  private final Log LOG = LogFactory.getLog(this.getClass().getName());
  
  /**
   * Used for formatting the id numbers
   * TODO: Replace with JobTracker reference.
   */
  private static NumberFormat idFormat = NumberFormat.getInstance();
  static {
    idFormat.setMinimumIntegerDigits(4);
    idFormat.setGroupingUsed(false);
  }


  private static ArchiveTaskLog.LogFilter logFilter =
      ArchiveTaskLog.LogFilter.SYSLOG;

  public static enum TaskLogs {MAP, REDUCE, BOTH}
  private static TaskLogs whichTaskLogs = TaskLogs.MAP;

  private static final String KEY_BASE = "mapred.inputformat.tasklog.";
  private static final String JOBID_KEY = KEY_BASE + "jobid";
  private static final String LOGFILTER_KEY = KEY_BASE + "logfilter";
  private static final String WHICHTASK_KEY = "KEY_BASE" + "task";

  public static void setJobid(final JobConf job, final int id) {
    job.setInt(JOBID_KEY, id);
  }

  public static int getJobid(final JobConf job) {
    return job.getInt(JOBID_KEY, -1);
  }

  public static void setLogfilter(final JobConf job,
        final ArchiveTaskLog.LogFilter lf) {
    job.set(LOGFILTER_KEY, lf);
  }

  public static ArchiveTaskLog.LogFilter getLogfilter(final JobConf job) {
    return (ArchiveTaskLog.LogFilter) job.getObject(LOGFILTER_KEY);
  }

  public static void setWhichTaskLogs(final JobConf job, final TaskLogs tl) {
    job.setObject(WHICHTASK_KEY, tl);
  }

  public static TaskLogs getWhichTaskLogs(final JobConf job) {
    return (TaskLogs) job.getObject(WHICHTASK_KEY);
  }

  public RecordReader getRecordReader(InputSplit split, JobConf job,
      Reporter reporter) throws IOException {
    final int jobid = getJobid(job);
    if (jobid <= 0) {
      throw new IOException("Set jobid");
    }

    // Filter for the userlogs directory.  Returns list of map, reduce, or both
    // map and reduce tasks.
    final FilenameFilter fnf = new FilenameFilter() {
      private final String prefix = "task_" +
          // JobTracker.idFormat.format(jobid) + "_" +
          // FIX: JobTracker.idFormat.format(jobid) + "_" +
          idFormat.format(jobid) + "_" +
          ((whichTaskLogs == TaskLogs.MAP)? "m":
            (whichTaskLogs == TaskLogs.REDUCE)? "r":
            "" /* Both map and reduce */);

      public boolean accept(File dir, String name) {
        return name.startsWith(prefix);
      }
    };

    File logDir = ArchiveTaskLog.LOG_DIR;
    if (logDir == null) {
      throw new IOException("Set hadoop.log.dir system property");
    }
    if (!logDir.exists()) {
      throw new FileNotFoundException(logDir.getAbsolutePath());
    }
    final File[] tds = logDir.listFiles(fnf);
    if (tds.length <= 0) {
      throw new FileNotFoundException("No log dirs found for jobid " + jobid);
    }
    
    // Finally, get this hosts's name to add to key.
    final String localHostname =
      DNS.getDefaultHost(job.get("mapred.tasktracker.dns.interface","default"),
          job.get("mapred.tasktracker.dns.nameserver","default"));


    return new RecordReader() {
      private String hostname = localHostname;
      private File[] userlogsDirs = tds;
      private int userlogsDirsIndex = 0;
      private LineRecordReader lrr = null;
      private long accumulatingPosition = 0;
      private String currentTask = null;
      
      public void close() throws IOException {
        if (this.lrr != null) {
          this.lrr.close();
          this.lrr = null;
        }
      }

      public WritableComparable createKey() {
        return new Text();
      }

      public Writable createValue() {
        // Values are same as those made by LineRecordReader#createValue().
        return new Text();
      }

      public long getPos() throws IOException {
        return this.accumulatingPosition +
          ((this.lrr != null)? this.lrr.getPos(): 0);
      }

      public float getProgress() throws IOException {
        return (this.lrr == null)? 0.0f:
          ((this.userlogsDirsIndex - 1 + this.lrr.getProgress()) /
            (float)this.userlogsDirs.length);
      }
      
      public boolean getNextLine(Writable key, Writable value)
      throws IOException {
        Writable lrrKey = this.lrr.createKey();
        boolean result = this.lrr.next(lrrKey, value);
        if (result) {
          // Amend key to include host and current task.
          ((Text)key).set(this.hostname + ":" + this.currentTask + ":" +
              ((LongWritable)lrrKey).toString());
        }
        return result;
      }

      public boolean next(Writable key, Writable value) throws IOException {
        if (this.lrr != null) {
          if (getNextLine(key, value)) {
            return true;
          }
          // Else, no more lines in this LineRecordReader. Close
          // and try and get another.
          this.accumulatingPosition += this.lrr.getPos();
          this.lrr.close();
          this.lrr = null;
        }
        if (this.userlogsDirsIndex >= this.userlogsDirs.length) {
          // There are no more userlogs dirs. We are done.
          return false;
        }
        this.currentTask =
            this.userlogsDirs[this.userlogsDirsIndex++].getName();
        // For now, hardcoded to read from syslog.
        ArchiveTaskLog.Reader tlr =
            new ArchiveTaskLog.Reader(this.currentTask);
        this.lrr = new LineRecordReader(tlr.getInputStream(), 0,
          tlr.getTotalLogSize());
        return getNextLine(key, value);
      }
    };
  }

  public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
    ArrayList<InputSplit> is = new ArrayList<InputSplit>(numSplits);
    for (int i = 0; i < numSplits; i++) {
      is.add(new TaskLogSplit());
    }
    return is.toArray(new InputSplit[is.size()]);
  }

  public void validateInput(JobConf job) throws IOException {
    // Nothing to validate.
  }

  public static class TaskLogSplit implements InputSplit {
    public TaskLogSplit() {
      super();
    }

    public long getLength() throws IOException {
      // Return '1' for '1' host's logs.
      return 1;
    }

    public String[] getLocations() throws IOException {
      return new String[0];
    }

    public void readFields(DataInput in) throws IOException {
      // Nothing to serialize.
    }

    public void write(DataOutput out) throws IOException {
      // Nothing to serialize.
    }
  }

  /**
   * Runs a mapreduce job that uses {@link TaskLogInputFormat} reading
   * {@link TaskLog} userlog directories.
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: TaskLogInputFormat <output> <jobid>");
      System.exit(1);
    }

    JobConf job = new JobConf(TaskLogInputFormat.class);

    job.setInputFormat(TaskLogInputFormat.class);
    TaskLogInputFormat.setJobid(job, Integer.parseInt(args[1]));
    TaskLogInputFormat.setLogfilter(job, ArchiveTaskLog.LogFilter.SYSLOG);
    TaskLogInputFormat.setWhichTaskLogs(job, TaskLogInputFormat.TaskLogs.MAP);
    job.setOutputPath(new Path(args[0]));
    job.setOutputFormat(TextOutputFormat.class);
    job.setOutputKeyClass(Text.class);

    JobClient.runJob(job);
  }
}