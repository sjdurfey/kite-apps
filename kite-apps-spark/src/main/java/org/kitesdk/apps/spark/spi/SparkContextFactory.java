/**
 * Copyright 2015 Cerner Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kitesdk.apps.spark.spi;

import com.google.common.collect.ImmutableMap;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.kitesdk.apps.AppContext;
import org.kitesdk.apps.AppException;

import java.util.Map;

/**
 * Factor to create Spark contexts from application context. This is necessary
 * since Spark does not yet all multiple contexts in a JVM. See
 * https://issues.apache.org/jira/browse/SPARK-2243 for details.
 */
public class SparkContextFactory {

  private static Map<String,String> appSettings = null;

  private static JavaSparkContext sparkContext = null;

  private static JavaStreamingContext streamingContext = null;

  private static SparkConf createConf(AppContext context) {

    SparkConf conf = new SparkConf().setAppName("FIXME_PLACEHOLDER");

    for (Map.Entry<String,String> entry: context.getSettings().entrySet()) {

      if (entry.getKey().startsWith("spark.")) {

        conf.set(entry.getKey(), entry.getValue());
      }
    }

    return conf;
  }

  public static synchronized JavaSparkContext getSparkContext(AppContext context) {

    if (sparkContext == null) {

      appSettings = ImmutableMap.<String,String>builder()
          .putAll(context.getSettings())
          .build();

      SparkConf conf = createConf(context);

      sparkContext = new JavaSparkContext(conf);
    } else {

      // Check to see if the settings are compatible.
      if (!appSettings.equals(context.getSettings()))
        throw new AppException("Can only create a Spark context for one collection of settings. See SPARK-2243.");

    }

    return sparkContext;
  }

  public static synchronized JavaStreamingContext getStreamingContext(AppContext context) {


    if (streamingContext == null) {

      streamingContext = new JavaStreamingContext(getSparkContext(context), new Duration(1000));
    } else {

      // Check to see if the settings are compatible.
      if (!appSettings.equals(context.getSettings()))
        throw new AppException("Can only create a Spark context for one collection of settings. See SPARK-2243.");

    }

    return streamingContext;
  }

  public static synchronized void shutdown() {

    if (streamingContext != null) {
      // FIXME: attempts at a graceful shutdown seem to block
      // indefinitely. Therefore we shut down on a separate thread
      // and timeout so we can make progress on tests.
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

          streamingContext.stop(true,true);
        }
      });

      thread.setDaemon(true);
      thread.start();

      streamingContext.awaitTermination(5000);
    }

    if (sparkContext != null) {
      sparkContext.stop();
    }

    // Remove the context now that we've been shut down.
    appSettings = null;
    sparkContext = null;
    streamingContext = null;
  }
}
