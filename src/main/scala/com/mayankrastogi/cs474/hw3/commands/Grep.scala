package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Grep.GrepParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

/**
 * Provides a functional, type-safe interface for building the Unix `grep` command.
 *
 * This command can be used to filter lines of text in a single or multiple files that match the supplied pattern. This
 * command can also be a candidate for the `Command.pipeTo()` method when built using the `buildForPipe()` method.
 */
object Grep {

  /**
   * Creates an empty grep command builder.
   *
   * @return A type-safe grep command builder.
   */
  def apply(): GrepBuilder[Empty] = GrepBuilder("", Seq.empty, Seq.empty)

  /**
   * Builds a grep command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.Grep]] object.
   *
   * @param pattern The pattern to match in the supplied `files`.
   * @param files   A sequence of files (paths) that will be read for pattern matching.
   * @param options The options for the grep command.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[Grep] case class GrepBuilder[I <: GrepParameters](private val pattern: String, private val files: Seq[String], private val options: Seq[String]) {

    /**
     * The pattern to match in the supplied files or the piped input.
     *
     * @param pattern The pattern.
     * @return A re-configured type-safe builder.
     */
    def pattern(pattern: String): GrepBuilder[I with Pattern] = copy(pattern = pattern)

    /**
     * Add a file to be read as input.
     *
     * This appends to the list of files that have already been added previously.
     *
     * @param path The path to the file.
     * @return A re-configured type-safe builder.
     */
    def addFile(path: String): GrepBuilder[I with FilePath] = addAllFiles(Seq(path))

    /**
     * Add multiple files to be read as input.
     *
     * This appends to the list of files that have already been added previously.
     *
     * @param filePaths The paths to the files.
     * @return A re-configured type-safe builder.
     */
    def addAllFiles(filePaths: Seq[String]): GrepBuilder[I with FilePath] = {
      val existingFiles = if (files == null) Seq.empty else files
      copy(files = existingFiles ++ filePaths)
    }

    /**
     * Perform a case-insensitive matching of the pattern.
     *
     * Equivalent to the `-i` option of the grep command.
     *
     * @return A re-configured type-safe builder.
     */
    def ignoreCase: GrepBuilder[I] = copy(options = options ++ Seq("-i"))

    /**
     * Output the lines that DO NOT match the supplied pattern.
     *
     * Equivalent to the `-v` option of the grep command.
     *
     * @return A re-configured type-safe builder.
     */
    def invertMatch: GrepBuilder[I] = copy(options = options ++ Seq("-v"))

    /**
     * Builds the `grep` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that reads the supplied files and filters the lines that match the supplied pattern.
     */
    def build(implicit ev: I =:= WithoutPipeParameters): Command[String] =
      Command[String](s"grep $pattern ${options.mkString(" ")} ${files.map("'" + _ + "'").mkString(" ")}".strip())

    /**
     * Builds the `grep` command that can be passed to the `Command.pipeTo()` method. Requires that any input file is
     * NOT specified.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that reads the standard input and filters the lines that match the supplied pattern.
     */
    def buildForPipe(implicit ev: I =:= WithPipeParameters): Command[PipeReceiver] =
      Command[PipeReceiver](s"grep '$pattern' ${options.mkString(" ")}".strip())
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object GrepParameters {

    type WithoutPipeParameters = Empty with Pattern with FilePath
    type WithPipeParameters = Empty with Pattern

    sealed trait GrepParameters

    sealed trait Empty extends GrepParameters

    sealed trait Pattern extends GrepParameters

    sealed trait FilePath extends GrepParameters

  }

}