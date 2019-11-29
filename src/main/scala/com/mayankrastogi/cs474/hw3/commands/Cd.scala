package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Cd.CdParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

/**
 * Provides a functional, type-safe interface for building the Unix `cd` command.
 *
 * This command can be used to change the working directory of the subsequent commands.
 */
object Cd {

  /**
   * Creates an empty cd command builder.
   *
   * @return A type-safe cd command builder.
   */
  def apply(): CdBuilder[Empty] = CdBuilder(".")

  /**
   * Builds a cd command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.Cd]] object.
   *
   * @param path The path to which the the working directory is to be switched.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[Cd] case class CdBuilder[I <: CdParameters](private val path: String) {

    /**
     * Switch the working directory to the logged-in user's home directory.
     *
     * @return A re-configured type-safe builder.
     */
    def home: CdBuilder[I with Path] = CdBuilder("")

    /**
     * Switch the working directory to the specified path.
     *
     * @param path The path.
     * @return A re-configured type-safe builder.
     */
    def path(path: String): CdBuilder[I with Path] = CdBuilder(path)

    /**
     * Builds the `cd` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that switches the working directory to the specified path and does not produce any output.
     */
    def build(implicit ev: I =:= MandatoryParameters): Command[Unit] = Command("cd " + path)
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object CdParameters {

    type MandatoryParameters = Empty with Path

    sealed trait CdParameters

    sealed trait Empty extends CdParameters

    sealed trait Path extends CdParameters

  }

}