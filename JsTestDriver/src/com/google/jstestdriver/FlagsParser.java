package com.google.jstestdriver;




public interface FlagsParser {

  /**
   * Parses the Flags from a String[], throwing a exception either on '--help' or no args.
   */
  public abstract Flags parseArgument(String[] strings);

}
