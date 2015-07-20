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

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

/**
 * Default spark context for the application.
 */
public class DefaultSparkContext {

  private static volatile JavaSparkContext context;

  public static void setContext(JavaSparkContext context) {
    DefaultSparkContext.context = context;
  }

  public static JavaSparkContext getContext() {
    return context;
  }


  private static volatile JavaStreamingContext streamingContext;

  public static void setStreamingContext(JavaStreamingContext context) {
    DefaultSparkContext.streamingContext = context;
  }

  public static JavaStreamingContext getStreamingContext() {
    return streamingContext;
  }

}