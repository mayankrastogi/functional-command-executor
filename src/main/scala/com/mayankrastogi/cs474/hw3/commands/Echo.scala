package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Echo.EchoParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers.stringParser

object Echo {

  def apply(): EchoBuilder[Empty] = EchoBuilder("", trailingNewline = true, backslashInterpretation = false)

  case class EchoBuilder[I <: EchoParameters](private val string: String, private val trailingNewline: Boolean, private val backslashInterpretation: Boolean) {

    def text(string: String): EchoBuilder[I with EchoString] = copy(string = string)

    def addTrailingNewLine(flag: Boolean): EchoBuilder[I with TrailingNewLineFlag] = copy(trailingNewline = flag)

    def interpretBackslashEscapes(flag: Boolean): EchoBuilder[I with BackslashInterpretationFlag] = copy(backslashInterpretation = flag)

    def build(implicit ev: I <:< MandatoryParameters): Command[String] =
      Command(s"echo ${if (trailingNewline) "" else "-n "}${if (backslashInterpretation) "-e" else "-E"} '$string'")
  }

  sealed trait EchoParameters

  object EchoParameters {
    type MandatoryParameters = Empty with EchoString

    sealed trait Empty extends EchoParameters

    sealed trait EchoString extends EchoParameters

    sealed trait TrailingNewLineFlag extends EchoParameters

    sealed trait BackslashInterpretationFlag extends EchoParameters

  }

}