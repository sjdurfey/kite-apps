package org.kitesdk.apps.examples.spark;

import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.kitesdk.apps.example.event.ExampleEvent;
import org.kitesdk.apps.scheduled.DataIn;
import org.kitesdk.apps.scheduled.DataOut;
import org.kitesdk.data.View;
import org.kitesdk.data.mapreduce.DatasetKeyInputFormat;
import org.kitesdk.data.mapreduce.DatasetKeyOutputFormat;
import org.kitsdk.apps.spark.AbstractSchedulableSparkJob;
import scala.Tuple2;

import java.io.IOException;

/**
 * Simple job that uses a Crunch pipeline to filter out a set of users.
 */
public class SparkJob extends AbstractSchedulableSparkJob {

  @Override
  public String getName() {
    return "example-spark";
  }

  public static class KeepOddUsers implements Function<Tuple2<ExampleEvent, Void>, Boolean> {

    @Override
    public Boolean call(Tuple2<ExampleEvent, Void> input) throws Exception {
      return input._1().getUserId() % 2 == 1;
    }
  }

  public void run(@DataIn(name="example.events", type=ExampleEvent.class) View<ExampleEvent> input,
                  @DataOut(name="odd.users", type=ExampleEvent.class) View<ExampleEvent> output) throws IOException {

    Job job = Job.getInstance(getConf());
    DatasetKeyInputFormat.configure(job).readFrom(input);
    DatasetKeyOutputFormat.configure(job).writeTo(output);

    JavaPairRDD<ExampleEvent, Void> inputData = getContext()
        .newAPIHadoopRDD(job.getConfiguration(), DatasetKeyInputFormat.class,
            ExampleEvent.class, Void.class);

    JavaPairRDD<ExampleEvent, Void> filteredData = inputData.filter(new KeepOddUsers());

    filteredData.saveAsNewAPIHadoopDataset(job.getConfiguration());
  }
}