package com.mayankrastogi.cs474.hw3.framework.tests

import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._
import com.mayankrastogi.cs474.hw3.framework.{Command, Do}
import org.scalatest.{FunSuite, Matchers}

class DoTest extends FunSuite with Matchers {

  test("A Do() must return the same command that was passed to it") {
    val unitCommand = Command[Unit]("unitCommand")
    Do(unitCommand) should be theSameInstanceAs unitCommand

    val stringCommand = Command[Unit]("stringCommand")
    Do(stringCommand) should be theSameInstanceAs stringCommand
  }
}
