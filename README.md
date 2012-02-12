Hadoop-LZO
==========

Hadoop-LZO is a project to bring splittable LZO compression to Hadoop.  LZO is an ideal compression format for Hadoop due to its combination of speed and compression size.  However, LZO files are not natively splittable, meaning the parallelism that is the core of Hadoop is gone.  This project re-enables that parallelism with LZO compressed files, and also comes with standard utilities (input/output streams, etc) for working with LZO files.

### Origins

This project is built out of the effort of many individuals:

- Chris Douglas, Hong Tang, Owen O'Malley contributed the original work at [http://code.google.com/a/apache-extras.org/p/hadoop-gpl-compression/](http://code.google.com/a/apache-extras.org/p/hadoop-gpl-compression/)
- Angus He, Dmitriy Ryaboy, Ilya Maykov, James Seigel, Jingguo Yao, Kevin Weil, Mark H. Butler, Michael G. Noll, Raghu Angadi, Todd Lipcon, Travis Crawford contributed the work on fixing lzop compatibility and splittable lzop. [https://github.com/kevinweil/hadoop-lzo](https://github.com/kevinweil/hadoop-lzo)
- Shevek contributed the work to port lzo into Java rather than use JNI [https://github.com/Karmasphere/lzo-java](https://github.com/Karmasphere/lzo-java)
- Owen O'Malley integrated the forks and switched to Maven [https://github.com/hortonworks/hadoop-lzo](https://github.com/hortonworks/hadoop-lzo)

### Hadoop and LZO, Together at Last

LZO is a wonderful compression scheme to use with Hadoop because it's incredibly fast, and (with a bit of work) it's splittable.  Gzip is decently fast, but cannot take advantage of Hadoop's natural map splits because it's impossible to start decompressing a gzip stream starting at a random offset in the file.  LZO's block format makes it possible to start decompressing at certain specific offsets of the file -- those that start new LZO block boundaries.  In addition to providing LZO decompression support, these classes provide an in-process indexer (com.hadoop.compression.lzo.LzoIndexer) and a map-reduce style indexer which will read a set of LZO files and output the offsets of LZO block boundaries that occur near the natural Hadoop block boundaries.  This enables a large LZO file to be split into multiple mappers and processed in parallel.  Because it is compressed, less data is read off disk, minimizing the number of IOPS required.  And LZO decompression is so fast that the CPU stays ahead of the disk read, so there is no performance impact from having to decompress data as it's read off disk.

### Building and Configuring

To get started, compile this project using 'mvn clean package'. The jar is also available from Maven central.

You can read more about Hadoop, LZO, and how Twitter is using it at [http://engineering.twitter.com/2010/04/hadoop-at-twitter.html](http://engineering.twitter.com/2010/04/hadoop-at-twitter.html).

Once the jar is built and installed, you may want to add them to the class paths.  That is, in hadoop-env.sh, set

        export HADOOP_CLASSPATH=/path/to/your/hadoop-lzo.jar

Make sure you restart your jobtrackers and tasktrackers after uploading and changing configs so that they take effect.

### Using Hadoop and LZO

#### Reading and Writing LZO Data
The project provides LzoInputStream and LzoOutputStream wrapping regular streams, to allow you to easily read and write compressed LZO data.  

#### Indexing LZO Files

At this point, you should also be able to use the indexer to index lzo files in Hadoop (recall: this makes them splittable, so that they can be analyzed in parallel in a mapreduce job).  Imagine that big_file.lzo is a 1 GB LZO file. You have three options:

- index it in-process via:

        hadoop jar /path/to/your/hadoop-lzo.jar com.hadoop.compression.lzo.LzoIndexer big_file.lzo

- index it in a map-reduce job via:

        hadoop jar /path/to/your/hadoop-lzo.jar com.hadoop.compression.lzo.DistributedLzoIndexer big_file.lzo

- index the files as they are written by configuring LzoTextOutputFormat

Either way, after 10-20 seconds there will be a file named big_file.lzo.index.  The newly-created index file tells the LzoTextInputFormat's getSplits function how to break the LZO file into splits that can be decompressed and processed in parallel.  Alternatively, if you specify a directory instead of a filename, both indexers will recursively walk the directory structure looking for .lzo files, indexing any that do not already have corresponding .lzo.index files.

#### Running MR Jobs over Indexed Files

Now run any job, say wordcount, over the new file.  In Java-based M/R jobs, just replace any uses of TextInputFormat by LzoTextInputFormat.  In streaming jobs, add "-inputformat com.hadoop.mapred.DeprecatedLzoTextInputFormat" (streaming still uses the old APIs, and needs a class that inherits from org.apache.hadoop.mapred.InputFormat).  For Pig jobs, email me or check the pig list -- I have custom LZO loader classes that work but are not (yet) contributed back.

Note that if you forget to index an .lzo file, the job will work but will process the entire file in a single split, which will be less efficient.
