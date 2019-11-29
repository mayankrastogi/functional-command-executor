package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Echo.EchoParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers.stringParser

/**
 * Provides a functional, type-safe interface for building the Unix `echo` command.
 *
 * This command returns the same `text` that was provided to it. Useful for writing to files by invoking the
 * `writeTo`/`appendTo` methods on the built command.
 */
object Echo {

  /**
   * Creates an empty echo command builder.
   *
   * @return A type-safe echo command builder.
   */
  def apply(): EchoBuilder[Empty] = EchoBuilder("", trailingNewline = true, backslashInterpretation = false)

  /**
   * Builds an echo command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.Echo]] object.
   *
   * @param string                  The string of text to echo back.
   * @param trailingNewline         Should a trailing new-line be added at the end of the output.
   * @param backslashInterpretation Should escaped characters be interpreted.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[Echo] case class EchoBuilder[I <: EchoParameters](private val string: String, private val trailingNewline: Boolean, private val backslashInterpretation: Boolean) {

    /**
     * The string of text to echo back.
     *
     * @param string The text.
     * @return A re-configured type-safe builder.
     */
    def text(string: String): EchoBuilder[I with EchoString] = copy(string = string)

    /**
     * Adds a new-line character at the end of the output if `true` (default).
     *
     * Equivalent to the `-n` option of the echo command when the flag is reset to `false`.
     *
     * @return A re-configured type-safe builder.
     */
    def addTrailingNewLine(flag: Boolean): EchoBuilder[I with TrailingNewLineFlag] = copy(trailingNewline = flag)

    /**
     * Enables interpretation of backslash escapes if set to `true`.
     *
     * Equivalent to the `-e` option of the echo command when the flag is set to `true`, or to the `-E` option when the
     * flag is reset to `false` (default).
     *
     * @return A re-configured type-safe builder.
     */
    def interpretBackslashEscapes(flag: Boolean): EchoBuilder[I with BackslashInterpretationFlag] = copy(backslashInterpretation = flag)

    /**
     * Builds the `echo` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that outputs the same [[String]] that was passed to it.
     */
    def build(implicit ev: I <:< MandatoryParameters): Command[String] =
      Command(s"echo ${if (trailingNewline) "" else "-n "}${if (backslashInterpretation) "-e" else "-E"} '$string'")
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object EchoParameters {

    type MandatoryParameters = Empty with EchoString

    sealed trait EchoParameters

    sealed trait Empty extends EchoParameters

    sealed trait EchoString extends EchoParameters

    sealed trait TrailingNewLineFlag extends EchoParameters

    sealed trait BackslashInterpretationFlag extends EchoParameters

  }

}