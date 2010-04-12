/**
 * Copyright (c) Neill Alexander. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software
 */
package com.nwalex.sponge.gui;

import java.io.PrintStream;
import org.apache.log4j.Logger;

/**
 * A very clever idea from:
 * http://stackoverflow.com/questions/1200175/log4j-redirect-stdout-to-dailyrollingfileappender
 * @author neill
 */
public class StdOutErrLog {

  private static final Logger logger = Logger.getLogger(StdOutErrLog.class);

  public static void tieSystemOutAndErrToLog() {
    System.setOut(createLoggingProxy(System.out, false));
    System.setErr(createLoggingProxy(System.err, true));
  }

  public static PrintStream createLoggingProxy(final PrintStream realPrintStream, final boolean error) {
    return new PrintStream(realPrintStream) {
      @Override
      public void print(final String string) {
        realPrintStream.println(string);
        if (error) {
          logger.error(string);
        } else {
          logger.info(string);
        }
      }
    };
  }
}

