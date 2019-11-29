package com.mayankrastogi.cs474.hw3.framework

import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver

/**
 * Defines how to parse the output received by executing a [[com.mayankrastogi.cs474.hw3.framework.Command]].
 *
 * @tparam T The type to which the output will be parsed to.
 */
trait CommandResultParser[+T] {
  /**
   * Parses the output received by executing a [[com.mayankrastogi.cs474.hw3.framework.Command]].
   *
   * @param output The output received by executing a `Command`.
   * @return The parsed output.
   */
  def parseFrom(output: String): T
}

/**
 * Companion object to [[com.mayankrastogi.cs474.hw3.framework.CommandResultParser]].
 */
object CommandResultParser {

  /**
   * Provides implicit parsers for Commands of type `Unit`, `String`, and `PipeReceiver`.
   */
  object DefaultParsers {

    /**
     * A dummy pipe receiver implementation that does nothing.
     */
    final object DummyPipeReceiver extends PipeReceiver

    /**
     * Parses string output to nothing by doing nothing.
     */
    implicit val unitParser: CommandResultParser[Unit] = _ => ()
    /**
     * Parses string output to the same string.
     */
    implicit val stringParser: CommandResultParser[String] = output => output
    /**
     * Parses string output to a dummy pipe receiver that does nothing.
     */
    implicit val pipeReceiverParser: CommandResultParser[PipeReceiver] = _ => DummyPipeReceiver
  }

}