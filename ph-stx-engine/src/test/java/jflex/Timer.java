/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package jflex;

/**
 * Very simple timer for code generation time statistics. Not very exact,
 * measures user time, not processor time.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class Timer
{

  /* the timer stores start and stop time from currentTimeMillis() */
  private long startTime, stopTime;

  /* flag if the timer is running (if stop time is valid) */
  private boolean running;

  /**
   * Construct a new timer that starts immediatly.
   */
  public Timer ()
  {
    startTime = System.currentTimeMillis ();
    running = true;
  }

  /**
   * Start the timer. If it is already running, the old start time is lost.
   */
  public void start ()
  {
    startTime = System.currentTimeMillis ();
    running = true;
  }

  /**
   * Stop the timer.
   */
  public void stop ()
  {
    stopTime = System.currentTimeMillis ();
    running = false;
  }

  /**
   * Return the number of milliseconds the timer has been running. (up till now,
   * if it still runs, up to the stop time if it has been stopped)
   */
  public long diff ()
  {
    if (running)
      return System.currentTimeMillis () - startTime;
    else
      return stopTime - startTime;
  }

  /**
   * Return a string representation of the timer.
   *
   * @return a string displaying the diff-time in readable format (h m s ms)
   * @see Timer#diff
   */
  @Override
  public String toString ()
  {
    final long diff = diff ();

    final long millis = diff % 1000;
    final long secs = (diff / 1000) % 60;
    final long mins = (diff / (1000 * 60)) % 60;
    final long hs = (diff / (1000 * 3600)) % 24;
    final long days = diff / (1000 * 3600 * 24);

    if (days > 0)
      return days + "d " + hs + "h " + mins + "m " + secs + "s " + millis + "ms";

    if (hs > 0)
      return hs + "h " + mins + "m " + secs + "s " + millis + "ms";

    if (mins > 0)
      return mins + "m " + secs + "s " + millis + "ms";

    if (secs > 0)
      return secs + "s " + millis + "ms";

    return millis + "ms";
  }
}
