package com.mayankrastogi.cs474.hw3.framework

import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._
import com.typesafe.scalalogging.LazyLogging

import scala.sys.process._
import scala.util.Try

case class Command[T](cmd: String,
                      private[framework] val fragments: Seq[Command[_]] = Seq.empty,
                      private[framework] val redirectsOutput: Boolean = false)
                     (implicit val parser: CommandResultParser[T])
  extends LazyLogging {

  def andThen[U](that: Command[U]): Command[U] = that.copy(fragments = this.fragments ++ Seq(this))(that.parser)

  def pipeTo(that: Command[_ <: PipeReceiver]): Command[T] = this.copy(cmd = this.cmd + " | " + that.cmd)

  def writeTo(filePath: String): Command[Unit] = this.copy(cmd = s"$cmd > '$filePath'", redirectsOutput = true)

  def appendTo(filePath: String): Command[Unit] = this.copy(cmd = s"$cmd >> '$filePath'", redirectsOutput = true)

  def execute: Either[Throwable, T] = {
    val shellPrefix = if (Command.isWindows) "wsl " else ""
    val previousCommands =
      if (fragments.nonEmpty)
        fragments.map(c => if (c.redirectsOutput) c.cmd else c.cmd + " > /dev/null").mkString(" && ") + " && "
      else ""

    val command = shellPrefix + "bash -c \"" + previousCommands + this.cmd + "\""
    logger.debug(s"Executing command: $command")

    Try(parser.parseFrom(command.!!)).toEither
  }

  override def toString: String = (fragments.map(_.toString) ++ Seq(cmd)).mkString(" && ")
}

object Command {
  val isWindows: Boolean = System.getProperty("os.name").toLowerCase.startsWith("windows")

  trait PipeReceiver

}