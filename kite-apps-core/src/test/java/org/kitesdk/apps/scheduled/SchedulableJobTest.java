package org.kitesdk.apps.scheduled;


import com.google.common.collect.Lists;
import org.apache.avro.generic.GenericData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitesdk.apps.test.TestScheduler;
import org.kitesdk.apps.test.apps.ScheduledInputOutputApp;
import org.kitesdk.data.Dataset;
import org.kitesdk.data.Datasets;
import org.kitesdk.data.MiniDFSTest;
import org.kitesdk.data.PartitionView;
import org.kitesdk.data.View;
import org.kitesdk.data.spi.DefaultConfiguration;
import org.kitesdk.data.spi.filesystem.DatasetTestUtilities;

import java.net.URI;
import java.util.List;

public class SchedulableJobTest extends MiniDFSTest {

  @Before
  public void setDefaultConfig() {
    DefaultConfiguration.set(getConfiguration());
  }

  @Test
  public void testSimpleMap() {

    TestScheduler scheduler = TestScheduler.load(ScheduledInputOutputApp.class, getConfiguration());

    Instant nominalTime = new DateTime(2015, 5, 15, 12, 0, 0, 0, DateTimeZone.UTC).toInstant();

    Dataset<GenericData.Record> in = Datasets.load(ScheduledInputOutputApp.INPUT_DATASET,
        GenericData.Record.class);

    Dataset<GenericData.Record> out = Datasets.load(ScheduledInputOutputApp.OUTPUT_DATASET,
        GenericData.Record.class);

    View<GenericData.Record> input = in.with("year", 2015)
        .with("month", 5)
        .with("day", 15)
        .with("hour", 12);

    DatasetTestUtilities.writeTestUsers(input, 10);

    scheduler.runScheduledJobs(nominalTime);

    DatasetTestUtilities.checkTestUsers(out, 10);

    // Make sure only the expected partition is present.
    List<PartitionView> partitions = Lists.newArrayList();

    for (PartitionView partition:  out.getCoveringPartitions()) {
      partitions.add(partition);
    }

    Assert.assertEquals(1, partitions.size());

    URI uri =  partitions.get(0).getUri();

    Assert.assertTrue(uri.toString().endsWith("year=2015&month=5&day=15&hour=12"));
  }
}