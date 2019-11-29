package com.mayankrastogi.cs474.hw3.framework

import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._
import com.typesafe.scalalogging.LazyLogging

import scala.sys.process._
import scala.util.Try

/**
 * Provides a functional interface for executing Unix commands using the Bourne-again shell (bash).
 * <p>
 * Supports conditional execution of multiple commands using the exit code for the previous command in the chain. A
 * functional interface for the `&&` conditional is provided using the `andThen()` method. When adding a command using
 * `andThen()`, any output to the <b>standard output</b>, produced by the commands prior to the last command, will be
 * suppressed. The framework design assumes that the user will always be interested in the output of the last command
 * executed and that all the previous commands do the job of setting up the environment required for the last command
 * to work, e.g. creating necessary files and directories using the [[com.mayankrastogi.cs474.hw3.commands.MkDir]]
 * command, changing the working directory using the [[com.mayankrastogi.cs474.hw3.commands.Cd]], etc.
 * <p>
 * Likewise, piping is supported using the `pipeTo` method. The framework design assumes that the piped receiver (the
 * command to which the output of the previous command is piped), will perform some operations on its input and produce
 * an output that leaves the structure of the input command intact, e.g. [[com.mayankrastogi.cs474.hw3.commands.Grep]]
 * and [[com.mayankrastogi.cs474.hw3.commands.Sort]] either filter or re-order the output of the input command while
 * leaving the format of individual lines intact. This allows us to filter the output of
 * [[com.mayankrastogi.cs474.hw3.commands.ls.Ls]] command using [[com.mayankrastogi.cs474.hw3.commands.Grep]] and obtain
 * a list of [[com.mayankrastogi.cs474.hw3.commands.ls.LsResultItem]]s so that the user may extract the desired
 * information from the output in a type-safe way.
 * <p>
 * The framework also supports redirection of standard output to files using the `writeTo` and `appendTo` methods. This
 * allows creation of files using the [[com.mayankrastogi.cs474.hw3.commands.Echo]] command.
 * <p>
 * The final execution is triggered by the `execute` method which prepares the final command for execution, executes it
 * using `bash`, and returns [[scala.util.Either]] an error if the command execution fails, or an output of type
 * specified in the type parameter of the [[com.mayankrastogi.cs474.hw3.framework.Command]]. The command that will be
 * executed, can be obtained using the `toString` method.
 * <p>
 * If a command executes successfully, everything written to the standard output during the execution, will be sent to a
 * [[com.mayankrastogi.cs474.hw3.framework.CommandResultParser]] to parse the [[String]] output to the type specified in
 * the type parameter of the Command. Default implicit parsers for [[scala.Unit]], [[String]], and
 * [[com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver]] are provided in
 * [[com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers]].
 * <p>
 * Apart from the in-built commands provided in the `com.mayankrastogi.cs474.hw3.commands` package, a user can run
 * arbitrary commands using `Command[Unit]("a_command")`, if the command is expected to produce no output (or the output
 * is not interesting), or by using `Command[String]("a_command")` for a command that produces string output. The
 * parsers for these commands can be provided implicitly by adding
 * `import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._`.
 *
 * @note If the JVM is running on Microsoft Windows, command execution is delegated to Bash via the Windows Subsystem
 *       for Linux (WSL). The framework will not work if WSL is not installed on Windows.
 * @param cmd             The Unix command to execute.
 * @param fragments       All commands that will be executed prior to running this command. This is meant for private
 *                        use of the framework and hence access is `private`.
 * @param redirectsOutput Flags whether this command redirects standard output to a file. This flag should be set to
 *                        `true` to avoid writing blank files. This is set automatically by `writeTo` and `appendTo`
 *                        methods, and hence access is `private`.
 * @param parser          The parser to parse the output received during command execution. Can be passed implicitly.
 * @tparam T The type of output expected from the execution of this command. A `parser` needs to be supplied for parsing
 *           to this type.
 */
case class Command[T](cmd: String,
                      private[framework] val fragments: Seq[Command[_]] = Seq.empty,
                      private[framework] val redirectsOutput: Boolean = false)
                     (implicit val parser: CommandResultParser[T])
  extends LazyLogging {

  /**
   * Chain the supplied command for execution after this command executes successfully.
   *
   * If the supplied command has commands configured to be executed prior to executing that command, all those commands
   * will get chained for execution after this command.
   *
   * @note This will suppress any output written to the standard output by this command. Redirection to files will still
   *       work if `redirectsOutput` flag is set. This flag is set automatically if `writeTo` or `appendTo` methods were
   *       used to do so.
   * @param that The next command to execute.
   * @tparam U The output type of the supplied command.
   * @return A new Command that is configured to execute the current command and then the supplied command. The output
   *         type gets changed to the output type of the supplied command.
   */
  def andThen[U](that: Command[U]): Command[U] =
    that.copy(fragments = this.fragments ++ Seq(this) ++ that.fragments)(that.parser)

  /**
   * Pipes the output of this command as an input to the supplied command.
   *
   * The supplied command is required to have its output type specified as
   * [[com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver]]. The supplied command should produce an output that
   * has the same format as this command, i.e. that output can be parsed using this command's parser.
   *
   * @note This will discard any commands that have been configured to execute prior to the supplied command.
   * @param that The command to which the output of this command will piped to as input. Must have output type as
   *             [[com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver]].
   * @return A new command that is configured to pipe the output of this command to the input of the supplied command.
   *         The output type remains the same as this command.
   */
  def pipeTo(that: Command[_ <: PipeReceiver]): Command[T] = this.copy(cmd = this.cmd + " | " + that.cmd)

  /**
   * Redirects standard output of this command to the specified file.
   *
   * The file gets created if it doesn't exist, or <b>overwritten</b> if it does.
   *
   * @param filePath The file to write to.
   * @return A command that is configured to write the output produced by this command to the specified file. The output
   *         type gets changed to [[scala.Unit]] since the command does not write to the standard output anymore.
   */
  def writeTo(filePath: String): Command[Unit] = this.copy(cmd = s"$cmd > '$filePath'", redirectsOutput = true)

  /**
   * Redirects standard output of this command to the specified file.
   *
   * The file gets created if it doesn't exist, or gets <b>appended</b> to if it does.
   *
   * @param filePath The file to append to.
   * @return A command that is configured to append the output produced by this command to the specified file.
   *         The output type gets changed to [[scala.Unit]] since the command does not write to the standard output
   *         anymore.
   */
  def appendTo(filePath: String): Command[Unit] = this.copy(cmd = s"$cmd >> '$filePath'", redirectsOutput = true)

  /**
   * Builds and executes the chain of commands configured.
   *
   * Everything written to the standard output during the execution is passed to the parser for this command. If the
   * command executes successfully (and the output also gets parsed successfully), the parsed result will be available
   * in [[scala.util.Right]]. Any exception raised during the execution, or parsing, will be available in
   * [[scala.util.Left]] which indicates failure.
   *
   * @return [[scala.util.Either]] the parsed output from the result of execution in `Right`, or the error in `Left`.
   */
  def execute: Either[Throwable, T] = {
    val command = this.toString
    logger.debug(s"Executing command: $command")

    Try(parser.parseFrom(command.!!)).toEither
  }

  /**
   * Builds the complete command that will be executed when `execute` is called.
   *
   * @return The built command.
   */
  override def toString: String = {
    val shellPrefix = if (Command.isWindows) "wsl " else ""
    val previousCommands =
      if (fragments.nonEmpty)
        fragments.map(c => if (c.redirectsOutput) c.cmd else c.cmd + " > /dev/null").mkString(" && ") + " && "
      else ""

    shellPrefix + "bash -c \"" + previousCommands + this.cmd + "\""
  }
}

/**
 * Companion object for the [[com.mayankrastogi.cs474.hw3.framework.Command]].
 */
object Command {
  /**
   * Tests whether the JVM is running on Microsoft Windows, using the system property `"os.name"`.
   */
  private[framework] val isWindows: Boolean = System.getProperty("os.name").toLowerCase.startsWith("windows")

  /**
   * Denotes that a command can receive input from standard input when passed to `pipeTo` method.
   */
  trait PipeReceiver

}