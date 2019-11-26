package com.mayankrastogi.cs474.hw3.framework

import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver

trait CommandResultParser[+T] {
  def parseFrom(output: String): T
}

object CommandResultParser {

  object DefaultParsers {
    implicit val unitParser: CommandResultParser[Unit] = _ => ()
    implicit val stringParser: CommandResultParser[String] = output => output
    implicit val pipeReceiverParser: CommandResultParser[PipeReceiver] = _ => new PipeReceiver {}
  }

}